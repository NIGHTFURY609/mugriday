package MusicSearchEngine;

import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/music_player";
    private static final String USER = "root";
    private static final String PASS = "";
    
    private Connection conn;
    
    public DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            initializeTables();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to connect to database. Make sure XAMPP MySQL is running.");
        }
    }
    
    private void initializeTables() {
        try {
            Statement stmt = conn.createStatement();
            
            // Create separate tables for each genre
            String[] genres = {"rock", "pop", "hiphop", "electronic", "classical", "jazz", "country", "rnb"};
            
            for (String genre : genres) {
                String tableName = "songs_" + genre;
                String createTable = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "title VARCHAR(255) NOT NULL," +
                    "artist VARCHAR(255) NOT NULL," +
                    "album VARCHAR(255)," +
                    "duration INT," +
                    "file_path VARCHAR(500)," +
                    "year INT," +
                    "date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "INDEX idx_title (title)," +
                    "INDEX idx_artist (artist)" +
                    ")";
                stmt.execute(createTable);
            }
            
            // Downloads tracking table
            String downloadsTable = "CREATE TABLE IF NOT EXISTS downloads (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "song_name VARCHAR(255) NOT NULL," +
                "torrent_name VARCHAR(500)," +
                "magnet_link TEXT," +
                "status VARCHAR(50) DEFAULT 'pending'," +
                "size VARCHAR(50)," +
                "seeders INT," +
                "date_started TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "date_completed TIMESTAMP NULL," +
                "INDEX idx_status (status)" +
                ")";
            stmt.execute(downloadsTable);
            
            // User preferences table
            String preferencesTable = "CREATE TABLE IF NOT EXISTS user_preferences (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "preference_key VARCHAR(100) UNIQUE," +
                "preference_value TEXT," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
            stmt.execute(preferencesTable);
            
            // Recently played table
            String recentTable = "CREATE TABLE IF NOT EXISTS recently_played (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "song_id INT," +
                "genre VARCHAR(50)," +
                "played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "INDEX idx_played_at (played_at)" +
                ")";
            stmt.execute(recentTable);
            
            stmt.close();
            System.out.println("Database tables initialized successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Song> searchSongs(String query) {
        List<Song> results = new ArrayList<>();
        String[] genres = {"rock", "pop", "hiphop", "electronic", "classical", "jazz", "country", "rnb"};
        
        try {
            for (String genre : genres) {
                String sql = "SELECT * FROM songs_" + genre + 
                    " WHERE title LIKE ? OR artist LIKE ? OR album LIKE ? LIMIT 10";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                String searchPattern = "%" + query + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                pstmt.setString(3, searchPattern);
                
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Song song = new Song(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("album"),
                        rs.getInt("duration"),
                        rs.getString("file_path"),
                        genre,
                        rs.getInt("year")
                    );
                    results.add(song);
                }
                rs.close();
                pstmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return results;
    }
    
    public List<Song> getSongsByGenre(String genre) {
    List<Song> songs = new ArrayList<>();
    
    // Map display names to table names
    String tableName = genreToTableName(genre);
    
    try {
        String sql = "SELECT * FROM " + tableName + " ORDER BY date_added DESC LIMIT 50";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while (rs.next()) {
            Song song = new Song(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("artist"),
                rs.getString("album"),
                rs.getInt("duration"),
                rs.getString("file_path"),
                genre,
                rs.getInt("year")
            );
            songs.add(song);
        }
        
        rs.close();
        stmt.close();
    } catch (SQLException e) {
        e.printStackTrace();
        System.err.println("Error fetching songs for genre: " + genre);
    }
    
    return songs;
}

// Add this helper method to convert genre names to table names
private String genreToTableName(String genre) {
    switch (genre.toLowerCase()) {
        case "rock":
            return "songs_rock";
        case "pop":
            return "songs_pop";
        case "hip hop":
        case "hiphop":
            return "songs_hiphop";
        case "electronic":
            return "songs_electronic";
        case "classical":
            return "songs_classical";
        case "jazz":
            return "songs_jazz";
        case "country":
            return "songs_country";
        case "r&b":
        case "rnb":
            return "songs_rnb";
        default:
            return "songs_" + genre.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
    
public boolean addSong(Song song) {
    String tableName = genreToTableName(song.getGenre());
    
    try {
        String sql = "INSERT INTO " + tableName + 
            " (title, artist, album, duration, file_path, year) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, song.getTitle());
        pstmt.setString(2, song.getArtist());
        pstmt.setString(3, song.getAlbum());
        pstmt.setInt(4, song.getDuration());
        pstmt.setString(5, song.getFilePath());
        pstmt.setInt(6, song.getYear());
        
        int rowsAffected = pstmt.executeUpdate();
        pstmt.close();
        
        return rowsAffected > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
    
    public boolean deleteSong(int songId, String genre) {
    String tableName = genreToTableName(genre);
    
    try {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, songId);
        
        int rowsAffected = pstmt.executeUpdate();
        pstmt.close();
        
        return rowsAffected > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
    
    public int addDownload(String songName, String torrentName, String magnetLink, String size, int seeders) {
        try {
            String sql = "INSERT INTO downloads (song_name, torrent_name, magnet_link, size, seeders, status) " +
                "VALUES (?, ?, ?, ?, ?, 'downloading')";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, songName);
            pstmt.setString(2, torrentName);
            pstmt.setString(3, magnetLink);
            pstmt.setString(4, size);
            pstmt.setInt(5, seeders);
            
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            int downloadId = -1;
            if (rs.next()) {
                downloadId = rs.getInt(1);
            }
            
            rs.close();
            pstmt.close();
            return downloadId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public boolean updateDownloadStatus(int downloadId, String status) {
        try {
            String sql = "UPDATE downloads SET status = ?";
            if (status.equals("completed")) {
                sql += ", date_completed = CURRENT_TIMESTAMP";
            }
            sql += " WHERE id = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, downloadId);
            
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<DownloadItem> getActiveDownloads() {
        List<DownloadItem> downloads = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM downloads WHERE status IN ('pending', 'downloading') ORDER BY date_started DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                DownloadItem item = new DownloadItem(
                    rs.getInt("id"),
                    rs.getString("song_name"),
                    rs.getString("torrent_name"),
                    rs.getString("status"),
                    rs.getString("size"),
                    rs.getInt("seeders")
                );
                downloads.add(item);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return downloads;
    }
    
    public void addToRecentlyPlayed(int songId, String genre) {
        try {
            String sql = "INSERT INTO recently_played (song_id, genre) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, songId);
            pstmt.setString(2, genre);
            
            pstmt.executeUpdate();
            pstmt.close();
            
            // Keep only last 50 entries
            String cleanupSql = "DELETE FROM recently_played WHERE id NOT IN " +
                "(SELECT id FROM (SELECT id FROM recently_played ORDER BY played_at DESC LIMIT 50) AS temp)";
            Statement stmt = conn.createStatement();
            stmt.execute(cleanupSql);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}