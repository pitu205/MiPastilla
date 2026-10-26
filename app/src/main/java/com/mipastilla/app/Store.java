package com.mipastilla.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.time.LocalDate;

final class Store {
    private final SharedPreferences p;
    Store(Context c) { p = c.getSharedPreferences("mi_pastilla_data", Context.MODE_PRIVATE); }
    String start() { return p.getString("start", LocalDate.now().toString()); }
    String end() { return p.getString("end", "2026-11-22"); }
    int hour() { return p.getInt("hour", 22); }
    int minute() { return p.getInt("minute", 0); }
    int dose() { return p.getInt("dose", 2); } // cuartos: 1,2,3,4
    boolean enabled() { return p.getBoolean("enabled", false); }
    void save(String start, String end, int hour, int minute, int dose) {
        p.edit().putString("start", start).putString("end", end).putInt("hour", hour).putInt("minute", minute).putInt("dose", dose).putBoolean("enabled", true).apply();
    }
    boolean scheduled(LocalDate date) {
        return enabled() && !date.isBefore(LocalDate.parse(start())) && !date.isAfter(LocalDate.parse(end()));
    }
    int doseFor(LocalDate date) { return p.getInt("override_"+date, dose()); }
    void override(LocalDate date, int quarters) { p.edit().putInt("override_"+date, quarters).apply(); }
    void removeOverride(LocalDate date) { p.edit().remove("override_"+date).apply(); }
    boolean taken(LocalDate d) { return p.contains("taken_"+d); }
    String recorded(LocalDate d) { return p.getString("taken_"+d, ""); }
    void record(LocalDate d, int quarters) {
        try {
            JSONObject entry = new JSONObject();
            entry.put("dose", quarters);
            entry.put("time", java.time.LocalDateTime.now().toString());
            p.edit().putString("taken_"+d, entry.toString()).apply();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
    static String dosage(int q) { return q==1?"¼":q==2?"½":q==3?"¾":"1"; }
}
