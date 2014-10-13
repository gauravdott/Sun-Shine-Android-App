package sunshine.arion.com.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;

import sunshine.arion.com.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    SimpleCursorAdapter weatherForecastAdapter;
    SharedPreferences preferences;
    private static final int FORECAST_LOADER = 0;
    private String mLocation;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {

            //In this case the ids need to be fully qualified with a table name, since
            // the content provider joins the Location and weather tables in the background
            // (both have an _id column)
            // On the other hand, that's annoying. On the other, you can search the weather table
            // using the Location set by the user, which is only in the Location table.
            // So the convenience is worth it.

            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_POSTAL_CODE
    };

    // These indices are tied to FORECAST_COLUMNS. If FORECAST_COLUMNS changes, these must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_POSTAL_CODE = 5;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        //updateWeather();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateWeather() {
        String postalCode = Utility.getPreferredLocation(getActivity());
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity());
        fetchWeatherTask.execute(postalCode);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        weatherForecastAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_forecast,
                null,
                // these column names
                new String[] {
                        WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
                },
                new int[] {
                        R.id.list_item_forecast_day,
                        R.id.list_item_forecast_weather,
                        R.id.list_item_forecast_max,
                        R.id.list_item_forecast_min
                },
                0
        );

        weatherForecastAdapter.setViewBinder(
            new SimpleCursorAdapter.ViewBinder() {

                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    boolean isMetric = Utility.isMetric(getActivity());
                    switch (columnIndex) {
                        case COL_WEATHER_MAX_TEMP:
                        case COL_WEATHER_MIN_TEMP:
                        {
                            //We have to do some formatting and possibly a conversion
                            ((TextView) view).setText(
                                    Utility.formatTemperature(cursor.getDouble(columnIndex), isMetric));
                            return true;
                        }
                        case COL_WEATHER_DATE:
                        {
                            String dateString = cursor.getString(columnIndex);
                            TextView dateView = (TextView)view;
                            dateView.setText(Utility.formatDate(dateString));
                            return true;
                        }
                    }
                    return false;
                }
        });

        View rootView = inflater.inflate(R.layout.fragment_my, container, false);

        final ListView weatherForecastList = (ListView) rootView.findViewById(R.id.listview_forecast);
        weatherForecastList.setAdapter(weatherForecastAdapter);

        weatherForecastList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent showDetails = new Intent(getActivity(), DetailActivity.class);
                SimpleCursorAdapter adapter = (SimpleCursorAdapter) parent.getAdapter();
                Cursor cursor = adapter.getCursor();

                if (cursor != null && cursor.moveToPosition(position)) {
//                    String dateString = Utility.formatDate(cursor.getString(COL_WEATHER_DATE));
//                    String weatherDescription = cursor.getString(COL_WEATHER_DESC);
//
//                    boolean isMetric = Utility.isMetric(getActivity());
//                    String high = Utility.formatTemperature(
//                            cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
//                    String low = Utility.formatTemperature(
//                            cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
//
//                    String detailString = String.format("%s - %s - %s/%s",
//                            dateString, weatherDescription, high, low);


//                    showDetails.putExtra(Intent.EXTRA_TEXT, detailString);
                    showDetails.putExtra(DetailActivity.DATE_KEY, cursor.getString(COL_WEATHER_DATE));
                    startActivity(showDetails);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new loader needs to be created. This
        // fragment only uses one loader, so we don't care about checking the id

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = Utility.getDbDateString(new Date());

        //Sort order: Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(mLocation, startDate);

        // Now we create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );

    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        weatherForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        weatherForecastAdapter.swapCursor(null);
    }
}