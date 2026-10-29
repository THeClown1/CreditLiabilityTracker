package com.kamal.creditliability.presentation.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardFormScreen(
    cardId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CardFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cardId == null) "Add Card" else "Edit Card") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Cancel") } }
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Loading card…")
            }
        } else if (state.isLoadFailed) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(state.message.orEmpty(), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onBack) { Text("Back to cards") }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.message?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                FormField(
                    value = state.bankName,
                    onValueChange = viewModel::onBankNameChanged,
                    label = "Bank Name",
                    error = state.fieldErrors["bankName"]
                )
                FormField(
                    value = state.cardName,
                    onValueChange = viewModel::onCardNameChanged,
                    label = "Card Name",
                    error = state.fieldErrors["cardName"]
                )
                FormField(
                    value = state.lastFourDigits,
                    onValueChange = viewModel::onLastFourDigitsChanged,
                    label = "Last 4 Digits",
                    error = state.fieldErrors["lastFourDigits"],
                    keyboardType = KeyboardType.Number
                )
                FormField(
                    value = state.creditLimit,
                    onValueChange = viewModel::onCreditLimitChanged,
                    label = "Credit Limit",
                    error = state.fieldErrors["creditLimit"],
                    keyboardType = KeyboardType.Decimal,
                    supportingText = "Enter an amount greater than 0."
                )
                FormField(
                    value = state.billingDay,
                    onValueChange = viewModel::onBillingDayChanged,
                    label = "Billing Day (optional)",
                    error = state.fieldErrors["billingDay"],
                    keyboardType = KeyboardType.Number
                )
                FormField(
                    value = state.dueDay,
                    onValueChange = viewModel::onDueDayChanged,
                    label = "Due Day (optional)",
                    error = state.fieldErrors["dueDay"],
                    keyboardType = KeyboardType.Number
                )
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isSaving) "Saving…" else "Save Card")
                }
            }
        }
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = {
            when {
                error != null -> Text(error)
                supportingText != null -> Text(supportingText)
            }
        }
    )
}
