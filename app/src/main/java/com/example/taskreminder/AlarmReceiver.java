package com.example.taskreminder;

import android.content.*;import android.os.*;import androidx.core.app.NotificationCompat;import android.app.*;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL="task_alarm";
    @Override public void onReceive(Context c,Intent intent){
        long id=intent.getLongExtra("taskId",-1); Task t=TaskStore.find(c,id); if(t==null||!t.active)return;
        java.util.List<Task> list=TaskStore.load(c); for(Task x:list)if(x.id==id){x.lastAcknowledgedDate="";} TaskStore.save(c,list);
        createChannel(c);
        Intent ai=new Intent(c,AlarmActivity.class).putExtra("taskId",id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent full=PendingIntent.getActivity(c,(int)(id+100000),ai,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification n=new NotificationCompat.Builder(c,CHANNEL).setSmallIcon(com.example.taskreminder.R.drawable.ic_launcher).setContentTitle("Task Reminder").setContentText(t.name).setPriority(NotificationCompat.PRIORITY_MAX).setCategory(NotificationCompat.CATEGORY_ALARM).setOngoing(true).setFullScreenIntent(full,true).build();
        ((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).notify((int)id,n);
        AlarmScheduler.schedule(c,t);
        c.startActivity(ai);
    }
    public static void createChannel(Context c){ if(Build.VERSION.SDK_INT>=26){NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE); NotificationChannel ch=new NotificationChannel(CHANNEL,"Task alarms",NotificationManager.IMPORTANCE_HIGH); ch.setDescription("Daily task reminders"); ch.setSound(null,null); nm.createNotificationChannel(ch);} }
}
