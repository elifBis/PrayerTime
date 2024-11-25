package com.elifbis.ezanvakti;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LocationService {

    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;
    private boolean isLocationReceived = false;


    private LocationRequest locationRequest;
    private com.google.android.gms.location.LocationCallback locationCallback;
    public LocationService(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }
    public void setupLocationUpdates(LocationCallback callback) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 saniyede bir güncelle
        locationRequest.setFastestInterval(5000); // En hızlı 5 saniyede bir güncelle

        locationCallback = new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        getCityFromCoordinates(latitude, longitude, callback);
                    }
                }
            }
        };
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
    public String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    public void getLastKnownLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (!isLocationEnabled()) {
                showLocationSettingsAlert();
                callback.onCityReady("Konum servisi kapalı. Lütfen açın.");
                return;
            }

            fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    getCityFromCoordinates(location.getLatitude(), location.getLongitude(), callback);
                } else {
                    callback.onCityReady("Unknown Location");
                }
            });
        } else {
            ActivityCompat.requestPermissions(
                    (MainActivity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1
            );
        }
    }

    private void getCityFromCoordinates(double latitude, double longitude, LocationCallback callback) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String city = addresses.get(0).getAdminArea();
                if (city != null) {
                    callback.onCityReady(city);
                } else {
                    callback.onCityReady("Unknown Location");
                }
            } else {
                callback.onCityReady("No addresses found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            callback.onCityReady("Error Finding Location");
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showLocationSettingsAlert() {
        new AlertDialog.Builder(context)
                .setTitle("Konum Servisi Kapalı")
                .setMessage("Uygulamanın düzgün çalışması için konum servisini açmanız gerekiyor. Ayarlarınızı kontrol edin.")
                .setPositiveButton("Ayarlar", (DialogInterface dialog, int which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                })
                .setNegativeButton("İptal", (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(context, "Konum servisi kapalı. Uygulama sınırlı çalışabilir.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    public interface LocationCallback {
        void onCityReady(String city);
    }
}
