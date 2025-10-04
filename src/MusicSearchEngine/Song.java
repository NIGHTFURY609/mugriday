package MusicSearchEngine;

public class Song {
    private int id;
    private String title;
    private String artist;
    private String album;
    private String genre;
    private String subgenre;
    private int releaseYear;
    private int duration; // in seconds
    private String filePath;
    private boolean isDownloaded;
    private int playCount;
    private double rating;

    // Constructor with all fields
    public Song(int id, String title, String artist, String album, String genre, 
                String subgenre, int releaseYear, int duration, String filePath, 
                boolean isDownloaded, int playCount, double rating) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.subgenre = subgenre;
        this.releaseYear = releaseYear;
        this.duration = duration;
        this.filePath = filePath;
        this.isDownloaded = isDownloaded;
        this.playCount = playCount;
        this.rating = rating;
    }

    // Constructor without id (for new songs)
    public Song(String title, String artist, String album, String genre, 
                String subgenre, int releaseYear, int duration, String filePath, 
                boolean isDownloaded) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.subgenre = subgenre;
        this.releaseYear = releaseYear;
        this.duration = duration;
        this.filePath = filePath;
        this.isDownloaded = isDownloaded;
        this.playCount = 0;
        this.rating = 0.0;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getGenre() {
        return genre;
    }

    public String getSubgenre() {
        return subgenre;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public int getDuration() {
        return duration;
    }

    public String getDurationFormatted() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public int getPlayCount() {
        return playCount;
    }

    public double getRating() {
        return rating;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setSubgenre(String subgenre) {
        this.subgenre = subgenre;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", genre='" + genre + '\'' +
                ", subgenre='" + subgenre + '\'' +
                ", releaseYear=" + releaseYear +
                ", duration=" + duration +
                ", filePath='" + filePath + '\'' +
                ", isDownloaded=" + isDownloaded +
                ", playCount=" + playCount +
                ", rating=" + rating +
                '}';
    }
}