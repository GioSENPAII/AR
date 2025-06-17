package com.codewithfk.arlearner.util

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode

object Utils {

    fun createAnchorNode(
        engine: Engine,
        modelLoader: ModelLoader,
        materialLoader: MaterialLoader,
        modelInstance: MutableList<ModelInstance>,
        anchor: Anchor,
        model: String
    ): AnchorNode {
        try {
            Log.d("AR_DEBUG", "Creating anchor node with model: $model")

            // Crear el nodo ancla
            val anchorNode = AnchorNode(engine = engine, anchor = anchor)
            Log.d("AR_DEBUG", "Anchor node created")

            // Intentar crear instancia del modelo si no existe
            if (modelInstance.isEmpty()) {
                Log.d("AR_DEBUG", "Creating model instances...")
                try {
                    val instances = modelLoader.createInstancedModel("models/$model", 5)
                    modelInstance.addAll(instances)
                    Log.d("AR_DEBUG", "Model instances created: ${instances.size}")
                } catch (e: Exception) {
                    Log.e("AR_ERROR", "Error creating model instances, using fallback cube", e)
                    // Si falla, usar un cubo colorido como fallback
                    return createFallbackCubeNode(engine, materialLoader, anchor, model)
                }
            }

            // Verificar que tenemos instancias disponibles
            if (modelInstance.isEmpty()) {
                Log.w("AR_DEBUG", "No model instances available, using fallback")
                return createFallbackCubeNode(engine, materialLoader, anchor, model)
            }

            Log.d("AR_DEBUG", "Getting model instance...")
            val instance = modelInstance.removeLastOrNull()
            if (instance == null) {
                Log.w("AR_DEBUG", "Failed to get model instance, using fallback")
                return createFallbackCubeNode(engine, materialLoader, anchor, model)
            }

            // Crear el nodo del modelo con tamaño más pequeño para menos sensibilidad
            Log.d("AR_DEBUG", "Creating model node...")
            val modelNode = ModelNode(
                modelInstance = instance,
                scaleToUnits = 0.2f // Tamaño base pequeño (20cm) para reducir sensibilidad aparente
            ).apply {
                isEditable = true
            }
            Log.d("AR_DEBUG", "Model node created")

            // Crear bounding box
            try {
                Log.d("AR_DEBUG", "Creating bounding box...")
                val boundingBox = CubeNode(
                    engine = engine,
                    size = modelNode.extents,
                    center = modelNode.center,
                    materialInstance = materialLoader.createColorInstance(Color.Cyan.copy(alpha = 0.3f))
                ).apply {
                    isVisible = false
                }
                Log.d("AR_DEBUG", "Bounding box created")

                // Agregar bounding box como hijo del modelo
                modelNode.addChildNode(boundingBox)

                // Configurar eventos de edición
                listOf(modelNode, anchorNode).forEach { node ->
                    node.onEditingChanged = { editingTransforms ->
                        boundingBox.isVisible = editingTransforms.isNotEmpty()
                    }
                }
                Log.d("AR_DEBUG", "Editing events configured")

            } catch (e: Exception) {
                Log.e("AR_ERROR", "Error creating bounding box (non-critical)", e)
                // Continuar sin bounding box si hay error
            }

            // Agregar modelo como hijo del ancla
            Log.d("AR_DEBUG", "Adding model node to anchor...")
            anchorNode.addChildNode(modelNode)

            Log.d("AR_DEBUG", "Anchor node setup complete")
            return anchorNode

        } catch (e: Exception) {
            Log.e("AR_ERROR", "Error in createAnchorNode: ${e.message}", e)
            e.printStackTrace()
            // En caso de error, usar cubo fallback
            return createFallbackCubeNode(engine, materialLoader, anchor, model)
        }
    }

    private fun createFallbackCubeNode(
        engine: Engine,
        materialLoader: MaterialLoader,
        anchor: Anchor,
        model: String
    ): AnchorNode {
        Log.d("AR_DEBUG", "Creating fallback cube for model: $model")

        val anchorNode = AnchorNode(engine = engine, anchor = anchor)

        // Elegir color basado en el modelo
        val color = when {
            model.contains("iron_man") -> Color.Red
            model.contains("waifu") -> Color.Magenta
            model.contains("sus") -> Color.Green
            else -> Color.Blue
        }

        val cubeNode = CubeNode(
            engine = engine,
            size = Float3(0.15f, 0.15f, 0.15f), // Usar Float3 para todas las dimensiones
            center = Float3(0f, 0.08f, 0f),
            materialInstance = materialLoader.createColorInstance(color)
        ).apply {
            isEditable = true
        }

        anchorNode.addChildNode(cubeNode)
        Log.d("AR_DEBUG", "Fallback cube created successfully")

        return anchorNode
    }
}