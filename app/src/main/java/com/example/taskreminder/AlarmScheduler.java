package com.example.taskreminder;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Build;
import java.util.*;

public class AlarmScheduler {
    public static void schedule(Context c, Task t) {
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if(am==null)return;

        Intent i=new Intent(c,AlarmReceiver.class).putExtra("taskId",t.id);
        PendingIntent pi=PendingIntent.getBroadcast(
                c,(int)t.id,i,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,t.hour);
        cal.set(Calendar.MINUTE,t.minute);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        if(cal.getTimeInMillis()<=System.currentTimeMillis()){
            cal.add(Calendar.DAY_OF_YEAR,1);
        }

        long trigger=cal.getTimeInMillis();

        try{
            if(Build.VERSION.SDK_INT>=31 && am.canScheduleExactAlarms()){
                Intent show=new Intent(c,MainActivity.class);
                PendingIntent showIntent=PendingIntent.getActivity(
                        c,(int)(t.id+500000),show,
                        PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
                AlarmManager.AlarmClockInfo info=
                        new AlarmManager.AlarmClockInfo(trigger,showIntent);
                am.setAlarmClock(info,pi);
            }else if(Build.VERSION.SDK_INT>=23){
                // Fallback for devices where exact-alarm access has not yet been granted.
                // This still wakes during Doze instead of silently failing.
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,trigger,pi);
            }else{
                am.set(AlarmManager.RTC_WAKEUP,trigger,pi);
            }
        }catch(SecurityException e){
            // Never leave a task without an alarm because of exact-alarm restrictions.
            if(Build.VERSION.SDK_INT>=23){
                try{ am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,trigger,pi); }
                catch(Exception ignored){}
            }
        }
    }

    public static boolean canScheduleExact(Context c){
        if(Build.VERSION.SDK_INT<31)return true;
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        return am!=null && am.canScheduleExactAlarms();
    }

    public static void openExactAlarmSettings(Context c){
        if(Build.VERSION.SDK_INT<31)return;
        try{
            Intent i=new Intent(
                    android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:"+c.getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);
        }catch(Exception ignored){
            try{
                Intent i=new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(i);
            }catch(Exception ignoredAgain){}
        }
    }

    public static void cancel(Context c, Task t) {
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if(am==null)return;
        Intent i=new Intent(c,AlarmReceiver.class).putExtra("taskId",t.id);
        PendingIntent pi=PendingIntent.getBroadcast(
                c,(int)t.id,i,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
        pi.cancel();
    }
}
