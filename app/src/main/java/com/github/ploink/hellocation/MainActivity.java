package com.github.ploink.hellocation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button buttonLastKnown;
    EditText text;
    EditText editInterval;
    LocationManager lm = null;
    Switch switchInterval;
    Spinner spinnerProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = findViewById(R.id.myTextView);
        buttonLastKnown = findViewById(R.id.button1);
        editInterval = findViewById(R.id.editText);
        switchInterval = findViewById(R.id.switch1);
        spinnerProvider = findViewById(R.id.spinner1);
        text.setMovementMethod(new ScrollingMovementMethod());
        text.setShowSoftInputOnFocus(false);
        addListeners();
        startLocationManager();
    }

    public void log(String s) {
        text.append(s + "\n");
        text.setSelection(text.getText().length());
        text.setTextIsSelectable(true);
    }

    public boolean startLocationManager() {
        if (lm == null) {
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (lm == null) {
                log("✕ Location service not found");
                return false;
            }
        }

        List<String> providers = lm.getProviders(false);
        if (providers==null || providers.isEmpty()) {
            log("✕ No location providers available. Asking permission.");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            return false;
        }
        if (spinnerProvider.getCount() == 0) {
            ArrayAdapter<String> providerArray = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, providers);
            spinnerProvider.setAdapter(providerArray);
        }
        return true;
    }

    public void getLastKnownLocation() {
        if (!startLocationManager()) return;
        try {
            String provider = spinnerProvider.getSelectedItem().toString();
            Location location = lm.getLastKnownLocation(provider);
            if (location == null) log("✕ No last known location for provider " + provider);
            else log("✓ Last known " + location.toString());
        } catch (SecurityException e) {
            log(e.getMessage());
        }
    }

    public boolean locationStart(boolean start) {
        if (!startLocationManager()) return false;
        if (!start) {
            lm.removeUpdates(mLocationListener);
            log("✕ Stopped location updates");
            return false;
        }
        int i;
        try {
            i = Integer.parseInt(editInterval.getText().toString()) * 1000;
        } catch (NumberFormatException e) {
            i = 0;
            editInterval.setText("0");
        }
        try {
            String provider = spinnerProvider.getSelectedItem().toString();
            lm.requestLocationUpdates(provider, i, 0, mLocationListener);
            log("✓ Started location updates for provider " + provider);
            return true;
        } catch (SecurityException e) {
            log("✕ " + e.getMessage());
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public synchronized void addListeners() {
        buttonLastKnown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getLastKnownLocation();
            }
        });

        switchInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (switchInterval.isChecked()) {
                    if (!locationStart(true)) switchInterval.setChecked(false);
                } else locationStart(false);
            }
        });

    }

    final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            log("✓ Updated " + location.toString());
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
