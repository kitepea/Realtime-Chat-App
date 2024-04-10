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
//        setContentView(R.layout.activity_main);
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
            //Dexter là một thư viện trong Android giúp đơn giản hóa quá trình yêu cầu quyền tại thời gian chạy
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
    /*Thus, to summarize

    shouldShowRequestPermissionRationale will return true only if the application was launched earlier and the user "denied" the permission WITHOUT checking "never ask again".
    In other cases (app launched first time, or the app launched earlier too and the user denied permission by checking "never ask again"), the return value is false.
    */
                    }).check();
        };
    }

    private void showLoginLayout() {
        startActivityForResult(AuthUI.getInstance() // bắt đầu activity đăng nhập và mong đợi kết quả trả về
                .createSignInIntentBuilder() // tạo intent builder để điều chỉnh quá trình đăng nhập
                .setIsSmartLockEnabled(false) // vô hiệu hóa smartlock
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers).build(), LOGIN_REQUEST_CODE);
        // Từ 1 SignInIntentBuilder, build() được gọi để tạo ra 1 Intent
    }

    private void checkUserFromFirebase() {
    // Add a new child node with the specified name to your data structure
        userRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
    /*  the value from the local cache to be returned immediately, instead of checking for an updated value on the server
        useful for data that only needs to be loaded once and isn't expected to change frequently or require active listening
        such as when initializing a UI element that you don't expect to change
        it executes onDataChange method immediately and after executing that method once, it stops listening to the reference location it is attached to.*/
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            // organize the data contained in this snapshot into a class of your choosing: UserModel
                            // chuyển đổi dữ liệu từ snapshot thành một đối tượng của lớp UserModel
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
        //  Java (and Kotlin) syntax does not support getting the class just by using the class name, so .class to force
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        finish();
    }

    private void goToHomeActivity(UserModel userModel) {
        com.floki.chatapp.Common.Common.currentUser = userModel;
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
        // if you call finish() after an intent you can't go back to the previous activity with the "back" button
        // lets the system know that the programmer wants the current Activity to be finished. And hence, it calls up onDestroy() after that.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            // IDP: indentity provider
            // data: là đối tượng chứa dữ liệu ở Activity B.
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Tác vụ đã thành công
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            } else
                Toast.makeText(MainActivity.this, "[ERROR]" + response.getError(), Toast.LENGTH_SHORT).show();
        }
    }
    // Phương thức startActivityForResult() được sử dụng khi Activity A start Activity B và muốn nhận dữ liệu trả về từ Activity B đó.
    // Phương thức onActivityResult() là phương thức xử lý kết quả trả về, từ Activity đã mở thông qua phương thức startActivityForResult()
}