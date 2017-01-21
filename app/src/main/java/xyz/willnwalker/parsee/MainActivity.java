package xyz.willnwalker.parsee;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.storage.FirebaseStorage;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "parsee.mainactivity";
    private static final int LOGIN_ACCOUNT = 2148;
    private FirebaseAuth firebaseAuth;
    private MyAuthStateListener authStateListener;
    private MapView mapView;
    private Database database;
    private TextView user_name;
    private LocationManager locationManager;
    private FirebaseStorage firebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapboxAccountManager.start(this, "pk.eyJ1Ijoid2lsbG53YWxrZXIiLCJhIjoiY2l5NzU5YWw0MDAycjMzbzZtbnIycWFvbyJ9.bze7QA84drv6yb37eK8xqg");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        user_name = (TextView) findViewById(R.id.user_name);

        //Location init
        /*locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(),location.getLongitude())).zoom(17).bearing(180).build();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };*/

        //Firebase init
        //database = new Database();
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new MyAuthStateListener();

        //Mapbox init
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                // Customize map with markers, polylines, etc.


            }
        });
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
                                database = new Database();
                                database.createUser(firebaseAuth.getCurrentUser().getUid(),b.getString("displayName"));
                            }
                            else{
                                Log.d(TAG,"new account not created!");
                                Log.d(TAG,task.getException().toString());
                            }
                        }


                    });
                    /*String key = databaseReference.child("USERS").push().getKey();
                    User user = new User(firebaseAuth.getCurrentUser().getUid(),b.getString("displayName"));
                    Map<String, Object> userValues = user.toMap();

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/USERS/"+key,userValues);
                    databaseReference.updateChildren(childUpdates);*/
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
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                //User userData = database.getUser(firebaseAuth.getCurrentUser().getUid());
                //user_name.setText(userData.displayName);
                //String displayName = databaseReference.child(user.getUid());
                //Log.d(TAG,displayName);
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
                startActivityForResult(new Intent(getApplicationContext(),LoginActivity.class), LOGIN_ACCOUNT);
            }
        }

        AtomicBoolean added = new AtomicBoolean(false);
    }
}