package com.autochip.trackpro;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import app_utility.CircularProgressBar;
import app_utility.PermissionHandler;
import app_utility.SharedPreferenceClass;
import app_utility.User;

import static app_utility.PermissionHandler.LOCATION_PERMISSION;
import static app_utility.PermissionHandler.WRITE_PERMISSION;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    CircularProgressBar circularProgressBar;
    private int nPermissionFlag = 0;
    /*
     * Id to identity READ_CONTACTS permission request.
     */
    //private static final int REQUEST_READ_CONTACTS = 0;

    /*
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    /*private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };*/
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    // UI references.
    public DatabaseReference mDatabase;
    private EditText etUserID, etName;
    private Button btnLogin;
    private int WHO_IS_USER = 0; //0 = admin, 1 = trackee
    SharedPreferenceClass sharedPreferenceClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedPreferenceClass = new SharedPreferenceClass(LoginActivity.this);
        circularProgressBar = new CircularProgressBar(this);
        if (isUserLoggedIn()) {
            Intent in = new Intent(LoginActivity.this, SettingsActivity.class);
            startActivity(in);
            finish();
        }
        etName = findViewById(R.id.et_name);
        etUserID = findViewById(R.id.et_ID);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private boolean isUserLoggedIn() {
        return sharedPreferenceClass.getUserLogStatus();
    }


    private boolean isUserIDValid(String ID) {
        return ID.length() == 10;
    }

    private boolean isNameValid(String name) {
        return name.length() > 2 && !TextUtils.isEmpty(name);
    }

    private void login() {
        String ID = etUserID.getText().toString();
        String name = etName.getText().toString();
        if (isUserIDValid(ID) && isNameValid(name)) {
            showProgressBar();
            /*Intent in = new Intent(LoginActivity.this, SettingsActivity.class);
            //in.putExtra("user", WHO_IS_USER);
            startActivity(in);
            sharedPreferenceClass.setUserLogStatus(true, name, ID);
            sharedPreferenceClass.setUserType(WHO_IS_USER);*/
            checkUserInDatabase(ID, name);
        }
    }

    private void checkUserInDatabase(final String phone, final String name) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isPresent = dataSnapshot.hasChild(phone);
                        if (isPresent) {
                            //Get map of users in dataSnapshot
                            String value = (String) dataSnapshot.child(phone).child("userType").getValue();
                            WHO_IS_USER = Integer.valueOf(value);
                            sharedPreferenceClass.setUserType(WHO_IS_USER);
                            sharedPreferenceClass.setUserLogStatus(true, name, phone);

                            Intent in = new Intent(LoginActivity.this, SettingsActivity.class);
                            startActivity(in);
                            finish();
                        } else {
                            writeNewUser(phone, name);
                        }
                        stopProgressBar();
                        //collectPhoneNumbers((Map<String,Object>) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        stopProgressBar();
                        //handle databaseError
                    }

                });
        /*mDatabase.addChildEventListener(new ChildEventListener() {
            // Retrieve new posts as they are added to Firebase
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newPost = (Map<String, Object>) snapshot.getValue();
                System.out.println("Author: " + newPost.get("author"));
                System.out.println("Title: " + newPost.get("title"));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
            //... ChildEventListener also defines onChildChanged, onChildRemoved,
            //    onChildMoved and onCanceled, covered in later sections.
        });*/
    }

    private void writeNewUser(final String phone, final String name) {
        User user = new User(phone, name, "0.0", "1");

        mDatabase.child("users").child(phone).setValue(user);

        mDatabase.child("users").child(phone).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        WHO_IS_USER = 1;
                        sharedPreferenceClass.setUserType(WHO_IS_USER);
                        sharedPreferenceClass.setUserLogStatus(true, name, phone);

                        Intent in = new Intent(LoginActivity.this, SettingsActivity.class);
                        startActivity(in);
                        finish();
                        //Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(LoginActivity.this, "failed", Toast.LENGTH_SHORT).show();
                        // Write failed
                        // ...
                    }
                });
    }

    private void showProgressBar() {
        circularProgressBar.setCanceledOnTouchOutside(false);
        circularProgressBar.setCancelable(false);
        circularProgressBar.show();
    }

    private void stopProgressBar() {
        if (circularProgressBar != null && circularProgressBar.isShowing())
            circularProgressBar.dismiss();
    }

    @Override
    public void onStart(){
        super.onStart();
        if (!PermissionHandler.hasPermissions(LoginActivity.this, WRITE_PERMISSION)) {
            ActivityCompat.requestPermissions(LoginActivity.this, WRITE_PERMISSION, 1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int PERMISSION_ALL, String permissions[], int[] grantResults) {
        StringBuilder sMSG = new StringBuilder();
        if(PERMISSION_ALL==1) {
            for (String sPermission : permissions) {
                switch (sPermission) {
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                //Show permission explanation dialog...
                                //showPermissionExplanation(SignInActivity.this.getResources().getString(R.string.phone_explanation));
                                //Toast.makeText(SignInActivity.this, "not given", Toast.LENGTH_SHORT).show();
                                sMSG.append("LOCATION, ");
                                nPermissionFlag = 0;
                            } else {
                                //Never ask again selected, or device policy prohibits the app from having that permission.
                                //So, disable that feature, or fall back to another situation...
                                //@SuppressWarnings("unused") AlertDialogs alertDialogs = new AlertDialogs(HomeScreen.this, 1, mListener);
                                //Toast.makeText(SignInActivity.this, "permission never ask", Toast.LENGTH_SHORT).show();
                                //showPermissionExplanation(HomeScreenActivity.this.getResources().getString(R.string.phone_explanation));
                                sMSG.append("LOCATION, ");
                                nPermissionFlag = 0;
                            }
                        }
                        break;

                }
            }
            if (!sMSG.toString().equals("") && !sMSG.toString().equals(" ")) {
                PermissionHandler permissionHandler = new PermissionHandler(LoginActivity.this, 0, sMSG.toString(), nPermissionFlag);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 3:if (resultCode != Activity.RESULT_OK) {
                LoginActivity.this.finish();
            }
                break;
        }
    }

    /*private void collectPhoneNumbers(Map<String,Object> users) {

        ArrayList<String> phoneNumbers = new ArrayList<>();

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : users.entrySet()){

            //Get user map
            Map singleUser = (Map) entry.getValue();
            //Get phone field and append to list
            phoneNumbers.add(String.valueOf(singleUser.get("phone")));
        }

        System.out.println(phoneNumbers.toString());
    }*/
}

