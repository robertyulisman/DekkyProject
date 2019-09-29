package com.newDevelopers.dekkyprojek.mp3player;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.newDevelopers.dekkyprojek.WelcomeScreen;
import com.example.jean.jcplayer.JcPlayerManagerListener;
import com.example.jean.jcplayer.general.JcStatus;
import com.example.jean.jcplayer.general.errors.OnInvalidPathListener;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.newDevelopers.dekkyprojek.R;
import com.google.android.gms.ads.InterstitialAd;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JcPlayer extends AppCompatActivity
        implements OnInvalidPathListener, JcPlayerManagerListener, AudioAdapter.AdapterCallback {

    private static final String TAG = JcPlayer.class.getSimpleName();
    private JcPlayerView player;
    private RecyclerView recyclerView;
    private AudioAdapter audioAdapter;
    private InterstitialAd interstitialAd;
    private AdView adView;
    private ImageView btn_back, btn_search, btn_close;
    private LinearLayout search, tulbar;
    private EditText t_search;
    ArrayList<JcAudio> jcAudios = new ArrayList<>();
    String title, url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jc_player);

        prepareAd();

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Log.i("hello", "world");
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (interstitialAd.isLoaded()) {
                            interstitialAd.show();
                        } else {
                            Log.d("TAG"," Interstitial not loaded");
                        }

                        prepareAd();
                    }
                });
            }
        }, 1000, 1000, TimeUnit.SECONDS);




        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = findViewById(R.id.recyclerView);
        player = findViewById(R.id.jcplayer);

        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);



        getJcAudios();

        btn_back = findViewById(R.id.btn_back);
        btn_search = findViewById(R.id.btn_search);
        btn_close = findViewById(R.id.btn_close);
        search = findViewById(R.id.search);
        tulbar = findViewById(R.id.tulbar);
        t_search = findViewById(R.id.t_search);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(JcPlayer.this, WelcomeScreen.class));
            }
        });
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setVisibility(View.VISIBLE);
                tulbar.setVisibility(View.GONE);
                t_search.requestFocus();
            }
        });
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setVisibility(View.GONE);
                tulbar.setVisibility(View.VISIBLE);
            }
        });

        t_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
               // searchData(editable.toString());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        player.createNotification();
    }

    protected void adapterSetup() {
        audioAdapter = new AudioAdapter(JcPlayer.this,player.getMyPlaylist());
        audioAdapter.setOnItemClickListener(new AudioAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                player.playAudio(player.getMyPlaylist().get(position));
            }

            @Override
            public void onSongItemDeleteClicked(int position) {
                Toast.makeText(JcPlayer.this, "Delete song at position " + position,
                        Toast.LENGTH_SHORT).show();
//                if(player.getCurrentPlayedAudio() != null) {
//                    Toast.makeText(MainActivity.this, "Current audio = " + player.getCurrentPlayedAudio().getPath(),
//                            Toast.LENGTH_SHORT).show();
//                }
                removeItem(position);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(audioAdapter);

        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

    }

    @Override
    public void onPause() {
        super.onPause();
        player.createNotification();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.kill();
    }

    @Override
    public void onPathError(JcAudio jcAudio) {
        Toast.makeText(this, jcAudio.getPath() + " with problems", Toast.LENGTH_LONG).show();
//        player.removeAudio(jcAudio);
//        player.next();
    }


    @Override
    public void onPreparedAudio(JcStatus status) {

    }

    @Override
    public void onCompletedAudio() {

    }

    @Override
    public void onPaused(JcStatus status) {

    }

    @Override
    public void onContinueAudio(JcStatus status) {

    }

    @Override
    public void onPlaying(JcStatus status) {

    }

    @Override
    public void onTimeChanged(@NonNull JcStatus status) {
        updateProgress(status);
    }

    @Override
    public void onJcpError(@NonNull Throwable throwable) {
        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void updateProgress(final JcStatus jcStatus) {
        Log.d(TAG, "Song duration = " + jcStatus.getDuration()
                + "\n song position = " + jcStatus.getCurrentPosition());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // calculate progress
                float progress = (float) (jcStatus.getDuration() - jcStatus.getCurrentPosition())
                        / (float) jcStatus.getDuration();
                progress = 1.0f - progress;
                audioAdapter.updateProgress(jcStatus.getJcAudio(), progress);
            }
        });
    }

    private void removeItem(int position) {
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(true);

        //        jcAudios.remove(position);
        player.removeAudio(player.getMyPlaylist().get(position));
        audioAdapter.notifyItemRemoved(position);

        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    @Override
    public void onStopped(JcStatus status) {

    }

    @Override
    public void downloadSong(int position) {
        url = jcAudios.get(position).getPath();
        title = jcAudios.get(position).getTitle();
        new DownloadFile().execute(url);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class DownloadFile extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String fileName;
        private String folder;
        private boolean isDownloaded;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(JcPlayer.this);
            this.progressDialog.setMessage("Downloading - "+title);
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();


                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);


                //Extract file name from URL
                fileName = f_url[0].substring(f_url[0].lastIndexOf('/') + 1, f_url[0].length());
                String extension = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
                fileName = title+"."+extension;

                //External directory path to save file
                folder = Environment.getExternalStorageDirectory() + File.separator + "Soyluna/";

                //Create androiddeft folder if it does not exist
                File directory = new File(folder);

                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Output stream to write file
                OutputStream output = new FileOutputStream(folder + fileName);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    Log.d(TAG, "Progress: " + (int) ((total * 100) / lengthOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
                return "Download Success";

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return "Check Internet Connection";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }


        @Override
        protected void onPostExecute(String message) {
            // dismiss the dialog after the file was downloaded
            this.progressDialog.dismiss();

            // Display File path after downloading
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();
            if(message.equals("Download Success")){
                getJcAudios();
                audioAdapter.notifyDataSetChanged();
            }
        }
    }
    protected void getJcAudios(){
        if(jcAudios.size() != 0){
            jcAudios.clear();
        }

        jcAudios = new ArrayList<>();
        jcAudios.add(JcAudio.createFromURL("A Rodar Mi Vida", "https://ia601407.us.archive.org/32/items/SoluluGuaGua/Jim%20y%20Yam%20cantan%20A%20rodar%20mi%20vida%20%C2%A6%20Momento%20Musical%20%28con%20letra%29%20%C2%A6%20Soy%20Luna.mp3"));
        jcAudios.add(JcAudio.createFromURL("Alas", "https://ia801407.us.archive.org/32/items/SoluluGuaGua/Elenco%20de%20Soy%20Luna%20-%20Alas%20%28fin%20de%20temporada%29%20ft.%20Karol%20Sevilla%20%28Official%20Music%20Video%29.mp3"));

        for (int i = 0; i < jcAudios.size(); i++){
            String folder = Environment.getExternalStorageDirectory() + File.separator + "Soyluna/"+jcAudios.get(i).getTitle()+".mp3";
            File directory = new File(folder);

            if (directory.exists()) {
                jcAudios.set(i, JcAudio.createFromFilePath(jcAudios.get(i).getTitle(), folder));
            }
        }

        player.initPlaylist(jcAudios, this);
        adapterSetup();
        audioAdapter.notifyDataSetChanged();
    }



    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            super.onBackPressed();
        }
    }

    public void  prepareAd() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.intertialAds));
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

//


}