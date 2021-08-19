package com.example.mmovie3.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

//Dao, Insert, Query и пр. аннотации помогают указать, что должен делать класс либо метод. Они содержатся в классе Room. Здесь они для синхронизации работы БД и этого интерфейса
//Dao - значит, что это объект для доступа к БД
//ниже пишем методы с готовыми запросами к БД, это удобно, не нужно создавать отдельный класс со строковыми запросами, для работы с БД
@Dao
public interface MovieDao {
    //Объект LiveData следит за жизненным циклом данных при помощи Observer, и при их изменении дает знать об этом приложению. Т е БД даст знать приложению, что в ней вознникли изменения
    //А так же все действия с объектами LiveData автоматически выполняются в другом потоке
    //Query  значит, что метод должен вызываться при запросе к БД, и отправить туда указанный запрос
    //метод возвращает объект LiveData, который содержит List<Movies>
    @Query("SELECT * FROM movies")
    LiveData<List<Movie>> getAllMovies();

    @Query("SELECT * FROM movies WHERE id == :id")
    Movie getMovieById(int id);

    @Query("DELETE FROM movies")
    void deleteAllMovies();

    //Insert позволяет вставить данные в БД
    @Insert
    void insertMovie(Movie movie);

    //Delete позволяет удалять данные из БД, но лишь те, что указали
    //Для удаления всех данных придется воспользоваться аннотацией Query
    @Delete
    void deleteMovie(Movie movie);
}
