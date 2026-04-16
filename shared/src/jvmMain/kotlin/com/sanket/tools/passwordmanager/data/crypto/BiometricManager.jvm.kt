package com.sanket.tools.passwordmanager.data.crypto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.KeyboardFocusManager
import java.util.Base64

actual class BiometricManager {

    @Volatile
    private var cachedAvailability: Boolean? = null

    actual fun shouldOfferAuthentication(): Boolean = isWindows()

    actual fun canAuthenticate(): Boolean {
        if (!isWindows()) return false

        cachedAvailability?.let { return it }

        val isAvailable = runCatching {
            executePowerShell(buildAvailabilityScript()).equals("Available", ignoreCase = true)
        }.getOrDefault(false)

        cachedAvailability = isAvailable
        return isAvailable
    }

    actual fun authenticationLabel(): String = "Windows Hello"

    actual suspend fun authenticate(title: String, subtitle: String): AuthResult {
        if (!isWindows()) return AuthResult.NotAvailable
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


    private fun runVerification(promptMessage: String): String? {
        val desktopResult = runCatching {
            executePowerShell(buildDesktopVerificationScript(promptMessage))
        }.getOrNull()

        val finalResult = when {
            desktopResult.isNullOrBlank() -> runCatching {
                executePowerShell(buildLegacyVerificationScript(promptMessage))
            }.getOrNull()

            desktopResult == RESULT_FALLBACK ||
                desktopResult == RESULT_TIMEOUT ||
                desktopResult.startsWith("Failed:") -> runCatching {
                executePowerShell(buildLegacyVerificationScript(promptMessage))
            }.getOrNull() ?: desktopResult

            else -> desktopResult
        }

        return finalResult
    }

    private fun isWindows(): Boolean {
        return System.getProperty("os.name")
            ?.startsWith("Windows", ignoreCase = true) == true
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

        val stdout = process.inputStream.bufferedReader().use { it.readText() }
        val stderr = process.errorStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
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
        appendLine("${ps}code = @\"")
        appendLine("using System;")
        appendLine("using System.Runtime.InteropServices;")
        appendLine("using System.Threading;")
        appendLine("")
        appendLine("public enum AsyncStatus : uint")
        appendLine("{")
        appendLine("    Started = 0,")
        appendLine("    Completed = 1,")
        appendLine("    Canceled = 2,")
        appendLine("    Error = 3,")
        appendLine("}")
        appendLine("")
        appendLine("public enum UserConsentVerificationResult : int")
        appendLine("{")
        appendLine("    Verified = 0,")
        appendLine("    DeviceNotPresent = 1,")
        appendLine("    NotConfiguredForUser = 2,")
        appendLine("    DisabledByPolicy = 3,")
        appendLine("    DeviceBusy = 4,")
        appendLine("    RetriesExhausted = 5,")
        appendLine("    Canceled = 6,")
        appendLine("}")
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
        appendLine("        out IAsyncOperationUserConsentVerificationResult asyncOperation);")
        appendLine("}")
        appendLine("")
        appendLine("[ComImport]")
        appendLine("[Guid(\"FD596FFD-2318-558F-9DBE-D21DF43764A5\")]")
        appendLine("[InterfaceType(ComInterfaceType.InterfaceIsIInspectable)]")
        appendLine("interface IAsyncOperationUserConsentVerificationResult")
        appendLine("{")
        appendLine("    uint Id { get; }")
        appendLine("    AsyncStatus Status { get; }")
        appendLine("    int ErrorCode { get; }")
        appendLine("    void Cancel();")
        appendLine("    void Close();")
        appendLine("    IntPtr Completed { get; set; }")
        appendLine("    UserConsentVerificationResult GetResults();")
        appendLine("}")
        appendLine("")
        appendLine("public static class HelloInteropBridge")
        appendLine("{")
        appendLine("    [DllImport(\"user32.dll\")]")
        appendLine("    private static extern IntPtr GetForegroundWindow();")
        appendLine("")
        appendLine("    public static string Request(string message)")
        appendLine("    {")
        appendLine("        var hwnd = GetForegroundWindow();")
        appendLine("        if (hwnd == IntPtr.Zero)")
        appendLine("        {")
        appendLine("            return \"$RESULT_FALLBACK\";")
        appendLine("        }")
        appendLine("")
        appendLine("        var verifierType = Type.GetType(")
        appendLine("            \"Windows.Security.Credentials.UI.UserConsentVerifier, Windows.Security.Credentials.UI, ContentType=WindowsRuntime\",")
        appendLine("            true);")
        appendLine("")
        appendLine("        var factory = System.Runtime.InteropServices.WindowsRuntime.WindowsRuntimeMarshal")
        appendLine("            .GetActivationFactory(verifierType);")
        appendLine("        var interop = (IUserConsentVerifierInterop)factory;")
        appendLine("        var iid = new Guid(\"FD596FFD-2318-558F-9DBE-D21DF43764A5\");")
        appendLine("")
        appendLine("        IAsyncOperationUserConsentVerificationResult operation;")
        appendLine("        var hr = interop.RequestVerificationForWindowAsync(hwnd, message, ref iid, out operation);")
        appendLine("        if (hr != 0)")
        appendLine("        {")
        appendLine("            return $\"Failed:0x{hr:X8}\";")
        appendLine("        }")
        appendLine("")
        appendLine("        for (var i = 0; i < 1800; i++)")
        appendLine("        {")
        appendLine("            var status = operation.Status;")
        appendLine("            if (status == AsyncStatus.Completed)")
        appendLine("            {")
        appendLine("                return operation.GetResults().ToString();")
        appendLine("            }")
        appendLine("")
        appendLine("            if (status == AsyncStatus.Canceled)")
        appendLine("            {")
        appendLine("                return UserConsentVerificationResult.Canceled.ToString();")
        appendLine("            }")
        appendLine("")
        appendLine("            if (status == AsyncStatus.Error)")
        appendLine("            {")
        appendLine("                return $\"Failed:0x{operation.ErrorCode:X8}\";")
        appendLine("            }")
        appendLine("")
        appendLine("            Thread.Sleep(100);")
        appendLine("        }")
        appendLine("")
        appendLine("        return \"$RESULT_TIMEOUT\";")
        appendLine("    }")
        appendLine("}")
        appendLine("\"@")
        appendLine("Add-Type -TypeDefinition ${ps}code -ReferencedAssemblies 'System.Runtime.WindowsRuntime'")
        appendLine("${ps}message = @'")
        appendLine(message)
        appendLine("'@")
        appendLine("[Console]::Out.Write([HelloInteropBridge]::Request(${ps}message))")
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

    private companion object {
        const val RESULT_FALLBACK = "Fallback"
        const val RESULT_TIMEOUT = "Timeout"
    }
}
