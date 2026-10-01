package com.example.taskreminder;

import org.json.JSONObject;

public class Task {
    public long id;
    public String name;
    public int hour, minute;
    public boolean active = true;
    public long completedAt = 0;
    public String lastAcknowledgedDate = "";

    public JSONObject toJson() throws Exception {
        JSONObject o = new JSONObject();
        o.put("id", id); o.put("name", name); o.put("hour", hour); o.put("minute", minute);
        o.put("active", active); o.put("completedAt", completedAt); o.put("lastAcknowledgedDate", lastAcknowledgedDate);
        return o;
    }
    public static Task fromJson(JSONObject o) throws Exception {
        Task t = new Task(); t.id=o.getLong("id"); t.name=o.getString("name"); t.hour=o.getInt("hour"); t.minute=o.getInt("minute");
        t.active=o.optBoolean("active", true); t.completedAt=o.optLong("completedAt",0); t.lastAcknowledgedDate=o.optString("lastAcknowledgedDate",""); return t;
    }
}
