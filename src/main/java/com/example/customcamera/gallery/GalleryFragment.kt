package com.example.customcamera.gallery

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.customcamera.gallery.MediaAdapter
import com.example.customcamera.databinding.FragmentGalleryBinding
import java.io.File

class GalleryFragment : Fragment() {

    private lateinit var binding: FragmentGalleryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentGalleryBinding.inflate(layoutInflater)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.recyclerView.adapter = MediaAdapter(getMediaFiles()) { mediaFile ->
            findNavController().navigate(
                GalleryFragmentDirections.actionGalleryFragmentToViewerFragment(
                    mediaFile.absolutePath
                )
            )
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val mediaFiles = getMediaFiles()
        binding.recyclerView.adapter = MediaAdapter(mediaFiles) { mediaFile ->
            findNavController().navigate(
                GalleryFragmentDirections.actionGalleryFragmentToViewerFragment(
                    mediaFile.absolutePath
                )
            )
        }
    }

    private fun getMediaFiles(): List<File> {
        val mediaFiles = mutableListOf<File>()

        val picturesDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "CustomCamera"
        )

        val videosDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "CustomCamera"
        )

        if (picturesDirectory.exists()) {
            val imageFiles = picturesDirectory.listFiles()?.filter { it.extension == "jpg" } ?: emptyList()
            mediaFiles.addAll(imageFiles)
        }

        if (videosDirectory.exists()) {
            val videoFiles = videosDirectory.listFiles()?.filter { it.extension == "mp4" } ?: emptyList()
            mediaFiles.addAll(videoFiles)
        }

        return mediaFiles.sortedByDescending { it.lastModified() }
    }
}