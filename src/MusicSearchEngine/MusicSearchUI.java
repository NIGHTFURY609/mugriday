package MusicSearchEngine;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.List;

public class MusicSearchUI extends JFrame {

    private final DatabaseManager dbManager;
    private final TorrentDownloader torrentDownloader;
    private JTextField searchField;
    private JComboBox<String> genreFilter;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JPanel nowPlayingPanel;
    private JLabel nowPlayingTitle;
    private JLabel nowPlayingArtist;
    private JPanel queuePanel;
    private DefaultListModel<String> queueListModel;
    private Song currentSong;

    // Color scheme inspired by Spotify
    private final Color BACKGROUND_COLOR = new Color(18, 18, 18);
    private final Color SIDEBAR_COLOR = new Color(0, 0, 0);
    private final Color CARD_COLOR = new Color(40, 40, 40);
    private final Color HOVER_COLOR = new Color(50, 50, 50);
    private final Color TEXT_COLOR = new Color(255, 255, 255);
    private final Color SUBTEXT_COLOR = new Color(179, 179, 179);
    private final Color ACCENT_COLOR = new Color(30, 215, 96);
    private final Color BUTTON_COLOR = new Color(255, 255, 255);

    public MusicSearchUI() {
        dbManager = new DatabaseManager();
        String downloadDir = System.getProperty("user.home") + "/Music/Downloads";
        torrentDownloader = new TorrentDownloader(dbManager, downloadDir);

        // Window Setup
        setTitle("Music Search Engine");
        setSize(1400, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout());

        // Create main components
        JPanel sidebar = createSidebar();
        JPanel mainContent = createMainContent();
        JPanel playerBar = createPlayerBar();

        add(sidebar, BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);
        add(playerBar, BorderLayout.SOUTH);

        // Initialize queue list
        queueListModel = new DefaultListModel<>();

        setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Logo/Title
        JLabel titleLabel = new JLabel("Your Library");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(titleLabel);
        sidebar.add(Box.createVerticalStrut(30));

        // Menu items
        String[] menuItems = {"Playlists", "Downloaded", "Search"};
        for (String item : menuItems) {
            JButton menuButton = createMenuButton(item);
            sidebar.add(menuButton);
            sidebar.add(Box.createVerticalStrut(10));
        }

        sidebar.add(Box.createVerticalStrut(30));

        // Queue Panel
        JLabel queueLabel = new JLabel("Queue");
        queueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        queueLabel.setForeground(TEXT_COLOR);
        queueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(queueLabel);
        sidebar.add(Box.createVerticalStrut(10));

        queuePanel = new JPanel();
        queuePanel.setBackground(SIDEBAR_COLOR);
        queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.Y_AXIS));
        JScrollPane queueScroll = new JScrollPane(queuePanel);
        queueScroll.setBackground(SIDEBAR_COLOR);
        queueScroll.setBorder(null);
        queueScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        queueScroll.setMaximumSize(new Dimension(240, 400));
        sidebar.add(queueScroll);

        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setForeground(SUBTEXT_COLOR);
        button.setBackground(SIDEBAR_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(240, 40));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setForeground(TEXT_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                button.setForeground(SUBTEXT_COLOR);
            }
        });

        return button;
    }

    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Search bar at top
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(BACKGROUND_COLOR);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        searchField = new JTextField("What do you want to play?");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setForeground(SUBTEXT_COLOR);
        searchField.setBackground(CARD_COLOR);
        searchField.setCaretColor(TEXT_COLOR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_COLOR, 2),
            new EmptyBorder(10, 15, 10, 15)
        ));

        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("What do you want to play?")) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_COLOR);
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("What do you want to play?");
                    searchField.setForeground(SUBTEXT_COLOR);
                }
            }
        });

        searchField.addActionListener(e -> performSearch());

        JButton searchButton = createStyledButton("Search", ACCENT_COLOR);
        searchButton.addActionListener(e -> performSearch());

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Results table
        String[] columnNames = {"Title", "Artist", "Album", "Genre", "Year", "Status", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Actions column is editable
            }
        };
        
        resultsTable = new JTable(tableModel);
        resultsTable.setBackground(BACKGROUND_COLOR);
        resultsTable.setForeground(TEXT_COLOR);
        resultsTable.setSelectionBackground(HOVER_COLOR);
        resultsTable.setSelectionForeground(TEXT_COLOR);
        resultsTable.setGridColor(CARD_COLOR);
        resultsTable.setRowHeight(60);
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resultsTable.setShowGrid(false);
        resultsTable.setIntercellSpacing(new Dimension(0, 5));

        // Custom cell renderer for better styling
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(isSelected ? HOVER_COLOR : BACKGROUND_COLOR);
                c.setForeground(column == 5 ? ACCENT_COLOR : TEXT_COLOR);
                setBorder(new EmptyBorder(5, 10, 5, 10));
                return c;
            }
        };

        for (int i = 0; i < resultsTable.getColumnCount(); i++) {
            resultsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Add button renderer for Actions column
        resultsTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        resultsTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Initial load
        performSearch();

        return mainPanel;
    }

    private JPanel createPlayerBar() {
        JPanel playerBar = new JPanel(new BorderLayout());
        playerBar.setBackground(CARD_COLOR);
        playerBar.setPreferredSize(new Dimension(0, 90));
        playerBar.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Now Playing Info
        nowPlayingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nowPlayingPanel.setBackground(CARD_COLOR);

        JLabel albumArt = new JLabel();
        albumArt.setPreferredSize(new Dimension(60, 60));
        albumArt.setBackground(HOVER_COLOR);
        albumArt.setOpaque(true);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(CARD_COLOR);

        nowPlayingTitle = new JLabel("Dream On");
        nowPlayingTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nowPlayingTitle.setForeground(TEXT_COLOR);

        nowPlayingArtist = new JLabel("Aerosmith");
        nowPlayingArtist.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nowPlayingArtist.setForeground(SUBTEXT_COLOR);

        infoPanel.add(nowPlayingTitle);
        infoPanel.add(nowPlayingArtist);

        nowPlayingPanel.add(albumArt);
        nowPlayingPanel.add(Box.createHorizontalStrut(15));
        nowPlayingPanel.add(infoPanel);

        // Player Controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlsPanel.setBackground(CARD_COLOR);

        JButton prevButton = createControlButton("⏮");
        JButton playButton = createControlButton("▶");
        JButton nextButton = createControlButton("⏭");

        controlsPanel.add(prevButton);
        controlsPanel.add(playButton);
        controlsPanel.add(nextButton);

        // Volume and additional controls
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(CARD_COLOR);

        JButton volumeButton = createControlButton("🔊");
        rightPanel.add(volumeButton);

        playerBar.add(nowPlayingPanel, BorderLayout.WEST);
        playerBar.add(controlsPanel, BorderLayout.CENTER);
        playerBar.add(rightPanel, BorderLayout.EAST);

        return playerBar;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.BLACK);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 40));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private JButton createControlButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        button.setForeground(TEXT_COLOR);
        button.setBackground(CARD_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(50, 50));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setForeground(ACCENT_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                button.setForeground(TEXT_COLOR);
            }
        });

        return button;
    }

    private void performSearch() {
        String searchQuery = searchField.getText();
        if (searchQuery.equals("What do you want to play?")) {
            searchQuery = "";
        }

        List<Song> songs = dbManager.searchSongs(searchQuery, "", "");

        tableModel.setRowCount(0);

        for (Song song : songs) {
            Object[] row = {
                song.getTitle(),
                song.getArtist(),
                song.getAlbum(),
                song.getGenre(),
                song.getReleaseYear(),
                song.isDownloaded() ? "✓ Local" : "Not Downloaded",
                "Actions"
            };
            tableModel.addRow(row);
        }

        // If no local results found, add recommendations
        if (songs.isEmpty() && !searchQuery.isEmpty()) {
            addRecommendationRows(searchQuery);
        }
    }

    private void addRecommendationRows(String query) {
        // Add YouTube recommendation
        Object[] youtubeRow = {
            "Search on YouTube",
            query,
            "External",
            "Recommendation",
            "",
            "🔗 External",
            "Open YouTube"
        };
        tableModel.addRow(youtubeRow);

        // Add Spotify recommendation
        Object[] spotifyRow = {
            "Search on Spotify",
            query,
            "External",
            "Recommendation",
            "",
            "🔗 External",
            "Open Spotify"
        };
        tableModel.addRow(spotifyRow);
    }

    // Button Renderer for Actions column
    class ButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton playButton;
        private JButton downloadButton;
        private JButton linkButton;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setBackground(BACKGROUND_COLOR);

            playButton = new JButton("▶");
            downloadButton = new JButton("⬇");
            linkButton = new JButton("🔗");

            styleActionButton(playButton);
            styleActionButton(downloadButton);
            styleActionButton(linkButton);

            add(playButton);
            add(downloadButton);
        }

        private void styleActionButton(JButton button) {
            button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            button.setForeground(TEXT_COLOR);
            button.setBackground(ACCENT_COLOR);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(45, 35));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            removeAll();
            
            String status = (String) table.getValueAt(row, 5);
            
            if (status.contains("External")) {
                add(linkButton);
            } else if (status.contains("Local")) {
                add(playButton);
            } else {
                add(playButton);
                add(downloadButton);
            }
            
            setBackground(isSelected ? HOVER_COLOR : BACKGROUND_COLOR);
            return this;
        }
    }

    // Button Editor for Actions column
    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton playButton;
        private JButton downloadButton;
        private JButton linkButton;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(BACKGROUND_COLOR);

            playButton = new JButton("▶");
            downloadButton = new JButton("⬇");
            linkButton = new JButton("🔗");

            styleActionButton(playButton);
            styleActionButton(downloadButton);
            styleActionButton(linkButton);

            playButton.addActionListener(e -> {
                playSong(selectedRow);
                fireEditingStopped();
            });

            downloadButton.addActionListener(e -> {
                downloadSong(selectedRow);
                fireEditingStopped();
            });

            linkButton.addActionListener(e -> {
                openExternalLink(selectedRow);
                fireEditingStopped();
            });
        }

        private void styleActionButton(JButton button) {
            button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            button.setForeground(TEXT_COLOR);
            button.setBackground(ACCENT_COLOR);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(45, 35));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            
            selectedRow = row;
            panel.removeAll();
            
            String status = (String) table.getValueAt(row, 5);
            
            if (status.contains("External")) {
                panel.add(linkButton);
            } else if (status.contains("Local")) {
                panel.add(playButton);
            } else {
                panel.add(playButton);
                panel.add(downloadButton);
            }
            
            panel.setBackground(isSelected ? HOVER_COLOR : BACKGROUND_COLOR);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }

        private void playSong(int row) {
            String title = (String) tableModel.getValueAt(row, 0);
            String artist = (String) tableModel.getValueAt(row, 1);
            
            nowPlayingTitle.setText(title);
            nowPlayingArtist.setText(artist);
            
            addToQueue(title, artist);
            
            JOptionPane.showMessageDialog(MusicSearchUI.this,
                "Now playing: " + title + " by " + artist,
                "Playing", JOptionPane.INFORMATION_MESSAGE);
        }

        private void downloadSong(int row) {
            String title = (String) tableModel.getValueAt(row, 0);
            String artist = (String) tableModel.getValueAt(row, 1);
            
            // Show download dialog
            int result = JOptionPane.showConfirmDialog(MusicSearchUI.this,
                "Download \"" + title + "\" by " + artist + " from torrent sources?",
                "Download Song", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                // Show progress dialog
                JDialog progressDialog = new JDialog(MusicSearchUI.this, "Searching Torrents", true);
                JLabel statusLabel = new JLabel("Searching for torrents...");
                statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
                progressDialog.add(statusLabel);
                progressDialog.setSize(300, 100);
                progressDialog.setLocationRelativeTo(MusicSearchUI.this);
                
                // Search torrents in background thread
                SwingWorker<List<TorrentDownloader.TorrentResult>, Void> worker = 
                    new SwingWorker<List<TorrentDownloader.TorrentResult>, Void>() {
                    
                    @Override
                    protected List<TorrentDownloader.TorrentResult> doInBackground() {
                        return torrentDownloader.searchTorrents(title, artist);
                    }
                    
                    @Override
                    protected void done() {
                        progressDialog.dispose();
                        try {
                            List<TorrentDownloader.TorrentResult> torrents = get();
                            
                            if (!torrents.isEmpty()) {
                                TorrentDownloader.TorrentResult selected = 
                                    torrentDownloader.showTorrentSelectionDialog(torrents, MusicSearchUI.this);
                                
                                if (selected != null) {
                                    boolean downloadStarted = torrentDownloader.downloadWithTorrentClient(
                                        selected, title, artist);
                                    
                                    if (downloadStarted) {
                                        tableModel.setValueAt("⬇ Downloading", row, 5);
                                        
                                        JOptionPane.showMessageDialog(MusicSearchUI.this,
                                            "Download started in your torrent client!\n" +
                                            "Once complete, the file will be added to your library.",
                                            "Download Started", JOptionPane.INFORMATION_MESSAGE);
                                    }
                                }
                            } else {
                                JOptionPane.showMessageDialog(MusicSearchUI.this,
                                    "No torrents found for this song.\n" +
                                    "Try searching on YouTube or Spotify instead.",
                                    "No Results", JOptionPane.WARNING_MESSAGE);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(MusicSearchUI.this,
                                "Error during torrent search: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                
                worker.execute();
                
                // Show dialog after starting worker
                Timer timer = new Timer(100, e -> progressDialog.setVisible(true));
                timer.setRepeats(false);
                timer.start();
            }
        }

        private void openExternalLink(int row) {
            String title = (String) tableModel.getValueAt(row, 0);
            String artist = (String) tableModel.getValueAt(row, 1);
            String linkType = (String) tableModel.getValueAt(row, 2);
            
            try {
                String url;
                if (linkType.equals("External") && title.contains("YouTube")) {
                    url = "https://www.youtube.com/results?search_query=" + 
                          artist.replace(" ", "+");
                } else {
                    url = "https://open.spotify.com/search/" + 
                          artist.replace(" ", "%20");
                }
                
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(MusicSearchUI.this,
                    "Could not open link: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addToQueue(String title, String artist) {
        JPanel queueItem = new JPanel();
        queueItem.setLayout(new BoxLayout(queueItem, BoxLayout.Y_AXIS));
        queueItem.setBackground(SIDEBAR_COLOR);
        queueItem.setBorder(new EmptyBorder(5, 0, 5, 0));
        queueItem.setMaximumSize(new Dimension(240, 50));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel artistLabel = new JLabel(artist);
        artistLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        artistLabel.setForeground(SUBTEXT_COLOR);
        artistLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        queueItem.add(titleLabel);
        queueItem.add(artistLabel);

        queuePanel.add(queueItem);
        queuePanel.revalidate();
        queuePanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MusicSearchUI();
        });
    }
}