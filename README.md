# EndureMetrics 📊🏃‍♂️

**EndureMetrics** es una plataforma avanzada de análisis y monitorización de rendimiento deportivo diseñada para atletas que buscan un control total sobre sus métricas. Construida sobre un stack tecnológico robusto, la aplicación permite procesar datos biométricos y de actividad con una interfaz de usuario de alta fidelidad.

## 🚀 Características Principales

- **Dashboard Inteligente**: Visualización de métricas críticas (VO2 Max, carga de entrenamiento, fatiga) mediante widgets dinámicos.
- **Gestión de Equipamiento**: Seguimiento detallado del desgaste de material (zapatillas, componentes de bicicleta).
- **Procesamiento de Datos FIT/GPX**: Integración nativa con el SDK de Garmin para el análisis de archivos de actividad.
- **Interfaz Premium**: UX moderna basada en el sistema de diseño **AtlantaFX**.

## 🛠️ Stack Tecnológico

- **Lenguaje**: Java 17
- **Framework Base**: Spring Boot 3.2.2
- **Interfaz Gráfica**: JavaFX 21 + AtlantaFX
- **Persistencia**: Spring Data JPA + PostgreSQL
- **Seguridad**: Encriptación de credenciales con jBCrypt
- **Métricas & Gráficos**: TilesFX
- **Análisis de Datos**: Jenetics (JPX) & Garmin FIT SDK

## 🏗️ Arquitectura

El proyecto sigue una arquitectura limpia orientada a la inyección de dependencias de Spring, facilitando la escalabilidad y el mantenimiento:

- `controller/`: Lógica de control de la UI y gestión de eventos.
- `service/`: Lógica de negocio y procesamiento de métricas.
- `model/`: Entidades JPA y modelos de datos.
- `view/`: Archivos FXML y recursos de diseño.

## 🔧 Instalación y Ejecución

1. **Requisitos**: JDK 17 y PostgreSQL instalado.
2. **Base de Datos**: Crear una base de datos llamada `enduremetrics`.
3. **Configuración**: Ajustar las credenciales en `src/main/resources/application.properties`.
4. **Compilar y Ejecutar**:
   ```bash
   mvn clean javafx:run
