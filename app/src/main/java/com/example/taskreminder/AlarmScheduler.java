package com.example.taskreminder;

import android.app.*;
import android.content.*;
import android.os.Build;
import java.util.*;

public class AlarmScheduler {
    public static void schedule(Context c, Task t) {
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        Intent i=new Intent(c,AlarmReceiver.class).putExtra("taskId",t.id);
        PendingIntent pi=PendingIntent.getBroadcast(c,(int)t.id,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,t.hour);
        cal.set(Calendar.MINUTE,t.minute);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        if(cal.getTimeInMillis()<=System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR,1);

        if(Build.VERSION.SDK_INT>=31 && am.canScheduleExactAlarms()){
            Intent show=new Intent(c,MainActivity.class);
            PendingIntent showIntent=PendingIntent.getActivity(c,(int)(t.id+500000),show,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            AlarmManager.AlarmClockInfo info=new AlarmManager.AlarmClockInfo(cal.getTimeInMillis(),showIntent);
            am.setAlarmClock(info,pi);
        }else if(Build.VERSION.SDK_INT>=23){
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pi);
        }else{
            am.setExact(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pi);
        }
    }

    public static void cancel(Context c, Task t) {
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        Intent i=new Intent(c,AlarmReceiver.class).putExtra("taskId",t.id);
        PendingIntent pi=PendingIntent.getBroadcast(c,(int)t.id,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);pi.cancel();
    }
}