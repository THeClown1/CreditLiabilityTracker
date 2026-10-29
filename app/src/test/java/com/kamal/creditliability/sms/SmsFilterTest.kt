package com.kamal.creditliability.sms

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsFilterTest {
    private val filter = SmsFilter()

    @Test
    fun acceptsCreditCardFinancialAlert() {
        assertTrue(
            filter.shouldProcess(
                "ICICI",
                "Your ICICI credit card XX1234 was used for INR 8,999 at FLIPKART."
            )
        )
    }

    @Test
    fun rejectsOtpMessages() {
        assertFalse(
            filter.shouldProcess(
                "ICICI",
                "Your credit card XX1234 OTP 123456 is valid for 10 minutes."
            )
        )
    }

    @Test
    fun rejectsMarketingAndNonFinancialMessages() {
        assertFalse(
            filter.shouldProcess(
                "BANK",
                "Exclusive offer: apply now for a new credit card and win rewards."
            )
        )
        assertFalse(filter.shouldProcess("FRIEND", "Are we still meeting for lunch?"))
    }
}