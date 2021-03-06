package com.example.m6frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class DisplayEventActivity extends AppCompatActivity {

    private final String TAG = "DisplayEventActivity";
    private RecyclerView recyclerView;
    private ArrayList<JSONObject> dataSet;
    private GoogleSignInAccount currentAccount;
    private boolean isLoading = false;
    private String activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_event);
        Intent intent = getIntent();
        activity = intent.getStringExtra("activity");

        recyclerView =  findViewById(R.id.recyclerView);
        currentAccount = GoogleSignIn.getLastSignedInAccount(this);


        dataSet = initEventData();

    }

    private void initAdapter() {

        EventRecyclerViewAdapter recyclerViewAdapter = new EventRecyclerViewAdapter(dataSet, this, activity);
        recyclerView.setAdapter(recyclerViewAdapter);

    }

    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager)
                        recyclerView.getLayoutManager();


                if (!isLoading && linearLayoutManager != null
                        && linearLayoutManager.findLastCompletelyVisibleItemPosition() == dataSet.size() - 1) {

                    isLoading = true;
                }

            }
        });
    }




    private ArrayList<JSONObject> initEventData() {
        final ArrayList<JSONObject> _dataSet = new ArrayList<>();

        RequestQueue requestQueue = Volley.newRequestQueue(DisplayEventActivity.this);


        if (activity.equals("myEvent")) {
            initMyEventData(_dataSet, requestQueue);

        } else {
            initEvents(_dataSet, requestQueue);

        }

        return _dataSet;

    }

    private void initEvents(ArrayList<JSONObject> _dataSet, RequestQueue requestQueue) {
        String url = "http://ec2-52-91-35-204.compute-1.amazonaws.com:8081/user/" + currentAccount.getEmail() + "/findevent/";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            JSONArray events = response.getJSONArray("events");

                            for (int i = 0; i < events.length(); i++) {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                Date startdate = format.parse(events.getJSONObject(i).getString("start").substring(0, 10) + " " + events.getJSONObject(i).getString("start").substring(11, 17));
                                Date enddate = format.parse(events.getJSONObject(i).getString("end").substring(0, 10) + " " + events.getJSONObject(i).getString("end").substring(11, 17));

                                String startstr = startdate.toString() + " - ";
                                String endstr = enddate.toString();

                                _dataSet.add(new JSONObject());
                                _dataSet.get(i).put("name", events.getJSONObject(i).getString("name"));
                                _dataSet.get(i).put("host", events.getJSONObject(i).getString("host"));
                                _dataSet.get(i).put("desc", events.getJSONObject(i).getString("desc"));
                                _dataSet.get(i).put("location", "Location: " + events.getJSONObject(i).getString("location"));
                                _dataSet.get(i).put("start", startstr);
                                _dataSet.get(i).put("end", endstr);
                                _dataSet.get(i).put("attendees", events.getJSONObject(i).getString("attendees"));
                                _dataSet.get(i).put("ownerPicture", currentAccount.getPhotoUrl());
                                Log.d(TAG, "event added");

                            }

                            initAdapter();
                            initScrollListener();
                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "error");
                    }
                });
        requestQueue.add(jsonObjectRequest);
        requestQueue.start();
    }

    private void initMyEventData(ArrayList<JSONObject> _dataSet, RequestQueue requestQueue) {
        String url = "http://ec2-52-91-35-204.compute-1.amazonaws.com:8081/user/" + currentAccount.getEmail() + "/event/";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Separated into the events you are hosting and events you are attending
                            //Put in the same data set for now
                            JSONArray hevents = response.getJSONArray("host");
                            JSONArray aevents = response.getJSONArray("attendee");

                            System.out.println(response.toString());

                            for (int i = 0; i < hevents.length(); i++) {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                Date startdate = format.parse(hevents.getJSONObject(i).getString("start").substring(0, 10) + " " + hevents.getJSONObject(i).getString("start").substring(11, 17));
                                Date enddate = format.parse(hevents.getJSONObject(i).getString("end").substring(0, 10) + " " + hevents.getJSONObject(i).getString("end").substring(11, 17));

                                String startstr = startdate.toString() + " - ";
                                String endstr = enddate.toString();

                                _dataSet.add(new JSONObject());
                                _dataSet.get(i).put("name", hevents.getJSONObject(i).getString("name"));
                                _dataSet.get(i).put("host", hevents.getJSONObject(i).getString("host"));
                                _dataSet.get(i).put("desc", hevents.getJSONObject(i).getString("desc"));
                                _dataSet.get(i).put("location", "Location: " + hevents.getJSONObject(i).getString("location"));
                                _dataSet.get(i).put("start", startstr);
                                _dataSet.get(i).put("end", endstr);
                                _dataSet.get(i).put("attendees", hevents.getJSONObject(i).getString("attendees"));
                                _dataSet.get(i).put("ownerPicture", currentAccount.getPhotoUrl());

                                Log.d(TAG, "event added");

                            }

                            for (int i = 0; i < aevents.length(); i++) {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                Date startdate = format.parse(aevents.getJSONObject(i).getString("start").substring(0, 10) + " " + aevents.getJSONObject(i).getString("start").substring(11, 17));
                                Date enddate = format.parse(aevents.getJSONObject(i).getString("end").substring(0, 10) + " " + aevents.getJSONObject(i).getString("end").substring(11, 17));

                                String startstr = startdate.toString() + " - ";
                                String endstr = enddate.toString();

                                _dataSet.add(new JSONObject());
                                _dataSet.get(i).put("name", aevents.getJSONObject(i).getString("name"));
                                _dataSet.get(i).put("host", aevents.getJSONObject(i).getString("host"));
                                _dataSet.get(i).put("desc", aevents.getJSONObject(i).getString("desc"));
                                _dataSet.get(i).put("location", "Location: " + aevents.getJSONObject(i).getString("location"));
                                _dataSet.get(i).put("start", startstr);
                                _dataSet.get(i).put("end", endstr);
                                _dataSet.get(i).put("attendees", aevents.getJSONObject(i).getString("attendees"));
                                _dataSet.get(i).put("ownerPicture", currentAccount.getPhotoUrl());
                                Log.d(TAG, "event added");

                            }
                            initAdapter();
                            initScrollListener();
                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "error");

                    }
                });
        requestQueue.add(jsonObjectRequest);
        requestQueue.start();

    }
}