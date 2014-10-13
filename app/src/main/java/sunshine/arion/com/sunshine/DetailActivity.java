package sunshine.arion.com.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sunshine.arion.com.data.WeatherContract;

public class DetailActivity extends ActionBarActivity {

    public static final String DATE_KEY = "forecast_date";
    public static final String LOCATION_KEY = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor>{

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
        private String mForecastString;
        private String mLocation;
        ShareActionProvider mShareActionProvider;

        public static final int DETAIL_LOADER = 0;

        public static final String[] FORECAST_COLUMNS = {
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
        };

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            return rootView;
        }

        public Intent createShareWeatherIntent() {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastString + FORECAST_SHARE_HASHTAG);
            shareIntent.setType("text/plain");

            return shareIntent;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);

            //Locate MenuItem with ShareActionProvider
            MenuItem item = menu.findItem(R.id.action_share);

            //Fetch and store ShareActionProvider
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareWeatherIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null ?");
            }

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            if (savedInstanceState != null) {
                mLocation = savedInstanceState.getString(LOCATION_KEY);
            }
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public Loader onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In OnCreate Loader");
            Intent intent = getActivity().getIntent();
            if (intent == null || !intent.hasExtra(DATE_KEY))
                return null;

            String forecastDate = intent.getStringExtra(DATE_KEY);

            // Sort order: Ascending, by date
            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

            mLocation = Utility.getPreferredLocation(getActivity());

            Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, forecastDate);
            Log.v(LOG_TAG, weatherForLocationUri.toString());

            //Now create and return a CursorLoader that will take care of
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
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.v(LOG_TAG, "In onLoaderFinished");

            if (!data.moveToFirst()) {return;}

            String dateString = Utility.formatDate(
                    data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT))
            );
            ((TextView) getView().findViewById(R.id.detail_forecast_day))
                    .setText(dateString);

            String weatherDescription = data.getString(
                    data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC)
            );
            ((TextView) getView().findViewById(R.id.detail_forecast_weather))
                    .setText(weatherDescription);

            boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatTemperature(
                    data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)), isMetric);

            ((TextView) getView().findViewById(R.id.detail_forecast_max))
                    .setText(high);

            String low = Utility.formatTemperature(
                    data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)), isMetric);

            ((TextView) getView().findViewById(R.id.detail_forecast_min))
                    .setText(low);

            // We still need this for the share intent
            mForecastString = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

            Log.v(LOG_TAG, " Forecast String: " + mForecastString);

            // If onCreateOptions has already happened, we need to update the intent to now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareWeatherIntent());
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

}
