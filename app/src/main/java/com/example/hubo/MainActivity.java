package com.example.hubo;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.Manifest;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.AlertDialog;



public class MainActivity extends AppCompatActivity implements MQTTManager.MQTTListener {

    Button meet;

    Button delivery;

    boolean flag;

    VideoView video;

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    private int speechRetryCount = 0;
    String selectedPerson;

    String email;

    Button btnSubmit;

    BottomSheetDialog bottomSheetDialog;

    Button buttonYes;

    Button buttonNo;
    TextInputEditText name;
    TextInputEditText purpose;

    boolean actionflag = false;

    SpeechRecognizer speechRecognizer;

    boolean bottomSheetFlag = true;

    boolean dialogFlag;

    AlertDialog yesOrNoDialog;

    private ApiCaller apiCaller;

    boolean restartFlag;

    private boolean mTimerRunning = true;

    AlertDialog emailFormAlert;

    private boolean voiceFlag = true;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private final AtomicBoolean isDetecting = new AtomicBoolean(false);
    private boolean isFaceDetected = false;

    private final Handler activityDelayHandler = new Handler();
    private final Runnable activityDelayRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isFaceDetected) {
                meet.setVisibility(View.GONE);
                delivery.setVisibility(View.GONE);
                video.setVisibility(View.GONE);
                isDetecting.set(false);
                if(bottomSheetDialog != null)
                {
                    bottomSheetFlag = false;
                    bottomSheetDialog.dismiss();
                }
                if(yesOrNoDialog != null)
                    yesOrNoDialog.dismiss();
                if(emailFormAlert != null)
                    emailFormAlert.dismiss();
                stopSpeechRecognition();
                voiceFlag = true;
            }
        }
    };

    String[] persons = {"Dana AlSani", "Fatima Farhana Mohammed", "Harish Abdul Wahab", "Jovian D Cunha", "Ritin Nair", "Mohammed Shahzad", "Sukesh Ramdas", "Vivek Isaac"};
    String[] emails = {"danaalsani@devlacus.com", "fatimafarhanamohammed@devlacus.com", "harishabdulwahab@devlacus.com", "joviandcunha@devlacus.com", "ritinnair@devlacus.com", "mohammedshahzad@devlacus.com", "sukeshramdas@devlacus.com", "vivekisaac@devlacus.com"};

    String[] employee_id = {"e10000fd-942b-4c08-8d17-02732b96a2b8", "9f94e975-5727-45ab-b155-b2672d1605df", "f90ec33b-e85c-4dca-b434-2325c3385b6c", "bbac478c-ff0f-40db-b285-35c8ac8c38ae", "1ed73db4-0f2f-43cf-8b46-c3bf3fa4b46c", "e9e5d5de-593f-48c0-b6bf-a3396d435c1d", "6860896b-3d76-4216-a293-5238a39f753c", "261daf56-0287-43fe-9c13-93f295a3c371"};

    String emp_id;

    private MQTTManager mqttManager;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        meet = findViewById(R.id.meet);
        video = findViewById(R.id.video);
        delivery = findViewById(R.id.delivery);

//        meet.setVisibility(View.GONE);
//        delivery.setVisibility(View.GONE);

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.welcome;
        playVideo(videoPath);

        mHandler = new Handler();

        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                if(voiceFlag)
                {
                    startSpeechRecognition();
                }
                resetActivityDelay();
                if (flag) {
                    showPersonListBottomSheet();
                    flag = false;
                }
                if (dialogFlag) {
                    showDialog();
                    dialogFlag = false;
                }
                if (restartFlag) {
                    name.setText("");
                    purpose.setText("");
                    restartFlag = false;
                }
            }
        });

        meet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.meet;
                playVideo(videoPath);
                speechRetryCount = 0;
                flag = true;
                meet.setVisibility(View.GONE);
                delivery.setVisibility(View.GONE);
                isFaceDetected = false;
            }
        });

        delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                isFaceDetected = false;
                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.delivery;
                playVideo(videoPath);
            }
        });

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void resetActivityDelay() {
        activityDelayHandler.removeCallbacks(activityDelayRunnable);
        activityDelayHandler.postDelayed(activityDelayRunnable, 30000);
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);


       speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String result = matches.get(0).toLowerCase();
                    findVoiceAction(result);
                } else {
                    retrySpeechRecognition();
                }
            }

            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onError(int error) {
                retrySpeechRecognition();
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        speechRecognizer.startListening(intent);
    }

    private void stopSpeechRecognition() {
        if (speechRecognizer != null) {
            // Stop listening
            speechRecognizer.stopListening();
            // Cancel pending speech recognition requests
            speechRecognizer.cancel();
            // Destroy the speech recognizer
            speechRecognizer.destroy();
        }
    }


    private void retrySpeechRecognition() {
        if (speechRetryCount < 3) {
            speechRetryCount++;
            startSpeechRecognition();
        } else {
            speechRetryCount = 0;
            Toast.makeText(MainActivity.this, "Speech recognition failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @OptIn(markerClass = ExperimentalGetImage.class)
    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build();
        FaceDetector detector = FaceDetection.getClient(options);

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), imageProxy -> {
            try {
                if (imageProxy.getImage() == null || isDetecting.get()) {
                    imageProxy.close();
                    return;
                }

                InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
                detector.process(image)
                        .addOnSuccessListener(faces -> {
                            if (!faces.isEmpty() && isDetecting.compareAndSet(false, true)) {
                                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.welcome;
                                playVideo(videoPath);
                                video.setVisibility(View.VISIBLE);
                                meet.setVisibility(View.VISIBLE);
                                delivery.setVisibility(View.VISIBLE);
                            }
                        })
                        .addOnCompleteListener(task -> imageProxy.close());
            } catch (Exception e) {
                imageProxy.close();
            }
        });

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    private void startCamera() {
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    public void showDialog() {

        speechRetryCount = 0;

        // Create a custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog_box, null);

        // Set the message
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        messageTextView.setText("Are you sure you want to meet "+showSelectedPerson()+"?");


        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);


        yesOrNoDialog = builder.create();
        yesOrNoDialog.setCanceledOnTouchOutside(false);

        buttonYes = dialogView.findViewById(R.id.yesButton);
        buttonNo = dialogView.findViewById(R.id.noButton);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yesOrNoDialog.dismiss();
                showEmailDialog();
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yesOrNoDialog.dismiss();
                meet.performClick();
            }
        });

        yesOrNoDialog.show();
    }



    public void showEmailDialog() {

        speechRetryCount = 0;

        View emailView = getLayoutInflater().inflate(R.layout.email_form, null);

        name = emailView.findViewById(R.id.name);
        purpose = emailView.findViewById(R.id.purpose);


        // Set up the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(emailView);

        // Create the AlertDialog
        emailFormAlert = builder.create();

        emailFormAlert.setCanceledOnTouchOutside(false);


        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.form;
        playVideo(videoPath);

        // Show the AlertDialog

        emailFormAlert.show();


        btnSubmit = emailView.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the submit button click
                String guestName = Objects.requireNonNull(name.getText()).toString();
                String purposeOfVisit = Objects.requireNonNull(purpose.getText()).toString();

                hideKeyboard(purpose);


                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.checking;
                playVideo(videoPath);

                if(!guestName.isEmpty() && !purposeOfVisit.isEmpty()) {
                    voiceFlag = false;
                    stopSpeechRecognition();
                    sendEmail(emp_id,"d3B83VBZFJCaprPr", purposeOfVisit, guestName);
                    emailFormAlert.dismiss();
                }
                else {
                    videoPath = "android.resource://" + getPackageName() + "/" + R.raw.form;
                    playVideo(videoPath);
                }
            }
        });
    }


    private void showPersonListBottomSheet() {

        meet.setVisibility(View.GONE);
        delivery.setVisibility(View.GONE);

        bottomSheetFlag = true;

        // Create a bottom sheet dialog
        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.persons_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(bottomSheetFlag)
                  bottomSheetDialog.show();
            }
        });


        // Set up the ListView with the list of persons
        ListView listView = bottomSheetView.findViewById(R.id.listViewPersons);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, persons);
        listView.setAdapter(adapter);

        // Set item click listener for the ListView
        listView.setOnItemClickListener((adapterView, view, position, id) -> {

            String selectedPerson = persons[position];
            String email = emails[position];


            this.selectedPerson = persons[position];
            this.email = email;
            emp_id = employee_id[position];

            findActivity(selectedPerson);
            bottomSheetFlag = false;
            bottomSheetDialog.dismiss();
            dialogFlag = true;
        });

        // Show the bottom sheet
        bottomSheetDialog.show();
    }

    public String showSelectedPerson()
    {
        return selectedPerson;
    }


    public void findActivity(String name)
    {
        String result = name.toLowerCase().trim();
        if(result.contains("dana"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.dana;
            playVideo(videoPath);

        }
        else if(result.contains("fatima"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.fatima;
            playVideo(videoPath);
        }
        else if(result.contains("harish"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.harish;
            playVideo(videoPath);
        }
        else if(result.contains("jovian"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.jovian;
            playVideo(videoPath);
        }
        else if(result.contains("ritin"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ritin;
            playVideo(videoPath);
        }
        else if(result.contains("shahzad"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shezad;
            playVideo(videoPath);
        }
        else if(result.contains("sukesh"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sukesh;
            playVideo(videoPath);
        }
        else if(result.contains("vivek"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.vivek;
            playVideo(videoPath);
        }

    }
    public void findVoiceAction(String action)
    {
        String result = action.toLowerCase().trim();
         if(result.contains("meet")) {
             String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.meet;
             playVideo(videoPath);
             bottomSheetFlag = false;
             if(bottomSheetDialog != null)
                 bottomSheetDialog.dismiss();
             if(yesOrNoDialog != null)
                 yesOrNoDialog.dismiss();
             meet.performClick();
         }
         else if(actionflag && result.contains("yes"))
         {
             buttonYes.performClick();
         }
         else if(actionflag && result.contains("no"))
         {
             buttonNo.performClick();
         }
         else if(result.contains("submit"))
         {
             btnSubmit.performClick();
         }
        else if(result.contains("dana"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.dana;
            startVoiceAction(videoPath,"Dana AlSani","e10000fd-942b-4c08-8d17-02732b96a2b8");

        }
        else if(result.contains("fatima"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.fatima;
            startVoiceAction(videoPath,"Fathima Farhana Mohammed","9f94e975-5727-45ab-b155-b2672d1605df");
        }
        else if(result.contains("harish"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.harish;
            startVoiceAction(videoPath,"Harish Abdul Wahab","f90ec33b-e85c-4dca-b434-2325c3385b6c");
        }
        else if(result.contains("jovian"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.jovian;
            startVoiceAction(videoPath,"Jovian D Cunha","bbac478c-ff0f-40db-b285-35c8ac8c38ae");

        }
        else if(result.contains("rithin") || result.contains("nair"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ritin;
            startVoiceAction(videoPath,"Ritin Nair","1ed73db4-0f2f-43cf-8b46-c3bf3fa4b46c");
        }
        else if(result.contains("shahzad"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shezad;
            startVoiceAction(videoPath,"Mohammed Shahzad","e9e5d5de-593f-48c0-b6bf-a3396d435c1d");
        }
        else if(result.contains("sukesh"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sukesh;
            startVoiceAction(videoPath,"Sukesh Ramdas","6860896b-3d76-4216-a293-5238a39f753c");
        }
        else if(result.contains("vivek"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.vivek;
            startVoiceAction(videoPath,"Vivek Isaac","261daf56-0287-43fe-9c13-93f295a3c371");
        }

    }

    public void startVoiceAction(String videoPath,String name,String emp_id)
    {
        playVideo(videoPath);
        bottomSheetFlag = false;
        actionflag = true;
        bottomSheetDialog.dismiss();
        selectedPerson = name;
        dialogFlag = true;
        this.emp_id = emp_id;
    }

    private void sendEmail(String employeeId,String guestId, String purposeOfVisit, String guestName) {

        apiCaller = new ApiCaller();
        apiCaller.executeApiCall(employeeId, guestId, purposeOfVisit, guestName);

        mqttManager = new MQTTManager(this);
        mqttManager.setMQTTListener(this);
        mqttManager.connectAndSubscribe("/user_data");
    }

    public void playVideo(String path)
    {
        stopSpeechRecognition();
        activityDelayHandler.removeCallbacks(activityDelayRunnable);
        video.setVideoURI(Uri.parse(path));
        video.start();
    }

    private void hideKeyboard(TextInputEditText purpose) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(purpose.getWindowToken(), 0);
        }
    }

    @Override
    public void onMessageReceived(String topic, String message) {

        restartFlag = true;

        if (message != null && message.toLowerCase().contains("accepted")) {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.available;
            playVideo(videoPath);
        } else {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.notavailable;
            playVideo(videoPath);
        }
        mqttManager.disconnect();
    }

}