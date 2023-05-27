package com.floki.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.floki.chatapp.Model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private final static int LOGIN_REQUEST_CODE = 160503;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    FirebaseDatabase database;
    DatabaseReference userRef;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (firebaseAuth != null && listener != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
        super.onStop();
        /*Superclass may have important
         *cleanup tasks that need to
         *be performed when the activity is stopped.
         */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build()
        );
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        //Dùng cho cả truy cập và tạo lần đầu
        userRef = database.getReference(com.floki.chatapp.Common.Common.USER_REFERENCES);
        listener = myFirebaseAuth -> {
            Dexter.withContext(this)
                    .withPermissions(Arrays.asList(
                            android.Manifest.permission.CAMERA,
//                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
//                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    )).withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                FirebaseUser user = myFirebaseAuth.getCurrentUser();
                                if (user != null) {
                                    checkUserFromFirebase();
                                } else showLoginLayout();
                            } else
                                Toast.makeText(MainActivity.this, "Please enable all permission", Toast.LENGTH_SHORT).show();
                        }

                        // When the user denies permission and needs to display the reason why the application needs that permission
                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                        }

                    }).check();
        };
    }
    private void showLoginLayout() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers).build(), LOGIN_REQUEST_CODE);
    }
    private void checkUserFromFirebase() {
        userRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            userModel.setUid(snapshot.getKey());
                            goToHomeActivity(userModel);
                        } else showRegisterLayout();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private void showRegisterLayout() {
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        finish();
    }

    private void goToHomeActivity(UserModel userModel) {
        com.floki.chatapp.Common.Common.currentUser = userModel;
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else
                Toast.makeText(MainActivity.this, "[ERROR]" + response.getError(), Toast.LENGTH_SHORT).show();
        }
    }
}