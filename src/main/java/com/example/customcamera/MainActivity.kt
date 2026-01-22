package com.example.customcamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.customcamera.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        initializeAppPermissions()
    }

    private fun initializeAppPermissions() {
        val requiredPermissions = getRequiredPermissions()
        requestMissingPermissions(requiredPermissions)
    }

    private fun getRequiredPermissions(): List<String> {
        val permissionsList = mutableListOf<String>()

        // Камера всегда требуется
        permissionsList.add(Manifest.permission.CAMERA)

        // Разрешения в зависимости от версии Android
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Для Android 13+
            permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissionsList.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissionsList.add(Manifest.permission.RECORD_AUDIO)
        } else {
            // Для Android до 13
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionsList.add(Manifest.permission.RECORD_AUDIO)
        }

        return permissionsList
    }

    private fun requestMissingPermissions(requiredPermissions: List<String>) {
        val missingPermissions = getMissingPermissions(requiredPermissions)

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun getMissingPermissions(requiredPermissions: List<String>): List<String> {
        return requiredPermissions.filter { permission ->
            !isPermissionGranted(permission)
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            handlePermissionRequestResult(grantResults)
        }
    }

    private fun handlePermissionRequestResult(grantResults: IntArray) {
        val areAllPermissionsGranted = grantResults.all { result ->
            result == PackageManager.PERMISSION_GRANTED
        }

        if (!areAllPermissionsGranted) {
            finish()
        }
    }
}