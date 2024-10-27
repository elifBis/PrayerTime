package com.elifbis.ezanvakti;

import android.widget.TextView;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class PrayerTimesService {
    private OkHttpClient client;

    public PrayerTimesService() {
        this.client = new OkHttpClient();
    }

    // New method to fetch prayer times and update UI elements directly
    public void populatePrayerTimesUI(String city, String date, TextView textFajr, TextView textSunrise,
                                      TextView textZuhr, TextView textAsr, TextView textMaghrib, TextView textIsha) {
        getPrayerTimes(city, date, new PrayerTimesCallback() {
            @Override
            public void onSuccess(PrayerTimes prayerTimes) {
                // Update UI elements on the main thread
                textFajr.post(() -> textFajr.setText(prayerTimes.getFajr()));
                textSunrise.post(() -> textSunrise.setText(prayerTimes.getSunrise()));
                textZuhr.post(() -> textZuhr.setText(prayerTimes.getZuhr()));
                textAsr.post(() -> textAsr.setText(prayerTimes.getAsr()));
                textMaghrib.post(() -> textMaghrib.setText(prayerTimes.getMaghrib()));
                textIsha.post(() -> textIsha.setText(prayerTimes.getIsha()));
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void getPrayerTimes(String city, String date, PrayerTimesCallback callback) {
        String url = "https://api.aladhan.com/v1/timingsByCity?city=" + city + "&country=TR&date=" + date + "&method=13";
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONObject timings = jsonObject.getJSONObject("data").getJSONObject("timings");

                        PrayerTimes prayerTimes = new PrayerTimes(
                                timings.getString("Fajr"),
                                timings.getString("Sunrise"),
                                timings.getString("Dhuhr"),
                                timings.getString("Asr"),
                                timings.getString("Maghrib"),
                                timings.getString("Isha")
                        );

                        callback.onSuccess(prayerTimes);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onFailure(e);
                    }
                }
            }
        });
    }

    public interface PrayerTimesCallback {
        void onSuccess(PrayerTimes prayerTimes);
        void onFailure(Exception e);
    }
}
