# Frigory 🍎❄️

**Frigory** es una aplicación nativa para Android diseñada para optimizar la gestión de inventarios alimentarios domésticos y combatir el desperdicio de comida. 
Este proyecto ha sido desarrollado como Trabajo de Fin de Grado (TFG) para el Ciclo de Desarrollo de Aplicaciones Multiplataforma (DAM) en la **UAX**.

## 🚀 Características principales

*   **Gestión de Inventario Inteligente:** Control total de existencias en tiempo real.
*   **Alertas de Caducidad Visuales:** Sistema de código de colores (Gris, Amarillo, Rojo) basado en la proximidad de la fecha de vencimiento.
*   **Consulta Automatizada (API):** Integración con la API de Open Food Facts mediante Retrofit para autocompletar datos de productos.
*   **Filosofía Offline-First:** Persistencia de datos local con la librería Room, permitiendo el uso total sin conexión a internet.
*   **Lista de la Compra Inteligente:** Sincronización automática y sugerencias proactivas de carencias del inventario al agotar existencias.
*   **Autenticación Segura:** Gestión de usuarios mediante Firebase Authentication.
*   **Interfaz Ergonómica: Diseño optimizado para el uso con una sola mano y adaptabilidad a diferentes sistemas de navegación (gestos/botones).

## 🛠️ Stack Tecnológico

*   **Lenguaje:** [Kotlin](https://kotlinlang.org/)
*   **Arquitectura:** MVVM (Model-View-ViewModel)
*   **Base de Datos:** Room Persistence Library (SQLite)
*   **Red:** Retrofit + Gson
*   **Seguridad:** Firebase Auth
*   **Diseño:** Material Design 3 (Material You)

## 📦 Instalación y Despliegue

Para probar la aplicación en un entorno de desarrollo o instalarla en un terminal real:

1. **Requisitos:** Android 8.0 (API 26) o superior.
2. **Instalación:** Descarga el archivo `frigory_v1.0.apk` de la sección de entregas o clona este repositorio e instálalo mediante Android Studio.
3. **Clonar el repositorio:**
   ```bash
git clone https://github.com/IvanLalov/Frigory.git

