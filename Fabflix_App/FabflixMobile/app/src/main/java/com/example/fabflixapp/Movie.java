package com.example.fabflixapp;

public class Movie
{
    private String id;
    private String title;
    private String year;
    private String director;
    private String genres;
    private String stars;

    Movie()
    {
        id = "";
        title = "";
        year = "";
        director = "";
        genres = "";
        stars = "";
    }

    Movie(String id, String title, String year, String director, String genres, String stars)
    {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = genres;
        this.stars = stars;
    }

    public void setId(String id) { this.id = id; }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setYear(String year)
    {
        this.year = year;
    }

    public void setDirector(String director)
    {
        this.director = director;
    }

    public void setGenres(String genres)
    {
        this.genres = genres;
    }

    public void setStars(String stars)
    {
        this.stars = stars;
    }

    public String getId() { return id; }

    public String getTitle()
    {
        return title;
    }

    public String getYear()
    {
        return year;
    }

    public String getDirector()
    {
        return director;
    }

    public String getGenres()
    {
        return genres;
    }

    public String getStars()
    {
        return stars;
    }
}
