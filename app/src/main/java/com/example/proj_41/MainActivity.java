package com.example.proj_41;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.internal.NavigationMenu;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.github.yavski.fabspeeddial.FabSpeedDial;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    TextView totalView;
    TextView dateView;
    Button saveButton;
    ConstraintLayout topMenu;
    //date change menu
    Button rngsbtn;
    Button rngebtn;
    Spinner spinnerrange;
    Spinner spinnergrouping;
    //Switch switchmode;
    //graph things
    CombinedChart chart;
    //datepicker QOL things
    int lastsyear;
    int lastsmonth;
    int lastsday;
    int lasteyear;
    int lastemonth;
    int lasteday;
    boolean pickermode;
    //userlimits
    float limits[]; //[0] = per-purchase, [1] = daily, [2] = weekly, [3] = monthly, [4] = yearly
    int limitmode;
    float x1,x2,y1,y2;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemClock.sleep(100);
        final FabSpeedDial fabSpeedDial = findViewById(R.id.fabSpeedDial);
        chart = findViewById(R.id.chart1);
        fabSpeedDial.bringToFront();

        //setting up date range things
        final android.icu.util.Calendar calendar = android.icu.util.Calendar.getInstance();
        lastsyear = lasteyear = calendar.get(Calendar.YEAR);
        lastsmonth = lastemonth = calendar.get(Calendar.MONTH);
        lastsday = lasteday = calendar.get(Calendar.DAY_OF_MONTH);
        
        //setting up limit things; later add a way to set this
        limits = new float[]{0, 0, 0, 0, 0};
        limitmode = 0;
        limits[3] = 1000;
        limits[0] = 20;
        calclimits(3);

        //date change menu
            rngsbtn = findViewById(R.id.rangestartbtn);
            rngebtn = findViewById(R.id.rangeendbtn);
            rngsbtn.setEnabled(false);
            rngebtn.setEnabled(false);
            pickermode = true;

            //range buttons create datepickerdialogs
            rngsbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickermode = true;

                    //create bundles to hold the current date; probably a better way to do this
                    Bundle currentdate = new Bundle();
                    currentdate.putInt("Year", lastsyear);
                    currentdate.putInt("Month", lastsmonth);
                    currentdate.putInt("Day", lastsday);

                    //create and show the datepickerdialog
                    DialogFragment datepicker = new DatePickerFragment();
                    ((DatePickerFragment)datepicker).setstartdate(currentdate);
                    datepicker.show(getSupportFragmentManager(), "datePicker");
                }
            });

            rngebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickermode = false;
                    Bundle currentdate = new Bundle();
                    currentdate.putInt("Year", lasteyear);
                    currentdate.putInt("Month", lastemonth);
                    currentdate.putInt("Day", lasteday);
                    DialogFragment datepicker = new DatePickerFragment();
                    ((DatePickerFragment)datepicker).setstartdate(currentdate);
                    datepicker.show(getSupportFragmentManager(), "datePicker");
                }
            });

            //creating the spinners
            ArrayList<String> ranges = new ArrayList<String>();
            ranges.add("All Time");
            ranges.add("Custom Range");
            ranges.add("Single Day");
            ArrayAdapter<String> modesadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, ranges);
            spinnerrange = findViewById(R.id.spinnerDay);
            spinnerrange.setAdapter(modesadapter);
            //this spinner controls which other options are enabled, so it has a listener
            spinnerrange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(position == 0){  //range = all
                        rngsbtn.setEnabled(false);
                        rngebtn.setEnabled(false);
                        spinnergrouping.setEnabled(true);
                    }
                    else if(position == 1){ //range = custom range
                        rngsbtn.setEnabled(true);
                        rngebtn.setEnabled(true);
                        spinnergrouping.setEnabled(true);
                    }
                    else{ // range = one day
                        rngsbtn.setEnabled(true);
                        rngebtn.setEnabled(false);
                        spinnergrouping.setEnabled(false);
                        spinnergrouping.setSelection(0);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //don't think anything needs to be done here
                }
            });

            ArrayList<String> grouping = new ArrayList<>();
            grouping.add("No Grouping");
            grouping.add("Group by Day");
            grouping.add("Group by Week");
            grouping.add("Group by Month");
            grouping.add("Group by Year");
            ArrayAdapter<String> groupingadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, grouping);
            spinnergrouping = findViewById(R.id.spinnerGrouping);
            spinnergrouping.setAdapter(groupingadapter);

            //later, add in the mode button for switching between single and cumulative modes
        //end change menu
        //floting menu

        fabSpeedDial.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                if(menuItem.getTitle().equals("Add Receipt")){
                    openCamera();
                }
                else if(menuItem.getTitle().equals("About")){
                    openAbout();
                }
                return true;
            }

            @Override
            public void onMenuClosed() {

            }
        });
        //initate visuals
        dateView=findViewById(R.id.DateView);
        topMenu=findViewById(R.id.topMenu);
        saveButton=findViewById(R.id.saveButton);
        //set visibility & date
        final String Date =  DateFormat.getDateInstance().format(calendar.getTime());
        dateView.setText("All Receipts");
        rngsbtn.setText(Date);
        rngebtn.setText(Date);

        //this sets the graph and chart raw values, very important
        ArrayList<BarEntry> initdata = Allrecipts();

        topMenu.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        //change button (replaced with date view so clicking the date brings up the menu)
        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.setVisibility(View.VISIBLE);
                topMenu.setVisibility(View.VISIBLE);
                chart.setVisibility(View.GONE);
            }
        });//click to change date
        //Save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the date/range selected from the button text
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
                String dateSelected = rngsbtn.getText().toString();
                String enddate = rngebtn.getText().toString();
                try{
                    Date start = sdf.parse(dateSelected);
                    calendar.setTime(start);
                    lastsday = calendar.get(Calendar.DAY_OF_MONTH);
                    lastsmonth = calendar.get(Calendar.MONTH);
                    lastsyear = calendar.get(Calendar.YEAR);
                    Date end = sdf.parse(enddate);
                    calendar.setTime(end);
                    lasteday = calendar.get(Calendar.DAY_OF_MONTH);
                    lastemonth = calendar.get(Calendar.MONTH);
                    lasteyear = calendar.get(Calendar.YEAR);
                }
                catch(Exception e){

                }
                ArrayList<BarEntry> newdata;

                //generate new data based on the state of the various options
                XAxis xAxis = chart.getXAxis();
                if(spinnerrange.getSelectedItem().toString().equals("All Time")){
                    newdata = Allrecipts();
                    dateView.setText("All Receipts");
                }
                else if(spinnerrange.getSelectedItem().toString().equals("Single Day")){
                    newdata = Allrecipts(dateSelected);
                    dateView.setText(dateSelected);
                }
                else{
                    newdata = Allrecipts(dateSelected, enddate);
                    dateView.setText(dateSelected + " - " + enddate);
                }

                //format the chart differently based on the grouping; mostly just the x axis
                limitmode = spinnergrouping.getSelectedItemPosition();
                if(limitmode == 0){ //no group
                    chart.setVisibleXRangeMinimum(5);
                    chart.setVisibleXRangeMaximum(14);
                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return String.valueOf((int)value);
                        }
                    });
                    xAxis.setGranularity(1f); // only intervals of 1 day

                }
                else if(limitmode == 1){ //group by day
                    chart.setVisibleXRangeMinimum(5);
                    chart.setVisibleXRangeMaximum(7);
                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            calendar.set(lastsyear, lastsmonth, lastsday);
                            calendar.add(Calendar.DATE, (int) value);
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
                            return sdf.format(calendar.getTime());
                        }
                    });
                    xAxis.setGranularity(1f); // only intervals of 1 day
                }
                else if(limitmode == 2){ //group by week
                    chart.setVisibleXRangeMinimum(3);
                    chart.setVisibleXRangeMaximum(10);
                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            calendar.set(lastsyear, lastsmonth, lastsday);
                            calendar.add(Calendar.WEEK_OF_MONTH, (int) value);
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");
                            return sdf.format(calendar.getTime());
                        }
                    });
                    xAxis.setGranularity(4f);
                }
                else if(limitmode == 3){ //group by month
                    chart.setVisibleXRangeMinimum(3);
                    chart.setVisibleXRangeMaximum(12);
                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            calendar.set(lastsyear, lastsmonth, lastsday);
                            calendar.add(Calendar.MONTH, (int) value);
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");
                            return sdf.format(calendar.getTime());
                        }
                    });
                    xAxis.setGranularity(1f); 
                }
                else if(limitmode == 4){ //group by year
                    chart.setVisibleXRangeMinimum(2);
                    chart.setVisibleXRangeMaximum(5);
                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            calendar.set(lastsyear, lastsmonth, lastsday);
                            calendar.add(Calendar.YEAR, (int) value);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                            return sdf.format(calendar.getTime());
                        }
                    });
                    xAxis.setGranularity(1f);
                }

                //hide the menu
                topMenu.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);
                chart.setVisibility(View.VISIBLE);
                updatechart(newdata);
            }
        });

        //chart things
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setTextColor(Color.LTGRAY);

        ValueFormatter custom = new MyValueFormatter("$");
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setTextColor(Color.LTGRAY);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setTextColor(Color.LTGRAY);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        l.setTextColor(Color.LTGRAY);
        chart.setExtraOffsets(0, 0, 0, 10);

        //finally, do the first update of the chart with the initial settings
        updatechart(initdata);

    }//close on create
    //swiping
    public boolean onTouchEvent(MotionEvent touchevent) {

        switch (touchevent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchevent.getX();
                y1 = touchevent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchevent.getX();
                y2 = touchevent.getY();
                if (x2 < x1) {
                    Intent intent = new Intent(this, CameraActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                if (x1 < x2) {
                    Intent intent = new Intent(this, AboutActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }
                break;
        }
        return false;
    }
    //use to calculate the rest of the limits based off of one of them
    private void calclimits(int i) {
        //i determines which limit to calculate the rest off of
        //1 = daily, 2 = weekly, 3 = monthly, 4 = yearly
        //later, need to account for leap years, variable month counts, etc. 
        switch (i){
            case 1:
                limits[2] = limits[1]*7;
                limits[3] = limits[1]*30;
                limits[4] = limits[1]*365;
                break;
            case 2:
                limits[1] = limits[2] / 7;
                limits[3] = limits[2] * 4;
                limits[4] = limits[2] * 52;
                break;
            case 3:
                limits[1] = limits[3] / 30;
                limits[2] = limits[3] / 4;
                limits[4] = limits[3] * 12;
                break;
            case 4:
                limits[1] = limits[4] / 365;
                limits[2] = limits[4] / 52;
                limits[3] = limits[4] / 12;
                break;
            default:
                Log.d("Error", "calclimits called with undefined mode");
        }
    }

    public void updatechart(ArrayList<BarEntry> newdata){
        //first create the bar data set
        //raw values -> BarEntry ArrayList -> BarDataSet -> IBarDataSet ArrayList -> BarData -> CombinedData
        MyBarDataSet initbdata;
        LineDataSet lineset;
        //line data is created on update, with a simple generator
        ArrayList<Entry> limitdata = generatelimitdata();

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) { //if chart data already exists, reuse that
            initbdata = (MyBarDataSet) chart.getData().getDataSetByLabel("Expenses", false);
            initbdata.setLimit(limits[limitmode]);
            initbdata.setValues(newdata);
            lineset = (LineDataSet) chart.getData().getDataSetByLabel("Limit", false);
            lineset.setValues(limitdata);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else{ //create new data
            initbdata = new MyBarDataSet(newdata, "Expenses");
            lineset = new LineDataSet(limitdata, "Limit");

            chart.setDrawOrder(new CombinedChart.DrawOrder[]{CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.BAR});
            //custom bar data stuff: see the class for details
            initbdata.setLimit(limits[limitmode]);
            initbdata.setMilestones(new float[]{
                    .5f, 1.25f});
            initbdata.setColors(
                    Color.GREEN, Color.YELLOW, Color.RED);
            initbdata.setDrawValues(true);
            lineset.setCircleColors(Color.WHITE);
            lineset.setColor(Color.WHITE);
            lineset.setLineWidth(2);
            lineset.setDrawCircles(false);
            lineset.enableDashedLine(0.5f, 0.5f, 0f);
            ArrayList<IBarDataSet> barsets = new ArrayList<>();
            barsets.add(initbdata);
            ArrayList<ILineDataSet> linesets = new ArrayList<>();
            linesets.add(lineset);
            //now for the combined data
            CombinedData initcdata = new CombinedData();
            BarData bardata = new BarData(barsets);
            LineData linedata = new LineData(linesets);
            //linedata.setDrawValues(false);
            initcdata.setData(bardata);
            initcdata.setData(linedata);
            initcdata.setValueTextColor(Color.LTGRAY);
            chart.setData(initcdata);
            chart.notifyDataSetChanged();
        }
        if(chart.getBarData().getEntryCount() > 0) {
            chart.getXAxis().setAxisMaximum(Math.max(chart.getBarData().getXMax(), 4) + 0.5f);
            chart.getXAxis().setAxisMinimum(Math.min(chart.getBarData().getXMin(), 0) - 0.5f);
        }else{
            chart.getXAxis().setAxisMaximum(4 + 0.5f);
            chart.getXAxis().setAxisMinimum(0 - 0.5f);
        }
        chart.resetViewPortOffsets();
        chart.setVisibleXRangeMinimum(5);
        chart.resetZoom();
        chart.moveViewToX(-1);
        chart.animateY(500);
        chart.getDescription().setEnabled(false);
        chart.setBackgroundColor(Color.TRANSPARENT);

    }

    private ArrayList<Entry> generatelimitdata() {
        //simply generate far more data points than necessary at the limit value to make a straight line
        ArrayList<Entry> output = new ArrayList<>();
        for (int i = -3; i < 1000; i++){
            output.add(new Entry(i, limits[limitmode]));
        }
        return output;
    }

    //this gives recipts in a certain range
    //the most complicated function
    //also probably not very well written
    public ArrayList<BarEntry> Allrecipts(String startDate, String endDate){
        Double totalAddedView=0.0;
        String reciptsAll="";
        String fileLine="";
        double fileTotal;
        float perday = 0;
        float perweek = 0;
        float permonth = 0;
        float peryear = 0;
        int sincestart = 0;
        File[] filelist = getFilesDir().listFiles();
        ArrayList<BarEntry> datalist = new ArrayList<>();
        Date sdate, edate, filedate, currentdate;
        Calendar c = Calendar.getInstance();
        //create Date objects for the start and end of the range
        //creation differs to enable optional parameters
        if(endDate != null) {
            c.set(lasteyear, lastemonth, lasteday, 23, 59, 59);
            edate = c.getTime();
        }
        else{
            c.set(lastsyear, lastsmonth, lastsday, 23, 59, 59);
            edate = c.getTime();
        }
        c.set(lastsyear, lastsmonth, lastsday, 0, 0, 0);
        sdate = currentdate = c.getTime();

        for(int i=0;i<filelist.length;i++){
            //parse all the relevant dates into Date objects
            SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy-HH:mm:ss.'txt'");
            String filename = filelist[i].getName();
            try {
                filedate = df.parse(filename);
                Log.d("TEST 9", "Parse successful");
                if(i == 0){ //special instructions for the first file
                    if(spinnerrange.getSelectedItemPosition() == 0) {
                        //if the range is set to all files, the start of the chart should be the date of the first file
                        c.setTime(filedate);
                        sdate = currentdate = filedate;
                        lastsyear = c.get(Calendar.YEAR);
                        lastsmonth = c.get(Calendar.MONTH);
                        lastsday = c.get(Calendar.DAY_OF_MONTH);
                    }
                    //also some output formatting for the receipt list at the bottom
                    SimpleDateFormat dfout = new SimpleDateFormat("MMM dd, yyyy");
                    reciptsAll += "\n--" + dfout.format(c.getTime()) + "--\n\n";
                }
            }
            catch (Exception e){
                filedate = null;
                Log.d("TEST 9", "Parse failed");
            }

            Log.d("TEST6", "File " + i + ": " + filename);
            Log.d("TEST10", "Start Date: " + sdate.toString() + " |End Date: " + edate.toString() + " |File Date: " + filedate.toString());

            //startdate == null happens when set to get all files, otherwise check if the file is in the range
            if( startDate == null || (filedate.after(sdate) && filedate.before(edate))) {
                int length = (int) new File(getFilesDir(), filelist[i].getName()).length();
                byte[] bytes = new byte[length];
                try {
                    FileInputStream in = new FileInputStream(new File(getFilesDir(), filelist[i].getName()));
                    //this will only give the time for the re
                    int dashloc = filename.indexOf("-");
                    String fileTime =  filename.substring(dashloc + 1, filename.length() - 4);

                    in.read(bytes);
                    in.close();
                    String contents = new String(bytes);

                    //split the file into its various receipt totals
                    String filesplit[] = contents.split("Receipt \\d total: ");

                    //add up the receipt totals inside the file
                    //filetotal is saved here for use later
                    //this is because we don't know if the file is from a new day/month/etc.
                    //parsedline is outside the loop just to prevent multiple declarations, not that it matters much
                    fileTotal = 0;
                    float parsedline;
                    for(int j = 1; j < filesplit.length; j++) {
                        fileLine = filesplit[j].replaceAll("\\s|\\$", "");
                        parsedline = Float.parseFloat(fileLine);
                        if(spinnergrouping.getSelectedItemPosition() == 0) {
                            datalist.add(new BarEntry(datalist.size(), (float)parsedline));
                        }
                        fileTotal += parsedline;
                        //totaladdedview can be updated immediately since it's the total of all files seen
                        totalAddedView += parsedline;
                    }

                    //compare to the last checked date
                    //first, split the two dates into their components
                    c.setTime(currentdate);
                    int cyear = c.get(c.YEAR);
                    int cmonth = c.get(c.MONTH);
                    int cweek = c.get(c.WEEK_OF_YEAR);
                    int cday = c.get(c.DAY_OF_YEAR);
                    c.setTime(filedate);
                    int fyear = c.get(c.YEAR);
                    int fmonth = c.get(c.MONTH);
                    int fweek = c.get(c.WEEK_OF_YEAR);
                    int fday = c.get(c.DAY_OF_YEAR);
                    //the general strategy is to check if a year/month/etc. has changed, and then update that
                    //the total for that period of time is added to the output data as a new entry first
                    //then the total of the receipts in the file we just read is added to a new time period
                    //also, since month, week, and day reset with year, we add 1000 to them when the year changes
                    //this prevents the reset from making new year values look earlier than old year ones
                    //finally, the loops ensure that blank data is added to fill in any gaps
                    //that is also the purpose of the sincestart counter
                    if(cyear < fyear){
                        for(int j = 0; j < fyear - cyear; j++) {
                            if (spinnergrouping.getSelectedItemPosition() == 4) {
                                datalist.add(new BarEntry(sincestart, peryear));
                                sincestart++;
                            }
                            peryear = 0;
                        }
                        fmonth += 1000;
                        fweek += 1000;
                        fday += 1000;
                    }
                    if(cmonth < fmonth){
                        for(int j = 0; j < fmonth - cmonth; j++) {
                            if (spinnergrouping.getSelectedItemPosition() == 3) {
                                datalist.add(new BarEntry(sincestart, permonth));
                                sincestart++;
                            }
                            permonth = 0;
                        }
                    }
                    if(cweek < fweek){
                        for(int j = 0; j < fweek - cweek; j++) {
                            if (spinnergrouping.getSelectedItemPosition() == 2) {
                                datalist.add(new BarEntry(sincestart, perweek));
                                sincestart++;
                            }
                            perweek = 0;
                        }
                    }
                    //day is special, since it's also used to output the new day message in the receipt textview at the bottom
                    //since all of the other groups only change if the day changes, and since they're all in their own if statements,
                    // this independent if statement is guaranteed to be true if any of the other ones are too
                    if(cday < fday){
                        c.setTime(currentdate);
                        for(int j = 0; j < fday - cday; j++) {
                            //need to add to c to make the simpledateformat output the right date
                            c.add(Calendar.DAY_OF_YEAR, 1);
                            SimpleDateFormat dfout = new SimpleDateFormat("MMM dd, yyyy");
                            reciptsAll += "\n--" + dfout.format(c.getTime()) + "--\n\n";
                            if(spinnergrouping.getSelectedItemPosition() == 1){
                                datalist.add(new BarEntry(sincestart, perday));
                                sincestart++;
                            }
                            perday = 0;
                        }
                        currentdate = filedate;
                    }
                    //finally, since resetting the groups for new time periods has already been done, it's safe to just add the filetotal
                    perday += fileTotal;
                    perweek += fileTotal;
                    permonth += fileTotal;
                    peryear += fileTotal;
                    //also safe to add the receipts since the day boundary has already been accounted for
                    reciptsAll = reciptsAll +fileTime+"\n"+contents + "\n";
                }
                catch (Exception e){
                }
            }
        }
        //add the last of the data, since the for loop only adds stuff when a boundary is hit
        if(spinnergrouping.getSelectedItemPosition() == 4){
            datalist.add(new BarEntry(sincestart, peryear));
        }
        if(spinnergrouping.getSelectedItemPosition() == 3){
            datalist.add(new BarEntry(sincestart, permonth));
        }
        if(spinnergrouping.getSelectedItemPosition() == 2) {
            datalist.add(new BarEntry(sincestart, perweek));
        }
        if(spinnergrouping.getSelectedItemPosition() == 1){
            datalist.add(new BarEntry(sincestart, perday));
        }

        //update the views so the parsing can actually be seen
        TextView fileFound = findViewById(R.id.FilesFound);
        totalView= findViewById(R.id.TotalView);
        totalView.setText("Total Spent:$"+String.format("%.2f", totalAddedView));
        fileFound.setMovementMethod(new ScrollingMovementMethod());
        fileFound.setText(reciptsAll);

        //return the data for use with the chart
        return datalist;
    }

    //these two are just workarounds for default/optional parameters
    public ArrayList<BarEntry> Allrecipts(){
        return Allrecipts(null);
    }

    public ArrayList<BarEntry> Allrecipts(String selectedDate){
        return Allrecipts(selectedDate, null);
    }

    static final String[] Months = new String[] { "Jan", "Feb",
            "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
            "Oct", "Nov", "Dec" };

    private void openAbout(){
        Intent intent1 = new Intent(this, AboutActivity.class);
        startActivity(intent1);
    }

    private void openCamera(){
        Intent intent2 = new Intent(this,CameraActivity.class);
        startActivity(intent2);
    }

    //ondateset is defined here so it has access to MainActivity's variables
    //this is basically what is called every time you say ok from a date picker
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if(pickermode) {
            lastsyear = year;
            lastsmonth = month;
            lastsday = dayOfMonth;
            rngsbtn.setText(Months[month] + " " + dayOfMonth + ", " + year);
        }
        else{
            lasteyear = year;
            lastemonth = month;
            lasteday = dayOfMonth;
            rngebtn.setText(Months[month] + " " + dayOfMonth + ", " + year);
        }
    }

    //here is the rest of the custom date picker
    public static class DatePickerFragment extends DialogFragment{
        int yearout, monthout, dayout;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            //the datepickerdialog is passed MainActivity as the source of the onDateSet function for the above reasons
            return new DatePickerDialog(getActivity(), (MainActivity)getActivity(), yearout, monthout, dayout);
        }

        //needs to be called before using the datepickerfragment
        //this is what sets the starting date to something reasonable
        public void setstartdate(Bundle currentdate) {
            yearout = (int)currentdate.get("Year");
            monthout = (int)currentdate.get("Month");
            dayout = (int)currentdate.get("Day");
        }
    }

}
