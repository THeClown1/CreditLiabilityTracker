package com.kamal.creditliability.domain.model

data class CardLiabilitySummary(
    val cardId: Long,
    val outstanding: Double,
    val creditLimit: Double,
    val availableCredit: Double,
    val utilisationPercentage: Double
)
