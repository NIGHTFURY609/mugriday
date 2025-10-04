package MusicSearchEngine;

import java.util.*;

/**
 * Minimal DatabaseManager stub to allow compilation and provide simple behavior
 * for the UI and TorrentDownloader. Replace with real JDBC logic as needed.
 */
public class DatabaseManager {

    private final Map<String, String> genreTableMap;

    public DatabaseManager() {
        genreTableMap = new HashMap<>();
        // Basic default genres — real implementation should load from DB
        genreTableMap.put("Unknown", "songs_unknown");
        genreTableMap.put("Pop", "songs_pop");
        genreTableMap.put("Rock", "songs_rock");
        genreTableMap.put("Hip-Hop", "songs_hiphop");
    }

    public List<String> getAllGenres() {
        return new ArrayList<>(genreTableMap.keySet());
    }

    /**
     * Simple stub search: returns no results. Real implementation should query DB.
     */
    public List<Song> searchSongs(String title, String artist, String genre) {
        return Collections.emptyList();
    }

    /**
     * Add a song to the appropriate genre table.
     * Real implementation should INSERT into DB.
     */
    public boolean addSong(Song song) {
        return false;
    }

    /**
     * Add to download queue. This overload accepts the torrent source (magnet/link).
     */
    public boolean addToDownloadQueue(String title, String artist, String genre, String torrentSource) {
        // In a real implementation this would persist a row in download_queue table.
        // For now, return true to indicate queued successfully.
        return true;
    }

    /**
     * Backwards-compatible overload that accepts three args (keeps older callers working).
     */
    public boolean addToDownloadQueue(String title, String artist, String genre) {
        return addToDownloadQueue(title, artist, genre, "");
    }

    public boolean updateDownloadStatus(String genre, int songId, boolean isDownloaded, String filePath) {
        // Stub: pretend update succeeded
        return true;
    }

    public boolean updateDownloadQueueStatus(int downloadId, String status) {
        return true;
    }

    public List<DownloadItem> getPendingDownloads() {
        return Collections.emptyList();
    }

    public boolean deleteSong(String genre, int songId) { return false; }

    public List<Song> getTopRatedSongs(int limit) { return Collections.emptyList(); }

    public List<Song> searchByArtist(String artist) { return Collections.emptyList(); }

    public List<Song> getSongsByAlbum(String album, String genre) { return Collections.emptyList(); }

    public void closeConnection() {
        // No resources in stub
    }

    public Map<String, Integer> getGenreStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        for (String g : genreTableMap.keySet()) stats.put(g, 0);
        return stats;
    }

    public List<Song> getRecentlyPlayed(int limit) { return Collections.emptyList(); }

    public List<Song> getSongsByGenre(String genre) { return Collections.emptyList(); }

    public boolean updatePlayCount(String genre, int songId) { return true; }

    public boolean updateRating(String genre, int songId, double rating) { return true; }
}