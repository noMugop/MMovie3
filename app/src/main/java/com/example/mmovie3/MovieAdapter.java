package com.example.mmovie3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mmovie3.data.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

//любые данные из класса могут быть переданны в xml, лишь через Adapter (переходник)
//1.1. onCreateViewHolder сохраняет ссылку на RecyclerView в переменную view с типом View (все потому, что у RecyclerView тип View).
//1.2. Внутри onCreateViewHolder в методе viewGroup.getContext() мы получаем ссылку на layout нашего XML док-та, например на ConstraintLayout
//2. MovieViewHolder берет view и через него получает ссылки на элементы RecyclerView
//3. в конструкторе присваиваем локальному массиву данные, полученные из вне
//4. onBindViewHolder создает внутри себя объект MovieViewHolder и привязывает полученные из вне данные к элементам RecyclerView
//5. Таким образом мы загружаем и сохраняем данные лишь единажды, как только в массиве будут изменятся данные, мы будем автоматически их обновлять
//6. Adapter это в свою очередь расширение для RecyclerView, которое помогает сделать всё это вызовом одной функции
//7. Библиотека Picasso упрощает загрузку картинки + кэшиирует изображение, т е сохраняет его в памяти и с в следующий раз берет его оттуда (https://github.com/square/picasso)
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    //масисв, который будет принимать данные из вне
    private List<Movie> movies;
    private OnPosterClickListener onPosterClickListener;
    private OnReachEndListener onReachEndListener;

    //выделить место под этот массив
    public MovieAdapter() {
        movies = new ArrayList<>();
    }

    //слушатель для постеров(картинок в приложении), принимающий позицию элемента
    interface OnPosterClickListener {
        void onPosterClick(int position);
    }

    //подгружать данные следующей страницы, только после того, как пользователь долистал страницу до конца
    //слушатель для этих целей
    interface OnReachEndListener {
        void onReachEnd();
    }

    public void setOnPosterClickListener(OnPosterClickListener onPosterClickListener) {
        this.onPosterClickListener = onPosterClickListener;
    }

    public void setOnReachEndListener(OnReachEndListener onReachEndListener) {
        this.onReachEndListener = onReachEndListener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.movie_item, viewGroup, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder movieViewHolder, int i) {
        //взять объект Movie с указанным индексом
        Movie movie = movies.get(i);
        //если мы достигли конца списка, и в слушатель что то попало
        //метод не вызывается, пока мы не получим первые 20 фильмов
        if(movies.size() >= 20 && i > movies.size() - 4 && onReachEndListener != null) {
            //вызвать метод подгружающий данные
            onReachEndListener.onReachEnd();
        }
        //не пришлось в ручную присваивать значения элементам XML
        //getPosterPath - вытащить из объекта путь к изображению, и загрузить его в imageView
        Picasso.get().load(movie.getPosterPath()).into(movieViewHolder.imageViewSmallPoster);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewSmallPoster;

        //контсруктор MovieViewHolder
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            //получили ссылку на imageViewSmallPoster в контрукторе ViewHolder-а
            imageViewSmallPoster = itemView.findViewById(R.id.imageViewSmallPoster);
            //слушатель для щелчка мышки setOnClickListener, метод объекта RecyclerView
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                //при нажатии на постер, передать номер элемента getAdapterPosition в наш интерфейсный метод
                //который мы переопределим в MainActivity
                public void onClick(View view) {
                    if(onPosterClickListener != null) {
                        onPosterClickListener.onPosterClick(getAdapterPosition());
                    }
                }
            });
        }
    }

    //очистить адаптер от фильмов
    public void clear() {
        this.movies.clear();
        notifyDataSetChanged();
    }

    //доступ к нашему массиву
    public void setMovies(List<Movie> movies) {
        this.movies = movies;
        //оповестить адаптер о том, что данные обновились, чтобы тот обновил хранимые данные
        notifyDataSetChanged();
    }

    //добавить один фильм, не заменяя весь массив
    public void addMovie(List<Movie> movies) {
        this.movies.addAll(movies);
        notifyDataSetChanged();
    }

    public List<Movie> getMovies() {
        return movies;
    }
}
