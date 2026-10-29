package com.kamal.creditliability.presentation.sms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamal.creditliability.presentation.cards.formatRupees
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsReviewScreen(onBack: () -> Unit, viewModel: SmsReviewViewModel = hiltViewModel()) {
    val candidates by viewModel.candidates.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<PendingSmsCandidate?>(null) }
    Scaffold(topBar = {
        TopAppBar(title = { Text("SMS review") }, navigationIcon = {
            TextButton(onClick = onBack) { Text("Back") }
        })
    }) { padding ->
        if (candidates.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
                Text("No transactions need review.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(candidates, key = { it.message.id }) { candidate ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                candidate.cards.firstOrNull { it.id == candidate.matchedCardId }
                                    ?.let { "${it.bankName} •••• ${it.lastFourDigits}" }
                                    ?: "Card not matched",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(candidate.parsed.merchant, style = MaterialTheme.typography.titleMedium)
                            Text(formatRupees(candidate.parsed.amount))
                            Text("${candidate.parsed.transactionType.replace('_', ' ')} · ${candidate.parsed.category}")
                            Text(
                                "Confidence ${String.format(Locale.getDefault(), "%.0f%%", candidate.parsed.confidence * 100)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    enabled = candidate.matchedCardId != null,
                                    onClick = { viewModel.approve(candidate) }
                                ) { Text("Add") }
                                OutlinedButton(onClick = { editing = candidate }) { Text("Edit") }
                                TextButton(onClick = { viewModel.ignore(candidate) }) { Text("Ignore") }
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { candidate ->
        var amount by remember(candidate.message.id) { mutableStateOf(candidate.parsed.amount.toString()) }
        var merchant by remember(candidate.message.id) { mutableStateOf(candidate.parsed.merchant) }
        var selectedCardId by remember(candidate.message.id) {
            mutableStateOf(candidate.matchedCardId ?: candidate.cards.firstOrNull()?.id)
        }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Edit transaction") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    candidate.cards.forEach { card ->
                        TextButton(onClick = { selectedCardId = card.id }) {
                            Text(
                                (if (selectedCardId == card.id) "✓ " else "") +
                                    "${card.bankName} •••• ${card.lastFourDigits}"
                            )
                        }
                    }
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = merchant,
                        onValueChange = { merchant = it },
                        label = { Text("Merchant") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    amount.toDoubleOrNull()?.let {
                        viewModel.approve(candidate, selectedCardId, it, merchant)
                    }
                    editing = null
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel") } }
        )
    }
}