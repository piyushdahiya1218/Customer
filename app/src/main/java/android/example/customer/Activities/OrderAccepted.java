package android.example.customer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.example.customer.Adapters.ConfirmOrderRecyclerAdapter;
import android.example.customer.Classes.CustomerCart;
import android.example.customer.Classes.Product;
import android.example.customer.R;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrderAccepted extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    FirebaseDatabase database;
    DatabaseReference reference;
    private RecyclerView recyclerView;
    String customerphonenumber=null;
    String vendorphonenumber=null;
    String businessname=null;
    boolean customerfirsttime =true;
    boolean vendorfirsttime=true;
    Marker customermarker;
    Marker vendormarker;
    CustomerCart customerCart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_accepted);

        Intent previntent=getIntent();
        customerphonenumber=previntent.getStringExtra("customerphonenumber");
        vendorphonenumber=previntent.getStringExtra("vendorphonenumber");
        businessname=previntent.getStringExtra("businessname");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used. When map is ready to be used, "onMapReady" function will be called
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        recyclerView=findViewById(R.id.orderacceptedrecyclerview);

        database=FirebaseDatabase.getInstance();
        reference=database.getReference("customercart").child(customerphonenumber);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    customerCart=snapshot.getValue(CustomerCart.class);
                    setAdapter(customerCart.getProducts());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setAdapter(ArrayList<Product> cartproductslist) {
        ConfirmOrderRecyclerAdapter adapter=new ConfirmOrderRecyclerAdapter(cartproductslist);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {

        //for customer
        reference=database.getReference("customerlocation").child(customerphonenumber);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    double customerlatitude=snapshot.child("latitude").getValue(Double.class);
                    double customerlongitude=snapshot.child("longitude").getValue(Double.class);
                    LatLng customerlatlng=new LatLng(customerlatitude,customerlongitude);
                    if(customerfirsttime){
                        customermarker=map.addMarker(new MarkerOptions().position(customerlatlng).title("You"));
                        customerfirsttime =false;
                    }
                    else{
                        customermarker.setPosition(customerlatlng);
                    }

                    //for vendor
                    reference=database.getReference("vendorlocation").child(vendorphonenumber);
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                double vendorlatitude=snapshot.child("latitude").getValue(Double.class);
                                double vendorlongitude=snapshot.child("longitude").getValue(Double.class);
                                LatLng vendorlatlng=new LatLng(vendorlatitude,vendorlongitude);
                                if(vendorfirsttime){
                                    vendormarker=map.addMarker(new MarkerOptions().position(vendorlatlng).title(businessname));
                                    vendorfirsttime=false;
                                }
                                else{
                                    vendormarker.setPosition(vendorlatlng);
                                }

                                if(customermarker!=null && vendormarker!=null){
                                    LatLngBounds.Builder builder=new LatLngBounds.Builder();
                                    builder.include(customermarker.getPosition());
                                    builder.include(vendormarker.getPosition());
                                    LatLngBounds bounds=builder.build();
                                    int width = getResources().getDisplayMetrics().widthPixels;
                                    int height = getResources().getDisplayMetrics().heightPixels;
                                    int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen
                                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                                    map.animateCamera(cu);
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




    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

}