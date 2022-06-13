package android.example.customer.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.example.customer.Adapters.SelectOrderRecyclerAdapter;
import android.example.customer.Classes.CustomerCart;
import android.example.customer.Classes.Product;
import android.example.customer.R;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectOrderActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference reference;
    private RecyclerView recyclerView;
    String username=null;
    String businessname=null;
    String vendorphonenumber =null;
    String customerphonenumber=null;
    ArrayList<Product> productslist=new ArrayList<Product>();
    ArrayList<Product> customercartlist=new ArrayList<Product>();
    TextView totalpricetextview;
    HashMap<String,Integer> imagesmap=new HashMap<>();
    int Tprice=0;
    int Titems=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_order);

        Intent previntent=getIntent();
        vendorphonenumber =previntent.getStringExtra("vendorphonenumber");
        customerphonenumber=previntent.getStringExtra("customerphonenumber");


        TextView usernametextview=findViewById(R.id.username);
        TextView businessnametextview=findViewById(R.id.businessname);
        recyclerView=findViewById(R.id.recyclerview);
        totalpricetextview=findViewById(R.id.totalprice);
        Button confirmbutton=findViewById(R.id.confirmbutton);
        TextView totalitemstextview=findViewById(R.id.totalitems);

        //set username and businessname
        database=FirebaseDatabase.getInstance();
        reference=database.getReference("vendor").child(vendorphonenumber);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    username=snapshot.child("username").getValue().toString();
                    usernametextview.setText(username);
                    businessname=snapshot.child("businessname").getValue().toString();
                    businessnametextview.setText(businessname);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //extract vendor cart from firebase
        reference=database.getReference("vendorcart").child(vendorphonenumber);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot product : snapshot.getChildren()){
                        if(product!=null){
                            productslist.add(product.getValue(Product.class));
                            customercartlist.add(product.getValue(Product.class));
                        }
                    }
                    if(!productslist.isEmpty()){
                        setnewimageRid();
                        setAdapter();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //set and update total price and total items
        reference=database.getReference("customercart").child(customerphonenumber);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Tprice=snapshot.child("tprice").getValue(Integer.class);
                    totalpricetextview.setText("Rs. "+ Tprice);
                    Titems=snapshot.child("titems").getValue(Integer.class);
                    totalitemstextview.setText(Titems+" items");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //when confirm button is clicked
        confirmbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Product> newlist=new ArrayList<>();
                for (Product product:customercartlist){
                    if(product.isSelected()){
                        newlist.add(product);
                    }
                }
                if(newlist.isEmpty()){
                    Toast.makeText(SelectOrderActivity.this, "Select an item", Toast.LENGTH_SHORT).show();
                }
                else{
                    CustomerCart customerCart=new CustomerCart(newlist,Tprice,Titems);
                    reference=database.getReference("customercart").child(customerphonenumber);
                    reference.setValue(customerCart);

                    Intent intent=new Intent(getApplicationContext(),ConfirmOrderActivity.class);
                    intent.putExtra("customerphonenumber",customerphonenumber);
                    intent.putExtra("vendorphonenumber",vendorphonenumber);
                    intent.putExtra("businessname",businessname);
                    startActivity(intent);
                }
            }
        });

    }

    private void setnewimageRid() {
        imagesmap.put("Orange",getResources().getIdentifier("orange","drawable",getPackageName()));
        Log.i("orangeRid ",String.valueOf(imagesmap.get("Orange")));
    }


    private void setAdapter() {
        for(int i=0;i<customercartlist.size();i++){
            customercartlist.get(i).setQuantity(1);
        }
        for (int i=0;i<productslist.size();i++){
            Log.i("productslist"+i,"q"+productslist.get(i).getQuantity());
        }
        for (int i=0;i<customercartlist.size();i++){
            Log.i("customercartlist"+i,"q"+customercartlist.get(i).getQuantity());
        }
        SelectOrderRecyclerAdapter adapter=new SelectOrderRecyclerAdapter(productslist,customercartlist,customerphonenumber);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }
}