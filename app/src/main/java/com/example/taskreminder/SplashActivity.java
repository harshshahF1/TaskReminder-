package com.example.taskreminder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {
    int dp(float x) { return (int)(x * getResources().getDisplayMetrics().density + 0.5f); }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(Color.rgb(12, 18, 34));
        getWindow().setNavigationBarColor(Color.rgb(12, 18, 34));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(24), dp(28), dp(24));

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(18, 27, 50), Color.rgb(38, 48, 92), Color.rgb(12, 18, 34)}
        );
        root.setBackground(bg);

        ImageView logo = new ImageView(this);
        logo.setImageResource(com.example.taskreminder.R.drawable.ic_launcher);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        root.addView(logo, new LinearLayout.LayoutParams(dp(128), dp(128)));

        TextView title = new TextView(this);
        title.setText("TaskReminder");
        title.setTextColor(Color.WHITE);
        title.setTextSize(32);
        title.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(18), 0, dp(6));
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("Stay on time. Finish what matters.");
        subtitle.setTextColor(Color.rgb(205, 213, 235));
        subtitle.setTextSize(15);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, -2));

        TextView developer = new TextView(this);
        developer.setText("Developed By Harsh Shah");
        developer.setTextColor(Color.rgb(185, 195, 220));
        developer.setTextSize(14);
        developer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams devParams = new LinearLayout.LayoutParams(-1, -2);
        devParams.gravity = Gravity.BOTTOM;
        devParams.topMargin = dp(190);
        root.addView(developer, devParams);

        setContentView(root);

        new android.os.Handler().postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 1600);
    }
}
