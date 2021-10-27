package com.metxdef.spinwheel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.metxdef.spinwheel.model.ProfileModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InviteActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private String oppositeUID, userID;

    private TextView referCodeTV;
    private Button shareBTN, redeemBTN;
    private FirebaseFirestore db;

    DocumentReference reference;
    CollectionReference colRef;

    private InterstitialAd interstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        init();

        loadInterstitialAd();

        redeemAvailability();
        loadData();
        clickListener();
    }

    private void redeemAvailability() {
        reference
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists() && documentSnapshot.contains("redeemed")) {
                            boolean isAvailable = (boolean) documentSnapshot.get("redeemed");

                            if (isAvailable) {
                                redeemBTN.setVisibility(View.GONE);
                                redeemBTN.setEnabled(false);
                            } else {
                                redeemBTN.setEnabled(true);
                                redeemBTN.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

    private void init() {
        referCodeTV = findViewById(R.id.referCodeTV);
        shareBTN = findViewById(R.id.shareBTN);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        redeemBTN = findViewById(R.id.redeemBTN);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Firebase Initals
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        db = FirebaseFirestore.getInstance();
        colRef = db.collection("users");
        reference = db.collection("users").document(userID);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadData() {
        reference
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String referCode = documentSnapshot.getData().get("referCode").toString();
                        referCodeTV.setText(referCode);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(InviteActivity.this, "Error: "+ e.getMessage() , Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void clickListener() {
        shareBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String referCode = referCodeTV.getText().toString();

                String shareBody = "Hey, Türkiye'nin en çok kazandıran uygulamasını kullanıyorum." +
                        "Sen de davet kodumu kullanarak 100 Coin kazanabilirsin. " +"Davet Kodum: " + referCode+"\n"+
                        "Play Store'dan İndir\n"+
                        "https://play.google.com/store/apps/details?id="+
                        getPackageName();

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(intent);
            }
        });

        redeemBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = new EditText(InviteActivity.this);
                editText.setHint("Davet Kodu");
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);

                editText.setLayoutParams(layoutParams);

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(InviteActivity.this);

                alertDialog.setTitle("Davet Kodunu Gir");

                alertDialog.setView(editText);

                alertDialog.setPositiveButton("Gir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputCode = editText.getText().toString();

                        if(TextUtils.isEmpty(inputCode)) {
                            Toast.makeText(InviteActivity.this, "Geçersiz davet kodu!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(inputCode.equals(referCodeTV.getText().toString())) {
                            Toast.makeText(InviteActivity.this, "Kendi davet kodunu giremezsin!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        
                        redeemQuery(inputCode, dialog);
                    }
                }).setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
    }

    private void redeemQuery(String inputCode, final DialogInterface dialog) {
        Query query = colRef.whereEqualTo("referCode", inputCode);

        query
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot doc: task.getResult()) {
                                oppositeUID = doc.getData().get("uid").toString();

                                colRef
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for(QueryDocumentSnapshot qs : queryDocumentSnapshots) {
                                                   if(qs.getId().equals(userID)) {
                                                       ProfileModel myModel = qs.toObject(ProfileModel.class);
                                                       int myCoins = myModel.getCoins();
                                                       int myUpdate = myCoins + 100;

                                                       Map<String, Object> myMap = new HashMap<>();
                                                       myMap.put("coins", myUpdate);
                                                       myMap.put("redeemed", true);

                                                       reference.update(myMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                           @Override
                                                           public void onComplete(@NonNull Task<Void> task) {
                                                               dialog.dismiss();
                                                               Toast.makeText(InviteActivity.this, "Tebrikler!", Toast.LENGTH_SHORT).show();
                                                               finish();
                                                               overridePendingTransition(0, 0);
                                                               startActivity(getIntent());
                                                               overridePendingTransition(0, 0);
                                                           }
                                                       });

                                                   } else if (qs.getId().equals(oppositeUID)) {
                                                       ProfileModel model = qs.toObject(ProfileModel.class);
                                                       int coins = model.getCoins();
                                                       int updatedCoins = coins + 100;

                                                       Map<String, Object> map = new HashMap<>();
                                                       map.put("coins", updatedCoins);

                                                       colRef.document(oppositeUID).update(map);
                                                   }

                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(InviteActivity.this, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });


                            }
                        } else {
                            Toast.makeText(InviteActivity.this, "Please check the logs.", Toast.LENGTH_SHORT).show();
                        }
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

        //if ad not loaded then .....
        finish();

    }
}