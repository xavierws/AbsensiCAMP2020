package com.example.falnerz.absensicamp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ShowActivity extends AppCompatActivity {

    private static ListView lview;
    private String eventnya;
    private static TextView tvcount;
    private AsyncTaskRetrieving proc;
    private static Toast toaster;
    private static Handler UIHandler;
    public static ListAdapter listAdapter;
    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        lview = findViewById(R.id.lviewPeserta);
        tvcount = findViewById(R.id.tvCount);
        TextView kegiatan = findViewById(R.id.tvKegiatan2);
        eventnya = MainActivity.eventnya;
        kegiatan.setText(MainActivity.eventTV);
        AlphaAnimation buttonClickAnim = new AlphaAnimation(1F, 0.5F);
        buttonClickAnim.setDuration(400);
        proc = new AsyncTaskRetrieving(this,eventnya);
        proc.execute();
        lview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toasting((String)lview.getItemAtPosition(position));
            }
        });
        EditText etext = findViewById(R.id.searchBar);

        etext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listAdapter.filtering(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    private void toasting(final String textnya){
        UIHandler.post(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                try {
                    Toast.makeText(getApplicationContext(),textnya, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error :"+e, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public static class AsyncTaskRetrieving extends AsyncTask<Void, Void, Void> {


        private boolean running,downloading;
        private String eventNow;
        private Context konteks;
        JSONArray dataJson;
        AsyncTaskRetrieving(Context inkonteks,String eventnya){
            eventNow = eventnya;
            dataJson = null;
            konteks = inkonteks;
            running=false;
            downloading=false;
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    toaster = Toast.makeText(konteks,"Please wait",Toast.LENGTH_SHORT);
                    toaster.show();
                }
            });
        }

        public boolean isRunning(){
            return running;
        }

        public boolean isDownloading(){return dataJson == null;}

        public void downloadData(){
            try {
                URL url = new URL("http://camp.jonathanrl.com/getAbsensi");
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                StringBuffer stringBuffer = null;
                stringBuffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
                dataJson = new JSONArray(stringBuffer.toString());
            }
            catch (Exception e){
                //do nothing
                System.out.println("download error : "+e);
            }
        }

        public JSONArray getDownloadedData(){
            return dataJson;
        }

        @SuppressLint("NewApi")
        @Override
        protected Void doInBackground(Void... params)
        {
            running = true;
            downloading = true;
            try {
                downloadData();
                while(isDownloading()){
                    //do nothing
                }
                List<Pair<String, String>> listData = new ArrayList<>();
                List<String> listNama = new ArrayList<>();
                int count = 0, total = 0;
                System.out.println("eventnya : "+eventNow);
                if (Objects.equals(eventNow, "bus_berangkat") ||
                        Objects.equals(eventNow, "bus_pulang")) {
                    String kehadiran = eventNow.substring(4);
                    int tempLen = dataJson.length();
                    String displayedName;
                    for (int i = 0; i < tempLen ; i++) {
                        try {
                            String noBus = dataJson.getJSONObject(i).getString(eventNow);
                            if (!Objects.equals(noBus, String.valueOf(MainActivity.busSelection))) {
                                continue;
                            }
                            total++;
                            String namanya=dataJson.getJSONObject(i).getString("nama");
                            if(namanya.length()>30) {
                                displayedName = namanya.substring(0,26) + ".....";
                            }
                            else{
                                displayedName=namanya;
                            }
                            listNama.add(namanya);
                            String stat = dataJson.getJSONObject(i).getString(kehadiran);
                            listData.add(new Pair(displayedName+"\n"+
                                    dataJson.getJSONObject(i).getString("nrp"),
                                    stat
                            ));
                            if (Objects.equals(stat, "1")) {
                                count++;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {//sisa event lain
                    if (Objects.equals(eventNow, "sesi")) {
                        eventNow = "sesi_" + MainActivity.sesiSelection;
                    }
                    total = dataJson.length();
                    String displayedName;
                    for (int i = 0; i < total; i++) {
                        try {
                            String stat = dataJson.getJSONObject(i).getString(eventNow);
                            String namanya=dataJson.getJSONObject(i).getString("nama");
                            if(namanya.length()>30) {
                                displayedName = namanya.substring(0,26) + ".....";
                            }
                            else{
                                displayedName=namanya;
                            }
                            listNama.add(namanya);
                            listData.add(new Pair(displayedName+"\n"+
                                    dataJson.getJSONObject(i).getString("nrp"),
                                    stat
                            ));
                            if (Objects.equals(stat, "1")) {
                                count++;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                initListView((ArrayList<Pair<String, String>>) listData, (ArrayList<String>) listNama,count, total);
            }
            catch (final Exception e){
                UIHandler.post(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        Toast.makeText(konteks, "Error:"+e, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;

        }

        private void initListView(final ArrayList<Pair<String, String>> listData,final ArrayList<String> listNama,
                                  final int count, final int total){
            System.out.println("masuk init");
            UIHandler.post(new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    try {
                        toaster.cancel();

                        listAdapter = new ListAdapter(konteks,listData,listNama);
                        lview.setAdapter(listAdapter);
                        tvcount.setText("Participant : " + count + " / " + total);
                        Toast.makeText(konteks, "Updated", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(konteks, "Error :"+e, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            running = false;
        }
    }

    public void refreshClicked(View view) {
        if(proc.isRunning()){return;}
        proc= new AsyncTaskRetrieving(this,eventnya);
        proc.execute();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ShowActivity.this,MainActivity.class));
        overridePendingTransition (R.transition.slide_down,R.transition.slide_up);
        finish();
        super.onBackPressed();
    }
}
