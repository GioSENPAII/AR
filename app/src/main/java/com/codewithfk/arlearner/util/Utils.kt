package com.codewithfk.arlearner.util

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
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

            // Crear instancia del modelo si no existe
            if (modelInstance.isEmpty()) {
                Log.d("AR_DEBUG", "Creating model instances...")
                try {
                    val instances = modelLoader.createInstancedModel(model, 10)
                    modelInstance.addAll(instances)
                    Log.d("AR_DEBUG", "Model instances created: ${instances.size}")
                } catch (e: Exception) {
                    Log.e("AR_ERROR", "Error creating model instances", e)
                    throw e
                }
            }

            // Verificar que tenemos instancias disponibles
            if (modelInstance.isEmpty()) {
                throw RuntimeException("No model instances available")
            }

            Log.d("AR_DEBUG", "Getting model instance...")
            val instance = modelInstance.removeLastOrNull()
            if (instance == null) {
                throw RuntimeException("Failed to get model instance")
            }

            // Crear el nodo del modelo
            Log.d("AR_DEBUG", "Creating model node...")
            val modelNode = ModelNode(
                modelInstance = instance,
                scaleToUnits = 0.2f
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
                    materialInstance = materialLoader.createColorInstance(Color.White)
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
            throw e
        }
    }
}