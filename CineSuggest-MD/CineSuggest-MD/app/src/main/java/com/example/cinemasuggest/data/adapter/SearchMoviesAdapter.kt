package com.example.cinemasuggest.view.search

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinemasuggest.R
import com.example.cinemasuggest.data.response.SearchResponseItem

class SearchMoviesAdapter(private val searchResults: List<SearchResponseItem>) :
    RecyclerView.Adapter<SearchMoviesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchResult = searchResults[position]
        holder.bind(searchResult)
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
        private val posterImageView: ImageView = itemView.findViewById(R.id.iv_poster)

        fun bind(movie: SearchResponseItem) {
            titleTextView.text = movie.title
            val posterUrl = "https://image.tmdb.org/t/p/w500${movie.poster}"
            Glide.with(itemView.context).load(posterUrl).into(posterImageView)

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetailSearchActivity::class.java).apply {
                    putExtra("EXTRA_TITLE", movie.title)
                    putExtra("EXTRA_POSTER", movie.poster)
                }
                context.startActivity(intent)
            }
        }
    }
}
