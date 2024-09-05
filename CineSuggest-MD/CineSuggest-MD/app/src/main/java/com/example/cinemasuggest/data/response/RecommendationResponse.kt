package com.example.cinemasuggest.data.response

import com.google.gson.annotations.SerializedName


data class RecommendationResponseItem(

	@field:SerializedName("movieId")
	val movieId: Int,

	@field:SerializedName("title")
	val title: String,

	@field:SerializedName("poster")
	val poster: String
)
