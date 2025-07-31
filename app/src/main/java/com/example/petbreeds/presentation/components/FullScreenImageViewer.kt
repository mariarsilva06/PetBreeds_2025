package com.example.petbreeds.presentation.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

@Composable
fun FullScreenImageViewer(
    images: List<String>,
    initialIndex: Int = 0,
    petName: String = "Pet",
    onDismiss: () -> Unit
) {
    var currentImageIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, images.size - 1)) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }

    if (images.isEmpty()) {
        onDismiss()
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Main Image
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(images[currentImageIndex])
                    .crossfade(true)
                    .build(),
                contentDescription = "$petName image ${currentImageIndex + 1}",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { /* Prevent click-through */ },
                contentScale = ContentScale.Fit
            )

            // Top Bar with Close and Download
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Image Counter
                if (images.size > 1) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .clip(CircleShape)
                    ) {
                        Text(
                            text = "${currentImageIndex + 1} / ${images.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Download Button
                IconButton(
                    onClick = {
                        if (!isDownloading) {
                            scope.launch {
                                downloadImage(
                                    context = context,
                                    imageUrl = images[currentImageIndex],
                                    fileName = "${petName}_${currentImageIndex + 1}",
                                    onStart = { isDownloading = true },
                                    onComplete = { isDownloading = false }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Navigation Arrows (only show if more than 1 image)
            if (images.size > 1) {
                // Previous Arrow
                if (currentImageIndex > 0) {
                    IconButton(
                        onClick = { currentImageIndex-- },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(16.dp)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIos,
                            contentDescription = "Previous image",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Next Arrow
                if (currentImageIndex < images.size - 1) {
                    IconButton(
                        onClick = { currentImageIndex++ },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(16.dp)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Next image",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Bottom Info Bar
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = petName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    if (images.size > 1) {
                        Text(
                            text = "Swipe or use arrows to navigate",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

private suspend fun downloadImage(
    context: Context,
    imageUrl: String,
    fileName: String,
    onStart: () -> Unit,
    onComplete: () -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            onStart()

            // Create image request
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .build()

            // Get the image loader and execute the request
            val imageLoader = coil.ImageLoader(context)
            val drawable = imageLoader.execute(request).drawable

            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap

                // Save to MediaStore (Android 10+)
                val contentValues = android.content.ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "${fileName}_${System.currentTimeMillis()}.jpg")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PetBreeds")
                }

                val resolver = context.contentResolver
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                imageUri?.let { uri ->
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                    }

                    // Show success message on main thread
                    withContext(Dispatchers.Main) {
                        // You could show a Toast here if you want
                        // Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            // Handle error
            withContext(Dispatchers.Main) {
                // You could show an error Toast here if you want
                // Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        } finally {
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}