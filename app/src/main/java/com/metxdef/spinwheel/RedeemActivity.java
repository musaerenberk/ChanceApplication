package com.metxdef.spinwheel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.metxdef.spinwheel.fragment.FragmentReplaceActivity;

public class RedeemActivity extends AppCompatActivity {

    private ImageView amazonImage;
    private CardView amazonCard;
    private InterstitialAd interstitialAd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem);

        init();

        loadInterstitialAd();
        loadImages();
        clickListener();

    }

    private void init() {
        amazonImage = findViewById(R.id.amazonImage);
        amazonCard = findViewById(R.id.amazonGiftCard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void loadImages() {
        String amazonGiftImageURL = "https://toppng.com/uploads/preview/amazon-gift-card-11549868480mv0semfsfp.png"; //image Url

        Glide.with(RedeemActivity.this)
                .load(amazonGiftImageURL)
                .into(amazonImage);

    }

    private void clickListener() {

        amazonCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RedeemActivity.this, FragmentReplaceActivity.class);
                intent.putExtra("position", 1);
                startActivity(intent);
            }
        });

    }

    private void loadInterstitialAd() {

        //Admob init
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.admob_interstitial_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());

    }

    @Override
    public void onBackPressed() {
        //Admob
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
}