package android.example.customer.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.example.customer.R;
import android.content.Intent;
import android.os.Bundle;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        startActivity(new Intent(SplashScreenActivity.this, PhoneNumberActivity.class));
        finish();
    }
}