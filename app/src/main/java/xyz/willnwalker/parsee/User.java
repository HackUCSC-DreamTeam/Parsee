package xyz.willnwalker.parsee;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 1/21/2017.
 */

public class User {
    String firstName;
    String lastName;
    List<User> group;

    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.group = new ArrayList<>();
    }
}
