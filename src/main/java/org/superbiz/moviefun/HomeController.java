package org.superbiz.moviefun;

import java.util.Iterator;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

@Controller
public class HomeController {
    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private TransactionOperations albumsTransactionOperations;
    private TransactionOperations moviesTransactionOperations;

    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures, AlbumFixtures albumFixtures, TransactionOperations albumsTransactionOperations, TransactionOperations moviesTransactionOperations) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.albumsTransactionOperations = albumsTransactionOperations;
        this.moviesTransactionOperations = moviesTransactionOperations;
    }

    @GetMapping({"/"})
    public String index() {
        return "index";
    }

    @GetMapping({"/setup"})
    public String setup(Map<String, Object> model) {
        Iterator var2 = this.movieFixtures.load().iterator();

        while(var2.hasNext()) {
            final Movie movie = (Movie)var2.next();
            this.moviesTransactionOperations.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    HomeController.this.moviesBean.addMovie(movie);
                }
            });
        }

        var2 = this.albumFixtures.load().iterator();

        while(var2.hasNext()) {
            final Album album = (Album)var2.next();
            this.albumsTransactionOperations.execute(new TransactionCallbackWithoutResult() {
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    HomeController.this.albumsBean.addAlbum(album);
                }
            });
        }

        model.put("movies", this.moviesBean.getMovies());
        model.put("albums", this.albumsBean.getAlbums());
        return "setup";
    }
}
