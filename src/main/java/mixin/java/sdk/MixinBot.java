package mixin.java.sdk;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.security.interfaces.RSAPrivateKey;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.Gson;
public class MixinBot {

  public static WebSocket connectToRemoteMixin(WebSocketListener callback,
                                              RSAPrivateKey pkey, String appid, String sessionid) {
    String token = MixinUtil.JWTTokenGen.genToken("/", "", pkey, appid, sessionid);
    OkHttpClient client = new OkHttpClient.Builder().build();
    Request request = new Request.Builder()
      .addHeader("Sec-WebSocket-Protocol", "MixinBot-Blaze-1")
      .addHeader("Authorization", "Bearer " + token)
      .url("wss://blaze.mixin.one/")
      .build();
    return client.newWebSocket(request, callback);
  }

  private static boolean send(WebSocket webSocket, MIXIN_Action action, String params) {
    JsonObject jsObj = new JsonObject();
    Gson gson = new Gson();
    JsonElement jsXp = gson.fromJson(params, JsonElement.class);
    jsObj.addProperty("id",UUID.randomUUID().toString());
    jsObj.addProperty("action", action.toString());
    jsObj.add("params",jsXp);
    // return webSocket.send(MixinUtil.jsonStrToByteString(jsObj.toString()));

    // String rawJson =
    //   "{" +
    //     "  'id': '" + UUID.randomUUID().toString() + "'," +
    //     "  'action': '" + action + "'," +
    //     "  'params': " + params +
    //     "}";
    // String json = rawJson.replaceAll("'", "\"");
    // System.out.println("-------------------send--begin------------");
    // System.out.println(json);
    // System.out.println(jsObj.toString());
    // System.out.println("-------------------send--end------------");

    return webSocket.send(MixinUtil.jsonStrToByteString(jsObj.toString()));
  }

  public static boolean sendListPendingMessages(WebSocket webSocket) {
    return send(webSocket, MIXIN_Action.LIST_PENDING_MESSAGES, null);
  }

  public static boolean sendMessageAck(WebSocket webSocket, String messageId) {
    String params =
      String.format(("{'message_id':'%s', 'status':'READ'}").replaceAll("'", "\"")
        , messageId);
    return send(webSocket, MIXIN_Action.ACKNOWLEDGE_MESSAGE_RECEIPT, params);
  }

  public static boolean sendText(
    WebSocket webSocket,
    String conversationId,
    String recipientId,
    String data) {
    // String param =
    //   String.format(
    //     ("{'conversation_id':'%s', 'recipient_id':'%s', 'message_id':'%s', 'category':'%s', " +
    //       "'data':'%s'}").replaceAll("'", "\"")
    //     ,
    //     conversationId,
    //     recipientId,
    //     UUID.randomUUID().toString(),
    //     MIXIN_Category.PLAIN_TEXT,
    //     toBase64(data)
    //   );
      JsonObject params = new JsonObject();
      params.addProperty("conversation_id",conversationId);
      params.addProperty("recipient_id",recipientId);
      params.addProperty("message_id",UUID.randomUUID().toString());
      params.addProperty("category",MIXIN_Category.PLAIN_TEXT.toString());
      params.addProperty("data",toBase64(data));
      // System.out.println("-------------------send-Text-begin------------");
      // System.out.println(param);
      // System.out.println(params.toString());
      // System.out.println("-------------------send-Text-end------------");
    return send(webSocket, MIXIN_Action.CREATE_MESSAGE, params.toString());
  }

  public static boolean sendSticker(
    WebSocket webSocket,
    String conversationId,
    String recipientId,
    String data) {
    String params =
      String.format(
        ("{'conversation_id':'%s', 'recipient_id':'%s', 'message_id':'%s', 'category':'%s', " +
          "'data':'%s'}").replaceAll("'", "\"")
        ,
        conversationId,
        recipientId,
        UUID.randomUUID().toString(),
        MIXIN_Category.PLAIN_STICKER,
        toBase64(data)
      );
    return send(webSocket, MIXIN_Action.CREATE_MESSAGE, params);
  }

  public static boolean sendContact(
    WebSocket webSocket,
    String conversationId,
    String recipientId,
    String contactId) {
    String params =
      String.format(
        ("{'conversation_id':'%s', 'recipient_id':'%s', 'message_id':'%s', 'category':'%s', " +
          "'data': '%s'}").replaceAll("'", "\""),
        conversationId,
        recipientId,
        UUID.randomUUID().toString(),
        MIXIN_Category.PLAIN_CONTACT,
        toBase64(String.format("{'user_id': '%s'}".replaceAll("'", "\""), contactId))
      );
    return send(webSocket, MIXIN_Action.CREATE_MESSAGE, params);
  }

  private static HashMap<String, String> makeHeaders(String token) {
    HashMap<String, String> headers = new HashMap<String, String>();
    // headers.put("Mixin-Device-Id", Config.ADMIN_ID);
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + token);
    return headers;
  }

  /*
  public static String assets() {
    String token = MixinUtil.JWTTokenGen.genToken("GET", "/assets", "");
    String res = api.basic.RestAPI.get(
      "https://api.mixin.one/assets",
      makeHeaders(token)
    );
    return res;
  }
  */

  public static void transfer(
    String assetId,
    String counterUserIid,
    double amount, String PIN, byte[] PAY_KEY,
    RSAPrivateKey pkey, String appid, String sessionid) throws IOException {
    String body =
      String.format(
        ("{'asset_id':'%s', 'counter_user_id':'%s', 'amount':'%s', 'memo':'hello', 'pin':'%s', " +
          "'trace_id': '%s'}").replaceAll("'", "\""),
        assetId,
        counterUserIid,
        amount,
        MixinUtil.encryptPayKey(PIN, PAY_KEY),
        UUID.randomUUID().toString());
    String token = MixinUtil.JWTTokenGen.genToken("POST", "/transfers", body,
                                                   pkey, appid, sessionid);
    String res = MixinHttpUtil.post(
      "https://api.mixin.one/transfers",
      makeHeaders(token),
      body
    );
  }

  private static String toBase64(String orig) {
    return new String(Base64.getEncoder().encode(orig.getBytes()));
  }
}
