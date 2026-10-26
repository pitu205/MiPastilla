# Mi Pastilla 1.3

Aplicación Android para registrar tomas de medicación.

- Inicia sin tratamientos programados.
- Permite programar un rango de días y dosis de 1/4, 1/2, 3/4 o 1 pastilla.
- Permite editar una fecha concreta, confirmar la toma diaria y eliminar un tratamiento sin borrar el historial.
- Notificaciones desde las 22:00 que intentan repetirse cada cinco minutos hasta la confirmación. Al abrir la notificación, la app indica dosis y fecha.
- Almacena los datos solo en el móvil.

Para compilar: GitHub Actions > Crear APK Android > artefacto MiPastilla-APK.

**Avisos:** Android puede limitar, retrasar o silenciar los avisos, especialmente si se restringe la actividad en segundo plano. Es un recordatorio auxiliar y no una garantía de alerta médica. No cambies las dosis sin indicación de un profesional.

- Nueva pantalla **Mis tomas**: calendario mensual con gris para confirmadas, verde para futuras programadas y naranja para vencidas sin confirmar. Permite ver el detalle tocando un día.

- Aviso verde grande al abrir la notificación: fecha y dosis, con botón grande "YA ME LA HE TOMADO". Desaparece tras confirmar la toma.
- Diseño con textos y botones más grandes, pensado para personas mayores.
