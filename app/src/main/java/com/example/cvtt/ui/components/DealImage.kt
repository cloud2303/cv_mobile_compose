package com.example.cvtt.ui.components

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import android.content.Context
import android.database.Cursor
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DealImage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var dealImageUri by remember { mutableStateOf<String?>(null) }
   val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        // Handle the returned Uri
       if (uri != null) {
           Log.d("PhotoPicker", "Selected URI: $uri")
              imageUri = uri
       } else {
           Log.d("PhotoPicker", "No media selected")
       }
    }
    fun dealImage() {
        if(imageUri == null) {
            return
        }
        val imageBytes = context.contentResolver.openInputStream(imageUri!!)?.readBytes()
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputDir = File(downloadsDir, "cvtt")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val filePath = outputDir.let {
            val file = File(it, "image.jpg")
            file.writeBytes(imageBytes!!)
            file.absolutePath
        }
        val outputFilePath = File(outputDir, "processed_image_gray.jpg").absolutePath
        val result = processImage(filePath, outputFilePath)
        Log.d("DealImage", "Result: $result")
        dealImageUri = outputFilePath
        //        Log.d("DealImage", "Result: $result")
//        val fileUri = Uri.fromFile(result?.let { File(it) })



    }
    Box(Modifier.safeDrawingPadding()) {
        Column(
            Modifier.verticalScroll(rememberScrollState())
        ) {
            Button(onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                Text(text ="Select Image")

            }
            imageUri?.let {
                AsyncImage(model = imageUri, contentDescription = "Selected Image", modifier = Modifier.fillMaxWidth())
            }
            Button(onClick = {dealImage() }) {
                Text(text = "处理图片")
            }
            dealImageUri?.let {
                AsyncImage(model = dealImageUri, contentDescription = "Dealed Image", modifier = Modifier.fillMaxWidth())
            }
        }

    }
}

// Define external native function
external fun processImage(inputPath: String, outputPath: String): String




suspend fun getFileDescriptor(context: Context, uri: Uri): ParcelFileDescriptor? {
    return withContext(Dispatchers.IO) {
        context.contentResolver.openFileDescriptor(uri, "r")
    }
}