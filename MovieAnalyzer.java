import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MovieAnalyzer {
  ArrayList<Movie> movies = new ArrayList<>();

  public MovieAnalyzer(String dataset_path) throws IOException {
    Path path = Paths.get(dataset_path);
    List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);
    allLines.remove(0);
    for (String line : allLines) {
      Movie movie = new Movie();
      String[] strArr = line.trim().split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
      movie.Series_Title =
          strArr[1].startsWith("\"") ? strArr[1].substring(1, strArr[1].length() - 1) : strArr[1];
      movie.Released_Year = Integer.parseInt(strArr[2]);
      movie.Certificate = strArr[3];
      movie.Runtime = strArr[4];
      movie.Genre = strArr[5].startsWith("\"") ? strArr[5].replaceAll("\"", "") : strArr[5];
      movie.IMDB_Rating = Float.parseFloat(strArr[6]);
      if (strArr[7].startsWith("\"")) {
        String overView = strArr[7].substring(1, strArr[7].length() - 1);
        movie.Overview = overView;
      } else {
        movie.Overview = strArr[7];
      }
      movie.Meta_score = strArr[8].isEmpty() ? -1 : Integer.parseInt(strArr[8]);
      movie.Director = strArr[9];
      movie.star1 = strArr[10];
      movie.star2 = strArr[11];
      movie.star3 = strArr[12];
      movie.star4 = strArr[13];
      movie.Noofvotes = strArr[14];
      if (strArr[15].isEmpty()) {
        movie.Gross = -1;
      } else {
        String gross;
        gross = strArr[15].substring(1, strArr[15].length() - 1);
        gross = gross.replaceAll(",", "");
        movie.Gross = Integer.parseInt(gross);
      }

      movies.add(movie);
    }

  }

  public Map<Integer, Integer> getMovieCountByYear() {
    Map<Integer, Integer> map = new TreeMap<>(new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return -o1.compareTo(o2);
      }
    });
    for (Movie movie : movies) {
      if (!map.containsKey(movie.Released_Year)) {
        map.put(movie.Released_Year, 1);
      } else {
        int val = map.get(movie.Released_Year);
        map.put(movie.Released_Year, val + 1);
      }
    }

    return map;
  }

  public Map<String, Integer> getMovieCountByGenre() {
    Map<String, Integer> map = new TreeMap<>();
    for (Movie movie : movies) {
      String[] genreList;
      genreList = movie.Genre.split(",");
      for (String genre : genreList) {
        genre = genre.trim();
        if (!map.containsKey(genre)) {
          map.put(genre, 1);
        } else {
          int val = map.get(genre);
          map.put(genre, val + 1);
        }
      }

    }

    List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
    list.sort(new Comparator<Map.Entry<String, Integer>>() {
      @Override
      public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
        return o2.getValue().compareTo(o1.getValue());
      }
    });
    Map<String, Integer> res = list.stream().collect(
        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
            LinkedHashMap::new));
    return res;
  }

  public Map<List<String>, Integer> getCoStarCount() {
    Map<List<String>, Integer> map = new HashMap<>();
    for (Movie movie : movies) {
      List<String> allStars = new ArrayList<>();
      allStars.add(movie.star1);
      allStars.add(movie.star2);
      allStars.add(movie.star3);
      allStars.add(movie.star4);
      for (int i = 0; i < allStars.size(); i++) {
        for (int j = i; j < allStars.size(); j++) {
          if (i != j) {
            List<String> list = new ArrayList<>();
            list.add(allStars.get(i));
            list.add(allStars.get(j));
            list.sort(new Comparator<String>() {
              @Override
              public int compare(String o1, String o2) {
                return o1.compareTo(o2);
              }
            });
            if (!map.containsKey(list)) {
              map.put(list, 1);
            } else {
              int val = map.get(list);
              map.put(list, val + 1);
            }
          }
        }
      }

    }
    return map;
  }

  public List<String> getTopMovies(int top_k, String by) {
    List<Movie> list = null;
    List<String> ans = new ArrayList<>();
    if (by.equals("runtime")) {
      list = movies.stream().sorted(new Comparator<Movie>() {
        @Override
        public int compare(Movie o1, Movie o2) {
          int runtime1 = Integer.parseInt(o1.Runtime.split(" ")[0]);
          int runtime2 = Integer.parseInt(o2.Runtime.split(" ")[0]);
          if (runtime1 == runtime2) {
            return o1.Series_Title.compareTo(o2.Series_Title);
          }
          return runtime2 - runtime1;
        }
      }).collect(Collectors.toList());
    }
    if (by.equals("overview")) {
      list = movies.stream().sorted(new Comparator<Movie>() {
        @Override
        public int compare(Movie o1, Movie o2) {
          if (o1.Overview.length() == o2.Overview.length()) {
            return o1.Series_Title.compareTo(o2.Series_Title);
          }
          return o2.Overview.length() - o1.Overview.length();
        }
      }).collect(Collectors.toList());
    }
    for (int i = 0; i < top_k; i++) {
      ans.add(list.get(i).Series_Title);
    }
    return ans;
  }

  public List<String> getTopStars(int top_k, String by) {
    List<String> ans = new ArrayList<>();
    if (by.equals("rating")) {
      ArrayList<String> starsName = new ArrayList<>();
      for (Movie movie : movies) {
        if (!starsName.contains(movie.star1)) {
          starsName.add(movie.star1);
        }
        if (!starsName.contains(movie.star2)) {
          starsName.add(movie.star2);
        }
        if (!starsName.contains(movie.star3)) {
          starsName.add(movie.star3);
        }
        if (!starsName.contains(movie.star4)) {
          starsName.add(movie.star4);
        }
      }
      ArrayList<Star> starsList = new ArrayList<>();
      for (String starName : starsName) {
        Star star = new Star();
        star.name = starName;
        for (Movie movie : movies) {
          if (movie.star1.equals(starName) || movie.star2.equals(starName) ||
              movie.star3.equals(starName) || movie.star4.equals(starName)) {
            star.rating += movie.IMDB_Rating;
            star.count++;
          }
        }
        star.avgRating = star.rating / star.count;
        starsList.add(star);
      }
      starsList.sort(new Comparator<Star>() {
        @Override
        public int compare(Star o1, Star o2) {
          if (o1.avgRating == o2.avgRating) {
            return o1.name.compareTo(o2.name);
          }
          if (o1.avgRating > o2.avgRating) {
            return -1;
          } else {
            return 1;
          }

        }
      });
      for (int i = 0; i < top_k; i++) {
        ans.add(starsList.get(i).name);
      }
    } else if (by.equals("gross")) {
      ArrayList<String> starsName = new ArrayList<>();
      for (Movie movie : movies) {
        if (!starsName.contains(movie.star1)) {
          starsName.add(movie.star1);
        }
        if (!starsName.contains(movie.star2)) {
          starsName.add(movie.star2);
        }
        if (!starsName.contains(movie.star3)) {
          starsName.add(movie.star3);
        }
        if (!starsName.contains(movie.star4)) {
          starsName.add(movie.star4);
        }
      }
      ArrayList<Star> starsList = new ArrayList<>();
      for (String starName : starsName) {
        Star star = new Star();
        star.name = starName;
        for (Movie movie : movies) {
          if (movie.star1.equals(starName) || movie.star2.equals(starName) ||
              movie.star3.equals(starName) || movie.star4.equals(starName)) {
            if (movie.Gross != -1) {
              star.gross += movie.Gross;
              star.count++;
            }

          }
        }
        if (star.count != 0) {
          star.avgGross = star.gross / star.count;
          starsList.add(star);
        }


      }
      starsList.sort(new Comparator<Star>() {
        @Override
        public int compare(Star o1, Star o2) {
          if (o1.avgGross == o2.avgGross) {
            return o1.name.compareTo(o2.name);
          }
          if (o1.avgGross > o2.avgGross) {
            return -1;
          } else {
            return 1;
          }

        }
      });
      for (int i = 0; i < top_k; i++) {
        ans.add(starsList.get(i).name);
      }
    }
    return ans;
  }

  public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
    List<String> ans = new ArrayList<>();
    for (Movie movie : movies) {
      if (movie.Genre.contains(genre) && movie.IMDB_Rating >= min_rating
          && Integer.parseInt(movie.Runtime.split(" ")[0]) <= max_runtime) {
        ans.add(movie.Series_Title);
      }
    }
    ans.sort(new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return o1.compareTo(o2);
      }
    });
    return ans;
  }
}

class Movie {
  String Series_Title;
  int Released_Year;
  String Certificate;
  String Runtime;
  String Genre;
  float IMDB_Rating;
  String Overview;
  int Meta_score;
  String Director;
  String star1;
  String star2;
  String star3;
  String star4;
  String Noofvotes;
  int Gross;
}

class Star {
  String name;
  double rating;
  double gross;
  double avgRating;
  double avgGross;
  int count;
}