# 🌟 AR Universe - Aplicación de Realidad Aumentada

Una aplicación Android moderna de Realidad Aumentada que permite a los usuarios colocar y manipular modelos 3D en el mundo real utilizando ARCore y Jetpack Compose.

## 📱 Características

- **🎮 Selección de Modelos**: Elige entre múltiples personajes 3D (Iron Man, Waifu, Sus)
- **🌍 Realidad Aumentada**: Coloca modelos 3D en superficies del mundo real
- **✋ Gestos Intuitivos**: Toca para colocar, pellizca para escalar, arrastra para mover
- **🎵 Audio Interactivo**: Reproduce sonidos al colocar modelos
- **🎨 Interfaz Moderna**: Diseño atractivo con gradientes y animaciones
- **📱 Gestión de Permisos**: Solicitud inteligente de permisos de cámara
- **🔧 Detección de Compatibilidad**: Verifica automáticamente el soporte de ARCore

## 🚀 Tecnologías Utilizadas

- **Kotlin** - Lenguaje principal
- **Jetpack Compose** - UI moderna y declarativa
- **ARCore** - Motor de realidad aumentada de Google
- **SceneView** - Renderizado 3D y gestión de escenas AR
- **Navigation Compose** - Navegación entre pantallas
- **Material 3** - Sistema de diseño de Google

## 📋 Requisitos

### Requisitos del Sistema
- **Android 7.0 (API level 24)** o superior
- **Dispositivo compatible con ARCore** ([Lista oficial](https://developers.google.com/ar/devices))
- **Google Play Services for AR** instalado
- **Permisos de cámara** habilitados

### Requisitos de Desarrollo
- **Android Studio** Arctic Fox o superior
- **Kotlin 1.9.0**
- **Gradle 8.5.2**
- **compileSdk 34**

## 🛠️ Instalación y Configuración

### 1. Clonar el Repositorio
```bash
git clone https://github.com/tuusuario/ar-universe.git
cd ar-universe
```

### 2. Abrir en Android Studio
1. Abre Android Studio
2. Selecciona "Open an existing project"
3. Navega a la carpeta del proyecto clonado
4. Espera a que Gradle sincronice las dependencias

### 3. Configurar Modelos 3D
Coloca tus archivos `.glb` en la carpeta:
```
app/src/main/assets/models/
├── iron_man.glb
├── waifu.glb
└── sus.glb
```

### 4. Configurar Audio (Opcional)
Coloca tu archivo de audio en:
```
app/src/main/assets/cancion.mp3
```

### 5. Compilar y Ejecutar
1. Conecta un dispositivo físico (AR no funciona en emuladores)
2. Asegúrate de que ARCore esté instalado en el dispositivo
3. Ejecuta la aplicación desde Android Studio

## 📁 Estructura del Proyecto

```
app/src/main/java/com/codewithfk/arlearner/
├── ui/
│   ├── navigation/
│   │   └── NavRoutes.kt          # Definición de rutas
│   ├── screens/
│   │   ├── HomeScreen.kt         # Pantalla principal con selección
│   │   └── DisplayScreen.kt      # Pantalla de AR
│   └── theme/
│       ├── Color.kt              # Colores del tema
│       ├── Theme.kt              # Configuración del tema
│       └── Type.kt               # Tipografía
├── util/
│   └── Utils.kt                  # Utilidades para crear nodos AR
├── MainActivity.kt               # Actividad principal
└── AndroidManifest.xml           # Configuración de permisos

app/src/main/assets/
├── models/                       # Modelos 3D (.glb)
└── cancion.mp3                   # Audio opcional
```

## 🎯 Cómo Usar la Aplicación

### 1. Pantalla Principal
- Selecciona uno de los modelos 3D disponibles
- Cada modelo tiene su propio tema de color y descripción

### 2. Experiencia AR
1. **Otorgar Permisos**: Acepta el permiso de cámara cuando se solicite
2. **Detectar Superficies**: Mueve lentamente el dispositivo para que ARCore detecte planos
3. **Colocar Modelo**: Toca en una superficie detectada para colocar el modelo 3D
4. **Manipular Objeto**:
   - **Pellizcar**: Escalar el modelo
   - **Arrastrar**: Mover el modelo
   - **Botón Reposicionar**: Mover al centro de la pantalla
   - **Botón Quitar**: Eliminar el modelo actual

### 3. Indicadores Visuales
- **Texto amarillo**: Problemas de tracking (poca luz, mucho movimiento, etc.)
- **Texto blanco**: Instrucciones normales
- **Texto rojo**: Errores de tracking

## 🔧 Personalización

### Agregar Nuevos Modelos
1. Coloca el archivo `.glb` en `app/src/main/assets/models/`
2. Modifica `HomeScreen.kt` para agregar el nuevo modelo:

```kotlin
val models = listOf(
    // ... modelos existentes
    ARModel(
        fileName = "tu_modelo.glb",
        displayName = "Tu Modelo",
        description = "Descripción del modelo",
        emoji = "🎭",
        primaryColor = Color(0xFF123456),
        secondaryColor = Color(0xFF654321)
    )
)
```

### Cambiar Colores del Tema
Modifica `app/src/main/java/com/codewithfk/arlearner/ui/theme/Color.kt`:

```kotlin
val Purple80 = Color(0xFFTuColor)
val PurpleGrey80 = Color(0xFFTuColor)
// ...
```

### Ajustar Sensibilidad de Escala
En `Utils.kt`, modifica el parámetro `scaleToUnits`:

```kotlin
val modelNode = ModelNode(
    modelInstance = instance,
    scaleToUnits = 0.1f // Más pequeño = menos sensible
)
```

## 🐛 Solución de Problemas

### Pantalla Negra en AR
- Verifica que ARCore esté instalado y actualizado
- Asegúrate de que el dispositivo sea compatible
- Otorga permisos de cámara

### Los Modelos No Aparecen
- Verifica que los archivos `.glb` estén en la carpeta correcta
- Comprueba que los nombres de archivo coincidan exactamente
- Revisa los logs en Android Studio para errores específicos

### Problemas de Tracking
- Usa la aplicación en un área bien iluminada
- Mueve el dispositivo lentamente
- Apunta a superficies con textura (evita superficies lisas o reflectantes)

### Error de Compilación
- Asegúrate de usar la versión correcta de Gradle y Kotlin
- Limpia y reconstruye el proyecto: `Build > Clean Project`
- Invalida caché: `File > Invalidate Caches and Restart`

## 📚 Dependencias Principales

```kotlin
// ARCore y SceneView
implementation("io.github.sceneview:arsceneview:2.2.1")

// Jetpack Compose
implementation("androidx.compose.ui:ui:$compose_version")
implementation("androidx.compose.material3:material3:$material3_version")

// Navegación
implementation("androidx.navigation:navigation-compose:$navigation_version")

// Serialización
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
```

## 📄 Licencia

Este proyecto está licenciado bajo la Licencia MIT - ve el archivo [LICENSE](LICENSE) para más detalles.

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Para contribuir:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 👨‍💻 Autor

**Giovanni** - [@GioSENPAII](https://github.com/GioSENPAII)

## 🙏 Agradecimientos

- [Google ARCore](https://developers.google.com/ar) por la tecnología de AR
- [SceneView](https://github.com/SceneView/sceneview-android) por la librería de renderizado 3D
- [Jetpack Compose](https://developer.android.com/jetpack/compose) por el framework de UI
- Comunidad de desarrolladores Android por el apoyo y recursos

## 📈 Versiones

### v1.0.0 (Actual)
- ✅ Selección de múltiples modelos 3D
- ✅ Interfaz moderna con Jetpack Compose
- ✅ Gestión completa de permisos y compatibilidad
- ✅ Controles de gestos para manipular objetos
- ✅ Audio interactivo
- ✅ Manejo robusto de errores

### Próximas Características
- 🔜 Múltiples objetos simultáneos
- 🔜 Persistencia de objetos entre sesiones
- 🔜 Captura de screenshots de la escena AR
- 🔜 Efectos de partículas y animaciones
- 🔜 Modo multijugador local

---

<div align="center">

**¿Te gustó el proyecto? ⭐ Dale una estrella en GitHub!**


</div>
