package com.mipastilla.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

final class Scheduler {
    static final int DAILY = 100, REPEAT = 101;
    static PendingIntent intent(Context c, int code) {
        Intent i = new Intent(c, ReminderReceiver.class).setAction(code==DAILY?"DAILY":"REPEAT");
        return PendingIntent.getBroadcast(c, code, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    static void cancel(Context c, int code) {
        AlarmManager am = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        am.cancel(intent(c,code));
    }
    static void schedule(Context c, int code, long atMillis) {
        AlarmManager am = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = intent(c,code);
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi);
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi);
        }
    }
    static long millis(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    static void refresh(Context c) {
        cancel(c, DAILY); cancel(c, REPEAT);
        Store s = new Store(c);
        if (!s.enabled()) return;
        LocalDate d=LocalDate.now();
        LocalDate last=LocalDate.parse(s.end());
        LocalDate first=LocalDate.parse(s.start());
        LocalDateTime now=LocalDateTime.now();
        LocalDateTime todayAt=d.atTime(s.hour(),s.minute());
        if (s.scheduled(d) && !s.taken(d) && !now.isBefore(todayAt)) {
            // Reanudar el aviso pendiente después de reiniciar o reprogramar.
            schedule(c, REPEAT, System.currentTimeMillis()+60_000);
        }
        LocalDate next=d;
        if (!now.isBefore(todayAt)) next=next.plusDays(1);
        if (next.isBefore(first)) next=first;
        if (!next.isAfter(last)) schedule(c, DAILY, millis(next.atTime(s.hour(),s.minute())));
    }
    static void nextDaily(Context c) {
        Store s=new Store(c);
        LocalDate tomorrow=LocalDate.now().plusDays(1);
        LocalDate first=LocalDate.parse(s.start());
        if(tomorrow.isBefore(first)) tomorrow=first;
        if (s.enabled() && !tomorrow.isAfter(LocalDate.parse(s.end()))) {
            schedule(c, DAILY, millis(tomorrow.atTime(s.hour(),s.minute())));
        }
    }
}
