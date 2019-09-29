package com.newDevelopers.dekkyprojek;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.newDevelopers.dekkyprojek.mp3player.JcPlayer;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import android.widget.TextView;


public class WelcomeScreen extends AppCompatActivity {

    private AdView iklanAds;
    private Button tombol;
    private TextView text;
    Dialog myDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);

        SharedPreferences sharedPreferences = getSharedPreferences("name", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("count",0);
        editor.putBoolean("later", false);
        editor.apply();

        myDialog = new Dialog(this);


        text=(TextView)findViewById(R.id.textsss);
        Typeface customfont=Typeface.createFromAsset(getAssets(),"font/contm.ttf");
        text.setTypeface(customfont);

        MobileAds.initialize(this, "ca-app-pub-3939903388953034~9374436762");
        iklanAds = (AdView) findViewById(R.id.kodeiklan);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        iklanAds.loadAd(adRequest);
        tombol = (Button) findViewById(R.id.tombolwelcome);
        tombol.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent songs = new Intent(WelcomeScreen.this, JcPlayer.class);
                startActivity(songs);
            }
        });
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(WelcomeScreen.this);
        alert.setTitle("Thanks for Using Our App, Please Rate Us :");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id="
                                    + getPackageName())));
                }
                dialog.dismiss();
                finish();
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        alert.create();
        alert.show();
    }
}
