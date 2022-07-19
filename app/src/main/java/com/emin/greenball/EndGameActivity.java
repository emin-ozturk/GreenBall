package com.emin.greenball;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class EndGameActivity extends AppCompatActivity {
    ImageView imgVibration;
    private InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_end_game);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        loadAd();
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        int score = getIntent().getIntExtra("score", 0);
        TextView txtScore = findViewById(R.id.txtScore);
        TextView txtNewRecord = findViewById(R.id.txtNewRecord);
        imgVibration = findViewById(R.id.imgVibration);
        txtScore.setText(String.valueOf(score));
        Database db = new Database(this);
        txtNewRecord.setText(db.getLanguage() == 0 ? "SKOR" : "SCORE");
        int bestScore = db.getBestScore();
        if (score > bestScore) {
            db.updateBestScore(score);
            txtNewRecord.setText(db.getLanguage() == 0 ? "YENİ REKOR" : "BEST SCORE");
        }

        if (db.getVibration() == 0) {
            imgVibration.setImageDrawable(getResources().getDrawable(R.drawable.ic_vibration_off));
        } else {
            imgVibration.setImageDrawable(getResources().getDrawable(R.drawable.ic_vibration));
        }

    }

    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getResources().getString(R.string.interstitial_new_game), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.i("greenball", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.i("greenball", loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }

    private void showAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
        } else {
            Log.d("greenball", "The interstitial ad wasn't ready yet.");
        }
    }

    public void newGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
        showAd();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, StartGameActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
        showAd();
    }

    public void vibration(View view) {
        Database db = new Database(this);
        if (db.getVibration() == 0) {
            db.updateVibration(1);
            imgVibration.setImageDrawable(getResources().getDrawable(R.drawable.ic_vibration));
        } else {
            db.updateVibration(0);
            imgVibration.setImageDrawable(getResources().getDrawable(R.drawable.ic_vibration_off));
        }
    }
}