package com.elifbis.ezanvakti;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView text_sunrise, text_fajr, text_zuhr, text_asr, text_maghrib, text_isha;
    private TextView text_fajr_time, text_sunrise_time, text_zuhr_time, text_asr_time, text_maghrib_time, text_isha_time;
    private EditText getcity; // Şehir arama için EditText
    private TextView text_city, text_date;
    private PrayerTimesService prayerTimesService;
    private LocationService locationService;
    private Button getPrayerDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        getcity = findViewById(R.id.etCitySearch);
        text_city = findViewById(R.id.tvCity);
        text_date = findViewById(R.id.tvDate);
        text_fajr = findViewById(R.id.tvFajr);
        text_sunrise = findViewById(R.id.tvSunrise);
        text_zuhr = findViewById(R.id.tvDhuhr);
        text_asr = findViewById(R.id.tvAsr);
        text_maghrib = findViewById(R.id.tvMaghrib);
        text_isha = findViewById(R.id.tvIsha);
        getPrayerDate = findViewById(R.id.btnSearch);
        text_fajr_time = findViewById(R.id.fajr_time);
        text_sunrise_time = findViewById(R.id.sunrise_time);
        text_zuhr_time = findViewById(R.id.zuhr_time);
        text_asr_time = findViewById(R.id.asr_time);
        text_maghrib_time = findViewById(R.id.maghrib_time);
        text_isha_time = findViewById(R.id.isha_time);

        // Initialize services
        prayerTimesService = new PrayerTimesService();
        locationService = new LocationService(this);

        String currentDate = locationService.getCurrentDate();
        text_date.setText(currentDate);

        // Lokasyonu al ve şehir ismini güncelle
        locationService.getLastKnownLocation(city -> {
            Log.e("LocationService", "Retrieved city: " + city); // Şehir adı alınıyor mu kontrol et
            text_city.setText(city);
            prayerTimesService.populatePrayerTimesUI(city, currentDate, text_fajr, text_sunrise, text_zuhr, text_asr, text_maghrib, text_isha);
        });

        getPrayerDate.setOnClickListener(view -> {
            String edit_city = getcity.getText().toString(); // Şehir ismini EditText'ten al
            prayerTimesService.populatePrayerTimesUI(edit_city, currentDate, text_fajr_time, text_sunrise_time, text_zuhr_time, text_asr_time, text_maghrib_time, text_isha_time);
        });
    }
}
