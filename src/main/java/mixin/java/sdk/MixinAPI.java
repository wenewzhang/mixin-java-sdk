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
import com.google.gson.JsonArray;
import com.google.gson.Gson;

public class MixinAPI {
  private  final String CLIENT_ID;
  private  final String CLIENT_SECRET;
  private  final String PIN;
  private  final String SESSION_ID;
  private  final String PIN_TOKEN;
  private  final RSAPrivateKey  PrivateKey;
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
  }
  public String getClientID() {
    return this.CLIENT_ID;
  }
  public JsonArray getAssets() {
  try{
    String res = MixinHttpUtil.get(
      "/assets",
      this.PrivateKey, this.CLIENT_ID, this.SESSION_ID
    );
    JsonParser parser = new JsonParser();
    JsonElement jsonTree = parser.parse(res);
    return jsonTree.getAsJsonObject().get("data").getAsJsonArray();
    // return res;
  } catch (IOException e){
      e.printStackTrace();
    }
    return null;
  }
  public JsonObject getAsset(String asset_id) {
  try{
    String res = MixinHttpUtil.get(
      "/assets/" + asset_id,
      this.PrivateKey, this.CLIENT_ID, this.SESSION_ID
    );
    JsonParser parser = new JsonParser();
    JsonElement jsonTree = parser.parse(res);
    return jsonTree.getAsJsonObject().get("data").getAsJsonObject();
    // return res;
  } catch (IOException e) { e.printStackTrace(); }
    return null;
  }
  public JsonObject transfer(
    String assetId,
    String opponentId,
    String amount,
    String memo) {
      String encryptPIN      =  MixinUtil.encryptPayKey(this.PIN,this.PAY_KEY);
      JsonObject jsBody = new JsonObject();
      jsBody.addProperty("asset_id",assetId);
      jsBody.addProperty("opponent_id",opponentId);
      jsBody.addProperty("amount",amount);
      jsBody.addProperty("pin",encryptPIN);
      jsBody.addProperty("trace_id",UUID.randomUUID().toString());
      jsBody.addProperty("memo",memo);
      // System.out.println(jsBody.toString());
      String token = MixinUtil.JWTTokenGen.genToken("POST", "/transfers", jsBody.toString(),
                                                     this.PrivateKey, this.CLIENT_ID, this.SESSION_ID);
      try {
        String res = MixinHttpUtil.post(
          MixinHttpUtil.baseUrl + "/transfers",
          MixinHttpUtil.makeHeaders(token),
          jsBody.toString()
        );
        // System.out.println(res);
        return processJsonObjectWithDataOrError(res);
      } catch (IOException e) { e.printStackTrace(); }
      return null;
  }
  public JsonObject verifyPin(String PIN) {
    String encryptPIN      =  MixinUtil.encryptPayKey(PIN,this.PAY_KEY);
    JsonObject jsBody = new JsonObject();
    jsBody.addProperty("pin",encryptPIN);
    // System.out.println(jsBody.toString());
    String token = MixinUtil.JWTTokenGen.genToken("POST", "/pin/verify", jsBody.toString(),
                                                   this.PrivateKey, this.CLIENT_ID, this.SESSION_ID);
    try {
       String res = MixinHttpUtil.post(
         MixinHttpUtil.baseUrl + "/pin/verify",
         MixinHttpUtil.makeHeaders(token),
         jsBody.toString()
       );
       return processJsonObjectWithDataOrError(res);
     } catch (IOException e) { e.printStackTrace(); }
     return null;
  }
  public JsonObject updatePin(String OldPin, String PIN) {
    String encryptPIN      =  MixinUtil.encryptPayKey(PIN,this.PAY_KEY);
    String encryptOldPin   =  "";
    if ( OldPin.equals("") ) { encryptOldPin = ""; }
    else { encryptOldPin = MixinUtil.encryptPayKey(OldPin,this.PAY_KEY);}

    JsonObject jsBody = new JsonObject();
    jsBody.addProperty("old_pin",encryptOldPin);
    jsBody.addProperty("pin",encryptPIN);
    // System.out.println(jsBody.toString());
    String token = MixinUtil.JWTTokenGen.genToken("POST", "/pin/update", jsBody.toString(),
                                                   this.PrivateKey, this.CLIENT_ID, this.SESSION_ID);
    try {
       String res = MixinHttpUtil.post(
         MixinHttpUtil.baseUrl + "/pin/update",
         MixinHttpUtil.makeHeaders(token),
         jsBody.toString()
       );
       return processJsonObjectWithDataOrError(res);
     } catch (IOException e) { e.printStackTrace(); }
     return null;
  }
  public static JsonObject processJsonObjectWithDataOrError(String res) {
    JsonParser parser = new JsonParser();
    JsonElement jsonTree = parser.parse(res);
    if ( jsonTree.isJsonObject() ) {
      if ( jsonTree.getAsJsonObject().get("data") != null ) {
         return  jsonTree.getAsJsonObject().get("data").getAsJsonObject();
      } else if  ( jsonTree.getAsJsonObject().get("error") != null ) {
         return  jsonTree.getAsJsonObject().get("error").getAsJsonObject();
      }
    }
    return null;
  }
  public JsonObject createUser(String fullName, String SessionSecret) {
    JsonObject jsBody = new JsonObject();
    jsBody.addProperty("full_name",fullName);
    jsBody.addProperty("session_secret",SessionSecret);
    String token = MixinUtil.JWTTokenGen.genToken("POST", "/users", jsBody.toString(),
                                                   this.PrivateKey, this.CLIENT_ID, this.SESSION_ID);
    try {
      String res = MixinHttpUtil.post(
        MixinHttpUtil.baseUrl + "/users",
        MixinHttpUtil.makeHeaders(token),
        jsBody.toString()
      );
      // System.out.println(res);
      return processJsonObjectWithDataOrError(res);
    } catch (IOException e) { e.printStackTrace(); }
    return null;
  }
  public JsonObject createWithdrawAddress(String assetID, String publicKey,
                                          String AccountName, String AccountTag,
                                          String Pin, String label) {
    String encryptPIN      =  MixinUtil.encryptPayKey(Pin,this.PAY_KEY);
    JsonObject jsBody = new JsonObject();
    if ( publicKey.equals("") ) { //for EOS
      jsBody.addProperty("asset_id",assetID);
      jsBody.addProperty("account_name",AccountName);
      jsBody.addProperty("account_tag",AccountTag);
      jsBody.addProperty("label",label);
      jsBody.addProperty("pin",encryptPIN);
    } else {
      jsBody.addProperty("asset_id",assetID);
      jsBody.addProperty("public_key",publicKey);
      jsBody.addProperty("label",label);
      jsBody.addProperty("pin",encryptPIN);
    }
    String token = MixinUtil.JWTTokenGen.genToken("POST", "/addresses", jsBody.toString(),
                                                   this.PrivateKey, this.CLIENT_ID, this.SESSION_ID);
    try {
      String res = MixinHttpUtil.post(
        MixinHttpUtil.baseUrl + "/addresses",
        MixinHttpUtil.makeHeaders(token),
        jsBody.toString()
      );
      // System.out.println(res);
      return processJsonObjectWithDataOrError(res);
    } catch (IOException e) { e.printStackTrace(); }
    return null;
  }
  public JsonObject getAddress(String address) {
  try{
    String res = MixinHttpUtil.get(
      "/addresses/" + address,
      this.PrivateKey, this.CLIENT_ID, this.SESSION_ID
    );
    System.out.println(res);
    return processJsonObjectWithDataOrError(res);
  } catch (IOException e) { e.printStackTrace(); }
    return null;
  }
  public JsonObject delAddress(String address, String PIN) {
    String encryptPIN      =  MixinUtil.encryptPayKey(PIN,this.PAY_KEY);
    JsonObject jsBody = new JsonObject();
    jsBody.addProperty("pin",encryptPIN);
    // System.out.println(jsBody.toString());
    String Url = "/addresses/" + address + "/delete";
    String token = MixinUtil.JWTTokenGen.genToken("POST", Url, jsBody.toString(),
                                                   this.PrivateKey, this.CLIENT_ID, this.SESSION_ID);
    try {
       String res = MixinHttpUtil.post(
         MixinHttpUtil.baseUrl + Url,
         MixinHttpUtil.makeHeaders(token),
         jsBody.toString()
       );
       System.out.println(res);
       return processJsonObjectWithDataOrError(res);
     } catch (IOException e) { e.printStackTrace(); }
     return null;
   }
   public JsonObject withdrawals(String addressID, String amount,
                                 String trace_id,
                                 String Pin, String memo) {
     String encryptPIN      =  MixinUtil.encryptPayKey(Pin,this.PAY_KEY);
     if ( trace_id.equals("") ) { trace_id = UUID.randomUUID().toString();}
     JsonObject jsBody = new JsonObject();
     jsBody.addProperty("address_id",addressID);
     jsBody.addProperty("amount",amount);
     jsBody.addProperty("trace_id",trace_id);
     jsBody.addProperty("memo",memo);
     jsBody.addProperty("pin",encryptPIN);
     String token = MixinUtil.JWTTokenGen.genToken("POST", "/withdrawals", jsBody.toString(),
                                                    this.PrivateKey, this.CLIENT_ID, this.SESSION_ID);
     try {
       String res = MixinHttpUtil.post(
         MixinHttpUtil.baseUrl + "/withdrawals",
         MixinHttpUtil.makeHeaders(token),
         jsBody.toString()
       );
       // System.out.println(res);
       return processJsonObjectWithDataOrError(res);
     } catch (IOException e) { e.printStackTrace(); }
     return null;
   }
}
