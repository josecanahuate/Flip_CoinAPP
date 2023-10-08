package com.roulette.flip;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.animation.Animator;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import com.google.android.gms.ads.AdRequest;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Random;
import android.media.MediaPlayer;

public class MainActivity extends AppCompatActivity {

    private LottieAnimationView coinAnimationView;
    private Button flipButton;
    private TextView resultText;
    private TextView caraCountText;
    private TextView cruzCountText;
    private int caraCount = 0;
    private int cruzCount = 0;
    private MediaPlayer mediaPlayer;
    private Random random = new Random();
    private InterstitialAd mInterstitialAd;
    BottomNavigationView bottomNavigationView;
    private int clickCounter = 0; // Contador de clics
    private AdView mAdView;
    private AdRequest adRequest;
    // ID ANUNCIO INTERSTICIAL
    private static final String AD_UNIT_ID = "ca-app-pub-4434685305331116/1270532919";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializa el MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.coinflip);

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "ad_clicked");
        mFirebaseAnalytics.logEvent("ad_clicked", bundle);

        // APLICACION DE DIMENSIONES
        // Obtener el ancho y alto en píxeles
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float density = getResources().getDisplayMetrics().density;
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        int layoutResourceId = 0;

        if (widthPixels == 320 && heightPixels == 480) {
            // 320x480
            layoutResourceId = R.layout.layout_320x480;

        } else if (widthPixels == 480 && heightPixels == 800) {
            // 480x800
            layoutResourceId = R.layout.layout_480x800;

        } else if (widthPixels == 720 && heightPixels == 1280) {
            // 720x1280
            layoutResourceId = R.layout.layout_720x1280;

        } else if (widthPixels == 1080 && heightPixels <= 1920) {
            // 1080x1920
            layoutResourceId = R.layout.layout_1080x1920;

        } else if (widthPixels == 1440 && heightPixels <= 2560) {
            // 1440x2560
            layoutResourceId = R.layout.layout_1440x2560;

        } else {
            // Otras resoluciones de pantalla
            layoutResourceId = R.layout.activity_main;
        }

        setContentView(layoutResourceId);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        coinAnimationView = findViewById(R.id.coinAnimationView);
        flipButton = findViewById(R.id.flipButton);
        resultText = findViewById(R.id.resultText);
        caraCountText = findViewById(R.id.caraCountText);
        cruzCountText = findViewById(R.id.cruzCountText);

        // Configura la animación Lottie
        coinAnimationView.setAnimation("coin_flip.json");
        coinAnimationView.setRepeatCount(0);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
            }
        });
        // Carga Banner
        mAdView = findViewById(R.id.adView);
        com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                FirebaseAnalytics.getInstance(MainActivity.this).logEvent("ad_clicked", null);
            }

            @Override
            public void onAdClosed() {
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                FirebaseAnalytics.getInstance(MainActivity.this).logEvent("ad_load_failed", null);
            }

            @Override
            public void onAdImpression() {
                FirebaseAnalytics.getInstance(MainActivity.this).logEvent("ad_impression", null);
            }

            @Override
            public void onAdLoaded() {
                FirebaseAnalytics.getInstance(MainActivity.this).logEvent("ad_loaded", null);
            }

            @Override
            public void onAdOpened() {
                FirebaseAnalytics.getInstance(MainActivity.this).logEvent("ad_opened", null);
            }

        });

        // Cargar el anuncio intersticial
        loadInterstitialAd();

        // Asigna un OnClickListener al botón
        flipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Genera un número aleatorio entre 4 y 7 una sola vez al inicio
                int randomNumToShowAd = new Random().nextInt(4) + 4;

                // Incrementa el contador de clics
                clickCounter++;

                // Verifica si el contador de clics alcanza el número aleatorio
                if (clickCounter >= randomNumToShowAd) {
                    // Muestra el anuncio intersticial
                    showInterstitial();

                    // Genera un nuevo número aleatorio entre 4 y 7 para mostrar el anuncio nuevamente
                    randomNumToShowAd = new Random().nextInt(4) + 4;

                    // Reinicia el contador de clics
                    clickCounter = 0;
                }
                flipCoin();
                playSound(); // Llama al método para reproducir el sonido
            }
        });


        coinAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // No es necesario implementar este método
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                int randomValue = random.nextInt(2);
                String result = (randomValue == 0) ? getString(R.string.carares) : getString(R.string.cruzres);
                resultText.setText(result);

                // Actualiza el conteo
                if (randomValue == 0) {
                    caraCount++;
                    caraCountText.setText(getString(R.string.cara) + caraCount);

                } else {
                    cruzCount++;
                    cruzCountText.setText(getString(R.string.cruz) + cruzCount);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // No es necesario implementar este método
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // No es necesario implementar este método
            }
        });
    }

    // Método para mostrar el anuncio intersticial
    public void showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    FirebaseAnalytics.getInstance(MainActivity.this).logEvent("ad_clicked", null);
                    Log.d(TAG, "El anuncio se clickeo.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Se llama cuando se cierra el anuncio
                    Log.d("MainActivity", "El anuncio se cerró.");
                    mInterstitialAd = null;
                    // Recargamos el anuncio para que esté listo para el siguiente botón
                    loadInterstitialAd();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Se llama si hay un error al mostrar el anuncio
                    Log.d("MainActivity", "No se pudo mostrar el anuncio.");
                    mInterstitialAd = null;
                }

                @Override
                public void onAdImpression() {
                    FirebaseAnalytics.getInstance(MainActivity.this).logEvent("ad_impression", null);
                    Log.d(TAG, "Ad recorded an impression.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Se llama cuando el anuncio se muestra correctamente
                    Log.d("MainActivity", "El anuncio se mostró.");
                }
            });
            mInterstitialAd.show(this);
        } else {
            Log.d("MainActivity", "El anuncio no está listo todavía.");
        }
    }

    // Método para cargar el anuncio intersticial
    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, AD_UNIT_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // El anuncio se cargó correctamente
                        mInterstitialAd = interstitialAd;
                        Log.d("MainActivity", "Anuncio intersticial cargado");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Error al cargar el anuncio
                        Log.d("MainActivity", "Error al cargar anuncio intersticial");
                        mInterstitialAd = null;
                    }
                });
    }

    // Método para reproducir el sonido al hacer clic en el botón
    private void playSound() {
        if (mediaPlayer != null) {
            mediaPlayer.start(); // Comienza a reproducir el sonido
            // Detener el sonido después de 1 segundo
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause(); // Pausa la reproducción del sonido
                        mediaPlayer.seekTo(0); // Reinicia el sonido al principio
                    }
                }
            }, 1000); // 1000 milisegundos = 1 segundo
        }
    }

    private void flipCoin() {
        // Reiniciar la animación Lottie
        coinAnimationView.setVisibility(View.VISIBLE);
        coinAnimationView.setProgress(0f);
        coinAnimationView.playAnimation();

        resultText.setText("");
    }
}
