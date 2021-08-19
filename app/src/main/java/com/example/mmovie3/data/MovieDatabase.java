package com.example.mmovie3.data;

import android.content.ContentProvider;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

//entities это таблицы, в нашем случае только таблица Movie.class
//по шаблону класса Movie будет создана таблица БД
//здесь мы используем паттерн Singleton - паттерн позволяет следить за тем, чтобы у нас был лишь один экземпляр класса. Даже названия будут соответствовать паттерну
//P.S. абстрактный класс может не реализовывать методы интерфейса
@Database(entities = {Movie.class}, version = 3, exportSchema = false)
public abstract class MovieDatabase extends RoomDatabase {

    private static final String DB_NAME = "movies.db";
    private static MovieDatabase database;
    //блок синхронизации во избежании проблем доступа к классу из разных потоков
    private static final Object LOCK = new Object();

    //если база данных не создана, создать ее, если она уже есть, то просто взять database
    //синхронизация следит за тем, чтобы в один момент времени могла быть создана лишь одна БД
    public static MovieDatabase getInstance(Context context) {
        synchronized (LOCK) {
            if (database == null) {
                //передаем (контекст, название класса который мы расширяем при помощи Room т е наш класс, и имя БД)
                database = Room.databaseBuilder(context, MovieDatabase.class, DB_NAME).fallbackToDestructiveMigration().build();
            }
        }
        return database;
    }

    //получить объект интерфейса MovieDao
    public abstract MovieDao movieDao();
}
