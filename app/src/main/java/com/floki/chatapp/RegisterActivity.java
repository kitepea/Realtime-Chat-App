package com.floki.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.floki.chatapp.Common.Common;
import com.floki.chatapp.Model.UserModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference userRef;

    MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker().build();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    Calendar calendar = Calendar.getInstance();
    boolean isSelectBirthDate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();
        setDefaultData();
    }
    private void init(){
        TextInputEditText edt_date_of_birth = findViewById(R.id.edt_date_of_birth);

        database = FirebaseDatabase.getInstance();
        userRef = database.getReference(Common.USER_REFERENCES);
        materialDatePicker.addOnPositiveButtonClickListener(selection -> { // selection(millis): time user picked from DatePickerDialog
                          // After confirm valid selection
            calendar.setTimeInMillis(selection);
            edt_date_of_birth.setText(simpleDateFormat.format(selection));
            isSelectBirthDate = true;
        });
    }
    private void setDefaultData() {
        TextInputEditText edt_phone = findViewById(R.id.edt_phone);
        TextInputEditText edt_date_of_birth = findViewById(R.id.edt_date_of_birth);
        Button btn_register = findViewById(R.id.btn_register);
        TextInputEditText edt_first_name = findViewById(R.id.edt_first_name);
        TextInputEditText edt_last_name = findViewById(R.id.edt_last_name);
        TextInputEditText edt_bio = findViewById(R.id.edt_bio);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        edt_phone.setText(user.getPhoneNumber());
        edt_phone.setEnabled(false);

        edt_date_of_birth.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                materialDatePicker.show(getSupportFragmentManager(), materialDatePicker.toString());
            }
        });

        btn_register.setOnClickListener(v -> {
            if (!isSelectBirthDate){
//                Toast.makeText(RegisterActivity.this, "Please enter birthdate", Toast.LENGTH_SHORT).show();
                Toast.makeText(RegisterActivity.this, "Please enter birthdate", Toast.LENGTH_LONG).show();
                return;
            }
        UserModel userModel = new UserModel();
        //Personal
        userModel.setFirstName(Objects.requireNonNull(edt_first_name.getText()).toString());
        userModel.setLastName(Objects.requireNonNull(edt_last_name.getText()).toString());
        userModel.setBio(Objects.requireNonNull(edt_bio.getText()).toString());
        userModel.setPhone(Objects.requireNonNull(edt_phone.getText()).toString());
        userModel.setBirthDate(calendar.getTimeInMillis());
        userModel.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.child(userModel.getUid())
                .setValue(userModel)
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Register success!", Toast.LENGTH_SHORT).show();
                    Common.currentUser = userModel;
                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                    finish();
                });
        });
    }
}