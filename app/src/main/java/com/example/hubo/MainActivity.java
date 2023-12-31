package com.example.hubo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import android.app.AlertDialog;


public class MainActivity extends AppCompatActivity {

    Button meet;

    Button delivery;

    boolean flag = true;

    boolean dialogFlag = true;

    VideoView video;

    public static final int RECORD_AUDIO_PERMISSION_CODE = 1;


    String selectedPerson;

    String email;

    Intent intent;

    boolean flagControl = true;

    Button btnSubmit;

    BottomSheetDialog bottomSheetDialog;

    Button buttonYes;

    Button buttonNo;
    TextInputEditText name;
    TextInputEditText purpose;

    View emailView;

    boolean bottomSheetFlag = true;

    String[] persons = {"Dana AlSani", "Fathima Farhana Mohammed", "Harish Abdul Wahab", "Jovian D Cunha", "Ritin Nair", "Mohammed Shahzad", "Sukesh Ramdas", "Vivek Isaac"};
    String[] emails = {"danaalsani@devlacus.com", "fatimafarhanamohammed@devlacus.com", "harishabdulwahab@devlacus.com", "joviandcunha@devlacus.com", "ritinnair@devlacus.com", "mohammedshahzad@devlacus.com", "sukeshramdas@devlacus.com", "vivekisaac@devlacus.com"};


    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> d = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (d != null && !d.isEmpty()) {
                        String res = d.get(0);
                        findVoiceAction(res);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        meet = findViewById(R.id.meet);
        video = findViewById(R.id.video);
        delivery = findViewById(R.id.delivery);

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.welcome;
        playVideo(videoPath);

        emailView = getLayoutInflater().inflate(R.layout.email_form, null);

        // Find views in the dialog layout
        name = emailView.findViewById(R.id.name);
        purpose = emailView.findViewById(R.id.purpose);


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_CODE);
        } else {
            initializeSpeechRecognizer();
        }



        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(flagControl)
                   startRecognition(intent);
                flagControl = true;
            }
        });

        meet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.meet;
                playVideo(videoPath);
                flag = true;

                meet.setVisibility(View.GONE);
                delivery.setVisibility(View.GONE);
                video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                        if (flag) {
                            showPersonListBottomSheet();
                            startRecognition(intent);
                            flag = false;
                        }
                    }
                });
            }
        });

        delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.delivery;
                playVideo(videoPath);
            }
        });
    }

    private void initializeSpeechRecognizer()
    {
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
    }
    private void startRecognition(Intent intent)
    {
        activityResultLauncher.launch(intent);
    }

    public void showDialog() {

        flagControl = false;

        // Create a custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog_box, null);

        // Set the message
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        messageTextView.setText("Are you sure you want to meet "+showSelectedPerson()+"?");


        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);



        AlertDialog yesOrNoDialog = builder.create();
        yesOrNoDialog.setCanceledOnTouchOutside(false);
        if(dialogFlag)
        {
            yesOrNoDialog.show();
            dialogFlag = false;
        }


        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Yes or No");
        startRecognition(intent);

        buttonYes = dialogView.findViewById(R.id.yesButton);
        buttonNo = dialogView.findViewById(R.id.noButton);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yesOrNoDialog.dismiss();
                showEmailDialog();
                intent.removeExtra(RecognizerIntent.EXTRA_PROMPT);
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogFlag = true;
                yesOrNoDialog.dismiss();
                meet.performClick();
                intent.removeExtra(RecognizerIntent.EXTRA_PROMPT);
            }
        });
    }



    public void showEmailDialog() {

        // Inflate the dialog view

        // Set up the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(emailView);

        // Create the AlertDialog
        AlertDialog emailFormAlert = builder.create();

        emailFormAlert.setCanceledOnTouchOutside(false);


        flagControl = false;
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.form;
        playVideo(videoPath);

        // Show the AlertDialog

        emailFormAlert.show();


        btnSubmit = emailView.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the submit button click
                String enteredName = Objects.requireNonNull(name.getText()).toString();
                String enteredPurpose = Objects.requireNonNull(purpose.getText()).toString();

                hideKeyboard(purpose);

                flagControl = false;

                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.checking;
                playVideo(videoPath);

                if(!enteredName.isEmpty() && !enteredPurpose.isEmpty()) {
                    sendEmail(email, enteredPurpose, enteredName);
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

            findActivity(selectedPerson);
            bottomSheetFlag = false;
            bottomSheetDialog.dismiss();
            delayedShowDialog();
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
            delayedShowDialog();
        }
        else if(result.contains("fatima"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.fatima;
            playVideo(videoPath);
            delayedShowDialog();
        }
        else if(result.contains("harish"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.harish;
            playVideo(videoPath);
            delayedShowDialog();
        }
        else if(result.contains("jovian"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.jovian;
            playVideo(videoPath);
            delayedShowDialog();
        }
        else if(result.contains("ritin"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ritin;
            playVideo(videoPath);
            delayedShowDialog();
        }
        else if(result.contains("shahzad"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shezad;
            playVideo(videoPath);
            delayedShowDialog();
        }
        else if(result.contains("sukesh"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sukesh;
            playVideo(videoPath);
            delayedShowDialog();
        }
        else if(result.contains("vivek"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.vivek;
            playVideo(videoPath);
            delayedShowDialog();
        }

    }

    public void findVoiceAction(String action)
    {
        String result = action.toLowerCase().trim();
         if(result.contains("meet")) {
             String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.meet;
             playVideo(videoPath);
             meet.performClick();
         }
         else if(result.contains("yes"))
         {
             buttonYes.performClick();
         }
         else if(result.contains("no"))
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
            playVideo(videoPath);
            bottomSheetFlag = false;
            bottomSheetDialog.dismiss();
            selectedPerson = "Dana AlSani";
            delayedShowDialog();
            setEmail(action);
        }
        else if(result.contains("fatima"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.fatima;
            playVideo(videoPath);
            bottomSheetFlag = false;
            bottomSheetDialog.dismiss();
            selectedPerson = "Fathima Farhana Mohammed";
            delayedShowDialog();
            setEmail(action);
        }
        else if(result.contains("harish"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.harish;
            playVideo(videoPath);
            bottomSheetFlag = false;
            bottomSheetDialog.dismiss();
            selectedPerson = "Harish Abdul Wahab";
            delayedShowDialog();
            setEmail(action);
        }
        else if(result.contains("jovian"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.jovian;
            playVideo(videoPath);
            bottomSheetFlag = false;
            bottomSheetDialog.dismiss();
            selectedPerson = "Jovian D Cunha";
            delayedShowDialog();
            setEmail(action);
        }
        else if(result.contains("rithin") || result.contains("nair"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.ritin;
            playVideo(videoPath);
            bottomSheetFlag = false;
            bottomSheetDialog.dismiss();
            selectedPerson = "Ritin Nair";
            delayedShowDialog();
            setEmail(action);
        }
        else if(result.contains("shahzad"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.shezad;
            playVideo(videoPath);
            bottomSheetFlag = false;
            bottomSheetDialog.dismiss();
            selectedPerson = "Mohammed Shahzad";
            delayedShowDialog();
            setEmail(action);
        }
        else if(result.contains("sukesh"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.sukesh;
            playVideo(videoPath);
            bottomSheetFlag = false;
            bottomSheetDialog.dismiss();
            selectedPerson = "Sukesh Ramdas";
            delayedShowDialog();
            setEmail(action);
        }
        else if(result.contains("vivek"))
        {
            String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.vivek;
            playVideo(videoPath);
            bottomSheetFlag = false;
            bottomSheetDialog.dismiss();
            selectedPerson = "Vivek Isaac";
            delayedShowDialog();
            setEmail(action);
        }
         else {
             startRecognition(intent);
         }

    }

    private void delayedShowDialog() {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showDialog();
            }
        }, 3500);
    }

    private void sendEmail(String toEmail,String toMessage, String toName) {
        String email = toEmail;
        String subject = "Requesting a Meet";
        String message = toMessage;

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));

        emailIntent.putExtra(Intent.EXTRA_EMAIL, toEmail);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        }
        intent.removeExtra(RecognizerIntent.EXTRA_PROMPT);
        initializeSpeechRecognizer();
    }

    public void playVideo(String path)
    {
        video.setVideoURI(Uri.parse(path));
        video.start();
    }

    private void setEmail(String name) {
        for (int i = 0; i < persons.length; i++) {
            if (persons[i].toLowerCase().contains(name)) {
                email = emails[i];
                break;
            }
        }
    }

    private void hideKeyboard(TextInputEditText purpose) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(purpose.getWindowToken(), 0);
        }
    }

}