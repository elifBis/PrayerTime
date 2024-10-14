package com.elifbis.ezanvakti;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
public class MainActivity extends AppCompatActivity {

    private TextView text_sunrise, text_fajr, text_zuhr, text_asr, text_maghrib, text_isha;
    //private TextView sunrise, fajr, zuhr, asr, maghrib, isha;
    private TextView  text_city, text_date;
    //private TextView  text_city, coords_Lat, coords_Long;

    private FusedLocationProviderClient fusedLocationClient;
    private OkHttpClient client;

    private Button getPrayerDate;
    private String current_city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       /* fajr = findViewById(R.id.fajr);
        sunrise = findViewById(R.id.sunrise);
        zuhr = findViewById(R.id.zuhr);
        asr = findViewById(R.id.asr);
        maghrib = findViewById(R.id.maghrib);
        isha = findViewById(R.id.isha);*/

        text_city = findViewById(R.id.tvCity);
        text_date = findViewById(R.id.tvDate);

        text_fajr = findViewById(R.id.tvFajr);
        text_sunrise = findViewById(R.id.tvSunrise);
        text_zuhr = findViewById(R.id.tvDhuhr);
        text_asr = findViewById(R.id.tvAsr);
        text_maghrib = findViewById(R.id.tvMaghrib);
        text_isha = findViewById(R.id.tvIsha);
        /*  text_date = findViewById(R.id.textDate);
        coords_Lat = findViewById(R.id.coordLat);
        coords_Long = findViewById(R.id.coordLong);*/
        getPrayerDate = findViewById((R.id.btnSearch));


        client = new OkHttpClient();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        String currentDate = getCurrentDate();
        text_date.setText("Tarih: " + currentDate);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        getLastKnownLocation();

        getPrayerDate.setOnClickListener(view -> {
            String city = text_city.getText().toString();
            String date = getCurrentDate(); // Tarihi al
            getPrayerTimes(city, date); // Namaz vakitlerini getir
        });
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnCompleteListener(this, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    android.location.Location location = task.getResult();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    //coords_Lat.setText("latitude"+ latitude);
                    //coords_Long.setText("longitude"+ longitude);

                    // Ters geocode işlemi ile şehir bul
                    getCityFromCoordinates(latitude, longitude);
                } else {
                    text_city.setText("City: Location Not Available - Konum alınamadı");
                }
            });
        }
    }
    private void getCityFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                String city = addresses.get(0).getAdminArea(); // getLocality yerine getAdminArea kullan
                Log.d("Geocoder", "Şehir: " + city); // Şehir bilgisi burada loglanır
                if (city != null) {
                    text_city.setText("City: " + city);
                    current_city = city;

                    // Şehir tespit edildiğinde namaz vakitlerini getir
                    String date = getCurrentDate(); // Güncel tarihi al
                    getPrayerTimes(current_city, date); // Namaz vakitlerini getir

                } else {
                    text_city.setText("City: Unknown Location");
                }
            } else {
                text_city.setText("City: No addresses found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            text_city.setText("City: Error Finding Location");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation();
            }
        }
    }


    private void getPrayerTimes(String city, String date) {
        String url = "https://api.aladhan.com/v1/timingsByCity?city=" + city + "&country=TR&date=" + date + "&method=13";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String jsonResponse = response.body().string();
                    Log.d("API Response", jsonResponse);  // Log the response
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONObject data = jsonObject.getJSONObject("data");
                        JSONObject timings = data.getJSONObject("timings");

                        final String sunriseTime = timings.getString("Sunrise");
                        final String fajrTime = timings.getString("Fajr");
                        final String zuhrTime = timings.getString("Dhuhr");
                        final String asrTime = timings.getString("Asr");
                        final String maghribTime = timings.getString("Maghrib");
                        final String ishaTime = timings.getString("Isha");

                        runOnUiThread(() -> {
                            text_sunrise.setText("Sunrise: " + sunriseTime);
                            text_fajr.setText("Fajr: " + fajrTime);
                            text_zuhr.setText("Dhuhr: " + zuhrTime);
                            text_asr.setText("Asr: " + asrTime);
                            text_maghrib.setText("Maghrib: " + maghribTime);
                            text_isha.setText("Isha: " + ishaTime);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("API Error", "Response not successful: " + response.message());
                }
            }
        });
    }
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    }


