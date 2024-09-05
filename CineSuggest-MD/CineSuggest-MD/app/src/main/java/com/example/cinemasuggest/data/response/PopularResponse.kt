package com.example.cinemasuggest.data.response

import com.google.gson.annotations.SerializedName

data class Movie(

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("title")
	val title: String,

	@field:SerializedName("poster")
	val poster: String
)
