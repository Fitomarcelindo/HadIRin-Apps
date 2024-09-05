package com.example.cinemasuggest.data.room.recommendation

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_movies")
data class UserMovie(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val movieId: String = " ",
    val title: String,
    val posterPath: String
)
