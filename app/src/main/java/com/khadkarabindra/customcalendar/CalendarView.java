package com.khadkarabindra.customcalendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by maxx on 6/29/17.
 */

public class CalendarView extends LinearLayout {

    // for logging
    private static final String LOGTAG = "Calendar View";

    // how many days to show, defaults to six weeks, 42 days
    private static final int DAYS_COUNT = 42;

    // default date format
    private static final String DATE_FORMAT = "MMM yyyy";

    // date format
    private String dateFormat;

    // current displayed month
    private Calendar currentDate = Calendar.getInstance();
    private Calendar today = Calendar.getInstance();

    //event handling
    // internal components
    private LinearLayout header;
    private LinearLayout llPrev;
    private LinearLayout llNext;
    private TextView txtDate;
    private RecyclerView grid;

    public CalendarView(Context context) {
        super(context);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initControl(context, attrs);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initControl(context, attrs);
    }

    /**
     * Load control xml layout
     */
    private void initControl(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.control_calendar, this);

        loadDateFormat(attrs);
        assignUiElements();
        assignClickHandlers();

        updateCalendar();
    }

    private void loadDateFormat(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarView);

        try {
            // try to load provided date format, and fallback to default otherwise
            dateFormat = ta.getString(R.styleable.CalendarView_dateFormat);
            if (dateFormat == null)
                dateFormat = DATE_FORMAT;
        } finally {
            ta.recycle();
        }
    }

    private void assignUiElements() {
        // layout is inflated, assign local variables to components
        header = (LinearLayout) findViewById(R.id.calendar_header);
        llPrev = (LinearLayout) findViewById(R.id.prevLL);
        llNext = (LinearLayout) findViewById(R.id.nextLL);
        txtDate = (TextView) findViewById(R.id.calendar_date_display);

        grid = (RecyclerView) findViewById(R.id.calendar_grid);
        grid.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        grid.setLayoutManager(layoutManager);
    }

    private void assignClickHandlers() {
        // add one month and refresh UI
        llNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, 1);
                today.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });

        // subtract one month and refresh UI
        llPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, -1);
                today.add(Calendar.MONTH, -1);
                updateCalendar();
            }
        });

    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar() {
        updateCalendar(null);
    }

    /**
     * Display dates correctly in grid
     */
    public void updateCalendar(HashSet<Date> events) {
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar) currentDate.clone();

        // determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // fill cells
        while (cells.size() < DAYS_COUNT) {
            cells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // update grid
        grid.setAdapter(new CalendarAdapter(getContext(), cells, events));

        // update title
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        txtDate.setText(sdf.format(currentDate.getTime()));

    }

    private class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
        // days with events
        private HashSet<Date> eventDays;
        private ArrayList<Date> days;
        private Context context;

        // for view inflation
        private LayoutInflater inflater;

        public CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Date> eventDays) {
            this.eventDays = eventDays;
            this.days = days;
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.control_calendar_day, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            Date date = days.get(position);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            // clear styling
            holder.tvDay.setTypeface(null, Typeface.NORMAL);
            holder.tvDay.setTextColor(Color.WHITE);

            // clear styling
            /*((TextView) view).setTypeface(null, Typeface.NORMAL);
            ((TextView) view).setTextColor(Color.BLACK);*/


            if (month != today.get(Calendar.MONTH)) {
                holder.tvDay.setTextColor(ContextCompat.getColor(getContext(), R.color.full_transparent));
            } else {
                holder.tvDay.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
                holder.tvDay.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            }

        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvDay;

            public ViewHolder(View view) {
                super(view);

                tvDay = (TextView) view.findViewById(R.id.dayTV);
            }
        }
    }

}
