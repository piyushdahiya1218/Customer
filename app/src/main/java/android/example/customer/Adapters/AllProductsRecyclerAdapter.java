package android.example.customer.Adapters;

import android.example.customer.Classes.Product;
import android.example.customer.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AllProductsRecyclerAdapter extends RecyclerView.Adapter<AllProductsRecyclerAdapter.MyViewHolder>{

    private ArrayList<Product> list;

    public AllProductsRecyclerAdapter(ArrayList<Product> list){
        this.list=list;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView productimage;
        private TextView productnameeng;
        private TextView productnamehindi;
        public MyViewHolder(@NonNull View view) {
            super(view);
            productimage=view.findViewById(R.id.homepageproductimage);
            productnameeng =view.findViewById(R.id.homepageproductsnameeng);
            productnamehindi=view.findViewById(R.id.homepageproductsnamehindi);
        }
    }

    @NonNull
    @Override
    public AllProductsRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_products_list_items,parent,false);
        return new AllProductsRecyclerAdapter.MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull AllProductsRecyclerAdapter.MyViewHolder holder, int position) {
        holder.productimage.setImageResource(list.get(holder.getAdapterPosition()).getImageRid());
        holder.productnameeng.setText(list.get(holder.getAdapterPosition()).getProductnameeng());
        holder.productnamehindi.setText(list.get(holder.getAdapterPosition()).getProductnamehindi());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
