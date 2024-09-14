package com.dessalines.rankmyfavs.db

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dessalines.rankmyfavs.utils.TAG
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors
import kotlin.system.exitProcess

@Database(
    version = 2,
    entities = [AppSettings::class, FavList::class, FavListItem::class, FavListMatch::class],
    exportSchema = true,
)
abstract class AppDB : RoomDatabase() {
    abstract fun appSettingsDao(): AppSettingsDao

    abstract fun favListDao(): FavListDao

    abstract fun favListItemDao(): FavListItemDao

    abstract fun favListMatchDao(): FavListMatchDao

    companion object {
        @Volatile
        private var instance: AppDB? = null

        fun getDatabase(context: Context): AppDB {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return instance ?: synchronized(this) {
                val i =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppDB::class.java,
                            TAG,
                        ).allowMainThreadQueries()
                        .addMigrations(
                            MIGRATION_1_2,
                        )
                        // Necessary because it can't insert data on creation
                        .addCallback(
                            object : Callback() {
                                override fun onOpen(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    Executors.newSingleThreadExecutor().execute {
                                        db.insert(
                                            "AppSettings",
                                            // Ensures it won't overwrite the existing data
                                            CONFLICT_IGNORE,
                                            ContentValues(2).apply {
                                                put("id", 1)
                                            },
                                        )
                                    }
                                }
                            },
                        ).build()
                instance = i
                // return instance
                i
            }
        }

        // Backup and restore db code from:
        // https://stackoverflow.com/a/73201175/1655478
        private const val DB_NAME = TAG
        private const val DB_BACKUP_SUFFIX = "-bkp"
        private const val SQLITE_WALFILE_SUFFIX = "-wal"
        private const val SQLITE_SHMFILE_SUFFIX = "-shm"

        /**
         * Backup the database
         */
        fun backupDatabase(context: Context): Int {
            var result = -99
            if (instance == null) return result

            val dbFile = context.getDatabasePath(DB_NAME)
            val dbWalFile = File(dbFile.path + SQLITE_WALFILE_SUFFIX)
            val dbShmFile = File(dbFile.path + SQLITE_SHMFILE_SUFFIX)
            val bkpFile = File(dbFile.path + DB_BACKUP_SUFFIX)
            val bkpWalFile = File(bkpFile.path + SQLITE_WALFILE_SUFFIX)
            val bkpShmFile = File(bkpFile.path + SQLITE_SHMFILE_SUFFIX)
            if (bkpFile.exists()) bkpFile.delete()
            if (bkpWalFile.exists()) bkpWalFile.delete()
            if (bkpShmFile.exists()) bkpShmFile.delete()
            checkpoint()
            try {
                dbFile.copyTo(bkpFile, true)
                if (dbWalFile.exists()) dbWalFile.copyTo(bkpWalFile, true)
                if (dbShmFile.exists()) dbShmFile.copyTo(bkpShmFile, true)
                result = 0
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }

        /**
         *  Restore the database and then restart the App
         */
        fun restoreDatabase(
            context: Context,
            restart: Boolean = true,
        ) {
            if (!File(context.getDatabasePath(DB_NAME).path + DB_BACKUP_SUFFIX).exists()) {
                return
            }
            if (instance == null) return
            val dbpath = instance!!.openHelper.readableDatabase.path!!
            val dbFile = File(dbpath)
            val dbWalFile = File(dbFile.path + SQLITE_WALFILE_SUFFIX)
            val dbShmFile = File(dbFile.path + SQLITE_SHMFILE_SUFFIX)
            val bkpFile = File(dbFile.path + DB_BACKUP_SUFFIX)
            val bkpWalFile = File(bkpFile.path + SQLITE_WALFILE_SUFFIX)
            val bkpShmFile = File(bkpFile.path + SQLITE_SHMFILE_SUFFIX)
            try {
                bkpFile.copyTo(dbFile, true)
                if (bkpWalFile.exists()) bkpWalFile.copyTo(dbWalFile, true)
                if (bkpShmFile.exists()) bkpShmFile.copyTo(dbShmFile, true)
                checkpoint()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (restart) {
                val i = context.packageManager.getLaunchIntentForPackage(context.packageName)
                i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(i)
                exitProcess(0)
            }
        }

        private fun checkpoint() {
            val db = instance!!.openHelper.writableDatabase
            db.query("PRAGMA wal_checkpoint(FULL);")
            db.query("PRAGMA wal_checkpoint(TRUNCATE);")
        }
    }
}
