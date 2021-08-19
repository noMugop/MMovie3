package com.example.mmovie3.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

//вся работа связанная с сетью
public class NetworkUtils {
    //коснтанта для создания запроса, дабы не вбивать в ручную
    private static final String BASE_URL = "https://api.themoviedb.org/3/discover/movie";

    //ключи
    private static final String PARAMS_API_KEY = "api_key";
    private static final String PARAMS_LANGUAGE = "language";
    private static final String PARAMS_SORT_BY = "sort_by";
    private static final String PARAMS_PAGE = "page";
    private static final String PARAMS_MIN_VOTE_COUNT = "vote_count.gte";

    //константа для создания запроса в buildURL, дабы не вбивать в ручную
    //эти ключи мы берем с сайта, из объекта, который был создан, с использованием API сайта
    private static final String API_KEY = "a14a376a2704b9c91446a56f236f5b50";
    private static final String SORT_BY_POPULARITY = "popularity.desc";
    private static final String SORT_BY_TOP_RATED = "vote_average.desc";
    private static final String MIN_VOTE_COUNT_VALUE = "1000";

    //значение отвечающее за результат buildURL()
    public static final int POPULARITY = 0;
    public static final int TOP_RATED = 1;

    //метод для получения JSON из сети
    //для его работы добавляем в AndroidManifest.xml строку <uses-permission android:name="android.permission.INTERNET"/>
    public static JSONObject getJSONFromNetwork(int sortBy, int page, String lang) {
        JSONObject result = null;
        //получить отформатированный адрес
        URL url = buildURL(sortBy, page, lang);
        try {
            //execute(скачать код страницы по заданному url, выполнить JSONLoadTask).а затем get() получить JSONObject, сохранить в result
            //AsyncTask и его методы, работают во второстепенном потоке, не в главном
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    //в зависимости от заданых в uri параметров, задает странице различные параметры
    //метод выдает нам отфарматированный адрес
    //Url и Uri (см. ТЕРМИНЫ_ANDROID STUDIO)
    public static URL buildURL(int sortBy, int page, String lang) {
        URL result = null;                          //URL - это тоже строка, но определенного формата, в виде адреса страницы
        String methodOfSort;
        if (sortBy == POPULARITY) {
            methodOfSort = SORT_BY_POPULARITY;
        } else {
            methodOfSort = SORT_BY_TOP_RATED;
        }
        //Uri позволяет собрать строку, для дальнейшей передачи ее, например в URL
        Uri uri = Uri.parse(BASE_URL).buildUpon()                           //записать адрес строки в Uri, сделав парсинг (извлечение данных) из BASE_URL. Uri хранит строку в специальном формате
                .appendQueryParameter(PARAMS_API_KEY, API_KEY)              //дописываем доп. ключи к нашей адресной строке, своего рода идентификация
                .appendQueryParameter(PARAMS_LANGUAGE, lang)                //язык страницы, зависит от переданной сюда переменной lang
                .appendQueryParameter(PARAMS_SORT_BY, methodOfSort)         //метод сортивровки
                .appendQueryParameter(PARAMS_MIN_VOTE_COUNT, MIN_VOTE_COUNT_VALUE) //минимальное кол-во голосов
                .appendQueryParameter(PARAMS_PAGE, Integer.toString(page))  //номер страницы
                .build();                                                   //собираем строку
        try {
            result = new URL(uri.toString());                               //результат возвращаем в виде URL строки
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;                                                      //если, что то не так, то вернется null
    }

    //загрузчик AsyncTaskLoader, работает практически как обычный AsyncTask, загрузка данных в другом потоке
    public static class JSONLoader extends AsyncTaskLoader<JSONObject> {

        //AsyncTaskLoader загружает данные не на прямую в url, а через объект Bundle.
        private Bundle bundle;
        private OnStartLoadingListener onStartLoadingListener;

        //OnStartLoadingListener реагирует на начало загрузки JSONLoader, в MainActivity
        public interface OnStartLoadingListener {
            void onStartLoading();
        }

        public void setOnStartLoadingListener(OnStartLoadingListener onStartLoadingListener) {
            this.onStartLoadingListener = onStartLoadingListener;
        }

        //конструктор
        public JSONLoader(@NonNull Context context, Bundle bundle) {
            super(context);
            this.bundle = bundle;
        }

        //нужен для того, чтобы при инициализации загрузчика, происходила загрузка
        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if(onStartLoadingListener != null) {
                onStartLoadingListener.onStartLoading();
            }
            //продолжить загрузку
            forceLoad();
        }

        //описываем что делать в другом потоке
        @Nullable
        @Override
        public JSONObject loadInBackground() {
            if(bundle == null) {
                return null;
            }
            //указать откуда мы хотим получить данные
            String urlAsString = bundle.getString("url");
            URL url = null;
            try {
                url = new URL(urlAsString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            //из полученного URL нужно получить данные
            JSONObject result = null;
            if (url == null) {
                //если наш адрес равен 0 или null, то возвращаем пустую ссылку
                return null;
            }
            //если все нормально, то создаем http соединение
            HttpURLConnection connection = null;
            try {
                //открываем соединение
                connection = (HttpURLConnection) url.openConnection();
                //подключаемся к потоку данных нашего соединения
                InputStream inputStream = connection.getInputStream();
                //выделяем место под данные, которые будем читать при помощи inputStreamReader(передаем поток)
                //читает сразу по много символов в массив
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                //читаем из буффера, по строчно
                BufferedReader reader = new BufferedReader(inputStreamReader);
                //склеивает полученные строки
                StringBuilder builder = new StringBuilder();
                //начинаем читать данные
                String line = reader.readLine();
                //получается рекурсия, a+=b, обновить значение c, b=c, a+=b
                //т е builder+=line, обновить значение reader, line=reader, builder+=line
                while (line != null) {
                    //append(), делает builder += line, склеивая всё в одну длинную строку
                    builder.append(line);
                    //затем читаем следующую строку
                    line = reader.readLine();
                }
                result = new JSONObject(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    //обязательно закрываем соединение после всех работ
                    connection.disconnect();
                }
            }
            //если туда что то попало, то вернуть JSONObject, иначе пустую ссылку
            return result;
        }
    }

    //загрузка данных из интернета, в другом потоке
    //здесь мы скачиваем код страницы, передав в метод отформатированный URL
    private static class JSONLoadTask extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... urls) {
            JSONObject result = null;
            if (urls == null || urls.length == 0) {
                //если наш адрес равен 0 или null, то возвращаем пустую ссылку
                return null;
            }
            //если все нормально, то создаем http соединение
            HttpURLConnection connection = null;
            try {
                //открываем соединение
                connection = (HttpURLConnection) urls[0].openConnection();
                //подключаемся к потоку данных нашего соединения
                InputStream inputStream = connection.getInputStream();
                //выделяем место под данные, которые будем читать при помощи inputStreamReader(передаем поток)
                //читает сразу по много символов в массив
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                //читаем из буффера, по строчно
                BufferedReader reader = new BufferedReader(inputStreamReader);
                //склеивает полученные строки
                StringBuilder builder = new StringBuilder();
                //начинаем читать данные
                String line = reader.readLine();
                //получается рекурсия, a+=b, обновить значение c, b=c, a+=b
                //т е builder+=line, обновить значение reader, line=reader, builder+=line
                while (line != null) {
                    //append(), делает builder += line, склеивая всё в одну длинную строку
                    builder.append(line);
                    //затем читаем следующую строку
                    line = reader.readLine();
                }
                result = new JSONObject(builder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    //обязательно закрываем соединение после всех работ
                    connection.disconnect();
                }
            }
            //если туда что то попало, то вернуть JSONObject, иначе пустую ссылку
            return result;
        }
    }
}


