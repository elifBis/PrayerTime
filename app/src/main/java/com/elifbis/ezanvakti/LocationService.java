package com.elifbis.ezanvakti;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LocationService {

    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;

    public LocationService(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    // Tarih bilgisini güncel olarak döndürür
    public String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    // Şehir bilgisini almak için son bilinen konumu kullanır
    public void getLastKnownLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

    // Enlem ve boylamdan şehir adını alır
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

    // Şehir ismi almak için callback interface
    public interface LocationCallback {
        void onCityReady(String city);
    }
}
