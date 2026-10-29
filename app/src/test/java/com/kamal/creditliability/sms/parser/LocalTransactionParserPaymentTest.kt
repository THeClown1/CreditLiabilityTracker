package com.kamal.creditliability.sms.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LocalTransactionParserPaymentTest {
    private val parser = LocalTransactionParser()

    @Test
    fun parsesBankCardPaymentAsLiabilityCreditNotSpending() {
        val parsed = parser.parse(
            sender = "ICICI",
            body = "Payment of INR 10,000 received towards your credit card XX1234.",
            receivedAt = 1_800_000_000_000L
        )

        assertNotNull(parsed)
        assertEquals("PAYMENT", parsed?.transactionType)
        assertEquals("PAYMENT", parsed?.category)
        assertEquals(10_000.0, parsed?.amount ?: 0.0, 0.0001)
        assertEquals("1234", parsed?.cardLast4)
        assertEquals("Card payment", parsed?.merchant)
    }

    @Test
    fun parsesRupeeAmountAndPaymentReceivedWording() {
        val parsed = parser.parse(
            sender = "HDFC",
            body = "Rs. 2,500.00 payment received for card 5678",
            receivedAt = 1_800_000_000_000L
        )

        assertNotNull(parsed)
        assertEquals("PAYMENT", parsed?.transactionType)
        assertEquals("PAYMENT", parsed?.category)
        assertEquals(2_500.0, parsed?.amount ?: 0.0, 0.0001)
        assertEquals("5678", parsed?.cardLast4)
    }

    @Test
    fun parsesRefundSmsAsLiabilityCreditAndNotPurchase() {
        val parsed = parser.parse(
            sender = "ICICI",
            body = "Refund of INR 2,000 has been credited to card XX1234.",
            receivedAt = 1_800_000_000_000L
        )

        assertNotNull(parsed)
        assertEquals("REFUND", parsed?.transactionType)
        assertEquals("REFUND", parsed?.category)
        assertEquals(2_000.0, parsed?.amount ?: 0.0, 0.0001)
        assertEquals("1234", parsed?.cardLast4)
        assertEquals("Card refund", parsed?.merchant)
    }
}