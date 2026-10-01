package com.example.taskreminder;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.Gravity;
import android.widget.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout list;
    ArrayList<Task> tasks;
    final int navy=Color.rgb(36,53,107), ink=Color.rgb(29,37,57), muted=Color.rgb(105,116,140), canvas=Color.rgb(245,247,252);
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(canvas);getWindow().setNavigationBarColor(canvas);getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);if(Build.VERSION.SDK_INT>=33)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},44);build();}
    GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    TextView textView(String s,int sp,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(color);if(bold)v.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));return v;}
    Button button(String label,boolean primary){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(15);b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));b.setTextColor(primary?Color.WHITE:navy);b.setBackground(shape(primary?navy:Color.rgb(232,237,250),16));return b;}
    void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(20),dp(18),dp(20),dp(12));root.setBackgroundColor(canvas);
        LinearLayout header=new LinearLayout(this);header.setOrientation(LinearLayout.HORIZONTAL);header.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.ic_launcher);header.addView(logo,new LinearLayout.LayoutParams(dp(52),dp(52)));
        LinearLayout titles=new LinearLayout(this);titles.setOrientation(LinearLayout.VERTICAL);titles.setPadding(dp(12),0,0,0);
        titles.addView(textView("TaskReminder",25,ink,true));titles.addView(textView("Stay on time. Finish what matters.",13,muted,false));header.addView(titles,new LinearLayout.LayoutParams(0,-2,1));root.addView(header,new LinearLayout.LayoutParams(-1,dp(70)));
        TextView section=textView("Your daily focus",19,ink,true);section.setPadding(dp(2),dp(22),0,dp(12));root.addView(section);
        Button add=button("+  Add daily task",true);add.setOnClickListener(v->addTask());root.addView(add,new LinearLayout.LayoutParams(-1,dp(54)));
        Button history=button("View completed history",false);history.setOnClickListener(v->showHistory());LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(48));hp.topMargin=dp(8);root.addView(history,hp);
        ScrollView sv=new ScrollView(this);sv.setClipToPadding(false);sv.setPadding(0,dp(14),0,0);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);sv.addView(list);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);refresh();
    }
    void refresh(){tasks=new ArrayList<>(TaskStore.load(this));list.removeAllViews();if(tasks.isEmpty()){LinearLayout empty=new LinearLayout(this);empty.setOrientation(LinearLayout.VERTICAL);empty.setGravity(Gravity.CENTER);empty.setPadding(dp(18),dp(28),dp(18),dp(28));empty.setBackground(shape(Color.WHITE,20));TextView e=textView("No tasks yet",18,ink,true);e.setGravity(Gravity.CENTER);empty.addView(e);TextView hint=textView("Add your first daily reminder to get started.",14,muted,false);hint.setGravity(Gravity.CENTER);empty.addView(hint);list.addView(empty);return;}for(Task t:tasks)if(t.active){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(16),dp(10),dp(16),dp(12));row.setBackground(shape(Color.WHITE,20));TextView n=textView(t.name,18,ink,true);n.setPadding(0,dp(2),0,dp(4));TextView time=textView(String.format(Locale.US,"◷  Every day at %02d:%02d",t.hour,t.minute),14,muted,false);Button done=button("Mark as done",false);done.setOnClickListener(v->complete(t.id));row.addView(n);row.addView(time);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(44));bp.topMargin=dp(8);row.addView(done,bp);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.bottomMargin=dp(12);list.addView(row,rp);}}
    void addTask(){final EditText name=new EditText(this);name.setHint("Task name (e.g. Analyse report)");final TimePicker tp=new TimePicker(this);tp.setIs24HourView(true);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(8),0,dp(8),0);box.addView(name);box.addView(tp);new AlertDialog.Builder(this).setTitle("New daily task").setView(box).setPositiveButton("Save",(d,w)->{String s=name.getText().toString().trim();if(s.isEmpty())return;Task t=new Task();t.id=System.currentTimeMillis();t.name=s;t.hour=tp.getHour();t.minute=tp.getMinute();tasks.add(t);TaskStore.save(this,tasks);AlarmScheduler.schedule(this,t);refresh();}).setNegativeButton("Cancel",null).show();}
    void complete(long id){Task t=TaskStore.find(this,id);if(t==null)return;t.active=false;t.completedAt=System.currentTimeMillis();ArrayList<Task> l=new ArrayList<>(TaskStore.load(this));for(Task x:l)if(x.id==id){x.active=false;x.completedAt=t.completedAt;}TaskStore.save(this,l);AlarmScheduler.cancel(this,t);refresh();}
    void showHistory(){StringBuilder s=new StringBuilder();for(Task t:TaskStore.load(this))if(!t.active&&t.completedAt>0)s.append("✓ ").append(t.name).append(" — ").append(new SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new Date(t.completedAt))).append("\n\n");if(s.length()==0)s.append("No completed tasks yet.");new AlertDialog.Builder(this).setTitle("Completed History").setMessage(s.toString()).setPositiveButton("OK",null).show();}
}
