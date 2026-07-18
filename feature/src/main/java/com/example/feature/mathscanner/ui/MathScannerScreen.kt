package com.example.feature.mathscanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Camera
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Switch
import com.example.domain.scanner.PreprocessFilterMode

@Composable
fun MathScannerScreen(
    viewModel: MathScannerViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                cameraExecutor.shutdown()
            } catch (e: Exception) {
                android.util.Log.e("MathScanner", "Executor shutdown error: ${e.message}")
            }
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                android.util.Log.e("MathScanner", "Unbinding camera error: ${e.message}")
            }
        }
    }
    
    // Viewfinder line animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        val inputStream = context.contentResolver.openInputStream(it)
                        val decoded = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        decoded
                    }
                    if (bitmap != null) {
                        viewModel.onCaptureImage(bitmap)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MathScanner", "Error reading gallery image: ${e.message}")
                }
            }
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Camera permission granted
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Viewport
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(surfaceProvider)
                        }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, 
                                cameraSelector, 
                                preview, 
                                imageCapture
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("MathScanner", "Error binding camera lifecycle: ${e.message}")
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // 1. Semi-transparent black cutout overlay for center viewfinder
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            val viewfinderWidth = width * 0.85f
            val viewfinderHeight = height * 0.22f
            
            val left = (width - viewfinderWidth) / 2f
            val top = (height - viewfinderHeight) / 2f
            val right = left + viewfinderWidth
            val bottom = top + viewfinderHeight
            
            // Draw 4 outer rectangles to shade the non-scanned area
            drawRect(color = Color.Black.copy(alpha = 0.5f), size = androidx.compose.ui.geometry.Size(width, top))
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = androidx.compose.ui.geometry.Offset(0f, bottom),
                size = androidx.compose.ui.geometry.Size(width, height - bottom)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = androidx.compose.ui.geometry.Offset(0f, top),
                size = androidx.compose.ui.geometry.Size(left, viewfinderHeight)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = androidx.compose.ui.geometry.Offset(right, top),
                size = androidx.compose.ui.geometry.Size(width - right, viewfinderHeight)
            )
        }

        // 2. Neon bounding box corner brackets
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            val viewfinderWidth = width * 0.85f
            val viewfinderHeight = height * 0.22f
            
            val left = (width - viewfinderWidth) / 2f
            val top = (height - viewfinderHeight) / 2f
            val right = left + viewfinderWidth
            val bottom = top + viewfinderHeight
            
            val bracketLen = 24.dp.toPx()
            val strokeWidth = 3.dp.toPx()
            
            // Top-Left corner
            drawLine(primaryColor, androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(left + bracketLen, top), strokeWidth)
            drawLine(primaryColor, androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(left, top + bracketLen), strokeWidth)
            
            // Top-Right corner
            drawLine(primaryColor, androidx.compose.ui.geometry.Offset(right, top), androidx.compose.ui.geometry.Offset(right - bracketLen, top), strokeWidth)
            drawLine(primaryColor, androidx.compose.ui.geometry.Offset(right, top), androidx.compose.ui.geometry.Offset(right, top + bracketLen), strokeWidth)
            
            // Bottom-Left corner
            drawLine(primaryColor, androidx.compose.ui.geometry.Offset(left, bottom), androidx.compose.ui.geometry.Offset(left + bracketLen, bottom), strokeWidth)
            drawLine(primaryColor, androidx.compose.ui.geometry.Offset(left, bottom), androidx.compose.ui.geometry.Offset(left, bottom - bracketLen), strokeWidth)
            
            // Bottom-Right corner
            drawLine(primaryColor, androidx.compose.ui.geometry.Offset(right, bottom), androidx.compose.ui.geometry.Offset(right - bracketLen, bottom), strokeWidth)
            drawLine(primaryColor, androidx.compose.ui.geometry.Offset(right, bottom), androidx.compose.ui.geometry.Offset(right, bottom - bracketLen), strokeWidth)
        }

        // 3. Animating scanline
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            val viewfinderWidth = width * 0.85f
            val viewfinderHeight = height * 0.22f
            
            val left = (width - viewfinderWidth) / 2f
            val top = (height - viewfinderHeight) / 2f
            
            val currentY = top + (viewfinderHeight * scanLineProgress)
            
            drawLine(
                color = primaryColor.copy(alpha = 0.8f),
                start = androidx.compose.ui.geometry.Offset(left + 2.dp.toPx(), currentY),
                end = androidx.compose.ui.geometry.Offset(left + viewfinderWidth - 2.dp.toPx(), currentY),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        // Top Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        // Guideline Tooltip above the box
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 190.dp)
                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Place equation inside the box",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }

        // Floating Control Panel for Crop & Preprocessing Filters
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Filter Mode row of chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enhancement",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PreprocessFilterMode.values().forEach { mode ->
                            val isSelected = state.filterMode == mode
                            val label = when (mode) {
                                PreprocessFilterMode.ORIGINAL -> "None"
                                PreprocessFilterMode.ENHANCED_GRAY -> "Gray"
                                PreprocessFilterMode.HIGH_CONTRAST_BW -> "B&W"
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setFilterMode(mode) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Crop Switch row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Crop to Viewfinder",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Increases accuracy of math text recognition",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.useCrop,
                        onCheckedChange = { viewModel.setUseCrop(it) }
                    )
                }
            }
        }

        // Capture/Action Buttons at the very bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                shape = CircleShape,
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.size(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.Photo, contentDescription = "Pick equation from gallery", modifier = Modifier.size(28.dp))
            }

            Button(
                onClick = {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    try {
                        imageCapture.takePicture(
                            cameraExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    scope.launch {
                                        try {
                                            val bitmap = withContext(Dispatchers.Default) {
                                                val converted = image.toBitmap()
                                                val rotated = converted.rotate(image.imageInfo.rotationDegrees.toFloat())
                                                if (converted != rotated) converted.recycle()
                                                rotated
                                            }
                                            viewModel.onCaptureImage(bitmap)
                                        } catch (e: Throwable) {
                                            android.util.Log.e("MathScanner", "Bitmap processing/rotation failed: ${e.message}")
                                        } finally {
                                            image.close()
                                        }
                                    }
                                }
                            }
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("MathScanner", "Capture failed: ${e.message}")
                    }
                },
                shape = CircleShape,
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.size(84.dp)
            ) {
                Icon(Icons.Default.Camera, contentDescription = "Capture math equation", modifier = Modifier.size(42.dp))
            }
        }

        // Loading Scrim
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Preprocessing & solving offline...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Results View Dialog with Side-by-Side raw/processed toggle
        if (state.result != null || state.error != null) {
            var recognizedText by remember(state.result) { mutableStateOf(state.result ?: "") }
            val clipboardManager = LocalClipboardManager.current
            var showProcessedImage by remember { mutableStateOf(true) }

            Dialog(
                onDismissRequest = { viewModel.clearResult() },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.error != null) "Scan Error" else "Scan Result",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearResult() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close dialog")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Visual Preprocessing Tab Controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (showProcessedImage) MaterialTheme.colorScheme.surface else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { showProcessedImage = true }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "OCR View",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (showProcessedImage) FontWeight.Bold else FontWeight.Normal,
                                    color = if (showProcessedImage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (!showProcessedImage) MaterialTheme.colorScheme.surface else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { showProcessedImage = false }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Original Image",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (!showProcessedImage) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!showProcessedImage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Selected Image Preview
                        if (showProcessedImage) {
                            state.processedImage?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "Preprocessed Crop Preview",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                )
                            } ?: Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No preprocessed image available", style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            state.capturedImage?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "Raw Captured Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                )
                            } ?: Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No original image available", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (state.error != null) {
                            Text(
                                text = state.error!!,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            TextField(
                                value = recognizedText,
                                onValueChange = { recognizedText = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Recognized Math") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Button(
                                onClick = { clipboardManager.setText(AnnotatedString(recognizedText)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Text")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun android.graphics.Bitmap.rotate(degrees: Float): android.graphics.Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return android.graphics.Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
