package com.junghoryu.popularmoviesudacity;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

enum DownloadStatus { IDLE, PROCESSING, NOT_INITIALISED, FAILED_OR_EMPTY, OK}
/**
 * Created by jryu075 on 2016-12-17.
 */

class GetRawData extends AsyncTask<String, Void, String>{
    private static final String TAG = "GetRawData";

    private DownloadStatus mDownLoadStatus;
    private final OnDownloadComplete mCallback;

    interface OnDownloadComplete {
        void onDownloadComplete(String data, DownloadStatus status);
    }

    public GetRawData(OnDownloadComplete callback) {
        this.mDownLoadStatus = DownloadStatus.IDLE;
        mCallback = callback;
    }

    void runInSameThread(String s) {
        Log.d(TAG, "runInSameThread starts");
        if(mCallback !=null) {
            mCallback.onDownloadComplete(doInBackground(s), mDownLoadStatus);
        }

        Log.d(TAG, "runInSameThread ends");
    }
    @Override
    protected void onPostExecute(String s) {
//        Log.d(TAG, "onPostExecute: parameter = " + s);
        
        if(mCallback != null) {
            mCallback.onDownloadComplete(s, mDownLoadStatus);
            
        }
        Log.d(TAG, "onPostExecute:  ends");
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        if(strings == null) {
            mDownLoadStatus = DownloadStatus.NOT_INITIALISED;
            return null;
        }

        try {
            mDownLoadStatus = DownloadStatus.PROCESSING;
            URL url = new URL(strings[0]);

            connection  = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response = connection.getResponseCode();
            Log.d(TAG, "doInBackground: The response code was " + response);

            StringBuilder result = new StringBuilder();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while(null != (line = reader.readLine())) {
                result.append(line).append("\n");
            }

            mDownLoadStatus = DownloadStatus.OK;
            return result.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Invalid URL" + e.getMessage() );;
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IO Exception reading data" + e.getMessage() );
        } catch(SecurityException e) {
            Log.e(TAG, "doInBackground:  Security Exception. Needs permission?" + e.getMessage() );
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: Error closing stream" + e.getMessage() );
                }
            }
        }

        mDownLoadStatus = DownloadStatus.FAILED_OR_EMPTY;
        return null;
    }
    

}