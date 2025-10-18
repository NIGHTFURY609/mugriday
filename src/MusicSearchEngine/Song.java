package MusicSearchEngine;

public class Song {
    private int id;
    private String title;
    private String artist;
    private String album;
    private int duration; // in seconds
    private String filePath;
    private String genre;
    private int year;
    
    public Song(int id, String title, String artist, String album, int duration, 
                String filePath, String genre, int year) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.filePath = filePath;
        this.genre = genre;
        this.year = year;
    }
    
    // Constructor for new songs (without ID)
    public Song(String title, String artist, String album, int duration, 
                String filePath, String genre, int year) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.filePath = filePath;
        this.genre = genre;
        this.year = year;
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
    
    public int getDuration() {
        return duration;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public int getYear() {
        return year;
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
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    // Utility method to format duration
    public String getFormattedDuration() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s (%s) [%s]", title, artist, album, getFormattedDuration());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Song song = (Song) obj;
        return id == song.id && 
               title.equals(song.title) && 
               artist.equals(song.artist);
    }
    
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        return result;
    }
}