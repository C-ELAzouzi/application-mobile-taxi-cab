package com.example.apptaxi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriversMapsActivity extends FragmentActivity implements OnMapReadyCallback ,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
    ,com.google.android.gms.location.LocationListener
    , View.OnClickListener, RoutingListener
{
        private GoogleMap mMap;
        private GoogleApiClient googleApiClient;
        private LocationRequest locationRequest;
        private Location lastlocation;
        private Marker marker;
        private String Userid;
        private FirebaseAuth mAuth;
        private Button logOut_button;
        private Button settingButton;
        private Intent intent;
        private boolean chekLogout=false;
        private  DatabaseReference driverWorkingRef;
        private String customerId="";
        private DatabaseReference driversRef;
        private DatabaseReference customerPickUpRef;
        private Marker driverMarker;
        private  GeoFire geoFireAvailable;
        private GeoFire geoFireWorking;
        private Handler mhalnder;
        private AlertDialog.Builder alertDialogBuilder;
        private boolean AcceptRequestCustomer=false;
        private ValueEventListener valueEventListener;
        private TextView txtName, txtPhone,txtCar;
        private CircleImageView profilePic;
        private RelativeLayout relativeLayout;
        private List<Polyline> polylines;
        private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
        private Button picked_customer;
        private double distance,prix;
        private  boolean picked_cust=false;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_drivers_maps);
            polylines = new ArrayList<>();
            mAuth=FirebaseAuth.getInstance();
            logOut_button=(Button)findViewById(R.id.logout_driver_btn);
            settingButton=(Button)findViewById(R.id.settings_driver_btn);
            settingButton.setOnClickListener(this);
            logOut_button.setOnClickListener(this);
            Userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
            alertDialogBuilder = new AlertDialog.Builder(this);
            mhalnder=new Handler();
            txtName = findViewById(R.id.name_driver);
            txtPhone = findViewById(R.id.phone_driver);
            profilePic = findViewById(R.id.profile_image_driver);
            txtCar=findViewById(R.id.car_name_driver);
            relativeLayout = findViewById(R.id.rel2);
            picked_customer=(Button)findViewById(R.id.picked_customer);
            picked_customer.setOnClickListener(this);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        private void GetCustomerRequest() {
            driversRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(Userid).child("CustomerRideId");
                driversRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            customerId=dataSnapshot.getValue().toString();
                            getCustomerLocation();

                        }
                        else
                        {
                            customerId="";
                                if(customerPickUpRef!=null)
                                {
                                    customerPickUpRef.removeEventListener(valueEventListener);
                                }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        }

        private void getCustomerLocation() {
            customerPickUpRef=FirebaseDatabase.getInstance().getReference().child("Customer Request").child(customerId).child("l");
            valueEventListener=customerPickUpRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                    {
                        List<Object> driverLocationMap=(List<Object>)dataSnapshot.getValue();
                        double locationlat=0;
                        double locationlin=0;
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
                        driverMarker=mMap.addMarker(new MarkerOptions().position(latLng).title("your Customer  is here").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
                        //getRouteToMarker(latLng);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    private void getRouteToMarker(LatLng latLng) {
            //call for the routing to begin
        Routing routing = new Routing.Builder()
                .key("AIzaSyCp86ImhLm3_-mCIzGkqH4O_VG8mclxzC0")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(lastlocation.getLatitude(),lastlocation.getAltitude()), latLng)
                .build();
        routing.execute();
    }
    @Override
        public void onClick(View v) {
            if(v==logOut_button)
            {
                logOut();
            }
            if(v==settingButton)
            {
                Intent intent=new Intent(getApplicationContext(),SeetingActivity.class);
                intent.putExtra("type","Drivers");
                startActivity(intent);
            }
            if(v==picked_customer)
            {
                if(!picked_cust)
                {
                    distance=0;
                    prix=10;
                    picked_cust=true;
                    picked_customer.setText("finish !!");
                }
                else
                {
                    picked_cust=false;
                    prix=distance*0.5;
                    picked_customer.setText(" prix :10 euro ");
                }


            }
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

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            locationRequest=new LocationRequest();
            locationRequest.setInterval(1100);
            locationRequest.setFastestInterval(1100);
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


            if(marker!=null)
            {
                marker.remove();
            }

            if(getApplicationContext()!=null)
            {
                if(picked_cust)
                {
                    distance+=lastlocation.distanceTo(location)/1000;
                    Toast.makeText(getApplicationContext(),distance+"",Toast.LENGTH_SHORT).show();

                }
                lastlocation=location;
                LatLng latLng=new LatLng(location.getLatitude(),location.getAltitude());
                MarkerOptions markerOptions=new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("user current location");
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                marker=mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
                if(googleApiClient!=null)
                {
                    LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
                }
                Userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference DriverAvailableRef=FirebaseDatabase.getInstance().getReference().child("Driver Available");
                driverWorkingRef=FirebaseDatabase.getInstance().getReference().child("Drivers working");
                 geoFireAvailable=new GeoFire(DriverAvailableRef);
                 geoFireWorking=new GeoFire(driverWorkingRef);
                 Runnable runnable=new Runnable() {
                    @Override
                    public void run() {
                        //GetCustomerRequest();
                        customerId="vcc1sjzQKkh091QHFD1hK63y3Y92";
                        switch (customerId)
                        {

                            case "":
                                geoFireWorking.removeLocation(Userid);
                                geoFireAvailable.setLocation(Userid,new GeoLocation(lastlocation.getLatitude(),lastlocation.getLongitude()));
                                break;
                            default:
                                if(!AcceptRequestCustomer) {
                                    SetAlert();
                                    alertDialogBuilder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            relativeLayout.setVisibility(View.VISIBLE);
                                            //getAssignedCustomerInformation();
                                            AcceptRequestCustomer=true;
                                             picked_customer.setVisibility(View.VISIBLE);
                                            geoFireAvailable.removeLocation(Userid);
                                            geoFireWorking.setLocation(Userid,new GeoLocation(lastlocation.getLatitude(),lastlocation.getLongitude()));
                                        }
                                    });
                                    alertDialogBuilder.setNegativeButton("refus", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            geoFireWorking.removeLocation(Userid);
                                            geoFireAvailable.setLocation(Userid,new GeoLocation(lastlocation.getLatitude(),lastlocation.getLongitude()));
                                            customerId="";
                                        }
                                    });
                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();
                                }
                                else
                                {
                                    geoFireAvailable.removeLocation(Userid);
                                    geoFireWorking.setLocation(Userid,new GeoLocation(lastlocation.getLatitude(),lastlocation.getLongitude()));
                                }
                                break;
                        }
                        mhalnder.postDelayed(this,5000);
                    }
                };
                runnable.run();
            }
            }
        protected  synchronized void  buildGoogleApiClient()
        {
            googleApiClient =new GoogleApiClient.Builder(this).
                    addConnectionCallbacks(this).
                    addOnConnectionFailedListener(this).
                    addApi(LocationServices.API).
                    build();
            googleApiClient.connect();
        }

        @Override
        protected void onStop() {
            super.onStop();

            if(!chekLogout)
            {
                removeUserFromDataBase();
                finish();
            }


        }
        private void logOut()
        {
            removeUserFromDataBase();
            mAuth.signOut();
            chekLogout=true;
            intent=new Intent(DriversMapsActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        private void removeUserFromDataBase()
        {

            DatabaseReference DriverAvailableRef=FirebaseDatabase.getInstance().getReference().child("Driver Available");
            GeoFire geoFire=new GeoFire(DriverAvailableRef);
            geoFire.removeLocation(Userid);
        }
        private void SetAlert()
        {
            alertDialogBuilder.setTitle("Customer Request");
            alertDialogBuilder.setMessage("if you accept the request click in ok !!");
            alertDialogBuilder.setIcon(R.drawable.request);
        }
    private void getAssignedCustomerInformation()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Custumers").child(customerId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()  &&  dataSnapshot.getChildrenCount() > 0)
                {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);

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

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortess) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }
}
