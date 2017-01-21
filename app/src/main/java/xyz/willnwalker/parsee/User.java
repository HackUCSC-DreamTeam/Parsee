package xyz.willnwalker.parsee;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by willi on 1/21/2017.
 */

public class User {

    String uid;
    String displayName;
    List<User> group;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String displayName) {
        this.uid = uid;
        this.displayName = displayName;
        this.group = new ArrayList<>();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("displayName", displayName);
        result.put("group", group);

        return result;
    }
}
