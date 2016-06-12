package com.mycompany.hkhang.flexibleworktimescheduler;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by hkhang on 6/10/16.
 */

public class TimeScheduler {
    enum Vacation {
        HALF_DAY_OFF,
        FULL_TIME_OFF,
        NONE
    }

    Context context;
    // Time information is minutes based. You should convert into hours and minutes
    int year;
    int month;
    int dayOfMonth;
    int dayOfWeek;

    int startTime;
    int endTime;
    Vacation vacation;
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

        SharedPreferences sharedPref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
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
                int vac = jsonObject.getInt("vacation");
                if (vac == 0)
                    vacation = Vacation.HALF_DAY_OFF;
                else if (vac == 1)
                    vacation = Vacation.FULL_TIME_OFF;
                else
                    vacation = Vacation.NONE;
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

        SharedPreferences sharedPref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        String jsonString = sharedPref.getString(strDate, null);

        if (jsonString == null)
            return 0;

        int startTime = 0;
        int endTime = 0;
        Vacation vacation = Vacation.NONE;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            startTime = jsonObject.getInt("startTime");
            endTime = jsonObject.getInt("endTime");
            int vac = jsonObject.getInt("vacation");
            if (vac == 0)
                vacation = Vacation.HALF_DAY_OFF;
            else if (vac == 1)
                vacation = Vacation.FULL_TIME_OFF;
            else
                vacation = Vacation.NONE;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (vacation == Vacation.FULL_TIME_OFF)
            return 8 * 60;
        else if (startTime == 0 || startTime > endTime) {
            if (vacation == Vacation.HALF_DAY_OFF)
                return 4 * 60;
            else
                return 0;
        }
        else {
            int workingTime = endTime - startTime;
            workingTime = workingTime - getTotalMealTime(startTime, endTime);
            if (vacation == Vacation.HALF_DAY_OFF)
                workingTime += 4 * 60;

            if (workingTime > 12 * 60)
                return 12 * 60;
            else
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

        setTime(startTime, endTime, vacation);
        updateInfo(year, month, dayOfMonth);
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;

        setTime(startTime, endTime, vacation);
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

    public void setVacation(Vacation vacation) {
        if (vacation == Vacation.FULL_TIME_OFF) {
            startTime = 0;
            endTime = 0;
        }
        setTime(startTime, endTime, vacation);
        updateInfo(year, month, dayOfMonth);
    }

    public Vacation getVacation() {
        return vacation;
    }

    private void setTime(int startTime, int endTime, Vacation vacation) {
        StringBuilder sb = new StringBuilder();
        sb.append(year).append("/").append(month).append("/").append(dayOfMonth);
        String strDate = sb.toString();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("startTime", startTime);
            jsonObject.put("endTime", endTime);

            int vac;
            if (vacation == Vacation.HALF_DAY_OFF)
                vac = 0;
            else if (vacation == Vacation.FULL_TIME_OFF)
                vac = 1;
            else
                vac = 2;
            jsonObject.put("vacation", vac);

            SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(strDate, jsonObject.toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        this.startTime = 0;
        this.endTime = 0;
        this.vacation = Vacation.NONE;

        setTime(startTime, endTime, vacation);
        updateInfo(year, month, dayOfMonth);
    }
}
