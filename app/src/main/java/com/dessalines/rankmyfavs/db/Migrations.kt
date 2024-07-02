package com.dessalines.rankmyfavs.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add min_confidence to settings
            db.execSQL(
                """
                ALTER TABLE AppSettings
                ADD COLUMN min_confidence
                INTEGER NOT NULL DEFAULT $DEFAULT_MIN_CONFIDENCE
                """.trimIndent(),
            )

            // Add match_count to favlistitem
            db.execSQL(
                """
                ALTER TABLE FavListItem
                ADD COLUMN match_count
                INTEGER NOT NULL DEFAULT 0
                """.trimIndent(),
            )
        }
    }
