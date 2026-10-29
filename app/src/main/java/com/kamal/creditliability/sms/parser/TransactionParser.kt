package com.kamal.creditliability.sms.parser

import com.kamal.creditliability.domain.model.ParsedTransaction

interface TransactionParser {
    fun parse(sender: String?, body: String, receivedAt: Long): ParsedTransaction?
}