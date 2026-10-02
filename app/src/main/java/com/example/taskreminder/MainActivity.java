package com.example.taskreminder;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout list;
    ArrayList<Task> tasks;
    final int ink=Color.rgb(24,31,48), muted=Color.rgb(103,113,135), canvas=Color.rgb(246,248,252);
    final int navy=Color.rgb(37,54,110), mint=Color.rgb(177,224,203), soft=Color.rgb(235,240,250);

    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    TextView tv(String s,float size,int color,boolean bold){
        TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);
        if(bold)v.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));return v;
    }
    Button button(String label,boolean primary){
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(15);
        b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        b.setTextColor(primary?Color.WHITE:navy);
        b.setBackground(shape(primary?navy:soft,18));
        b.setPadding(dp(12),0,dp(12),0);return b;
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(canvas);getWindow().setNavigationBarColor(canvas);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if(Build.VERSION.SDK_INT>=33)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},44);
        build();
    }

    @Override protected void onResume(){
        super.onResume();
        if(tasks!=null){
            for(Task t:TaskStore.load(this)){
                if(t.active) AlarmScheduler.schedule(this,t);
            }
        }
    }

    void build(){
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(14),dp(18),dp(10));
        root.setBackgroundColor(canvas);

        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.ic_launcher);
        top.addView(logo,new LinearLayout.LayoutParams(dp(50),dp(50)));

        LinearLayout titleBox=new LinearLayout(this);titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setPadding(dp(12),0,0,0);
        titleBox.addView(tv("TaskReminder",24,ink,true));
        titleBox.addView(tv("Your day, one task at a time.",13,muted,false));
        top.addView(titleBox,new LinearLayout.LayoutParams(0,-2,1));
        root.addView(top,new LinearLayout.LayoutParams(-1,dp(62)));

        LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(18),dp(16),dp(18),dp(16));hero.setBackground(shape(navy,24));
        TextView h1=tv("Stay ahead of your day",22,Color.WHITE,true);
        hero.addView(h1);hero.addView(tv("Set it once. Get an alarm every day until you finish it.",14,Color.rgb(210,219,240),false));
        Button add=button("＋  Add daily task",true);
        add.setBackground(shape(mint,18));add.setTextColor(Color.rgb(19,28,48));add.setOnClickListener(v->addTask());
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,dp(50));ap.topMargin=dp(14);hero.addView(add,ap);
        LinearLayout.LayoutParams herop=new LinearLayout.LayoutParams(-1,-2);herop.topMargin=dp(14);root.addView(hero,herop);

        LinearLayout section=new LinearLayout(this);section.setGravity(Gravity.CENTER_VERTICAL);
        TextView st=tv("Today’s reminders",19,ink,true);section.addView(st,new LinearLayout.LayoutParams(0,-2,1));
        TextView hist=tv("History  ›",14,navy,true);hist.setPadding(dp(8),dp(10),dp(4),dp(10));hist.setOnClickListener(v->showHistory());
        section.addView(hist);LinearLayout.LayoutParams secp=new LinearLayout.LayoutParams(-1,dp(54));secp.topMargin=dp(8);root.addView(section,secp);

        ScrollView sv=new ScrollView(this);sv.setClipToPadding(false);sv.setPadding(0,0,0,dp(8));
        list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);sv.addView(list);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);refresh();
    }

    void refresh(){
        tasks=new ArrayList<>(TaskStore.load(this));list.removeAllViews();
        int active=0;for(Task t:tasks)if(t.active)active++;
        if(active==0){
            LinearLayout empty=new LinearLayout(this);empty.setOrientation(LinearLayout.VERTICAL);empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(20),dp(30),dp(20),dp(30));empty.setBackground(shape(Color.WHITE,22));
            TextView e=tv("Nothing scheduled yet",18,ink,true);e.setGravity(Gravity.CENTER);empty.addView(e);
            TextView h=tv("Add a daily task and TaskReminder will alert you automatically.",14,muted,false);
            h.setGravity(Gravity.CENTER);h.setPadding(dp(10),dp(7),dp(10),0);empty.addView(h);
            list.addView(empty);return;
        }
        for(Task t:tasks)if(t.active)addCard(t);
    }

    void addCard(Task t){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(17),dp(15),dp(17),dp(15));card.setBackground(shape(Color.WHITE,22));

        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
        TextView icon=tv("⏰",25,navy,false);row.addView(icon,new LinearLayout.LayoutParams(dp(42),dp(42)));
        LinearLayout names=new LinearLayout(this);names.setOrientation(LinearLayout.VERTICAL);
        names.addView(tv(t.name,17,ink,true));
        names.addView(tv(String.format(Locale.US,"Every day  •  %02d:%02d",t.hour,t.minute),13,muted,false));
        row.addView(names,new LinearLayout.LayoutParams(0,-2,1));
        card.addView(row);

        LinearLayout status=new LinearLayout(this);status.setGravity(Gravity.CENTER_VERTICAL);
        TextView chip=tv("●  ACTIVE",12,Color.rgb(38,115,82),true);
        chip.setPadding(dp(10),dp(7),dp(10),dp(7));chip.setBackground(shape(Color.rgb(229,246,237),14));
        status.addView(chip,new LinearLayout.LayoutParams(-2,dp(32)));
        Button done=button("Mark as done  ✓",false);done.setOnClickListener(v->complete(t.id));
        LinearLayout.LayoutParams dpms=new LinearLayout.LayoutParams(0,dp(42),1);dpms.leftMargin=dp(10);status.addView(done,dpms);
        LinearLayout.LayoutParams stp=new LinearLayout.LayoutParams(-1,dp(46));stp.topMargin=dp(12);card.addView(status,stp);

        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.bottomMargin=dp(12);list.addView(card,cp);
    }

    void addTask(){
        final Dialog dialog=new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(canvas);
        root.setPadding(dp(20),dp(16),dp(20),dp(14));

        LinearLayout head=new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView back=tv("‹",38,ink,false);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(v->dialog.dismiss());
        head.addView(back,new LinearLayout.LayoutParams(dp(48),dp(52)));
        LinearLayout ht=new LinearLayout(this);
        ht.setOrientation(LinearLayout.VERTICAL);
        ht.addView(tv("New daily reminder",23,ink,true));
        ht.addView(tv("Create an alarm that repeats every day",13,muted,false));
        head.addView(ht,new LinearLayout.LayoutParams(0,-2,1));
        root.addView(head,new LinearLayout.LayoutParams(-1,dp(58)));

        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0,dp(12),0,dp(12));

        TextView taskLabel=tv("TASK",12,muted,true);
        content.addView(taskLabel,new LinearLayout.LayoutParams(-1,dp(28)));

        EditText name=new EditText(this);
        name.setHint("e.g. Analyse report of data breach");
        name.setSingleLine(true);
        name.setTextSize(17);
        name.setPadding(dp(16),0,dp(16),0);
        name.setBackground(shape(Color.WHITE,18));
        content.addView(name,new LinearLayout.LayoutParams(-1,dp(58)));

        TextView timeLabel=tv("ALARM TIME",12,muted,true);
        LinearLayout.LayoutParams tlp=new LinearLayout.LayoutParams(-1,dp(28));
        tlp.topMargin=dp(22);content.addView(timeLabel,tlp);

        LinearLayout timeCard=new LinearLayout(this);
        timeCard.setOrientation(LinearLayout.VERTICAL);
        timeCard.setGravity(Gravity.CENTER);
        timeCard.setPadding(dp(8),dp(10),dp(8),dp(10));
        timeCard.setBackground(shape(Color.WHITE,24));

        TextView every=tv("EVERY DAY",12,Color.rgb(38,115,82),true);
        every.setGravity(Gravity.CENTER);
        timeCard.addView(every,new LinearLayout.LayoutParams(-1,dp(30)));

        TimePicker tp=new TimePicker(this);
        tp.setIs24HourView(false);
        // Keep the native clock at normal scale and give the complete dial enough room.
        // The previous 210dp area + 1.12x scaling clipped the lower half of the clock.
        tp.setScaleX(1.0f);tp.setScaleY(1.0f);
        LinearLayout.LayoutParams tpp=new LinearLayout.LayoutParams(-1,dp(320));
        timeCard.addView(tp,tpp);

        TextView note=tv("The alarm will ring automatically at this time.",13,muted,false);
        note.setGravity(Gravity.CENTER);
        timeCard.addView(note,new LinearLayout.LayoutParams(-1,dp(42)));
        // Let the card wrap around the full clock instead of clipping it to a fixed height.
        LinearLayout.LayoutParams tcp=new LinearLayout.LayoutParams(-1,-2);
        tcp.topMargin=dp(8);content.addView(timeCard,tcp);

        TextView info=tv("🔔  Full-screen alarm  •  🔊  Alarm sound  •  🗣  Task name spoken",13,muted,false);
        info.setGravity(Gravity.CENTER);
        info.setPadding(dp(4),dp(14),dp(4),dp(8));
        content.addView(info,new LinearLayout.LayoutParams(-1,dp(58)));

        scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        Button create=button("Create daily alarm  ✓",true);
        create.setTextSize(17);
        create.setBackground(shape(navy,20));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(58));
        cp.topMargin=dp(8);root.addView(create,cp);

        create.setOnClickListener(v->{
            String s=name.getText().toString().trim();
            if(s.isEmpty()){name.setError("Enter a task name");name.requestFocus();return;}
            Task t=new Task();
            t.id=System.currentTimeMillis();t.name=s;t.hour=tp.getHour();t.minute=tp.getMinute();
            tasks.add(t);
            TaskStore.save(this,tasks);
            AlarmScheduler.schedule(this,t);

            // Android 12+ may require the user to allow exact alarms.
            // Open the system setting once for this newly created reminder.
            if(Build.VERSION.SDK_INT>=31 && !AlarmScheduler.canScheduleExact(this)){
                new Handler(Looper.getMainLooper()).postDelayed(
                        ()->AlarmScheduler.openExactAlarmSettings(this),250);
            }

            refresh();dialog.dismiss();
        });

        dialog.setContentView(root);
        Window w=dialog.getWindow();
        if(w!=null){
            w.setBackgroundDrawableResource(android.R.color.transparent);
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.setOnShowListener(v->{
            Window ww=dialog.getWindow();
            if(ww!=null){
                ww.setLayout(-1,-1);
                ww.setGravity(Gravity.CENTER);
            }
        });
        dialog.show();
        Window w2=dialog.getWindow();
        if(w2!=null){
            w2.setLayout(-1,-1);
            w2.setGravity(Gravity.CENTER);
        }
    }

    void complete(long id){
        Task t=TaskStore.find(this,id);if(t==null)return;
        long now=System.currentTimeMillis();ArrayList<Task> l=new ArrayList<>(TaskStore.load(this));
        for(Task x:l)if(x.id==id){x.active=false;x.completedAt=now;}
        TaskStore.save(this,l);AlarmScheduler.cancel(this,t);refresh();
    }

    void showHistory(){
        StringBuilder s=new StringBuilder();
        for(Task t:TaskStore.load(this))if(!t.active&&t.completedAt>0)
            s.append("✓  ").append(t.name).append("\n")
             .append(new SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(new Date(t.completedAt))).append("\n\n");
        if(s.length()==0)s.append("No completed tasks yet.\n\nFinish a task and it will appear here.");
        TextView body=tv(s.toString(),15,ink,false);body.setPadding(dp(4),dp(8),dp(4),dp(4));
        new AlertDialog.Builder(this).setTitle("Completed history").setView(body).setPositiveButton("Done",null).show();
    }
}