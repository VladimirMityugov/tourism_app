package com.example.permissionsapp.presentation

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.permissionsapp.data.local.entities.PhotoData
import com.example.tourismApp.R
import com.example.tourismApp.databinding.PhotoItemBinding
import com.example.tourismApp.databinding.RouteItemBinding
import javax.inject.Inject

class RoutesAdapter(
    val onItemClick: (PhotoData) -> Unit
) : ListAdapter<PhotoData, RoutesViewHolder>(

    DiffUtilCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutesViewHolder {
        return RoutesViewHolder(
            binding = RouteItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RoutesViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            routeName.text = item.routeName
            routeDate.text = buildString {
                append("Route date : ")
                append(item.date)
            }
            Glide
                .with(photo.context)
                .load(Uri.parse(item.pic_src))
                .centerCrop()
                .error(R.drawable.ic_baseline_error_outline_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(photo)
        }

        holder.binding.root.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }
}

class RoutesViewHolder(val binding: RouteItemBinding) : ViewHolder(binding.root)

class DiffUtilRoutes : DiffUtil.ItemCallback<PhotoData>() {
    override fun areItemsTheSame(oldItem: PhotoData, newItem: PhotoData): Boolean {
        return oldItem.pic_src == newItem.pic_src
    }

    override fun areContentsTheSame(oldItem: PhotoData, newItem: PhotoData): Boolean {
        return oldItem == newItem
    }

}