package com.example.cristiano.nfc.nfc;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.util.Log;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

class NFC_read_write {

    //region Public Methods
    static void readFromIntent(Intent intent, Tag myTag) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }

            buildTagViews(msgs);


        }
    }
    //endregion

    //region Private Methods
    static void readPayload(){
        global_variables.setParticipants(new ArrayList<String>());

        String text2 = global_variables.getText();
        String text2temp;

        global_variables.setId(Integer.parseInt(text2.substring(text2.indexOf(":")+2, text2.indexOf(";"))));

        text2temp = text2.substring(text2.indexOf(";")+2);
        text2=text2temp;

        String numPartecTxt = text2.substring(text2.indexOf(":")+2, text2.indexOf(";"));
        text2temp = text2.substring(text2.indexOf(";")+2);
        text2=text2temp;

        global_variables.setParticipantsCount(Integer.parseInt(numPartecTxt));

        if(global_variables.getParticipantsCount()>1) {
            for (int i = 0; i < global_variables.getParticipantsCount(); i++) {
                if(i==0) {
                    global_variables.addParticipant(text2.substring(text2.indexOf(":")+2, text2.indexOf(",")));
                    text2temp = text2.substring(text2.indexOf(",")+2);
                    text2=text2temp;
                }else if (i== global_variables.getParticipantsCount()-1){
                    global_variables.addParticipant(text2.substring(0, text2.indexOf(";")));
                }else {
                    global_variables.addParticipant(text2.substring(0, text2.indexOf(",")));
                    text2temp = text2.substring(text2.indexOf(",")+2);
                    text2=text2temp;
                }
            }
        }else if(global_variables.getParticipantsCount()==1) {
            global_variables.addParticipant(text2.substring(text2.indexOf(":")+2, text2.indexOf(";")));
        }

        for (String string: global_variables.getParticipants()) {
            if(string.equalsIgnoreCase(global_variables.getName())){
                global_variables.setAlreadyRegistered(true);
            }
        }

        if(!global_variables.isAlreadyRegistered()){
            global_variables.addParticipant(global_variables.getName());
            global_variables.setParticipantsCount(global_variables.getParticipantsCount()+1);
        }

        global_variables.setAlreadyRegistered(false);
    }

    private static void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";

        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

        global_variables.setText(text);
    }

    static void write(Tag tag) throws IOException, FormatException {
        String newText = "ID: " + String.valueOf(global_variables.getId())  + ";" + "\n"
                + "NumPartecipanti: " + Integer.toString(global_variables.getParticipantsCount()) + ";" + "\n"
                + "Partecipanti: ";

        for (int i = 0; i< global_variables.getParticipantsCount(); i++) {
            if (i< global_variables.getParticipantsCount()-1){
                newText += global_variables.getParticipants().get(i) + ", ";
            }else if (i == global_variables.getParticipantsCount()-1){
                newText += global_variables.getParticipants().get(i) + ";";
            }
        }

        NdefRecord[] records = { NFC_read_write.createRecord(newText) };
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }

    static NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        // set status byte (see NDEF spec for actual bits)
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }
    //endregion
}
