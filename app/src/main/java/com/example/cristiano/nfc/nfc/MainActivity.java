package com.example.cristiano.nfc.nfc;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //region Private Attributes
    private static final String ERROR_DETECTED = "Nessuna tag NFC rilevata!";
    private static final String WRITE_SUCCESS = "Dati inseriti correttamente!";
    private static final String WRITE_ERROR = "Errore durante la scrittura. Assicurarsi di essere sufficientemente vicino alla tag";
    private Context context;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private boolean writeMode;

    private Tag myTag;
    private int tagIdToSearchFor = 1;

    private LinearLayout layoutMediaAndHint;
    private LinearLayout layoutSetName;
    private TextView textViewParticipants;
    private TextView textViewTitle2;
    private WebView webViewGif;
    private EditText editTextNameInput;
    private ImageView imageViewMediaAndHint;

    private int images[] = {R.drawable.img1, R.drawable.img2};
    //endregion

    //region Overrides
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        textViewParticipants = (TextView) findViewById(R.id.partecipantsTxt);
        textViewTitle2 = (TextView) findViewById(R.id.titleTxtView2);
        webViewGif = (WebView) findViewById(R.id.gif);
        webViewGif.setVerticalScrollBarEnabled(false);
        webViewGif.setHorizontalScrollBarEnabled(false);
        editTextNameInput = (EditText) findViewById(R.id.nameEditText);
        editTextNameInput.setHint("Scegliere un nome");
        layoutMediaAndHint = (LinearLayout) findViewById(R.id.mediaLayout);
        layoutSetName = (LinearLayout) findViewById(R.id.layoutSceltaNome);
        imageViewMediaAndHint = (ImageView) findViewById(R.id.imgViewMedia);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "Questo dispositivo non supporta la tecnologia NFC", Toast.LENGTH_LONG).show();
            finish();
        }

        String x = "<!DOCTYPE html><html style=\"background-color: #262626\"><body><img src=\"https://i.pinimg.com/originals/cb/05/42/cb05420fec7a12bb752da11df0fb553f.gif\" alt=\" \" width=\"120%\"  height=\"120%\" style=\"margin: 0; overflow: hidden; \"></body></html>";

        webViewGif.loadData(x, "text/html", "utf-8");


        String playerNameFromSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).getString("PLAYER_NAME", "nomeNonTrovato");

        if (playerNameFromSharedPreferences != null) {
            if(playerNameFromSharedPreferences.equalsIgnoreCase("nomeNonTrovato")){
                makeVisible(layoutSetName);
            }else{
                global_variables.setName(playerNameFromSharedPreferences);
            }
        }

        webViewGif.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });


        NFC_read_write.readFromIntent(getIntent(), myTag);

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);

        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }

        NFC_read_write.readFromIntent(intent, myTag);

        NFC_read_write.readPayload();

        try {
            NFC_read_write.write(myTag);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }

        if(tagIdToSearchFor >= Integer.parseInt(String.valueOf(global_variables.getId()))) {
            imageViewMediaAndHint.setImageResource(images[global_variables.getId()-1]);
            makeVisible(layoutMediaAndHint);

            tagIdToSearchFor++;

            StringBuilder participantsText = new StringBuilder();

            for(int i = 0; i<global_variables.getParticipantsCount(); i++){
                if(i==global_variables.getParticipantsCount()-1) {
                    participantsText.append(global_variables.getParticipants().get(i)).append(".");
                }else{
                    participantsText.append(global_variables.getParticipants().get(i)).append(", ");
                }
            }

            textViewParticipants.setText(participantsText.toString());
        }else{
            Toast.makeText(this, "Tag saliubcaiubco! Cercane un'altra!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        //write mode off
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onResume(){
        super.onResume();

        //write mode on
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }
    //endregion

    //region Public Methods
    public void onClickCloseBtnMedia(View v){
        makeInvisible(layoutMediaAndHint);
    }

    public void onClickSetNameBtn(View v){
        String name = editTextNameInput.getText().toString();

        if(!name.equalsIgnoreCase("")){
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("PLAYER_NAME", name).apply();
            makeInvisible(layoutSetName);
            Toast.makeText(this, "Nome inserito!", Toast.LENGTH_SHORT).show();
            global_variables.setName(name);
        }else{
            Toast.makeText(this, "Inserire un nome!", Toast.LENGTH_LONG).show();
        }

    }



    public void onClickAdminModeBtn(View v){
        Intent myIntent = new Intent(this, admin_mode.class);
        startActivity(myIntent);
        finish();
    }

    public void onClickNameBtn(View v){
        editTextNameInput.setText(global_variables.getName());
        makeVisible(layoutSetName);
    }
    //endregion

    //region Private Methods
    private void makeVisible(View input){
        input.setVisibility(View.VISIBLE);
    }

    private void makeInvisible(View input){
        input.setVisibility(View.INVISIBLE);
    }
    //endregion
}


