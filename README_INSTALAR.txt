DETECTOR CORREOS — ARCHIVOS PARA TU PROYECTO ANDROID STUDIO

Este paquete está pensado para sustituir el módulo "app" de un proyecto Android Studio vacío.

IMPORTANTE:
Tu proyecto de Android Studio debe tener soporte Kotlin y usar Gradle.

PASOS:
1. Cierra la app en ejecución si la tienes.
2. En Android Studio, en el panel izquierdo, cambia la vista a "Project".
3. Haz una copia de seguridad de tu carpeta app si quieres.
4. Sustituye/copía el contenido de esta carpeta "app" dentro de la carpeta "app" de tu proyecto.
5. Pulsa "Sync Project with Gradle Files".
6. Si Android Studio te pide instalar SDK 35, acepta.
7. Pulsa:
   Build > Build App Bundle(s) / APK(s) > Build APK(s)

APK:
Normalmente aparecerá en:
app\build\outputs\apk\debug\app-debug.apk

INSTALAR EN EL MÓVIL:
- Pasa app-debug.apk al Android.
- Ábrelo.
- Android puede pedir permiso para "Instalar aplicaciones desconocidas" desde el navegador/gestor de archivos.
- Activa ese permiso solo para esa fuente.
- Pulsa Instalar.

La APK v0.1:
- No accede a Gmail.
- Tiene Inicio y Casos.
- Filtra fuertes/relacionados.
- Permite marcar casos como revisados.
- Usa datos de prueba.