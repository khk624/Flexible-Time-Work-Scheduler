package com.mycompany.hkhang.flexibleworktimescheduler;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    TimeScheduler timeScheduler;
    TextView textViewWeek;
    TextView textViewStartTime;
    TextView textViewEndTime;
    TextView textViewTotalTime;
    TextView textViewRemaining;

    private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);

        init();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                timeScheduler.updateInfo(year, month, dayOfMonth);
                updateUI();

                if (isDoubleClick()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Choose an action")
                            .setItems(R.array.popup_actions, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0)
                                        openTimePicker(0);
                                    else if (which == 1)
                                        openTimePicker(1);
                                    else if (which == 2)
                                        openHolidayPopup();
                                    else if (which == 3)
                                        reset();
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    //TODO find out better way for double clicking
    // workaround for find out double click, GestureDetector is not working for some reason.
    private boolean isDoubleClick() {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
            lastClickTime = clickTime;
            return true;
        } else {
            lastClickTime = clickTime;
            return false;
        }
    }

    public void openTimePicker(final int mode) {
        Calendar cal = Calendar.getInstance();
        int hours;
        int minutes;

        if (mode == 0)
            minutes = timeScheduler.getStartTime();
        else
            minutes = timeScheduler.getEndTime();

        if (minutes == 0) {
            minutes = cal.get(Calendar.HOUR) * 60;
            minutes += cal.get(Calendar.MINUTE);
        }

        hours = minutes / 60;
        minutes = minutes % 60;

        //TODO to find better Time Picker UI
        TimePickerDialog tpd = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        if (mode == 0) {
                            timeScheduler.setStartTime(hourOfDay * 60 + minute);
                        }
                        else {
                            timeScheduler.setEndTime(hourOfDay * 60 + minute);
                        }
                        updateUI();
                    }
                }, hours, minutes, false);
        tpd.show();
    }

    public void openHolidayPopup() {
        final CharSequence[] Report_items = {"Half Day Off", "Full Time Off / Holiday", "NONE"};
        int selected = 2;

        final AlertDialog.Builder holidayDialog = new AlertDialog.Builder(this);
        holidayDialog.setTitle("Day Off / Holiday");
        holidayDialog.setSingleChoiceItems(Report_items, selected,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0)
                            timeScheduler.setVacation(TimeScheduler.Vacation.HALF_DAY_OFF);
                        else if (item == 1)
                            timeScheduler.setVacation(TimeScheduler.Vacation.FULL_TIME_OFF);
                        else
                            timeScheduler.setVacation(TimeScheduler.Vacation.NONE);
                        updateUI();
                        dialog.dismiss();
                    }
                });
        AlertDialog alert_dialog = holidayDialog.create();
        alert_dialog.show();
    }

    public void reset() {
        timeScheduler.reset();
        updateUI();
    }

    public void init() {
        textViewWeek = (TextView) findViewById(R.id.textView_week);
        textViewStartTime = (TextView) findViewById(R.id.textView_startTime);
        textViewEndTime = (TextView) findViewById(R.id.textView_endTime);
        textViewTotalTime = (TextView) findViewById(R.id.textView_total_time);
        textViewRemaining = (TextView) findViewById(R.id.textView_remaining_time);

        timeScheduler = new TimeScheduler(getApplicationContext());

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        timeScheduler.updateInfo(year, month, dayOfMonth);
        updateUI();
    }

    private void updateUI() {
        StringBuilder sb = new StringBuilder();
        sb.append(timeScheduler.getMonth()+1).append("    ").append(timeScheduler.getDayOfWeek()).append("th week");

        textViewWeek.setText(sb.toString());

        textViewStartTime.setText(formatOutput(timeScheduler.getStartTime()));
        textViewEndTime.setText(formatOutput(timeScheduler.getEndTime()));

        textViewTotalTime.setText(formatOutput(timeScheduler.getTotalTime()));
        textViewRemaining.setText(formatOutput(timeScheduler.getRemainingTime()));
    }

    private String formatOutput(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02d", hours)).append(":").append(String.format("%02d", minutes));

        return sb.toString();
    }
}