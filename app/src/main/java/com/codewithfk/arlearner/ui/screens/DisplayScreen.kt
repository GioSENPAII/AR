package com.codewithfk.arlearner.ui.screens

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.codewithfk.arlearner.util.Utils
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView

@Composable
fun DisplayScreen(navController: NavController) {

    val modelName = "models/cat.glb"
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine = engine)
    val materialLoader = rememberMaterialLoader(engine = engine)
    val cameraNode = rememberARCameraNode(engine = engine)
    val childNodes = rememberNodes()
    val view = rememberView(engine = engine)
    val collisionSystem = rememberCollisionSystem(view = view)
    val modelInstances = remember { mutableListOf<ModelInstance>() }
    val frame = remember { mutableStateOf<Frame?>(null) }

    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }

    // --- NUEVO: Estado para guardar el tamaño del contenedor de la escena ---
    val viewSize = remember { mutableStateOf(IntSize.Zero) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    // --- CAMBIO: El Box principal ahora mide su propio tamaño ---
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
            onSessionUpdated = { _, updatedFrame ->
                frame.value = updatedFrame
            },
            sessionConfiguration = { session, config ->
                config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { motionEvent, node ->
                    if (node == null && childNodes.isEmpty()) {
                        frame.value?.hitTest(motionEvent.x, motionEvent.y)?.firstOrNull {
                            it.isValid(depthPoint = false, point = false)
                        }?.createAnchorOrNull()?.let { anchor ->
                            val anchorNode = Utils.createAnchorNode(
                                engine, modelLoader, materialLoader, modelInstances, anchor, modelName
                            )
                            childNodes.add(anchorNode)

                            try {
                                if (mediaPlayer.isPlaying) {
                                    mediaPlayer.stop(); mediaPlayer.reset()
                                }
                                val afd = context.assets.openFd("cancion.mp3")
                                mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                afd.close()
                                mediaPlayer.prepare(); mediaPlayer.start()
                            } catch (e: Exception) {
                                Log.e("AR_ERROR", "Error al reproducir audio", e)
                            }
                        }
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

        Text(
            text = "Toca un plano o arrastra el modelo",
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
            fontSize = 20.sp
        )

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
                    // --- CORRECCIÓN: Usamos el tamaño y el frame guardados en el estado ---
                    val centerX = viewSize.value.width / 2f
                    val centerY = viewSize.value.height / 2f
                    val hitResult = frame.value?.hitTest(centerX, centerY)
                    val newAnchor = hitResult?.firstOrNull { it.isValid(depthPoint = false, point = false) }?.createAnchorOrNull()

                    newAnchor?.let {
                        node.anchor = it
                        oldAnchor?.detach()
                    }
                }
            }) {
                Text(text = "Reposicionar")
            }

            Button(onClick = {
                mediaPlayer.stop()
                mediaPlayer.reset()
                childNodes.clear()
                modelInstances.clear()
            }) {
                Text(text = "Quitar Modelo")
            }
        }
    }
}