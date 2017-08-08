package com.nirwanastudio.bookingque;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.Resource;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    private String mUsername;
    //Firebase Authentication Provider
    List<AuthUI.IdpConfig> providers;

    //Firebase Database Instance
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReferences;

    TextView textName1;
    TextView textEmail1;
    ImageView imageProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsername = ANONYMOUS;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        providers = new ArrayList<>();

        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("customer");
        mChatPhotosStorageReferences = mFirebaseStorage.getReference().child("customer_photos");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final View headerView = navigationView.getHeaderView(0);
        textName1 = (TextView) headerView.findViewById(R.id.textName);
        textEmail1 = (TextView) headerView.findViewById(R.id.textEmail);
        imageProfile = (ImageView) headerView.findViewById(R.id.imageProfile);

        //Firebased Authentication Listener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null)
                {
                    // user is signed in
                    onSignedInIntialize(user.getDisplayName());
                    Toast.makeText(MainActivity.this, "Welcome to app," + user.getDisplayName().toString() , Toast.LENGTH_SHORT).show();

                    textName1.setText(user.getDisplayName());
                    textEmail1.setText(user.getEmail());

                    Picasso.with(getApplicationContext())
                            .load(user.getPhotoUrl())
                            .into(imageProfile);

                }
                else
                {
                    // user is signed out
                    onSignOutCleanUp();

                    textEmail1.setText("");
                    textName1.setText("Anonymous");
                    providers.clear();
                    providers.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
                    providers.add(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

                    startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(providers)
                            .build(),RC_SIGN_IN);

                }
            }
        };



    }

    private void onSignedInIntialize(String username)
    {
        mUsername = username;

        attachDatabaseReadListener();
    }

    private void onSignOutCleanUp()
    {
        mUsername = ANONYMOUS;


    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null)
        {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    // Cast the value to
                    // Fill the data here
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }


    private void detachDatabaseReadListener(){
        if (mChildEventListener !=null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN)
        {
            if (resultCode == RESULT_OK)
            {
                Toast.makeText(this, "Signed in !", Toast.LENGTH_SHORT).show();

            }
            else if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "Sign in canceled !", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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

        if (id == R.id.nav_profile) {
            ProfileFragment fragment = new ProfileFragment();
            FragmentManager  manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.mainLayout, fragment).commit();
        } else if (id == R.id.nav_balance)
        {
            BalanceFragment fragment = new BalanceFragment();
            FragmentManager  manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.mainLayout, fragment).commit();

        }
        else if (id == R.id.nav_history)
        {
            HistoryFragment fragment = new HistoryFragment();
            FragmentManager  manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.mainLayout, fragment).commit();
        }
        else if (id == R.id.nav_bookingnow)
        {
            BookingFragment bookingFragment = new BookingFragment();
            FragmentManager  manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.mainLayout, bookingFragment).commit();
        }
        else if (id == R.id.nav_share)
        {

        }
        else if (id == R.id.nav_logout)
        {
            AuthUI.getInstance().signOut(this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
