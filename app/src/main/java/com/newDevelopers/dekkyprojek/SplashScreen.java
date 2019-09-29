package com.newDevelopers.dekkyprojek;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.newDevelopers.dekkyprojek.mp3player.JcPlayer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class SplashScreen extends Activity {
    private ProgressBar progressLoading;
    private InterstitialAd intersialAds;
    private ImageView gambaricon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        MobileAds.initialize(this, "ca-app-pub-3024153912335658~1073940881");
        intersialAds = new InterstitialAd(SplashScreen.this);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("368D860BF3DB6AF49AE0A2AE0AE80C9B")
                .build();
        intersialAds.setAdUnitId(getString(R.string.intertialAds));
        // Load ads into Interstitial Adsa
        intersialAds.loadAd(adRequest);
        intersialAds.setAdListener(new AdListener() {
            public void onAdLoaded() {
            }
        });



        gambaricon = (ImageView) findViewById(R.id.gambariconbulet);
        progressLoading = (ProgressBar) findViewById(R.id.putar);

        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                doWork();
                startApp();
                finish();
            }
        }).start();
    }

    private void doWork() {
        for (int progress = 0; progress < 100; progress += 10) {
            try {
                Thread.sleep(1000);
                progressLoading.setProgress(progress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void startApp() {

        Intent intent = new Intent(SplashScreen.this, JcPlayer.class);
        startActivity(intent);
//        Load ad afters splash
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (intersialAds.isLoaded()) {
                    intersialAds.show();
                }
            }
        });
    }

}