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
import java.util.Map;

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
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, User>> t = new GenericTypeIndicator<Map<String, User>>() {};
                Map<String, User> users = dataSnapshot.getValue(t);
                List<User> listUsers = new ArrayList<User>();
                for(Map.Entry<String, User> entries: users.entrySet()) {
                    listUsers.add(entries.getValue());
                }
                Log.d("TEST1", listUsers.toString());

                setUsers(listUsers);
//                Object object = dataSnapshot.getValue();
//                Log.d("TEST1", object.toString());
//                if(object instanceof Map) {
//                    Map<String, User> map = object.get
//                };

//                GenericTypeIndicator<Map<Object, Object>> t = new GenericTypeIndicator<Map<Object, Object>>() {};
//                Map<Object, Object> users = dataSnapshot.getValue(t);
//
//                for(Map.Entry<Object, Object> entries: users.entrySet()) {
//                    Log.d("MAPTEST", entries.getKey() + " : " + entries.getValue());
//                }
////                    setUsers(users);
//                Log.d("Database Test", getUsers().toString());
////                GenericTypeIndicator<List<User>> t = new GenericTypeIndicator<List<User>>() {};
////                Log.d("SINGLE_LISTENER", users.toString() + " " + getUsers().size());
////
////                List<User> users = dataSnapshot.getValue(t);
////                setUsers(users);
////                Log.d("Database Test", getUsers().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, User>> t = new GenericTypeIndicator<Map<String, User>>() {};
                Map<String, User> users = dataSnapshot.getValue(t);
                List<User> listUsers = new ArrayList<User>();
                for(Map.Entry<String, User> entries: users.entrySet()) {
                    listUsers.add(entries.getValue());
                }
                Log.d("TEST1", listUsers.toString());

                setUsers(listUsers);
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
