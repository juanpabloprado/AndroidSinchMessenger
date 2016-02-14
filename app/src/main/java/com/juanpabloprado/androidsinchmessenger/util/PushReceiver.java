package com.juanpabloprado.androidsinchmessenger.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import com.google.gson.Gson;
import com.parse.ParsePushBroadcastReceiver;

public class PushReceiver extends ParsePushBroadcastReceiver {

  private static final String TAG = PushReceiver.class.getSimpleName();

  @Override protected Bitmap getLargeIcon(Context context, Intent intent) {
    Gson gson = new Gson();
    String extras = intent.getStringExtra(KEY_PUSH_DATA);
    PushNotification notification = gson.fromJson(extras, PushNotification.class);
    if (notification.fromUserId != null) {
      Log.i(TAG, notification.fromUserId);
    }
    return super.getLargeIcon(context, intent);
  }

  private class PushNotification {
    public String alert;
    public String fromUserId;
  }
}
