package com.leyna.nailmanagement.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.leyna.nailmanagement.data.dao.GelDao
import com.leyna.nailmanagement.data.dao.GelInventoryDao
import com.leyna.nailmanagement.data.dao.NailStyleDao
import com.leyna.nailmanagement.data.entity.Gel
import com.leyna.nailmanagement.data.entity.GelInventory
import com.leyna.nailmanagement.data.entity.NailStyle
import com.leyna.nailmanagement.data.entity.NailStyleGelCrossRef

@Database(
    entities = [Gel::class, NailStyle::class, NailStyleGelCrossRef::class, GelInventory::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gelDao(): GelDao
    abstract fun nailStyleDao(): NailStyleDao
    abstract fun gelInventoryDao(): GelInventoryDao

    companion object {
        private const val DATABASE_NAME = "nail_management_database"

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to gels table
                db.execSQL("ALTER TABLE gels ADD COLUMN brand TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE gels ADD COLUMN series TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE gels ADD COLUMN category TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE gels ADD COLUMN store TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE gels ADD COLUMN storeNote TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE gels ADD COLUMN notes TEXT DEFAULT NULL")

                // Create inventory table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS gel_inventory (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        gelId INTEGER NOT NULL,
                        purchaseDate INTEGER,
                        expiryDate INTEGER,
                        usedUpDate INTEGER,
                        writeOffDate INTEGER,
                        note TEXT,
                        FOREIGN KEY(gelId) REFERENCES gels(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_gel_inventory_gelId ON gel_inventory(gelId)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
