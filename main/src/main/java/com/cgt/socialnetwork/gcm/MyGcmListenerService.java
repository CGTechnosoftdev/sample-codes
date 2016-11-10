package com.cgt.socialnetwork.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;
import com.cgt.socialnetwork.MainApp;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.ui.ActivityMain;
import com.cgt.socialnetwork.utils.Utils;

import java.util.Random;

/**
 * Created by CGTechnosoft
 */
public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = "MyGcmListenerService";

    @Override
    public void onMessageReceived(String from, Bundle data) {

        int action = Utils.parseInt(data.getString(Constants.KEY_ACTION));
        String message = data.getString(Constants.KEY_MESSAGE);

        switch (action) {
            case Constants.TAG_CREATED:
            case Constants.TAG_LIKED:
            case Constants.TAG_REPLY:
                AppModuleManager.getInstance(this).getFeedController().fetchFeedsAsync(true);
                break;
        }

        Preference.getInstance(this).put(Constants.PREF_KEY_RELOAD_NOTIFICATIONS, true);
        sendNotification(message);
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, ActivityMain.class);
        intent.putExtra(Constants.KEY_CALLING_COMPONENT, ActivityMain.NOTIFICATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 4587, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(MainApp.getInstance().getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }
}