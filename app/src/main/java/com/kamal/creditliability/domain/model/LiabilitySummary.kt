package com.kamal.creditliability.domain.model

data class LiabilitySummary(
    val totalLiability: Double = 0.0,
    val totalCreditLimit: Double = 0.0,
    val availableCredit: Double = 0.0,
    val overallUtilisationPercentage: Double = 0.0,
    val cards: List<CardLiabilitySummary> = emptyList()
)
