package com.mipastilla.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.net.Uri;
import android.view.View;
import android.widget.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends Activity {
    Store store;
    LinearLayout root;
    TextView range, status, history;
    LocalDate first, last;
    int dose;
    final String[] choices={"¼ pastilla","½ pastilla","¾ pastilla","1 pastilla"};
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        store=new Store(this);
        first=LocalDate.parse(store.start());last=LocalDate.parse(store.end());dose=store.dose();
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},77);
        render();
    }
    int dp(int v){ return (int)(getResources().getDisplayMetrics().density*v); }
    TextView text(String value,int size){ TextView t=new TextView(this); t.setText(value); t.setTextSize(size);t.setTextColor(Color.rgb(31,41,55));t.setPadding(0,dp(7),0,dp(7));return t; }
    void addButton(String title,Runnable callback){ Button b=new Button(this); b.setText(title);b.setAllCaps(false);root.addView(b);b.setOnClickListener(v->callback.run()); }
    void render(){
        ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(22),dp(24),dp(22),dp(30));
        scroll.addView(root);setContentView(scroll);
        TextView heading=text("Mi Pastilla",28);heading.setTextColor(Color.rgb(38,91,133));root.addView(heading);
        root.addView(text("Recordatorio diario · 22:00 · repetición cada 5 minutos hasta confirmar",14));
        status=text("",18);root.addView(status);
        addButton("Confirmar la toma de hoy",()->confirm());
        addButton("Editar dosis de un día",()->editDay());
        root.addView(text("PROGRAMAR TRATAMIENTO",20));
        range=text("",16);root.addView(range);
        addButton("Elegir fecha de inicio",()->pickDate(first,true));
        addButton("Elegir fecha de fin",()->pickDate(last,false));
        addButton("Dosis diaria predeterminada: "+choices[dose-1],()->chooseDose());
        addButton("Guardar rango completo",()->save());
        addButton("Permisos de alarma del teléfono",()->openAlarmSettings());
        root.addView(text("HISTORIAL (últimos 30 días)",20));
        history=text("",15);root.addView(history);
        refreshText();
    }
    void refreshText(){
        range.setText("Del "+first+" al "+last+" (ambos incluidos)\nDosis prevista: "+choices[dose-1]);
        LocalDate today=LocalDate.now();
        if(!store.scheduled(today))status.setText("Hoy no hay una toma programada.");
        else if(store.taken(today))status.setText("✓ Toma de hoy registrada: "+entry(today));
        else status.setText("PENDIENTE: "+choices[store.doseFor(today)-1]);
        StringBuilder h=new StringBuilder();
        for(int i=0;i<30;i++){
            LocalDate d=today.minusDays(i);
            if(store.scheduled(d)||store.taken(d)){
                String state=store.taken(d)?"CONFIRMADA · "+entry(d):d.equals(today)?"PENDIENTE": "SIN CONFIRMACIÓN";
                h.append(d).append("  ·  ").append(state).append("\n");
            }
        }
        history.setText(h.length()==0?"Todavía no hay registros.":h.toString());
    }
    String entry(LocalDate d){
        try{org.json.JSONObject j=new org.json.JSONObject(store.recorded(d)); return Store.dosage(j.getInt("dose"))+" pastilla · "+j.getString("time").replace('T',' ');}
        catch(Exception e){return "registrada";}
    }
    void pickDate(LocalDate d,boolean begin){
        DatePickerDialog dlg=new DatePickerDialog(this,(p,y,m,day)->{
            LocalDate chosen=LocalDate.of(y,m+1,day);
            if(begin) first=chosen;else last=chosen;
            refreshText();
        },d.getYear(),d.getMonthValue()-1,d.getDayOfMonth());dlg.show();
    }
    void chooseDose(){new AlertDialog.Builder(this).setTitle("Dosis del rango").setSingleChoiceItems(choices,dose-1,(dialog,index)->{dose=index+1;dialog.dismiss();render();}).setNegativeButton("Cancelar",null).show();}
    void save(){
        if(last.isBefore(first)){new AlertDialog.Builder(this).setMessage("La fecha final no puede ser anterior a la inicial.").setPositiveButton("Aceptar",null).show();return;}
        store.save(first.toString(),last.toString(),22,0,dose);Scheduler.refresh(this);render();
        Toast.makeText(this,"Tratamiento programado",Toast.LENGTH_SHORT).show();
        permissionHint();
    }
    void confirm(){
        LocalDate day=LocalDate.now();
        if(!store.scheduled(day)){Toast.makeText(this,"Hoy no hay tratamiento programado",Toast.LENGTH_LONG).show();return;}
        if(store.taken(day)){new AlertDialog.Builder(this).setTitle("Toma ya confirmada").setMessage("Ya has registrado la pastilla de hoy. No se registrará otra toma.").setPositiveButton("Aceptar",null).show();return;}
        final int[] chosen={store.doseFor(day)};
        new AlertDialog.Builder(this).setTitle("¿Qué cantidad has tomado?")
            .setSingleChoiceItems(choices,chosen[0]-1,(dlg,which)->chosen[0]=which+1)
            .setNegativeButton("Cancelar",null)
            .setPositiveButton("Confirmar",(dlg,which)->{
                new AlertDialog.Builder(this).setTitle("Confirmar toma")
                    .setMessage("¿Confirmas que YA has tomado "+choices[chosen[0]-1]+"? Esto solo registra la toma; no indica qué dosis debes tomar.")
                    .setNegativeButton("Volver",null)
                    .setPositiveButton("Sí, la he tomado",(a,b)->{store.record(day,chosen[0]);ReminderReceiver.clear(this);Scheduler.refresh(this);render();}).show();
            }).show();
    }
    void editDay(){
        LocalDate today=LocalDate.now();
        DatePickerDialog pick=new DatePickerDialog(this,(v,y,m,d)->{
            LocalDate date=LocalDate.of(y,m+1,d);
            if(!store.scheduled(date)){Toast.makeText(this,"Esta fecha está fuera del rango guardado",Toast.LENGTH_LONG).show();return;}
            int current=store.doseFor(date);
            new AlertDialog.Builder(this).setTitle("Dosis programada: "+date)
                .setSingleChoiceItems(choices,current-1,(dialog,which)->{
                    store.override(date,which+1);Scheduler.refresh(this);dialog.dismiss();render();
                    Toast.makeText(this,"Dosis modificada para "+date,Toast.LENGTH_SHORT).show();
                }).setNegativeButton("Cancelar",null).show();
        },today.getYear(),today.getMonthValue()-1,today.getDayOfMonth());pick.show();
    }
    void openAlarmSettings(){
        if(Build.VERSION.SDK_INT>=31){
            try{startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}catch(Exception e){Toast.makeText(this,"Comprueba los permisos de alarmas en Ajustes",Toast.LENGTH_LONG).show();}
        }else Toast.makeText(this,"No necesitas activar permisos especiales de alarmas",Toast.LENGTH_LONG).show();
    }
    void permissionHint(){
        if(Build.VERSION.SDK_INT>=31){android.app.AlarmManager manager=(android.app.AlarmManager)getSystemService(ALARM_SERVICE);
            if(!manager.canScheduleExactAlarms())new AlertDialog.Builder(this).setTitle("Permiso importante").setMessage("Activa 'Alarmas y recordatorios' para que el aviso sea lo más puntual posible.")
            .setNegativeButton("Luego",null).setPositiveButton("Abrir ajustes",(d,w)->openAlarmSettings()).show();}
    }
    @Override protected void onResume(){super.onResume();if(store!=null && status!=null) {Scheduler.refresh(this);refreshText();}}
}
