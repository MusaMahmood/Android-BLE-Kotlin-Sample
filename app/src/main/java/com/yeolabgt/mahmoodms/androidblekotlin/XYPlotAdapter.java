package com.yeolabgt.mahmoodms.androidblekotlin;

import android.view.View;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.StepMode;

import java.text.DecimalFormat;

/**
 * Created by mahmoodms on 5/15/2017.
 *
 */

class XYPlotAdapter {

//    private final static String TAG = XYPlotAdapter.class.getSimpleName();
    XYPlot xyPlot = null;

    /**
     * This function implies that plotImplicitXVals is false. Therefore domain parameters need to be specified:
     * @param findViewByID R.id in /res/
     * @param domainLabel x-axis label
     * @param rangeLabel y-axis label
     * @param domainIncrement x-axis increment
     */
    XYPlotAdapter(View findViewByID, String domainLabel, String rangeLabel,  double domainIncrement) {
        this.xyPlot = (XYPlot) findViewByID;
        this.xyPlot.setDomainBoundaries(0, 1, BoundaryMode.AUTO); //Default
        this.xyPlot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        this.xyPlot.setDomainStepValue(domainIncrement);
        //Default Config:
        this.xyPlot.setRangeStepMode(StepMode.INCREMENT_BY_VAL);
        this.xyPlot.setDomainLabel(domainLabel);
        this.xyPlot.setRangeLabel(rangeLabel);
        this.xyPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("#.###"));
        this.xyPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new DecimalFormat("#"));
        this.xyPlot.setRangeBoundaries(-0.004, 0.004, BoundaryMode.AUTO);
        this.xyPlot.setRangeStep(StepMode.SUBDIVIDE, 5.0);
    }

    XYPlotAdapter(View findViewByID, boolean plotImplicitXVals, int historySize) {
        this.xyPlot = (XYPlot) findViewByID;
        int historySeconds = historySize/250;
        if(plotImplicitXVals) {
            this.xyPlot.setDomainBoundaries(0, historySize, BoundaryMode.FIXED);
            this.xyPlot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
            this.xyPlot.setDomainStepValue(historySize/5);
        } else {
            this.xyPlot.setDomainBoundaries(0, historySeconds, BoundaryMode.AUTO);
            this.xyPlot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
            this.xyPlot.setDomainStepValue(historySeconds / 4);
        }
        //Default Config:
        this.xyPlot.setRangeStepMode(StepMode.INCREMENT_BY_VAL);
        this.xyPlot.setDomainLabel("Time (seconds)");
        this.xyPlot.setRangeLabel("Voltage (mV)");
        this.xyPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("#.###"));
        this.xyPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new DecimalFormat("#"));
        this.xyPlot.setRangeBoundaries(-0.004, 0.004, BoundaryMode.AUTO);
        this.xyPlot.setRangeStep(StepMode.SUBDIVIDE, 5.0);
    }

    void setXyPlotDomainIncrement(double domainIncrement) {
        this.xyPlot.setDomainStepValue(domainIncrement);
    }
}