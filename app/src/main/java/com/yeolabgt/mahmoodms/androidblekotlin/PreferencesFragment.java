package com.yeolabgt.mahmoodms.androidblekotlin;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

/**
 * Created by mmahmood31 on 11/2/2017.
 *
 */

public class PreferencesFragment extends PreferenceFragment {
    //Key Association:
    public static String SETTINGS_CH_SELECT = "switch_ch";
    public static String SETTINGS_SHOW_PSDA = "psda";
    public static String SETTINGS_PSD_RANGE = "psda_long";
    public static String SETTINGS_SHOW_BUTTONS = "showUIElements";
    public static String SETTINGS_SAVE_TIMESTAMPS = "timestamps";
    public static String SETTINGS_SAVE_CLASS = "save_class";
    public static String SETTINGS_BIT_PRECISION = "bit_precision";
    public static String SETTINGS_FILTER_DATA = "filterData";

    public static boolean channelSelect(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_CH_SELECT, true);
    }

    public static boolean showPSDA(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_SHOW_PSDA, true);
    }

    public static boolean psdaWideRange(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_PSD_RANGE, false);
    }

    public static boolean showUIElements(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_SHOW_BUTTONS, false);
    }

    public static boolean saveTimestamps(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_SAVE_TIMESTAMPS, false);
    }

    public static boolean saveClass(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_SAVE_CLASS, true);
    }

    public static boolean setBitPrecision(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_BIT_PRECISION, true);
    }

    public static boolean setFilterData(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTINGS_FILTER_DATA, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }
}
