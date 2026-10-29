package com.kamal.creditliability.domain.service

import com.kamal.creditliability.data.entity.BillingStatementEntity
import com.kamal.creditliability.data.entity.EmiObligationEntity
import com.kamal.creditliability.data.entity.TransactionEntity
import com.kamal.creditliability.domain.model.CategorySpend
import com.kamal.creditliability.domain.model.FinanceSummary
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceCalculator @Inject constructor() {
    fun calculate(
        transactions: List<TransactionEntity>,
        statements: List<BillingStatementEntity>,
        emis: List<EmiObligationEntity>,
        nowMillis: Long = System.currentTimeMillis()
    ): FinanceSummary {
        val zone = ZoneId.systemDefault()
        val latestStatementByCard = statements.groupBy { it.cardId }
            .mapValues { (_, values) -> values.maxByOrNull { it.statementDate } }
        val currentBill = latestStatementByCard.values.filterNotNull().sumOf { statement ->
            val smsAndManualPayments = transactions.asSequence()
                .filter {
                    it.cardId == statement.cardId &&
                        it.transactionType == "PAYMENT" &&
                        it.transactionDate > statement.statementDate
                }
                .sumOf(TransactionEntity::amount)
            (statement.statementAmount - maxOf(statement.paidAmount, smsAndManualPayments))
                .coerceAtLeast(0.0)
        }

        val unbilledAmount = transactions.sumOf { transaction ->
            val statementDate = latestStatementByCard[transaction.cardId]?.statementDate
            if (statementDate != null && transaction.transactionDate <= statementDate) 0.0
            else liabilityImpact(transaction)
        }.coerceAtLeast(0.0)

        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        val monthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val nextMonth = today.withDayOfMonth(1).plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val monthTransactions = transactions.filter {
            it.transactionDate >= monthStart && it.transactionDate < nextMonth
        }
        val spendByCategory = mutableMapOf<String, Double>()
        monthTransactions.filter { it.transactionType in SPENDING_TYPES }.forEach {
            spendByCategory[it.category] = (spendByCategory[it.category] ?: 0.0) + it.amount
        }
        monthTransactions.filter { it.transactionType == "REFUND" && it.relatedTransactionId != null }
            .forEach { refund ->
                val purchase = transactions.firstOrNull { it.id == refund.relatedTransactionId }
                if (purchase != null) {
                    spendByCategory[purchase.category] =
                        (spendByCategory[purchase.category] ?: 0.0) - refund.amount
                }
            }
        val categorySpending = spendByCategory.mapNotNull { (category, amount) ->
            amount.takeIf { it > 0.0 }?.let { CategorySpend(category, it) }
        }.sortedByDescending(CategorySpend::amount)
        val monthlySpend = categorySpending.sumOf(CategorySpend::amount)
        val monthlyEmi = emis.filter { it.active && it.remainingTenure > 0 }
            .sumOf(EmiObligationEntity::emiAmount)

        val insights = buildList {
            if (currentBill > 0.0) add("Outstanding statement bills total ₹${currentBill.asMoney()}.")
            if (monthlySpend > 0.0) add("Tracked spending this month is ₹${monthlySpend.asMoney()}.")
            categorySpending.firstOrNull()?.let {
                add("${it.category.lowercase(Locale.ROOT).replaceFirstChar(Char::uppercase)} is your largest tracked category at ₹${it.amount.asMoney()}.")
            }
            if (monthlyEmi > 0.0) add("Recorded active EMI instalments total ₹${monthlyEmi.asMoney()} per month.")
        }
        return FinanceSummary(
            currentBill = currentBill,
            unbilledAmount = unbilledAmount,
            monthlyEmi = monthlyEmi,
            totalMonthlySpending = monthlySpend,
            monthlySpending = categorySpending,
            insights = insights
        )
    }

    private fun liabilityImpact(transaction: TransactionEntity): Double = when (transaction.transactionType) {
        "PURCHASE", "FEE", "EMI", "CASH_WITHDRAWAL" -> transaction.amount
        "PAYMENT", "REFUND" -> -transaction.amount
        else -> 0.0
    }

    private fun Double.asMoney(): String = String.format(Locale.US, "%,.0f", this)

    private companion object {
        val SPENDING_TYPES = setOf("PURCHASE", "FEE", "EMI", "CASH_WITHDRAWAL")
    }
}