package xyz.willnwalker.parsee;

import android.location.Location;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by willi on 1/21/2017.
 */

@IgnoreExtraProperties
public class User {

    public String uid;
    public String displayName;
    public String testString;
    public List<User> friends;
    public Location myLocation;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String displayName) {
        this.uid = uid;
        this.displayName = displayName;
        this.testString = "99";
        this.friends = new ArrayList<>();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("testString", testString);
        result.put("displayName", displayName);
        result.put("friends", friends);
        result.put("location", myLocation);

        return result;
    }

    @Override
    public String toString() {
        return "id: " + this.uid + " name: " + this.displayName;
    }
}
