package com.kamal.creditliability.domain.model

data class CategorySpend(val category: String, val amount: Double)

data class FinanceSummary(
    val currentBill: Double = 0.0,
    val unbilledAmount: Double = 0.0,
    val monthlyEmi: Double = 0.0,
    val totalMonthlySpending: Double = 0.0,
    val monthlySpending: List<CategorySpend> = emptyList(),
    val insights: List<String> = emptyList()
)