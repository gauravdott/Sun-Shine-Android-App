package sunshine.arion.com.sunshine.arion.com.test;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.Map;
import java.util.Set;

import sunshine.arion.com.data.WeatherContract.LocationEntry;
import sunshine.arion.com.data.WeatherContract.WeatherEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();
    static public String TEST_CITY_NAME = "North Pole";
    static public String TEST_LOCATION = "99705";
    static public String TEST_DATE = "20141205";


    public void testGetType() {

        // content://sunshine.arion.com/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        //vnd.android.cursor.dir/sunshine.arion.com/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocationId = "302012";
        // content://sunshine.arion.com/weather/302012
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocation(testLocationId));
        //vnd.android.cursor.dir/sunshine.arion.com/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://sunshine.arion.com/weather/302012/20140612
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocationWithDate(testLocationId, testDate));
        //vnd.android.cursor.dir/sunshine.arion.com/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://sunshine.arion.com/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        //vnd.android.cursor.dir/sunshine.arion.com/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://sunshine.arion.com/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        //vnd.android.cursor.dir/sunshine.arion.com/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);


    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source) {
        for (String key : source.keySet()) {
            destination.put(key, source.getAsString(key));
        }
    }

    public void testInsertReadProvider() {

        ContentValues locationValues = createNorthPoleLocationValues();

        long locationRowId;
        Uri locationInsertUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, locationValues);
        assertTrue(locationInsertUri != null);

        locationRowId = ContentUris.parseId(locationInsertUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);


        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = createWeatherValues(locationRowId);

        Uri weatherInsertUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
        assertTrue(weatherInsertUri != null);

        long weatherRowId = ContentUris.parseId(weatherInsertUri);


        // A cursor is your primary interface to the query results.

        //Test Weather content uri
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (weatherCursor.moveToFirst()) {
            validateCursor(weatherCursor, weatherValues);
        } else {
            fail("No Weather data returned!");
        }

        Cursor locationCursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (locationCursor.moveToFirst()) {
            validateCursor(locationCursor, locationValues);
        } else {
            fail("No Weather data returned!");
        }

        //Test Location ID content uri
        locationCursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order

        );

        if (locationCursor.moveToFirst()) {
            validateCursor(locationCursor, locationValues);
        } else {
            fail("No Weather data returned!");
        }

        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        addAllContentValues(weatherValues, locationValues);

        // Get the joined Weather and Location data
        Cursor weatherLocationCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TEST_LOCATION),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (weatherLocationCursor.moveToFirst()) {
            validateCursor(weatherLocationCursor, weatherValues);
        } else {
            fail("No Weather data returned!");
        }

        // Get the joined Weather and Location data with a start date
        weatherLocationCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION, TEST_DATE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (weatherLocationCursor.moveToFirst()) {
            validateCursor(weatherLocationCursor, weatherValues);
        } else {
            fail("No Weather data returned!");
        }

        weatherLocationCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION, TEST_DATE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (weatherLocationCursor.moveToFirst()) {
            validateCursor(weatherLocationCursor, weatherValues);
        } else {
            fail("No Weather data returned!");
        }

    }

    static ContentValues createWeatherValues(long locationRowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, TEST_DATE);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;
    }

    static ContentValues createNorthPoleLocationValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(LocationEntry.COLUMN_POSTAL_CODE, TEST_LOCATION);
        testValues.put(LocationEntry.COLUMN_LOCATION_NAME, TEST_CITY_NAME);
        testValues.put(LocationEntry.COLUMN_COORD_LATITUDE, 64.7488);
        testValues.put(LocationEntry.COLUMN_COORD_LONGITUDE, -147.353);

        return testValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

    //Brings our database to an empty state
    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    //Since we want each test to start with a clean slate, run deleteAllRecords
    //in setup (called by test runner before each test
    public void setUp() {
        deleteAllRecords();
    }

    public void testUpdateLocation() {
        //Create a new map of values, where column names are the keys
        ContentValues values = createNorthPoleLocationValues();

        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        //Verify we got a row back
        assertTrue(locationRowId != -1);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_LOCATION_NAME, "Santa's Village");

        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI, updatedValues, LocationEntry._ID + "= ?",
                new String[] {Long.toString(locationRowId)}
        );
        assertEquals(count, 1);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        validateCursor(cursor, updatedValues);
    }

}
