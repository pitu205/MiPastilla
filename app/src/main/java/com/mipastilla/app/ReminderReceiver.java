package com.mipastilla.app;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReminderReceiver extends BroadcastReceiver {
    static final String CHANNEL="toma_persistente_v1";
    static final int NOTIFICATION_ID=2200;
    @Override public void onReceive(Context c, Intent i) {
        Store s=new Store(c);
        LocalDate day=LocalDate.now();
        if ("DAILY".equals(i.getAction())) Scheduler.nextDaily(c);
        if (!s.scheduled(day) || s.taken(day) || LocalDateTime.now().isBefore(day.atTime(s.hour(),s.minute()))) {
            Scheduler.cancel(c, Scheduler.REPEAT);
            return;
        }
        notifyUser(c,s.doseFor(day),day);
        Scheduler.schedule(c,Scheduler.REPEAT,System.currentTimeMillis()+5*60_000L);
    }
    static void notifyUser(Context c,int dose,LocalDate day) {
        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26) {
            NotificationChannel ch=new NotificationChannel(CHANNEL,"Avisos de medicación",NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Avisos sonoros de medicación sin confirmar");
            ch.enableVibration(true);
            nm.createNotificationChannel(ch);
        }
        if(Build.VERSION.SDK_INT>=33 && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) return;
        Intent open=new Intent(c,MainActivity.class);
        open.putExtra("alarm_day",day.toString());
        open.putExtra("alarm_dose",dose);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi=PendingIntent.getActivity(c,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification n=new Notification.Builder(c,CHANNEL)
            .setSmallIcon(com.mipastilla.app.R.drawable.ic_pill)
            .setContentTitle("Alarma para su toma: "+Store.dosage(dose)+" pastilla")
            .setContentText("Toma del "+day+". Abre para confirmar.")
            .setContentIntent(pi).setAutoCancel(false).setOngoing(true)
            .setCategory(Notification.CATEGORY_REMINDER).build();
        nm.cancel(NOTIFICATION_ID);
        nm.notify(NOTIFICATION_ID,n);
    }
    static void clear(Context c) {
        ((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
        Scheduler.cancel(c,Scheduler.REPEAT);
    }
}
