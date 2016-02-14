package com.juanpabloprado.androidsinchmessenger;

import android.app.Application;
import com.juanpabloprado.androidsinchmessenger.model.ParseMessage;
import com.juanpabloprado.androidsinchmessenger.util.ParseConstants;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class MessengerApplication extends Application {
  @Override public void onCreate() {
    super.onCreate();

    ParseObject.registerSubclass(ParseMessage.class);
    Parse.initialize(this);
    ParseInstallation.getCurrentInstallation().saveInBackground();
  }

  public static void updateParseInstallation(ParseUser user) {
    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
    installation.put(ParseConstants.INSTALLATION_OBJECT_USER_FIELD, user);
    installation.saveInBackground();
  }
}
