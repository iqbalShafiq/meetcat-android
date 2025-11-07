package id.usecase.meetcat.presentation.screen.camera

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

private const val TAG = "CameraScreen"
private val cameraExecutor = Executors.newSingleThreadExecutor()

@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onImageCaptured: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    var hasCameraPermission by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onImageCaptured(it) }
    }

    // Check permission on launch
    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                hasCameraPermission = true
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Cleanup camera resources
    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !hasCameraPermission -> {
                    // Permission denied screen
                    PermissionDeniedContent(
                        onNavigateBack = onNavigateBack,
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )
                }

                capturedImageUri != null -> {
                    // Image preview screen
                    ImagePreviewContent(
                        imageUri = capturedImageUri!!,
                        onRetake = { capturedImageUri = null },
                        onConfirm = {
                            onImageCaptured(capturedImageUri!!)
                            onNavigateBack()
                        }
                    )
                }

                else -> {
                    // Camera screen
                    CameraContent(
                        lifecycleOwner = lifecycleOwner,
                        lensFacing = lensFacing,
                        zoomRatio = zoomRatio,
                        onZoomChange = { newZoom ->
                            zoomRatio = newZoom
                            camera?.cameraControl?.setLinearZoom(newZoom)
                        },
                        onCameraReady = { newCamera, newImageCapture, provider ->
                            camera = newCamera
                            imageCapture = newImageCapture
                            cameraProvider = provider
                        },
                        onNavigateBack = onNavigateBack,
                        onFlipCamera = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        onOpenGallery = {
                            galleryLauncher.launch("image/*")
                        },
                        onCaptureImage = {
                            if (!isCapturing && imageCapture != null) {
                                isCapturing = true
                                captureImage(
                                    context = context,
                                    imageCapture = imageCapture!!,
                                    onImageCaptured = { uri ->
                                        capturedImageUri = uri
                                        isCapturing = false
                                    },
                                    onError = { exception ->
                                        Log.e(TAG, "Image capture failed", exception)
                                        isCapturing = false
                                    }
                                )
                            }
                        },
                        isCapturing = isCapturing
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onNavigateBack: () -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.size(16.dp))

        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = "This app needs camera access to take photos. Please grant camera permission to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.size(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = onNavigateBack,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Text("Cancel")
            }

            FloatingActionButton(
                onClick = onRequestPermission,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun ImagePreviewContent(
    imageUri: Uri,
    onRetake: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Captured image",
            modifier = Modifier.fillMaxSize()
        )

        // Bottom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FloatingActionButton(
                onClick = onRetake,
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Retake"
                )
            }

            FloatingActionButton(
                onClick = onConfirm,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm"
                )
            }
        }
    }
}

@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
private fun CameraContent(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    lensFacing: Int,
    zoomRatio: Float,
    onZoomChange: (Float) -> Unit,
    onCameraReady: (Camera, ImageCapture, ProcessCameraProvider) -> Unit,
    onNavigateBack: () -> Unit,
    onFlipCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    onCaptureImage: () -> Unit,
    isCapturing: Boolean,
    modifier: Modifier = Modifier
) {
    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var minZoom by remember { mutableFloatStateOf(1f) }
    var maxZoom by remember { mutableFloatStateOf(1f) }

    Box(modifier = modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { ctx ->
                val preview = PreviewView(ctx)
                previewView = preview
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    try {
                        val provider = cameraProviderFuture.get()
                        val previewUseCase = Preview.Builder().build()
                        val imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build()

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        provider.unbindAll()
                        val cam = provider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            previewUseCase,
                            imageCapture
                        )

                        previewUseCase.surfaceProvider = preview.surfaceProvider

                        // Get zoom range
                        val zoomState = cam.cameraInfo.zoomState.value
                        minZoom = zoomState?.minZoomRatio ?: 1f
                        maxZoom = zoomState?.maxZoomRatio ?: 1f

                        camera = cam
                        onCameraReady(cam, imageCapture, provider)
                    } catch (e: Exception) {
                        Log.e(TAG, "Camera initialization failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                preview
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(camera) {
                    // Pinch to zoom
                    detectTransformGestures { _, _, zoom, _ ->
                        camera?.let { cam ->
                            val currentZoom = cam.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                            val newZoom = (currentZoom * zoom).coerceIn(minZoom, maxZoom)
                            cam.cameraControl.setZoomRatio(newZoom)
                            // Normalize zoom for slider (0-1 range)
                            val normalizedZoom = (newZoom - minZoom) / (maxZoom - minZoom)
                            onZoomChange(normalizedZoom)
                        }
                    }
                }
                .pointerInput(camera, previewView) {
                    // Tap to focus
                    detectTapGestures { offset ->
                        camera?.let { cam ->
                            previewView?.let { preview ->
                                val factory = preview.meteringPointFactory
                                val point = factory.createPoint(offset.x, offset.y)
                                val action = FocusMeteringAction
                                    .Builder(point)
                                    .build()
                                cam.cameraControl.startFocusAndMetering(action)
                            }
                        }
                    }
                },
            update = { preview ->
                // Update when lens changes
                previewView = preview
            }
        )

        // Top controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onFlipCamera,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "Flip camera",
                    tint = Color.White
                )
            }
        }

        // Zoom slider
        if (maxZoom > minZoom) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        MaterialTheme.shapes.medium
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = "${(minZoom + zoomRatio * (maxZoom - minZoom)).format(1)}x",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
                Slider(
                    value = zoomRatio,
                    onValueChange = { newZoom ->
                        onZoomChange(newZoom)
                        camera?.let { cam ->
                            val actualZoom = minZoom + newZoom * (maxZoom - minZoom)
                            cam.cameraControl.setZoomRatio(actualZoom)
                        }
                    },
                    modifier = Modifier
                        .width(150.dp)
                        .padding(vertical = 8.dp),
                    valueRange = 0f..1f
                )
            }
        }

        // Bottom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gallery button
            FloatingActionButton(
                onClick = onOpenGallery,
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Open gallery",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Shutter button
            Surface(
                onClick = onCaptureImage,
                enabled = !isCapturing,
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = if (isCapturing) Color.Gray else Color.White
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(70.dp),
                            shape = CircleShape,
                            color = Color.White,
                            tonalElevation = 4.dp
                        ) {}
                    }
                }
            }

            // Placeholder for symmetry
            Spacer(modifier = Modifier.size(56.dp))
        }
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        cameraExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

private fun Float.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}
