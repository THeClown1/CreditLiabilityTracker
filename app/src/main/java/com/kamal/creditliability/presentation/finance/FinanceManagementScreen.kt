package com.kamal.creditliability.presentation.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.presentation.cards.formatRupees

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceManagementScreen(
    onBack: () -> Unit,
    viewModel: FinanceManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var emiCardId by remember { mutableStateOf<Long?>(null) }
    var billCardId by remember { mutableStateOf<Long?>(null) }
    var merchant by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var emiAmount by remember { mutableStateOf("") }
    var tenure by remember { mutableStateOf("") }
    var remaining by remember { mutableStateOf("") }
    var interest by remember { mutableStateOf("0") }
    var fee by remember { mutableStateOf("0") }
    var nextDueDate by remember { mutableStateOf("") }
    var statementDate by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var billAmount by remember { mutableStateOf("") }
    var paidAmount by remember { mutableStateOf("0") }

    LaunchedEffect(state.cards) {
        if (emiCardId == null) emiCardId = state.cards.firstOrNull()?.id
        if (billCardId == null) billCardId = state.cards.firstOrNull()?.id
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Bills and EMIs") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
        )
    }) { padding ->
        if (state.cards.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
                Text("Add a card before recording its statements or EMI obligations.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SectionCard(title = "Add EMI obligation") {
                        CardPicker("Card", state.cards, emiCardId) { emiCardId = it }
                        InputField(merchant, { merchant = it }, "Merchant")
                        InputField(totalAmount, { totalAmount = it }, "Total amount")
                        InputField(emiAmount, { emiAmount = it }, "Monthly EMI amount")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InputField(tenure, { tenure = it }, "Tenure (months)", Modifier.weight(1f))
                            InputField(remaining, { remaining = it }, "Remaining months", Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InputField(interest, { interest = it }, "Interest", Modifier.weight(1f))
                            InputField(fee, { fee = it }, "Processing fee", Modifier.weight(1f))
                        }
                        InputField(nextDueDate, { nextDueDate = it }, "Next due date (YYYY-MM-DD, optional)")
                        Button(
                            onClick = {
                                viewModel.saveEmi(
                                    emiCardId, merchant, totalAmount, emiAmount, tenure, remaining,
                                    interest, fee, nextDueDate
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Save EMI") }
                    }
                }
                items(state.emis, key = { "emi-${it.id}" }) { emi ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(emi.merchant, style = MaterialTheme.typography.titleMedium)
                            Text("${formatRupees(emi.emiAmount)} per month · ${emi.remainingTenure} of ${emi.tenure} months remaining")
                            Text("Total ${formatRupees(emi.totalAmount)} · Interest ${formatRupees(emi.interest)} · Fee ${formatRupees(emi.processingFee)}")
                            Text(state.cards.firstOrNull { it.id == emi.cardId }?.let(::cardLabel).orEmpty())
                            emi.nextDueDate?.let { Text("Next due: ${dateLabel(it)}") }
                        }
                    }
                }
                item {
                    SectionCard(title = "Add statement") {
                        CardPicker("Card", state.cards, billCardId) { billCardId = it }
                        InputField(statementDate, { statementDate = it }, "Statement date (YYYY-MM-DD)")
                        InputField(dueDate, { dueDate = it }, "Due date (YYYY-MM-DD)")
                        InputField(billAmount, { billAmount = it }, "Statement amount")
                        InputField(paidAmount, { paidAmount = it }, "Paid amount")
                        Button(
                            onClick = {
                                viewModel.saveStatement(billCardId, statementDate, dueDate, billAmount, paidAmount)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Save statement") }
                    }
                }
                items(state.statements, key = { "statement-${it.id}" }) { statement ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(state.cards.firstOrNull { it.id == statement.cardId }?.let(::cardLabel).orEmpty())
                            Text("Statement ${dateLabel(statement.statementDate)} · Due ${dateLabel(statement.dueDate)}")
                            Text("Bill ${formatRupees(statement.statementAmount)} · Paid ${formatRupees(statement.paidAmount)}")
                            Text("Status: ${statement.paymentStatus}")
                            if (statement.paymentStatus != "PAID") {
                                TextButton(onClick = { viewModel.markStatementPaid(statement) }) {
                                    Text("Mark fully paid")
                                }
                            }
                        }
                    }
                }
                state.message?.let { message ->
                    item { Text(message, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun CardPicker(
    label: String,
    cards: List<CreditCardEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = cards.firstOrNull { it.id == selectedId }
    Column {
        OutlinedTextField(
            value = selected?.let(::cardLabel) ?: "Select card",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = { expanded = true }) { Text("Choose card") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            cards.forEach { card ->
                DropdownMenuItem(
                    text = { Text(cardLabel(card)) },
                    onClick = { onSelect(card.id); expanded = false }
                )
            }
        }
    }
}

private fun dateLabel(timestamp: Long): String =
    java.time.LocalDate.ofEpochDay(timestamp / 86_400_000L).toString()

private fun cardLabel(card: CreditCardEntity): String =
    "${card.bankName} · ${card.cardName} · •••• ${card.lastFourDigits}"
