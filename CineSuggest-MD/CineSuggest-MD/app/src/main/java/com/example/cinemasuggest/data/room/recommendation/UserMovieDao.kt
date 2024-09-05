package com.example.cinemasuggest.data.room.recommendation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserMovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(movie: UserMovie)

    @Query("SELECT * FROM user_movies WHERE title = :title AND userId = :userId LIMIT 1")
    fun getMovieByTitle(title: String, userId: String): UserMovie?

    @Query("SELECT * FROM user_movies WHERE userId = :userId")
    fun getAllMovies(userId: String): List<UserMovie>

    @Query("DELETE FROM user_movies WHERE userId = :userId")
    fun deleteAllMovies(userId: String)
}

