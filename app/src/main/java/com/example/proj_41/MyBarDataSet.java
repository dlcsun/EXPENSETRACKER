package com.example.proj_41;

import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.List;

class MyBarDataSet extends BarDataSet {
    float limit;
    float milestones[]; // in percent, from lowest to greatest
    //must also set colors to an array of colors 1 longer than milestones

    public MyBarDataSet(List<BarEntry> yVals, String label){
        super(yVals, label);
    }

    public void setLimit(float limit) {
        this.limit = limit;
    }

    public void setMilestones(float[] milestones) {
        this.milestones = milestones;
    }

    @Override
    public int getColor(int index){
        for(int i = 0; i < milestones.length; i++){
            if(getEntryForIndex(index).getY() / limit < milestones[i]){
                return mColors.get(i);
            }
        }
        return mColors.get(milestones.length);
    }
}
