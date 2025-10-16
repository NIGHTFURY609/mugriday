package MusicSearchEngine;

import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/musicband_db";
    private static final String DB_USER = "root"; // change as needed
    private static final String DB_PASS = ""; // change as needed

    private Connection conn;
    private final Map<String, String> genreTableMap = new HashMap<>();

    public DatabaseManager() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            loadGenresFromDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Loads genres and their table names dynamically from DB */
    // CORRECTED VERSION
/** Loads genres and their table names dynamically from the 'genres' table */
private void loadGenresFromDB() throws SQLException {
    genreTableMap.clear();
    // This query selects the genre name and its corresponding table name from the main genres table.
    String sql = "SELECT genre_name, table_name FROM genres";

    try (Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        while (rs.next()) {
            // Populate the map with the genre as the key and the table name as the value
            genreTableMap.put(rs.getString("genre_name"), rs.getString("table_name"));
        }
    }
}

    public List<String> getAllGenres() {
        return new ArrayList<>(genreTableMap.keySet());
    }

    /** Search for songs by title and/or artist within a specific genre */
    public List<Song> searchSongs(String title, String artist, String genre) {
        List<Song> results = new ArrayList<>();
        try {
            String table = genreTableMap.get(genre);
            if (table == null) return results;

            // In the searchSongs method...
            // CORRECTED VERSION
            StringBuilder sql = new StringBuilder("SELECT id, title, artist, album, subgenre, release_year, duration, rating, is_downloaded, file_path, play_count FROM ");
            sql.append(table).append(" WHERE 1=1 ");
            List<Object> params = new ArrayList<>();

            if (title != null && !title.isEmpty()) {
                sql.append("AND title LIKE ? ");
                params.add("%" + title + "%");
            }
            if (artist != null && !artist.isEmpty()) {
                sql.append("AND artist LIKE ? ");
                params.add("%" + artist + "%");
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++)
                    ps.setObject(i + 1, params.get(i));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapSong(rs, genre));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /** Add a new song dynamically into the correct genre table */
    public boolean addSong(Song song) {
        try {
            String table = genreTableMap.get(song.getGenre());
            if (table == null) return false;

            String sql = "INSERT INTO " + table +
                    " (title, artist, album, subgenre, release_year, duration, file_path, is_downloaded) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, song.getTitle());
                ps.setString(2, song.getArtist());
                ps.setString(3, song.getAlbum());
                ps.setString(4, song.getSubgenre());
                ps.setInt(5, song.getReleaseYear());
                ps.setInt(6, song.getDuration());
                ps.setString(7, song.getFilePath());
                ps.setBoolean(8, song.isDownloaded());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Add to download_queue (torrent download system hook) */
    public boolean addToDownloadQueue(String title, String artist, String genre, String torrentSource) {
        try {
            String sql = "INSERT INTO download_queue (title, artist, genre, target_table, torrent_source, status) " +
                         "VALUES (?, ?, ?, ?, ?, 'pending')";
            String table = genreTableMap.get(genre);
            if (table == null) return false;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, title);
                ps.setString(2, artist);
                ps.setString(3, genre);
                ps.setString(4, table);
                ps.setString(5, torrentSource);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 3-arg overload for backward compatibility */
    public boolean addToDownloadQueue(String title, String artist, String genre) {
        return addToDownloadQueue(title, artist, genre, "");
    }

    public boolean updateDownloadStatus(String genre, int songId, boolean isDownloaded, String filePath) {
        try {
            String table = genreTableMap.get(genre);
            if (table == null) return false;

            String sql = "UPDATE " + table + " SET is_downloaded = ?, file_path = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, isDownloaded);
                ps.setString(2, filePath);
                ps.setInt(3, songId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateDownloadQueueStatus(int downloadId, String status) {
        try {
            String sql = "UPDATE download_queue SET status = ?, completed_date = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setInt(2, downloadId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<DownloadItem> getPendingDownloads() {
    List<DownloadItem> list = new ArrayList<>();
    String sql = "SELECT id, title, artist, genre, status, torrent_source FROM download_queue WHERE status = 'pending'";
    try (Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        while (rs.next()) {
            DownloadItem item = new DownloadItem(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("artist"),
                    rs.getString("genre"),
                    rs.getString("status"),
                    rs.getString("torrent_source")
            );
            list.add(item);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}


    public boolean deleteSong(String genre, int songId) {
        try {
            String table = genreTableMap.get(genre);
            if (table == null) return false;

            String sql = "DELETE FROM " + table + " WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, songId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Fetch top rated songs across all tables using the all_songs view */
    public List<Song> getTopRatedSongs(int limit) {
        List<Song> list = new ArrayList<>();
        String sql = "SELECT * FROM all_songs ORDER BY rating DESC LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSong(rs, rs.getString("genre")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

private Song mapSong(ResultSet rs, String genre) throws SQLException {
    Song s = new Song();
    s.setId(rs.getInt("id"));
    s.setTitle(rs.getString("title"));
    s.setArtist(rs.getString("artist"));
    s.setAlbum(rs.getString("album"));
    s.setGenre(genre);
    s.setSubgenre(rs.getString("subgenre"));
    s.setReleaseYear(rs.getInt("release_year"));
    s.setDuration(rs.getInt("duration"));
    s.setFilePath(rs.getString("file_path"));
    s.setDownloaded(rs.getBoolean("is_downloaded"));
    s.setPlayCount(rs.getInt("play_count"));
    s.setRating(rs.getDouble("rating"));
    return s;
}


    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed())
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
