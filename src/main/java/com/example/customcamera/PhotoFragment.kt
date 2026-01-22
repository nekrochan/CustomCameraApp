package com.example.customcamera

import android.content.ContentValues
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.customcamera.databinding.FragmentPhotoBinding
import androidx.core.graphics.drawable.toDrawable

class PhotoFragment : Fragment() {

    private lateinit var binding: FragmentPhotoBinding
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private val scaleGestureDetector by lazy {

        ScaleGestureDetector(requireContext(), object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val oldZoom = camera?.cameraInfo?.zoomState?.value ?: return false
                val newZoom = oldZoom.zoomRatio * detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(newZoom.coerceIn(oldZoom.minZoomRatio, oldZoom.maxZoomRatio))
                return true
            }
        })
    }
    private fun takePhoto() {

        val imageCapture = imageCapture ?: return
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "CustomCamera" + SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(System.currentTimeMillis()))
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CustomCamera")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(requireActivity().contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(e: ImageCaptureException) {
                    Log.e("CameraX", "Unable to take a photo: ${e.message}", e)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    binding.root.foreground = Color.WHITE.toDrawable()
                    binding.root.postDelayed({ binding.root.foreground = null }, 100)
                }
            }
        )
    }

    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            try {
                cameraProvider.unbindAll()
                imageCapture = ImageCapture.Builder().build()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                setupGestures()
            } catch (e: Exception) {
                Log.e("CameraX", "Unable to run camera: ", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setupGestures() {
        val viewFinder = binding.previewView
        viewFinder.setOnTouchListener { view, event ->
            scaleGestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                val factory = viewFinder.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF).build()
                camera?.cameraControl?.startFocusAndMetering(action)
                view.performClick()
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        startCamera()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentPhotoBinding.inflate(layoutInflater)
        binding.takePhoto.setOnClickListener { takePhoto() }
        binding.switchCam.setOnClickListener {
            if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            }
            startCamera()
        }
        binding.switchCam.setOnClickListener {
            cameraExecutor.shutdown()
            findNavController().navigate(PhotoFragmentDirections.actionMainFragmentToVideoFragment())
        }
        binding.toGallery.setOnClickListener {
            cameraExecutor.shutdown()
            findNavController().navigate(PhotoFragmentDirections.actionMainFragmentToGalleryFragment())
        }
        return binding.root
    }

}