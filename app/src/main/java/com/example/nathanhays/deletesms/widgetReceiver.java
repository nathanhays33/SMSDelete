package com.example.nathanhays.deletesms;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Created by nathanhays on 8/18/14.
 */
public class widgetReceiver extends AppWidgetProvider {

    public static final String ACTION_BUTTON1_CLICKED = "com.nathanhaze.com.BUTTONCLICKED";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, widgetReceiver.class);
            intent.setAction(ACTION_BUTTON1_CLICKED);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);


            // Get the layout for the App Widget and attach an on-click listener to the button
            RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteView.setOnClickPendingIntent(R.id.button, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current App Widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteView);

            remoteView.setOnClickPendingIntent(R.id.button, pendingIntent);

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String a = intent.getAction();
        String b = ACTION_BUTTON1_CLICKED;
        if (intent.getAction().equals(ACTION_BUTTON1_CLICKED)) {
            //do some really cool stuff here
            /*
            Toast toast = Toast.makeText(context, "Widget button was clicked", Toast.LENGTH_LONG);
            toast.show();
            */

            smsTools temp = new smsTools();
            int count = temp.deleteList("content://sms/", false, context);

            Toast toast = Toast.makeText(context, Integer.toString(count) + " was taken care of", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public static PendingIntent buildButtonPendingIntent(Context context) {
/*
        Toast toast = Toast.makeText(context, "Widget button was clicked", Toast.LENGTH_LONG);
        toast.show();
        */
        // initiate widget update request
        Intent intent = new Intent();
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static CharSequence getDesc() {
        return "Sync to see some of our funniest joke collections";
    }

    private static CharSequence getTitle() {
        return "Funny Jokes";
    }

    public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
        /*
        ComponentName myWidget = new ComponentName(context,
                widgetBroadCast.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, remoteViews);
        */
    }
}