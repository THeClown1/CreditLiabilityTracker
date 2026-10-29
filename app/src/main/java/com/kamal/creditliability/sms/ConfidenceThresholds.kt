package com.kamal.creditliability.sms

data class ConfidenceThresholds(
    val automaticSave: Double = 0.95,
    val review: Double = 0.70
) {
    init {
        require(review in 0.0..automaticSave && automaticSave <= 1.0)
    }
}