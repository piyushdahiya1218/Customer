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

public class OrderAcceptedRecyclerAdapter extends RecyclerView.Adapter<OrderAcceptedRecyclerAdapter.MyViewHolder>{

    private ArrayList<Product> customercartlist;

    public OrderAcceptedRecyclerAdapter(ArrayList<Product> customercartlist){
        this.customercartlist = customercartlist;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView productimage;
        private TextView productnameeng;
        private TextView productnamehindi;
        private TextView productprice;
        private TextView productquantity;

        public MyViewHolder(@NonNull View view) {
            super(view);
            productimage=view.findViewById(R.id.productimage_accepted);
            productnameeng =view.findViewById(R.id.productnameeng_accepted);
            productnamehindi=view.findViewById(R.id.productnamehindi_accepted);
            productprice=view.findViewById(R.id.productprice_accepted);
            productquantity=view.findViewById(R.id.productquantity_accepted);
        }
    }

    @NonNull
    @Override
    public OrderAcceptedRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemview= LayoutInflater.from(parent.getContext()).inflate(R.layout.order_accepted_list_items,parent,false);
        return new OrderAcceptedRecyclerAdapter.MyViewHolder(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAcceptedRecyclerAdapter.MyViewHolder holder, int position) {
        holder.productimage.setImageResource(customercartlist.get(holder.getAdapterPosition()).getImageRid());
        holder.productnameeng.setText(customercartlist.get(holder.getAdapterPosition()).getProductnameeng());
        holder.productnamehindi.setText(customercartlist.get(holder.getAdapterPosition()).getProductnamehindi());
        holder.productquantity.setText(String.valueOf(customercartlist.get(holder.getAdapterPosition()).getQuantity())+"kg");
        holder.productprice.setText("Rs. "+String.valueOf(customercartlist.get(holder.getAdapterPosition()).getPrice()));
    }

    @Override
    public int getItemCount() {
        return customercartlist.size();
    }
}
