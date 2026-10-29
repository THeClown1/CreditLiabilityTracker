package com.kamal.creditliability.sms.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LocalTransactionParserBankTest {
    private val parser = LocalTransactionParser()
    private val receivedAt = 1_800_000_000_000L

    @Test
    fun parsesSbiRupeeSymbolPurchase() {
        val parsed = parser.parse(
            "SBI",
            "SBI Credit Card XX1234 used for ₹5,000 at AMAZON on 14-Oct-26.",
            receivedAt
        )
        assertNotNull(parsed)
        assertEquals("SBI", parsed?.bank)
        assertEquals("PURCHASE", parsed?.transactionType)
        assertEquals(5_000.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("1234", parsed?.cardLast4)
    }

    @Test
    fun parsesAxisCashWithdrawalWithRsAmount() {
        val parsed = parser.parse(
            "AXIS",
            "Your Axis Bank card ending 4321 cash withdrawal of Rs. 1,000.00 on 14/10/2026.",
            receivedAt
        )
        assertNotNull(parsed)
        assertEquals("AXIS", parsed?.bank)
        assertEquals("CASH_WITHDRAWAL", parsed?.transactionType)
        assertEquals("CASH_WITHDRAWAL", parsed?.category)
        assertEquals(1_000.0, parsed?.amount ?: 0.0, 0.001)
        assertEquals("4321", parsed?.cardLast4)
    }

    @Test
    fun identifiesFeesAndEmiAsTheirOwnTypes() {
        val fee = parser.parse(
            "HDFC",
            "HDFC Credit Card XX1122 fee of INR 500 charged.",
            receivedAt
        )
        val emi = parser.parse(
            "ICICI",
            "ICICI Credit Card XX3344 EMI installment of INR 2,000.",
            receivedAt
        )
        assertEquals("FEE", fee?.transactionType)
        assertEquals("FEES", fee?.category)
        assertEquals("EMI", emi?.transactionType)
        assertEquals("EMI", emi?.category)
    }
}