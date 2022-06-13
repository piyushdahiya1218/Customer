package android.example.customer.Adapters;

import android.example.customer.Classes.Product;
import android.example.customer.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SelectOrderRecyclerAdapter extends RecyclerView.Adapter<SelectOrderRecyclerAdapter.MyViewHolder>{

    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference reference=database.getReference("customercart");

    private ArrayList<Product> productslist;
    private ArrayList<Product> customercartlist;
    private String phonenumber;

    //constructor of this adapter
    public SelectOrderRecyclerAdapter(ArrayList<Product> productslist, ArrayList<Product> customercartlist, String phonenumber){
        this.productslist = productslist;
        this.customercartlist=customercartlist;
        this.phonenumber=phonenumber;
        reference.child(phonenumber).child("tprice").setValue(0);
        reference.child(phonenumber).child("titems").setValue(0);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView productimage;
        private TextView productnameeng;
        private TextView productnamehindi;
        private TextView productprice;
        private TextView productquantity;
        private Button quantityadd;
        private Button quantitysub;
        private Button addbutton;
        public MyViewHolder(final View view){
            super(view);
            productimage=view.findViewById(R.id.productimage);
            productnameeng =view.findViewById(R.id.productnameeng);
            productnamehindi=view.findViewById(R.id.productnamehindi);
            productprice=view.findViewById(R.id.productprice);
            productquantity=view.findViewById(R.id.productquantity);
            quantityadd=view.findViewById(R.id.quantityadd);
            quantitysub=view.findViewById(R.id.quantitysub);
            addbutton=view.findViewById(R.id.addbutton);
        }
    }


    @NonNull
    @Override
    public SelectOrderRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview= LayoutInflater.from(parent.getContext()).inflate(R.layout.select_order_list_items,parent,false);
        return new MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectOrderRecyclerAdapter.MyViewHolder holder, int position) {
        holder.productimage.setImageResource(customercartlist.get(holder.getAdapterPosition()).getImageRid());
        holder.productnameeng.setText(customercartlist.get(holder.getAdapterPosition()).getProductnameeng());
        holder.productnamehindi.setText(customercartlist.get(holder.getAdapterPosition()).getProductnamehindi());
        holder.productquantity.setText(String.valueOf(customercartlist.get(holder.getAdapterPosition()).getQuantity()));
        holder.productprice.setText(String.valueOf(productslist.get(holder.getAdapterPosition()).getPrice()));

        holder.quantityadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(customercartlist.get(holder.getAdapterPosition()).isSelected()){
                    if(Integer.parseInt(holder.productquantity.getText().toString()) != productslist.get(holder.getAdapterPosition()).getQuantity()){
                        customercartlist.get(holder.getAdapterPosition()).setQuantity(customercartlist.get(holder.getAdapterPosition()).getQuantity()+1);
                        holder.productquantity.setText(String.valueOf(customercartlist.get(holder.getAdapterPosition()).getQuantity()));

                        customercartlist.get(holder.getAdapterPosition()).setPrice(customercartlist.get(holder.getAdapterPosition()).getQuantity() * productslist.get(holder.getAdapterPosition()).getPrice());
                        holder.productprice.setText(String.valueOf(customercartlist.get(holder.getAdapterPosition()).getPrice()));
                        calculateTprice();
                        calculateTitems();
                    }
                }
            }
        });
        holder.quantitysub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(customercartlist.get(holder.getAdapterPosition()).isSelected()){
                    if(Integer.parseInt(holder.productquantity.getText().toString()) != 1){
                        customercartlist.get(holder.getAdapterPosition()).setQuantity(customercartlist.get(holder.getAdapterPosition()).getQuantity()-1);
                        holder.productquantity.setText(String.valueOf(customercartlist.get(holder.getAdapterPosition()).getQuantity()));

                        customercartlist.get(holder.getAdapterPosition()).setPrice(customercartlist.get(holder.getAdapterPosition()).getQuantity() * productslist.get(holder.getAdapterPosition()).getPrice());
                        holder.productprice.setText(String.valueOf(customercartlist.get(holder.getAdapterPosition()).getPrice()));
                        calculateTprice();
                        calculateTitems();
                    }
                    else{
                        customercartlist.get(holder.getAdapterPosition()).setSelected(false);
                        holder.itemView.setBackgroundResource(R.drawable.border_unselected);

                        holder.addbutton.setVisibility(View.VISIBLE);
                        holder.quantitysub.setVisibility(View.INVISIBLE);
                        calculateTprice();
                        calculateTitems();
                    }
                }
            }
        });
        holder.addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customercartlist.get(holder.getAdapterPosition()).setSelected(true);
                holder.itemView.setBackgroundResource(R.drawable.border_selected);
                calculateTprice();
                calculateTitems();

                holder.addbutton.setVisibility(View.GONE);
                holder.quantitysub.setVisibility(View.VISIBLE);
            }
        });

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(customercartlist.get(holder.getAdapterPosition()).isSelected()){
//                    customercartlist.get(holder.getAdapterPosition()).setSelected(false);
//                    holder.itemView.setBackgroundResource(R.drawable.border_unselected);
//
//                    customercartlist.get(holder.getAdapterPosition()).setQuantity(1);
//                    holder.productquantity.setText(String.valueOf(customercartlist.get(holder.getAdapterPosition()).getQuantity()));
//
//                    customercartlist.get(holder.getAdapterPosition()).setPrice(productslist.get(holder.getAdapterPosition()).getPrice());
//                    holder.productprice.setText(String.valueOf(customercartlist.get(holder.getAdapterPosition()).getPrice()));
//                    calculateTprice();
//                }
//                else{
//                    customercartlist.get(holder.getAdapterPosition()).setSelected(true);
//                    holder.itemView.setBackgroundResource(R.drawable.border_selected);
//                    calculateTprice();
//                }
//            }
//        });
    }

    private void calculateTitems() {
        int Titems=0;
        for(Product product:customercartlist){
            if(product.isSelected()){
                Titems++;
            }
        }
        reference.child(phonenumber).child("titems").setValue(Titems);
    }

    private void calculateTprice() {
        int Tprice=0;
        for(Product product:customercartlist){
            if(product.isSelected()){
                Tprice=Tprice+product.getPrice();
            }
        }
        reference.child(phonenumber).child("tprice").setValue(Tprice);
    }


    @Override
    public int getItemCount() {
        if(productslist ==null){
            return 0;
        }
        return productslist.size();
    }
}
