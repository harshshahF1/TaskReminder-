package com.example.taskreminder;

import android.app.*;
import android.content.*;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL="task_alarm";

    @Override public void onReceive(Context c,Intent intent){
        long id=intent.getLongExtra("taskId",-1);
        Task t=TaskStore.find(c,id);
        if(t==null||!t.active)return;

        java.util.List<Task> list=TaskStore.load(c);
        for(Task x:list)if(x.id==id)x.lastAcknowledgedDate="";
        TaskStore.save(c,list);

        createChannel(c);

        Intent ai=new Intent(c,AlarmActivity.class)
                .putExtra("taskId",id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent full=PendingIntent.getActivity(
                c,(int)(id+100000),ai,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        Notification n=new NotificationCompat.Builder(c,CHANNEL)
                .setSmallIcon(com.example.taskreminder.R.drawable.ic_launcher)
                .setContentTitle("⏰  TaskReminder alarm")
                .setContentText(t.name)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(full,true)
                .build();

        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int)id,n);

        AlarmScheduler.schedule(c,t);
        try{c.startActivity(ai);}catch(Exception ignored){}
    }

    public static void createChannel(Context c){
        if(android.os.Build.VERSION.SDK_INT>=26){
            NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel old=nm.getNotificationChannel(CHANNEL);
            if(old==null){
                UriSound:
                NotificationChannel ch=new NotificationChannel(
                        CHANNEL,"Task alarms",NotificationManager.IMPORTANCE_HIGH);
                ch.setDescription("Full-screen daily task alarms");
                android.net.Uri uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                AudioAttributes aa=new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                ch.setSound(uri,aa);
                ch.enableVibration(true);
                ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                nm.createNotificationChannel(ch);
            }
        }
    }
}