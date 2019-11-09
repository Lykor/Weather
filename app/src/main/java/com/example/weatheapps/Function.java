package com.example.weatheapps;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class Function {
  // private static final String OPEN_WEATHER_MAP_API = "2b1cdf491ccf9192213ece92d3da79c1";

  public interface AsyncResponse {

    void processFinish(String output1, String output2, String output3, String output4, String output5, String output6, String output8);
  }

  public static class placeIdTask extends AsyncTask<String, Void, JSONObject> {

    public AsyncResponse delegate = null;//Call back interface

    public placeIdTask(AsyncResponse asyncResponse) {
      delegate = asyncResponse;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
      JSONObject jsonWeather = null;
      try {
        jsonWeather = getWeatherJSON(params[0],params[1]);
      } catch (Exception e) {
        Log.d("Error", "Cannot process JSON results", e);
      }
      return jsonWeather;
    }

    @Override
    protected void onPostExecute(JSONObject json) {
      try {
        if(json != null){
          JSONObject details = json.getJSONArray("weather").getJSONObject(0);
          JSONObject main = json.getJSONObject("main");
          DateFormat df = DateFormat.getDateTimeInstance();

          String city = json.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country");
          String description = details.getString("description").toUpperCase(Locale.US);
          String temperature = String.format("%.2f", main.getDouble("temp"))+ "°";
          String humidity = main.getString("humidity") + "%";
          String pressure = main.getString("pressure") + " hPa";
          String updatedOn = df.format(new Date(json.getLong("dt")*1000));

          delegate.processFinish(city, description, temperature, humidity, pressure, updatedOn, ""+ (json.getJSONObject("sys").getLong("sunrise") * 1000));
        }
      } catch (JSONException e) {

      }
    }
  }

  public static JSONObject getWeatherJSON(String s,String met){
    try {

      URL url = new URL(String.format("http://api.openweathermap.org/data/2.5/weather?q="+s+"&mode=json&APPID=2b1cdf491ccf9192213ece92d3da79c1&units="+met));
      HttpURLConnection connection =
        (HttpURLConnection)url.openConnection();

     // connection.addRequestProperty("x-api-key", OPEN_WEATHER_MAP_API);

      BufferedReader reader = new BufferedReader(
        new InputStreamReader(connection.getInputStream()));

      StringBuffer json = new StringBuffer(1024);
      String tmp="";
      while((tmp=reader.readLine())!=null)
        json.append(tmp).append("\n");
      reader.close();

      JSONObject data = new JSONObject(json.toString());

      if(data.getInt("cod") != 200){
        return null;
      }

      return data;
    }catch(Exception e){
      return null;
    }
  }
}
