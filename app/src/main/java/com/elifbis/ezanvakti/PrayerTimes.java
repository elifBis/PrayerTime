package com.elifbis.ezanvakti;

public class PrayerTimes {
    private String fajr;
    private String sunrise;
    private String zuhr;
    private String asr;
    private String maghrib;
    private String isha;

    public PrayerTimes(String fajr, String sunrise, String zuhr, String asr, String maghrib, String isha) {
        this.fajr = fajr;
        this.sunrise = sunrise;
        this.zuhr = zuhr;
        this.asr = asr;
        this.maghrib = maghrib;
        this.isha = isha;
    }

    // Getter ve Setter metodları
    public String getFajr() { return fajr; }
    public String getSunrise() { return sunrise; }
    public String getZuhr() { return zuhr; }
    public String getAsr() { return asr; }
    public String getMaghrib() { return maghrib; }
    public String getIsha() { return isha; }
}
