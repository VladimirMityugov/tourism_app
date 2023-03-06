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
import javax.inject.Inject

class PhotoAdapter @Inject constructor(
    val onItemClick: (PhotoData) -> Unit,
    val onDeletePhotoClick: (PhotoData) -> Unit,
    val onLongClick: (PhotoData) -> Unit
) : ListAdapter<PhotoData, MyViewHolder>(
    DiffUtilCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            binding = PhotoItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            onItemClick = onItemClick,
            onLongClick = onLongClick,
            onDeletePhotoClick = onDeletePhotoClick
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }
}

class MyViewHolder(
    val binding: PhotoItemBinding,
    val onItemClick: (PhotoData) -> Unit,
    val onDeletePhotoClick: (PhotoData) -> Unit,
    val onLongClick: (PhotoData) -> Unit
) : ViewHolder(binding.root) {

    fun bind(item: PhotoData) {
        with(binding) {
            date.text = buildString {
                if (item.description != null) {
                    append(item.description.take(15))
                    if (item.description.length > 15) {
                        append("...")
                    }
                }
            }
            Glide
                .with(photo.context)
                .load(Uri.parse(item.pic_src))
                .centerCrop()
                .error(R.drawable.ic_baseline_error_outline_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(photo)

            trashIcon.setOnClickListener {
                onDeletePhotoClick(item)
            }

            root.setOnClickListener {
                onItemClick(item)
            }

            root.setOnLongClickListener {
                onLongClick(item)
                true
            }

        }


    }
}


class DiffUtilCallback : DiffUtil.ItemCallback<PhotoData>() {
    override fun areItemsTheSame(oldItem: PhotoData, newItem: PhotoData): Boolean {
        return oldItem.pic_src == newItem.pic_src
    }

    override fun areContentsTheSame(oldItem: PhotoData, newItem: PhotoData): Boolean {
        return oldItem == newItem
    }

}