package com.emin.greenball;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {
    private Thread thread;
    Activity activity;
    Canvas canvas;
    Paint paint;
    Paint selectedPaint; //Seçilen hedefin rengini belirler
    int screenX, screenY;
    int bulletRadius, targetRadius; //Mermi ve hedefin ekranın ortasına olan mesafesi
    int score;
    int selectedTarget = -1; //Seçilen hedefin dizideki sırasını tutar
    int time; //Level'ler arasında geçen süre
    int targetRotateCount; //Seçilen hedefin dönüş sayısı
    float bulletSize, targetSize; //Mermi ve hedefin çapları
    float bulletDegree, targetDegree; //Mermi ve hedefin dönüş dereceleri
    float bulletX, bulletY; //Mermi ve hedefin x ve y koordinatları
    float centerCircleX, centerCircleY; //Merkez yuvarlağın x ve y koordinatları
    float targetSpend; //Hedefin dönüş hızları
    float lineStartX, lineStartY, lineStopX, lineStopY;
    float lineStartRadius, lineStopRadius;
    float centerCircleOut, centerCircleIn; //Merkez yuvarlağın ölçüleri
    boolean isPlaying; //Oyun ekranında olup olmadığını kontrol eder
    boolean isShot; //Hedefin vurulup vurulmadığını kontrol eder
    boolean isFire; //Merminin ateşlemip ateşlenmediğini kontrol eder
    boolean isWritingScore; //Mermi hedefe çarptığında skorun birden fazla artmasını önler
    boolean isRotateBulletRight; //Merminin sağa mı sola mı döneceğini kontrol eder, beşlarken sağa dönecek
    boolean isRotateTargetRight; //Hedefin sağa mı sola mı döneceğini kontrol eder, beşlarken sağa dönecek
    Random random;
    Float[][] targetPosition = new Float[8][2]; //Tüm hedeflerin koordinatlarını tutar
    Typeface font;
    Vibrator vibrator;
    Database db;
    //Hedef yuvarlağının çevresinin koordinatları
    List<Float> targetBottomRightX = new ArrayList<>();
    List<Float> targetBottomRightY = new ArrayList<>();
    List<Float> targetBottomLeftX = new ArrayList<>();
    List<Float> targetBottomLeftY = new ArrayList<>();
    List<Float> targetTopLeftX = new ArrayList<>();
    List<Float> targetTopLeftY = new ArrayList<>();
    List<Float> targetTopRightX = new ArrayList<>();
    List<Float> targetTopRightY = new ArrayList<>();

    //Mermi yuvarlağının çevresinin koordinatları
    List<Float> bulletBottomRightX = new ArrayList<>();
    List<Float> bulletBottomRightY = new ArrayList<>();
    List<Float> bulletBottomLeftX = new ArrayList<>();
    List<Float> bulletBottomLeftY = new ArrayList<>();
    List<Float> bulletTopLeftX = new ArrayList<>();
    List<Float> bulletTopLeftY = new ArrayList<>();
    List<Float> bulletTopRightX = new ArrayList<>();
    List<Float> bulletTopRightY = new ArrayList<>();
    private RewardedAd mRewardedAd;

    int GAMEMODE;
    final int MODEGAMEBEFORE = 1, MODEGAME = 2, MODEGAMEAFTER=3, MODEAD = 4;
    float continueTime;
    int life;

    public GameView(Context context, Activity activity, int screenX, int screenY) {
        super(context);
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(context, getResources().getString(R.string.reward),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d("greenball", loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d("greenball", "Ad was loaded.");
                    }
                });

        this.activity = activity;
        this.screenX = screenX;
        this.screenY = screenY;
        init();
    }

    private void init() {
        GAMEMODE = MODEGAMEBEFORE;
        continueTime = 0;
        paint = new Paint();
        selectedPaint = new Paint();
        selectedPaint.setColor(Color.argb(255, 0, 200, 83));
        centerCircleX = screenX / 2f;
        centerCircleY = screenY / 2f;
        bulletX = (float) (centerCircleX + Math.cos(Math.toRadians(-90)));
        bulletY = (float) (centerCircleY + Math.sin(Math.toRadians(-90)));
        targetRadius = (int) Math.min(((screenX / 2f) - (screenX * 0.12f)), 500);
        bulletSize = targetRadius * 0.044f;
        targetSize = targetRadius * 0.11f;
        centerCircleOut = targetRadius * 0.1f;
        centerCircleIn = targetRadius * 0.025f;
        lineStartRadius = centerCircleOut + 15f;
        lineStopRadius = lineStartRadius + targetRadius * 0.1098f;
        lineStartX = (float) (centerCircleX + Math.cos(Math.toRadians(-90)) * lineStartRadius);
        lineStartY = (float) (centerCircleY + Math.sin(Math.toRadians(-90)) * lineStartRadius);
        lineStopX = (float) (centerCircleX + Math.cos(Math.toRadians(-90)) * lineStopRadius);
        lineStopY = (float) (centerCircleY + Math.sin(Math.toRadians(-90)) * lineStopRadius);
        bulletRadius = 0;
        targetDegree = -90f;
        isPlaying = true;
        random = new Random();
        font = Typeface.createFromAsset(getContext().getAssets(), "teko_regular.ttf");
        vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        db = new Database(activity);

        targetPosition[0][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree)) * targetRadius); //1. X
        targetPosition[0][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree)) * targetRadius); //1. Y

        targetPosition[1][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 45f)) * targetRadius); //2. X
        targetPosition[1][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 45f)) * targetRadius); //2. Y

        targetPosition[2][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 90f)) * targetRadius); //3. X
        targetPosition[2][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 90f)) * targetRadius); //3. Y

        targetPosition[3][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 135f)) * targetRadius); //4. X
        targetPosition[3][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 135f)) * targetRadius); //4. Y

        targetPosition[4][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 180f)) * targetRadius); //5. X
        targetPosition[4][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 180f)) * targetRadius); //5. Y

        targetPosition[5][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 225f)) * targetRadius); //6. X
        targetPosition[5][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 225f)) * targetRadius); //6. Y

        targetPosition[6][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 270f)) * targetRadius); //7. X
        targetPosition[6][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 270f)) * targetRadius); //7. Y

        targetPosition[7][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 315f)) * targetRadius); //8. X
        targetPosition[7][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 315f)) * targetRadius); //8. Y
    }

    private void newGame() {
        bulletRadius = 0;
        bulletDegree = -90;
        targetDegree = -90f;
        score = 0;
        time = 0;
        targetSpend = 0f;
        targetRotateCount = 0;
        life = 1;
        isFire = false;
        isShot = false;
        isWritingScore = false;
        isRotateBulletRight = true;
        isRotateTargetRight = true;
        selectedTarget = newTarget();
        GAMEMODE = MODEGAME;
    }

    private void continueGame() {
        bulletRadius = 0;
        bulletDegree = -90;
        targetDegree = -90f;
        time = 0;
        targetSpend = 0f;
        targetRotateCount = 0;
        isFire = false;
        isShot = false;
        isWritingScore = false;
        isRotateBulletRight = true;
        isRotateTargetRight = true;
        selectedTarget = newTarget();
    }

    @Override
    public void run() {
        while (isPlaying) {
            draw();
            update();
            sleep();
        }
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            canvas = getHolder().lockCanvas();
            canvas.drawColor(Color.argb(255, 15, 23, 35));

            //Skor
            paint.setColor(Color.argb(255, 78, 86, 98));
            paint.setTextSize(targetRadius * 1.5f);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(font);
            canvas.drawText(String.valueOf(score), screenX / 2f, ((screenY / 2f) - ((paint.descent() + paint.ascent()) / 2)) + targetRadius * 0.122f, paint);

            //Hedefler
            paint.setColor(Color.argb(255, 78, 86, 98));
            canvas.drawCircle(targetPosition[0][0], targetPosition[0][1], targetSize, selectedTarget == 0 ? selectedPaint : paint);
            canvas.drawCircle(targetPosition[1][0], targetPosition[1][1], targetSize, selectedTarget == 1 ? selectedPaint : paint);
            canvas.drawCircle(targetPosition[2][0], targetPosition[2][1], targetSize, selectedTarget == 2 ? selectedPaint : paint);
            canvas.drawCircle(targetPosition[3][0], targetPosition[3][1], targetSize, selectedTarget == 3 ? selectedPaint : paint);
            canvas.drawCircle(targetPosition[4][0], targetPosition[4][1], targetSize, selectedTarget == 4 ? selectedPaint : paint);
            canvas.drawCircle(targetPosition[5][0], targetPosition[5][1], targetSize, selectedTarget == 5 ? selectedPaint : paint);
            canvas.drawCircle(targetPosition[6][0], targetPosition[6][1], targetSize, selectedTarget == 6 ? selectedPaint : paint);
            canvas.drawCircle(targetPosition[7][0], targetPosition[7][1], targetSize, selectedTarget == 7 ? selectedPaint : paint);

            //Mermi
            paint.setColor(Color.WHITE);
            canvas.drawCircle(bulletX, bulletY, bulletSize, paint);

            //Merkez yuvarlak
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(5);
            canvas.drawLine(lineStartX, lineStartY, lineStopX, lineStopY, paint);
            canvas.drawCircle(centerCircleX, centerCircleY, centerCircleOut, paint);
            paint.setColor(Color.argb(255, 15, 23, 35));
            canvas.drawCircle(centerCircleX, centerCircleY, centerCircleIn, paint);


            if (GAMEMODE == MODEGAMEBEFORE) {
                //Yeni oyun
                paint.setColor(Color.WHITE);
                paint.setTextSize(targetRadius* 0.17f);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTypeface(font);
                String text = db.getLanguage() == 0 ? "Oynamak için dokun" : "Tap to play";
                canvas.drawText(text, screenX / 2f, screenY / 10f * 9f, paint);
            }

            if (GAMEMODE == MODEGAMEAFTER || GAMEMODE == MODEAD) {
                paint.setColor(Color.argb(80, 0, 0, 0));
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

                paint.setColor(Color.WHITE);
                paint.setTextSize(targetRadius* 0.5f);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTypeface(font);
                String t1 = db.getLanguage() == 0 ? "Kaybettin" : "Game Over";
                canvas.drawText(t1, screenX / 2f, screenY / 10f * 2f, paint);

                paint.setTextSize(targetRadius* 0.6f);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTypeface(font);
                int time = (int) Math.ceil(5f - continueTime);
                String t2 = String.valueOf(time);
                canvas.drawText(t2, screenX / 2f, screenY / 10f * 5f, paint);

                paint.setTextSize(targetRadius* 0.2f);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTypeface(font);
                String t3 = db.getLanguage() == 0 ? "Devam etmek için tıkla" : "Tap to continue";
                canvas.drawText(t3, screenX / 2f, screenY / 10f * 7f, paint);
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void update() {
        if (GAMEMODE == MODEGAMEAFTER) {
            continueTime += 0.06;
            if (continueTime > 6) {
                isPlaying = false;
                Intent intent = new Intent(getContext(), EndGameActivity.class);
                intent.putExtra("score", score);
                getContext().startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                activity.finish();
            }
            return;
        }

        if (GAMEMODE == MODEGAMEBEFORE) {
            return;
        }
        if (!isFire) {
            if (isRotateBulletRight) {
                bulletDegree += 2.5f;
            } else {
                bulletDegree -= 2.5f;
            }
        } else {
            if (!isShot) {
                bulletRadius += 50;
            } else {
                bulletRadius -= 50;
                if (bulletRadius == 0) {
                    isFire = false;
                    isShot = false;
                    isWritingScore = false;
                }
            }
        }

        if (score > 3) {
            time++;
        }

        bulletX = (float) (centerCircleX + Math.cos(Math.toRadians(bulletDegree)) * bulletRadius);
        bulletY = (float) (centerCircleY + Math.sin(Math.toRadians(bulletDegree)) * bulletRadius);

        lineStartX = (float) (centerCircleX + Math.cos(Math.toRadians(bulletDegree)) * lineStartRadius);
        lineStartY = (float) (centerCircleY + Math.sin(Math.toRadians(bulletDegree)) * lineStartRadius);
        lineStopX = (float) (centerCircleX + Math.cos(Math.toRadians(bulletDegree)) * lineStopRadius);
        lineStopY = (float) (centerCircleY + Math.sin(Math.toRadians(bulletDegree)) * lineStopRadius);

        targetPosition[0][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree)) * targetRadius); //1. X
        targetPosition[0][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree)) * targetRadius); //1. Y

        targetPosition[1][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 45f)) * targetRadius); //2. X
        targetPosition[1][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 45f)) * targetRadius); //2. Y

        targetPosition[2][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 90f)) * targetRadius); //3. X
        targetPosition[2][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 90f)) * targetRadius); //3. Y

        targetPosition[3][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 135f)) * targetRadius); //4. X
        targetPosition[3][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 135f)) * targetRadius); //4. Y

        targetPosition[4][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 180f)) * targetRadius); //5. X
        targetPosition[4][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 180f)) * targetRadius); //5. Y

        targetPosition[5][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 225f)) * targetRadius); //6. X
        targetPosition[5][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 225f)) * targetRadius); //6. Y

        targetPosition[6][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 270f)) * targetRadius); //7. X
        targetPosition[6][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 270f)) * targetRadius); //7. Y

        targetPosition[7][0] = (float) (centerCircleX + Math.cos(Math.toRadians(targetDegree + 315f)) * targetRadius); //8. X
        targetPosition[7][1] = (float) (centerCircleY + Math.sin(Math.toRadians(targetDegree + 315f)) * targetRadius); //8. Y

        level();
        isShot();
        isGameOver();

    }

    private int newTarget() {
        int target;
        do {
            target = random.nextInt(8);
        } while (target == selectedTarget);
        return target;
    }

    private void level() {
        if (score > 3 && score <= 7) {
            if (time <= 30) {
                targetSpend += 0.075f;
            } else if (time <= 60) {
                targetSpend -= 0.075f;
            }
        } else if (score > 7 && score <= 12) {
            if (time <= 40) {
                targetSpend += 0.1f;
            } else if (time <= 80) {
                targetSpend -= 0.1f;
            }
        } else if (score > 12 && score <= 18) {
            if (isRotateTargetRight) {
                if (time <= 40) {
                    targetSpend += 0.1f;
                } else if (time <= 80) {
                    targetSpend -= 0.1f;
                }
            } else {
                if (time <= 40) {
                    targetSpend -= 0.1f;
                } else if (time <= 80) {
                    targetSpend += 0.1f;
                }
            }
        } else if (score > 18 && score <= 25) {
            if (isRotateTargetRight) {
                if (time <= 40) {
                    targetSpend += 0.15f;
                } else if (time <= 80) {
                    targetSpend -= 0.15f;
                }
            } else {
                if (time <= 40) {
                    targetSpend -= 0.15f;
                } else if (time <= 80) {
                    targetSpend += 0.15f;
                }
            }
        } else if (score > 25) {
            if (isRotateTargetRight) {
                if (time <= 40) {
                    targetSpend += 0.2f;
                } else if (time <= 80) {
                    targetSpend -= 0.2f;
                }
            } else {
                if (time <= 40) {
                    targetSpend -= 0.2f;
                } else if (time <= 80) {
                    targetSpend += 0.2f;
                }
            }
        }
        if (time >= 150) {
            time = 0;
            targetSpend = 0;
            targetRotateCount++;
            if (targetRotateCount % 2 == 0) {
                isRotateTargetRight = !isRotateTargetRight;
                targetRotateCount = 0;
            }
        }
        targetDegree += targetSpend;
    }

    private void isShot() {
        targetBottomRightX.clear();
        targetBottomRightY.clear();
        targetBottomLeftX.clear();
        targetBottomLeftY.clear();
        targetTopLeftX.clear();
        targetTopLeftY.clear();
        targetTopRightX.clear();
        targetTopRightY.clear();
        bulletBottomRightX.clear();
        bulletBottomRightY.clear();
        bulletBottomLeftX.clear();
        bulletBottomLeftY.clear();
        bulletTopLeftX.clear();
        bulletTopLeftY.clear();
        bulletTopRightX.clear();
        bulletTopRightY.clear();

        for (int i = 0; i <= 90; i++) {
            //Hedefin sağ alt koordinatları
            targetBottomRightX.add((float) (targetPosition[selectedTarget][0] + Math.cos(Math.toRadians(i)) * targetSize));
            targetBottomRightY.add((float) (targetPosition[selectedTarget][1] + Math.sin(Math.toRadians(i)) * targetSize));

            //Merminin sağ alt koordinatları
            bulletBottomRightX.add((float) (bulletX + Math.cos(Math.toRadians(i)) * bulletSize));
            bulletBottomRightY.add((float) (bulletY + Math.sin(Math.toRadians(i)) * bulletSize));
        }

        for (int i = 90; i <= 180; i++) {
            //Hedefin sol alt koordinatları
            targetBottomLeftX.add((float) (targetPosition[selectedTarget][0] + Math.cos(Math.toRadians(i)) * targetSize));
            targetBottomLeftY.add((float) (targetPosition[selectedTarget][1] + Math.sin(Math.toRadians(i)) * targetSize));

            //Meminin sol alt koordinatları
            bulletBottomLeftX.add((float) (bulletX + Math.cos(Math.toRadians(i)) * bulletSize));
            bulletBottomLeftY.add((float) (bulletY + Math.sin(Math.toRadians(i)) * bulletSize));
        }

        for (int i = 180; i <= 270; i++) {
            //Hedefin sol üst koordinatları
            targetTopLeftX.add((float) (targetPosition[selectedTarget][0] + Math.cos(Math.toRadians(i)) * targetSize));
            targetTopLeftY.add((float) (targetPosition[selectedTarget][1] + Math.sin(Math.toRadians(i)) * targetSize));

            //Merminin sol üst koordinatları
            bulletTopLeftX.add((float) (bulletX + Math.cos(Math.toRadians(i)) * bulletSize));
            bulletTopLeftY.add((float) (bulletY + Math.sin(Math.toRadians(i)) * bulletSize));
        }

        for (int i = 270; i <= 360; i++) {
            //Hedefin sağ üst koordinatları
            targetTopRightX.add((float) (targetPosition[selectedTarget][0] + Math.cos(Math.toRadians(i)) * targetSize));
            targetTopRightY.add((float) (targetPosition[selectedTarget][1] + Math.sin(Math.toRadians(i)) * targetSize));

            //Merminin sağ üst koordinatları
            bulletTopRightX.add((float) (bulletX + Math.cos(Math.toRadians(i)) * bulletSize));
            bulletTopRightY.add((float) (bulletY + Math.sin(Math.toRadians(i)) * bulletSize));
        }

        //Merminin sağ alt kısmı hedefin sol üst kısmına çarptı mı?
        for (int i = 0; i <= 90; i++) {
            for (int j = 0; j <= 90; j++) {
                if (bulletBottomRightX.get(i) >= targetTopLeftX.get(j) && bulletBottomRightX.get(i) <= targetPosition[selectedTarget][0] &&
                        bulletBottomRightY.get(i) >= targetTopLeftY.get(j) && bulletBottomRightY.get(i) <= targetPosition[selectedTarget][1]) {
                    if (!isWritingScore) {
                        score++;
                        isWritingScore = true;
                        isRotateBulletRight = !isRotateBulletRight;
                        selectedTarget = newTarget();
                        isShot = true;
                        if (db.getVibration() == 1) {
                            vibrator.vibrate(50);
                        }
                    }
                }
            }
        }

        //Merminin sol alt kısmı hedefin sağ üst kısmına çarptı mı?
        for (int i = 0; i <= 90; i++) {
            for (int j = 0; j <= 90; j++) {
                if (bulletBottomLeftX.get(i) <= targetTopRightX.get(j) && bulletBottomLeftX.get(i) >= targetPosition[selectedTarget][0] &&
                        bulletBottomLeftY.get(i) >= targetTopRightY.get(j) && bulletBottomLeftY.get(i) <= targetPosition[selectedTarget][1]) {
                    if (!isWritingScore) {
                        score++;
                        isWritingScore = true;
                        isRotateBulletRight = !isRotateBulletRight;
                        selectedTarget = newTarget();
                        isShot = true;
                        if (db.getVibration() == 1) {
                            vibrator.vibrate(50);
                        }
                    }
                }
            }
        }

        //Merminin sol üst kısmı hedefin sağ alt kısmına çarptı mı?
        for (int i = 0; i <= 90; i++) {
            for (int j = 0; j <= 90; j++) {
                if (bulletTopLeftX.get(i) <= targetBottomRightX.get(j) && bulletTopLeftX.get(i) >= targetPosition[selectedTarget][0] &&
                        bulletTopLeftY.get(i) <= targetBottomRightY.get(j) && bulletTopLeftY.get(i) >= targetPosition[selectedTarget][1]) {
                    if (!isWritingScore) {
                        score++;
                        isWritingScore = true;
                        isRotateBulletRight = !isRotateBulletRight;
                        selectedTarget = newTarget();
                        isShot = true;
                        if (db.getVibration() == 1) {
                            vibrator.vibrate(50);
                        }
                    }
                }
            }
        }

        //Merminin sağ üst kısmı hedefin sol alt kısmına çarptı mı?
        for (int i = 0; i <= 90; i++) {
            for (int j = 0; j <= 90; j++) {
                if (bulletTopRightX.get(i) >= targetBottomLeftX.get(j) && bulletTopRightX.get(i) <= targetPosition[selectedTarget][0] &&
                        bulletTopRightY.get(i) <= targetBottomLeftY.get(j) && bulletTopRightY.get(i) >= targetPosition[selectedTarget][1]) {
                    if (!isWritingScore) {
                        score++;
                        isWritingScore = true;
                        isRotateBulletRight = !isRotateBulletRight;
                        selectedTarget = newTarget();
                        isShot = true;
                        if (db.getVibration() == 1) {
                            vibrator.vibrate(50);
                        }
                    }
                }
            }
        }
    }

    private void isGameOver() {
        //Mermi ekrandan çıktıktan sonra oyun bitiyor
        if (bulletX <= -70 || bulletX >= screenX + 70 ||
                bulletY <= -70 || bulletY >= screenY + 70) {
            if (life == 0) {
                isPlaying = false;
                Intent intent = new Intent(getContext(), EndGameActivity.class);
                intent.putExtra("score", score);
                getContext().startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                activity.finish();
            }
            life--;
            GAMEMODE = MODEGAMEAFTER;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (GAMEMODE == MODEGAME) {
            isFire = true;
        } else if (GAMEMODE == MODEGAMEAFTER) {
            if (mRewardedAd != null) {
                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d("greenball", "Ad was shown.");
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.d("greenball", "Ad failed to show.");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Set the ad reference to null so you don't show the ad a second time.
                        Log.d("greenball", "Ad was dismissed.");
                        mRewardedAd = null;
                        isPlaying = false;
                        Intent intent = new Intent(getContext(), EndGameActivity.class);
                        intent.putExtra("score", score);
                        getContext().startActivity(intent);
                        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        activity.finish();
                    }
                });
                Activity activityContext = activity;
                mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d("greenball", "The user earned the reward.");
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();
                        GAMEMODE = MODEGAME;
                        continueGame();
                    }
                });
            } else {
                Log.d("greenball", "The rewarded ad wasn't ready yet.");
            }
        } else {
            newGame();
        }
        return super.onTouchEvent(event);
    }

    private void sleep() {
        try {
            Thread.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}