package MusicSearchEngine;

// File: DatabaseManager.java
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/musicband_db";
    private static final String USER = "root"; // Your MySQL username
    private static final String PASS = ""; // Your MySQL password

    public List<Song> searchSongs(String title, String artist, String genre) {
        List<Song> songs = new ArrayList<>();
        
        // Base query
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM songs WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Dynamically add conditions to the query
        if (title != null && !title.trim().isEmpty()) {
            sqlBuilder.append(" AND title LIKE ?");
            params.add("%" + title + "%");
        }
        if (artist != null && !artist.trim().isEmpty()) {
            sqlBuilder.append(" AND artist LIKE ?");
            params.add("%" + artist + "%");
        }
        if (genre != null && !genre.trim().isEmpty()) {
            sqlBuilder.append(" AND genre LIKE ?");
            params.add("%" + genre + "%");
        }

        sqlBuilder.append(" ORDER BY artist, title");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            // Set the parameters in the PreparedStatement
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Song song = new Song(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("artist"),
                    rs.getString("album"),
                    rs.getString("genre"),
                    rs.getInt("release_year")
                );
                songs.add(song);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // In a real app, you'd show an error dialog to the user
        }

        return songs;
    }
}
