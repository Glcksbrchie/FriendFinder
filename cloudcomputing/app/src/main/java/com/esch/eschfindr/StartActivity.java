package com.esch.eschfindr;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StartActivity extends AppCompatActivity {

    private static final int REGISTER_RESPONSE_CODE = 100;
    private final String TAG = "StartActivity";

    private TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    @Override
    protected void onStart() {
        super.onStart();

        progressText = findViewById(R.id.start_state_text);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            verifyUserData(FirebaseAuth.getInstance().getCurrentUser());
        } else {
            showRegisterActivity();
        }

    }

    private void showRegisterActivity() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder()
                                        .setRequireName(true)
                                        .build()))
                        .build(),
                REGISTER_RESPONSE_CODE);
    }


    private void showMainActivty() {
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
        finish();
        Log.d(TAG, "Showing MainActivity");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == REGISTER_RESPONSE_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                verifyUserData(FirebaseAuth.getInstance().getCurrentUser());
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

                showSnackbar(R.string.unknown_error);
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }

    public void verifyUserData(FirebaseUser currentUser) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser user = currentUser;

        //update progress text
        progressText.setText(R.string.verifying);

        //First check if DB entry exists already

        Log.d(TAG, "Current user Id:" + user.getUid());

        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Map<String, Object> data = task.getResult().getData();
                if(data != null) {
                    if(data.containsKey("firebasetoken") &&
                            data.containsKey("email") &&
                            data.containsKey("uid") &&
                            data.containsKey("username") ) {
                        Log.d(TAG, "All data correct");
                        showMainActivty();
                    } else {
                        createUser(user);
                    }
                } else {
                    setProgressText("User does not exist. Updating");
                    createUser(user);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("FAILURE", e.toString());
            }
        });
    }

    public void showSnackbar(int text) {
        Snackbar.make(findViewById(R.id.startActivityLayout), getString(text), Snackbar.LENGTH_SHORT);
    }

    public void createUser(FirebaseUser firebaseUser) {

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d(TAG, "Creating user");
        final String firebasetoken = FirebaseInstanceId.getInstance().getToken();

        String name = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();
        final String uid = firebaseUser.getUid();
        String username = firebaseUser.getDisplayName();

        HashMap<String, Object> user = new HashMap<>();
        user.put("firebasetoken", firebasetoken);
        user.put("email", email);
        user.put("uid", uid);
        user.put("username", username);

        db.collection("users").document(uid).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Created user :" + firebasetoken + "UID" + uid);
                showMainActivty();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.toString());
            }
        });



    }

    public void setProgressText(String progress) {
        progressText.setText(progress);
    }

}
