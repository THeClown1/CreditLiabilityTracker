package com.kamal.creditliability.presentation.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamal.creditliability.data.entity.CreditCardEntity
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(
    onBack: () -> Unit,
    onAddCard: () -> Unit,
    onEditCard: (Long) -> Unit,
    onViewCard: (Long) -> Unit,
    viewModel: CardListViewModel = hiltViewModel()
) {
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var cardToDelete by remember { mutableStateOf<CreditCardEntity?>(null) }

    cardToDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            title = { Text("Delete card?") },
            text = {
                Text(
                    "Delete ${card.cardName} ending in ${card.lastFourDigits}? " +
                        "Its saved transactions will also be deleted."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCard(card)
                        cardToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cards") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCard) {
                Text("+ Add Card")
            }
        }
    ) { innerPadding ->
        if (cards.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Text("No credit cards added.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.padding(6.dp))
                Button(onClick = onAddCard) { Text("+ Add Card") }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                errorMessage?.let { message ->
                    item {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
                items(cards, key = CreditCardEntity::id) { card ->
                    CardListItem(
                        card = card,
                        onEdit = { onEditCard(card.id) },
                        onView = { onViewCard(card.id) },
                        onDelete = { cardToDelete = card }
                    )
                }
            }
        }
    }
}

@Composable
private fun CardListItem(
    card: CreditCardEntity,
    onEdit: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(card.bankName, style = MaterialTheme.typography.labelLarge)
            Text(card.cardName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("•••• ${card.lastFourDigits}", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Credit limit: ${formatRupees(card.creditLimit)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                card.billingDay?.let { Text("Billing day: $it", style = MaterialTheme.typography.bodySmall) }
                card.dueDay?.let { Text("Due day: $it", style = MaterialTheme.typography.bodySmall) }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onView) { Text("Details") }
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

internal fun formatRupees(amount: Double): String =
    NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).format(amount)
