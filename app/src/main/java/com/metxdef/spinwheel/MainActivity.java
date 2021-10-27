package com.metxdef.spinwheel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.facebook.ads.AdSettings;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.metxdef.spinwheel.fragment.FragmentReplaceActivity;
import com.metxdef.spinwheel.model.ProfileModel;



import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private CardView dailyCheckCard, luckySpinCard, taskCard, referCard, redeemCard, aboutCard, watchCard;
    private CircleImageView profileImage;
    private TextView coinsTv, nameTV, emailTV;
    Toolbar toolbar;
    private Dialog dialog;
    Internet internet;

    //Admob
    AdView adView;
    InterstitialAd interstitialAd;



    FirebaseFirestore db;
    FirebaseAuth auth;
    private FirebaseUser user;
    private DocumentReference documentReference;
    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setSupportActionBar(toolbar);

        internet = new Internet(MainActivity.this);

        AdSettings.isTestMode(this); // for testing

        //Admob banner ads
        adView = findViewById(R.id.banner_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        //Admob inters ads
        loadInterstitialAd();

        checkInternetConnection();

        //Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        documentReference = db.collection("users").document(userID);
        getDataFromDatabase();

        //Click Listener
        clickListener();
    }

    private void init() {
        dailyCheckCard = findViewById(R.id.dailyCheckCard);
        luckySpinCard = findViewById(R.id.luckySpinCard);
        taskCard = findViewById(R.id.taskCard);
        referCard = findViewById(R.id.referCard);
        redeemCard = findViewById(R.id.redeemCard);
        aboutCard = findViewById(R.id.aboutCard);
        watchCard = findViewById(R.id.watchCard);
        profileImage = findViewById(R.id.profileImage);
        coinsTv = findViewById(R.id.coinsTV);
        toolbar = findViewById(R.id.toolbar);
        emailTV = findViewById(R.id.mailTV);
        nameTV = findViewById(R.id.nameTV);


        dialog = new Dialog(this);
        dialog.setContentView(R.layout.loading_dialog);
        if(dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
    }

    private void clickListener() {
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInterstitialAd(1);
            }
        });

        referCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInterstitialAd(2);
            }
        });

        dailyCheckCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dailyCheck();
            }
        });

        redeemCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInterstitialAd(3);
            }
        });

        luckySpinCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInterstitialAd(4);
            }
        });

        aboutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInterstitialAd(5);
            }
        });

        watchCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInterstitialAd(6);
            }
        });
    }

    private void getDataFromDatabase() {
        dialog.show();

        documentReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ProfileModel model = documentSnapshot.toObject(ProfileModel.class);
                        nameTV.setText(model.getName());
                        emailTV.setText(model.getEmail());
                        coinsTv.setText(String.valueOf(model.getCoins()));

                        Glide.with(getApplicationContext())
                                .load(model.getImage())
                                .timeout(6000)
                                .placeholder(R.drawable.profile)
                                .into(profileImage);

                        dialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Error: " +e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void checkInternetConnection() {
        if(internet.isConnected()) {
            new isInternetActive().execute();
        } else {
            Toast.makeText(this, "Lütfen internet bağlantınızı kontrol edin.", Toast.LENGTH_SHORT).show();
        }
    }

    private void dailyCheck() {
        if(internet.isConnected()) {

            final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            sweetAlertDialog.setTitleText("Lütfen bekleyin..");
            sweetAlertDialog.setCancelable(false);
            sweetAlertDialog.show();


            final Date currentDate = Calendar.getInstance().getTime();
            final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            CollectionReference colRefDaily = db.collection("Daily Check");
            colRefDaily.document(userID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String dbDateString = documentSnapshot.get("date").toString();

                                try {
                                    Date dbDate = dateFormat.parse(dbDateString);

                                    String xDate = dateFormat.format(currentDate);
                                    Date date = dateFormat.parse(xDate);


                                    if (date.after(dbDate) && date.compareTo(dbDate) != 0) {
                                        //Reward Available

                                        documentReference
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        ProfileModel model = documentSnapshot.toObject(ProfileModel.class);

                                                        int currentCoins = model.getCoins();
                                                        int update = currentCoins + 10;

                                                        int spinC = model.getSpins();
                                                        int updateSpins = spinC + 2;

                                                        Map<String, Object> map = new HashMap<>();
                                                        map.put("coins", update);
                                                        map.put("spins", updateSpins);

                                                        documentReference.update(map);

                                                        Date newDate = Calendar.getInstance().getTime();
                                                        String newDateString = dateFormat.format(newDate);

                                                        Map<String, Object> dateMap = new HashMap<>();
                                                        dateMap.put("date", newDateString);

                                                        colRefDaily.document(userID).update(dateMap)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        sweetAlertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                                                        sweetAlertDialog.setTitleText("Başarılı!");
                                                                        sweetAlertDialog.setContentText("Ödüller başarıyla hesabına eklendi!");
                                                                        sweetAlertDialog.setConfirmButton("Tamam", new SweetAlertDialog.OnSweetClickListener() {
                                                                            @Override
                                                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                                sweetAlertDialog.dismissWithAnimation();
                                                                                finish();
                                                                                overridePendingTransition(0, 0);
                                                                                startActivity(getIntent());
                                                                                overridePendingTransition(0, 0);
                                                                            }
                                                                        }).show();
                                                                    }
                                                                });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });


                                    } else {
                                        sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                        sweetAlertDialog.setTitleText("Başarısız!");
                                        sweetAlertDialog.setContentText("Günlük hediyenizi zaten aldınız, yarın tekrar bekleriz!");
                                        sweetAlertDialog.setConfirmButton("Tamam", null);
                                        sweetAlertDialog.show();
                                    }

                                } catch (ParseException e) {
                                    e.printStackTrace();

                                    sweetAlertDialog.dismissWithAnimation();
                                }

                            } else {
                                sweetAlertDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                                sweetAlertDialog.setTitleText("Sistem Meşgul!");
                                sweetAlertDialog.setContentText("Sistem şu an meşgul, lütfen sonra tekrar deneyin.");
                                sweetAlertDialog.setConfirmButton("Tamam", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismissWithAnimation();
                                    }
                                });
                                sweetAlertDialog.show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    sweetAlertDialog.dismissWithAnimation();
                }
            });
        } else {
            Toast.makeText(this, "Lütfen internet bağlantınızı kontrol edin!", Toast.LENGTH_SHORT).show();
        }
    }


    class isInternetActive extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {

            InputStream inputStream = null;
            String json = "";

            try {


                String strURL = "https://icons.iconarchive.com/";
                URL url = new URL(strURL);

                URLConnection urlConnection = url.openConnection();
                urlConnection.setDoOutput(true);
                inputStream = urlConnection.getInputStream();
                json = "success";

            } catch (Exception e) {
                e.printStackTrace();
                json = "failed";
            }

            return json;
        }

        @Override
        protected void onPostExecute(String s) {
            
            if(s != null) {
                if(s.equals("success")) {
                    Toast.makeText(MainActivity.this, "İnternet Bağlandı.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "İnternet Yok!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "İnternet Yok!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(MainActivity.this, "İnternete bağlanılıyor..", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }
    }

    private void loadInterstitialAd() {

        //Admob init
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.admob_interstitial_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());


    }

    private void showInterstitialAd(final int i) {

        //Admob Ads
        if(interstitialAd.isLoaded()) {
            interstitialAd.show();

            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();

                    if(i == 1) {
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    }

                    if(i == 2) {
                        startActivity(new Intent(MainActivity.this, InviteActivity.class));
                    }

                    if(i == 3) {
                        startActivity(new Intent(MainActivity.this, RedeemActivity.class));
                    }

                    if(i == 4) {
                        Intent intent = new Intent(MainActivity.this, FragmentReplaceActivity.class);
                        intent.putExtra("position", 2);
                        startActivity(intent);
                    }

                    if(i == 5) {
                        Intent intent = new Intent(MainActivity.this, FragmentReplaceActivity.class);
                        intent.putExtra("position", 3);
                        startActivity(intent);
                    }

                    if(i == 6) {
                        startActivity(new Intent(MainActivity.this, WatchActivity.class));
                    }
                }
            });

            return;
        }



        if(i == 1) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }

        if(i == 2) {
            startActivity(new Intent(MainActivity.this, InviteActivity.class));
        }

        if(i == 3) {
            startActivity(new Intent(MainActivity.this, RedeemActivity.class));
        }

        if(i == 4) {
            Intent intent = new Intent(MainActivity.this, FragmentReplaceActivity.class);
            intent.putExtra("position", 2);
            startActivity(intent);
        }

        if(i == 5) {
            Intent intent = new Intent(MainActivity.this, FragmentReplaceActivity.class);
            intent.putExtra("position", 3);
            startActivity(intent);
        }

        if(i == 6) {
            startActivity(new Intent(MainActivity.this, WatchActivity.class));
        }
    }




}