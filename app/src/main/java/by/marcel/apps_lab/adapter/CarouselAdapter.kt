package by.marcel.apps_lab.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import by.marcel.apps_lab.R

class CarouselAdapter(private val images: List<Int>) : RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.poster_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_anim, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val realPosition = position % images.size
        val imageRes = images[realPosition]
        holder.imageView.setImageResource(imageRes)
    }

    override fun getItemCount(): Int {
        return Integer.MAX_VALUE
    }
}