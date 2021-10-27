package com.metxdef.spinwheel.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.metxdef.spinwheel.MainActivity;
import com.metxdef.spinwheel.model.AmazonModel;
import com.metxdef.spinwheel.model.ProfileModel;
import com.metxdef.spinwheel.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;


public class AmazonFragment extends Fragment {

    private RadioGroup radioGroup;
    private Button withdrawBTN;
    private TextView coinsTV;
    String name, email;
    private Dialog dialog;

    //Firestore
     FirebaseFirestore db;
     DocumentReference reference;
     FirebaseUser user;
     private String userID;


    public AmazonFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_amazon, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        loadData();
        clickListener();
    }

    private void init(View view) {
        //Get Instance
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();
        userID = Objects.requireNonNull(user).getUid();
        reference = db.collection("users").document(userID);

        radioGroup = view.findViewById(R.id.radioGroup);
        withdrawBTN = view.findViewById(R.id.submitBTN);
        coinsTV = view.findViewById(R.id.coinsTV);

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.loading_dialog);
        if(dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
    }

    private void loadData() {
        reference
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ProfileModel model = documentSnapshot.toObject(ProfileModel.class);

                        coinsTV.setText(String.valueOf(model.getCoins()));

                        name = model.getName();
                        email = model.getEmail();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });
    }

    private void clickListener() {
        withdrawBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(getActivity())
                        .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {

                                if(report.areAllPermissionsGranted()) {

                                    String filePath = Environment.getExternalStorageDirectory()
                                            +"/SpinWheelApp/Amazon Gift Card/";
                                    File file = new File(filePath);
                                    file.mkdirs();

                                    int currentCoins = Integer.parseInt(coinsTV.getText().toString());
                                    int checkedID = radioGroup.getCheckedRadioButtonId();
                                    switch (checkedID) {

                                        case R.id.amazon25:
                                            AmazonCard(25, currentCoins);
                                            break;

                                        case R.id.amazon50:
                                            AmazonCard(50, currentCoins);
                                            break;
                                    }

                                }else {
                                    Toast.makeText(getContext(), "Lütfen yetkilendirmeye izin verin!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                            }
                        }).check();
            }
        });
    }

    private void AmazonCard(int amazonCard, int currentCoins) {

        if(amazonCard == 25) {

            if(currentCoins >= 6000) { // Min coins should be 6000
                sendGiftCard(1);
            } else {
                Toast.makeText(getContext(), "Yeterli bakiyeniz yok!", Toast.LENGTH_SHORT).show();
            }
        }
        else if (amazonCard == 50) {
            if(currentCoins >= 12000) {
                sendGiftCard(2);

            } else {
                Toast.makeText(getContext(), "Yeterli bakiyeniz yok!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    DocumentReference amazonRef;
    CollectionReference amazonColRef;
    Query query;

    private void sendGiftCard(final int cardAmount) {

        dialog.show();

        amazonRef = FirebaseFirestore.getInstance().collection("Gift Cards").document("Amazon");
        amazonColRef = db.collection("Gift Cards").document("Amazon").collection("1");

        if(cardAmount == 1) {

            query = amazonColRef.whereEqualTo("amazon", 25);

        }else if (cardAmount == 2) {
            query = amazonColRef.whereEqualTo("amazon", 50);
        }

        query
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            Random random = new Random();

                            int docCount = 0;

                            for(DocumentSnapshot documentS : task.getResult()) {
                                docCount++;
                            }

                            int rand = random.nextInt(docCount);

                            Iterator iterator = task.getResult().iterator();

                            for(int i = 0; i < rand; i++) {
                                iterator.next();
                            }

                            DocumentSnapshot childSnapshot = (DocumentSnapshot) iterator.next();

                            AmazonModel model = childSnapshot.toObject(AmazonModel.class);

                            int id = model.getId();
                            String giftCode = model.getAmazonCode();

                            printAmazonCode(id, giftCode, cardAmount);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

    }

    private void printAmazonCode(int id, String amazonCode, int cardAmount) {

        updateDate(cardAmount, id);

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.getDefault());
        String currentTime = dateFormat.format(date);

        String text = "Tarih: " + currentTime + "\n" +
                "İsim: " + name + "\n" +
                "Email: " + email + "\n" +
                "Ödeme Kimliği: " + id + "\n\n" +
                "Amazon Hediye Kodu: " + amazonCode;


        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(800, 800, 1).create();

        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Paint paint = new Paint();

        page.getCanvas().drawText(text, 10, 12, paint);
        pdfDocument.finishPage(page);

        String filePath = Environment.getExternalStorageDirectory()+"/SpinWheelApp/Amazon Gift Card/"
                +System.currentTimeMillis()
                +userID+"amazonCode.pdf";

        File file = new File(filePath);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        pdfDocument.close();



        //Open pdf document 
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        try {
            startActivity(Intent.createChooser(intent, "Birlikte aç"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Lütfen PDF okuyucu kurunuz!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDate(int cardAmount, int id) {
        Map<String, Object> map = new HashMap<>();

        int currentCoins = Integer.parseInt(coinsTV.getText().toString());

        if(cardAmount == 1) { //User select 25$ option
            int updatedCoins = currentCoins - 6000;
            map.put("coins", updatedCoins);

        } else if (cardAmount == 2) {
            int updatedCoins = currentCoins - 12000;
            map.put("coins", updatedCoins);
        }

        reference.update(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(getContext(), "Tebrikler!", Toast.LENGTH_SHORT).show();
                        }

                        dialog.dismiss();
                    }
                });

        db.collection("Gift Cards")
                .document("Amazon")
                .collection("1")
                .document(String.valueOf(id))
                .delete();
    }
}