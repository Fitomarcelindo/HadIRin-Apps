package com.example.cinemasuggest.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cinemasuggest.data.room.auth.User
import com.example.cinemasuggest.data.room.auth.UserDao
import com.example.cinemasuggest.data.room.recommendation.UserMovie
import com.example.cinemasuggest.data.room.recommendation.UserMovieDao

@Database(entities = [User::class, UserMovie::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userMovieDao(): UserMovieDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `user_movies` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `poster` TEXT NOT NULL)")
            }
        }
    }
}