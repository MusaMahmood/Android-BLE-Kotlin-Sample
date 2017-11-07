package com.yeolabgt.mahmoodms.androidblekotlin;

import android.os.Environment;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by mmahmood31 on 10/19/2017.
 *
 */

class SaveDataFile {

    private final static String TAG = SaveDataFile.class.getSimpleName();
    //Math Stuff
    private int mLinesWritten; //for timestamp
    private double mIncrement;
    private short fpPrecision = 64; //float vs double (default)
    private int mResolutionBits;
    private boolean includeClass = true; //Saves by default, no need to change.
    private boolean saveTimestamps = false;
    private boolean initialized = false;
    private CSVWriter csvWriter;
    File file;

    SaveDataFile(String directory, String fileName, int byteResolution, double increment) throws IOException {
        File root = Environment.getExternalStorageDirectory();
        if(root.canWrite()) {
            File dir = new File(root.getAbsolutePath()+directory);
            boolean resultMkdir = dir.mkdirs();
            if (!resultMkdir) {
                Log.e(TAG, "MKDIRS FAILED");
            }
            this.file = new File(dir, fileName+".csv");
            if (this.file.exists() && !this.file.isDirectory()) {
                Log.d(TAG, "File " + this.file.toString() + " already exists - appending data");
                FileWriter fileWriter = new FileWriter(this.file, true);
                this.csvWriter = new CSVWriter(fileWriter);
            } else {
                this.csvWriter = new CSVWriter(new FileWriter(this.file));
            }
            this.mResolutionBits = byteResolution;
            this.mIncrement = increment;
            this.initialized = true;
        }
    }

    void setFpPrecision(short fpPrecision) {
        this.fpPrecision = fpPrecision;
    }

    void setSaveTimestamps(boolean saveTimestamps) {
        this.saveTimestamps = saveTimestamps;
    }

    void setIncludeClass(boolean includeClass) {
        this.includeClass = includeClass;
    }

    int getResolutionBits() {
        return mResolutionBits;
    }

    void writeToDisk(byte[]... byteArrays) {
        if(this.fpPrecision==64) {
            writeToDiskDouble(byteArrays);
        } else if (this.fpPrecision==32) {
            writeToDiskFloat(byteArrays);
        }
    }

    private void writeToDiskFloat(byte[]... byteArrays) {
        int len = byteArrays.length; // Number of channels
        float[][] floats;
        if(this.mResolutionBits == 16) floats = new float[len][byteArrays[0].length/2];
        else if (this.mResolutionBits == 24) floats = new float[len][byteArrays[0].length/3];
        else floats = null;
        for (int ch = 0; ch < len; ch++) { // each channel
            if(this.mResolutionBits == 16) {
                for (int dp = 0; dp < byteArrays[ch].length/2; dp++) { // each datapoint
                    floats[ch][dp] = DataChannel.Companion.bytesToFloat32(byteArrays[ch][2*dp],
                            byteArrays[ch][2*dp+1]);
                }
            } else if (this.mResolutionBits == 24) {
                for (int dp = 0; dp < byteArrays[ch].length/3; dp++) {
                    floats[ch][dp] = DataChannel.Companion.bytesToFloat32(byteArrays[ch][3*dp],
                            byteArrays[ch][3*dp+1], byteArrays[ch][3*dp+2]);
                }
            }
        }
        try {
            exportFile(floats);
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }
    }

    private void writeToDiskDouble(byte[]... byteArrays) {
        int len = byteArrays.length; // Number of channels
        double[][] doubles;
        if(this.mResolutionBits == 16) doubles = new double[len][byteArrays[0].length/2];
        else if (this.mResolutionBits == 24) doubles = new double[len][byteArrays[0].length/3];
        else doubles = null;
        for (int ch = 0; ch < len; ch++) { // each channel
            if(this.mResolutionBits == 16) {
                for (int dp = 0; dp < byteArrays[ch].length/2; dp++) { // each datapoint
                    doubles[ch][dp] = DataChannel.Companion.bytesToDouble(byteArrays[ch][2*dp],
                            byteArrays[ch][2*dp+1]);
                }
            } else if (this.mResolutionBits == 24) {
                for (int dp = 0; dp < byteArrays[ch].length/3; dp++) {
                    doubles[ch][dp] = DataChannel.Companion.bytesToDouble(byteArrays[ch][3*dp],
                            byteArrays[ch][3*dp+1], byteArrays[ch][3*dp+2]);
                }
            }
        }
        try {
            exportFile(doubles);
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }
    }

    private void exportFile(float[]... floats) throws IOException {
        int numDp = floats[0].length;
        int numChannels = floats.length;
        int columns = numChannels+((this.includeClass?1:0)+(this.saveTimestamps?1:0));
        String[][] writeCSVValue;
        writeCSVValue = new String[numDp][columns];
        for (int dp = 0; dp < numDp; dp++) {
            if(this.saveTimestamps) {
                if(fpPrecision==64) writeCSVValue[dp][0] = (double) mLinesWritten*mIncrement + "";
                else writeCSVValue[dp][0] = (float) mLinesWritten*mIncrement + "";
            }
            for (int ch = 0; ch < numChannels; ch++) {
                if(!this.saveTimestamps) writeCSVValue[dp][ch] = floats[ch][dp]+"";
                else writeCSVValue[dp][ch+1] = floats[ch][dp]+"";
            }
            if(this.includeClass) {
                writeCSVValue[dp][columns-1] = DeviceControlActivity.Companion.getMSSVEPClass() + ""; //always last column
            }
            csvWriter.writeNext(writeCSVValue[dp], false);
            this.mLinesWritten++;
        }
    }

    private void exportFile(double[]... doubles) throws IOException {
        int numDp = doubles[0].length;
        int numChannels = doubles.length;
        int columns = numChannels+((this.includeClass?1:0)+(this.saveTimestamps?1:0));
        String[][] writeCSVValue;
        writeCSVValue = new String[numDp][columns];
        for (int dp = 0; dp < numDp; dp++) {
            if(this.saveTimestamps) {
                if(fpPrecision==64) writeCSVValue[dp][0] = (double) mLinesWritten*mIncrement + "";
                else writeCSVValue[dp][0] = (float) mLinesWritten*mIncrement + "";
            }
            for (int ch = 0; ch < numChannels; ch++) {
                if(!this.saveTimestamps) writeCSVValue[dp][ch] = doubles[ch][dp]+"";
                else writeCSVValue[dp][ch+1] = doubles[ch][dp]+"";
            }
            if(this.includeClass) {
                writeCSVValue[dp][columns-1] = DeviceControlActivity.Companion.getMSSVEPClass() + ""; //always last column
            }
            csvWriter.writeNext(writeCSVValue[dp], false);
            this.mLinesWritten++;
        }
    }

    void terminateDataFileWriter() throws IOException {
        if(this.initialized) {
            this.csvWriter.flush();
            this.csvWriter.close();
            this.initialized = false;
            this.mLinesWritten = 0;
        }
    }
}
