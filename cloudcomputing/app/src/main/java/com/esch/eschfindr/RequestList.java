package com.esch.eschfindr;

import android.support.annotation.NonNull;
import android.util.Log;

import com.esch.eschfindr.data.Friend;
import com.esch.eschfindr.data.Request;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

class RequestList {

    private static final String TAG = "RequestList";
    private ArrayList<Request> requests = new ArrayList<>();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private MainActivity mainActivity;

    public RequestList(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

    }

    public ArrayList<Request> getList() {
        return requests;
    }

    public void refresh() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //First clear list
        Log.d(TAG, "Refreshing requests");
        db.collection("users").document(user.getUid()).collection("requests").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Log.d(TAG, "TEST:" + (queryDocumentSnapshots.getDocuments().size()));
                        requests.clear();
                        for(DocumentSnapshot d : queryDocumentSnapshots.getDocuments()) {
                            String uid = (String) d.getData().get("uid");
                            String name = (String) d.getData().get("name");
                            String id = d.getId();
                            Request friend = new Request(name, uid,id);
                            requests.add(friend);
                        }
                        mainActivity.updateRequestList();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });
    }
}
