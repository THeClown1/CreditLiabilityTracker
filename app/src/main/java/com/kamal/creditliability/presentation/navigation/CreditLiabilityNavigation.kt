package com.kamal.creditliability.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kamal.creditliability.presentation.cards.CardDetailsScreen
import com.kamal.creditliability.presentation.cards.CardDetailsViewModel
import com.kamal.creditliability.presentation.cards.CardFormScreen
import com.kamal.creditliability.presentation.cards.CardFormViewModel
import com.kamal.creditliability.presentation.cards.CardListScreen
import com.kamal.creditliability.presentation.dashboard.DashboardScreen
import com.kamal.creditliability.presentation.dashboard.DashboardViewModel
import com.kamal.creditliability.presentation.transactions.ManualTransactionScreen
import com.kamal.creditliability.presentation.sms.SmsReviewScreen
import com.kamal.creditliability.presentation.finance.FinanceManagementScreen

private object Destinations {
    const val Dashboard = "dashboard"
    const val Cards = "cards"
    const val CardForm = "card-form?cardId={cardId}"
    const val CardDetails = "card-details/{cardId}"
    const val AddTransaction = "transaction-form"
    const val SmsReview = "sms-review"
    const val Financials = "financials"
}

@Composable
fun CreditLiabilityNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Destinations.Dashboard) {
        composable(Destinations.Dashboard) {
            val viewModel: DashboardViewModel = hiltViewModel()
            val cards by viewModel.cards.collectAsStateWithLifecycle()
            val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()
            val liabilitySummary by viewModel.liabilitySummary.collectAsStateWithLifecycle()
            val financeSummary by viewModel.financeSummary.collectAsStateWithLifecycle()

            DashboardScreen(
                cards = cards,
                transactions = recentTransactions,
                liabilitySummary = liabilitySummary,
                financeSummary = financeSummary,
                onManageCards = { navController.navigate(Destinations.Cards) },
                onOpenFinancials = { navController.navigate(Destinations.Financials) },
                onAddTransaction = { navController.navigate(Destinations.AddTransaction) },
                onOpenSmsReview = { navController.navigate(Destinations.SmsReview) }
            )
        }
        composable(Destinations.Cards) {
            CardListScreen(
                onBack = { navController.popBackStack() },
                onAddCard = { navController.navigate("card-form") },
                onEditCard = { cardId ->
                    navController.navigate("card-form?cardId=$cardId")
                },
                onViewCard = { cardId ->
                    navController.navigate("card-details/$cardId")
                }
            )
        }
        composable(
            route = Destinations.CardForm,
            arguments = listOf(
                navArgument(CardFormViewModel.CARD_ID_ARGUMENT) {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { entry ->
            val cardId = entry.arguments
                ?.getLong(CardFormViewModel.CARD_ID_ARGUMENT)
                ?.takeIf { it > 0L }

            CardFormScreen(
                cardId = cardId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Destinations.CardDetails,
            arguments = listOf(
                navArgument(CardDetailsViewModel.CARD_ID_ARGUMENT) {
                    type = NavType.LongType
                }
            )
        ) {
            CardDetailsScreen(onBack = { navController.popBackStack() })
        }
        composable(Destinations.AddTransaction) {
            ManualTransactionScreen(
                onBack = { navController.popBackStack() },
                onAddCard = { navController.navigate(Destinations.Cards) },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(Destinations.SmsReview) {
            SmsReviewScreen(onBack = { navController.popBackStack() })
        }
        composable(Destinations.Financials) {
            FinanceManagementScreen(onBack = { navController.popBackStack() })
        }
    }
}
