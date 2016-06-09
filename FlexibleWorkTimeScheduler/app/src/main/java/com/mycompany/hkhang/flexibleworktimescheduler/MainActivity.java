package com.mycompany.hkhang.flexibleworktimescheduler;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.sql.Time;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    int start;
    int end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CalendarView calendarView=(CalendarView) findViewById(R.id.calendarView);

        updateCurrentDate();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                Calendar cal = new GregorianCalendar();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DATE, dayOfMonth);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setTimeZone(cal.getTimeZone());

                String date = dateFormat.format(cal.getTime());
                String weekInfo = formatWeekInfoOuput(date, cal.get(Calendar.WEEK_OF_MONTH));

                TextView textViewWeek = (TextView) findViewById(R.id.textView_week);
                textViewWeek.setText(weekInfo);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose an action")
                        .setItems(R.array.popup_actions, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0)
                                    openDatePicker(0);
                                else if (which == 1)
                                    openDatePicker(1);
                                else if (which == 2)
                                    openHolidayPopup();
                                else if (which == 3)
                                    reset();
                            }
                        });

                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public void openDatePicker(final int mode) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR);
        int minute = cal.get(Calendar.MINUTE);

        TimePickerDialog tpd = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        if (mode == 0) {
                            start = hourOfDay * 60 + minute;
                        }
                        else if (mode == 1) {
                            end = hourOfDay * 60 + minute;
                        }
                    }
                }, hour, minute, false);
        tpd.show();
    }

    public void openHolidayPopup() {

    }

    public void reset() {

    }

    public void updateCurrentDate() {
        Calendar cal = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(cal.getTimeZone());

        String currentDate = dateFormat.format(cal.getTime());
        String weekInfo = formatWeekInfoOuput(currentDate, cal.get(Calendar.WEEK_OF_MONTH));

        TextView textViewWeek = (TextView) findViewById(R.id.textView_week);
        textViewWeek.setText(weekInfo);
    }

    public String formatWeekInfoOuput(String date, int nthWeek) {
        StringBuilder sb = new StringBuilder();

        sb.append(date).append("    ");
        sb.append(nthWeek);

        if (nthWeek == 1)
            sb.append("st");
        else if (nthWeek == 2)
            sb.append("nd");
        else if (nthWeek == 3)
            sb.append("rd");
        else
            sb.append("th");
        sb.append(" Week");

        return sb.toString();
    }
}
