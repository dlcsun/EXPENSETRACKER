package com.example.proj_41;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

public class MyValueFormatter extends ValueFormatter
{

    private final DecimalFormat mFormat;
    private String prefix;

    public MyValueFormatter(String prefix) {
        mFormat = new DecimalFormat("###,###,###,##0.00");
        this.prefix = prefix;
    }

    @Override
    public String getFormattedValue(float value) {
        return prefix + mFormat.format(value);
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        if (axis instanceof XAxis) {
            return mFormat.format(value);
        } else if (value > 0) {
            return prefix + mFormat.format(value);
        } else {
            return mFormat.format(value);
        }
    }
}
