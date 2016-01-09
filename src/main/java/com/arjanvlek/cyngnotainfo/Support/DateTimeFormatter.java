package com.arjanvlek.cyngnotainfo.Support;

import android.support.v4.app.Fragment;
import android.content.Context;

import com.arjanvlek.cyngnotainfo.R;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class used to format the date retrieved from the update server
 */
public class DateTimeFormatter {
    private Context context;
    private Fragment fragment;

    /**
     * Create a new DateTimeFormatter.
     *
     * @param context  Application Context
     * @param fragment Currently active fragment
     */
    public DateTimeFormatter(Context context, Fragment fragment) {
        this.context = context;
        this.fragment = fragment;
    }

    public String formatDateTime(LocalDateTime rawDateTime) {
        String date = rawDateTime.getSecondOfMinute() + ":" + rawDateTime.getMinuteOfHour() + ":" + rawDateTime.getHourOfDay() + " " + rawDateTime.getDayOfMonth() + "/" + rawDateTime.getMonthOfYear() + "/" + rawDateTime.getYear();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ss:mm:hh dd/MM/yyyy", Locale.getDefault());
        Date parsedDate = null;
        try {
            parsedDate = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        String formattedDate = dateFormat.format(parsedDate);
        String formattedTime = timeFormat.format(parsedDate);
        DateTime dateTime1 = new DateTime(Integer.parseInt(String.valueOf(rawDateTime.getYear())), Integer.parseInt(String.valueOf(rawDateTime.getMonthOfYear())), Integer.parseInt(String.valueOf(rawDateTime.getDayOfMonth())), Integer.parseInt(String.valueOf(rawDateTime.getHourOfDay())), Integer.parseInt(String.valueOf(rawDateTime.getMinuteOfHour())), Integer.parseInt(String.valueOf(rawDateTime.getSecondOfMinute())));
        DateTime today = DateTime.now();
        if ((dateTime1.getDayOfMonth() == today.getDayOfMonth()) && dateTime1.getMonthOfYear() == today.getMonthOfYear()) {
            return formattedTime;
        } else if ((dateTime1.getDayOfMonth() + 1) == today.getDayOfMonth() && dateTime1.getMonthOfYear() == today.getMonthOfYear() && dateTime1.getYear() == today.getYear()) {
            return fragment.getString(R.string.yesterday) + " " + fragment.getString(R.string.at) + " " + formattedTime;
        }
        return formattedDate + " " + fragment.getString(R.string.at) + " " + formattedTime;
    }

}
