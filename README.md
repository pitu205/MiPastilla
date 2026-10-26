# Mi Pastilla – Android

App sencilla y local para **registrar** la toma de medicación (no recomienda dosis).

## Generar APK desde GitHub (sin Android Studio)
1. Crea un repositorio **privado** en GitHub.
2. Sube el contenido de este ZIP a la **raíz** del repositorio (incluida la carpeta oculta `.github`).
3. Abre **Actions → Crear APK Android → Run workflow** (o espera a que se ejecute al subir a `main`).
4. Cuando el flujo termine en verde, abre la ejecución y descarga **MiPastilla-APK** en **Artifacts**.
5. Extrae el ZIP descargado, instala `app-debug.apk` en tu Android y concede notificaciones y, si se solicita, **Alarmas y recordatorios**.

## Funciones
- Rango de fechas inclusivo configurable; por defecto del día actual al 22/11/2026.
- Dosis por rango: ¼, ½, ¾ o 1 pastilla; cambio de dosis en días concretos.
- Aviso a las 22:00 con repetición programada cada cinco minutos hasta registrar la toma.
- Confirmación explícita de cantidad realmente tomada, con control anti-duplicación diario.
- Historial local de las últimas 30 fechas.

## Limitaciones importantes
Android puede retrasar las alarmas por ahorro de batería, permisos, modo No molestar o restricciones del fabricante. Los avisos **no están garantizados** cada cinco minutos. Mantén habilitadas las notificaciones y las alarmas exactas, y prueba el dispositivo antes de depender de la aplicación. Los registros solo quedan en este teléfono y se pierden al desinstalar la aplicación.
No confirmes una toma sin haberla realizado; en caso de duda de dosis u omisión, consulta al profesional sanitario. La aplicación no indica dosis ni autoriza tomar otra pastilla.

El APK de depuración está firmado automáticamente para instalación directa; no se distribuye en Play Store.
