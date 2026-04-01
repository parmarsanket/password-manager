package com.sanket.tools.passwordmanager

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.sanket.tools.passwordmanager.data.crypto.ActivityProvider
import com.sanket.tools.passwordmanager.data.export.BackupFileActivityBridge
import org.koin.core.context.GlobalContext

class MainActivity : FragmentActivity() {
    private val activityProvider by lazy(LazyThreadSafetyMode.NONE) {
        GlobalContext.get().get<ActivityProvider>()
    }
    private val backupFileBridge by lazy(LazyThreadSafetyMode.NONE) {
        GlobalContext.get().get<BackupFileActivityBridge>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityProvider.setCurrentActivity(this)

        setContent {
            App()
        }
    }

    override fun onStart() {
        super.onStart()
        activityProvider.setCurrentActivity(this)
    }

    override fun onStop() {
        activityProvider.clearActivity(this)
        super.onStop()
    }

    @Deprecated("Uses platform activity results for file import/export.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        backupFileBridge.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
