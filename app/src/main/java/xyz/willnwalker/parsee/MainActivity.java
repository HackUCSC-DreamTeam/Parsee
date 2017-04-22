package xyz.willnwalker.parsee;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "parsee.mainactivity";
    private static final int LOGIN_ACCOUNT = 2148; //Code for login activity
    private static final int PERMISSIONS_LOCATION = 0;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private MyAuthStateListener authStateListener;
    private MapboxMap map;
    private LocationServices locationServices;
    private ArrayList<MarkerViewOptions> markers;

    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.mapView) MapView mapView;
    //@BindView(R.id.user_name) TextView user_name; //Broken, due to including in layout file. Maybe it'll work, someday.
    //@BindView(R.id.user_email) TextView user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, "pk.eyJ1Ijoid2lsbG53YWxrZXIiLCJhIjoiY2l5NzU5YWw0MDAycjMzbzZtbnIycWFvbyJ9.bze7QA84drv6yb37eK8xqg");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        locationServices = LocationServices.getLocationServices(this);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        //Firebase init
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new MyAuthStateListener();

        //Mapbox init
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                // Customize map with markers, polylines, etc.
                map = mapboxMap;
                toggleGps(!map.isMyLocationEnabled());
            }
        });

        markers = new ArrayList<>();
    }

    //Click Handlers
    @OnClick(R.id.fab)
    public void fabOnClick(View v) {
        if(map!=null){
            toggleGps(!map.isMyLocationEnabled());
        }
    }

    private void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            if (!locationServices.areLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
            } else {
                enableLocation(true);
            }
        } else {
            enableLocation(false);
        }
    }

    private void enableLocation(boolean enabled) {
        if (enabled) {
            // If we have the last location of the user, we can move the camera to that position.
            Location lastLocation = locationServices.getLastLocation();
            if (lastLocation != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
            }

            locationServices.addLocationListener(   new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Move the map camera to where the user location is and then remove the
                        // listener so the camera isn't constantly updating when the user location
                        // changes. When the user disables and then enables the location again, this
                        // listener is registered again and will adjust the camera once again.
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
                        if(firebaseAuth.getCurrentUser()!=null){
                            DatabaseReference userlocation = firebaseDatabase.getReference("USERS").child(firebaseAuth.getCurrentUser().getUid()).child("lastknownlocation");
                            userlocation.setValue(location.getLatitude()+" "+location.getLongitude());
                        }
                        locationServices.removeLocationListener(this);
                    }
                }
            });
            fab.setImageResource(R.drawable.ic_my_location);
        } else {
            fab.setImageResource(R.drawable.ic_location_disabled);
        }
        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation(true);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!authStateListener.added.get()) {
            firebaseAuth.addAuthStateListener(authStateListener);
            authStateListener.added.set(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
//        if (authStateListener != null) {
//            firebaseAuth.removeAuthStateListener(authStateListener);
//        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_addfriend) {
            drawer.closeDrawer(GravityCompat.START);
            new MaterialDialog.Builder(this).title("Add Friend").content("Enter your friend's username in the box below.").positiveText("OK").negativeText("Cancel").input("Username", "", new MaterialDialog.InputCallback() {
                @Override
                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    firebaseDatabase.getReference("REVERSE_USERS").child(input.toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String friended_uid = (String) dataSnapshot.getValue();
                            if(friended_uid!=null){
                                firebaseDatabase.getReference("USERS").child(firebaseAuth.getCurrentUser().getUid()).child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String current_username = (String) dataSnapshot.getValue();
                                        if(current_username!=null){
                                            firebaseDatabase.getReference("USERS").child(friended_uid).child("friend_requests").child(firebaseAuth.getCurrentUser().getUid()).setValue(current_username);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"User does not exist!",Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }).show();
        } else if (id == R.id.nav_friendrequests) {
            final String userid = firebaseAuth.getCurrentUser().getUid();
            Query FriendRequests = firebaseDatabase.getReference("USERS").child(firebaseAuth.getCurrentUser().getUid()).child("friend_requests").orderByKey();
            if(FriendRequests.equals(null)){
                Toast.makeText(this,"You have no pending Friend Requests.",Toast.LENGTH_LONG).show();
            }
            FriendRequests.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        Log.d(TAG,postSnapshot.toString());
                        doDataStuff(userid, postSnapshot);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else if (id == R.id.nav_friends) {
            Intent i = new Intent(this,FriendActivity.class);
            startActivity(i);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == LOGIN_ACCOUNT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                final Bundle b = data.getExtras();
                if(b.getBoolean("newAccount")){
                    Log.d(TAG,"it was a new account:");
                    Log.d(TAG,"username: "+b.getString("username"));
                    Log.d(TAG,"password: "+b.getString("password"));
                    firebaseAuth.createUserWithEmailAndPassword(b.getString("username"),b.getString("password")).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG,"new account created!");
                                DatabaseReference user = firebaseDatabase.getReference("USERS").child(firebaseAuth.getCurrentUser().getUid());
                                user.child("displayName").setValue(b.getString("displayName"));
                                user.child("email").setValue(b.getString("username"));
                                firebaseDatabase.getReference("REVERSE_USERS").child(b.getString("displayName")).setValue(firebaseAuth.getCurrentUser().getUid());
                            }
                            else{
                                Log.d(TAG,"new account not created!");
                                Log.d(TAG,task.getException().toString());
                                System.exit(-1);
                            }
                        }


                    });
                }
                else{
                    firebaseAuth.signInWithEmailAndPassword(b.getString("username"),b.getString("password"));
                }
            }
            else{
                //User must've clicked the back button, exit in this case
                finish();
            }
        }
    }

    public boolean logout(MenuItem item){
        FirebaseAuth.getInstance().signOut();
        return true;
    }

    private class MyAuthStateListener implements FirebaseAuth.AuthStateListener {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if(map!=null){
                map.clear(); //Ensure past data isn't left on the map
            }
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                DatabaseReference db = firebaseDatabase.getReference("USERS").child(firebaseAuth.getCurrentUser().getUid());
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                DatabaseReference username = db.child("displayName");
                username.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String received = (String) dataSnapshot.getValue();
                        if(received!=null&&(!received.equals(""))){
                            ((TextView)findViewById(R.id.user_name)).setText(received);
                            //user_name.setText(received);
                            Log.d(TAG,received);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                DatabaseReference useremail = db.child("email");
                useremail.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String received = (String) dataSnapshot.getValue();
                        if(received!=null&&(!received.equals(""))){
                            ((TextView)findViewById(R.id.user_email)).setText(received);
                            //user_email.setText(received);
                            Log.d(TAG,received);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Query friends = db.child("friends").orderByKey();
                friends.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                            Log.d(TAG,postSnapshot.getKey());
                            final String user = (String)postSnapshot.getValue();
                            firebaseDatabase.getReference("USERS").child(postSnapshot.getKey()).child("lastknownlocation").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.getValue()!=null){
                                        Log.d(TAG,dataSnapshot.toString());
                                        String[] s = dataSnapshot.getValue().toString().split(" ");
                                        Double lat = Double.parseDouble(s[0]);
                                        Double lon = Double.parseDouble(s[1]);
                                        MarkerViewOptions markerViewOptions = new MarkerViewOptions().position(new LatLng(lat,lon)).title(user);
                                        for(int i=0;i<markers.size();i++){
                                            if(markers.get(i).getTitle().equals(user)){
                                                map.removeMarker(markers.get(i).getMarker());
                                                markers.remove(i);
                                                markers.add(i,markerViewOptions);
                                                map.addMarker(markerViewOptions);
                                            }
                                        }
                                        markers.add(markerViewOptions);
                                        if(map!=null){
                                            map.addMarker(markerViewOptions);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
                startActivityForResult(new Intent(getApplicationContext(),LoginActivity.class), LOGIN_ACCOUNT);
            }
        }

        AtomicBoolean added = new AtomicBoolean(false);
    }

    private void doDataStuff(final String userid, final DataSnapshot postSnapshot){
        new MaterialDialog.Builder(this).title("Friend Request")
                .content(postSnapshot.getValue()+" would like to be your friend.")
                .positiveText("Accept").negativeText("Deny").neutralText("Ignore")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        postSnapshot.getRef().setValue(null);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final DatabaseReference stuff = firebaseDatabase.getReference("USERS");
                        stuff.child(userid).child("friends").child(postSnapshot.getKey()).setValue(postSnapshot.getValue());
                        stuff.child(userid).child("displayName").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                stuff.child(postSnapshot.getKey()).child("friends").child(userid).setValue(dataSnapshot.getValue());
                                postSnapshot.getRef().setValue(null);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }).show();
    }
}