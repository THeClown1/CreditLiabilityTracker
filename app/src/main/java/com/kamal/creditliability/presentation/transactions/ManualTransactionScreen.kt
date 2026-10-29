package com.kamal.creditliability.presentation.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamal.creditliability.data.entity.CreditCardEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private const val MILLIS_PER_DAY = 86_400_000L

private val transactionTypes = listOf(
    "PURCHASE",
    "PAYMENT",
    "REFUND",
    "FEE",
    "EMI",
    "CASH_WITHDRAWAL",
    "OTHER"
)

private val transactionCategories = listOf(
    "SHOPPING",
    "FOOD",
    "TRAVEL",
    "FUEL",
    "BILLS",
    "ENTERTAINMENT",
    "HEALTHCARE",
    "EMI",
    "SUBSCRIPTION",
    "CASH_WITHDRAWAL",
    "FEES",
    "PAYMENT",
    "REFUND",
    "OTHERS"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualTransactionScreen(
    onBack: () -> Unit,
    onAddCard: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ManualTransactionViewModel = hiltViewModel()
) {
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.transactionDate
    )

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(viewModel::onTransactionDateChanged)
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Cancel") } }
            )
        }
    ) { innerPadding ->
        if (cards.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Add a credit card before entering a transaction.")
                Spacer(Modifier.height(12.dp))
                Button(onClick = onAddCard) { Text("Go to cards") }
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
                SelectionField(
                    label = "Card",
                    value = cards.firstOrNull { it.id == state.cardId }?.let(::cardLabel)
                        ?: "Select a card",
                    options = cards.map(::cardLabel),
                    error = state.fieldErrors["cardId"],
                    onSelected = { label ->
                        cards.firstOrNull { cardLabel(it) == label }?.let {
                            viewModel.selectCard(it.id)
                        }
                    }
                )
                TransactionTextField(
                    value = state.amount,
                    onValueChange = viewModel::onAmountChanged,
                    label = "Amount (₹)",
                    error = state.fieldErrors["amount"],
                    keyboardType = KeyboardType.Decimal
                )
                TransactionTextField(
                    value = state.merchant,
                    onValueChange = viewModel::onMerchantChanged,
                    label = "Merchant",
                    error = state.fieldErrors["merchant"]
                )
                SelectionField(
                    label = "Category",
                    value = state.category,
                    options = transactionCategories,
                    error = state.fieldErrors["category"],
                    onSelected = viewModel::onCategoryChanged
                )
                SelectionField(
                    label = "Transaction Type",
                    value = state.transactionType,
                    options = transactionTypes,
                    error = state.fieldErrors["transactionType"],
                    onSelected = viewModel::onTransactionTypeChanged
                )
                OutlinedTextField(
                    value = LocalDate.ofEpochDay(state.transactionDate / MILLIS_PER_DAY)
                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("Choose") } }
                )
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChanged,
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                state.message?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isSaving) "Saving…" else "Save")
                }
            }
        }
    }
}

@Composable
private fun SelectionField(
    label: String,
    value: String,
    options: List<String>,
    error: String?,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            isError = error != null,
            supportingText = error?.let { message -> ({ Text(message) }) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TransactionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = { error?.let { Text(it) } }
    )
}

private fun cardLabel(card: CreditCardEntity): String =
    "${card.bankName} · ${card.cardName} · •••• ${card.lastFourDigits}"
