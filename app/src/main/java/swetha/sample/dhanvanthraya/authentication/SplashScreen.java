package swetha.sample.dhanvanthraya.authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import swetha.sample.ayushokya.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        new Handler().postDelayed(() ->
        {
                startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                finish();
        },5000);
    }
}