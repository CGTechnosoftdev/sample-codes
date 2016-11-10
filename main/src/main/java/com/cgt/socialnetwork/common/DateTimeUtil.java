package com.cgt.socialnetwork.common;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by CGTechnosoft
 */
public class DateTimeUtil {

    private static String[] dateFormats = {"EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd MMM yyyy HH:mm:ss Z",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ss", "yyyyMMdd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "MMMMM dd, yyyy",
            "yyyy-MM-dd", "EEE, MMM dd, yyyy", "MM/dd/yyyy HH:mm:ss aa", "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm:ss ZZZ", "EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd MMM yyyy HH:mm:ss z",
            "EEE, dd MMM yyyy HH:mm:ss", "EEE, dd MMM yyyy", "dd MMM yyyy HH:mm:ss zzz", "EEE, dd MMM yyyy HH:mm:ss 'Z'",
            "MM/dd/yyy", "dd/MM/yyy"};

    public static final String DATE_FORMAT_SERVER = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String DATE_FORMAT_MOBILE = "dd MMM yyyy";

    private static SimpleDateFormat[] dateFormatters;

    public static String getDateFromTimeStamp(long timestamp, String format) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            Date netDate = new Date(timestamp);
            return formatter.format(netDate);
        } catch (Exception ex) {
            return null;
        }
    }

    public static long parseDateString(String v, String format) {
        long rc = 0;
        if (!TextUtils.isEmpty(v)) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(format, Locale.ENGLISH);
            dateFormatter.setLenient(false);
            try {
                return dateFormatter.parse(v).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return rc;
    }

    public synchronized static long parseDateString(String v) {
        long rc = 0;
        if (v != null) {
            rc = parseUnknownDateString(v);
        }
        return rc;
    }

    private static long parseUnknownDateString(String dateString) {
        long rc = 0;
        if (dateFormatters == null) {
            dateFormatters = new SimpleDateFormat[dateFormats.length];
            for (int i = 0; i < dateFormats.length; i++) {
                dateFormatters[i] = new SimpleDateFormat(dateFormats[i], Locale.ENGLISH);
            }
        }
        for (int i = 0; i < dateFormatters.length; i++) {
            try {
                dateFormatters[i].setLenient(false);
                rc = dateFormatters[i].parse(dateString).getTime();
                break;
            } catch (ParseException pe) {
                rc = 0;
            }
        }
        return rc;
    }

    public static long convertUTCToGMT(String strDate) {
        try {
            SimpleDateFormat sourceFormat = new SimpleDateFormat(DATE_FORMAT_SERVER);
            sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsed = sourceFormat.parse(strDate);

            SimpleDateFormat destFormat = new SimpleDateFormat(DATE_FORMAT_SERVER);
            destFormat.setTimeZone(TimeZone.getDefault());

            String result = destFormat.format(parsed);
            return parseDateString(result, DATE_FORMAT_SERVER);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static long convertUTCToGMT(String strDate, String format) {
        try {
            SimpleDateFormat sourceFormat = new SimpleDateFormat(format);
            sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsed = sourceFormat.parse(strDate);

            SimpleDateFormat destFormat = new SimpleDateFormat(format);
            destFormat.setTimeZone(TimeZone.getDefault());

            String result = destFormat.format(parsed);
            return parseDateString(result, DATE_FORMAT_SERVER);
        } catch (ParseException e) {
            //e.printStackTrace();
        }

        return 0;
    }
}
