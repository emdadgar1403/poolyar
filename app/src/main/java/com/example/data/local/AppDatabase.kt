package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AllowanceDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.SettingsDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.AllowanceWeekEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.UserSettingsEntity

@Database(
    entities = [
        TransactionEntity::class,
        AllowanceWeekEntity::class,
        GoalEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun allowanceDao(): AllowanceDao
    abstract fun goalDao(): GoalDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "poulyar_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
