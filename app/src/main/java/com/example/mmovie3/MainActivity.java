package com.example.mmovie3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.HasDefaultViewModelProviderFactory;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mmovie3.data.MainViewModel;
import com.example.mmovie3.data.Movie;
import com.example.mmovie3.utils.JSONUtils;
import com.example.mmovie3.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//LoaderCallbacks - значит, что MainActivity является слушателем. <передаем данные, которые мы хотим получить из загрузчика>
//требует переопределить три метода onCreateLoader, onLoadFinished, onLoaderReset
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {

    private Switch switchSort;
    private RecyclerView recyclerViewPosters;
    private MovieAdapter movieAdapter;
    private TextView textViewTopRated;
    private TextView textViewPopularity;
    private ProgressBar progressBarLoading;

    private MainViewModel viewModel;

    //уникальное id загрузчика, не важно какое число.
    private static final int LOADER_ID = 32;
    //менеджер загрузок
    private LoaderManager loaderManager;

    //переменная хранит номер загружаемой страницы, и будет увеличиваться на 1, как только загрузится выбранная страница
    private static int page = 1;
    //чтобы setOnReachEndListener не вызывался несоклько раз, пока данные грузятся, нужна данная переменная
    private static boolean isLoading = false;
    //сохранить текущий способ сортировки, при подгрузке новых данных, для этого сделаи переменную глобальной
    private static int methodOfSort;

    private static String lang;
    private static int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //чтобы получить язык, который используется на устройстве в данный момент
        lang = Locale.getDefault().getLanguage();
        //получить экземпляр загрузчика loaderManager и передать контекст приложения
        //loaderManager отвечает за все загрузки, которые происходят в приложении
        loaderManager = LoaderManager.getInstance(this);
        //передаем viewModel контекст и класс viewModel, который мы хотим использовать
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        //выровнять по центру текст ActionBar
        centerTitle();
        textViewPopularity = findViewById(R.id.textViewPopularity);
        textViewTopRated = findViewById(R.id.textViewTopRated);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        //получаем id переключателя
        switchSort = findViewById(R.id.switchSort);
        recyclerViewPosters = findViewById(R.id.recyclerViewPosters);
        //укажем, что хотим сортировать элементы в RecyclerView сеткой
        recyclerViewPosters.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        //создаем адаптер
        movieAdapter = new MovieAdapter();
        //устанавливаем у recyclerView адаптер для массива фильмов
        recyclerViewPosters.setAdapter(movieAdapter);
        //изначально фильмы сортируются по рейтингу
        switchSort.setChecked(true);
        //добавляем нашему свичу слушатель (observer). Слушатель для галочки OnChecked
        switchSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //начинать загрузку с первой страницы
                page = 1;
                setMethodOfSort(isChecked);
            }
        });
        //затем переключаем switch, чтобы сработал метод-слушатель и в наш адаптер попали данные
        switchSort.setChecked(false);

        movieAdapter.setOnPosterClickListener(new MovieAdapter.OnPosterClickListener() {
            //при нажатии на элемент RecyclerView, передать позицию элемента
            //вывести в Toast номер элемента
            @Override
            public void onPosterClick(int position) {
                Toast.makeText(MainActivity.this, "Clicked" + position, Toast.LENGTH_SHORT).show();
            }
        });

        //если долистали до конца страницы, то снова запустить downloadData, и подгрузить следующие 20 фильмов
        movieAdapter.setOnReachEndListener(new MovieAdapter.OnReachEndListener() {
            @Override
            public void onReachEnd() {
                //если подгурзка данных не началась
                if(!isLoading) {
                    downloadData(methodOfSort, page);
                }
            }
        });
        //получаем наши фильмы из БД
        LiveData<List<Movie>> moviesFromLiveData = viewModel.getMovies();
        //следит за состоянием объекта moviesFromLiveData, который содержит данные из БД, и в случае, если данные изменились, то обновляет данные в адаптере
        //который в свою очередь выводит данные в приложение, связывая БД и View элементы XML
        moviesFromLiveData.observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                //если интернет отключен, то взять фильмы из БД
                //если мы только запустили приложение, либо изменили метод сортировки, то значени page становится == 1 мы начинаем подгружать данные из БД
                if(page == 1) {
                    movieAdapter.setMovies(movies);
                }
            }
        });
    }

    //данные методы вызываются при нажатии на текст в приложении
    public void onClickSetPopularity(View view) {
        setMethodOfSort(false);
        switchSort.setChecked(false);
    }

    public void onClickSetTopRated(View view) {
        setMethodOfSort(true);
        switchSort.setChecked(true);

    }

    //метод выставляет сортировку по популярности либо по рейтингу
    private void setMethodOfSort(boolean isTopRated) {
        if(isTopRated) {
            textViewTopRated.setTextColor(getResources().getColor(R.color.purple_200));
            textViewPopularity.setTextColor(getResources().getColor(R.color.white));
            methodOfSort = NetworkUtils.TOP_RATED;
        } else {
            textViewPopularity.setTextColor(getResources().getColor(R.color.purple_200));
            textViewTopRated.setTextColor(getResources().getColor(R.color.white));
            methodOfSort = NetworkUtils.POPULARITY;
        }
        //просто вынесли в отдельный метод
        downloadData(methodOfSort, page);
    }

    //здесь мы будм запускать загрузчик jsonLoader
    private void downloadData(int methodOfSort, int page) {
        //формируем URL
        URL url = NetworkUtils.buildURL(methodOfSort, page, lang);
        Bundle bundle = new Bundle();
        //вставляем данные в Bundle
        bundle.putString("url", url.toString());
        //запускаем загрузчик
        //restartLoader проверит, существует ли загрузчик, если его нет, то restartLoader его создаст, вызвав метод initLoader
        //если же загружчик есть, то restartLoader просто перезапустит его
        //третий параметр, это слушатель событий, который мы реализовали в MainActivity. вот так -> MainActivity implements LoaderManager.LoaderCallbacks, поэтому просто передаем this
        loaderManager.restartLoader(LOADER_ID, bundle, this);
    }

    //возвращает число колонок в RecyclerView, в зависимости от ориентации экрана
    //взять ширину экрана в пикселях / на 185(размер постера w185), если полученное число > 2-х, то использовать полученое значение, если меньше 2-х, то использовать 2
    private int getColumnCount() {
        //получить размер экрана
        DisplayMetrics displayMetrics = new DisplayMetrics();
        //получить характеристики экрана и сохранить их в displayMetrics
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //ширину экрана мы получаем в аппарато-независимых пикселях dp, для того, чтобы пиксели выглядили одинаково хорошо на разных экранах
        //пиксели / density (плотность), float переводим в int
        int width = ( int) (displayMetrics.widthPixels / displayMetrics.density);
        //получить кол-во колонок. ? if (width / 185 > 2) { return width / 185 } , : else { return 2 }
        return width / 185 > 2 ? width / 185 : 2;
    }

    //методы загрузки данных теперь здесь, в загрузчике
    //передаем(уникальный id загрузчика он может быть любым и Bundle)
    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle bundle) {
        //создаем загрузчик
        NetworkUtils.JSONLoader jsonLoader = new NetworkUtils.JSONLoader(this, bundle);
        //переопределяем слушатель, который слушает, не начали-ли подгружаться новые страницы
        jsonLoader.setOnStartLoadingListener(new NetworkUtils.JSONLoader.OnStartLoadingListener() {
            @Override
            public void onStartLoading() {
                //показать прогресс загрузки
                progressBarLoading.setVisibility(View.VISIBLE);
                //здесь просто переключается значение, которое дает знать movieAdapter.onBindViewHolder, что подгрузка началась, после чего так же запускается setOnReachEndListener
                isLoading = true;
            }
        });
        //и возвращаем его
        return jsonLoader;
    }

    //сюда попадают данные, которые мы получаем при завершении работы заргузчика
    @Override
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject jsonObject) {
        //получаем список фильмов из JSON объекта
        ArrayList<Movie> movies = JSONUtils.getMoviesFromJSON(jsonObject);
        //если в movies поступили новые данные, удаляем старые данные из БД
        if(movies != null && !movies.isEmpty()) {
            //но сначала проверяем на какой странице находимся
            if(page == 1) {
                viewModel.deleteAllMovies();
                //почистить адаптер
                movieAdapter.clear();
            }
            //в цикле заполняем БД новыми данными
            for(Movie movie : movies) {
                viewModel.insertMovie(movie);
            }
            //сразу полсе добавления фильмов в БД, добавляем их в адаптер
            movieAdapter.addMovie(movies);
            //как только прогрузилась первая страница, начать грузить следующую
            page++;
        }
        //после окончания загрузки очередной страницы, снова переключаем значени
        isLoading = false;
        //не забыть уничтожить загрузчик, после использования
        //убрать progressBar
        progressBarLoading.setVisibility(View.INVISIBLE);
        loaderManager.destroyLoader(LOADER_ID);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }

    //выровнять текст ActionBar-а по центру
    private void centerTitle() {
        ArrayList<View> textViews = new ArrayList<>();
        getWindow().getDecorView().findViewsWithText(textViews, getTitle(), View.FIND_VIEWS_WITH_TEXT);
        if(textViews.size() > 0) {
            AppCompatTextView appCompatTextView = null;
            if(textViews.size() == 1) {
                appCompatTextView = (AppCompatTextView) textViews.get(0);
            } else {
                for(View v : textViews) {
                    if(v.getParent() instanceof Toolbar) {
                        appCompatTextView = (AppCompatTextView) v;
                        break;
                    }
                }
            }

            if(appCompatTextView != null) {
                ViewGroup.LayoutParams params = appCompatTextView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                appCompatTextView.setLayoutParams(params);
                appCompatTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
        }
    }
}

