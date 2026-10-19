package com.qawai.vpncheck;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.widget.RemoteViews;

public class StatusWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager mgr, int[] ids) {
        SharedPreferences p =
                context.getSharedPreferences("vpncheck", Context.MODE_PRIVATE);
        String status = p.getString("w_status", "—");
        String time = p.getString("w_time", "");
        String color = p.getString("w_color", "#FF66BB6A");

        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
        rv.setTextViewText(R.id.w_title, "Check Connection");
        rv.setTextViewText(R.id.w_status, status);
        rv.setTextViewText(R.id.w_time, time);
        try {
            rv.setInt(R.id.w_root, "setBackgroundColor", Color.parseColor("#E6101418"));
        } catch (Exception ignored) {}
        try {
            rv.setTextColor(R.id.w_status, Color.parseColor(color));
        } catch (Exception ignored) {}

        Intent open = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        if (open != null) {
            open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
            PendingIntent pi = PendingIntent.getActivity(context, 0, open, flags);
            rv.setOnClickPendingIntent(R.id.w_refresh, pi);
            rv.setOnClickPendingIntent(R.id.w_title, pi);
        }

        mgr.updateAppWidget(ids, rv);
    }

    static void push(Context context) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        ComponentName cn = new ComponentName(context, StatusWidget.class);
        int[] ids = mgr.getAppWidgetIds(cn);
        if (ids != null && ids.length > 0) {
            new StatusWidget().onUpdate(context, mgr, ids);
        }
    }
}
