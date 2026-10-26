# Mi Pastilla v1.4

Interfaz sencilla: pantalla inicial con botón **Crear nueva alerta** y las alertas guardadas. En el formulario se eligen fecha inicial, final, cantidad (¼, ½, ¾, 1) y hora mediante selector 24 h. Al aceptar se guarda y vuelve al inicio.

La versión actual permite **una programación activa** que se puede editar/eliminar; no gestiona varias alertas simultáneas. Conserva el historial de confirmaciones. La aplicación no prescribe dosis.

## Compilación

Subir los archivos al repositorio y ejecutar GitHub Actions. Descargar el artefacto MiPastilla-APK.

## Avisos

Para el funcionamiento de notificaciones y alarmas en Android, conceder permisos necesarios y comprobar la gestión de batería. Las repeticiones cada 5 minutos pueden retrasarse por las restricciones de Android.
