package com.friscotap.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "Frisco Receiver";
    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        // Create an intent to start the service
        Intent serviceIntent = new Intent(context, NotificationService.class);
        context.startService(serviceIntent);
    }

    public void setAlarm(Context context) {
        Log.d(TAG, "setAlarm()");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent;

        // Setup the pending intent with alarmIntent (containing the beerlist)
        pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Setup the repeating alarm event
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,            		// Type
                System.currentTimeMillis() + (15 * MINUTE),	// Time to start in milliseconds
                15 * MINUTE,								// Time interval
                pendingIntent								// PendingIntent action to take
        );

    }

    public void cancelAlarm(Context context) {
        Log.d(TAG, "cancelAlarm()");

        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public void updateAlarm(Context context) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE);

        // Cancel everything
        alarmManager.cancel(sender);
        sender.cancel();

        // TODO: Set the notification duration from the settings

        // Re-create the pending intent
    }
}
