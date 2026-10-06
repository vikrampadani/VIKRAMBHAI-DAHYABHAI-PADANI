package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DailyStatsDao
import com.example.data.local.dao.JapaSessionDao
import com.example.data.local.dao.MantraDao
import com.example.data.local.entity.DailyStatsEntity
import com.example.data.local.entity.JapaSessionEntity
import com.example.data.local.entity.MantraEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MantraEntity::class,
        JapaSessionEntity::class,
        DailyStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mantraDao(): MantraDao
    abstract fun japaSessionDao(): JapaSessionDao
    abstract fun dailyStatsDao(): DailyStatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mantra_jap_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialMantras(database.mantraDao())
                    }
                }
            }
        }

        suspend fun populateInitialMantras(dao: MantraDao) {
            if (dao.getMantraCount() == 0) {
                dao.insertAll(DefaultMantras.list)
            }
        }
    }
}
