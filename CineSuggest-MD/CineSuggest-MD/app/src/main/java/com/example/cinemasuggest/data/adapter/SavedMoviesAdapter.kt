package com.example.cinemasuggest.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinemasuggest.R
import com.example.cinemasuggest.data.room.recommendation.UserMovie

class SavedMoviesAdapter(
    private val savedMovies: List<UserMovie>,
    private val onItemClick: (UserMovie) -> Unit
) : RecyclerView.Adapter<SavedMoviesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_movie, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val savedMovie = savedMovies[position]
        holder.bind(savedMovie)
    }

    override fun getItemCount(): Int = savedMovies.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
        private val posterImageView: ImageView = itemView.findViewById(R.id.iv_poster)

        fun bind(userMovie: UserMovie) {
            titleTextView.text = userMovie.title
            val posterUrl = "https://image.tmdb.org/t/p/w500${userMovie.posterPath}"
            Glide.with(itemView.context)
                .load(posterUrl)
                .into(posterImageView)

            itemView.setOnClickListener { onItemClick(userMovie) }
        }
    }
}
