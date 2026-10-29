package com.kamal.creditliability.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kamal.creditliability.data.entity.BillingStatementEntity
import com.kamal.creditliability.data.entity.CreditCardEntity
import com.kamal.creditliability.data.entity.EmiObligationEntity
import com.kamal.creditliability.data.entity.SmsMessageEntity
import com.kamal.creditliability.data.entity.TransactionEntity

@Database(
    entities = [
        CreditCardEntity::class,
        TransactionEntity::class,
        SmsMessageEntity::class,
        EmiObligationEntity::class,
        BillingStatementEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun creditCardDao(): CreditCardDao
    abstract fun transactionDao(): TransactionDao
    abstract fun smsMessageDao(): SmsMessageDao
    abstract fun emiObligationDao(): EmiObligationDao
    abstract fun billingStatementDao(): BillingStatementDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN relatedTransactionId INTEGER")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_transactions_relatedTransactionId " +
                        "ON transactions(relatedTransactionId)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS emi_obligations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cardId INTEGER NOT NULL,
                        merchant TEXT NOT NULL,
                        totalAmount REAL NOT NULL,
                        emiAmount REAL NOT NULL,
                        tenure INTEGER NOT NULL,
                        remainingTenure INTEGER NOT NULL,
                        interest REAL NOT NULL,
                        processingFee REAL NOT NULL,
                        nextDueDate INTEGER,
                        active INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(cardId) REFERENCES credit_cards(id)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )"""
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_emi_obligations_cardId " +
                        "ON emi_obligations(cardId)"
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS billing_statements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cardId INTEGER NOT NULL,
                        statementDate INTEGER NOT NULL,
                        dueDate INTEGER NOT NULL,
                        statementAmount REAL NOT NULL,
                        paidAmount REAL NOT NULL,
                        paymentStatus TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(cardId) REFERENCES credit_cards(id)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )"""
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_billing_statements_cardId_statementDate " +
                        "ON billing_statements(cardId, statementDate)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }

        private const val DATABASE_NAME = "credit_liability_tracker.db"
    }
}