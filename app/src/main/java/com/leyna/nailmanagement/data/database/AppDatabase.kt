package com.leyna.nailmanagement.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.leyna.nailmanagement.data.dao.GelDao
import com.leyna.nailmanagement.data.dao.NailStyleDao
import com.leyna.nailmanagement.data.entity.Gel
import com.leyna.nailmanagement.data.entity.NailStyle
import com.leyna.nailmanagement.data.entity.NailStyleGelCrossRef

@Database(
    entities = [Gel::class, NailStyle::class, NailStyleGelCrossRef::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gelDao(): GelDao
    abstract fun nailStyleDao(): NailStyleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nail_management_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}