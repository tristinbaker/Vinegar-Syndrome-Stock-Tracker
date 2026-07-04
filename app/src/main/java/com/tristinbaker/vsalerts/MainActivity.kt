package com.tristinbaker.vsalerts

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tristinbaker.vsalerts.ui.VsAlertsNavHost
import com.tristinbaker.vsalerts.ui.theme.VsAlertsTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            VsAlertsTheme {
                var showBatteryPrompt by remember { mutableStateOf(!isIgnoringBatteryOptimizations()) }

                VsAlertsNavHost()

                if (showBatteryPrompt) {
                    AlertDialog(
                        onDismissRequest = { showBatteryPrompt = false },
                        title = { Text("Allow background checks") },
                        text = {
                            Text(
                                "This app has no server — it checks stock/price on your phone in the " +
                                    "background. Android may delay or skip those checks to save battery " +
                                    "unless you exempt this app from battery optimization.",
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showBatteryPrompt = false
                                startActivity(
                                    Intent(
                                        android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        Uri.parse("package:$packageName"),
                                    ),
                                )
                            }) { Text("Disable it") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showBatteryPrompt = false }) { Text("Not now") }
                        },
                    )
                }
            }
        }
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(PowerManager::class.java) ?: return true
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }
}
