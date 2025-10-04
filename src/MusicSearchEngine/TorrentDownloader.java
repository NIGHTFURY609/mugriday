package MusicSearchEngine;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TorrentDownloader class handles searching and downloading music from torrent sources.
 * This implementation provides a framework for torrent integration.
 * 
 * Note: Actual torrent downloading requires external libraries like:
 * - libtorrent4j
 * - ttorrent
 * - Or integration with torrent clients via their APIs
 */
public class TorrentDownloader {
    
    private static final String[] TORRENT_SEARCH_SITES = {
        "https://apibay.org/q.php?q=", // The Pirate Bay API
        // Add more torrent search APIs as needed
    };
    
    private DatabaseManager dbManager;
    private String downloadDirectory;

    public TorrentDownloader(DatabaseManager dbManager, String downloadDirectory) {
        this.dbManager = dbManager;
        this.downloadDirectory = downloadDirectory;
        
        // Create download directory if it doesn't exist
        File dir = new File(downloadDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Search for torrents matching the given song and artist
     */
    public List<TorrentResult> searchTorrents(String title, String artist) {
        List<TorrentResult> results = new ArrayList<>();
        
        String searchQuery = title + " " + artist + " mp3";
        
        try {
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());
            
            // Search using The Pirate Bay API (apibay.org)
            String apiUrl = TORRENT_SEARCH_SITES[0] + encodedQuery;
            
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                
                // Parse JSON response (simple parsing)
                results = parseTorrentResults(response.toString(), title, artist);
            }
            
        } catch (Exception e) {
            System.err.println("Error searching torrents: " + e.getMessage());
        }
        
        return results;
    }

    /**
     * Parse torrent search results from JSON
     */
    private List<TorrentResult> parseTorrentResults(String jsonResponse, String title, String artist) {
        List<TorrentResult> results = new ArrayList<>();
        
        try {
            // Simple JSON parsing (in production, use a proper JSON library like Gson or Jackson)
            Pattern namePattern = Pattern.compile("\"name\":\"([^\"]+)\"");
            Pattern sizePattern = Pattern.compile("\"size\":\"?(\\d+)\"?");
            Pattern seedersPattern = Pattern.compile("\"seeders\":\"?(\\d+)\"?");
            Pattern magnetPattern = Pattern.compile("\"info_hash\":\"([^\"]+)\"");
            
            Matcher nameMatcher = namePattern.matcher(jsonResponse);
            Matcher sizeMatcher = sizePattern.matcher(jsonResponse);
            Matcher seedersMatcher = seedersPattern.matcher(jsonResponse);
            Matcher magnetMatcher = magnetPattern.matcher(jsonResponse);
            
            while (nameMatcher.find() && sizeMatcher.find() && seedersMatcher.find() && magnetMatcher.find()) {
                String name = nameMatcher.group(1);
                long size = Long.parseLong(sizeMatcher.group(1));
                int seeders = Integer.parseInt(seedersMatcher.group(1));
                String infoHash = magnetMatcher.group(1);
                
                // Create magnet link
                String magnetLink = "magnet:?xt=urn:btih:" + infoHash + 
                                  "&dn=" + URLEncoder.encode(name, StandardCharsets.UTF_8.toString());
                
                TorrentResult result = new TorrentResult(name, magnetLink, size, seeders);
                results.add(result);
                
                // Limit to top 5 results
                if (results.size() >= 5) break;
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing torrent results: " + e.getMessage());
        }
        
        return results;
    }

    /**
     * Download a torrent using the system's default torrent client
     */
    public boolean downloadWithTorrentClient(TorrentResult torrent, String title, String artist) {
        try {
            // Add to download queue in database
            dbManager.addToDownloadQueue(title, artist, torrent.getMagnetLink());
            
            // Try to open magnet link with default application
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    desktop.browse(new java.net.URI(torrent.getMagnetLink()));
                    return true;
                }
            }
            
            // Fallback: Copy magnet link to clipboard
            java.awt.datatransfer.StringSelection stringSelection = 
                new java.awt.datatransfer.StringSelection(torrent.getMagnetLink());
            java.awt.datatransfer.Clipboard clipboard = 
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            
            JOptionPane.showMessageDialog(null,
                "Magnet link copied to clipboard!\nPaste it in your torrent client.",
                "Download Started", JOptionPane.INFORMATION_MESSAGE);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error starting download: " + e.getMessage());
            return false;
        }
    }

    /**
     * Download torrent programmatically (requires external library)
     */
    public void downloadTorrentProgrammatically(TorrentResult torrent, String title, String artist) {
        // This is a placeholder for programmatic torrent downloading
        // In production, you would use a library like libtorrent4j:
        
        /*
        Example with libtorrent4j:
        
        SessionManager sessionManager = new SessionManager();
        sessionManager.start();
        
        byte[] data = sessionManager.fetchMagnet(torrent.getMagnetLink(), 30);
        
        if (data != null) {
            File torrentFile = new File(downloadDirectory, title + ".torrent");
            Files.write(torrentFile.toPath(), data);
            
            TorrentInfo ti = TorrentInfo.bdecode(data);
            sessionManager.download(ti, new File(downloadDirectory));
            
            // Update database when complete
            dbManager.updateDownloadStatus(songId, true, downloadDirectory + "/" + title + ".mp3");
        }
        */
        
        JOptionPane.showMessageDialog(null,
            "Programmatic torrent downloading requires additional libraries.\n" +
            "Current implementation uses system torrent client.\n\n" +
            "To enable this feature, add libtorrent4j or similar library to your project.",
            "Feature Info", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show torrent selection dialog to user
     */
    public TorrentResult showTorrentSelectionDialog(List<TorrentResult> torrents, java.awt.Component parent) {
        if (torrents.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                "No torrents found for this song.",
                "No Results", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        // Create selection dialog
        String[] options = new String[torrents.size()];
        for (int i = 0; i < torrents.size(); i++) {
            TorrentResult t = torrents.get(i);
            options[i] = String.format("%s (%.2f MB, %d seeders)", 
                t.getName(), t.getSize() / (1024.0 * 1024.0), t.getSeeders());
        }

        int selection = JOptionPane.showOptionDialog(parent,
            "Select a torrent to download:",
            "Available Torrents",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (selection >= 0 && selection < torrents.size()) {
            return torrents.get(selection);
        }

        return null;
    }

    /**
     * Inner class to represent a torrent search result
     */
    public static class TorrentResult {
        private String name;
        private String magnetLink;
        private long size;
        private int seeders;

        public TorrentResult(String name, String magnetLink, long size, int seeders) {
            this.name = name;
            this.magnetLink = magnetLink;
            this.size = size;
            this.seeders = seeders;
        }

        public String getName() {
            return name;
        }

        public String getMagnetLink() {
            return magnetLink;
        }

        public long getSize() {
            return size;
        }

        public int getSeeders() {
            return seeders;
        }

        @Override
        public String toString() {
            return String.format("%s (%.2f MB, %d seeders)", 
                name, size / (1024.0 * 1024.0), seeders);
        }
    }
}