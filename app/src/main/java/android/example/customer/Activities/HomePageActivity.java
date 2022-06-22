package android.example.customer.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.example.customer.Adapters.AllProductsRecyclerAdapter;
import android.example.customer.Adapters.CustomInfoWindowAdapter;
import android.example.customer.Adapters.SelectOrderRecyclerAdapter;
import android.example.customer.Classes.CustomerLocation;
import android.example.customer.Classes.Product;
import android.example.customer.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static android.example.customer.R.id.currentlocationbuttonhomepage;

public class HomePageActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerDragListener {

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
    Boolean active;
    String snippet="";
    LinearLayout linearLayout;
    private RecyclerView recyclerView;
    ArrayList<Product> allproductslist=new ArrayList<>();
    Boolean searchmode=false;
    String searchedlocationstring=null;
    String streetlocationstring=null;
    Boolean isMarkerdragged=false;




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

        //search view of map
        SearchView mapsearchview=findViewById(R.id.mapsearchview);
        mapsearchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchmode=true;
                searchedlocationstring=mapsearchview.getQuery().toString();
                List<Address> searchedadresslist=null;
                if(searchedlocationstring!=null && !searchedlocationstring.equals("")){
                    Geocoder geocoder=new Geocoder(getApplicationContext());
                    try {
                        searchedadresslist=geocoder.getFromLocationName(searchedlocationstring, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(searchedadresslist!=null){
                        Address address=searchedadresslist.get(0);
                        LatLng searchedLatlng=new LatLng(address.getLatitude(),address.getLongitude());
                        currentlocationmarker.setPosition(searchedLatlng);
                        currentlocationmarker.setTitle(searchedlocationstring);
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLatlng,15));
                        setinfowindowadapter(map,searchedlocationstring);

                        database = FirebaseDatabase.getInstance();
                        reference = database.getReference("customerlocation").child(customerphonenumber);
                        reference.child("latitude").setValue(address.getLatitude());
                        reference.child("longitude").setValue(address.getLongitude());

                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        Button currentlocationbutton=findViewById(currentlocationbuttonhomepage);
        //when current location button is clicked
        currentlocationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchmode=false;
                isMarkerdragged=false;
                currentlocationmarker.setTitle("My Location");
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

//        linearLayout=findViewById(R.id.buttonslinearlayout);
//        LinearLayout linearLayouttwo = findViewById(R.id.buttonslinearlayouttwo);
//        EditText searchedittext=findViewById(R.id.search);
//        Button searchbutton=findViewById(R.id.searchbutton);
//
//        searchbutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String searchquery=searchedittext.getText().toString();
//                if(!searchquery.equals("")){
//                    filterbuttons(searchquery);
//                }
//            }
//        });

        //for vendor marker icon
        int height=100;
        int width=100;
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.vendoricon);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

        //when all button is clicked
        Button allbutton=findViewById(R.id.all);
        allbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //for list
                setAdapter(allproductslist);

                //for map
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
                                            active=snapshot.child("active").getValue(Boolean.class);
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
                                                if(active){
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
                                                        Marker marker=map.addMarker(new MarkerOptions().position(vendorcurrentlocation).title(businessname).snippet(snippet).icon(smallMarkerIcon));
                                                        vendormarkers.put(vendorlocation.getKey(),marker);
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    //listening to visibility changes
                                    reference=database.getReference("vendor");
                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                for(DataSnapshot vendor : snapshot.getChildren()){
                                                    if(vendor!=null){
                                                        Boolean active=vendor.child("active").getValue(Boolean.class);
                                                        Marker marker=vendormarkers.get(vendor.getKey());
                                                        if(marker!=null){
                                                            //when inactive
                                                            if(active!=null && !active){
                                                                marker.setVisible(false);
                                                            }
                                                            //when active
                                                            else{
                                                                marker.setVisible(true);
                                                            }
                                                        }
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
                //for list
                ArrayList<Product> fruitslist=new ArrayList<>();
                for(int i=0;i<allproductslist.size();i++){
                    if(allproductslist.get(i).getProducttype().equals("fruits")){
                        Product product=allproductslist.get(i);
                        fruitslist.add(product);
                    }
                }
                setAdapter(fruitslist);


                //for map
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
                                                        active=snapshot.child("active").getValue(Boolean.class);
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
                                                            if(active){
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
                                                                    Marker marker=map.addMarker(new MarkerOptions().position(vendorcurrentlocation).title(businessname).snippet(snippet).icon(smallMarkerIcon));
                                                                    vendormarkers.put(vendorlocation.getKey(),marker);
                                                                }

                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                                //listening to visibility changes
                                                reference=database.getReference("vendor");
                                                reference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            for(DataSnapshot vendor : snapshot.getChildren()){
                                                                if(vendor!=null){
                                                                    Boolean active=vendor.child("active").getValue(Boolean.class);
                                                                    Marker marker=vendormarkers.get(vendor.getKey());
                                                                    if(marker!=null){
                                                                        //when inactive
                                                                        if(active!=null && !active){
                                                                            marker.setVisible(false);
                                                                        }
                                                                        //when active
                                                                        else{
                                                                            marker.setVisible(true);
                                                                        }
                                                                    }
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
                //for list
                ArrayList<Product> vegetableslist=new ArrayList<>();
                for(int i=0;i<allproductslist.size();i++){
                    if(allproductslist.get(i).getProducttype().equals("vegetables")){
                        Product product=allproductslist.get(i);
                        vegetableslist.add(product);
                    }
                }
                setAdapter(vegetableslist);

                //for map
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


                //to get phonenumbers of all the vegetable vendors
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
                                                        active=snapshot.child("active").getValue(Boolean.class);
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
                                                            if(active){
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
                                                                    Marker marker=map.addMarker(new MarkerOptions().position(vendorcurrentlocation).title(businessname).snippet(snippet).icon(smallMarkerIcon));
                                                                    vendormarkers.put(vendorlocation.getKey(),marker);
                                                                }

                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                                //listening to visibility changes
                                                reference=database.getReference("vendor");
                                                reference.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            for(DataSnapshot vendor : snapshot.getChildren()){
                                                                if(vendor!=null){
                                                                    Boolean active=vendor.child("active").getValue(Boolean.class);
                                                                    Marker marker=vendormarkers.get(vendor.getKey());
                                                                    if(marker!=null){
                                                                        //when inactive
                                                                        if(active!=null && !active){
                                                                            marker.setVisible(false);
                                                                        }
                                                                        //when active
                                                                        else{
                                                                            marker.setVisible(true);
                                                                        }
                                                                    }
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

        //populate all products list
        recyclerView=findViewById(R.id.allproductsrecyclerview);
        setallproductslist();
        setAdapter(allproductslist);
    }

    private void setinfowindowadapter(GoogleMap map, String searchedlocationstring) {
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter(getApplicationContext(),map,searchedlocationstring));
    }

    private void setAdapter(ArrayList<Product> list) {
        AllProductsRecyclerAdapter adapter=new AllProductsRecyclerAdapter(list);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    private void setallproductslist() {
        //fruits
        allproductslist.add(new Product("Apricot","खुबानी",getResources().getIdentifier("apricot","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Avocado","एवोकाडो",getResources().getIdentifier("avocado","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Blueberry","ब्लूबेरी",getResources().getIdentifier("blueberry","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Coconut","नारियल",getResources().getIdentifier("coconut","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Custard Apple","शरीफा",getResources().getIdentifier("custardapple","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Date","खजूर",getResources().getIdentifier("date","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Dragon Fruit","ड्रैगन फल",getResources().getIdentifier("dragonfruit","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Grape Fruit","चकोतरा",getResources().getIdentifier("grapefruit","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Grapes","अंगूर",getResources().getIdentifier("grapes","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Guava","अमरूद",getResources().getIdentifier("guava","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Kiwi","कीवी",getResources().getIdentifier("kiwi","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Lemon","नींबू",getResources().getIdentifier("lemon","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Lychee","लीची",getResources().getIdentifier("lychee","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Mandarin","मंदारिन फल",getResources().getIdentifier("mandarin","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Mango","आम",getResources().getIdentifier("mango","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Melon","खरबूज",getResources().getIdentifier("melon","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Mulberry","शहतूत",getResources().getIdentifier("mulberry","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Muskmelon","खरबूजा",getResources().getIdentifier("muskmelon","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Papaya","पपीता",getResources().getIdentifier("papaya","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Peach","आडू",getResources().getIdentifier("peach","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Pear","नाशपाती",getResources().getIdentifier("pear","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Pineapple","अनानास",getResources().getIdentifier("pineapple","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Plum","आलूबुखारा",getResources().getIdentifier("plum","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Pomegranate","अनार",getResources().getIdentifier("pomegranate","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Quince","श्रीफल",getResources().getIdentifier("quince","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Raspberry","रसभरी",getResources().getIdentifier("raspberry","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Starfruit","स्टार फल",getResources().getIdentifier("starfruit","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Strawberry","स्ट्रॉबेरी",getResources().getIdentifier("strawberry","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Sweet Lime","मीठा नींबू",getResources().getIdentifier("sweetlime","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Tendor Coconut","कच्चा नारियल",getResources().getIdentifier("tendercoconut","drawable",getPackageName()),10,1,false,"fruits"));

        allproductslist.add(new Product("Black Grapes","काले अंगूर",getResources().getIdentifier("blackgrapes","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Apple","सेब",getResources().getIdentifier("apple","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Orange","संतरा",getResources().getIdentifier("orange","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Banana","केला",getResources().getIdentifier("banana","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Cherry","चेरी",getResources().getIdentifier("cherry","drawable",getPackageName()),10,1,false,"fruits"));
        allproductslist.add(new Product("Watermelon","तरबूज",getResources().getIdentifier("watermelon","drawable",getPackageName()),10,1,false,"fruits"));


        //vegetables
        allproductslist.add(new Product("Asparagus","एस्परैगस",getResources().getIdentifier("asparagus","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Beetroot","चुकंदर",getResources().getIdentifier("beetroot","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Bitter Gourd","करेला",getResources().getIdentifier("bittergourd","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Bottle Gourd","लौकी",getResources().getIdentifier("bottlegourd","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Brinjal","बैंगन",getResources().getIdentifier("brinjal","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Broccoli","ब्रॉकली",getResources().getIdentifier("broccoli","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Cherry Tomato","चेरी टमाटर",getResources().getIdentifier("cherrytomato","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Chilly","मिर्च",getResources().getIdentifier("chilly","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Coriander","धनिया",getResources().getIdentifier("coriander","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Corn","मक्का",getResources().getIdentifier("corn","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Cucumber","खीरा",getResources().getIdentifier("cucumber","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Drumsticks","सहजन",getResources().getIdentifier("drumsticks","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Fenugreek","मेंथी",getResources().getIdentifier("fenugreek","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Garlic","लहसुन",getResources().getIdentifier("garlic","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Tinda","टिंडा",getResources().getIdentifier("tinda","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Jackfruit","कटहल",getResources().getIdentifier("jackfruit","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Kale","गोभी",getResources().getIdentifier("kale","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Lettuce","सलाद पत्ता",getResources().getIdentifier("lettuce","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Lotus Stem","कमल का तना",getResources().getIdentifier("lotusstem","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Olive","जैतून",getResources().getIdentifier("olive","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Onion","प्याज़",getResources().getIdentifier("onion","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Peas","मटर",getResources().getIdentifier("peas","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Potato","आलू",getResources().getIdentifier("potato","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Pumpkin","कद्दू",getResources().getIdentifier("pumpkin","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Red Bell Pepper","लाल शिमला मिर्च",getResources().getIdentifier("redbellpepper","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Ridge Gourd","तोरई",getResources().getIdentifier("ridgegourd","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Saag Bathua","साग बथुआ",getResources().getIdentifier("saagbathua","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Snake Cucumber","नाग खीरा",getResources().getIdentifier("snakecucumber","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Sweet Potato","शकरकंद",getResources().getIdentifier("sweetpotato","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Tomato","टमाटर",getResources().getIdentifier("tomato","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Turnip","शलजम",getResources().getIdentifier("turnip","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("White Radish","मूली",getResources().getIdentifier("whiteradish","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Yellow Bell Pepper","पीली बेल मिर्च",getResources().getIdentifier("yellowbellpepper","drawable",getPackageName()),10,1,false,"vegetables"));

        allproductslist.add(new Product("Beans","फलियां",getResources().getIdentifier("beans","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Cabbage","पत्ता गोभी",getResources().getIdentifier("cabbage","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Carrot","गाजर",getResources().getIdentifier("carrot","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Cauliflower","फूलगोभी",getResources().getIdentifier("cauliflower","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Ladyfinger","भिन्डी",getResources().getIdentifier("ladyfinger","drawable",getPackageName()),10,1,false,"vegetables"));
        allproductslist.add(new Product("Spinach","पालक",getResources().getIdentifier("spinach","drawable",getPackageName()),10,1,false,"vegetables"));
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

        map.setOnMarkerDragListener(this);

        map.setInfoWindowAdapter(new CustomInfoWindowAdapter(getApplicationContext(),map,searchedlocationstring));

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                if(!marker.getTitle().equals("My Location") && !marker.getTitle().equals(searchedlocationstring) && !marker.getTitle().equals(streetlocationstring)){
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
                                        active=snapshot.child("active").getValue(Boolean.class);
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
                                        if(active){
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
                                                int height=100;
                                                int width=100;
                                                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.vendoricon);
                                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                                                BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                                                marker.setIcon(smallMarkerIcon);
                                                vendormarkers.put(vendorlocation.getKey(),marker);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //listening to visibility changes
                            reference=database.getReference("vendor");
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        for(DataSnapshot vendor : snapshot.getChildren()){
                                            if(vendor!=null){
                                                Boolean active=vendor.child("active").getValue(Boolean.class);
                                                Marker marker=vendormarkers.get(vendor.getKey());
                                                if(marker!=null){
                                                    //when inactive
                                                    if(active!=null && !active){
                                                        marker.setVisible(false);
                                                    }
                                                    //when active
                                                    else{
                                                        marker.setVisible(true);
                                                    }
                                                }
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        LatLng latLng=marker.getPosition();
        try {
            Geocoder geocoder=new Geocoder(getApplicationContext());
            List<Address> addresses=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addresses.size()>0){
                isMarkerdragged=true;

                Address address=addresses.get(0);
                streetlocationstring=address.getAddressLine(0);
                currentlocationmarker.setTitle(streetlocationstring);

                setinfowindowadapter(map,streetlocationstring);

                database = FirebaseDatabase.getInstance();
                reference = database.getReference("customerlocation").child(customerphonenumber);
                reference.child("latitude").setValue(address.getLatitude());
                reference.child("longitude").setValue(address.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (location!=null && searchmode==false && isMarkerdragged==false) {
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
                    currentlocationmarkeroptions=new MarkerOptions().position(currentstaticlatlng).title("My Location").draggable(true);
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