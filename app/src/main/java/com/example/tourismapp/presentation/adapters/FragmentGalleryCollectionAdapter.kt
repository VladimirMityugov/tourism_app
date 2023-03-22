package com.example.tourismapp.presentation.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tourismapp.ui.main.photos.GallerySingleFragment


class FragmentGalleryCollectionAdapter (
    fragment: Fragment,
    private val gallerySize:Int
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = gallerySize

    override fun createFragment(position: Int): Fragment = GallerySingleFragment.newInstance(position)

}