package com.friscotap.notification;

import java.util.ArrayList;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.friscotap.core.Beer;
import com.friscotap.core.BeerDataSource;
import com.friscotap.core.FriscoMain;

public class NotificationService extends IntentService {
    private static final String TAG = "Frisco NotificationService";
    private static final String NOTIFICATION_ID = "NewBeersTapped";
    private BeerDataSource mDataSource;
    private ArrayList<Beer> dbList;

    public NotificationService(String name) {
        super(name);
    }

    public NotificationService() {
        // Required Empty Constructor
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onStartCommand()");
        int numNewBeers = 0;
        ArrayList<Beer> webList;
        Context context = this;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                TAG
        );
        try {
            // Acquire lock
            wl.acquire();

            // Open the database and grab the beer list stored
            mDataSource = new BeerDataSource(context);
            mDataSource.open();
            dbList = (ArrayList<Beer>) mDataSource.getAllBeers();
            mDataSource.close();

            // Do a remote update and compare to the beers from the database
            webList = RemoteUpdateTask();

            if(webList != null) {
                // If the remote update was successful, then perform the compare
                numNewBeers = compareAndUpdate(webList);
            }

            // Create/Update with a notification if there are new beers found
            if(numNewBeers > 0) {
                Intent mainIntent = new Intent(context, FriscoMain.class);

                PendingIntent pendingIntent =
                        PendingIntent.getActivity(
                                context,
                                0,
                                mainIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT
                        );

                NotificationManager notifyManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

                builder.setContentTitle("Frisco Taphouse");
                builder.setContentText("" + numNewBeers + " new beers tapped!");
                builder.setSmallIcon(com.friscotap.core.R.drawable.frisco_notify);
                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);
                builder.setPriority(Notification.PRIORITY_HIGH);
                builder.setDefaults(
                        Notification.DEFAULT_LIGHTS |
                                Notification.DEFAULT_VIBRATE
                );

                // Setup the notification to cancel on click and send the
                // notification.
                Notification beerNotify = builder.build();
                notifyManager.notify(NOTIFICATION_ID, 0, beerNotify);
            }

            // Release lock
            wl.release();
        }
        catch(Exception e) {
            Log.d(TAG, "" + e.toString());

            // Release lock
            wl.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int compareAndUpdate(ArrayList<Beer> newList) {
        Log.d(TAG, "compareAndUpdate()");
        int iReturn = 0;

        for(int i = 0; i < newList.size(); i++) {
            // Get the current beer that we're checking
            Beer beer = newList.get(i);

            // Now check to see if the current beer is in the old list.
            int oldIndex = dbList.indexOf(beer);

            // We really only care about completely new beers, so this is
            // the only check we need.
            if(oldIndex == -1) {
                // If it's not in the old list, then the beer is new.
                iReturn++;
            }
        }

        return iReturn;
    }

    private ArrayList<Beer> RemoteUpdateTask() {
        Log.d(TAG, "RemoteUpdateTask()");

        Document listResponse;
        Elements listItems = null;
        ArrayList<Beer> newList = new ArrayList<Beer>();

        try {
            //listResponse = Jsoup.connect(FriscoHelper.REMOTE_URL).get();
            //listItems = listResponse.select(FriscoHelper.QUERY_DRAFTS);

            // Add new beers based on the webpage get request
            for(Element li : listItems) {
                String beerName = li.text();
                Beer b = new Beer();

                if(beerName.contains("*")) {
                    b.setOunces(10);
                    b.setName(beerName.substring(0, beerName.length() - 1));
                }
                else {
                    b.setName(beerName);
                }

                newList.add(b);
            }
        }
        catch (Exception e) {
            Log.d(TAG, "Exception: " + e.toString());
            e.printStackTrace();
        }

        return newList;
    }
}
