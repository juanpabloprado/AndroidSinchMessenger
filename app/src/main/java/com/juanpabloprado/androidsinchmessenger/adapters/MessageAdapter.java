package com.juanpabloprado.androidsinchmessenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.juanpabloprado.androidsinchmessenger.R;
import com.parse.ParseUser;
import com.sinch.android.rtc.messaging.Message;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends BaseAdapter {

  public static final int DIRECTION_INCOMING = 0;
  public static final int DIRECTION_OUTGOING = 1;

  private List<Message> messages;
  private Context mContext;
  private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());

  public MessageAdapter(Context context) {
    this.mContext = context;
    messages = new ArrayList<>();
  }

  public void addMessage(Message message) {
    messages.add(message);
    notifyDataSetChanged();
  }

  @Override public int getCount() {
    return messages.size();
  }

  @Override public Object getItem(int i) {
    return messages.get(i);
  }

  @Override public long getItemId(int i) {
    return i;
  }

  @Override public int getViewTypeCount() {
    return 2;
  }

  @Override public int getItemViewType(int i) {
    String currentUserId = ParseUser.getCurrentUser().getObjectId();
    if (messages.get(i).getSenderId().equals(currentUserId)) {
      return DIRECTION_OUTGOING;
    } else {
      return DIRECTION_INCOMING;
    }
  }

  @Override public View getView(int i, View convertView, ViewGroup viewGroup) {
    int direction = getItemViewType(i);
    ViewHolder holder;

    //show message on left or right, depending on if
    //it's incoming or outgoing
    if (convertView == null) {
      int res = 0;
      if (direction == DIRECTION_INCOMING) {
        res = R.layout.message_right;
      } else if (direction == DIRECTION_OUTGOING) {
        res = R.layout.message_left;
      }
      convertView = LayoutInflater.from(mContext).inflate(res, null);

      holder = new ViewHolder();
      holder.txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
      holder.nameField = (TextView) convertView.findViewById(R.id.txtSender);
      holder.dateField = (TextView) convertView.findViewById(R.id.txtDate);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    convertView.setEnabled(false);
    convertView.setOnClickListener(null);

    Message message = messages.get(i);
    String name = message.getSenderId();

    holder.dateField.setText(dateFormat.format(message.getTimestamp()));
    holder.txtMessage.setText(message.getTextBody());
    holder.nameField.setText(name);

    return convertView;
  }

  private static class ViewHolder {
    TextView txtMessage;
    TextView nameField;
    TextView dateField;
  }
}

