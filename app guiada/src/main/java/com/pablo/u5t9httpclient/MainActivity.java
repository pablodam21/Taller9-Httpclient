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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //constantes
    private final static String URL_GEOGAMES = "http://api.geogames.org/wikipediaSearchJSON";
    private final static String USER_NAME = "pablodam22";
    private final static int ROWS = 10;

    //atributos
    private EditText etPlaceName;
    private Button btSearch;
    private ListView lvSearchResult;
    private ArrayList<String> listSearchResult;
    private ExecutorService executor;


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

        lvSearchResult = findViewById(R.id.lvSearchResult);

        lvSearchResult.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,listSearchResult));
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
    }

    private void startBackgroundTask(final URL url) {
        final int CONNECTION_TIMEOUT = 10000;
        final int READ_TIMEOUT = 7000;

        executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                final ArrayList<String> searchResult = new ArrayList<>();

                try {
                    urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
                    urlConnection.setReadTimeout(READ_TIMEOUT);

                    urlConnection.connect();
                    getData(urlConnection, searchResult);
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
                        if (searchResult.size() > 0){
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) lvSearchResult.getAdapter();
                            adapter.clear();
                            adapter.addAll(searchResult);
                            adapter.notifyDataSetChanged();
                        }else {
                            Toast.makeText(getApplicationContext(),"Not possible to contact" + URL_GEOGAMES, Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        });
    }

    private void getData(HttpURLConnection urlConnection, ArrayList<String> searchResult) throws IOException,JSONException{
        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
            String resultStream = readStream(urlConnection.getInputStream());

            JSONObject json = new JSONObject(resultStream);
            JSONArray jArray = json.getJSONArray("geonames");

            if (jArray.length() > 0){
                for (int i = 0; i <jArray.length() ; i++) {
                    JSONObject item = jArray.getJSONObject(i);
                    searchResult.add(item.getString("summary"));
                }
            }else {
                searchResult.add("No Informacion found at geogames");
            }
        }else{
            Log.i("JSONException","ErrorCode" + urlConnection.getResponseCode());
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
}