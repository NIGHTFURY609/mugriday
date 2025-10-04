package MusicSearchEngine;

public class DownloadItem {
    private int id;
    private String title;
    private String artist;
    private String genre;
    private String status;
    private String torrentSource;

    public DownloadItem(int id, String title, String artist, String genre, String status, String torrentSource) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.status = status;
        this.torrentSource = torrentSource;
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

    public String getGenre() {
        return genre;
    }

    public String getStatus() {
        return status;
    }

    public String getTorrentSource() {
        return torrentSource;
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

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTorrentSource(String torrentSource) {
        this.torrentSource = torrentSource;
    }

    @Override
    public String toString() {
        return "DownloadItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", genre='" + genre + '\'' +
                ", status='" + status + '\'' +
                ", torrentSource='" + torrentSource + '\'' +
                '}';
    }
}