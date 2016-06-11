package com.mycompany.hkhang.flexibleworktimescheduler;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by hkhang on 6/10/16.
 */

public class TimeScheduler {
    Context context;
    // Time information is minutes based. You should convert into hours and minutes
    int year;
    int month;
    int dayOfMonth;
    int dayOfWeek;

    int startTime;
    int endTime;
    int totalTime;
    int remainingTime;

    TimeScheduler(Context context) {
        this.context = context;
    }

    public void updateInfo(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;

        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DATE, dayOfMonth);

        this.dayOfWeek = cal.get(Calendar.WEEK_OF_MONTH);

        StringBuilder sb = new StringBuilder();
        sb.append(year).append("/").append(month).append("/").append(dayOfMonth);
        String strDate = sb.toString();

        SharedPreferences sharedPref = context.getSharedPreferences("TEST", Context.MODE_PRIVATE);
        String jsonString = sharedPref.getString(strDate, null);

        if (jsonString == null) {
            startTime = 0;
            endTime = 0;
        }
        else {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                startTime = jsonObject.getInt("startTime");
                endTime = jsonObject.getInt("endTime");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        totalTime = updateTotalTime();

        remainingTime = (40 * 60) - totalTime;
        if (remainingTime < 0)
            remainingTime = 0;
    }

    private int updateTotalTime() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DATE, dayOfMonth);

        int day = cal.get(Calendar.DAY_OF_WEEK);
        int index = dayOfMonth;

        switch (day) {
            case Calendar.SUNDAY:
                index++;
                break;
            case Calendar.MONDAY:
                break;
            case Calendar.TUESDAY:
                index--;
                break;
            case Calendar.WEDNESDAY:
                index -= 2;
                break;
            case Calendar.THURSDAY:
                index -= 3;
                break;
            case Calendar.FRIDAY:
                index -= 4;
                break;
            case Calendar.SATURDAY:
                index -= 5;
                break;
        }

        int totalTime = 0;
        for (int i = 0; i < 5; i++)
            totalTime += getDayWorkingHour(year, month, index+i);
        return totalTime;
    }

    // date format "YYYY/MM/DD"
    public int getDayWorkingHour(int year, int month, int dayOfMonth) {
        StringBuilder sb = new StringBuilder();
        sb.append(year).append("/").append(month).append("/").append(dayOfMonth);
        String strDate = sb.toString();

        SharedPreferences sharedPref = context.getSharedPreferences("TEST", Context.MODE_PRIVATE);
        String jsonString = sharedPref.getString(strDate, null);

        if (jsonString == null)
            return 0;

        int startTime = 0;
        int endTime = 0;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            startTime = jsonObject.getInt("startTime");
            endTime = jsonObject.getInt("endTime");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (startTime == 0 || startTime > endTime)
            return 0;
        else {
            int workingTime = endTime - startTime;
            workingTime = workingTime - getTotalMealTime(startTime, endTime);
            return workingTime;
        }
    }

    private int getTotalMealTime(int startTime, int endTime) {
        int lunchStartTime = 12 * 60;
        int dinnerStartTime = 18 * 60;

        if (startTime < lunchStartTime) {
            if (endTime < lunchStartTime)
                return 0;
            else if (endTime >= lunchStartTime && endTime < lunchStartTime + 60)
                return endTime - lunchStartTime;
            else {
                if (endTime < dinnerStartTime)
                    return 60;
                else if (endTime >= dinnerStartTime && endTime < dinnerStartTime + 60)
                    return endTime - dinnerStartTime + 60;
                else
                    return 120;
            }
        }
        else if (startTime >= lunchStartTime && startTime < lunchStartTime + 60) {
            int missingTime = lunchStartTime + 60 - startTime;

            if (endTime < dinnerStartTime)
                return missingTime;
            else if (endTime >= dinnerStartTime && endTime < dinnerStartTime + 60)
                return endTime - dinnerStartTime + missingTime;
            else
                return missingTime + 60;
        }
        else if (startTime < dinnerStartTime) {
            if (endTime < dinnerStartTime)
                return 0;
            else if (endTime >= dinnerStartTime && endTime < dinnerStartTime + 60)
                return endTime - dinnerStartTime;
            else
                return 60;
        }
        else if (startTime >= dinnerStartTime && startTime < dinnerStartTime + 60) {
            if (endTime >= dinnerStartTime && endTime < dinnerStartTime + 60)
                return 0;
            else
                return dinnerStartTime + 60 - endTime;
        }
        else {
            return 0;
        }
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;

        StringBuilder sb = new StringBuilder();
        sb.append(year).append("/").append(month).append("/").append(dayOfMonth);
        String strDate = sb.toString();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("startTime", startTime);
            jsonObject.put("endTime", endTime);

            SharedPreferences sp = context.getSharedPreferences("TEST", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(strDate, jsonObject.toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateInfo(year, month, dayOfMonth);
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;

        StringBuilder sb = new StringBuilder();
        sb.append(year).append("/").append(month).append("/").append(dayOfMonth);
        String strDate = sb.toString();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("startTime", startTime);
            jsonObject.put("endTime", endTime);

            SharedPreferences sp = context.getSharedPreferences("TEST", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(strDate, jsonObject.toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateInfo(year, month, dayOfMonth);
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }
}
