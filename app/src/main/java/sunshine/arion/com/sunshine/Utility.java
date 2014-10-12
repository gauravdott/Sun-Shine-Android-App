package sunshine.arion.com.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {

    //Format used for storing dates in the database. Also used for converting those
    //strings back into date objects for comparison/processing

    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Converts Date class to a string representation, used for easy comparison and database lookup
     * @param date The input date
     * @return a DB-fiendly representation of the date, using the format defined in the DATE_FORMAT
     */
    public static String getDbDateString(Date date) {
        //Because the API returns a unix timestamp (measured in seconds),
        //it must be converted to milliseconds in order to be converted to valid date.

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }

    /**
     * Converts a dateText to a long Unix representation
     * @param dateText the input date string
     * @return the Date object
     */
    public static Date getDateFromDb(String dateText) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            return dbDateFormat.parse(dateText);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_unit_key),
                context.getString(R.string.pref_unit_metric))
                .equals(context.getString(R.string.pref_unit_metric));
    }

    static String formatTemperature(double temperature, boolean isMetric) {
        double temp;
        if ( !isMetric ) {
            temp = 9 * temperature/5 * 32;
        } else {
            temp = temperature;
        }
        return String.format("%.0f", temp);
    }

    static String formatDate(String dateString) {
        Date date = getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }
//
//    /**
//     * Helper method to convert the database representation of the date into something
//     * to display to users. A classy and polished user experience as "20140102" is, we can do better
//     *
//     * @param context Context to use for resource localization
//     * @param dateStr The db formatted date string, expected to be of the form specified
//     *                in Utility.DATE_FORMAT
//     * @return a user-friendly representation of the date
//     */
//
//    public static String getFriendlyDateString(Context context, String dateStr) {
//        //The day string for forecast uses the following logic:
//        //For today, "Today, Jun 8"
//        //For tomorrow, "Tomorrow"
//        //For the next 5 days: "Wednesday" (just the day name)
//        //For all the days after that: "Mon Jun 8"
//
//        Date todayDate = new Date();
//        String todayStr = WeatherContract.
//    }

    public static String getPreferredLocation(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }
}
