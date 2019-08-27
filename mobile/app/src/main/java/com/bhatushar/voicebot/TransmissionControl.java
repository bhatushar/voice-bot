package com.bhatushar.voicebot;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

class TransmissionControl {
    private static final String LOG_TAG = "TransmissionControl";

    private static final String URL = "http://192.168.43.88:80/";

    /**
     * Contains the final data to be transmitted
     * First index contains the code, second index contains the magnitude (if any).
      */
    private int[] data = new int[2];

    // Dictionaries
    private static final Hashtable<String, Integer> codeDictionary = new Hashtable<>(10);
    private static final Set<Integer> validCodes = new HashSet<>(6);;
    private static final Hashtable<String, Integer> digits = new Hashtable<>(9);

    /**
     * Method initializes all static dictionaries.
     * TransmissionControl has 3 dictionaries: codeDictionary, validCodes and digits
     * They are initialized via a static method because they're all final objects.
     * Initializing them for every object will unnecessarily slow down the process.
     */
    static void initDataDictionary() {
        /**
         * Each keyword in a valid command is given a prime number
         *      move: 2
         *      forward: 3
         *      backward: 5
         *      turn: 7
         *      left: 11
         *      right: 13
         *      set: 17
         *      speed: 19
         *      stop: 23
         *      steps: 29
         */
        codeDictionary.put("move", 2);
        codeDictionary.put("forward", 3);
        codeDictionary.put("backward", 5);
        codeDictionary.put("backwards", 5);
        codeDictionary.put("turn", 7);
        codeDictionary.put("left", 11);
        codeDictionary.put("right", 13);
        codeDictionary.put("set", 17);
        codeDictionary.put("speed", 19);
        codeDictionary.put("stop", 23);
        codeDictionary.put("steps", 29);

        /**
         * A valid code is generated my multiplying the numbers assigned to each keyword.
         * Since product of two prime numbers is unique, the code generated will also be unique.
         * Possible combinations:                  Product
         *     move forward <number> step(s)       174
         *     move backward <number> steps(s)     290
         *     turn left <number> step(s)          2233
         *     turn right <number> steps(s)        2639
         *     set speed <number>                  323
         *     stop                                23
         */
        validCodes.add(174);
        validCodes.add(290);
        validCodes.add(2233);
        validCodes.add(2639);
        validCodes.add(323);
        validCodes.add(23);

        /**
         * Since SpeechRecognizer doesn't return numerical digits, I implemented this table.
         * It's a simple mapping from word to digit.
         */
        digits.put("one", 1);
        digits.put("two", 2);
        digits.put("three", 3);
        digits.put("four", 4);
        digits.put("five", 5);
        digits.put("six", 6);
        digits.put("seven", 7);
        digits.put("eight", 8);
        digits.put("nine", 9);
    }

    /**
     * Method accepts a command string and parses it to obtain equivalent code.
     * Standard command form is: [verb][noun][magnitude][unit]
     * The length may vary, but the order is preserved.
     * parseCommand parses the string and generates equivalent data if the command is valid.
     *
     * @param command String to be parsed
     * @return Parsing status
     */
    boolean parseCommand(@NotNull String command) {
        String[] words = command.split("\\s+");
        // Index position 2 and 3 are reserved
        final short MAGNITUDE = 2, UNIT = 3;
        boolean parsed;

        // Iterate words array to obtain corresponding code
        int code = 1;
        for (int i = 0; i < words.length; i++) {
            if (i == UNIT && words[i].charAt(words[i].length()-1) != 's')
                /*
                    Special case for unit identification.
                    The codeDictionary only contains plural form of units.
                    If last character of words[UNIT] is not 's', unit is in singular form.
                    Appending 's' to match with equivalent unit in codeDictionary.
                */
                words[UNIT] += 's';
            try {
                if (i != MAGNITUDE)
                    // Magnitudes are not part of codeDictionary
                    code *= codeDictionary.get(words[i]);
            } catch (NullPointerException e) {
                // Unknown word in command. Cannot generate code.
                code = 1;
                break;
            }
        }

        // Checking for validity
        if (!validCodes.contains(code)) {
            Log.e(LOG_TAG, "Parsing failed. Invalid code generated: " + code);
            parsed = false;
        } else {
            data[0] = code;
            if (code != codeDictionary.get("stop")) {
                // The code is accompanied by a magnitude
                try {
                    if (digits.contains(words[MAGNITUDE]))
                        // Single digit word
                        data[1] = digits.get(words[MAGNITUDE]);
                    // Parse integer
                    else data[1] = Integer.parseInt(words[MAGNITUDE]);
                    parsed = true;
                } catch (NumberFormatException e) {
                    // Magnitude is not a number
                    parsed = false;
                }
            } else {
                // Code to be sent is for stopping
                // No magnitude required
                data[1] = 0;
                parsed = true;
            }
        }
        return parsed;
    }

    void transmit(Context context) {
        String params = "?code=" + data[0] + "&value=" + data[1];
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL+params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Log the response from the bot
                        Log.d(LOG_TAG, response);
                        Log.d(LOG_TAG, "Transmission complete.");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Reponse error: "+error.getMessage());
                error.printStackTrace();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}

