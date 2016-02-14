package com.juanpabloprado.androidsinchmessenger.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.juanpabloprado.androidsinchmessenger.R;
import com.juanpabloprado.androidsinchmessenger.adapters.MessageAdapter;
import com.juanpabloprado.androidsinchmessenger.model.ParseMessage;
import com.juanpabloprado.androidsinchmessenger.service.MessageService;
import com.juanpabloprado.androidsinchmessenger.util.ParseConstants;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public class MessagingActivity extends Activity {

  private static final String TAG = MessagingActivity.class.getSimpleName();
  private String mRecipientId;
  private EditText mMessageBodyField;
  private MessageService.MessageServiceInterface mMessageService;
  private MessageAdapter mMessageAdapter;
  private String mRecipientName;
  private String mCurrentUserId;
  private ServiceConnection mServiceConnection = new MyServiceConnection();
  private MessageClientListener mMessageClientListener = new MyMessageClientListener();
  private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.messaging);

    //bind the messaging service
    bindService(new Intent(this, MessageService.class), mServiceConnection, BIND_AUTO_CREATE);

    //get mRecipientId from the intent
    Intent intent = getIntent();
    mRecipientId = intent.getStringExtra(ParseConstants.RECIPIENT_ID);
    mRecipientName = intent.getStringExtra(ParseConstants.RECIPIENT_NAME);
    mCurrentUserId = ParseUser.getCurrentUser().getObjectId();

    //set the message adapter to the listview
    ListView messagesListView = (ListView) findViewById(R.id.listMessages);
    mMessageAdapter = new MessageAdapter(this);
    messagesListView.setAdapter(mMessageAdapter);
    populateMessageHistory();

    mMessageBodyField = (EditText) findViewById(R.id.messageBodyField);

    findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        sendMessage();
      }
    });
  }

  //get previous messages from parse & display
  private void populateMessageHistory() {
    ParseQuery<ParseMessage> query = ParseQuery.getQuery(ParseConstants.CLASS_MESSAGE);
    String[] userIds = { mCurrentUserId, mRecipientId };
    query.whereContainedIn(ParseConstants.MESSAGE_OBJECT_SENDER_ID_FIELD, Arrays.asList(userIds));
    query.whereContainedIn(ParseConstants.MESSAGE_OBJECT_RECIPIENT_ID_FIELD,
        Arrays.asList(userIds));

    query.orderByAscending(ParseConstants.KEY_CREATED_AT);

    query.findInBackground(new FindCallback<ParseMessage>() {
      @Override public void done(List<ParseMessage> messages, ParseException e) {
        if (e == null) {

          for (int i = 0; i < messages.size(); i++) {
            WritableMessage message =
                new WritableMessage(messages.get(i).getRecipientId(), messages.get(i).getMessage());

            message.addHeader(ParseConstants.DATE,
                dateFormat.format(messages.get(i).getCreatedAt()));

            if (messages.get(i).getSenderId().equals(mCurrentUserId)) {
              mMessageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING,
                  mRecipientName);
            } else {
              mMessageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING,
                  mRecipientName);
            }
          }
        }
      }
    });
  }

  private void sendMessage() {
    String messageBody = mMessageBodyField.getText().toString();
    if (messageBody.isEmpty()) {
      Toast.makeText(this, "Please enter a message", Toast.LENGTH_LONG).show();
      return;
    }

    mMessageService.sendMessage(mRecipientId, messageBody);
    mMessageBodyField.setText("");
  }

  @Override public void onDestroy() {
    mMessageService.removeMessageClientListener(mMessageClientListener);
    unbindService(mServiceConnection);
    super.onDestroy();
  }

  private class MyServiceConnection implements ServiceConnection {
    @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      mMessageService = (MessageService.MessageServiceInterface) iBinder;
      mMessageService.addMessageClientListener(mMessageClientListener);
    }

    @Override public void onServiceDisconnected(ComponentName componentName) {
      mMessageService = null;
    }
  }

  private class MyMessageClientListener implements MessageClientListener {
    @Override public void onMessageFailed(MessageClient client, Message message,
        MessageFailureInfo failureInfo) {
      Toast.makeText(MessagingActivity.this, "Message failed to send.", Toast.LENGTH_LONG).show();
    }

    @Override public void onIncomingMessage(MessageClient client, final Message message) {
      if (message.getSenderId().equals(mRecipientId)) {
        final WritableMessage writableMessage =
            new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
        writableMessage.addHeader(ParseConstants.DATE, dateFormat.format(new Date()));
        mMessageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING,
            mRecipientName);
      }
    }

    @Override public void onMessageSent(MessageClient client, Message message, String recipientId) {

      final WritableMessage writableMessage =
          new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
      writableMessage.addHeader(ParseConstants.DATE, dateFormat.format(new Date()));

      //only add message to parse database if it doesn't already exist there
      ParseQuery<ParseMessage> query = ParseQuery.getQuery(ParseConstants.CLASS_MESSAGE);
      query.whereEqualTo(ParseConstants.MESSAGE_OBJECT_SINCH_ID_FIELD, message.getMessageId());
      query.findInBackground(new FindCallback<ParseMessage>() {
        @Override public void done(List<ParseMessage> messages, ParseException e) {
          if (e == null) {
            if (messages.size() == 0) {
              ParseMessage parseMessage = new ParseMessage();
              parseMessage.setSenderId(mCurrentUserId);
              parseMessage.setRecipientId(mRecipientId);
              parseMessage.setMessage(writableMessage.getTextBody());
              parseMessage.setSinchId(writableMessage.getMessageId());
              parseMessage.saveInBackground();

              mMessageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING,
                  mRecipientName);
            }
          }
        }
      });

      ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
      userQuery.getInBackground(mRecipientId, new GetCallback<ParseUser>() {
        @Override public void done(ParseUser toUser, ParseException e) {
          if (e == null) {
            try {
              sendPushNotification(toUser);
            } catch (JSONException e1) {
              e1.printStackTrace();
            }
          } else {
            Log.e(TAG, e.getMessage());
          }
        }
      });
    }

    @Override
    public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {
    }

    @Override public void onShouldSendPushData(MessageClient client, Message message,
        List<PushPair> pushPairs) {
    }
  }

  private void sendPushNotification(final ParseUser toUser) throws JSONException {
    ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
    query.whereEqualTo(ParseConstants.INSTALLATION_OBJECT_USER_FIELD, toUser);

    JSONObject data = new JSONObject();
    data.put(ParseConstants.KEY_PUSH_ALERT,
        getString(R.string.push_message, ParseUser.getCurrentUser().getUsername()));
    data.put(ParseConstants.KEY_PUSH_FROM_USER_ID, ParseUser.getCurrentUser().getObjectId());

    // send push notification
    ParsePush push = new ParsePush();
    push.setQuery(query);
    push.setData(data);
    push.sendInBackground(new SendCallback() {
      @Override public void done(ParseException e) {
        if (e == null) {
          // Success
          Log.i(TAG, "Notification was send successfully to %s" + toUser.getUsername() + " user");
        } else {
          Log.e(TAG, e.getMessage());
        }
      }
    });
  }
}
