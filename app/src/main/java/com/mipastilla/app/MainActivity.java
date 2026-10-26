package com.mipastilla.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.widget.GridLayout;
import java.time.YearMonth;
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
    int dose, chosenHour=22, chosenMinute=0;
    YearMonth shownMonth = YearMonth.now();
    final String[] choices={"¼ pastilla","½ pastilla","¾ pastilla","1 pastilla"};
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        store=new Store(this);
        first=store.enabled()?LocalDate.parse(store.start()):LocalDate.now();
        last=store.enabled()?LocalDate.parse(store.end()):LocalDate.now();dose=store.dose();chosenHour=store.hour();chosenMinute=store.minute();
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},77);
        render();
    }
    int dp(int v){ return (int)(getResources().getDisplayMetrics().density*v); }
    TextView text(String value,int size){ TextView t=new TextView(this); t.setText(value); t.setTextSize(size);t.setTextColor(Color.rgb(31,41,55));t.setPadding(0,dp(7),0,dp(7));return t; }
    void addButton(String title,Runnable callback){ Button b=new Button(this); b.setText(title);b.setAllCaps(false);b.setTextSize(19);b.setMinHeight(dp(62));root.addView(b);b.setOnClickListener(v->callback.run()); }
    String timeLabel(){return String.format(java.util.Locale.getDefault(),"%02d:%02d",chosenHour,chosenMinute);}
    String dateLabel(LocalDate d){return d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));}
    void addLargeButton(LinearLayout parent,String label,Runnable action,boolean primary){
        Button button=new Button(this); button.setText(label);button.setAllCaps(false);
        button.setTextSize(23);button.setTypeface(null,Typeface.BOLD);button.setMinHeight(dp(78));
        if(primary){button.setTextColor(Color.WHITE);button.setBackgroundTintList(ColorStateList.valueOf(color(25,115,61)));}
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(78));lp.setMargins(0,dp(12),0,dp(4));
        parent.addView(button,lp);button.setOnClickListener(v->action.run());
    }
    void render(){
        ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);
        root.setPadding(dp(20),dp(26),dp(20),dp(30));scroll.addView(root);setContentView(scroll);
        TextView heading=text("Mi Pastilla",36);heading.setTypeface(null,Typeface.BOLD);heading.setGravity(Gravity.CENTER);
        heading.setTextColor(color(24,95,57));root.addView(heading);
        showAlarmBanner();
        if(store.enabled()){
            TextView label=text("MIS ALERTAS",25);label.setTypeface(null,Typeface.BOLD);root.addView(label);
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(18),dp(16),dp(18),dp(16));card.setBackground(background(color(226,245,231)));
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(12),0,dp(12));root.addView(card,cp);
            TextView daily=text(Store.dosage(store.dose())+" pastilla · "+String.format(java.util.Locale.getDefault(),"%02d:%02d",store.hour(),store.minute()),27);
            daily.setTypeface(null,Typeface.BOLD);card.addView(daily);
            card.addView(text("Del "+dateLabel(LocalDate.parse(store.start()))+" al "+dateLabel(LocalDate.parse(store.end())),21));
            LocalDate today=LocalDate.now();
            if(store.scheduled(today))card.addView(text(store.taken(today)?"HOY: TOMA CONFIRMADA":"HOY: PENDIENTE",21));
            if(store.scheduled(today)&&!store.taken(today))addLargeButton(root,"YA ME LA HE TOMADO",()->confirm(),true);
            addLargeButton(root,"MIS TOMAS · CALENDARIO",()->showCalendar(),false);
            addLargeButton(root,"EDITAR ALERTA",()->showCreateAlert(),false);
            addLargeButton(root,"ELIMINAR ALERTA",()->deleteTreatment(),false);
        } else {
            TextView empty=text("No tienes alertas creadas",23);empty.setGravity(Gravity.CENTER);
            root.addView(empty);
        }
        addLargeButton(root,"+ CREAR NUEVA ALERTA",()->{
            first=LocalDate.now();last=first;dose=2;chosenHour=22;chosenMinute=0;showCreateAlert();
        },true);
        if(!store.enabled())addLargeButton(root,"MIS TOMAS · CALENDARIO",()->showCalendar(),false);
    }
    void showCreateAlert(){
        ScrollView sc=new ScrollView(this);LinearLayout page=new LinearLayout(this);page.setOrientation(1);
        page.setPadding(dp(20),dp(20),dp(20),dp(30));sc.addView(page);setContentView(sc);
        addLargeButton(page,"← VOLVER",()->render(),false);
        TextView heading=text("Crear nueva alerta",31);heading.setTypeface(null,Typeface.BOLD);page.addView(heading);
        TextView date1=text("Inicio: "+dateLabel(first),23);page.addView(date1);
        addLargeButton(page,"ELEGIR FECHA DE INICIO",()->pickDateForForm(true),false);
        TextView date2=text("Fin: "+dateLabel(last),23);page.addView(date2);
        addLargeButton(page,"ELEGIR FECHA DE FIN",()->pickDateForForm(false),false);
        TextView doseLabel=text("Cantidad: "+choices[dose-1],24);page.addView(doseLabel);
        addLargeButton(page,"ELEGIR CANTIDAD",()->new AlertDialog.Builder(this).setTitle("Cantidad programada")
            .setSingleChoiceItems(choices,dose-1,(dlg,which)->{dose=which+1;dlg.dismiss();showCreateAlert();})
            .setNegativeButton("Cancelar",null).show(),false);
        TextView hourLabel=text("Hora: "+timeLabel(),26);hourLabel.setTypeface(null,Typeface.BOLD);page.addView(hourLabel);
        addLargeButton(page,"ELEGIR HORA",()->new TimePickerDialog(this,(picker,h,m)->{
            chosenHour=h;chosenMinute=m;showCreateAlert();
        },chosenHour,chosenMinute,true).show(),false);
        addLargeButton(page,"ACEPTAR Y GUARDAR",()->save(),true);
    }
    void pickDateForForm(boolean begin){
        LocalDate d=begin?first:last;
        new DatePickerDialog(this,(picker,y,m,day)->{
            LocalDate selected=LocalDate.of(y,m+1,day);
            if(begin){first=selected;if(last.isBefore(first))last=first;}else last=selected;
            showCreateAlert();
        },d.getYear(),d.getMonthValue()-1,d.getDayOfMonth()).show();
    }
    void showAlarmBanner(){
        Intent intent=getIntent();
        if(intent==null || !intent.hasExtra("alarm_day")) return;
        LocalDate alarmDate;
        try { alarmDate=LocalDate.parse(intent.getStringExtra("alarm_day")); }
        catch(Exception e){ intent.removeExtra("alarm_day"); return; }
        if(!store.scheduled(alarmDate) || store.taken(alarmDate)){
            intent.removeExtra("alarm_day"); return;
        }
        int alarmDose=store.doseFor(alarmDate);
        LinearLayout banner=new LinearLayout(this);
        banner.setOrientation(LinearLayout.VERTICAL);
        banner.setPadding(dp(18),dp(18),dp(18),dp(20));
        banner.setBackground(background(color(29,125,65)));
        LinearLayout.LayoutParams margin=new LinearLayout.LayoutParams(-1,-2);
        margin.setMargins(0,dp(14),0,dp(18));
        root.addView(banner,margin);
        TextView title=text("ALARMA DE SU TOMA",27);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null,Typeface.BOLD);
        title.setTextColor(Color.WHITE);
        banner.addView(title);
        TextView message=text("VA A TOMAR\n"+Store.dosage(alarmDose)+" PASTILLA\nDEL DÍA "+alarmDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),27);
        message.setTextColor(Color.WHITE);
        message.setTypeface(null,Typeface.BOLD);
        message.setGravity(Gravity.CENTER);
        message.setLineSpacing(dp(5),1f);
        banner.addView(message);
        Button confirm=new Button(this);
        confirm.setAllCaps(false);
        confirm.setText("YA ME LA HE TOMADO");
        confirm.setTextSize(22);
        confirm.setTypeface(null,Typeface.BOLD);
        confirm.setTextColor(color(13,90,48));
        confirm.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(86));
        bp.setMargins(0,dp(12),0,0);
        banner.addView(confirm,bp);
        confirm.setOnClickListener(v->confirmForDay(alarmDate));
    }
    @Override protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        setIntent(intent);
        render();
    }
    int color(int r,int g,int b){return Color.rgb(r,g,b);}
    GradientDrawable background(int fill){
        GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(10));return d;
    }
    void showCalendar(){
        ScrollView scroll=new ScrollView(this);
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(16),dp(18),dp(16),dp(24));scroll.addView(page);setContentView(scroll);
        Button back=new Button(this);back.setText("← Volver");back.setAllCaps(false);
        back.setOnClickListener(v->render());page.addView(back);
        TextView title=text("Mis tomas",30);title.setTypeface(null,Typeface.BOLD);page.addView(title);
        page.addView(text("GRIS: YA TOMADA\nVERDE: PRÓXIMAS TOMAS\nNARANJA: SIN CONFIRMAR",18));
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);page.addView(nav);
        Button prev=new Button(this);prev.setText("‹");nav.addView(prev,new LinearLayout.LayoutParams(0,dp(50),1));
        TextView month=text(shownMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy",new java.util.Locale("es","ES"))),19);
        month.setGravity(Gravity.CENTER);month.setTypeface(null,Typeface.BOLD);
        nav.addView(month,new LinearLayout.LayoutParams(0,dp(50),4));
        Button next=new Button(this);next.setText("›");nav.addView(next,new LinearLayout.LayoutParams(0,dp(50),1));
        prev.setOnClickListener(v->{shownMonth=shownMonth.minusMonths(1);showCalendar();});
        next.setOnClickListener(v->{shownMonth=shownMonth.plusMonths(1);showCalendar();});
        GridLayout grid=new GridLayout(this);grid.setColumnCount(7);page.addView(grid);
        int width=getResources().getDisplayMetrics().widthPixels-dp(32);
        int cell=width/7;
        for(String label:new String[]{"L","M","X","J","V","S","D"}){
            TextView day=text(label,13);day.setTypeface(null,Typeface.BOLD);day.setGravity(Gravity.CENTER);
            grid.addView(day,new android.view.ViewGroup.LayoutParams(cell,dp(38)));
        }
        int offset=shownMonth.atDay(1).getDayOfWeek().getValue()-1;
        for(int i=0;i<offset;i++) grid.addView(new TextView(this),new android.view.ViewGroup.LayoutParams(cell,dp(53)));
        LocalDate today=LocalDate.now();
        for(int n=1;n<=shownMonth.lengthOfMonth();n++){
            LocalDate day=shownMonth.atDay(n);
            boolean taken=store.taken(day), scheduled=store.scheduled(day);
            TextView tile=text(Integer.toString(n)+(taken?"\n✓":scheduled?"\n"+Store.dosage(store.doseFor(day)):""),17);
            tile.setGravity(Gravity.CENTER);tile.setPadding(dp(2),dp(2),dp(2),dp(2));
            if(taken) tile.setBackground(background(color(202,207,214)));
            else if(scheduled && !day.isBefore(today)) tile.setBackground(background(color(175,228,186)));
            else if(scheduled) tile.setBackground(background(color(250,210,154)));
            else tile.setBackground(background(color(242,244,247)));
            if(day.equals(today)) tile.setTypeface(null,Typeface.BOLD);
            GridLayout.LayoutParams params=new GridLayout.LayoutParams();params.width=cell-dp(2);params.height=dp(51);
            params.setMargins(dp(1),dp(1),dp(1),dp(1));grid.addView(tile,params);
            tile.setOnClickListener(v->dayDetails(day));
        }
        page.addView(text("Pulsa sobre cualquier día para ver el detalle de la toma.",14));
        if(!store.enabled())page.addView(text("No hay un tratamiento activo. Las tomas anteriores confirmadas siguen visibles en gris.",17));
    }
    void dayDetails(LocalDate d){
        String message;
        if(store.taken(d)) message="Toma confirmada\n"+entry(d);
        else if(store.scheduled(d)) message= "Dosis programada: "+Store.dosage(store.doseFor(d))+" pastilla\n"+
                (d.isBefore(LocalDate.now())?"Vencida, sin confirmación": "Pendiente de confirmar");
        else message="No hay una toma programada ni confirmada para este día.";
        new AlertDialog.Builder(this).setTitle("Toma del "+d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            .setMessage(message).setPositiveButton("Cerrar",null).show();
    }
    @Override public void onBackPressed(){render();}
    void refreshText(){
        range.setText((store.enabled()?"Tratamiento guardado: "+store.start()+" al "+store.end()+" (ambos incluidos)\nDosis diaria: "+choices[store.dose()-1]:"Todavía no hay tratamiento guardado.")+"\nRango que estás editando: "+first+" al "+last+" · "+choices[dose-1]);
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
        store.save(first.toString(),last.toString(),chosenHour,chosenMinute,dose);Scheduler.refresh(this);render();
        Toast.makeText(this,"Alerta guardada",Toast.LENGTH_SHORT).show();
        permissionHint();
    }
    void deleteTreatment(){
        new AlertDialog.Builder(this).setTitle("Eliminar tratamiento")
            .setMessage("¿Eliminar la programación y detener sus alarmas? Se conservará el historial de tomas confirmadas.")
            .setNegativeButton("Cancelar",null)
            .setPositiveButton("Eliminar",(dialog,which)->{
                store.deleteTreatment();ReminderReceiver.clear(this);Scheduler.refresh(this);
                first=LocalDate.now();last=first;dose=2;
                if(getIntent()!=null)getIntent().removeExtra("alarm_day");render();
            }).show();
    }
    void confirm(){ confirmForDay(LocalDate.now()); }
    void confirmForDay(LocalDate day){
        if(!store.scheduled(day)){Toast.makeText(this,"No hay tratamiento programado para esa fecha",Toast.LENGTH_LONG).show();return;}
        if(store.taken(day)){new AlertDialog.Builder(this).setTitle("Toma ya confirmada").setMessage("Ya has registrado la toma de este día. No se registrará otra toma.").setPositiveButton("Aceptar",null).show();return;}
        final int[] chosen={store.doseFor(day)};
        new AlertDialog.Builder(this).setTitle("¿Qué cantidad has tomado el "+day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))+"?")
            .setSingleChoiceItems(choices,chosen[0]-1,(dlg,which)->chosen[0]=which+1)
            .setNegativeButton("Cancelar",null)
            .setPositiveButton("Confirmar",(dlg,which)->{
                new AlertDialog.Builder(this).setTitle("Confirmar toma")
                    .setMessage("¿Confirmas que YA has tomado "+choices[chosen[0]-1]+"? Esto solo registra la toma; no indica qué dosis debes tomar.")
                    .setNegativeButton("Volver",null)
                    .setPositiveButton("Sí, la he tomado",(a,b)->{store.record(day,chosen[0]);ReminderReceiver.clear(this);Scheduler.refresh(this);if(getIntent()!=null)getIntent().removeExtra("alarm_day");render();}).show();
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
    @Override protected void onResume(){super.onResume();if(store!=null) Scheduler.refresh(this);}
}
