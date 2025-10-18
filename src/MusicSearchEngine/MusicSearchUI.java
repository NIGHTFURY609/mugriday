package MusicSearchEngine;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.table.*;
import java.net.URI;

public class MusicSearchUI extends JFrame {
    private static final Color BG_PRIMARY = new Color(18, 18, 18);
    private static final Color BG_SECONDARY = new Color(24, 24, 24);
    private static final Color BG_HOVER = new Color(40, 40, 40);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(179, 179, 179);
    private static final Color ACCENT_GREEN = new Color(30, 215, 96);
    
    private JTextField searchField;
    private JPanel mainContentPanel;
    private JPanel queuePanel;
    private JLabel nowPlayingLabel;
    private DatabaseManager dbManager;
    private TorrentDownloader torrentDownloader;
    private List<Song> currentQueue = new ArrayList<>();
    private Song currentSong;
    
    public MusicSearchUI() {
        dbManager = new DatabaseManager();
        torrentDownloader = new TorrentDownloader(dbManager);
        
        setTitle("Music Player");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_PRIMARY);
        
        createUI();
        loadRecentActivity();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void createUI() {
        // Main container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_PRIMARY);
        
        // Left sidebar
        JPanel sidebar = createSidebar();
        
        // Center content with search
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BG_PRIMARY);
        
        // Top search bar
        JPanel searchPanel = createSearchPanel();
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Main content area
        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBackground(BG_PRIMARY);
        
        JScrollPane scrollPane = new JScrollPane(mainContentPanel);
        scrollPane.setBackground(BG_PRIMARY);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Right queue panel
        queuePanel = createQueuePanel();
        
        mainContainer.add(sidebar, BorderLayout.WEST);
        mainContainer.add(centerPanel, BorderLayout.CENTER);
        mainContainer.add(queuePanel, BorderLayout.EAST);
        
        add(mainContainer, BorderLayout.CENTER);
        
        // Bottom player bar
        JPanel playerBar = createPlayerBar();
        add(playerBar, BorderLayout.SOUTH);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SECONDARY);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel libraryLabel = new JLabel("Your Library");
        libraryLabel.setForeground(TEXT_PRIMARY);
        libraryLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sidebar.add(libraryLabel);
        
        sidebar.add(Box.createVerticalStrut(20));
        
        // Genre buttons
        String[] genres = {"Rock", "Pop", "Hip Hop", "Electronic", "Classical", "Jazz", "Country", "R&B"};
        for (String genre : genres) {
            JButton genreBtn = createSidebarButton(genre);
            genreBtn.addActionListener(e -> loadGenre(genre));
            sidebar.add(genreBtn);
            sidebar.add(Box.createVerticalStrut(5));
        }
        
        sidebar.add(Box.createVerticalGlue());
        
        return sidebar;
    }
    
    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(TEXT_SECONDARY);
        btn.setBackground(BG_SECONDARY);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(TEXT_PRIMARY);
            }
            public void mouseExited(MouseEvent e) {
                btn.setForeground(TEXT_SECONDARY);
            }
        });
        
        return btn;
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        searchField = new JTextField("What do you want to play?");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setForeground(TEXT_SECONDARY);
        searchField.setBackground(new Color(33, 33, 33));
        searchField.setCaretColor(TEXT_PRIMARY);
        searchField.setBorder(new EmptyBorder(12, 40, 12, 15));
        
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("What do you want to play?")) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_PRIMARY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("What do you want to play?");
                    searchField.setForeground(TEXT_SECONDARY);
                }
            }
        });
        
        searchField.addActionListener(e -> performSearch());
        
        panel.add(searchField, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createQueuePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_SECONDARY);
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBorder(new EmptyBorder(20, 15, 20, 15));
        
        JLabel queueLabel = new JLabel("Queue");
        queueLabel.setForeground(TEXT_PRIMARY);
        queueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        queueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(queueLabel);
        
        panel.add(Box.createVerticalStrut(20));
        
        JLabel nowPlayingHeader = new JLabel("Now Playing");
        nowPlayingHeader.setForeground(TEXT_SECONDARY);
        nowPlayingHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nowPlayingHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(nowPlayingHeader);
        
        panel.add(Box.createVerticalStrut(10));
        
        return panel;
    }
    
    private JPanel createPlayerBar() {
        JPanel playerBar = new JPanel(new BorderLayout());
        playerBar.setBackground(BG_SECONDARY);
        playerBar.setPreferredSize(new Dimension(0, 90));
        playerBar.setBorder(new MatteBorder(1, 0, 0, 0, BG_HOVER));
        
        // Left - Now playing info
        JPanel nowPlayingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nowPlayingPanel.setBackground(BG_SECONDARY);
        nowPlayingPanel.setBorder(new EmptyBorder(0, 20, 0, 0));
        
        nowPlayingLabel = new JLabel("No song playing");
        nowPlayingLabel.setForeground(TEXT_PRIMARY);
        nowPlayingLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nowPlayingPanel.add(nowPlayingLabel);
        
        // Center - Controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        controlsPanel.setBackground(BG_SECONDARY);
        
        JButton prevBtn = createControlButton("⏮");
        JButton playBtn = createControlButton("▶");
        JButton nextBtn = createControlButton("⏭");
        
        controlsPanel.add(prevBtn);
        controlsPanel.add(playBtn);
        controlsPanel.add(nextBtn);
        
        playerBar.add(nowPlayingPanel, BorderLayout.WEST);
        playerBar.add(controlsPanel, BorderLayout.CENTER);
        
        return playerBar;
    }
    
    private JButton createControlButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_HOVER);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(40, 40));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ACCENT_GREEN);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(BG_HOVER);
            }
        });
        
        return btn;
    }
    
    private void loadRecentActivity() {
        mainContentPanel.removeAll();
        
        // Throwback section
        addSection("Throwback", "Playlists full of favourites, still going strong");
        
        // Based on recent listening
        addSection("Based on your recent listening", "Inspired by your recent activity");
        
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    private void addSection(String title, String subtitle) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBackground(BG_PRIMARY);
        sectionPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        sectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        sectionPanel.add(titleLabel);
        sectionPanel.add(Box.createVerticalStrut(5));
        sectionPanel.add(subtitleLabel);
        sectionPanel.add(Box.createVerticalStrut(20));
        
        // Placeholder cards
        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        cardsPanel.setBackground(BG_PRIMARY);
        cardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        for (int i = 0; i < 4; i++) {
            cardsPanel.add(createPlaylistCard());
        }
        
        sectionPanel.add(cardsPanel);
        mainContentPanel.add(sectionPanel);
    }
    
    private JPanel createPlaylistCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_HOVER);
        card.setPreferredSize(new Dimension(180, 240));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel coverLabel = new JLabel("♫");
        coverLabel.setForeground(ACCENT_GREEN);
        coverLabel.setFont(new Font("Segoe UI", Font.BOLD, 60));
        coverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(coverLabel);
        card.add(Box.createVerticalGlue());
        
        return card;
    }
    
    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty() || query.equals("What do you want to play?")) {
            return;
        }
        
        mainContentPanel.removeAll();
        
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(BG_PRIMARY);
        resultsPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel resultsLabel = new JLabel("Search Results for: " + query);
        resultsLabel.setForeground(TEXT_PRIMARY);
        resultsLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        resultsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsPanel.add(resultsLabel);
        resultsPanel.add(Box.createVerticalStrut(20));
        
        // Search local database
        List<Song> localSongs = dbManager.searchSongs(query);
        
        if (!localSongs.isEmpty()) {
            JLabel localLabel = new JLabel("Local Library");
            localLabel.setForeground(ACCENT_GREEN);
            localLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            localLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            resultsPanel.add(localLabel);
            resultsPanel.add(Box.createVerticalStrut(10));
            
            for (Song song : localSongs) {
                resultsPanel.add(createSongRow(song, true));
            }
            resultsPanel.add(Box.createVerticalStrut(20));
        }
        
        // Show external recommendations
        JLabel externalLabel = new JLabel("Not in Library? Try these:");
        externalLabel.setForeground(TEXT_PRIMARY);
        externalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        externalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsPanel.add(externalLabel);
        resultsPanel.add(Box.createVerticalStrut(10));
        
        // Torrent search results
        JLabel torrentLabel = new JLabel("Download via Torrent");
        torrentLabel.setForeground(TEXT_SECONDARY);
        torrentLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        torrentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsPanel.add(torrentLabel);
        resultsPanel.add(Box.createVerticalStrut(5));
        
        List<TorrentDownloader.TorrentResult> torrents = torrentDownloader.searchTorrents(query + " mp3");
        for (int i = 0; i < Math.min(3, torrents.size()); i++) {
            resultsPanel.add(createTorrentRow(torrents.get(i)));
        }
        
        resultsPanel.add(Box.createVerticalStrut(20));
        
        // External links
        JLabel streamLabel = new JLabel("Stream Online");
        streamLabel.setForeground(TEXT_SECONDARY);
        streamLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        streamLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsPanel.add(streamLabel);
        resultsPanel.add(Box.createVerticalStrut(5));
        
        resultsPanel.add(createExternalLinkRow("YouTube", query, "https://www.youtube.com/results?search_query="));
        resultsPanel.add(createExternalLinkRow("Spotify", query, "https://open.spotify.com/search/"));
        
        mainContentPanel.add(resultsPanel);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    private JPanel createSongRow(Song song, boolean isLocal) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setBackground(BG_PRIMARY);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setBorder(new EmptyBorder(8, 10, 8, 10));
        
        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                row.setBackground(BG_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                row.setBackground(BG_PRIMARY);
            }
        });
        
        // Song info
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(BG_PRIMARY);
        
        JLabel titleLabel = new JLabel(song.getTitle());
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel artistLabel = new JLabel(song.getArtist());
        artistLabel.setForeground(TEXT_SECONDARY);
        artistLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        infoPanel.add(titleLabel);
        infoPanel.add(artistLabel);
        
        // Actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setBackground(BG_PRIMARY);
        
        JButton playBtn = new JButton("▶");
        styleActionButton(playBtn);
        playBtn.addActionListener(e -> playSong(song));
        
        JButton queueBtn = new JButton("+");
        styleActionButton(queueBtn);
        queueBtn.addActionListener(e -> addToQueue(song));
        
        actionsPanel.add(playBtn);
        actionsPanel.add(queueBtn);
        
        row.add(infoPanel, BorderLayout.CENTER);
        row.add(actionsPanel, BorderLayout.EAST);
        
        return row;
    }
    
    private JPanel createTorrentRow(TorrentDownloader.TorrentResult torrent) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setBackground(BG_PRIMARY);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setBorder(new EmptyBorder(8, 10, 8, 10));
        
        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                row.setBackground(BG_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                row.setBackground(BG_PRIMARY);
            }
        });
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(BG_PRIMARY);
        
        JLabel nameLabel = new JLabel(torrent.getName());
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel detailsLabel = new JLabel(String.format("Size: %s | Seeders: %d", torrent.getSize(), torrent.getSeeders()));
        detailsLabel.setForeground(TEXT_SECONDARY);
        detailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        infoPanel.add(nameLabel);
        infoPanel.add(detailsLabel);
        
        JButton downloadBtn = new JButton("⬇ Download");
        styleActionButton(downloadBtn);
        downloadBtn.addActionListener(e -> {
            torrentDownloader.downloadTorrent(torrent);
            JOptionPane.showMessageDialog(this, "Opening magnet link in torrent client...");
        });
        
        row.add(infoPanel, BorderLayout.CENTER);
        row.add(downloadBtn, BorderLayout.EAST);
        
        return row;
    }
    
    private JPanel createExternalLinkRow(String platform, String query, String baseUrl) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setBackground(BG_PRIMARY);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                row.setBackground(BG_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                row.setBackground(BG_PRIMARY);
            }
        });
        
        JLabel platformLabel = new JLabel("Search on " + platform);
        platformLabel.setForeground(TEXT_PRIMARY);
        platformLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JButton openBtn = new JButton("Open →");
        styleActionButton(openBtn);
        openBtn.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI(baseUrl + query.replace(" ", "+")));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        row.add(platformLabel, BorderLayout.CENTER);
        row.add(openBtn, BorderLayout.EAST);
        
        return row;
    }
    
    private void styleActionButton(JButton btn) {
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(ACCENT_GREEN);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(80, 35));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(40, 235, 116));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(ACCENT_GREEN);
            }
        });
    }
    
    private void loadGenre(String genre) {
        mainContentPanel.removeAll();
        
        JPanel genrePanel = new JPanel();
        genrePanel.setLayout(new BoxLayout(genrePanel, BoxLayout.Y_AXIS));
        genrePanel.setBackground(BG_PRIMARY);
        genrePanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel genreLabel = new JLabel(genre + " Music");
        genreLabel.setForeground(TEXT_PRIMARY);
        genreLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        genreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        genrePanel.add(genreLabel);
        genrePanel.add(Box.createVerticalStrut(20));
        
        List<Song> songs = dbManager.getSongsByGenre(genre);
        
        if (songs.isEmpty()) {
            JLabel emptyLabel = new JLabel("No songs in this genre yet");
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            genrePanel.add(emptyLabel);
        } else {
            for (Song song : songs) {
                genrePanel.add(createSongRow(song, true));
            }
        }
        
        mainContentPanel.add(genrePanel);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    private void playSong(Song song) {
        currentSong = song;
        nowPlayingLabel.setText(song.getTitle() + " - " + song.getArtist());
    }
    
    private void addToQueue(Song song) {
        currentQueue.add(song);
        updateQueueDisplay();
    }
    
    private void updateQueueDisplay() {
        // Remove old queue items (keep header)
        Component[] components = queuePanel.getComponents();
        for (int i = 3; i < components.length; i++) {
            queuePanel.remove(components[i]);
        }
        
        for (Song song : currentQueue) {
            JLabel queueItem = new JLabel(song.getTitle());
            queueItem.setForeground(TEXT_SECONDARY);
            queueItem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            queueItem.setAlignmentX(Component.LEFT_ALIGNMENT);
            queuePanel.add(queueItem);
            queuePanel.add(Box.createVerticalStrut(10));
        }
        
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