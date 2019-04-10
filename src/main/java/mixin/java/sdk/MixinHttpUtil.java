package mixin.java.sdk;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.Gson;
import java.util.UUID;
import java.security.interfaces.RSAPrivateKey;

public class MixinHttpUtil {

  private static final OkHttpClient client = new OkHttpClient();

  private static HashMap<String, String> makeHeaders(String token) {
    HashMap<String, String> headers = new HashMap<String, String>();
    // headers.put("Mixin-Device-Id", Config.ADMIN_ID);
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + token);
    return headers;
  }

  public static String get(String url,
                          RSAPrivateKey pkey,
                          String appid,
                          String sessionid ) throws IOException {
    String token = MixinUtil.JWTTokenGen.genToken("GET", url, "",
                                                   pkey, appid, sessionid);
    Request request = new Request.Builder()
                                  .header("Authorization", "Bearer " + token)
                                  .url(url)
                                  .build();
    System.out.println("------------get-----------------");
    System.out.println(url);
    System.out.println(token);
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      throw new IOException("Unexpected code " + response);
    }
    /*
    Headers responseHeaders = response.headers();
    for (int i = 0; i < responseHeaders.size(); i++) {
      System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
    }
    */
    return response.body().string();
  }

  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  public static String post(
    String url, HashMap<String, String> headers, String body) throws IOException {
    Request.Builder builder = new Request.Builder().url(url).post(RequestBody.create(JSON, body));
    if (headers.size() > 0) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        builder.addHeader(entry.getKey(), entry.getValue());
      }
    }
    Request request = builder.build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      throw new IOException("Unexpected code " + response);
    }
    return response.body().string();
  }
  public static void transfer(
    String assetId,
    String opponentId,
    String amount,
    String encryptPIN,
    RSAPrivateKey pkey,
    String appid,
    String sessionid) throws IOException {
      JsonObject jsBody = new JsonObject();
      jsBody.addProperty("asset_id",assetId);
      jsBody.addProperty("opponent_id",opponentId);
      jsBody.addProperty("amount",amount);
      jsBody.addProperty("pin",encryptPIN);
      jsBody.addProperty("trace_id",UUID.randomUUID().toString());
      jsBody.addProperty("memo","hello");
      System.out.println(jsBody.toString());
      String token = MixinUtil.JWTTokenGen.genToken("POST", "/transfers", jsBody.toString(),
                                                     pkey, appid, sessionid);
      String res = post(
        "https://api.mixin.one/transfers",
        makeHeaders(token),
        jsBody.toString()
  );
  }
  public static String getAssets(
                            RSAPrivateKey pkey,
                            String appid,
                            String sessionid ) {
  try{
    String res = get(
      "https://api.mixin.one/assets",
      pkey, appid, sessionid
    );
    return res;
  } catch (IOException e){
			e.printStackTrace();
		}
    return null;
  }
}
