package com.pablo.u5t9httpclient;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    //constantes
    private final static String URL_GEOGAMES = "http://api.geogames.org/wikipediaSearchJSON";
    private final static String USER_NAME = "pablodam22";
    private final static int ROWS = 10;
    private final static String ID_WHEATHER = "d0c16a584e6940a339ef995671406c0d";

    //atributos
    private EditText etPlaceName;
    private Button btSearch;
    private ListView lvSearchResult;
    private ArrayList<GeonamesPlace> listSearchResult;
    private ExecutorService executor;
    private ArrayList<WeatherPlace> listSearchWeatherResult;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUI();
    }

    private void setUI(){
        etPlaceName = findViewById(R.id.etPlaceName);
        btSearch = findViewById(R.id.btSearch);

        btSearch.setOnClickListener(this);

        listSearchResult = new ArrayList<>();
        listSearchWeatherResult = new ArrayList<>();

        lvSearchResult = findViewById(R.id.lvSearchResult);
        lvSearchResult.setOnItemClickListener(this);

        lvSearchResult.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,new ArrayList<>()));
        progressBar = findViewById(R.id.progressBar);
    }



    @Override
    public void onClick(View v) {
        if (isNetworkAvailable()){
            // check if user has written a place
            String place = etPlaceName.getText().toString();
            
            if (!place.isEmpty()){
                URL url;
                
                try {
                    Uri.Builder builder = new Uri.Builder();
                    
                    builder.scheme("http").authority("api.geonames.org").appendPath("wikipediaSearchJSON")
                            .appendQueryParameter("q",place)
                            .appendQueryParameter("lang","es")
                            .appendQueryParameter("maxRows",String.valueOf(ROWS))
                            .appendQueryParameter("username",USER_NAME);
                    url = new URL(builder.build().toString());
                    
                    startBackgroundTask(url);


                } catch (MalformedURLException e){
                    Log.i("URL",e.getMessage());
                }
            }else {
                Toast.makeText(this,"Write a place to search", Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(this,"Sorry, network is not available", Toast.LENGTH_LONG).show();
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etPlaceName.getWindowToken(), 0);
    }

    private void startBackgroundTask(final URL url) {
        final int CONNECTION_TIMEOUT = 10000;
        final int READ_TIMEOUT = 7000;

        progressBar.setVisibility(View.VISIBLE);

        executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;


                try {
                    urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
                    urlConnection.setReadTimeout(READ_TIMEOUT);

                    urlConnection.connect();
                    getData(urlConnection);
                } catch (IOException e) {
                    Log.i("URL",e.getMessage());
                } catch (JSONException e){
                    Log.i("JSONException",e.getMessage());
                } finally {
                    if (url != null ) urlConnection.disconnect();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listSearchResult.size() > 0){
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) lvSearchResult.getAdapter();
                            adapter.clear();
                            for(GeonamesPlace geonamesPlace: listSearchResult){
                                adapter.add(geonamesPlace.toString());
                            }
                            adapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        }else {
                            Toast.makeText(getApplicationContext(),"Not possible to contact" + URL_GEOGAMES, Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    private void startBackgroundTaskWeather(final URL url) {
        final int CONNECTION_TIMEOUT = 10000;
        final int READ_TIMEOUT = 7000;

        executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;

                try {
                    urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
                    urlConnection.setReadTimeout(READ_TIMEOUT);

                    urlConnection.connect();
                    getDataWeather(urlConnection);
                } catch (IOException e) {
                    Log.i("URL",e.getMessage());
                } catch (JSONException e){
                    Log.i("JSONException",e.getMessage());
                } finally {
                    if (url != null ) urlConnection.disconnect();
                }
            }
        });
    }

    private void getData(HttpURLConnection urlConnection) throws IOException, JSONException{

        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
            String resultStream = readStream(urlConnection.getInputStream());

            JSONObject json = new JSONObject(resultStream);
            JSONArray jArray = json.getJSONArray("geonames");

            if (jArray.length() > 0){
                for (int i = 0; i <jArray.length() ; i++) {
                    JSONObject item = jArray.getJSONObject(i);
                    String descripcion = item.getString("summary");
                    invalidateOptionsMenu();
                    int lat = item.getInt("lat");
                    int lng = item.getInt("lng");
                    GeonamesPlace geonamesPlace = new GeonamesPlace(descripcion,lat,lng);
                    listSearchResult.add(geonamesPlace);
                    buildWeatherPlace(geonamesPlace);
                }
            }
        }else{
            Log.i("JSONException","ErrorCode" + urlConnection.getResponseCode());
        }
    }

    private void getDataWeather(HttpURLConnection urlConnectionWeather) throws IOException, JSONException {
        if (urlConnectionWeather.getResponseCode() == HttpURLConnection.HTTP_OK){
            String resultStream = readStream(urlConnectionWeather.getInputStream());

            double lat = 0;
            double longi = 0;
            double temp = 0;
            double humedad = 0;
            String descripcion = null;

            JSONObject json = new JSONObject(resultStream);


            JSONObject cardinal = (JSONObject) json.get("coord");
            lat = cardinal.getDouble("lat");
            longi = cardinal.getDouble("lon");

            JSONArray wheather = json.getJSONArray("weather");
            descripcion = wheather.getJSONObject(0).getString("description");


            JSONObject main = json.getJSONObject("main");
            humedad = main.getDouble("humidity");
            temp = main.getDouble("temp");

            WeatherPlace weatherPlace = new WeatherPlace(lat,longi,humedad,temp,descripcion);
            listSearchWeatherResult.add(weatherPlace);
        }
    }

    private String readStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();

        BufferedReader reader = new BufferedReader( new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String nextLine = "";

        while ((nextLine = reader.readLine())!= null){
            sb.append(nextLine);
        }
        return sb.toString();
    }

    @SuppressWarnings("deprecation")
    private Boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) return false;

            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);

            return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));


        } else {
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
            return nwInfo != null && nwInfo.isConnected();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (executor!=null){
            executor.shutdownNow();
            Log.i("EXECUTOR","ALL TASKS CANCELLED !!!!!!!");
        }
    }

    private void buildWeatherPlace(GeonamesPlace geonamesPlace){
        if (isNetworkAvailable()){
            try {
                Uri.Builder builder = new Uri.Builder();

                builder.scheme("http").authority("api.openweathermap.org")
                        .appendPath("data").appendPath("2.5").appendPath("weather")
                        .appendQueryParameter("lat",String.valueOf(geonamesPlace.getLatitud()))
                        .appendQueryParameter("lon",String.valueOf(geonamesPlace.getLongitud()))
                        .appendQueryParameter("units","metric")
                        .appendQueryParameter("appid",ID_WHEATHER);
                URL url = new URL(builder.build().toString());
                startBackgroundTaskWeather(url);


            } catch (MalformedURLException e){
                Log.i("URL",e.getMessage());
            }

        }else {
            Toast.makeText(this,"Sorry, network is not available", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, listSearchWeatherResult.get(position).toString(),Toast.LENGTH_LONG).show();
    }
}