package com.kamal.creditliability.di

import android.content.Context
import com.kamal.creditliability.data.local.AppDatabase
import com.kamal.creditliability.data.local.BillingStatementDao
import com.kamal.creditliability.data.local.CreditCardDao
import com.kamal.creditliability.data.local.EmiObligationDao
import com.kamal.creditliability.data.local.SmsMessageDao
import com.kamal.creditliability.data.local.TransactionDao
import com.kamal.creditliability.data.repository.RoomBillingStatementRepository
import com.kamal.creditliability.data.repository.RoomCreditCardRepository
import com.kamal.creditliability.data.repository.RoomEmiObligationRepository
import com.kamal.creditliability.data.repository.RoomSmsMessageRepository
import com.kamal.creditliability.data.repository.RoomTransactionRepository
import com.kamal.creditliability.domain.repository.BillingStatementRepository
import com.kamal.creditliability.domain.repository.CreditCardRepository
import com.kamal.creditliability.domain.repository.EmiObligationRepository
import com.kamal.creditliability.domain.repository.SmsMessageRepository
import com.kamal.creditliability.domain.repository.TransactionRepository
import com.kamal.creditliability.sms.ConfidenceThresholds
import com.kamal.creditliability.sms.parser.LocalTransactionParser
import com.kamal.creditliability.sms.parser.TransactionParser
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideCreditCardDao(database: AppDatabase): CreditCardDao = database.creditCardDao()

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideSmsMessageDao(database: AppDatabase): SmsMessageDao = database.smsMessageDao()

    @Provides
    fun provideEmiObligationDao(database: AppDatabase): EmiObligationDao = database.emiObligationDao()

    @Provides
    fun provideBillingStatementDao(database: AppDatabase): BillingStatementDao = database.billingStatementDao()

    @Provides
    @Singleton
    fun provideConfidenceThresholds(): ConfidenceThresholds = ConfidenceThresholds()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCreditCardRepository(repository: RoomCreditCardRepository): CreditCardRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(repository: RoomTransactionRepository): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindSmsMessageRepository(repository: RoomSmsMessageRepository): SmsMessageRepository

    @Binds
    @Singleton
    abstract fun bindEmiObligationRepository(repository: RoomEmiObligationRepository): EmiObligationRepository

    @Binds
    @Singleton
    abstract fun bindBillingStatementRepository(repository: RoomBillingStatementRepository): BillingStatementRepository

    @Binds
    @Singleton
    abstract fun bindTransactionParser(parser: LocalTransactionParser): TransactionParser
}