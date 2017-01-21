package xyz.willnwalker.parsee;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by willi on 1/21/2017.
 */

public class Database {

    FirebaseDatabase database;
    DatabaseReference mDatabase;

    final String KEY = "USERS";

    public Database() {
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(KEY);
        addListener();
    }

    public User createUser(String uid, String displayName) {
        User user = new User(uid, displayName);
        mDatabase.child(uid).setValue(user);
        return user;
    }

    public void getUser(String uid) {
    }

    public void deleteUser(String uid) {
        mDatabase.child(uid).removeValue();
    }


    public void addListener() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                Log.d("value", value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("cancel", "Error: " + databaseError.toException());
            }
        });
    }

    public void removeListeners() {

    }
}
