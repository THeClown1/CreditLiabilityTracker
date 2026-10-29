package com.kamal.creditliability.presentation.security

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DeleteAllDataControl(
    modifier: Modifier = Modifier,
    viewModel: DeleteAllDataViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirm by remember { mutableStateOf(false) }
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Local data", style = MaterialTheme.typography.titleMedium)
            Text("Delete all saved cards, transactions, statements, EMI details, and stored SMS.")
            if (state.completed) {
                Text("All local tracker data has been deleted.")
            } else {
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                OutlinedButton(onClick = { confirm = true }, enabled = !state.isDeleting) {
                    Text(if (state.isDeleting) "Deleting…" else "Delete all data")
                }
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Delete all local data?") },
            text = { Text("This permanently removes every saved card, transaction, statement, EMI obligation, and stored SMS from this app.") },
            confirmButton = {
                Button(onClick = {
                    confirm = false
                    viewModel.deleteAll()
                }) { Text("Delete everything") }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } }
        )
    }
}