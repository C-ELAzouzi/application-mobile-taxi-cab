package com.example.apptaxi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomersMapsActivity extends FragmentActivity implements OnMapReadyCallback , OnClickListener
    , GoogleApiClient.ConnectionCallbacks
    ,GoogleApiClient.OnConnectionFailedListener
    ,com.google.android.gms.location.LocationListener
{

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Marker marker;
    private FirebaseAuth mAuth;
    private String userId;
    private Button logOut_btn;
    private Button call_driver_btn,cancel_driver_ride,settings_btn;
    private Location lastLocation;
    private DatabaseReference DriversAvailableReference;
    private String UserId;
    private GeoLocation geoLocation;
    private int radius=1;
    private boolean driverFound=false;
    private DatabaseReference driversRef;
    private String costumerId;
    private  DatabaseReference DriversLocationReference;
    private Marker driverMarker;
    private  DatabaseReference databaseReference;
    private boolean RequestType=false;
    private GeoQuery geoQuery;
    private ValueEventListener valueEventListener;
    private TextView txtName, txtPhone, txtCarName;
    private CircleImageView profilePic;
    private RelativeLayout relativeLayout;
    private List<Marker>markerList;
    private  boolean GetAllDrivers=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_maps);
        cancel_driver_ride=findViewById(R.id.cancel_ride_driver);
        mAuth=FirebaseAuth.getInstance();
        costumerId=mAuth.getCurrentUser().getUid();
        logOut_btn=(Button)findViewById(R.id.logout_customer_btn);
        call_driver_btn=(Button)findViewById(R.id.call_a_car_button);
        settings_btn=(Button)findViewById(R.id.settings_customer_btn);
        settings_btn.setOnClickListener(this);
        cancel_driver_ride.setOnClickListener(this);
        logOut_btn.setOnClickListener(this);
        call_driver_btn.setOnClickListener(this);
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Customer Request");
        DriversAvailableReference=FirebaseDatabase.getInstance().getReference().child("Driver Available");
        DriversLocationReference=FirebaseDatabase.getInstance().getReference().child("Drivers working");
        txtName = findViewById(R.id.name_driver);
        txtPhone = findViewById(R.id.phone_driver);
        txtCarName = findViewById(R.id.car_name_driver);
        profilePic = findViewById(R.id.profile_image_driver);
        relativeLayout = findViewById(R.id.rel1);
        markerList=new ArrayList<Marker>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
    }
    public void GetAllDrivers()
    {
        GeoFire geoFire=new GeoFire(DriversAvailableReference);
        geoLocation=new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude());
        geoQuery=geoFire.queryAtLocation(geoLocation,1000);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

             for(Marker marker :markerList) {
                 if (marker.getTag().equals(key)) {
                     return;
                 }
             }
                 LatLng latLng=new LatLng(location.latitude,location.longitude);
                 Marker marker1=mMap.addMarker(new MarkerOptions().position(latLng).title(key).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                 marker1.setTag(key);
                 markerList.add(marker1);

            }
            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onClick(View v) {
            if(v==logOut_btn)
            {
                mAuth.signOut();
                logOut();
            }
            if(v==cancel_driver_ride)
            {
                if(geoQuery!=null)
                {
                    geoQuery.removeAllListeners();
                }
                DriversLocationReference.removeEventListener(valueEventListener);
                if(driverFound)
                {
                    driversRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("CustomerRideId");
                    driversRef.removeValue();
                    userId=null;
                }
                driverFound=false;
                radius=1;
                costumerId=mAuth.getCurrentUser().getUid();
                GeoFire geoFire=new GeoFire(databaseReference);
                geoFire.removeLocation(costumerId);
                if(driverMarker!=null)
                {
                    driverMarker.remove();
                }
                call_driver_btn.setText("call a taxi");
                cancel_driver_ride.setVisibility(View.INVISIBLE);
            }
            if(v==call_driver_btn)
            {
                    double locationlat=1.8503056232312476;
                    double locationlin=50.96251106110639;
                    LatLng latLng1=new LatLng(lastLocation.getLatitude(),lastLocation.getAltitude());
                    MarkerOptions markerOptions=new MarkerOptions();
                    markerOptions.position(latLng1);
                    markerOptions.title("user current location");
                    marker=mMap.addMarker(markerOptions);
                    costumerId=mAuth.getCurrentUser().getUid();
                    DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("Customer Request");
                    GeoFire geoFire=new GeoFire(databaseReference);
                    geoFire.setLocation(costumerId,new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude()));
                    call_driver_btn.setText("searching......");
                    LatLng latLng=new LatLng(locationlat,locationlin);

                    driverMarker=mMap.addMarker(new MarkerOptions().position(latLng).title("your driver is here").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                    marker=mMap.addMarker(new MarkerOptions().position(latLng1).title("my position").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
                    Location locationCustomer=new Location("");
                    locationCustomer.setAltitude(latLng1.latitude);
                    locationCustomer.setLongitude(latLng1.longitude);
                    Location locationDriver=new Location("");
                    locationDriver.setAltitude(latLng.latitude);
                    locationDriver.setLongitude(latLng.longitude);
                    double distance=locationCustomer.distanceTo(locationDriver);
                    distance=distance/100000;
                    call_driver_btn.setText("driver found  "+distance+"km");
                    cancel_driver_ride.setVisibility(View.VISIBLE);
                    relativeLayout.setVisibility(View.VISIBLE);
                    //getAssignedDriverInformation();





                    //FindClosestDriverId();
            }
            if(v==settings_btn)
            {
                Intent intent=new Intent(getApplicationContext(),SeetingActivity.class);
                intent.putExtra("type","Custumers");
                startActivity(intent);
            }
    }


    private void logOut() {
        Intent intent=new Intent(CustomersMapsActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest=new LocationRequest();
        locationRequest.setFastestInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation=location;
        if(!GetAllDrivers)
        {
            GetAllDrivers();
            GetAllDrivers=true;
        }
        LatLng latLng=new LatLng(location.getLatitude(),location.getAltitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        if(googleApiClient!=null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        }


    }
    private  synchronized void buildGoogleApiClient()
    {
        googleApiClient=new GoogleApiClient.Builder(this).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).
                addApi(LocationServices.API).build();
        googleApiClient.connect();
    }
    private void removeUserFromDataBase()
    {
        userId=mAuth.getCurrentUser().getUid();
        GeoFire geoFire=new GeoFire(databaseReference);
        geoFire.removeLocation(userId);
    }
    private void FindClosestDriverId() {
        GeoFire geoFire=new GeoFire(DriversAvailableReference);
        geoLocation=new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude());
        geoQuery=geoFire.queryAtLocation(geoLocation,radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound )
                {
                    driverFound=true;
                    userId=key;
                    driversRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);
                    HashMap driverMap=new HashMap();
                    driverMap.put("CustomerRideId",costumerId);
                    driversRef.updateChildren(driverMap);
                    call_driver_btn.setText("looking for driver ...");
                    GettingDriverLocation();
                }
            }
            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound)
                {
                    radius++;
                    FindClosestDriverId();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void GettingDriverLocation()
    {
        valueEventListener=DriversLocationReference.child(userId).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() )
                {
                    List<Object> driverLocationMap=(List<Object>)dataSnapshot.getValue();
                    double locationlat=0;
                    double locationlin=0;
                    call_driver_btn.setText("Driver found");
                    if(driverLocationMap.get(0)!=null)
                    {
                        locationlat=Double.parseDouble(driverLocationMap.get(0).toString());
                    }
                    if(driverLocationMap.get(1)!=null)
                    {
                        locationlin=Double.parseDouble(driverLocationMap.get(0).toString());
                    }
                    if(driverMarker!=null)
                    {
                        driverMarker.remove();
                    }
                    LatLng latLng=new LatLng(locationlat,locationlin);
                    LatLng latLng1=new LatLng(lastLocation.getLatitude(),lastLocation.getAltitude());
                    driverMarker=mMap.addMarker(new MarkerOptions().position(latLng).title("your driver is here").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                    marker=mMap.addMarker(new MarkerOptions().position(latLng1).title("my position").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
                    Location locationCustomer=new Location("");
                    locationCustomer.setAltitude(latLng1.latitude);
                    locationCustomer.setLongitude(latLng1.longitude);
                    Location locationDriver=new Location("");
                    locationDriver.setAltitude(latLng.latitude);
                    locationDriver.setLongitude(latLng.longitude);
                    double distance=locationCustomer.distanceTo(locationDriver);
                    call_driver_btn.setText("driver found  "+distance);
                    cancel_driver_ride.setVisibility(View.VISIBLE);
                    relativeLayout.setVisibility(View.VISIBLE);
                    getAssignedDriverInformation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAssignedDriverInformation()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()  &&  dataSnapshot.getChildrenCount() > 0)
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String car = dataSnapshot.child("car").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);
                    txtCarName.setText(car);

                    if (dataSnapshot.hasChild("image"))
                    {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profilePic);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
