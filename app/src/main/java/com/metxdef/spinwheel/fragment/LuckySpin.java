package com.metxdef.spinwheel.fragment;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.RewardedVideoAd;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.metxdef.spinwheel.R;
import com.metxdef.spinwheel.WatchActivity;
import com.metxdef.spinwheel.model.ProfileModel;
import com.metxdef.spinwheel.spin.SpinItem;
import com.metxdef.spinwheel.spin.WheelView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class LuckySpin extends Fragment {

    private Button playBTN, watchBTN;
    private TextView coinsTV;
    private WheelView wheelView;
    List<SpinItem> spinItemList = new ArrayList<>();
    private FirebaseUser user;
    FirebaseFirestore db;
    CollectionReference reference;
    int currentSpins;

    private com.google.android.gms.ads.InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;

    public LuckySpin() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lucky_spin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadAd();
        loadRewardedAds();

        init(view);
        loadData();
        spinList();
        clickListener();
    }

    private void init(View view) {
        playBTN = view.findViewById(R.id.playBTN);
        wheelView = view.findViewById(R.id.wheelView);
        coinsTV = view.findViewById(R.id.coinsTV);
        watchBTN = view.findViewById(R.id.watchBTN);

        //Firebase
        reference = FirebaseFirestore.getInstance().collection("users");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    private void spinList() {
        SpinItem item1 = new SpinItem();
        item1.text = "2";         //Coin text
        item1.color = 0xffFFF3E0; //Background color
        spinItemList.add(item1);

        SpinItem item2 = new SpinItem();
        item2.text = "2";
        item2.color = 0xffFFE0B2;
        spinItemList.add(item2);

        SpinItem item3 = new SpinItem();
        item3.text = "3";
        item3.color = 0xffFFF3E0;
        spinItemList.add(item3);

        SpinItem item4 = new SpinItem();
        item4.text = "2";
        item4.color = 0xffFFE0B2;
        spinItemList.add(item4);

        SpinItem item5 = new SpinItem();
        item5.text = "6";
        item5.color = 0xffFFF3E0;
        spinItemList.add(item5);

        SpinItem item6 = new SpinItem();
        item6.text = "8";
        item6.color = 0xffFFE0B2;
        spinItemList.add(item6);

        SpinItem item7 = new SpinItem();
        item7.text = "10";
        item7.color = 0xffFFF3E0;
        spinItemList.add(item7);

        SpinItem item8 = new SpinItem();
        item8.text = "7";
        item8.color = 0xffFFE0B2;
        spinItemList.add(item8);

        SpinItem item9 = new SpinItem();
        item9.text = "9";
        item9.color = 0xffFFF3E0;
        spinItemList.add(item9);

        SpinItem item10 = new SpinItem();
        item10.text = "5";
        item10.color = 0xffFFE0B2;
        spinItemList.add(item10);

        SpinItem item11 = new SpinItem();
        item11.text = "5";
        item11.color = 0xffFFF3E0;
        spinItemList.add(item11);

        SpinItem item12 = new SpinItem();
        item12.text = "5";
        item12.color = 0xffFFE0B2;
        spinItemList.add(item12);

        wheelView.setData(spinItemList);
        wheelView.setRound(getRandCircleRound());

        wheelView.LuckyRoundItemSelectedListener(new WheelView.LuckyRoundItemSelectedListener() {
            @Override
            public void LuckyRoundItemSelected(int index) {
                playBTN.setEnabled(true);
                playBTN.setAlpha(1f);

                //wheel stop rotating:: here to show ad
                double random = Math.random();
                if(random > 0.6) {
                    showAd();
                }

                String value = spinItemList.get(index - 1).text;
                updateDataFirebase(Integer.parseInt(value));


            }
        });
    }

    private void clickListener() {
        
        playBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = getRandomIndex();

                if(currentSpins >= 1 && currentSpins < 3) {
                    wheelView.startWheelWithTargetIndex(index);
                    Toast.makeText(getActivity(), "Video izleyerek daha fazla çevirme hakkı kazanabilirsiniz.", Toast.LENGTH_SHORT).show();
                    watchBTN.setVisibility(View.VISIBLE);
                }

                if(currentSpins < 1) {
                    playBTN.setEnabled(false);
                    playBTN.setAlpha(.6f);
                    Toast.makeText(getActivity(), "Video izleyerek daha fazla çevirme hakkı kazanabilirsiniz.", Toast.LENGTH_SHORT).show();
                    watchBTN.setVisibility(View.VISIBLE);

                } else {
                    playBTN.setEnabled(false);
                    playBTN.setAlpha(.6f);
                    wheelView.startWheelWithTargetIndex(index);

                    watchBTN.setVisibility(View.INVISIBLE);
                }

            }
        });

        watchBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRewardedAds();
            }
        });
    }

    private void showRewardedAds() {
        if (rewardedAd.isLoaded()) {
            Activity activityContext = (Activity) getContext();
            RewardedAdCallback adCallback = new RewardedAdCallback() {
                @Override
                public void onRewardedAdOpened() {
                    // Ad opened.

                }

                @Override
                public void onRewardedAdClosed() {
                    // Ad closed.

                }

                @Override
                public void onUserEarnedReward(@NonNull RewardItem reward) {
                    int up = currentSpins + 1;
                    Map<String, Object> map = new HashMap<>();
                    map.put("spins", up);

                    reference
                            .document(user.getUid())
                            .update(map);
                }
            };
            rewardedAd.show(activityContext, adCallback);

        }

    }

    private int getRandomIndex() {

        int[] index = new int[] {1,1,1,1,2,2,2,2,2,2,3,3,3,3,3,3,4,4,4,4,5,5,5,6,6,7,7,9,9,10,11,12};

        int random = new Random().nextInt(index.length);

        return index[random];
    }

    private int getRandCircleRound(){
        Random random = new Random();

        return random.nextInt(10)+15;
    }

    private void loadData() {
        reference.document(user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ProfileModel model = documentSnapshot.toObject(ProfileModel.class);
                        coinsTV.setText(String.valueOf(model.getCoins()));

                        currentSpins = model.getSpins();

                        String currentSpin ="Çarkı Çevir: " + String.valueOf(currentSpins);
                        playBTN.setText(currentSpin);
                        
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                if(getActivity() != null)
                    getActivity().finish();
            }
        });
    }

    private void updateDataFirebase(int reward) {
        int currentCoins = Integer.parseInt(coinsTV.getText().toString());
        int updatedCoins = currentCoins + reward;

        int updatedSpins = currentSpins - 1;

        Map<String, Object> map = new HashMap<>();
        map.put("coins", updatedCoins);
        map.put("spins", updatedSpins);

        reference.document(user.getUid())
                .update(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(getContext(), "Para yüklendi.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error: " + task.getException()
                                    .getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void loadAd() {
            //Admob init
        interstitialAd = new InterstitialAd(Objects.requireNonNull(getContext()));
        interstitialAd.setAdUnitId(getString(R.string.admob_interstitial_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void loadRewardedAds() {
        rewardedAd = new RewardedAd(Objects.requireNonNull(getContext()), getString(R.string.admob_rewarded_id));
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

    private void showAd() {
            if(interstitialAd.isLoaded()) {
                interstitialAd.show();

                return;
            }
    }

}