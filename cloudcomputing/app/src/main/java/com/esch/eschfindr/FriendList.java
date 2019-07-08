package com.esch.eschfindr;

import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.esch.eschfindr.data.Friend;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class FriendList {

    private static final String TAG = "FriendList";
    private FirebaseUser user;
    private ArrayList<Friend> currentList = new ArrayList<>();
    private FriendRecyclerViewAdapter recyclerViewAdapter;
    private MainActivity mainActivity;

    public FriendList(MainActivity mainActivity) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        this.mainActivity = mainActivity;
    }

    public void refresh() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //First clear list
        Log.d(TAG, "Refreshing friends");
        db.collection("users").document(user.getUid()).collection("friends").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Log.d(TAG, "TEST:" + (queryDocumentSnapshots.getDocuments().size()));
                        currentList.clear();
                        for(DocumentSnapshot d : queryDocumentSnapshots.getDocuments()) {
                            //TODO straighten this spaghetti
                            String uid = d.getString("uid");
                            String name = d.getString("name");
                            String id = d.getId();
                            LatLng location = new LatLng(0,0);

                            if(d.get("location") != null) {
                                HashMap<String, Object> loc = (HashMap<String, Object>) d.get("location");
                                if(loc.get("longitude") instanceof Long ) {
                                    location = new LatLng((Double) loc.get("latitude"), ((Long) loc.get("longitude")).doubleValue());
                                } else {
                                    location = new LatLng((Double) loc.get("latitude"),(Double)  loc.get("longitude"));
                                }
                            }

                            boolean state = (boolean) d.getData().get("allow");
                            int allow;
                            if(state) allow = Friend.TRUE;
                            else allow = Friend.FALSE;
                            System.out.println("Fetched: " + name + "| " + state);
                            Friend friend = new Friend(uid, name, allow,id, location);
                            currentList.add(friend);
                        }
                        mainActivity.updateFriendList();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });


    }


    public ArrayList<Friend> getList() {
        return currentList;
    }

    public void toggle(Friend item) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final boolean state = (item.allow == Friend.TRUE) ? false : true;
        item.setState(state);
        mainActivity.updateFriendList();
        db.collection("users").document(user.getUid()).collection("friends").document(item.id).update("allow", state).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //refresh();
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mainActivity.showError("Failed to toggle");
                //We failed here but showed success early. lets just refresh everything and tell user something went wrong
                refresh();
                Log.d(TAG, e.toString());
            }
        });
    }

    public void requestFriend(String email) {

        //verify email here

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(TAG, "Requesting friend");
        //First check if user with email exists
        db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG,""+queryDocumentSnapshots.getDocuments().size());
                if(queryDocumentSnapshots.getDocuments().size() == 1) {
                    String uid = (String) queryDocumentSnapshots.getDocuments().get(0).get("uid");
                    addFriendRequest(uid);
                } else {
                    mainActivity.showError("No user with that email found");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    private void addFriendRequest(String uid) {
        System.out.println(uid);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("uid", user.getUid());
        requestData.put("name", user.getDisplayName());

        db.collection("users")
                .document(uid)
                .collection("requests")
                .add(requestData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                mainActivity.showError("Friend request sent");
            }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mainActivity.showError(e.toString());
                Log.e(TAG, e.toString());
            }
        });
    }
}
