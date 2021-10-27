package com.metxdef.spinwheel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.metxdef.spinwheel.model.ProfileModel;

import java.util.HashMap;
import java.util.Map;

public class WatchActivity extends AppCompatActivity {

    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    private Button watchBTN1;
    private TextView coinsTV;
    private String userID;

    DocumentReference reference;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        userID = FirebaseAuth.getInstance().getUid();
        reference = FirebaseFirestore.getInstance().collection("users").document(userID);

        init();

        loadData();

        loadInterstitialAd();
        loadRewardedAds();

        clickListener();
    }

    private void clickListener() {
        watchBTN1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRewardVideo();
            }
        });
    }

    private void showRewardVideo() {
        if (rewardedAd.isLoaded()) {
            Activity activityContext = WatchActivity.this;
            RewardedAdCallback adCallback = new RewardedAdCallback() {
                @Override
                public void onRewardedAdOpened() {
                    // Ad opened.
                    watchBTN1.setVisibility(View.GONE);
                }

                @Override
                public void onRewardedAdClosed() {
                    // Ad closed.
                    watchBTN1.setVisibility(View.VISIBLE);
                }

                @Override
                public void onUserEarnedReward(@NonNull RewardItem reward) {
                    updateDataFirestore();
                }
            };
            rewardedAd.show(activityContext, adCallback);
        }
    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("İzle ve Kazan");
        }

        watchBTN1 = findViewById(R.id.watchBTN1);
        coinsTV = findViewById(R.id.coinsTV);
    }

    private void loadRewardedAds() {
        rewardedAd = new RewardedAd(WatchActivity.this, getString(R.string.admob_rewarded_id));
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
            }

            @Override
            public void onRewardedAdFailedToLoad(LoadAdError adError) {
                // Ad failed to load.
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
    }

    private void loadInterstitialAd() {
        //Admob init
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.admob_interstitial_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onBackPressed() {
        //admob
        if(interstitialAd.isLoaded()) {
            interstitialAd.show();

            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    finish();
                }
            });
            return;
        }
        finish();
    }

    private void loadData() {
        reference
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ProfileModel model = documentSnapshot.toObject(ProfileModel.class);
                        coinsTV.setText(String.valueOf(model.getCoins()));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WatchActivity.this, "Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateDataFirestore() {
        int currentCoins = Integer.parseInt(coinsTV.getText().toString());
        int updatedCoin = currentCoins + 5;

        Map<String,Object> map = new HashMap<>();
        map.put("coins", updatedCoin);

        reference
                .update(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(WatchActivity.this,
                                    "Coinler başarıyla yüklendi!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}