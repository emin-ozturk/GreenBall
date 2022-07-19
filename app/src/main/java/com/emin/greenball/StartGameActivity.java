package com.emin.greenball;

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
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.List;

public class StartGameActivity extends AppCompatActivity {
    ImageView imgVibration;
    TextView txtLanguage, txtScoreText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_game);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        TextView txtScore = findViewById(R.id.txtScore);
        txtScoreText = findViewById(R.id.txtScoreText);
        txtLanguage = findViewById(R.id.txtLanguage);
        imgVibration = findViewById(R.id.imgVibration);

        Database db = new Database(this);
        if (db.list().size() == 0) {
            db.createTable();
        }

        txtScore.setText(String.valueOf(db.getBestScore()));
        txtLanguage.setText(db.getLanguage() == 0 ? "TR" : "EN");
        txtScoreText.setText(db.getLanguage() == 0 ? "SKOR" : "SCORE");
        if (db.getVibration() == 0) {
            imgVibration.setImageDrawable(getResources().getDrawable(R.drawable.ic_vibration_off));
        } else {
            imgVibration.setImageDrawable(getResources().getDrawable(R.drawable.ic_vibration));
        }
    }

    public void startGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
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

    public void language(View view) {
        Database db = new Database(this);
        if (db.getLanguage() == 0) {
            db.updateLanguage(1);
            txtLanguage.setText("EN");
            txtScoreText.setText("SCORE");
        } else {
            db.updateLanguage(0);
            txtLanguage.setText("TR");
            txtScoreText.setText("SKOR");
        }
        Log.d("tag", "language:" + db.getLanguage());
    }
}