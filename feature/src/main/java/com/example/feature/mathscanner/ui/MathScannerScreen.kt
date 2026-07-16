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

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Camera
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun MathScannerScreen(
    viewModel: MathScannerViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(it))
            viewModel.onCaptureImage(bitmap)
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, handle camera
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            android.util.Log.e("MathScanner", "Error processing selected file")
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        // Capture Buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    // We need a way to only launch after permission is granted.
                    // This is simple, but might need to be refined.
                    // For now, I'll just trigger it.
                    imageCapture.takePicture(
                        cameraExecutor,
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val bitmap = image.toBitmap().rotate(image.imageInfo.rotationDegrees.toFloat())
                                viewModel.onCaptureImage(bitmap)
                                image.close()
                            }
                        }
                    )
                },
                shape = CircleShape,
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.Camera, contentDescription = "Camera", modifier = Modifier.size(36.dp))
            }
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                shape = CircleShape,
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.Photo, contentDescription = "Gallery", modifier = Modifier.size(36.dp))
            }
        }

        // Loading Overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Processing image...", color = Color.White)
                }
            }
        }

        // Result Dialog
        if (state.result != null || state.error != null) {
            var recognizedText by remember(state.result) { mutableStateOf(state.result ?: "") }
            val clipboardManager = LocalClipboardManager.current

            Dialog(
                onDismissRequest = { viewModel.clearResult() },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                                text = if (state.isSolved) "Solution" else if (state.error != null) "Error" else "Recognized Text",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearResult() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        state.capturedImage?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Captured Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(vertical = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (state.error != null) {
                            Text(
                                text = state.error!!,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (!state.isSolved) {
                            TextField(
                                value = recognizedText,
                                onValueChange = { recognizedText = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Recognized Text") },
                                colors = TextFieldDefaults.colors()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { clipboardManager.setText(AnnotatedString(recognizedText)) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy Text")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.solveMath(recognizedText) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continue")
                            }
                        } else {
                            Text(
                                text = state.result ?: "",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { clipboardManager.setText(AnnotatedString(state.result ?: "")) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy Result")
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
