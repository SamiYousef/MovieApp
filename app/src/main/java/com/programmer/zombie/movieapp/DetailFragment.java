package com.programmer.zombie.movieapp;


import android.app.Fragment;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailFragment extends Fragment {

    private long position;
    private boolean isFavourite;
    String key;
    String reviewsData;
    private ShareActionProvider shareActionProvider;

    LinearLayout trailers;

    TextView movie_review;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null)
            position = savedInstanceState.getLong("position");

        position = MainActivity.pos;

        View root = inflater.inflate(R.layout.fragment_detail, container, false);

        trailers = (LinearLayout) root.findViewById(R.id.trailers);

        TextView movie_title = (TextView) root.findViewById(R.id.movie_name);
        movie_title.setText(MainActivity.data.get((int) position).title);

        ImageView movie_icon = (ImageView) root.findViewById(R.id.movie_icon);
        Picasso.with(getActivity().getBaseContext())
                .load("http://image.tmdb.org/t/p/w185/" + MainActivity.data.get((int) position).posterPath)
                .into(movie_icon);

        TextView movie_release_date = (TextView) root.findViewById(R.id.movie_release_date);
        movie_release_date.setText(MainActivity.data.get((int) position).reliseDate);

        TextView movie_vote_average = (TextView) root.findViewById(R.id.movie_vote_average);
        movie_vote_average.setText(MainActivity.data.get((int) position).rate);

        TextView movie_overview = (TextView) root.findViewById(R.id.movie_overview);
        movie_overview.setText(MainActivity.data.get((int) position).overview);

        movie_review = (TextView) root.findViewById(R.id.movie_review);

        final Button favourite = (Button) root.findViewById(R.id.movie_add_to_favorite);
        if (FavouriteMoviesDataBase.isFavourite(getActivity().getBaseContext(), MainActivity.data.get((int) position)._id)) {
            favourite.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.star_big_on, 0, 0, 0);
            isFavourite = true;
        }
        favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteOpenHelper helper = new FavouriteMoviesDataBase(getActivity().getBaseContext());
                SQLiteDatabase dp = helper.getReadableDatabase();
                if (isFavourite) {
                    favourite.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.star_big_off, 0, 0, 0);
                    FavouriteMoviesDataBase.deleteData(dp, MainActivity.data.get((int) position)._id);
                    isFavourite = false;
                } else {
                    favourite.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.star_big_on, 0, 0, 0);
                    FavouriteMoviesDataBase.insertData(dp,
                            MainActivity.data.get((int) position)._id,
                            MainActivity.data.get((int) position).rate,
                            MainActivity.data.get((int) position).reliseDate,
                            MainActivity.data.get((int) position).overview,
                            MainActivity.data.get((int) position).posterPath,
                            MainActivity.data.get((int) position).title);

                    dp.close();
                    isFavourite = true;
                }
            }
        });

        return root;
    }

    private void getTrailers() {


        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://api.themoviedb.org/3/movie/" + MainActivity.data.get((int) MainActivity.pos)._id + "/videos",
                new RequestParams("api_key", "f62207fb95b0b4ab18845d3dc05eddc8"), new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject forecastJson = new JSONObject(response);
                            JSONArray posterArray = forecastJson.getJSONArray("results");

                            for (int i = 0; i < posterArray.length(); i++) {

                                JSONObject movieTrailer = posterArray.getJSONObject(i);
                                final String key1 = movieTrailer.getString("key");
                                key = key1;

                                Button btn = new Button(getActivity());
                                btn.setText("Trailer" + (i + 1));
                                btn.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);
                                btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + key1)));
                                    }
                                });
                                trailers.addView(btn);

                            }
                            setIntent();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable error, String content) {
                        checkError(statusCode);
                    }
                });
    }

    private void checkError(int statusCode) {
        if (statusCode == 404) {
            Toast.makeText(getActivity(), "Requested resource not found", Toast.LENGTH_LONG).show();
        } else if (statusCode == 500) {
            Toast.makeText(getActivity(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
        }
    }

    private void getReview() {

        reviewsData = "";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://api.themoviedb.org/3/movie/" + MainActivity.data.get((int) MainActivity.pos)._id + "/reviews",
                new RequestParams("api_key", "f62207fb95b0b4ab18845d3dc05eddc8"), new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject forecastJson = new JSONObject(response);
                            JSONArray posterArray = forecastJson.getJSONArray("results");

                            for (int i = 0; i < posterArray.length(); i++) {
                                JSONObject movieReview = posterArray.getJSONObject(i);
                                reviewsData += movieReview.getString("author").toLowerCase();
                                reviewsData += "\n";
                                reviewsData += movieReview.getString("content");
                                reviewsData += "\n**************\n";
                            }
                            movie_review.setText(reviewsData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable error, String content) {
                        checkError(statusCode);
                    }
                });
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("position", position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        getTrailers();
        getReview();
    }

    private void setIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" + key + "\n\n" + MainActivity.data.get((int) MainActivity.pos).title);
        shareActionProvider.setShareIntent(intent);
    }
}
