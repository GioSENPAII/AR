package com.codewithfk.arlearner.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.codewithfk.arlearner.util.Utils
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView

@Composable
fun DisplayScreen(navController: NavController) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var arCoreStatus by remember { mutableStateOf<ARCoreStatus?>(null) }

    // Launcher para permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Verificar estado de ARCore
    LaunchedEffect(Unit) {
        try {
            val availability = ArCoreApk.getInstance().checkAvailability(context)
            arCoreStatus = when (availability) {
                ArCoreApk.Availability.SUPPORTED_INSTALLED -> {
                    ARCoreStatus.Available
                }
                ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD -> {
                    ARCoreStatus.NeedsUpdate
                }
                ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                    ARCoreStatus.NotInstalled
                }
                ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
                    ARCoreStatus.DeviceNotSupported
                }
                ArCoreApk.Availability.UNKNOWN_CHECKING -> {
                    ARCoreStatus.Checking
                }
                ArCoreApk.Availability.UNKNOWN_ERROR -> {
                    ARCoreStatus.UnknownError
                }
                ArCoreApk.Availability.UNKNOWN_TIMED_OUT -> {
                    ARCoreStatus.TimedOut
                }
                else -> ARCoreStatus.UnknownError
            }

            Log.d("AR_DEBUG", "ARCore availability: $availability")
            Log.d("AR_DEBUG", "ARCore status: $arCoreStatus")

        } catch (e: Exception) {
            arCoreStatus = ARCoreStatus.Error("Error verificando ARCore: ${e.message}")
            Log.e("AR_ERROR", "Error checking ARCore availability", e)
        }
    }

    // Solicitar permisos si no los tenemos
    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Mostrar interfaz según el estado
    when {
        !hasCameraPermission -> {
            CameraPermissionScreen(
                onRequestPermission = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onGoBack = { navController.popBackStack() }
            )
        }

        arCoreStatus == null || arCoreStatus == ARCoreStatus.Checking -> {
            LoadingScreen("Verificando compatibilidad AR...")
        }

        arCoreStatus == ARCoreStatus.Available -> {
            ARScreenContent(navController = navController)
        }

        else -> {
            ARCoreErrorScreen(
                status = arCoreStatus!!,
                onGoBack = { navController.popBackStack() },
                context = context
            )
        }
    }
}

@Composable
private fun CameraPermissionScreen(
    onRequestPermission: () -> Unit,
    onGoBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📸",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Permiso de Cámara Requerido",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Se necesita acceso a la cámara para usar las funciones de Realidad Aumentada",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    Button(
                        onClick = onGoBack,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(onClick = onRequestPermission) {
                        Text("Otorgar Permiso")
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🔄",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = message,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ARCoreErrorScreen(
    status: ARCoreStatus,
    onGoBack: () -> Unit,
    context: android.content.Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val (emoji, title, description, actionText, action) = when (status) {
                    ARCoreStatus.NotInstalled -> {
                        Tuple5(
                            "📦",
                            "ARCore No Instalado",
                            "Google Play Services for AR no está instalado en tu dispositivo. Es necesario para usar funciones de AR.",
                            "Instalar ARCore"
                        ) {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.ar.core")
                                setPackage("com.android.vending")
                            }
                            context.startActivity(intent)
                        }
                    }

                    ARCoreStatus.NeedsUpdate -> {
                        Tuple5(
                            "🔄",
                            "Actualización Requerida",
                            "Tu versión de ARCore está desactualizada. Necesitas actualizarla para usar AR.",
                            "Actualizar ARCore"
                        ) {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.ar.core")
                                setPackage("com.android.vending")
                            }
                            context.startActivity(intent)
                        }
                    }

                    ARCoreStatus.DeviceNotSupported -> {
                        Tuple5(
                            "❌",
                            "Dispositivo No Compatible",
                            "Tu dispositivo no es compatible con ARCore. Las funciones de AR no están disponibles.",
                            null,
                            null
                        )
                    }

                    is ARCoreStatus.Error -> {
                        Tuple5(
                            "⚠️",
                            "Error de ARCore",
                            status.message,
                            null,
                            null
                        )
                    }

                    else -> {
                        Tuple5(
                            "❓",
                            "Estado Desconocido",
                            "Hay un problema desconocido con ARCore.",
                            null,
                            null
                        )
                    }
                }

                Text(
                    text = emoji,
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row {
                    Button(
                        onClick = onGoBack,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Volver")
                    }

                    if (actionText != null && action != null) {
                        Button(onClick = action) {
                            Text(actionText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ARScreenContent(navController: NavController) {
    val modelName = "models/cat.glb"
    val context = LocalContext.current

    // Verificar que el modelo existe
    var modelExists by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        try {
            context.assets.open(modelName).use {
                Log.d("AR_DEBUG", "Model file found: $modelName")
                modelExists = true
            }
        } catch (e: Exception) {
            Log.e("AR_ERROR", "Model file not found: $modelName", e)
            modelExists = false
        }
    }

    // Mostrar error si el modelo no existe
    if (modelExists == false) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "❌",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Archivo de modelo no encontrado",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "El archivo $modelName no está en la carpeta assets",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Volver")
            }
        }
        return
    }

    // Mostrar loading mientras verificamos el modelo
    if (modelExists == null) {
        LoadingScreen("Verificando recursos...")
        return
    }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine = engine)
    val materialLoader = rememberMaterialLoader(engine = engine)
    val cameraNode = rememberARCameraNode(engine = engine)
    val childNodes = rememberNodes()
    val view = rememberView(engine = engine)
    val collisionSystem = rememberCollisionSystem(view = view)
    val modelInstances = remember { mutableListOf<ModelInstance>() }
    val frame = remember { mutableStateOf<Frame?>(null) }

    val mediaPlayer = remember { MediaPlayer() }
    val viewSize = remember { mutableStateOf(IntSize.Zero) }

    var trackingFailureReason by remember { mutableStateOf<TrackingFailureReason?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer.release()
            } catch (e: Exception) {
                Log.e("AR_ERROR", "Error releasing media player", e)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { viewSize.value = it }
    ) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            childNodes = childNodes,
            engine = engine,
            view = view,
            modelLoader = modelLoader,
            collisionSystem = collisionSystem,
            planeRenderer = true,
            cameraNode = cameraNode,
            materialLoader = materialLoader,
            onSessionUpdated = { session, updatedFrame ->
                frame.value = updatedFrame

                val camera = updatedFrame.camera
                if (camera.trackingState == com.google.ar.core.TrackingState.TRACKING) {
                    trackingFailureReason = null
                } else {
                    trackingFailureReason = camera.trackingFailureReason
                }
            },
            onSessionFailed = { exception ->
                val message = when (exception) {
                    is UnavailableArcoreNotInstalledException ->
                        "ARCore no está instalado"
                    is UnavailableApkTooOldException ->
                        "ARCore necesita actualización"
                    is UnavailableDeviceNotCompatibleException ->
                        "Dispositivo no compatible con AR"
                    is UnavailableSdkTooOldException ->
                        "Versión de Android muy antigua"
                    is UnavailableUserDeclinedInstallationException ->
                        "Usuario rechazó instalación de ARCore"
                    else -> "Error de sesión AR: ${exception.message}"
                }
                Log.e("AR_ERROR", "AR Session failed: $message", exception)
            },
            sessionConfiguration = { session, config ->
                config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { motionEvent, node ->
                    Log.d("AR_DEBUG", "Tap detected at (${motionEvent.x}, ${motionEvent.y})")

                    if (node == null && childNodes.isEmpty()) {
                        try {
                            val currentFrame = frame.value
                            if (currentFrame == null) {
                                Log.w("AR_DEBUG", "Frame is null")
                                return@rememberOnGestureListener
                            }

                            Log.d("AR_DEBUG", "Performing hit test...")
                            val hitResults = currentFrame.hitTest(motionEvent.x, motionEvent.y)
                            Log.d("AR_DEBUG", "Hit test returned ${hitResults.size} results")

                            val validHit = hitResults.firstOrNull { hitResult ->
                                val isValid = hitResult.isValid(depthPoint = false, point = false)
                                Log.d("AR_DEBUG", "Hit result valid: $isValid")
                                isValid
                            }

                            if (validHit == null) {
                                Log.w("AR_DEBUG", "No valid hit found")
                                return@rememberOnGestureListener
                            }

                            Log.d("AR_DEBUG", "Creating anchor...")
                            val anchor = validHit.createAnchorOrNull()
                            if (anchor == null) {
                                Log.w("AR_DEBUG", "Failed to create anchor")
                                return@rememberOnGestureListener
                            }

                            Log.d("AR_DEBUG", "Creating anchor node...")
                            val anchorNode = Utils.createAnchorNode(
                                engine, modelLoader, materialLoader, modelInstances, anchor, modelName
                            )

                            Log.d("AR_DEBUG", "Adding anchor node to scene...")
                            childNodes.add(anchorNode)
                            Log.d("AR_DEBUG", "Model placed successfully!")

                            // Reproducir audio con manejo de errores mejorado
                            try {
                                Log.d("AR_DEBUG", "Starting audio playback...")
                                if (mediaPlayer.isPlaying) {
                                    mediaPlayer.stop()
                                    mediaPlayer.reset()
                                }

                                val afd = context.assets.openFd("cancion.mp3")
                                mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                afd.close()
                                mediaPlayer.prepare()
                                mediaPlayer.start()
                                Log.d("AR_DEBUG", "Audio started successfully")
                            } catch (audioException: Exception) {
                                Log.e("AR_ERROR", "Error with audio (non-critical)", audioException)
                                // No hacer nada crítico aquí, el modelo ya se colocó
                            }

                        } catch (e: Exception) {
                            Log.e("AR_ERROR", "Error al crear modelo: ${e.message}", e)
                            e.printStackTrace()
                            // Aquí podrías mostrar un Toast o mensaje al usuario
                        }
                    } else {
                        Log.d("AR_DEBUG", "Tap ignored - node exists or childNodes not empty")
                    }
                },
                onScroll = { e1, e2, node, scrollDelta ->
                    node?.let { draggedNode ->
                        val oldAnchor = (draggedNode as? AnchorNode)?.anchor
                        val hitTestResult = frame.value?.hitTest(e2.x, e2.y)
                        val newAnchor = hitTestResult?.firstOrNull {
                            it.isValid(depthPoint = false, point = false)
                        }?.createAnchorOrNull()
                        newAnchor?.let {
                            (draggedNode as? AnchorNode)?.anchor = it
                            oldAnchor?.detach()
                        }
                    }
                }
            )
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (trackingFailureReason) {
                TrackingFailureReason.INSUFFICIENT_LIGHT -> {
                    Text(
                        text = "Necesita más luz para funcionar",
                        fontSize = 16.sp,
                        color = Color.Yellow,
                        textAlign = TextAlign.Center
                    )
                }
                TrackingFailureReason.EXCESSIVE_MOTION -> {
                    Text(
                        text = "Mueve el dispositivo más lentamente",
                        fontSize = 16.sp,
                        color = Color.Yellow,
                        textAlign = TextAlign.Center
                    )
                }
                TrackingFailureReason.INSUFFICIENT_FEATURES -> {
                    Text(
                        text = "Apunta a una superficie con más detalles",
                        fontSize = 16.sp,
                        color = Color.Yellow,
                        textAlign = TextAlign.Center
                    )
                }
                null -> {
                    Text(
                        text = "Mueve la cámara para detectar superficies\nToca un plano para colocar el modelo",
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    Text(
                        text = "Problema de tracking AR",
                        fontSize = 16.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                val modelNode = childNodes.firstOrNull() as? AnchorNode
                modelNode?.let { node ->
                    val oldAnchor = node.anchor
                    val centerX = viewSize.value.width / 2f
                    val centerY = viewSize.value.height / 2f
                    val hitResult = frame.value?.hitTest(centerX, centerY)
                    val newAnchor = hitResult?.firstOrNull {
                        it.isValid(depthPoint = false, point = false)
                    }?.createAnchorOrNull()

                    newAnchor?.let {
                        node.anchor = it
                        oldAnchor?.detach()
                    }
                }
            }) {
                Text(text = "Reposicionar")
            }

            Button(onClick = {
                try {
                    mediaPlayer.stop()
                    mediaPlayer.reset()
                } catch (e: Exception) {
                    Log.e("AR_ERROR", "Error stopping media player", e)
                }
                childNodes.clear()
                modelInstances.clear()
            }) {
                Text(text = "Quitar Modelo")
            }
        }
    }
}

// Clases auxiliares
sealed class ARCoreStatus {
    object Available : ARCoreStatus()
    object NotInstalled : ARCoreStatus()
    object NeedsUpdate : ARCoreStatus()
    object DeviceNotSupported : ARCoreStatus()
    object Checking : ARCoreStatus()
    object TimedOut : ARCoreStatus()
    object UnknownError : ARCoreStatus()
    data class Error(val message: String) : ARCoreStatus()
}

data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)