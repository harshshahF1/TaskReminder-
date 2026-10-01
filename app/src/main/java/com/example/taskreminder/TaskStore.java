package com.example.taskreminder;

import android.content.Context;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class TaskStore {
    private static final String PREF="tasks";
    public static List<Task> load(Context c) {
        ArrayList<Task> list=new ArrayList<>(); String s=c.getSharedPreferences(PREF,0).getString("data","[]");
        try { JSONArray a=new JSONArray(s); for(int i=0;i<a.length();i++) list.add(Task.fromJson(a.getJSONObject(i))); } catch(Exception ignored) {}
        return list;
    }
    public static void save(Context c,List<Task> list) {
        JSONArray a=new JSONArray(); try { for(Task t:list)a.put(t.toJson()); } catch(Exception ignored) {}
        c.getSharedPreferences(PREF,0).edit().putString("data",a.toString()).apply();
    }
    public static Task find(Context c,long id){ for(Task t:load(c)) if(t.id==id)return t; return null; }
}
