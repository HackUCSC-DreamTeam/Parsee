package xyz.willnwalker.parsee;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**

 Database database = new Database();
 database.createUser("0", "Will");
 database.createUser("1", "Kreg");

 * Created by willi on 1/21/2017.
 */

public class Database {

    Database instance;
    FirebaseDatabase database;
    DatabaseReference mDatabase;

    final String KEY = "USERS";

    volatile List<User> users;

    public Database() {
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(KEY);
        users = new ArrayList<>();
        addListener();
    }

    public User createUser(String uid, String displayName) {
        User user = new User(uid, displayName);
        mDatabase.child(uid).setValue(user);
        return user;
    }

    public void deleteUser(String uid) {
        mDatabase.child(uid).removeValue();
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<User> getUsers() {
        return users;
    }

    public User getUser(String uid) {
        Log.d("getUser", uid + " " + getUsers().size());

        int i = 0;
        for(User user: getUsers()) {
            if(user.uid.equals(uid)) {
                return user;
            }
        }
        return null;
    }

    private void addListener() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<User>> t = new GenericTypeIndicator<List<User>>() {};

                List<User> users = dataSnapshot.getValue(t);
                setUsers(users);
                Log.d("Listener", users.toString() + " " + getUsers().size());
                Log.d("Database Test", getUser("0") + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("cancel", "Error: " + databaseError.toException());
            }
        });
    }

    public void removeListeners() {

    }

    public Database getInstance() {
        return instance;
    }
}
