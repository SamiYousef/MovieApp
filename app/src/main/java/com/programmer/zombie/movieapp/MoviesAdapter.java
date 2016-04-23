package com.programmer.zombie.movieapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/*
 *
 * Created by Sami Youssef on 3/10/2016.
 */
public class MoviesAdapter extends ArrayAdapter<MovieData> {

    public MoviesAdapter(Context context, List<MovieData> listMovies) {
        super(context, 0, listMovies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MovieData mMovie = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item, parent, false);
        }

        ImageView mMovieLogo = (ImageView) convertView.findViewById(R.id.movie_logo);
        Picasso.with(getContext())
                .load("http://image.tmdb.org/t/p/w185/" + mMovie.posterPath)
//                .placeholder(R.drawable.poster)
//                .fit()
                .into(mMovieLogo);

        return convertView;
    }
}
