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

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;
    public Database() {
        
    }

    public void set(String key, String value) {
        DatabaseReference ref = database.getReference(key);
        ref.setValue(value);
    }

    public void addListener() {
        ref.addValueEventListener(new ValueEventListener() {
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
}
