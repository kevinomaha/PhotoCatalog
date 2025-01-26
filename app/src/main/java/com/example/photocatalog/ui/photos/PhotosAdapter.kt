package com.example.photocatalog.ui.photos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photocatalog.data.models.Photo
import com.example.photocatalog.databinding.ItemPhotoBinding
import java.text.SimpleDateFormat
import java.util.*

class PhotosAdapter(
    private val onPhotoClick: (Photo) -> Unit,
    private val onPhotoLongClick: (Photo) -> Unit
) : ListAdapter<Photo, PhotosAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PhotoViewHolder(
        private val binding: ItemPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(photo: Photo) {
            binding.apply {
                Glide.with(photoImage)
                    .load(photo.thumbnailUrl)
                    .centerCrop()
                    .into(photoImage)

                photoDate.text = photo.timestamp?.let { dateFormat.format(it) } ?: ""
                locationIcon.visibility = if (photo.location != null) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                root.setOnClickListener { onPhotoClick(photo) }
                root.setOnLongClickListener {
                    onPhotoLongClick(photo)
                    true
                }
            }
        }
    }
}

class PhotoDiffCallback : DiffUtil.ItemCallback<Photo>() {
    override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
        return oldItem == newItem
    }
}
