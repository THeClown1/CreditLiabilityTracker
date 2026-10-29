package com.kamal.creditliability.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {
    @Inject
    lateinit var messageProcessor: SmsMessageProcessor

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) return
        val sender = messages.firstNotNullOfOrNull { it.originatingAddress }
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
        val receivedAt = messages.maxOfOrNull { it.timestampMillis } ?: System.currentTimeMillis()
        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                messageProcessor.processIncoming(sender, body, receivedAt)
            } catch (_: Exception) {
                // Avoid logging SMS content or financial data.
            } finally {
                pendingResult.finish()
            }
        }
    }
}