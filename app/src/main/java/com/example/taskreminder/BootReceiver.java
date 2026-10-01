package com.example.taskreminder;
import android.content.*;public class BootReceiver extends BroadcastReceiver{public void onReceive(Context c,Intent i){for(Task t:TaskStore.load(c))if(t.active)AlarmScheduler.schedule(c,t);}}
