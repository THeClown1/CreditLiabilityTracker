package com.kamal.creditliability.presentation.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamal.creditliability.data.entity.TransactionEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private const val MILLIS_PER_DAY = 86_400_000L
private const val ALL_CATEGORIES = "All categories"
private const val ALL_TYPES = "All types"

private val categories = listOf(
    ALL_CATEGORIES,
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

private val transactionTypes = listOf(
    ALL_TYPES,
    "PURCHASE",
    "PAYMENT",
    "REFUND",
    "FEE",
    "EMI",
    "CASH_WITHDRAWAL",
    "OTHER"
)

private enum class DateFilter { FROM, TO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailsScreen(
    onBack: () -> Unit,
    viewModel: CardDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var activeDateFilter by remember { mutableStateOf<DateFilter?>(null) }

    activeDateFilter?.let { target ->
        val currentDate = when (target) {
            DateFilter.FROM -> state.filters.fromDateMillis
            DateFilter.TO -> state.filters.toDateMillis
        }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentDate ?: (LocalDate.now().toEpochDay() * MILLIS_PER_DAY)
        )
        DatePickerDialog(
            onDismissRequest = { activeDateFilter = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (target) {
                            DateFilter.FROM -> viewModel.setFromDate(pickerState.selectedDateMillis)
                            DateFilter.TO -> viewModel.setToDate(pickerState.selectedDateMillis)
                        }
                        activeDateFilter = null
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDateFilter = null }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.card?.cardName ?: "Card Details") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Text(
                    "Loading card…",
                    modifier = Modifier.padding(innerPadding).padding(24.dp)
                )
            }
            state.card == null -> {
                Text(
                    "This card could not be found.",
                    modifier = Modifier.padding(innerPadding).padding(24.dp)
                )
            }
            else -> {
                val card = requireNotNull(state.card)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(card.bankName, style = MaterialTheme.typography.labelLarge)
                                Text(card.cardName, style = MaterialTheme.typography.titleLarge)
                                Text("•••• ${card.lastFourDigits}")
                                DetailValue(
                                    label = "Current Liability",
                                    value = formatRupees(state.liability?.outstanding ?: 0.0)
                                )
                                DetailValue(
                                    label = "Credit Limit",
                                    value = formatRupees(card.creditLimit)
                                )
                                DetailValue(
                                    label = "Available Credit",
                                    value = formatRupees(state.liability?.availableCredit ?: card.creditLimit)
                                )
                                DetailValue(
                                    label = "Utilisation",
                                    value = formatPercent(state.liability?.utilisationPercentage ?: 0.0)
                                )
                                card.billingDay?.let { DetailValue("Billing Day", it.toString()) }
                                card.dueDay?.let { DetailValue("Due Day", it.toString()) }
                            }
                        }
                    }
                    item {
                        Text(
                            "TRANSACTIONS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DateFilterButton(
                                label = state.filters.fromDateMillis?.let { "From: ${formatDate(it)}" }
                                    ?: "From date",
                                onClick = { activeDateFilter = DateFilter.FROM }
                            )
                            DateFilterButton(
                                label = state.filters.toDateMillis?.let { "To: ${formatDate(it)}" }
                                    ?: "To date",
                                onClick = { activeDateFilter = DateFilter.TO }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterDropdown(
                                label = "Category",
                                value = state.filters.category ?: ALL_CATEGORIES,
                                options = categories,
                                modifier = Modifier.weight(1f),
                                onSelected = { selected ->
                                    viewModel.setCategory(selected.takeUnless { it == ALL_CATEGORIES })
                                }
                            )
                            FilterDropdown(
                                label = "Type",
                                value = state.filters.transactionType ?: ALL_TYPES,
                                options = transactionTypes,
                                modifier = Modifier.weight(1f),
                                onSelected = { selected ->
                                    viewModel.setTransactionType(selected.takeUnless { it == ALL_TYPES })
                                }
                            )
                        }
                        if (state.filters != CardTransactionFilters()) {
                            TextButton(onClick = viewModel::clearFilters) {
                                Text("Clear filters")
                            }
                        }
                    }
                    if (state.transactions.isEmpty()) {
                        item {
                            Text(
                                "No transactions match these filters.",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        items(state.transactions, key = TransactionEntity::id) { transaction ->
                            TransactionHistoryItem(transaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailValue(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DateFilterButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text(label) }
}

@Composable
private fun FilterDropdown(
    label: String,
    value: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            singleLine = true
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
private fun TransactionHistoryItem(transaction: TransactionEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(transaction.merchant, fontWeight = FontWeight.SemiBold)
                Text(
                    "${transaction.transactionType.replace('_', ' ')} · ${transaction.category}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    formatDate(transaction.transactionDate),
                    style = MaterialTheme.typography.bodySmall
                )
                transaction.description?.takeIf(String::isNotBlank)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                transaction.relatedTransactionId?.let {
                    Text("Refund linked to purchase #$it", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                (if (transaction.transactionType in setOf("PAYMENT", "REFUND")) "−" else "") +
                    formatRupees(transaction.amount),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private fun formatDate(timestamp: Long): String =
    LocalDate.ofEpochDay(timestamp / MILLIS_PER_DAY)
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()))

private fun formatPercent(value: Double): String =
    if (value % 1.0 == 0.0) "${value.toLong()}%"
    else String.format(Locale.getDefault(), "%.1f%%", value)
