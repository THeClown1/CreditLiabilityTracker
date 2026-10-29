package com.kamal.creditliability.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.data.entity.TransactionEntity
import com.kamal.creditliability.domain.model.LiabilitySummary
import com.kamal.creditliability.domain.model.FinanceSummary
import com.kamal.creditliability.presentation.cards.formatRupees
import com.kamal.creditliability.presentation.security.DeleteAllDataControl
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private const val MILLIS_PER_DAY = 86_400_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    cards: List<CreditCardEntity>,
    transactions: List<TransactionEntity>,
    liabilitySummary: LiabilitySummary,
    financeSummary: FinanceSummary,
    onManageCards: () -> Unit,
    onOpenFinancials: () -> Unit,
    onAddTransaction: () -> Unit,
    onOpenSmsReview: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Credit Liability") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "CREDIT LIABILITY",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = formatRupees(liabilitySummary.totalLiability),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Total Liability",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SummaryValue(
                            label = "Credit Limit",
                            value = formatRupees(liabilitySummary.totalCreditLimit),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryValue(
                            label = "Available Credit",
                            value = formatRupees(liabilitySummary.availableCredit),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    SummaryValue(
                        label = "Overall Utilisation",
                        value = formatPercent(liabilitySummary.overallUtilisationPercentage)
                    )
                }
            }

            Button(onClick = onAddTransaction, modifier = Modifier.fillMaxWidth()) {
                Text("+ Add Transaction")
            }

            SmsPermissionCard()
            Button(onClick = onOpenSmsReview, modifier = Modifier.fillMaxWidth()) {
                Text("Review SMS transactions")
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("BILLING & EMI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SummaryValue("Current Bill", formatRupees(financeSummary.currentBill), Modifier.weight(1f))
                        SummaryValue("Unbilled", formatRupees(financeSummary.unbilledAmount), Modifier.weight(1f))
                    }
                    SummaryValue("Active monthly EMI", formatRupees(financeSummary.monthlyEmi))
                    Button(onClick = onOpenFinancials, modifier = Modifier.fillMaxWidth()) {
                        Text("Manage bills and EMIs")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("MONTHLY SPENDING", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (financeSummary.monthlySpending.isEmpty()) {
                        Text("No eligible spending transactions this month.")
                    } else {
                        financeSummary.monthlySpending.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(item.category.replace('_', ' '))
                                Text(formatRupees(item.amount))
                            }
                        }
                        SummaryValue("Total", formatRupees(financeSummary.totalMonthlySpending))
                    }
                }
            }

            if (financeSummary.insights.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("INSIGHTS · TRACKED DATA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        financeSummary.insights.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }

            DeleteAllDataControl()

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "MY CARDS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (cards.isEmpty()) {
                            Text(
                                text = "No credit cards added.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            cards.forEach { card ->
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(card.cardName, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${card.bankName} · •••• ${card.lastFourDigits}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    val cardSummary = liabilitySummary.cards.firstOrNull {
                                        it.cardId == card.id
                                    }
                                    if (cardSummary != null) {
                                        Text(
                                            "Liability: ${formatRupees(cardSummary.outstanding)} · " +
                                                "Available: ${formatRupees(cardSummary.availableCredit)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            "Utilisation: ${formatPercent(cardSummary.utilisationPercentage)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                        Button(
                            onClick = onManageCards,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (cards.isEmpty()) "+ Add Card" else "Manage Cards")
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "RECENT TRANSACTIONS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (transactions.isEmpty()) {
                    Text("No transactions recorded yet.")
                } else {
                    transactions.forEach { transaction ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        transaction.merchant,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "${transaction.transactionType.replace('_', ' ')} · ${transaction.category}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        LocalDate.ofEpochDay(transaction.transactionDate / MILLIS_PER_DAY)
                                            .format(
                                                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                                                    .withLocale(Locale.getDefault())
                                            ),
                                        style = MaterialTheme.typography.bodySmall
                                    )
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
                }
            }
        }
    }
}

private fun formatPercent(value: Double): String =
    if (value % 1.0 == 0.0) {
        "${value.toLong()}%"
    } else {
        String.format(Locale.getDefault(), "%.1f%%", value)
    }

@Composable
private fun SummaryValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}
