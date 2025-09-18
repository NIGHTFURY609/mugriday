package MusicSearchEngine;
// File: Song.java
public class Song {
    private final int id;
    private final String title;
    private final String artist;
    private final String album;
    private final String genre;
    private final int releaseYear;

    public Song(int id, String title, String artist, String album, String genre, int releaseYear) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.releaseYear = releaseYear;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getGenre() { return genre; }
    public int getReleaseYear() { return releaseYear; }
}