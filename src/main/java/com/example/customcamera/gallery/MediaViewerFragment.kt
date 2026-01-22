package com.example.customcamera.gallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.customcamera.databinding.FragmentMediaViewerBinding
import java.io.File

class MediaViewerFragment : Fragment() {
    private lateinit var binding: FragmentMediaViewerBinding
    private val navigationArgs: MediaViewerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentMediaViewerBinding.inflate(layoutInflater)

        if (navigationArgs.path == null) {
            findNavController().navigate(
                MediaViewerFragmentDirections.actionViewerFragmentToGalleryFragment()
            )
            return binding.root
        }

        val mediaFile = File(navigationArgs.path.toString())
        displayMediaContent(mediaFile)
        setupDeleteButton(mediaFile)

        return binding.root
    }

    private fun displayMediaContent(mediaFile: File) {
        val isVideoFile = mediaFile.extension == "mp4"

        if (isVideoFile) {
            showVideoPlayer(mediaFile)
            hideImageView()
        } else {
            showImage(mediaFile)
            hideVideoPlayer()
        }
    }

    private fun showImage(imageFile: File) {
        binding.videoView.visibility = View.GONE
        binding.imageView.visibility = View.VISIBLE
        Glide.with(this).load(imageFile).into(binding.imageView)
    }

    private fun showVideoPlayer(videoFile: File) {
        binding.imageView.visibility = View.GONE
        binding.videoView.visibility = View.VISIBLE
        binding.videoView.setVideoPath(videoFile.absolutePath)
        binding.videoView.setMediaController(MediaController(requireContext()))
        binding.videoView.start()
    }

    private fun hideImageView() {
        binding.imageView.visibility = View.GONE
    }

    private fun hideVideoPlayer() {
        binding.videoView.visibility = View.GONE
    }

    private fun setupDeleteButton(mediaFile: File) {
        binding.deletingButton.setOnClickListener {
            deleteMediaFile(mediaFile)
        }
    }

    private fun deleteMediaFile(mediaFile: File) {
        if (mediaFile.exists()) {
            try {
                val isDeleted = mediaFile.delete()
                if (isDeleted) {
                    navigateBackToGallery()
                } else {
                    Log.e("MediaViewer", "Failed to delete file: ${mediaFile.name}")
                }
            } catch (exception: Exception) {
                Log.e("MediaViewer", "Error deleting file: ${mediaFile.name}", exception)
            }
        }
    }

    private fun navigateBackToGallery() {
        findNavController().navigate(
            MediaViewerFragmentDirections.actionViewerFragmentToGalleryFragment()
        )
    }
}