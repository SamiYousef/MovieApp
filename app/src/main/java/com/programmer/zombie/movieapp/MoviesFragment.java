package com.programmer.zombie.movieapp;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MoviesFragment extends Fragment {

    MoviesAdapter mAdapter;
    GridView mGridView;

    SharedPreferences prefs;
    String selectedOption;

    String selectedOptionKey = "com.example.app.option";


    public interface WorkoutListListener {
        void itemClicked(long id);
    }

    private WorkoutListListener listener;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (WorkoutListListener) activity;
    }

    public void invokeWS(String link, RequestParams params) {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(link, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                final String _ID = "id";
                final String POSTER_PATH = "poster_path";
                final String OVERVIEW = "overview";
                final String VOTE_AVERAGE = "vote_average";
                final String TITLE = "original_title";
                final String LIST = "results";
                final String RELEASE_DATE = "release_date";

                try {
                    JSONObject forecastJson = new JSONObject(response);
                    JSONArray posterArray = forecastJson.getJSONArray(LIST);

                    MainActivity.data.clear();
                    for (int i = 0; i < posterArray.length(); i++) {
                        JSONObject movieForecast = posterArray.getJSONObject(i);


                        MovieData movie = new MovieData();
                        movie._id = movieForecast.getInt(_ID);
                        movie.posterPath = movieForecast.getString(POSTER_PATH);
                        movie.overview = movieForecast.getString(OVERVIEW);
                        movie.rate = movieForecast.getString(VOTE_AVERAGE);
                        movie.title = movieForecast.getString(TITLE);
                        movie.reliseDate = movieForecast.getString(RELEASE_DATE);

                        MainActivity.data.add(movie);
                    }

                    mAdapter = new MoviesAdapter(getContext(), MainActivity.data);
                    mGridView.setAdapter(mAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Throwable error, String content) {
                checkStatusCode(statusCode);
            }
        });
    }


    void checkStatusCode(int statusCode) {
        if (statusCode == 404) {
            Toast.makeText(getActivity(), "Requested resource not found", Toast.LENGTH_LONG).show();
        } else if (statusCode == 500) {
            Toast.makeText(getActivity(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        //load new data if movie is removed from database
        showSelectedPref();
    }

    private void loadDataFromDataBase() {
        Cursor cursor = FavouriteMoviesDataBase.LoadFavouriteMovies(getContext());
        MainActivity.data.clear();
        if (cursor.moveToFirst()) {
            do {
                MovieData movie = new MovieData();
                movie._id = Integer.parseInt(cursor.getString(0));
                movie.rate = cursor.getString(1);
                movie.reliseDate = cursor.getString(2);
                movie.overview = cursor.getString(3);
                movie.posterPath = cursor.getString(4);
                movie.title = cursor.getString(5);

                MainActivity.data.add(movie);
            } while (cursor.moveToNext());
        }


        mAdapter = new MoviesAdapter(getContext(), MainActivity.data);
        mGridView.setAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_movies, container, false);

        prefs = getActivity().getSharedPreferences("com.example.app", Context.MODE_PRIVATE);

        selectedOption = prefs.getString(selectedOptionKey, "most_popular");//****

        mGridView = (GridView) root.findViewById(R.id.movies_grid);


        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.itemClicked(id);
            }
        });

        if (!(savedInstanceState != null && savedInstanceState.getBoolean("dataLoaded"))) {
            showSelectedPref();
        } else {
            mAdapter = new MoviesAdapter(getContext(), MainActivity.data);
            mGridView.setAdapter(mAdapter);
        }

        return root;
    }

    private void showSelectedPref() {
        switch (prefs.getString(selectedOptionKey, "most_popular")) {
            case "most_popular":
                RequestParams params = new RequestParams();
                params.add("api_key", "f62207fb95b0b4ab18845d3dc05eddc8");
                invokeWS("http://api.themoviedb.org/3/movie/popular", params);
                break;
            case "highest_rated":
                RequestParams param = new RequestParams();
                param.add("api_key", "f62207fb95b0b4ab18845d3dc05eddc8");
                invokeWS("http://api.themoviedb.org/3/movie/top_rated", param);
                break;
            case "favorites":
                loadDataFromDataBase();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("dataLoaded", true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_settings, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.most_popular:
                prefs.edit().putString(selectedOptionKey, "most_popular").apply();
                break;
            case R.id.highest_rated:
                prefs.edit().putString(selectedOptionKey, "highest_rated").apply();
                break;
            case R.id.favorites:
                prefs.edit().putString(selectedOptionKey, "favorites").apply();
                break;
        }
        showSelectedPref();
        return super.onOptionsItemSelected(item);
    }
}
