package com.example.cristiano.nfc.nfc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class admin_mode extends AppCompatActivity {

    //region Private Attributes
    private static final String ERROR_DETECTED = "Nessuna tag NFC rilevata!";
    private static final String WRITE_SUCCESS = "Dati inseriti correttamente!";
    private static final String WRITE_ERROR = "Errore durante la scrittura. Assicurarsi di essere sufficientemente vicino alla tag";

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter writeTagFilters[];
    private boolean writeMode;
    private Tag myTag;
    private Context context;
    private TextView tvNFCContent;
    private EditText idEditText;
    private EditText numEditText;
    private EditText participantsEditText;
    private Button btnWrite;
    //endregion

    //region Overrides
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_mode);
        context = this;

        tvNFCContent = (TextView) findViewById(R.id.nfc_contents);
        idEditText = (EditText) findViewById(R.id.idEditText);
        numEditText = (EditText) findViewById(R.id.numEditText);
        participantsEditText = (EditText) findViewById(R.id.participantsEditText);
        btnWrite = (Button) findViewById(R.id.writeButton);

        btnWrite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                try {
                    if(myTag ==null) {
                        Toast.makeText(context, ERROR_DETECTED, Toast.LENGTH_LONG).show();
                    } else if (
                            (!idEditText.getText().toString().equalsIgnoreCase("") && Integer.parseInt(idEditText.getText().toString()) > 0 && Integer.parseInt(idEditText.getText().toString()) < 3) &&
                                    (!numEditText.getText().toString().equalsIgnoreCase("") && Integer.parseInt(numEditText.getText().toString()) >= 0)
                    ) {
                        write(myTag);
                        Toast.makeText(context, WRITE_SUCCESS, Toast.LENGTH_LONG ).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                } catch (FormatException e) {
                    Toast.makeText(context, WRITE_ERROR, Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                }
            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "Questo dispositivo non supporta la tecnologia NFC", Toast.LENGTH_LONG).show();
            finish();
        }

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);

        NFC_read_write.readFromIntent(intent, myTag);
        NFC_read_write.readPayload();

        tvNFCContent.setText(global_variables.getText());

        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
    }

    @Override
    public void onBackPressed(){
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
        finish();
    }
    //endregion

    //region Public Methods
    public void onClickPlayMode(View v){
        onBackPressed();
    }
    //endregion

    //region Private Methods
    private void write(Tag tag) throws IOException, FormatException {
        String idString = idEditText.getText().toString();
        String numString = numEditText.getText().toString();
        String participantsString = participantsEditText.getText().toString();

        String newText = "ID: " + idString  + ";" + "\n"
                + "NumPartecipanti: " + numString + ";" + "\n"
                + "Partecipanti: "+ participantsString + ";";

        NdefRecord[] records = { NFC_read_write.createRecord(newText) };
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }

    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }
    //endregion
}
