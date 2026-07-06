package com.example.ai.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cached_weather` (
                `cacheKey` TEXT NOT NULL,
                `city` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `weather` TEXT NOT NULL,
                `temperature` TEXT NOT NULL,
                `tempMax` TEXT NOT NULL,
                `tempMin` TEXT NOT NULL,
                `humidity` TEXT NOT NULL,
                `wind` TEXT NOT NULL,
                `airQuality` TEXT NOT NULL,
                `precipitation` TEXT NOT NULL,
                `icon` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`cacheKey`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cached_poi` (
                `id` TEXT NOT NULL,
                `cacheKey` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `city` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `address` TEXT NOT NULL,
                `telephone` TEXT NOT NULL,
                `longitude` REAL NOT NULL,
                `latitude` REAL NOT NULL,
                `rating` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cached_images` (
                `keyword` TEXT NOT NULL,
                `imageUrl` TEXT NOT NULL,
                `photographer` TEXT NOT NULL,
                `cachedAt` INTEGER NOT NULL,
                PRIMARY KEY(`keyword`)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `cached_poi` ADD COLUMN `photoUrl` TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `users`")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `users` (
                `id` TEXT NOT NULL,
                `phone` TEXT NOT NULL,
                `nickname` TEXT NOT NULL,
                `avatar` TEXT NOT NULL,
                `gender` TEXT NOT NULL,
                `birthday` TEXT,
                `signature` TEXT,
                `city` TEXT,
                `travelPreference` TEXT NOT NULL,
                `createTime` INTEGER NOT NULL,
                `updateTime` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `users` ADD COLUMN `password` TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `favorites` ADD COLUMN `userPhone` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `favorites` ADD COLUMN `title` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `favorites` ADD COLUMN `subtitle` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `favorites` ADD COLUMN `imageUrl` TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `history` ADD COLUMN `userPhone` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `history` ADD COLUMN `title` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `history` ADD COLUMN `description` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `history` ADD COLUMN `days` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `history` ADD COLUMN `scheduleCount` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `history` ADD COLUMN `hotelCount` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `history` ADD COLUMN `foodCount` INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `history` ADD COLUMN `planJson` TEXT NOT NULL DEFAULT ''")
    }
}
