package com.example.taskreminder;

import android.Manifest;import android.app.*;import android.content.*;import android.graphics.Color;import android.os.*;import android.view.*;import android.widget.*;import java.text.*;import java.util.*;

public class MainActivity extends Activity {
    LinearLayout list; ArrayList<Task> tasks;
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    @Override public void onCreate(Bundle b){super.onCreate(b); if(Build.VERSION.SDK_INT>=33)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},44); build();}
    TextView tv(String s,int sp){TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(Color.DKGRAY);v.setPadding(dp(16),dp(12),dp(16),dp(12));return v;}
    void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(16),dp(16),dp(16),dp(8));
        TextView title=tv("Task Reminder",26); title.setTextColor(Color.BLACK);root.addView(title,new LinearLayout.LayoutParams(-1,dp(55)));
        Button add=new Button(this);add.setText("+ Add daily task");add.setOnClickListener(v->addTask());root.addView(add,new LinearLayout.LayoutParams(-1,dp(52)));
        Button history=new Button(this);history.setText("History");history.setOnClickListener(v->showHistory());root.addView(history,new LinearLayout.LayoutParams(-1,dp(48)));
        ScrollView sv=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);sv.addView(list);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);refresh();
    }
    void refresh(){tasks=new ArrayList<>(TaskStore.load(this));list.removeAllViews();if(tasks.isEmpty())list.addView(tv("No tasks yet. Add your first daily reminder.",16)); for(Task t:tasks)if(t.active){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(0,dp(8),0,dp(8));TextView n=tv(t.name,19);TextView time=tv(String.format(Locale.US,"Every day at %02d:%02d",t.hour,t.minute),15);Button done=new Button(this);done.setText("Mark Done");done.setOnClickListener(v->complete(t.id));row.addView(n);row.addView(time);row.addView(done);list.addView(row);}}
    void addTask(){ final EditText name=new EditText(this);name.setHint("Task name (e.g. Analyse report of data breach)"); final TimePicker tp=new TimePicker(this);tp.setIs24HourView(true); LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(8),0,dp(8),0);box.addView(name);box.addView(tp); new AlertDialog.Builder(this).setTitle("New daily task").setView(box).setPositiveButton("Save",(d,w)->{String s=name.getText().toString().trim();if(s.isEmpty())return;Task t=new Task();t.id=System.currentTimeMillis();t.name=s;t.hour=tp.getHour();t.minute=tp.getMinute();tasks.add(t);TaskStore.save(this,tasks);AlarmScheduler.schedule(this,t);refresh();}).setNegativeButton("Cancel",null).show();}
    void complete(long id){Task t=TaskStore.find(this,id);if(t==null)return; t.active=false;t.completedAt=System.currentTimeMillis();ArrayList<Task> l=new ArrayList<>(TaskStore.load(this));for(Task x:l)if(x.id==id){x.active=false;x.completedAt=t.completedAt;}TaskStore.save(this,l);AlarmScheduler.cancel(this,t);refresh();}
    void showHistory(){StringBuilder s=new StringBuilder();for(Task t:TaskStore.load(this))if(!t.active&&t.completedAt>0)s.append("✓ ").append(t.name).append(" — ").append(new SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new Date(t.completedAt))).append("\n\n");if(s.length()==0)s.append("No completed tasks yet.");new AlertDialog.Builder(this).setTitle("Completed History").setMessage(s.toString()).setPositiveButton("OK",null).show();}
}
