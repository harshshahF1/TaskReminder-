package com.example.taskreminder;

import android.app.*;import android.content.*;import android.graphics.Color;import android.os.*;import android.speech.tts.TextToSpeech;import android.view.*;import android.widget.*;import java.util.*;

public class AlarmActivity extends Activity {
    TextToSpeech tts; long taskId; Handler h=new Handler(Looper.getMainLooper());
    Runnable speak=new Runnable(){public void run(){Task t=TaskStore.find(AlarmActivity.this,taskId);if(t!=null&&t.active){tts.speak(t.name,TextToSpeech.QUEUE_FLUSH,null,"task");h.postDelayed(this,4000);}}};
    int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);taskId=getIntent().getLongExtra("taskId",-1);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER);root.setPadding(dp(28),dp(28),dp(28),dp(28));
        TextView head=new TextView(this);head.setText("DAILY REMINDER");head.setTextSize(18);head.setTextColor(Color.GRAY);head.setGravity(Gravity.CENTER);root.addView(head,new LinearLayout.LayoutParams(-1,dp(50)));
        TextView task=new TextView(this);Task t=TaskStore.find(this,taskId);task.setText(t==null?"Task":t.name);task.setTextSize(30);task.setTextColor(Color.BLACK);task.setGravity(Gravity.CENTER);root.addView(task,new LinearLayout.LayoutParams(-1,0,1));
        Button ok=new Button(this);ok.setText("OK — STOP ALERT");ok.setTextSize(18);ok.setOnClickListener(v->acknowledge());root.addView(ok,new LinearLayout.LayoutParams(-1,dp(65)));setContentView(root);
        tts=new TextToSpeech(this,status->{if(status==TextToSpeech.SUCCESS){tts.setLanguage(Locale.getDefault());speak.run();}});
    }
    void acknowledge(){h.removeCallbacks(speak);if(tts!=null)tts.stop();((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel((int)taskId);finish();}
    @Override protected void onDestroy(){h.removeCallbacks(speak);if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
