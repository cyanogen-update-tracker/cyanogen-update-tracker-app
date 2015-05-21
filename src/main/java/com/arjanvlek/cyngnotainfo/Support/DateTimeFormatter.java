package com.arjanvlek.cyngnotainfo.Support;

import android.support.v4.app.Fragment;
import android.content.Context;

import com.arjanvlek.cyngnotainfo.R;

import org.joda.time.DateTime;

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
     * @param context Application Context
     * @param fragment Currently active fragment
     */
    public DateTimeFormatter(Context context, Fragment fragment) {
        this.context = context;
        this.fragment = fragment;
    }

    /**
     * Formats a raw date time string to a localized Android date time string
     * @param rawDateTime Date from the update server
     * @return Date string in local format (checks for 12/24 hour and regional date notation)
     */
    public String formatDateTime(String rawDateTime) {

        String year = rawDateTime.substring(0,4);
        String month = rawDateTime.substring(5,7);
        String day = rawDateTime.substring(8,10);
        String hours = rawDateTime.substring(11,13);
        String minutes = rawDateTime.substring(14,16);
        String seconds = rawDateTime.substring(17,19);

        String date = seconds + ":"+ minutes + ":" + hours + " " + day + "/" + month + "/" + year;
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
        DateTime dateTime1 = new DateTime(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day),Integer.parseInt(hours),Integer.parseInt(minutes),Integer.parseInt(seconds));
        DateTime today = DateTime.now();
        if((dateTime1.getDayOfMonth() == today.getDayOfMonth() )&& dateTime1.getMonthOfYear() == today.getMonthOfYear()){
            return fragment.getString(R.string.today) + " " + fragment.getString(R.string.at) + " " +  formattedTime;
        }
        else if((dateTime1.getDayOfMonth() + 1) == today.getDayOfMonth() && dateTime1.getMonthOfYear() == today.getMonthOfYear() && dateTime1.getYear() == today.getYear()) {
            return fragment.getString(R.string.yesterday) + " "  + fragment.getString(R.string.at) + " " + formattedTime;
        }
        return formattedDate + " " + fragment.getString(R.string.at) + " " + formattedTime;
    }

}
