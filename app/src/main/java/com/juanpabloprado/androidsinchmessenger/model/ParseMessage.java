package com.juanpabloprado.androidsinchmessenger.model;

import com.juanpabloprado.androidsinchmessenger.util.ParseConstants;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName(ParseConstants.CLASS_MESSAGE) public class ParseMessage extends ParseObject {
  private String recipientId;
  private String senderId;
  private String message;
  private String sinchId;

  public String getRecipientId() {
    return getString(ParseConstants.MESSAGE_OBJECT_RECIPIENT_ID_FIELD);
  }

  public void setRecipientId(String recipientId) {
    put(ParseConstants.MESSAGE_OBJECT_RECIPIENT_ID_FIELD, recipientId);
  }

  public String getSenderId() {
    return getString(ParseConstants.MESSAGE_OBJECT_SENDER_ID_FIELD);
  }

  public void setSenderId(String senderId) {
    put(ParseConstants.MESSAGE_OBJECT_SENDER_ID_FIELD, senderId);
  }

  public String getMessage() {
    return getString(ParseConstants.MESSAGE_OBJECT_MESSAGE_FIELD);
  }

  public void setMessage(String message) {
    put(ParseConstants.MESSAGE_OBJECT_MESSAGE_FIELD, message);
  }

  public String getSinchId() {
    return getString(ParseConstants.MESSAGE_OBJECT_SINCH_ID_FIELD);
  }

  public void setSinchId(String sinchId) {
    put(ParseConstants.MESSAGE_OBJECT_SINCH_ID_FIELD, sinchId);
  }

}
