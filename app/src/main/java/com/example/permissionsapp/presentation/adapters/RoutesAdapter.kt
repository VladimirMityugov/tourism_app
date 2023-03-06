package com.example.permissionsapp.presentation


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.permissionsapp.data.local.entities.RouteData
import com.example.tourismApp.R
import com.example.tourismApp.databinding.RouteItemBinding


class RoutesAdapter(
    val onItemClick: (RouteData) -> Unit,
    val onDeleteRouteClick: (RouteData) -> Unit
) : ListAdapter<RouteData, RoutesViewHolder>(
    DiffUtilRoutes()
) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutesViewHolder {
        return RoutesViewHolder(
            binding = RouteItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            onItemClick = onItemClick,
            onDeleteRouteClick = onDeleteRouteClick
        )
    }

    override fun onBindViewHolder(holder: RoutesViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }
}

class RoutesViewHolder(
    val binding: RouteItemBinding,
    val onDeleteRouteClick: (RouteData) -> Unit,
    val onItemClick: (RouteData) -> Unit
) : ViewHolder(binding.root) {

    private var isTrashVisible = false
    fun bind(item: RouteData) {
        with(binding) {
            routeName.text = item.route_name
            routeStartDate.text = buildString {
                append("Route started : ")
                append(item.start_date)
            }

            routeEndDate.text = buildString {
                append("Route finished : ")
                append(item.end_date)
            }

            Glide
                .with(routePicture.context)
                .load(item.bmp)
                .error(R.drawable.ic_baseline_error_outline_24)
                .placeholder(R.drawable.ic_baseline_image_24)
                .into(routePicture)

            trashIcon.setOnClickListener {
                onDeleteRouteClick(item)
            }

            root.setOnClickListener {
                onItemClick(item)
            }

            root.setOnLongClickListener {
                if(!isTrashVisible){
                    trashIcon.visibility = View.VISIBLE
                    isTrashVisible = true
                } else {
                    trashIcon.visibility = View.INVISIBLE
                    isTrashVisible = false
                }
                true
            }
        }
    }
}

class DiffUtilRoutes : DiffUtil.ItemCallback<RouteData>() {
    override fun areItemsTheSame(oldItem: RouteData, newItem: RouteData): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RouteData, newItem: RouteData): Boolean {
        return oldItem == newItem
    }

}