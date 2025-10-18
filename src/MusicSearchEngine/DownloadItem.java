package MusicSearchEngine;
public class DownloadItem {
    private int id;
    private String songName;
    private String torrentName;
    private String status;
    private String size;
    private int seeders;
    private int progress; // 0-100
    
    public DownloadItem(int id, String songName, String torrentName, String status, String size, int seeders) {
        this.id = id;
        this.songName = songName;
        this.torrentName = torrentName;
        this.status = status;
        this.size = size;
        this.seeders = seeders;
        this.progress = 0;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getSongName() {
        return songName;
    }
    
    public String getTorrentName() {
        return torrentName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getSize() {
        return size;
    }
    
    public int getSeeders() {
        return seeders;
    }
    
    public int getProgress() {
        return progress;
    }
    
    // Setters
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setProgress(int progress) {
        this.progress = Math.min(100, Math.max(0, progress));
    }
    
    public String getStatusDisplay() {
        switch (status) {
            case "pending":
                return "Pending";
            case "downloading":
                return "Downloading " + progress + "%";
            case "completed":
                return "Completed";
            case "failed":
                return "Failed";
            case "paused":
                return "Paused";
            default:
                return "Unknown";
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s [%s] - %s", songName, size, getStatusDisplay());
    }
}