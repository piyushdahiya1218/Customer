package android.example.customer.Adapters;

import android.content.Context;
import android.example.customer.R;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{

    private final View window;
    private Context context;
    GoogleMap map;

    public CustomInfoWindowAdapter(Context context, GoogleMap map) {
        this.context = context;
        window= LayoutInflater.from(context).inflate(R.layout.custom_info_window,null);
        this.map=map;
    }

    private void renderWindowText(Marker marker, View view){
        String title=marker.getTitle();
        TextView titletextview=view.findViewById(R.id.title);
        titletextview.setText(title);

        String description=marker.getSnippet();
        TextView descriptiontextview=view.findViewById(R.id.description);
        descriptiontextview.setText(description);
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        if (marker.getTitle().equals("My Location")){
            return null;
        }
        else{
            renderWindowText(marker, window);
            return window;
        }
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        if (marker.getTitle().equals("My Location")){
            return null;
        }
        else{
            renderWindowText(marker, window);
            return window;
        }
    }
}
