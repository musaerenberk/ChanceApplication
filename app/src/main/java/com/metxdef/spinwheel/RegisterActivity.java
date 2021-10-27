package com.metxdef.spinwheel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {
    Button registerBTN;
    EditText nameEdit, emailEdit, passwordEdit, confirmPasswordEdit;
    ProgressBar progressBar;
    private static final String TAG = "DocSnippets";
    TextView loginTV;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userID,deviceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();

        init();

        clickListener();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
        super.onBackPressed();
    }

    @SuppressLint("HardwareIds")
    private void init() {
        registerBTN = findViewById(R.id.registerBTN);
        nameEdit = findViewById(R.id.nameET);
        emailEdit = findViewById(R.id.mailET);
        passwordEdit = findViewById(R.id.passwordET);
        confirmPasswordEdit = findViewById(R.id.confirmPass);
        progressBar = findViewById(R.id.progressBar);
        loginTV = findViewById(R.id.loginTV);
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void clickListener() {

        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        registerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEdit.getText().toString();
                String email = emailEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                String confirmPass = confirmPasswordEdit.getText().toString();

                if(name.isEmpty()) {
                    nameEdit.setError("Gerekli!");
                    return;
                }

                if(email.isEmpty()) {
                    emailEdit.setError("Gerekli!");
                    return;
                }

                if(password.isEmpty()) {
                    passwordEdit.setError("Gerekli!");
                    return;
                }

                if(confirmPass.isEmpty() || !password.equals(confirmPass)) {
                    confirmPasswordEdit.setError("Geçersiz Şifre");
                    return;
                }
                    createAccount(email, password);
            }
        });
    }

    private void createAccount(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            //Registeration successful:
                            FirebaseUser user = auth.getCurrentUser();
                            updateUi(user,email);

                        } else {
                            //Registeration failed:
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, "Error:"+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void updateUi(FirebaseUser user, String email) {
        String refer = email.substring(0, email.lastIndexOf("@"));
        String referCode = refer.replace(".", "");
        userID = user.getUid();

        Map<String, Object> map = new HashMap<>();
        map.put("name", nameEdit.getText().toString());
        map.put("email", email);
        map.put("uid", userID);
        map.put("image", " ");
        map.put("coins", 0);
        map.put("referCode", referCode);
        map.put("spins", 2);
        map.put("deviceID", deviceID);

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1); // to get yesterday date.

        Date previousDate = calendar.getTime();
        String dateString = dateFormat.format(previousDate);

        Map<String, Object> dateObject = new HashMap<>();
        dateObject.put("date", dateString);

        db.collection("Daily Check").document(userID).set(dateObject);

        DocumentReference docRef = db.collection("users").document(userID);

        docRef
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        user.sendEmailVerification();
                        Toast.makeText(RegisterActivity.this,
                                "Hoşgeldin! Hesabını onaylamak için e-posta adresine gelen linke tıkla!",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        progressBar.setVisibility(View.GONE);
    }

}