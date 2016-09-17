package com.example.bhavya.helpme;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public static LatLng mLatLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng you = new LatLng(-1,-1);

        if(getIntent()!=null){
             Double lattitude = Double.parseDouble(getIntent().getStringExtra("lat"));
             Double longitude = Double.parseDouble(getIntent().getStringExtra("long"));
            you = new LatLng(lattitude,longitude);
        }
        if(you.latitude == -1.0 && you.longitude == -1.0){
            SharedPreferences preferences = getSharedPreferences("HelpMe", Context.MODE_PRIVATE);
            Double lattitude  = Double.parseDouble(preferences.getString("lattitude",""));
            Double longitude  = Double.parseDouble(preferences.getString("longitude",""));
            you = new LatLng(lattitude,longitude);
        }

        mMap.addMarker(new MarkerOptions().position(you).title("You"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(you));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 11.0f ) );
    }

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getLocation(LocationEvent le){
        mLatLng = new LatLng(Double.parseDouble(le.Lattitude),Double.parseDouble(le.Longitude));
    }
}
