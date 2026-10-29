package com.kamal.creditliability.presentation.dashboard

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun SmsPermissionCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var showRationale by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        granted = isGranted
        if (!isGranted) showSettings = true
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECEIVE_SMS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("SMS transaction detection", style = MaterialTheme.typography.titleMedium)
            Text(
                if (granted) "Enabled. Matching card alerts are processed on this device."
                else "Optional. Allow SMS access to detect matching credit card alerts.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (!granted) {
                Button(onClick = { showRationale = true }) {
                    Text("Enable SMS access")
                }
            }
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Allow SMS access?") },
            text = {
                Text("The app reads incoming messages locally to identify credit card transactions. OTPs and promotions are ignored; message content is not sent to AI.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) { Text("Not now") }
            }
        )
    }
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("SMS access is off") },
            text = { Text("Enable SMS permission in app settings to process incoming card alerts.") },
            confirmButton = {
                TextButton(onClick = {
                    showSettings = false
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }) { Text("Open settings") }
            },
            dismissButton = {
                TextButton(onClick = { showSettings = false }) { Text("Close") }
            }
        )
    }
}