package com.example.bhavya.helpme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    SharedPreferences preferences;
    public static final String SHARED_PREF_FILE = "HelpMe";
    public static final String SHARED_PREF_KEY = "emergencyContact";
    public static final String SHARED_PREF_INVALID = "-1";
    public String LATTITUDE = "-1";
    public String LONGITUDE = "-1";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        /*
            Navigation drawer
         */
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        /*
            Shared preferences
         */
        preferences = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);

        if (preferences.getString(SHARED_PREF_KEY, SHARED_PREF_INVALID).length() <= 0) {
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString(SHARED_PREF_KEY, SHARED_PREF_INVALID);
            edit.apply();
        }

        /*
            Listen for changes in shared preferences
         */
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key == SHARED_PREF_KEY) {
                    initViews(preferences.getString(SHARED_PREF_KEY, ""));
                }
            }

        };
        preferences.registerOnSharedPreferenceChangeListener(prefListener);
        String value = preferences.getString(SHARED_PREF_KEY, "");
        initViews(value);

    } // end onCreate()

    /*
    *
    *  Initialize home screen with contacts fragment
    *
     */
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void initViews(String value) {
        if (value.equals("-1")) {
            ContactsEmptyFragment fragment = new ContactsEmptyFragment();
            getFragmentManager().beginTransaction().replace(R.id.contactFragmentContainer, fragment)
                    .commit();
        } else {
            ContactsDefaultFragment fragment = new ContactsDefaultFragment();
            Bundle b = new Bundle();
            b.putSerializable(SHARED_PREF_KEY, value);
            fragment.setArguments(b);
            getFragmentManager().beginTransaction().replace(R.id.contactFragmentContainer, fragment)
                    .commit();
        }
    }  // end initViews


    /*
    *
    *
    * Code for FAB
    *
    *
     */
    @OnClick(R.id.fab)
    public void fabInput(View view) {
        new MaterialDialog.Builder(this)
                .title(R.string.input_title)
                .content(R.string.input_content)
                .inputRangeRes(10, 10, R.color.material_red_500)
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                        String regex = "\\d+";
                        if (input.toString().matches(regex)) {
                            preferences = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = preferences.edit();
                            edit.putString(SHARED_PREF_KEY, input.toString());
                            edit.commit();
                        } else {
                            Toast.makeText(MainActivity.this, "Please enter only digits", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .positiveText(R.string.input_agree)
                .negativeText(R.string.input_disagree)
                .show();
    } // end fabInput()


    /*
    *
    *
    *
    *  Code for navigation drawer and menu
    *
    *
    *
    */
    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        switch (item.getItemId()) {
            case R.id.nav_contacts:
                String value = preferences.getString(SHARED_PREF_KEY, "");
                initViews(value);
                break;
            case R.id.nav_police:
                break;
            case R.id.nav_map:
                if (LATTITUDE.equals("-1") || LONGITUDE.equals("-1")) {
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    String provider = locationManager.getBestProvider(criteria, false);
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return false;
                    }
                    Location location = locationManager.getLastKnownLocation(provider);
                    if(location == null){
                        Toast.makeText(this,"No recent location",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    LATTITUDE = String.valueOf(location.getLatitude());
                    LONGITUDE = String.valueOf(location.getLongitude());
                    Toast.makeText(this,"No location specified",Toast.LENGTH_SHORT).show();
                }
                    Intent intent = new Intent(this,MapsActivity.class);
                    intent.putExtra("lat",LATTITUDE);
                    intent.putExtra("long",LONGITUDE);
                    startActivity(intent);

                break;
            case R.id.nav_tips:
                break;
            case R.id.nav_developer:
                break;
            case R.id.nav_open_source:
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    } // end code for navigation drawer

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getLocation(LocationEvent le){
        Toast.makeText(this,le.Lattitude+" "+le.Longitude,Toast.LENGTH_SHORT).show();
        LATTITUDE = le.Lattitude;
        LONGITUDE = le.Longitude;
    }

}
