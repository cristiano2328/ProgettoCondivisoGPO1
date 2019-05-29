package com.example.cristiano.nfc.nfc;

import java.util.ArrayList;

class global_variables {
    private static String text = "";
    private static ArrayList<String> participants = new ArrayList<String>();
    private static boolean alreadyRegistered = false;
    private static String name = "";
    private static int participantsCount = 0;
    private static int Id = 0;

    static String getText() {
        return text;
    }

    static void setText(String text) {
        global_variables.text = text;
    }

    static ArrayList<String> getParticipants() {
        return participants;
    }

    static void addParticipant(String input) {
        global_variables.participants.add(input);
    }

    static boolean isAlreadyRegistered() {
        return alreadyRegistered;
    }

    static void setAlreadyRegistered(boolean input) {
        global_variables.alreadyRegistered = input;
    }

    static String getName() {
        return name;
    }

    static void setName(String input) {
        global_variables.name = input;
    }

    static int getParticipantsCount() {
        return participantsCount;
    }

    static void setParticipantsCount(int input) {
        global_variables.participantsCount = input;
    }

    static int getId() {
        return Id;
    }

    static void setId(int input) {
        Id = input;
    }

    static void setParticipants(ArrayList<String> participants) {
        global_variables.participants = participants;
    }
}
