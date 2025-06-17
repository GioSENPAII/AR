package com.codewithfk.arlearner.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.codewithfk.arlearner.ui.navigation.DisplayScreen

data class ARModel(
    val fileName: String,
    val displayName: String,
    val description: String,
    val emoji: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

@Composable
fun HomeScreen(navController: NavController) {
    val models = listOf(
        ARModel(
            fileName = "iron_man.glb",
            displayName = "Iron Man",
            description = "El héroe blindado de Marvel",
            emoji = "🤖",
            primaryColor = Color(0xFFDC143C),
            secondaryColor = Color(0xFFFFD700)
        ),
        ARModel(
            fileName = "waifu.glb",
            displayName = "Waifu",
            description = "Personaje anime en 3D",
            emoji = "👩‍🎨",
            primaryColor = Color(0xFFFF69B4),
            secondaryColor = Color(0xFF9370DB)
        ),
        ARModel(
            fileName = "sus.glb",
            displayName = "Sus",
            description = "Among Us crewmate",
            emoji = "🚀",
            primaryColor = Color(0xFF32CD32),
            secondaryColor = Color(0xFF00CED1)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0C29),
                        Color(0xFF24243e),
                        Color(0xFF302B63)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "🌟 AR Universe 🌟",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Elige tu personaje favorito y dale vida en realidad aumentada",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            items(models) { model ->
                ModelCard(
                    model = model,
                    onClick = {
                        navController.navigate(DisplayScreen(selectedModel = model.fileName))
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📱 Instrucciones",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "1. Mueve tu dispositivo para detectar superficies\n" +
                                    "2. Toca un plano para colocar tu modelo\n" +
                                    "3. Pellizca para redimensionar\n" +
                                    "4. Arrastra para mover el objeto",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModelCard(
    model: ARModel,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val cardColor by animateColorAsState(
        targetValue = if (isPressed) model.primaryColor.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.15f),
        label = "cardColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) {
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(
            2.dp,
            Brush.horizontalGradient(
                colors = listOf(model.primaryColor, model.secondaryColor)
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji circle
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(35.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                model.primaryColor.copy(alpha = 0.3f),
                                model.secondaryColor.copy(alpha = 0.3f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = model.emoji,
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = model.displayName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = model.description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Arrow
            Text(
                text = "▶️",
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }

    // Handle press state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }

    // Update press state on click
    DisposableEffect(Unit) {
        onDispose { }
    }
}