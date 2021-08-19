package com.example.mmovie3.data;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.security.PrivateKey;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.crypto.spec.PSource;

//класс для хранения и редактирования данных приложения через БД, посредством использования MovieDatabase и MovieDao
//AndroidViewModel является подклассом ViewModel и нужен для хранения и управления данными, связанными с UI.
//Засчет того, что у него есть свой жизненный цикл (LifeCycle) он хранит состояние нашей активности, пока программа работает. Не полностью, но заменяет собой onSaveInstanceState
//onPause, onStop итд не влияют на жизненный цикл AndroidViewModel
public class MainViewModel extends AndroidViewModel {

    //У нас получается БД сразу с доступом к методам интерфейса MovieDao, засчет того, что метод movieDao, возвращает объект MovieDao
    //static для того, чтобы иметь доступ из doInBackground
    private static MovieDatabase database;
    //сюда сохраняем объект LiveData, который вернет нам getAllMovies()
    private LiveData<List<Movie>> movies;

    //конструктор AndroidViewModel обязательно нужно переопределить, здесь инициализируем данные
    public MainViewModel(@NonNull Application application) {
        super(application);
        //получить экземпляр БД, если его нет, то создать. Передаем ссылку на приложение (контекст)
        database = MovieDatabase.getInstance(application);
        //получить все фильмы из БД и сохранить в объект LiveData
        movies = database.movieDao().getAllMovies();
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }

    //все действия в этом методе должны быть выполнены в другом програмном потоке, в этом нам поможет класс GetMovieTask унаследовавший AsyncTask
    public Movie getMovieByID(int id) {
        try {
            //при помощи AsyncTask execute(выполнить) GetMovieTask в другом потоке, передав id, get(получить) фильм
            return new GetMovieTask().execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //если что то пошло не так, то вернуть null
        return null;
    }

    private static class GetMovieTask extends AsyncTask<Integer, Void, Movie> {
        @Override
        protected Movie doInBackground(Integer... integers) {
            //передать id фильма в getMovieById, id попал в массив integers первым элементом, когда мы передали его туда через execute(id)
            //в итоге за нас все сделает getMovieById, передав запрос в БД
            if(integers != null && integers.length > 0) {
                return database.movieDao().getMovieById(integers[0]);
            }
            //если мы не передали параметры, то получим null
            return null;
        }
    }

    public void deleteAllMovies() {
        new DeleteMoviesTask().execute();
    }

    private static class DeleteMoviesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... integers) {
            //удалить все фильмы
                database.movieDao().deleteAllMovies();
            return null;
        }
    }

    public void insertMovie(Movie movie) {
        new InsertTask().execute(movie);
    }

    //так же как в GetMovieTask
    private static class InsertTask extends AsyncTask<Movie, Void, Void> {
        @Override
        protected Void doInBackground(Movie... movies) {
            if(movies != null && movies.length > 0) {
                database.movieDao().insertMovie(movies[0]);
            }
            return null;
        }
    }

    public void deleteMovie(Movie movie) {
        new DeleteTask().execute(movie);
    }

    private static class DeleteTask extends AsyncTask<Movie, Void, Void> {
        @Override
        protected Void doInBackground(Movie... movies) {
            //удалить все фильмы
            if(movies != null && movies.length > 0) {
                database.movieDao().deleteMovie(movies[0]);
            }
            return null;
        }
    }
}
