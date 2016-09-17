package com.example.bhavya.helpme;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.security.Provider;

import butterknife.BindView;
import butterknife.OnClick;

import static android.content.Context.LOCATION_SERVICE;

/**q
 * Created by bhavya on 16/9/16.
 */
public class ContactsDefaultFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    @BindView(R.id.contact_emergency_default_textview)
    TextView mEmergencyContactNo;
//    @BindView(R.id.contact_emergency_default_button) Button mEmergencyContactButton;

    Button mShowMapButton;
    Button mEmergencyContactButton;
    TextView mEmergencyContactTextView;
    public static SharedPreferences preferences;
    GoogleApiClient mClient;
    public static String lattitude = "-1";
    public static String longitude = "-1";


    // Empty constructor required
    public ContactsDefaultFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_default, container, false);
        Bundle args = getArguments();
        mEmergencyContactTextView = (TextView) view.findViewById(R.id.contact_emergency_default_textview);
        if (null != args) {
            mEmergencyContactTextView.setText("Your selected contact is " + args.getSerializable("emergencyContact"));
        } else {
            mEmergencyContactTextView.setText("No contact specified");
        }

        mShowMapButton = (Button) view.findViewById(R.id.contact_emergency_show_map_button);
        mShowMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),MapsActivity.class);
                intent.putExtra("lat",lattitude);
                intent.putExtra("long",longitude);
                startActivity(intent);
            }
        });


        mEmergencyContactButton = (Button) view.findViewById(R.id.contact_emergency_default_button);
        mEmergencyContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
                // Define the criteria how to select the locatioin provider -> use
                // default
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, false);
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location location = locationManager.getLastKnownLocation(provider);
                if(location!=null){
                    lattitude = String.valueOf(location.getLatitude());
                    longitude = String.valueOf(location.getLongitude());
                    SharedPreferences preferences = getActivity().getSharedPreferences("HelpMe",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("lattitude",lattitude);
                    editor.putString("longitude",longitude);
                    editor.commit();
                    //Toast.makeText(getActivity(),"lat "+lattitude+"long "+longitude,Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(getActivity(),"No location found",Toast.LENGTH_SHORT).show();
                    startLocationUpdate();
                }

                //TODO: Write code to send sms with text "Help me http://maps.google.com/maps?f=q&q=(lat,long) "

                }
            });

        // Initialize location services
        if (mClient == null) {
            mClient = new GoogleApiClient.Builder(getActivity()).
                    addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        return view;
    }

    @Override
    public void onStart() {
        mClient.connect();
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        mClient.disconnect();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!checkPermission())
            return;
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getActivity(), "failed " + i, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "failed " + connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
    }


    private void startLocationUpdate() {
        final LocationRequest lr = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(lr);
        LocationServices.SettingsApi.checkLocationSettings(mClient, builder.build())
                .setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                        int statusCode = locationSettingsResult.getStatus().getStatusCode();
                        if (statusCode == LocationSettingsStatusCodes.SUCCESS) {
                            if(!checkPermission())
                                return;

                            LocationServices.FusedLocationApi.requestLocationUpdates(mClient,lr,ContactsDefaultFragment.this);
                        } else if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                locationSettingsResult.getStatus()
                                        .startResolutionForResult(getActivity(), 1);
                            } catch (IntentSender.SendIntentException e) {
                                // ignore
                            }
                        } else {
                            Toast.makeText(getActivity(), "Could not fetch Location", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                startLocationUpdate();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
        Update location in static variable
     */
    @Override
    public void onLocationChanged(Location location) {
        lattitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
        LocationEvent le = new LocationEvent();
        le.Lattitude = lattitude;
        le.Longitude = longitude;
        EventBus.getDefault().post(le);
    }

    /*
        Code to get location and save it in shared preferences
     */
    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        return true;
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getLocation(LocationEvent le){
        //Toast.makeText(getActivity(),le.Lattitude+" "+le.Longitude,Toast.LENGTH_SHORT).show();
    }

}
