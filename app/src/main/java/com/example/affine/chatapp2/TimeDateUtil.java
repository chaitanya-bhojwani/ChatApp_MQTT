package com.example.affine.chatapp2;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by VikashPatel on 21/11/17.
 */

/**
 * Date and Time Pattern	             Result
 * <p>
 * "yyyy.MM.dd G 'at' HH:mm:ss z"	     2001.07.04 AD at 12:08:56 PDT
 * "EEE, MMM d, ''yy"	                 Wed, Jul 4, '01
 * "h:mm a"	                             12:08 PM
 * "hh 'o''clock' a, zzzz"	             12 o'clock PM, Pacific Daylight Time
 * "K:mm a, z"	                         0:08 PM, PDT
 * "yyyyy.MMMMM.dd GGG hh:mm aaa"	     02001.July.04 AD 12:08 PM
 * "EEE, d MMM yyyy HH:mm:ss Z"	         Wed, 4 Jul 2001 12:08:56 -0700
 * "yyMMddHHmmssZ"	                     010704120856-0700
 * "yyyy-MM-dd'T'HH:mm:ss.SSSZ"	         2001-07-04T12:08:56.235-0700
 * "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"	     2001-07-04T12:08:56.235-07:00
 * "YYYY-'W'ww-u"	                     2001-W27-3
 */

public class TimeDateUtil {

    public static String formatTime(long miliseconds) {
        Date date = new Date(miliseconds);
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
        return formatter.format(date);
    }

    public static String formatDateTime(long miliseconds) {
        Date date = new Date(miliseconds);
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM, HH:mm a");
        return formatter.format(date);
    }

    public static String formatDayDateOnly(long l) {
        Date date = new Date(l);
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d");
        return formatter.format(date);
    }

    public static String formatTimeOnly(long l) {
        Date date = new Date(l);
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aaa");
        return formatter.format(date);
    }

    public static String formatDateAndTime(long l) {
        Date date = new Date(l);
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d,yyyy, hh:mm aaa");
        return formatter.format(date);
    }

    public static String formatDayDateWithFullName(long l) {
        Date date = new Date(l);
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE MMMM d");
        return formatter.format(date);
    }


}
