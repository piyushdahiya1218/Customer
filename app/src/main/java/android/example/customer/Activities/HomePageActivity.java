package android.example.customer.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.example.customer.Adapters.CustomInfoWindowAdapter;
import android.example.customer.Classes.CustomerLocation;
import android.example.customer.R;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;

import static android.example.customer.R.id.currentlocationbuttonhomepage;

public class HomePageActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap map;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    FusedLocationProviderClient fusedLocationProviderClient;
    FirebaseDatabase database;
    DatabaseReference reference;
    private LocationManager locationManager;
    private final int MIN_TIME = 1000;    // 1 second
    private final int MIN_DISTANCE = 1;   // 1 meter
    MarkerOptions currentlocationmarkeroptions;
    Marker currentlocationmarker;
    LatLng currentreallatlng;
    LatLng currentstaticlatlng;
    HashMap<String,Marker> vendormarkers=new HashMap<>();
    String customerphonenumber;
    HashSet<String> vendors =new HashSet<>();
    String vendorconstraint="all";
    String businessname=null;
    String snippet="";
    LinearLayout linearLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Intent intent=getIntent();
        customerphonenumber =intent.getStringExtra("customerphonenumber");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used. When map is ready to be used, "onMapReady" function will be called
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        getcurrentstaticlocation();     //runs one time just to bring the screen to current location

        getlocationupdates();           //this then tracks real time location

        Button currentlocationbutton=findViewById(currentlocationbuttonhomepage);
        //when current location button is clicked
        currentlocationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will take the map to our real-time location
                if(currentreallatlng!=null){
                    currentlocationmarker.setPosition(currentreallatlng);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentreallatlng, 18f));
                }
                //if real-time location unavailable, will take the map to our last-updated location
                else if(currentstaticlatlng!=null){
                    currentlocationmarker.setPosition(currentstaticlatlng);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentstaticlatlng, 18f));
                }
                //if neither are available, then it will try to get these locations
                else{
                    getcurrentstaticlocation();
                    getlocationupdates();
                }
            }
        });


        //****MAP STUFF ENDS HERE****

        linearLayout=findViewById(R.id.buttonslinearlayout);
        EditText searchedittext=findViewById(R.id.search);
        Button searchbutton=findViewById(R.id.searchbutton);

        searchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchquery=searchedittext.getText().toString();
                if(!searchquery.equals("")){
                    filterbuttons(searchquery);
                }
            }
        });

        Button allbutton=findViewById(R.id.all);
        allbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vendorconstraint="all";
                map.clear();
                vendormarkers.clear();
//                getcurrentstaticlocation();
//                getlocationupdates();
                if(currentreallatlng!=null){
                    map.addMarker(new MarkerOptions().position(currentreallatlng).title("My Location"));
                }
                else{
                    map.addMarker(new MarkerOptions().position(currentstaticlatlng).title("My Location"));
                }


                reference=database.getReference("vendorlocation");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(vendorconstraint.equals("all")){
                            for (DataSnapshot vendorlocation : snapshot.getChildren()){
                                if(vendorlocation!=null){
                                    String latitudestring=vendorlocation.child("latitude").getValue().toString();
                                    double latitude=Double.parseDouble(latitudestring);
                                    String longitudestring=vendorlocation.child("longitude").getValue().toString();
                                    double longitude=Double.parseDouble(longitudestring);
                                    LatLng vendorcurrentlocation=new LatLng(latitude,longitude);

                                    //getting business name for marker title
                                    reference=database.getReference("vendor").child(vendorlocation.getKey());
                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            businessname=snapshot.child("businessname").getValue().toString();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    //getting vendors cart and setting markers
                                    reference=database.getReference("vendorcart").child(vendorlocation.getKey());
                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(vendorconstraint.equals("all")){
                                                snippet="";
                                                for(DataSnapshot product : snapshot.getChildren()){
                                                    if(product!=null){
                                                        snippet=snippet+product.child("productnameeng").getValue().toString()+" (Price: Rs."+product.child("price").getValue().toString()+", Quantity: "+product.child("quantity").getValue().toString()+"kg)"+"\n";
                                                    }
                                                }
                                                //for updating existing markers
                                                if(vendormarkers.containsKey(vendorlocation.getKey())){
                                                    Marker marker=vendormarkers.get(vendorlocation.getKey());
                                                    marker.setSnippet(snippet);
                                                    marker.setPosition(vendorcurrentlocation);
                                                }
                                                //for new markers
                                                else{
                                                    Log.i("key: "+vendorlocation.getKey(), "snippet: "+snippet);
                                                    Log.i("Oncreate() all businessname",businessname);
                                                    Marker marker=map.addMarker(new MarkerOptions().position(vendorcurrentlocation).title(businessname).snippet(snippet));
                                                    vendormarkers.put(vendorlocation.getKey(),marker);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });


        Button fruitsbutton=findViewById(R.id.fruits);
        fruitsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vendorconstraint="fruits";
                map.clear();
                vendormarkers.clear();
                vendors.clear();
//                getcurrentstaticlocation();
//                getlocationupdates();
                if(currentreallatlng!=null){
                    map.addMarker(new MarkerOptions().position(currentreallatlng).title("My Location"));
                }
                else{
                    map.addMarker(new MarkerOptions().position(currentstaticlatlng).title("My Location"));
                }


                //to get phonenumbers of all the fruit vendors
                database=FirebaseDatabase.getInstance();
                reference=database.getReference("vendor");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(vendorconstraint.equals("fruits")){
                            for(DataSnapshot vendor : snapshot.getChildren()){
                                if(vendor!=null){
                                    if(vendor.child("producttype").getValue().toString().equals("fruits")){
                                        vendors.add(vendor.getKey());
                                    }
                                }
                            }

                            //get location and set markers of fruit vendors we got from above hashset
                            reference=database.getReference("vendorlocation");
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(vendorconstraint.equals("fruits")){
                                        for(DataSnapshot vendorlocation : snapshot.getChildren()){
                                            if(vendors.contains(vendorlocation.getKey())){
                                                String latitudestring=vendorlocation.child("latitude").getValue().toString();
                                                double latitude=Double.parseDouble(latitudestring);
                                                String longitudestring=vendorlocation.child("longitude").getValue().toString();
                                                double longitude=Double.parseDouble(longitudestring);
                                                LatLng vendorcurrentlocation=new LatLng(latitude,longitude);

                                                //getting business name for marker title
                                                reference=database.getReference("vendor").child(vendorlocation.getKey());
                                                reference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        businessname=snapshot.child("businessname").getValue().toString();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                                //getting vendors cart and setting markers
                                                reference=database.getReference("vendorcart").child(vendorlocation.getKey());
                                                reference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(vendorconstraint.equals("fruits")){
                                                            snippet="";
                                                            for(DataSnapshot product : snapshot.getChildren()){
                                                                if(product!=null){
                                                                    snippet=snippet+product.child("productnameeng").getValue().toString()+" (Price: Rs."+product.child("price").getValue().toString()+", Quantity: "+product.child("quantity").getValue().toString()+"kg)"+"\n";
                                                                }
                                                            }
                                                            //for updating existing markers
                                                            if(vendormarkers.containsKey(vendorlocation.getKey())){
                                                                Marker marker=vendormarkers.get(vendorlocation.getKey());
                                                                marker.setSnippet(snippet);
                                                                marker.setPosition(vendorcurrentlocation);
                                                            }
                                                            //for new markers
                                                            else{
                                                                Log.i("key: "+vendorlocation.getKey(), "snippet: "+snippet);
                                                                Log.i("Oncreate() fruits businessname",businessname);
                                                                Marker marker=map.addMarker(new MarkerOptions().position(vendorcurrentlocation).title(businessname).snippet(snippet));
                                                                vendormarkers.put(vendorlocation.getKey(),marker);
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }
        });

        Button vegetablesbutton=findViewById(R.id.vegetables);
        vegetablesbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vendorconstraint="vegetables";
                map.clear();
                vendormarkers.clear();
                vendors.clear();
//                getcurrentstaticlocation();
//                getlocationupdates();
                if(currentreallatlng!=null){
                    map.addMarker(new MarkerOptions().position(currentreallatlng).title("My Location"));
                }
                else{
                    map.addMarker(new MarkerOptions().position(currentstaticlatlng).title("My Location"));
                }


                //to get phonenumbers of all the fruit vendors
                database=FirebaseDatabase.getInstance();
                reference=database.getReference("vendor");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(vendorconstraint.equals("vegetables")){
                            for(DataSnapshot vendor : snapshot.getChildren()){
                                if(vendor!=null){
                                    if(vendor.child("producttype").getValue().toString().equals("vegetables")){
                                        vendors.add(vendor.getKey());
                                    }
                                }
                            }

                            //get location and set markers of fruit vendors we got from above function
                            reference=database.getReference("vendorlocation");
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(vendorconstraint.equals("vegetables")){
                                        for(DataSnapshot vendorlocation : snapshot.getChildren()){
                                            if(vendors.contains(vendorlocation.getKey())){
                                                String latitudestring=vendorlocation.child("latitude").getValue().toString();
                                                double latitude=Double.parseDouble(latitudestring);
                                                String longitudestring=vendorlocation.child("longitude").getValue().toString();
                                                double longitude=Double.parseDouble(longitudestring);
                                                LatLng vendorcurrentlocation=new LatLng(latitude,longitude);

                                                //getting business name for marker title
                                                reference=database.getReference("vendor").child(vendorlocation.getKey());
                                                reference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        businessname=snapshot.child("businessname").getValue().toString();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                                //getting vendors cart and setting markers
                                                reference=database.getReference("vendorcart").child(vendorlocation.getKey());
                                                reference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(vendorconstraint.equals("vegetables")){
                                                            snippet="";
                                                            for(DataSnapshot product : snapshot.getChildren()){
                                                                if(product!=null){
                                                                    snippet=snippet+product.child("productnameeng").getValue().toString()+" (Price: Rs."+product.child("price").getValue().toString()+", Quantity: "+product.child("quantity").getValue().toString()+"kg)"+"\n";
                                                                }
                                                            }
                                                            //for updating existing markers
                                                            if(vendormarkers.containsKey(vendorlocation.getKey())){
                                                                Marker marker=vendormarkers.get(vendorlocation.getKey());
                                                                marker.setSnippet(snippet);
                                                                marker.setPosition(vendorcurrentlocation);
                                                            }
                                                            //for new markers
                                                            else{
                                                                Log.i("key: "+vendorlocation.getKey(), "snippet: "+snippet);
                                                                Log.i("Oncreate() vegies businessname",businessname);
                                                                Marker marker=map.addMarker(new MarkerOptions().position(vendorcurrentlocation).title(businessname).snippet(snippet));
                                                                vendormarkers.put(vendorlocation.getKey(),marker);
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }

    private void filterbuttons(String searchquery) {
        for (int i=0;i<linearLayout.getChildCount();i++){
            Button button= (Button) linearLayout.getChildAt(i);
            Toast.makeText(this, button.getText().toString(), Toast.LENGTH_SHORT).show();
            if(button.getText().toString().contains(searchquery)){
                button.setVisibility(View.VISIBLE);
            }
            else{
                button.setVisibility(View.GONE);
            }
        }
    }


    //runs when map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        //map will initially load at this point (center of delhi)
        LatLng indiaGate = new LatLng(28.614484790108023, 77.22988168052079);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(indiaGate, 16f));

        //to show default google map current location button
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            map.setMyLocationEnabled(true);
//        }

        map.setInfoWindowAdapter(new CustomInfoWindowAdapter(getApplicationContext(),map));
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                if(!marker.getTitle().equals("My Location")){
                    Log.i("marker "+marker.getTitle()+" "+marker.getTag()," clicked");
                    Intent intent=new Intent(getApplicationContext(),SelectOrderActivity.class);
                    intent.putExtra("vendorphonenumber",String.valueOf(marker.getTag()));
                    intent.putExtra("customerphonenumber", customerphonenumber);
                    startActivity(intent);
                }
            }
        });

        database=FirebaseDatabase.getInstance();
        reference=database.getReference("vendorlocation");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(vendorconstraint.equals("all")){
                    for (DataSnapshot vendorlocation : snapshot.getChildren()){
                        if(vendorlocation!=null){
                            //getting vendor location
                            String latitudestring=vendorlocation.child("latitude").getValue().toString();
                            double latitude=Double.parseDouble(latitudestring);
                            String longitudestring=vendorlocation.child("longitude").getValue().toString();
                            double longitude=Double.parseDouble(longitudestring);
                            LatLng vendorcurrentlocation=new LatLng(latitude,longitude);

                            //getting business name for marker title
                            reference=database.getReference("vendor").child(vendorlocation.getKey());
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        Log.i("OnMapReady businessname",snapshot.child("username").getValue().toString());
                                        businessname=snapshot.child("businessname").getValue().toString();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //getting vendors cart and setting markers
                            reference=database.getReference("vendorcart").child(vendorlocation.getKey());
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(vendorconstraint.equals("all")){
                                        snippet="";
                                        for(DataSnapshot product : snapshot.getChildren()){
                                            if(product!=null){
                                                snippet=snippet+product.child("productnameeng").getValue().toString()+" (Price: Rs."+product.child("price").getValue().toString()+", Quantity: "+product.child("quantity").getValue().toString()+"kg)"+"\n";
                                            }
                                        }
                                        //for updating existing markers
                                        if(vendormarkers.containsKey(vendorlocation.getKey())){
                                            Marker marker=vendormarkers.get(vendorlocation.getKey());
                                            marker.setSnippet(snippet);
                                            marker.setPosition(vendorcurrentlocation);
                                        }
                                        //for new markers
                                        else{
                                            Log.i("key: "+vendorlocation.getKey(), "snippet: "+snippet);
                                            Log.i("businessname",businessname);
                                            Marker marker=map.addMarker(new MarkerOptions().position(vendorcurrentlocation).title(businessname).snippet(snippet));
                                            marker.setTag(vendorlocation.getKey());
                                            vendormarkers.put(vendorlocation.getKey(),marker);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void updatemarker(String phonenumber) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("customerlocation").child(phonenumber);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CustomerLocation customerLocation = snapshot.getValue(CustomerLocation.class);
                    if (customerLocation != null && currentlocationmarker!=null) {
                        currentreallatlng = new LatLng(customerLocation.getLatitude(), customerLocation.getLongitude());
                        currentlocationmarker.setPosition(currentreallatlng);
//                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentreallatlng, 18f));
                    }
//                    try {
//                        VendorLocation vendorLocation = snapshot.getValue(VendorLocation.class);
//                        if (vendorLocation != null) {
//                            currentreallatlng = new LatLng(vendorLocation.getLatitude(), vendorLocation.getLongitude());
//                            currentlocationmarker.setPosition(currentreallatlng);
//                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentreallatlng, 18f));
//                        }
//                    } catch (Exception e) {
//                        Log.i("EXCEPTION", e.getMessage());
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getlocationupdates() {
//        Toast.makeText(this, "Getting Current Location", Toast.LENGTH_SHORT).show();
        if (locationManager != null) {
            //if we have access to location
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //if gps is off
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                    Toast.makeText(this, "prompt to turn on GPS", Toast.LENGTH_SHORT).show();
                    checkgps();
                }

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {        //ask for location by gps
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {       //if gps not available, ask for location by network
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                } else {
                    Toast.makeText(this, "No location provider available", Toast.LENGTH_SHORT).show();
                }
            } else {
                checkLocationPermission();
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
//        Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();
        if (location != null) {
            database = FirebaseDatabase.getInstance();
            reference = database.getReference("customerlocation");
            reference.child(customerphonenumber).setValue(location);
            updatemarker(customerphonenumber);

//            Intent intent = getIntent();
//            String phonenumber = intent.getStringExtra("phonenumber");
//            database = FirebaseDatabase.getInstance();
//            reference = database.getReference("vendorlocation");
//            reference.child(phonenumber).setValue(location);
//            updatemarker(phonenumber);
        }
    }

    private void checkgps() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });

        //runs if gps is off
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in "onActivityResult()".
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(HomePageActivity.this, LocationRequest.PRIORITY_HIGH_ACCURACY);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
//                        Toast.makeText(this, "gps enabled", Toast.LENGTH_SHORT).show();
                        getcurrentstaticlocation();
                        getlocationupdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(this, "gps is required", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    default:
                        break;
                }
                break;
        }
    }


    //points a marker on map to current location
    public void getcurrentstaticlocation() {
        //checks if we have location access or not, if we have then run
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
//                    Toast.makeText(this, "getting static location", Toast.LENGTH_SHORT).show();
                    currentstaticlatlng = new LatLng(location.getLatitude(), location.getLongitude());
                    currentlocationmarkeroptions=new MarkerOptions().position(currentstaticlatlng).title("My Location");
                    currentlocationmarker=map.addMarker(currentlocationmarkeroptions);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentstaticlatlng, 18f));
                }
                if(location==null){
//                    Toast.makeText(this, "static location null", Toast.LENGTH_SHORT).show();
                }
            });
        }
//        else{
//            checkLocationPermission();
//        }
    }




// code below checks whether we have permission to access location or not, if not, then ask for permission

    //this function runs whenever location permission is required
    public void checkLocationPermission() {

        //runs when we dont have location permission, (this will always run when starting app for first time)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //requesting for location permission, this basically creates the popup for location request (this takes us to function "onRequestPermissionsResult")
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }

        //runs when we already have location permission
//        else {
//            getcurrentstaticlocation();         //get current location on map
//        }
    }

    //location permission popup, defines what to do when permission is accepted or declined
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {

                //runs when we click on 'grant permission' on the popup
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    getcurrentstaticlocation();            //after getting location permission, we want to get the current location on map
//                    Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
                    getcurrentstaticlocation();
                    getlocationupdates();
                }
                //runs when we click on 'deny permission' on the popup
                else {
                    Toast.makeText(this, "permission required", Toast.LENGTH_SHORT).show();
                    finish();           //return back to previous activity
                }
            }
        }
    }

    LocationListener locationListener=new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}