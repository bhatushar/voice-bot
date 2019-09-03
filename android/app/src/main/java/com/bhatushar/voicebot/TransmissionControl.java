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

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reference chart for transmission codes:
 * Command              Transmission code
 * move
 * |----forward
 * |    |----step              22
 * |    |----cm                26
 * |    |----inch              34
 * |----backward
 *      |----step              33
 *      |----cm                39
 *      |----inch              51
 * turn
 * |----left
 * |    |----step              55
 * |    |----degree            95
 * |    |----radian            115
 * |----right
 *      |----step              77
 *      |----degree            133
 *      |----radian            161
 * set
 * |----speed                  29
 * stop                        31
 */
class TransmissionControl {
    private static final String LOG_TAG = "TransmissionControl";
    // IP address of the bot
    private static final String URL = "http://192.168.43.88:80/";

    // Stores the regression rules for command parsing
    static private Pattern pattern;
    // Data tables, details in initRules()
    private static final Hashtable<String, Integer> wordMap = new Hashtable<>(12);
    private static final Hashtable<String, Integer> digitMap = new Hashtable<>(9);

    /**
     * Contains the final data to be transmitted
     * First index contains the code, second index contains the magnitude (if any).
      */
    private int[] data = new int[2];


    /**
     * Accepts a numerical string or a single digit word and returns the equivalent number.
     * Only valid string must be passed otherwise the method will throw a NullPointException.
     *
     * @param s Numeral string
     * @return Equivalent integer
     */
    private int getNumber(String s) {
        int num;
        try {
            // If s is a numerical string
            num = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            // s is a digit word
            num = digitMap.get(s);
        }
        return num;
    }

    /**
     * Method initializes the hash tables and creates the regression rules. These data elements
     * are used for validating the command string and generating an equivalent transmission code.
     * TransmissionControl has 2 dictionaries: wordMap and digitMap
     * They are initialized via a static method because they're common for all instances and need
     * to be set only once during program execution.
     */
    static void initRules() {
        /* Each keyword in a valid command is given a prime number
         *      forward: 2
         *      backward: 3
         *      left: 5
         *      right: 7
         *      step: 11
         *      centimetre/cm: 13
         *      inch: 17
         *      degree: 19
         *      radian:23
         *      speed: 29
         *      stop: 31
         */
        wordMap.put("forward", 2);
        wordMap.put("backward", 3);
        wordMap.put("left", 5);
        wordMap.put("right", 7);
        wordMap.put("step", 11);
        wordMap.put("centimetre", 13);
        wordMap.put("cm", 13);
        wordMap.put("inch", 17);
        wordMap.put("degree", 19);
        wordMap.put("radian", 23);
        wordMap.put("speed", 29);
        wordMap.put("stop", 31);

        /* The regex rules match for the following strings:
         * move [forward/backward] <number> [step/cm/inch]
         * turn [left/right] <number> [step/degree/radian]
         * set [speed] <number>
         * [stop]
         * Words within square brackets are keywords which have an entry in wordMap.
         * <number> is a one digit word or a numerical string.
         * The rules also support plural forms of units (like inches, degrees etc) as well as
         * abbreviations (like cm for centimetre).
         */
        // Optional space after a number - 30 centimetres or 30cm
        String number = "(one|two|three|four|five|six|seven|eight|nine|[0-9]+)\\s?";
        String regex =
                "^(move\\s(forward|backward)\\s"+ number +"(step|centimetre|cm|inch)(?:s|es)?)|" +
                "(turn\\s(left|right)\\s"+ number +"(step|degree|radian)s?)|" +
                "(set\\s(speed)\\s"+ number +")|" +
                "(stop)$";
        pattern = Pattern.compile(regex);

        /*
         * Since SpeechRecognizer doesn't return numerical digits, I implemented this table.
         * It's a simple mapping from word to digit.
         */
        digitMap.put("one", 1);
        digitMap.put("two", 2);
        digitMap.put("three", 3);
        digitMap.put("four", 4);
        digitMap.put("five", 5);
        digitMap.put("six", 6);
        digitMap.put("seven", 7);
        digitMap.put("eight", 8);
        digitMap.put("nine", 9);
    }

    /**
     * Method accepts a command string and parses it to obtain equivalent code.
     * Standard command form is: [verb][noun][magnitude][unit]
     * The length may vary, but the order is preserved.
     * parseCommand parses the string and generates equivalent transmission data if the command is valid.
     *
     * @param command String to be parsed
     * @return Parsing status
     */
    boolean parseCommand(@NotNull String command) {
        int code = 1;
        Matcher matcher = pattern.matcher(command);
        /* Quick guide to how this works:
         * Regex rules are divided into groups. When a string is matched successfully, relevant
         * groups contain information about the string.
         * For example, group 1 to 4 contain information about MOVE command. This means when a MOVE
         * command is received, groups 1 to 4 get populated while other groups are set to null.
         * Group ID     Contains
         * 0            Entire command string
         * 1            MOVE command
         * 2            forward/backward
         * 3            Number
         * 4            step/centimetre/cm/inch
         * 5            TURN command
         * 6            left/right
         * 7            Number
         * 8            step/degree/radian
         * 9            SET command
         * 10           speed
         * 11           Number
         * 12           STOP command
         */
        if (matcher.matches()) {
            for (int i = 0; i <= matcher.groupCount(); i++)
                Log.d(LOG_TAG, i + ": " + matcher.group(i));
            // Proceed if command matches the regex rules
            if (matcher.group(1) != null) {
                // Move command
                code *= wordMap.get(matcher.group(2)); // Direction
                code *= wordMap.get(matcher.group(4)); // Unit
                data[1] = getNumber(matcher.group(3));
            } else if (matcher.group(5) != null) {
                // Turn command
                code *= wordMap.get(matcher.group(6)); // Direction
                code *= wordMap.get(matcher.group(8)); // Unit
                data[1] = getNumber(matcher.group(7));
            } else if (matcher.group(9) != null) {
                // Set command
                code = wordMap.get(matcher.group(10)); // Parameter
                data[1] = getNumber(matcher.group(11));
            } else if (matcher.group(12) != null) {
                // Stop command
                code = wordMap.get(matcher.group(12));
            }
            data[0] = code;
            return true;
        } else return false; // Invalid command
    }

    /**
     * Transmits the code generated by parseCommand() over to the specified URL.
     * The basic syntax fot the address is: http://196.168.43.88:80/?code={code}&value={value}
     * The value parameter is optional.
     * The method uses Volley for request transmission. Since the response is returned to the
     * main thread, it requires a reference to the corresponding context.
     *
     * @param context MainActivity reference.
     */
    void transmit(Context context) {
        String params = "?code=" + data[0] + "&value=" + data[1];
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL+params,
            new Response.Listener<String>() {
                // For standard response
                @Override
                public void onResponse(String response) {
                    // Log the response from the bot
                    Log.d(LOG_TAG, response);
                    Log.d(LOG_TAG, "Transmission complete.");
                }
            }, new Response.ErrorListener() {
                // For error response
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

