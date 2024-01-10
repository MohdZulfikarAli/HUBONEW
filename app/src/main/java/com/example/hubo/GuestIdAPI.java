package com.example.hubo;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class GuestIdAPI{

    private GuestIdInterface apiCallback;
    public GuestIdAPI(GuestIdInterface apiCallback)
    {
        this.apiCallback = apiCallback;
    }
    private static final String TAG = "ApiCallTask";
    private String apiUrl = "https://hubo2.domainenroll.com/api/v1/save-unknown";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface GuestIdInterface {
        void onApiResult(String result);
    }


    public void retriveGuestId(String employeeId,String guestImage) {
        CompletableFuture.runAsync(() -> {
            try {
                String result = performApiCall(employeeId, guestImage);
                notifyCallback(result);
            } catch (Exception e) {
                notifyCallback("error");
            }
        });
    }

    private String performApiCall(String employeeId, String guestImage) throws IOException {
        // Create URL
        URL url = new URL(apiUrl);

        // Open a connection using HttpURLConnection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the request method to POST
        connection.setRequestMethod("POST");

        // Enable input/output streams
        connection.setDoInput(true);
        connection.setDoOutput(true);

        // Set the content type to JSON
        connection.setRequestProperty("Content-Type", "application/json");

        // Create JSON data to send using the values from params
        String jsonData = "{\n" +
                "    \"employee_id\": \"" + employeeId + "\",\n" +
                "    \"guest_image\": \"" + guestImage + "\"\n" +
                "}";

        // Get the output stream and write the JSON data to the server
        try (DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream())) {
            dataOutputStream.writeBytes(jsonData);
        }

        // Get the response code
        int responseCode = connection.getResponseCode();
        Log.d(TAG, "Response Code: " + responseCode);

        // Read the response from the server
        InputStream inputStream;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            response.append(line);
        }

        bufferedReader.close();
        inputStream.close();

        // Close the connection
        connection.disconnect();

        // Return the response
        return response.toString();
    }

    private void notifyCallback(String result) {
        mainHandler.post(() -> {
            apiCallback.onApiResult(result);
        });
    }
}

