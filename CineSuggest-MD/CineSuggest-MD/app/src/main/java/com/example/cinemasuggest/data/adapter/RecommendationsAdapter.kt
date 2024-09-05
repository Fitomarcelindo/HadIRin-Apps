package com.example.cinemasuggest.data.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cinemasuggest.R
import com.example.cinemasuggest.data.response.RecommendationResponseItem
import com.example.cinemasuggest.view.cinerec.DetailRecActivity

class RecommendationsAdapter(private val recommendations: List<RecommendationResponseItem>) :
    RecyclerView.Adapter<RecommendationsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recommendation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recommendation = recommendations[position]
        holder.bind(recommendation)
    }

    override fun getItemCount(): Int {
        return recommendations.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_title)
        private val posterImageView: ImageView = itemView.findViewById(R.id.iv_poster)

        fun bind(recommendation: RecommendationResponseItem) {
            titleTextView.text = recommendation.title
            val posterUrl = "https://image.tmdb.org/t/p/w500${recommendation.poster}"
            Glide.with(itemView.context)
                .load(posterUrl)
                .into(posterImageView)

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetailRecActivity::class.java).apply {
                    putExtra("EXTRA_POSTER", recommendation.poster)
                    putExtra("EXTRA_TITLE", recommendation.title)
                }
                context.startActivity(intent)
            }
        }
    }
}
