package com.example.customcamera.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.customcamera.databinding.ItemGalleryBinding
import java.io.File

class MediaAdapter(
    private val mediaFiles: List<File>,
    private val onMediaItemClick: (File) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemGalleryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MediaViewHolder(binding)
    }

    override fun getItemCount(): Int = mediaFiles.size

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaFile = mediaFiles[position]

        Glide.with(holder.binding.root).load(mediaFile).into(holder.binding.thumbnailImageView)

        if (mediaFile.extension == "mp4") {
            holder.binding.videoIndicatorIcon.visibility = View.VISIBLE
        } else {
            holder.binding.videoIndicatorIcon.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onMediaItemClick(mediaFile)
        }
    }

    inner class MediaViewHolder(val binding: ItemGalleryBinding) :
        RecyclerView.ViewHolder(binding.root)

}