package app_utility;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String phone;
    public String location;
    public String userType;
    public String name;
    public boolean notify;
    public boolean adminPermission;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String phone, String location) {
        this.phone = phone;
        this.location=  location;
    }

    public User(String phone, String name, String location, String userType) {
        this.phone = phone;
        this.name = name;
        this.location=  location;
        this.userType = userType;
    }

    public User(String phone, String name, String location, String userType, boolean notify, boolean adminPermission) {
        this.phone = phone;
        this.name = name;
        this.location=  location;
        this.userType = userType;
        this.notify = notify;
        this.adminPermission = adminPermission;
    }

    public User(String userType){
        this.userType = userType;
    }

    public User(String location, int value){
        this.location = location;
    }

}