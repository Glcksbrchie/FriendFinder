package com.esch.eschfindr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.esch.eschfindr.data.Friend;
import com.esch.eschfindr.data.InternalLocation;
import com.esch.eschfindr.data.Request;
import com.esch.eschfindr.dummy.DummyContent;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements FriendFragment.OnListFragmentInteractionListener, RequestFragment.OnListFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    private TextView mTextMessage;

    private FriendList friendList = new FriendList(this);
    private RequestList requestList = new RequestList(this);

    private FriendFragment friendFragment = FriendFragment.newInstance(1, friendList.getList());
    private RequestFragment requestFragment = RequestFragment.newInstance(1, requestList.getList());
    private SupportMapFragment locatorFragment = LocatorFragment.newInstance();
    private FusedLocationProviderClient client;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_friends:
                    mTextMessage.setText(R.string.title_home);
                    replaceFragment(friendFragment);
                    friendList.refresh();
                    findViewById(R.id.addFriendButton).setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_requests:
                    mTextMessage.setText(R.string.no_requests);
                    replaceFragment(requestFragment);
                    requestList.refresh();
                    findViewById(R.id.addFriendButton).setVisibility(View.INVISIBLE);
                    return true;
                case R.id.navigation_map:
                    mTextMessage.setText(R.string.title_notifications);
                    if(mapready) updateMap();
                    replaceFragment(locatorFragment);
                    findViewById(R.id.addFriendButton).setVisibility(View.INVISIBLE);
                    return true;
            }
            return false;
        }
    };

    private void updateMap() {

        map.clear();
        for(Friend f : friendList.getList()) {
            map.addMarker(new MarkerOptions().title(f.name)
            .position(f.pos));
            Log.d(TAG, "Added marker to map:" + f.name +" | "+ f.pos);
        }

    }


    private boolean mapready = false;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        FloatingActionButton b = (FloatingActionButton) findViewById(R.id.addFriendButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFriendDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.navigation_logout:
                FirebaseAuth.getInstance().signOut();
                client.removeLocationUpdates(locLoop);
                Intent i = new Intent(this, StartActivity.class);
                startActivity(i);
                finish();
                break;
        }
        return true;
    }


    @Override
    public void onListFragmentInteraction(Friend item) {
        friendList.toggle(item);
        System.out.println("Interaction with" + item.name);
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    public void updateFriendList() {
        friendFragment.viewAdapter.notifyDataSetChanged();
    }

    public void updateRequestList() {
        requestFragment.viewAdapter.notifyDataSetChanged();
    }

    public void showError(String failed_to_toggle) {
        Snackbar.make(findViewById(R.id.frameLayout), failed_to_toggle, Snackbar.LENGTH_SHORT);
    }


    public void showAddFriendDialog() {
        final EditText nameEditText = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Friendname")
                .setMessage("emailadress")
                .setView(nameEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        friendList.requestFriend(nameEditText.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        replaceFragment(friendFragment);
        friendList.refresh();
        findViewById(R.id.addFriendButton).setVisibility(View.VISIBLE);
        client = LocationServices.getFusedLocationProviderClient(this);
        startLocationUpdates();
        locatorFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapready = true;
                map = googleMap;
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("FEHLER FEHLER");
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, 300);
            }
            return;
        } else {
            setupLocationProvider();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case 300:
                setupLocationProvider();
        }
    }

    @SuppressLint("MissingPermission")
    private void setupLocationProvider() {

        LocationRequest rq = new LocationRequest();
        rq.setInterval(10000);
        rq.setFastestInterval(5000);
        rq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        client.requestLocationUpdates(rq, locLoop, Looper.myLooper());
    }

    private void updateLocation(Location lastLocation) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        InternalLocation loc = new InternalLocation(lastLocation.getLongitude(), lastLocation.getLatitude());

        Log.d("Loca", "Current user id" + user.getUid());
        db.collection("users").document(user.getUid()).update("location", loc.getRep());
    }

    @Override
    public void decline(Request item) {
        String uid = item.uid;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        db.collection("users").document(user.getUid()).collection("requests").document(item.id).delete();
    }

    /*
    This should be done server -side via a rest api. but cant use google functions, soooo lets just
    do this insecure here
     */

    @Override
    public void accept(Request item) {
        String uid = item.uid;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Map<String, Object> data = new HashMap<>();
        data.put("allow", true);
        data.put("name", item.name);
        data.put("uid", uid);

        Map<String, Object> owndata = new HashMap<>();
        owndata.put("allow", true);
        owndata.put("name", user.getDisplayName());
        owndata.put("uid", user.getUid());


        db.collection("users").document(user.getUid()).collection("requests").document(item.id).delete();
        db.collection("users").document(user.getUid()).collection("friends").document(uid).set(data);
        db.collection("users").document(uid).collection("friends").document(user.getUid()).set(owndata).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.toString());
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Accepted request");
            }
        });
    }

    private LocationCallback locLoop = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            updateLocation(locationResult.getLastLocation());
        }
    };

    @Override
    public void refresh() {
        requestList.refresh();
    }
}
