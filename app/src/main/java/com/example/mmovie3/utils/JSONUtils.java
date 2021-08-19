package com.example.mmovie3.utils;

import com.example.mmovie3.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//преобразование JSON в объект Movie
public class JSONUtils {

    //ключ для получения массива results, из JSON
    private static final String KEY_RESULTS = "results";
    //прочие ключи
    private static final String KEY_VOTE_COUNT = "vote_count";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_ORIGINAL_TITLE = "original_title";
    private static final String KEY_OVERVIEW = "overview";
    private static final String KEY_POSTER_PATH = "poster_path";
    private static final String KEY_BACKDROP_PATH = "backdrop_path";
    private static final String KEY_VOTE_AVERAGE = "vote_average";
    private static final String KEY_RELEASE_DATE = "release_date";

    //данные для заполнения начала строки, для передачи адреса и размера постера в XML
    public static final String BASE_POSTER_URL = "https://image.tmdb.org/t/p/";
    //размеры постеров мы подсмотрели в API на сайте
    public static final String SMALL_POSTER_SIZE = "w185";
    public static final String BIG_POSTER_SIZE = "w185";

    //получить массив с фильмами из объекта JSON (нужно для работы с БД)
    public static ArrayList<Movie> getMoviesFromJSON(JSONObject jsonObject) {
        //массив со списком наших фильмов
        ArrayList<Movie> result = new ArrayList<>();
        if(jsonObject == null) {
            //если jsonObject указывает на пустую ссылку, то выходим из метода вернув null
            return result;
        }
        try {
            //достать массив results, по ключу KEY_RESULTS
            JSONArray jsonArray = jsonObject.getJSONArray((KEY_RESULTS));
            for(int i = 0; i < jsonArray.length(); i++) {
                //доставать из полученного массива по одному элементу, пока i не будет равно длинне массива
                JSONObject objectMovie = jsonArray.getJSONObject(i);
                //вытащить все данные из полученного элемента массива
                int id = objectMovie.getInt(KEY_VOTE_COUNT);;
                int voteCount = objectMovie.getInt(KEY_ID);
                String title = objectMovie.getString(KEY_TITLE);
                String originalTitle = objectMovie.getString(KEY_ORIGINAL_TITLE);
                String overview = objectMovie.getString(KEY_OVERVIEW);
                String posterPath = BASE_POSTER_URL + SMALL_POSTER_SIZE + objectMovie.getString(KEY_POSTER_PATH);
                String bigPosterPath = BASE_POSTER_URL + BIG_POSTER_SIZE + objectMovie.getString(KEY_POSTER_PATH);
                String backdropPath = objectMovie.getString(KEY_BACKDROP_PATH);
                double voteAverage = objectMovie.getDouble(KEY_VOTE_AVERAGE);
                String releaseDate = objectMovie.getString(KEY_RELEASE_DATE);
                //сохраняем все полученные данные в объект Movie, получаем данные об одном фильме
                Movie movie = new Movie(id, voteCount, title, originalTitle, overview, posterPath, bigPosterPath, backdropPath, voteAverage, releaseDate);
                result.add(movie);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
