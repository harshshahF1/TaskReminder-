package com.example.taskreminder;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;
import java.util.*;

public class AlarmActivity extends Activity {
    private TextToSpeech tts;
    private MediaPlayer alarmPlayer;
    private long taskId;
    private final Handler handler=new Handler(Looper.getMainLooper());

    private final Runnable speak=new Runnable(){
        @Override public void run(){
            Task t=TaskStore.find(AlarmActivity.this,taskId);
            if(t!=null && t.active && tts!=null){
                tts.speak(t.name,TextToSpeech.QUEUE_FLUSH,null,"taskReminder");
                handler.postDelayed(this,3500);
            }
        }
    };

    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+0.5f);}

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        // Modern Android lock-screen behavior: wake the display and show this alarm
        // directly above the keyguard, without requiring the user to unlock first.
        if(Build.VERSION.SDK_INT>=27){
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );
        if(Build.VERSION.SDK_INT>=26){
            getWindow().setStatusBarColor(Color.rgb(10,16,31));
            getWindow().setNavigationBarColor(Color.rgb(10,16,31));
        }
        // Ask Android to remove the keyguard only when the device policy allows it.
        // On a secure lock screen the alarm remains visible above the lock screen.
        try{
            android.app.KeyguardManager km=(android.app.KeyguardManager)getSystemService(KEYGUARD_SERVICE);
            if(km!=null && km.isKeyguardLocked() && Build.VERSION.SDK_INT>=26){
                km.requestDismissKeyguard(this,null);
            }
        }catch(Exception ignored){}
        if(Build.VERSION.SDK_INT>=27) getWindow().setNavigationBarColor(Color.rgb(10,16,31));
        if(Build.VERSION.SDK_INT>=23) getWindow().getDecorView().setSystemUiVisibility(0);

        taskId=getIntent().getLongExtra("taskId",-1);
        Task task=TaskStore.find(this,taskId);
        if(task==null || !task.active){finish();return;}

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24),dp(34),dp(24),dp(24));
        root.setBackgroundColor(Color.rgb(10,16,31));

        TextView badge=tv("⏰  DAILY ALARM",14,Color.rgb(171,220,198),true);
        badge.setGravity(Gravity.CENTER);
        root.addView(badge,new LinearLayout.LayoutParams(-1,dp(44)));

        TextView title=tv("It’s time to focus",26,Color.WHITE,true);
        title.setGravity(Gravity.CENTER);
        root.addView(title,new LinearLayout.LayoutParams(-1,dp(58)));

        TextView taskView=tv(task.name,31,Color.WHITE,true);
        taskView.setGravity(Gravity.CENTER);
        taskView.setPadding(dp(12),dp(18),dp(12),dp(18));
        root.addView(taskView,new LinearLayout.LayoutParams(-1,0,1));

        TextView hint=tv("Alarm is ringing • Your task is waiting",14,Color.rgb(180,190,211),false);
        hint.setGravity(Gravity.CENTER);
        root.addView(hint,new LinearLayout.LayoutParams(-1,dp(42)));

        Button stop=new Button(this);
        stop.setText("STOP ALARM");
        stop.setAllCaps(false);
        stop.setTextSize(18);
        stop.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        stop.setTextColor(Color.rgb(10,16,31));
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.rgb(171,220,198));
        bg.setCornerRadius(dp(22));
        stop.setBackground(bg);
        stop.setOnClickListener(v->acknowledge());
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(62));
        sp.topMargin=dp(8);
        root.addView(stop,sp);

        TextView completeHint=tv("Stopping the alarm does not mark the task as done.",13,Color.rgb(150,161,185),false);
        completeHint.setGravity(Gravity.CENTER);
        completeHint.setPadding(0,dp(12),0,0);
        root.addView(completeHint,new LinearLayout.LayoutParams(-1,dp(42)));

        setContentView(root);
        startAlarmSound();
        tts=new TextToSpeech(this,status->{
            if(status==TextToSpeech.SUCCESS){
                tts.setLanguage(Locale.getDefault());
                tts.setSpeechRate(0.95f);
                speak.run();
            }
        });
    }

    TextView tv(String s,int size,int color,boolean bold){
        TextView v=new TextView(this);
        v.setText(s);v.setTextSize(size);v.setTextColor(color);
        if(bold)v.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
        return v;
    }

    private void startAlarmSound(){
        try{
            Uri uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if(uri==null) uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            alarmPlayer=MediaPlayer.create(this,uri);
            if(alarmPlayer!=null){
                alarmPlayer.setAudioStreamType(android.media.AudioManager.STREAM_ALARM);
                alarmPlayer.setLooping(true);
                alarmPlayer.start();
            }
        }catch(Exception ignored){}
    }

    private void stopAlarmSound(){
        if(alarmPlayer!=null){
            try{if(alarmPlayer.isPlaying())alarmPlayer.stop();}catch(Exception ignored){}
            alarmPlayer.release();alarmPlayer=null;
        }
    }

    void acknowledge(){
        handler.removeCallbacks(speak);
        if(tts!=null)tts.stop();
        stopAlarmSound();
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel((int)taskId);
        finish();
    }

    @Override public void onBackPressed(){ acknowledge(); }

    @Override protected void onDestroy(){
        handler.removeCallbacks(speak);
        stopAlarmSound();
        if(tts!=null){tts.stop();tts.shutdown();tts=null;}
        super.onDestroy();
    }
}