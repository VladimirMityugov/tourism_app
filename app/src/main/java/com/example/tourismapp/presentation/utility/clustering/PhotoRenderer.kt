package com.example.tourismapp.presentation.utility.clustering

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.example.tourismapp.R
import com.example.tourismapp.presentation.utility.Auxiliary
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.ClusterManager.*
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator


class PhotoRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<PhotoMarker>
) : DefaultClusterRenderer<PhotoMarker>(
    context,
    map,
    clusterManager
) {
    private val clusterIconGenerator: IconGenerator =
        IconGenerator(context)
    private val clusterImageView: ImageView
    private val dimension: Int
    private val myContext = context

    init {
        val multiProfile: View = View.inflate(context, R.layout.multi_profile, null)
        clusterIconGenerator.setContentView(multiProfile)
        clusterIconGenerator.setBackground(null)
        clusterImageView = multiProfile.findViewById(R.id.image)
        dimension = context.resources.getDimension(R.dimen.custom_profile_image).toInt()
    }

    override fun onBeforeClusterItemRendered(
        photoMarker: PhotoMarker,
        markerOptions: MarkerOptions
    ) {
        markerOptions
            .icon(getItemIcon(photoMarker))
            .title(photoMarker.title)
    }

    override fun onClusterItemUpdated(photoMarker: PhotoMarker, marker: Marker) {
        marker.setIcon(getItemIcon(photoMarker))
        marker.title = photoMarker.title
    }

    private fun getItemIcon(photoMarker: PhotoMarker): BitmapDescriptor {
        val imageView = Auxiliary.getRoundedImageView(myContext, photoMarker)
        val iconGenerator = IconGenerator(myContext)
        iconGenerator.setContentView(imageView)
        iconGenerator.setBackground(null)
        val icon = iconGenerator.makeIcon()
        return BitmapDescriptorFactory.fromBitmap(icon)
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<PhotoMarker?>,
        markerOptions: MarkerOptions
    ) {
        markerOptions.icon(getClusterIcon(cluster))
    }

    override fun onClusterUpdated(cluster: Cluster<PhotoMarker?>, marker: Marker) {
        marker.setIcon(getClusterIcon(cluster))
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getClusterIcon(cluster: Cluster<PhotoMarker?>): BitmapDescriptor {
        val photos: MutableList<Drawable> = ArrayList(4.coerceAtMost(cluster.size))
        val width = dimension
        val height = dimension
        for (p in cluster.items) {
            if (photos.size == 4) break
            if (p != null) {
                val uri = Uri.parse(p.pic_src)
                val drawable = Auxiliary.getDrawableFromUri(uri, myContext)
                drawable?.setBounds(0, 0, width, height)
                if (drawable != null) {
                    photos.add(drawable)
                }
            }
        }
        val multiDrawable = MultiDrawable(photos)
        multiDrawable.setBounds(0, 0, width, height)
        clusterImageView.setImageDrawable(multiDrawable)
        val icon: Bitmap =
            clusterIconGenerator.makeIcon(cluster.size.toString())
        return BitmapDescriptorFactory.fromBitmap(icon)
    }

    override fun shouldRenderAsCluster(cluster: Cluster<PhotoMarker?>): Boolean {
        return cluster.size > 1
    }
}






