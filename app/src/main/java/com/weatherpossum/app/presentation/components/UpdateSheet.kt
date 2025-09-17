package com.weatherpossum.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherpossum.app.ui.viewmodel.UpdateViewModel


/**
 * Automatic update dialog that only appears when an update is available
 * Provides seamless update experience with minimal user interaction
 */
@Composable
fun UpdateSheetWithContext(
    vm: UpdateViewModel,
    context: android.content.Context,
    onDismiss: () -> Unit
) {
    val cand = vm.candidate
    if (cand == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = !vm.downloading,
                onClick = {
                    vm.downloadAndInstall(context)
                }
            ) { 
                Text(
                    if (vm.downloading) "Installing…" else "Update Now"
                ) 
            }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text("Later") 
            } 
        },
        title = { 
            Text(
                text = "Update Available",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "WeatherPossum ${cand.versionName} is now available!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                if (vm.progressText != null) {
                    Text(
                        text = vm.progressText!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (!cand.notes.isBlank()) {
                    Text(
                        text = "What's new:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = cand.notes,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (vm.error != null) {
                    Text(
                        text = "Update failed: ${vm.error}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}
