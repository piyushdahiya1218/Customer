package android.example.customer.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.example.customer.R;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PhoneNumberActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        EditText getphonenumber=findViewById(R.id.phonenumber);

        Button submitbutton=findViewById(R.id.submitbutton);
        submitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String customerphonenumber=getphonenumber.getText().toString();
                if(!customerphonenumber.equals("")){
                    Intent intent=new Intent(getApplicationContext(), HomePageActivity.class);
                    intent.putExtra("customerphonenumber", customerphonenumber);
                    startActivity(intent);
                }
            }
        });
    }
}