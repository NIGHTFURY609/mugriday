package MusicSearchEngine;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MusicSearchUI extends JFrame {

    private final DatabaseManager dbManager;
    private final JTextField titleField;
    private final JTextField artistField;
    private final JTextField genreField;
    private final JTable resultsTable;
    private final DefaultTableModel tableModel;

    public MusicSearchUI() {
        dbManager = new DatabaseManager();

        // --- Basic Window Setup ---
        setTitle("Music Search Engine");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // --- UI Components ---

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(1, 7, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        titleField = new JTextField();
        artistField = new JTextField();
        genreField = new JTextField();
        JButton searchButton = new JButton("Search");

        inputPanel.add(new JLabel("Title:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Artist:"));
        inputPanel.add(artistField);
        inputPanel.add(new JLabel("Genre:"));
        inputPanel.add(genreField);
        inputPanel.add(searchButton);
        
        // Results Table
        String[] columnNames = {"Title", "Artist", "Album", "Genre", "Year"};
        tableModel = new DefaultTableModel(columnNames, 0);
        resultsTable = new JTable(tableModel);
        resultsTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(resultsTable);

        // --- Layout ---
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // --- Action Listener for the Search Button ---
        searchButton.addActionListener(e -> performSearch());
        
        // Initial search to show all songs
        performSearch();
    }

    private void performSearch() {
        String title = titleField.getText();
        String artist = artistField.getText();
        String genre = genreField.getText();

        List<Song> songs = dbManager.searchSongs(title, artist, genre);

        // Clear previous results from the table
        tableModel.setRowCount(0);

        // Populate the table with new results
        for (Song song : songs) {
            Object[] row = {
                song.getTitle(),
                song.getArtist(),
                song.getAlbum(),
                song.getGenre(),
                song.getReleaseYear()
            };
            tableModel.addRow(row);
        }
    }
}