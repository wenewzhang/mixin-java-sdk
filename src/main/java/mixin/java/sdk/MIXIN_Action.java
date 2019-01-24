package mixin.java.sdk;

import com.google.gson.JsonObject;

public enum MIXIN_Action {

  LIST_PENDING_MESSAGES,
  CREATE_MESSAGE,
  ACKNOWLEDGE_MESSAGE_RECEIPT,
  ADD,
  ERROR,
  NOT_IMPLEMENTED_BY_THIS_SDK_YET;

  public static MIXIN_Action parseFrom(JsonObject obj) {
    return parseFrom(obj.get("action").getAsString());
  }

  public static MIXIN_Action parseFrom(String value) {
    if (value == null) {
      throw new IllegalArgumentException("the value to parse cannot be null");
    } else if (value.length() == 0) {
      return null;
    } else {
      try {
        return MIXIN_Action.valueOf(value);
      } catch (Exception e) {
        return NOT_IMPLEMENTED_BY_THIS_SDK_YET;
      }
    }
  }
}
