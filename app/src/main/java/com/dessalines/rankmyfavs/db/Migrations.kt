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

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add TierList table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS TierList (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    fav_list_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    color INTEGER NOT NULL,
                    tier_order INTEGER NOT NULL,
                    FOREIGN KEY(fav_list_id) REFERENCES FavList(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )

            // Create an index on fav_list_id to optimize queries
            db.execSQL(
                """
                CREATE INDEX index_TierList_fav_list_id ON TierList(fav_list_id)
                """.trimIndent(),
            )

            // Create an index on tier_order to support ordering
            db.execSQL(
                """
                CREATE INDEX index_TierList_tier_order ON TierList(tier_order)
                """.trimIndent(),
            )

            // Add tier_list_initialized to FavList
            db.execSQL(
                """
                ALTER TABLE FavList
                ADD COLUMN tier_list_initialized
                INTEGER NOT NULL DEFAULT 0
                """.trimIndent(),
            )
        }
    }
