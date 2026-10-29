package com.kamal.creditliability.ai

import com.kamal.creditliability.domain.model.ParsedTransaction

/** Optional interpretation provider. Implementations must return validated structured data only. */
interface AiTransactionParser {
    suspend fun parse(sender: String?, body: String, receivedAt: Long): ParsedTransaction?
}