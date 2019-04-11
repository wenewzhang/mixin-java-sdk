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
public class MixinAPI {
  private  final String CLIENT_ID;
  private  final String CLIENT_SECRET;
  private  final String PIN;
  private  final String SESSION_ID;
  private  final String PIN_TOKEN;
  private  final RSAPrivateKey  PrivateKey;
  private  final String encryptPIN;
  private  final byte[] PAY_KEY;

  public MixinAPI(String CLIENT_ID, String CLIENT_SECRET, String PIN,
                  String SESSION_ID, String PIN_TOKEN, RSAPrivateKey  PrivateKey) {
    this.CLIENT_ID       =  CLIENT_ID;
    this.CLIENT_SECRET   =  CLIENT_SECRET;
    this.PIN             =  PIN;
    this.SESSION_ID      =  SESSION_ID;
    this.PIN_TOKEN       =  PIN_TOKEN;
    this.PrivateKey      =  PrivateKey;
    this.PAY_KEY         =  MixinUtil.decrypt(this.PrivateKey, this.PIN_TOKEN, this.SESSION_ID);
    this.encryptPIN      =  MixinUtil.encryptPayKey(this.PIN,this.PAY_KEY);
  }
  public JsonObject getAssets() {
  try{
    String res = MixinHttpUtil.get(
      "/assets",
      this.PrivateKey, this.CLIENT_ID, this.SESSION_ID
    );
    JsonParser parser = new JsonParser();
    JsonElement jsonTree = parser.parse(res);
    return jsonTree.getAsJsonObject();
    // return res;
  } catch (IOException e){
      e.printStackTrace();
    }
    return null;
  }
  public String transfer(
    String assetId,
    String opponentId,
    String amount) throws IOException {
      JsonObject jsBody = new JsonObject();
      jsBody.addProperty("asset_id",assetId);
      jsBody.addProperty("opponent_id",opponentId);
      jsBody.addProperty("amount",amount);
      jsBody.addProperty("pin",this.encryptPIN);
      jsBody.addProperty("trace_id",UUID.randomUUID().toString());
      jsBody.addProperty("memo","hello");
      System.out.println(jsBody.toString());
      String token = MixinUtil.JWTTokenGen.genToken("POST", "/transfers", jsBody.toString(),
                                                     this.PrivateKey, this.CLIENT_ID, this.SESSION_ID);
      String res = MixinHttpUtil.post(
        MixinHttpUtil.baseUrl + "/transfers",
        MixinHttpUtil.makeHeaders(token),
        jsBody.toString()
  );
      return res;
  }
}
