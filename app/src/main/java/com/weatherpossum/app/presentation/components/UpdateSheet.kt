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
 * Update dialog sheet for displaying available app updates
 * Shows update information, progress, and handles user interaction
 */
@Composable
fun UpdateSheet(
    vm: UpdateViewModel,
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
                    // Note: Context needs to be passed from the calling composable
                    // This is a limitation of the current design
                }
            ) { 
                Text(
                    if (vm.downloading) "Downloading…" else "Download & Install"
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
                text = "Update available — ${cand.versionName}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                if (vm.progressText != null) {
                    Text(
                        text = vm.progressText!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (!cand.notes.isBlank()) {
                    Text(
                        text = cand.notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (vm.error != null) {
                    Text(
                        text = vm.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}

/**
 * Enhanced update sheet with context parameter for download functionality
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
                    if (vm.downloading) "Downloading…" else "Download & Install"
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
                text = "Update available — ${cand.versionName}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                if (vm.progressText != null) {
                    Text(
                        text = vm.progressText!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (!cand.notes.isBlank()) {
                    Text(
                        text = cand.notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (vm.error != null) {
                    Text(
                        text = vm.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}
