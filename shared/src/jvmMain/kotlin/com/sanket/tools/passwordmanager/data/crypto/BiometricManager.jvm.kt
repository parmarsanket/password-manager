package com.sanket.tools.passwordmanager.data.crypto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Base64
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

actual class BiometricManager {

    @Volatile
    private var cachedAvailability: Boolean? = null

    /**
     * Tracks which verification approach works on this machine so we don't
     * waste time retrying the wrong one on every call.
     */
    @Volatile
    private var preferredMethod: VerificationMethod = VerificationMethod.UNKNOWN

    actual fun shouldOfferAuthentication(): Boolean = isWindows() || isLinux() || isMac()

    actual fun canAuthenticate(): Boolean {
        if (isWindows()) {
            cachedAvailability?.let { return it }

            val isAvailable = runCatching {
                executePowerShell(buildAvailabilityScript()).equals("Available", ignoreCase = true)
            }.getOrDefault(false)

            cachedAvailability = isAvailable
            return isAvailable
        }

        if (isLinux()) {
            cachedAvailability?.let { return it }
            val isAvailable = isPkexecAvailable()
            cachedAvailability = isAvailable
            return isAvailable
        }

        if (isMac()) {
            cachedAvailability?.let { return it }
            // On Mac, we check if the 'swift' command is available to run our auth script
            val isAvailable = isSwiftAvailable()
            cachedAvailability = isAvailable
            return isAvailable
        }

        return false
    }

    actual fun authenticationLabel(): String {
        return when {
            isWindows() -> "Windows Hello"
            isLinux() -> "System Authentication"
            isMac() -> "Touch ID"
            else -> "Biometric"
        }
    }

    actual suspend fun authenticate(title: String, subtitle: String): AuthResult {
        if (isWindows()) {
            return withContext(Dispatchers.Default) {
                val promptMessage = listOf(title.trim(), subtitle.trim())
                    .filter { it.isNotBlank() }
                    .joinToString(System.lineSeparator())

                val result = runVerification(promptMessage)
                    ?: return@withContext AuthResult.Failure("Windows Hello is unavailable right now.")

                when (result) {
                    "Verified" -> {
                        cachedAvailability = true
                        AuthResult.Success
                    }

                    "Canceled" -> AuthResult.Canceled
                    RESULT_TIMEOUT -> AuthResult.Failure("Windows Hello did not complete. Try again.")
                    "RetriesExhausted" -> AuthResult.Failure("Too many failed attempts. Try again in a moment.")
                    "DeviceBusy" -> AuthResult.Failure("Windows Hello is busy. Try again.")
                    "DisabledByPolicy" -> AuthResult.Failure("Windows Hello is disabled by policy on this device.")
                    "NotConfiguredForUser" -> {
                        cachedAvailability = false
                        AuthResult.Failure("Set up Windows Hello PIN or fingerprint in Windows settings first.")
                    }

                    "DeviceNotPresent" -> {
                        cachedAvailability = false
                        AuthResult.NotAvailable
                    }

                    else -> AuthResult.Failure(unexpectedFailureMessage(result))
                }
            }
        }

        if (isLinux()) {
            return withContext(Dispatchers.IO) {
                try {
                    // Trigger system authentication dialog via pkexec.
                    val process = ProcessBuilder("pkexec", "true").start()
                    val exitCode = process.waitFor()

                    if (exitCode == 0) {
                        AuthResult.Success
                    } else {
                        AuthResult.Canceled
                    }
                } catch (e: Exception) {
                    AuthResult.Failure("System authentication failed: ${e.message}")
                }
            }
        }

        if (isMac()) {
            return withContext(Dispatchers.IO) {
                try {
                    val promptMessage = listOf(title, subtitle).filter { it.isNotBlank() }.joinToString(": ")
                    // We use a small Swift script to access the LocalAuthentication framework.
                    // This supports Touch ID, Face ID, and Apple Watch.
                    val script = """
                        import LocalAuthentication
                        import Foundation
                        let context = LAContext()
                        var error: NSError?
                        if context.canEvaluatePolicy(.deviceOwnerAuthentication, error: &error) {
                            context.evaluatePolicy(.deviceOwnerAuthentication, localizedReason: "$promptMessage") { success, _ in
                                exit(success ? 0 : 1)
                            }
                            RunLoop.main.run()
                        } else {
                            exit(2)
                        }
                    """.trimIndent()

                    val process = ProcessBuilder("swift", "-").start()
                    process.outputStream.use { it.write(script.toByteArray()) }
                    
                    val exitCode = process.waitFor()
                    when (exitCode) {
                        0 -> AuthResult.Success
                        1 -> AuthResult.Canceled
                        2 -> AuthResult.Failure("Touch ID is not available or not configured.")
                        else -> AuthResult.Failure("macOS authentication failed.")
                    }
                } catch (e: Exception) {
                    AuthResult.Failure("macOS authentication failed: ${e.message}")
                }
            }
        }

        return AuthResult.NotAvailable
    }


    private fun runVerification(promptMessage: String): String? {
        // If we already know which method works, use it directly (skip the try/fallback loop).
        when (preferredMethod) {
            VerificationMethod.DESKTOP -> {
                val result = runCatching {
                    executePowerShell(buildDesktopVerificationScript(promptMessage))
                }.getOrNull()
                if (!result.isNullOrBlank() && result != RESULT_FALLBACK && !result.startsWith("Failed:")) {
                    return result
                }
                // Desktop stopped working – reset and fall through to legacy
                preferredMethod = VerificationMethod.UNKNOWN
            }
            VerificationMethod.LEGACY -> {
                return runCatching {
                    executePowerShell(buildLegacyVerificationScript(promptMessage))
                }.getOrNull()
            }
            VerificationMethod.UNKNOWN -> { /* try desktop then legacy */ }
        }

        val desktopResult = runCatching {
            executePowerShell(buildDesktopVerificationScript(promptMessage))
        }.getOrNull()

        val finalResult = when {
            desktopResult.isNullOrBlank() -> {
                val legacyResult = runCatching {
                    executePowerShell(buildLegacyVerificationScript(promptMessage))
                }.getOrNull()
                if (!legacyResult.isNullOrBlank()) preferredMethod = VerificationMethod.LEGACY
                legacyResult
            }

            desktopResult == RESULT_FALLBACK ||
                desktopResult == RESULT_TIMEOUT ||
                desktopResult.startsWith("Failed:") -> {
                val legacyResult = runCatching {
                    executePowerShell(buildLegacyVerificationScript(promptMessage))
                }.getOrNull()
                if (!legacyResult.isNullOrBlank()) preferredMethod = VerificationMethod.LEGACY
                legacyResult ?: desktopResult
            }

            else -> {
                preferredMethod = VerificationMethod.DESKTOP
                desktopResult
            }
        }

        return finalResult
    }

    private fun isWindows(): Boolean {
        return System.getProperty("os.name")
            ?.startsWith("Windows", ignoreCase = true) == true
    }

    private fun isLinux(): Boolean {
        return System.getProperty("os.name")
            ?.startsWith("Linux", ignoreCase = true) == true
    }

    private fun isMac(): Boolean {
        return System.getProperty("os.name")
            ?.startsWith("Mac", ignoreCase = true) == true
    }

    private fun isPkexecAvailable(): Boolean {
        return try {
            val process = ProcessBuilder("which", "pkexec").start()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun isSwiftAvailable(): Boolean {
        return try {
            val process = ProcessBuilder("which", "swift").start()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun executePowerShell(script: String): String {
        val preparedScript = prepareScript(script)
        val encodedScript = Base64.getEncoder()
            .encodeToString(preparedScript.toByteArray(Charsets.UTF_16LE))

        val process = ProcessBuilder(
            "powershell.exe",
            "-NoProfile",
            "-NonInteractive",
            "-WindowStyle",
            "Hidden",
            "-EncodedCommand",
            encodedScript
        )
            .start()

        // Read stdout and stderr in parallel to prevent deadlocks on large output
        val stdoutFuture = CompletableFuture.supplyAsync {
            process.inputStream.bufferedReader().use { it.readText() }
        }
        val stderrFuture = CompletableFuture.supplyAsync {
            process.errorStream.bufferedReader().use { it.readText() }
        }

        val exitCode = process.waitFor()
        val stdout = stdoutFuture.get(5, TimeUnit.SECONDS)
        val stderr = stderrFuture.get(5, TimeUnit.SECONDS)
        val output = sanitizePowerShellOutput(stdout)
        val errorOutput = sanitizePowerShellOutput(stderr)
        if (exitCode != 0) {
            throw IllegalStateException(
                errorOutput.ifBlank {
                    output.ifBlank { "PowerShell exited with code $exitCode." }
                }
            )
        }

        return output.ifBlank { errorOutput }
    }

    private fun prepareScript(script: String): String = buildString {
        val ps = '$'
        appendLine("${ps}ProgressPreference = 'SilentlyContinue'")
        appendLine("${ps}InformationPreference = 'SilentlyContinue'")
        appendLine("${ps}WarningPreference = 'SilentlyContinue'")
        appendLine("${ps}VerbosePreference = 'SilentlyContinue'")
        appendLine("${ps}ErrorActionPreference = 'Stop'")
        appendLine("[Console]::OutputEncoding = [System.Text.Encoding]::UTF8")
        appendLine(script)
    }

    private fun sanitizePowerShellOutput(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return ""
        if (trimmed.contains("#< CLIXML") || trimmed.contains("<Objs Version=\"1.1.0.1\"")) {
            return ""
        }

        return trimmed.lineSequence()
            .map { it.trim() }
            .filter { line ->
                line.isNotBlank() &&
                    !line.startsWith("#< CLIXML") &&
                    !line.startsWith("<Objs") &&
                    !line.startsWith("<Obj ") &&
                    !line.startsWith("<TN") &&
                    !line.startsWith("<T>") &&
                    !line.startsWith("<MS>") &&
                    !line.startsWith("<PR ") &&
                    !line.startsWith("<AV>") &&
                    !line.startsWith("</")
            }
            .joinToString(" ")
            .trim()
    }

    private fun unexpectedFailureMessage(result: String): String {
        val sanitized = sanitizePowerShellOutput(result)
        return if (sanitized.isBlank()) {
            "Windows Hello authentication failed. Try again."
        } else {
            "Windows Hello authentication failed. $sanitized"
        }
    }

    private fun buildAvailabilityScript(): String = buildString {
        val ps = '$'
        appendLine("Add-Type -AssemblyName System.Runtime.WindowsRuntime")
        appendLine("${ps}asTask = ([System.WindowsRuntimeSystemExtensions].GetMethods() |")
        appendLine("    Where-Object {")
        appendLine("        ${ps}_.Name -eq 'AsTask' -and")
        appendLine("        ${ps}_.GetParameters().Count -eq 1 -and")
        appendLine("        ${ps}_.GetParameters()[0].ParameterType.Name -like 'IAsyncOperation*'")
        appendLine("    } | Select-Object -First 1)")
        appendLine("if (${ps}null -eq ${ps}asTask) { throw 'Unable to resolve WinRT task bridge.' }")
        appendLine("${ps}op = [Windows.Security.Credentials.UI.UserConsentVerifier,Windows.Security.Credentials.UI,ContentType=WindowsRuntime]::CheckAvailabilityAsync()")
        appendLine("${ps}task = ${ps}asTask.MakeGenericMethod([Windows.Security.Credentials.UI.UserConsentVerifierAvailability]).Invoke(${ps}null, @(${ps}op))")
        appendLine("${ps}task.Wait()")
        appendLine("[Console]::Out.Write(${ps}task.Result.ToString())")
    }

    private fun buildDesktopVerificationScript(message: String): String = buildString {
        val ps = '$'
        appendLine("Add-Type -AssemblyName System.Runtime.WindowsRuntime")
        // Cache the compiled interop assembly in TEMP so Add-Type compilation (~1.5s) only
        // happens once per Windows session. Subsequent calls just load the tiny DLL.
        appendLine("${ps}dllPath = Join-Path ${ps}env:TEMP 'PM_HelloInteropBridge_v2.dll'")
        appendLine("if (Test-Path ${ps}dllPath) {")
        appendLine("    Add-Type -Path ${ps}dllPath")
        appendLine("} else {")
        appendLine("${ps}code = @\"")
        appendLine("using System;")
        appendLine("using System.Runtime.InteropServices;")
        appendLine("using System.Threading.Tasks;")
        appendLine("using System.Reflection;")
        appendLine("using System.Linq;")
        appendLine("")
        appendLine("[ComImport]")
        appendLine("[Guid(\"39E050C3-4E74-441A-8DC0-B81104DF949C\")]")
        appendLine("[InterfaceType(ComInterfaceType.InterfaceIsIInspectable)]")
        appendLine("interface IUserConsentVerifierInterop")
        appendLine("{")
        appendLine("    [PreserveSig]")
        appendLine("    int RequestVerificationForWindowAsync(")
        appendLine("        IntPtr appWindow,")
        appendLine("        [MarshalAs(UnmanagedType.HString)] string message,")
        appendLine("        ref Guid riid,")
        appendLine("        [MarshalAs(UnmanagedType.IUnknown)] out object asyncOperation);")
        appendLine("}")
        appendLine("")
        appendLine("public static class HelloInteropBridge")
        appendLine("{")
        appendLine("    [DllImport(\"user32.dll\")]")
        appendLine("    private static extern IntPtr GetForegroundWindow();")
        appendLine("")
        appendLine("    [DllImport(\"user32.dll\")]")
        appendLine("    private static extern bool SetForegroundWindow(IntPtr hWnd);")
        appendLine("")
        appendLine("    [DllImport(\"user32.dll\")]")
        appendLine("    private static extern uint GetWindowThreadProcessId(IntPtr hWnd, out uint lpdwProcessId);")
        appendLine("")
        appendLine("    [DllImport(\"user32.dll\")]")
        appendLine("    private static extern IntPtr GetAncestor(IntPtr hWnd, uint gaFlags);")
        appendLine("")
        appendLine("    [DllImport(\"user32.dll\")]")
        appendLine("    [return: MarshalAs(UnmanagedType.Bool)]")
        appendLine("    private static extern bool EnumWindows(EnumWindowsProc lpEnumFunc, IntPtr lParam);")
        appendLine("    private delegate bool EnumWindowsProc(IntPtr hWnd, IntPtr lParam);")
        appendLine("")
        appendLine("    [DllImport(\"user32.dll\")]")
        appendLine("    private static extern bool IsWindowVisible(IntPtr hWnd);")
        appendLine("")
        appendLine("    private const uint GA_ROOT = 2;")
        appendLine("")
        appendLine("    private static IntPtr FindRootWindow(uint processId)")
        appendLine("    {")
        appendLine("        IntPtr foundHwnd = IntPtr.Zero;")
        appendLine("        EnumWindows((hWnd, lParam) => {")
        appendLine("            uint windowPid;")
        appendLine("            GetWindowThreadProcessId(hWnd, out windowPid);")
        appendLine("            if (windowPid == processId && IsWindowVisible(hWnd))")
        appendLine("            {")
        appendLine("                foundHwnd = GetAncestor(hWnd, GA_ROOT);")
        appendLine("                return false;")
        appendLine("            }")
        appendLine("            return true;")
        appendLine("        }, IntPtr.Zero);")
        appendLine("        return foundHwnd;")
        appendLine("    }")
        appendLine("")
        appendLine("    public static string Request(string message, int parentPid)")
        appendLine("    {")
        appendLine("        try {")
        appendLine("            IntPtr hwnd = FindRootWindow((uint)parentPid);")
        appendLine("            if (hwnd == IntPtr.Zero) hwnd = GetForegroundWindow();")
        appendLine("            if (hwnd != IntPtr.Zero) SetForegroundWindow(hwnd);")
        appendLine("")
        appendLine("            var verifierType = Type.GetType(\"Windows.Security.Credentials.UI.UserConsentVerifier, Windows.Security.Credentials.UI, ContentType=WindowsRuntime\");")
        appendLine("            var factory = System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeMarshal.GetActivationFactory(verifierType);")
        appendLine("            var interop = (IUserConsentVerifierInterop)factory;")
        appendLine("            Guid iid = new Guid(\"FD596FFD-2318-558F-9DBE-D21DF43764A5\");")
        appendLine("")
        appendLine("            object asyncOp;")
        appendLine("            int hr = interop.RequestVerificationForWindowAsync(hwnd, message, ref iid, out asyncOp);")
        appendLine("            if (hr != 0) return \"Failed:0x\" + hr.ToString(\"X8\");")
        appendLine("")
        appendLine("            var asTaskMethod = typeof(System.WindowsRuntimeSystemExtensions)")
        appendLine("                .GetMethods(BindingFlags.Public | BindingFlags.Static)")
        appendLine("                .FirstOrDefault(m => m.Name == \"AsTask\" && m.IsGenericMethod);")
        appendLine("")
        appendLine("            var resultType = Type.GetType(\"Windows.Security.Credentials.UI.UserConsentVerificationResult, Windows.Security.Credentials.UI, ContentType=WindowsRuntime\");")
        appendLine("            var genericAsTask = asTaskMethod.MakeGenericMethod(resultType);")
        appendLine("            var task = (Task)genericAsTask.Invoke(null, new[] { asyncOp });")
        appendLine("")
        appendLine("            task.Wait();")
        appendLine("            var resultProperty = task.GetType().GetProperty(\"Result\");")
        appendLine("            return resultProperty.GetValue(task).ToString();")
        appendLine("        } catch (Exception ex) {")
        appendLine("            return \"Failed: \" + ex.Message;")
        appendLine("        }")
        appendLine("    }")
        appendLine("}")
        appendLine("\"@")
        appendLine("    try {")
        appendLine("        Add-Type -TypeDefinition ${ps}code -ReferencedAssemblies 'System.Runtime.WindowsRuntime' -OutputAssembly ${ps}dllPath")
        appendLine("        Add-Type -Path ${ps}dllPath")
        appendLine("    } catch {")
        appendLine("        Add-Type -TypeDefinition ${ps}code -ReferencedAssemblies 'System.Runtime.WindowsRuntime'")
        appendLine("    }")
        appendLine("}")
        appendLine("${ps}parentPid = (Get-CimInstance Win32_Process -Filter \"ProcessId = ${ps}PID\").ParentProcessId")
        appendLine("${ps}message = @'")
        appendLine(message)
        appendLine("'@")
        appendLine("[Console]::Out.Write([HelloInteropBridge]::Request(${ps}message, ${ps}parentPid))")
    }

    private fun buildLegacyVerificationScript(message: String): String = buildString {
        val ps = '$'
        appendLine("Add-Type -AssemblyName System.Runtime.WindowsRuntime")
        appendLine("${ps}asTask = ([System.WindowsRuntimeSystemExtensions].GetMethods() |")
        appendLine("    Where-Object {")
        appendLine("        ${ps}_.Name -eq 'AsTask' -and")
        appendLine("        ${ps}_.GetParameters().Count -eq 1 -and")
        appendLine("        ${ps}_.GetParameters()[0].ParameterType.Name -like 'IAsyncOperation*'")
        appendLine("    } | Select-Object -First 1)")
        appendLine("if (${ps}null -eq ${ps}asTask) { throw 'Unable to resolve WinRT task bridge.' }")
        appendLine("${ps}message = @'")
        appendLine(message)
        appendLine("'@")
        appendLine("${ps}op = [Windows.Security.Credentials.UI.UserConsentVerifier,Windows.Security.Credentials.UI,ContentType=WindowsRuntime]::RequestVerificationAsync(${ps}message)")
        appendLine("${ps}task = ${ps}asTask.MakeGenericMethod([Windows.Security.Credentials.UI.UserConsentVerificationResult]).Invoke(${ps}null, @(${ps}op))")
        appendLine("if (-not ${ps}task.Wait(180000)) { [Console]::Out.Write(\"$RESULT_TIMEOUT\"); exit 0 }")
        appendLine("[Console]::Out.Write(${ps}task.Result.ToString())")
    }

    private enum class VerificationMethod { UNKNOWN, DESKTOP, LEGACY }

    private companion object {
        const val RESULT_FALLBACK = "Fallback"
        const val RESULT_TIMEOUT = "Timeout"
    }
}
