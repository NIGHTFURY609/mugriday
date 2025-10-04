# Music Search Engine - Spotify-Inspired UI

A Java Swing-based music search engine with Spotify-inspired UI that allows you to search, download, and play music from local storage and torrent sources.

## Features

### ✅ Implemented Features

1. **Spotify-Inspired Dark UI**
   - Modern dark theme matching Spotify's design
   - Smooth hover effects and animations
   - Sidebar with library and queue management
   - Player bar at the bottom
   - Clean, intuitive search interface

2. **Music Search**
   - Search local music library by title, artist, or genre
   - Real-time search results display
   - Shows download status for each song

3. **Torrent Integration**
   - Search torrents from The Pirate Bay API
   - Select from multiple torrent results
   - Download via system torrent client
   - Track download queue in database

4. **External Recommendations**
   - When no local results found, shows YouTube and Spotify links
   - One-click opening of external platforms
   - Direct search links to find songs online

5. **Queue Management**
   - Add songs to play queue
   - View queue in sidebar
   - Now playing display

## Database Setup

### 1. Install XAMPP

Download and install XAMPP from [https://www.apachefriends.org/](https://www.apachefriends.org/)

### 2. Start MySQL

- Open XAMPP Control Panel
- Start **Apache** and **MySQL** services

### 3. Create Database

Open phpMyAdmin (http://localhost/phpmyadmin/) and run:

```sql
CREATE DATABASE music_db;
USE music_db;

CREATE TABLE songs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255) NOT NULL,
    album VARCHAR(255),
    genre VARCHAR(100),
    release_year INT,
    file_path VARCHAR(500),
    is_downloaded BOOLEAN DEFAULT TRUE,
    added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE download_queue (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    torrent_source VARCHAR(500),
    requested_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_date TIMESTAMP NULL
);

-- Sample data
INSERT INTO songs (title, artist, album, genre, release_year, file_path, is_downloaded) VALUES
('Dream On', 'Aerosmith', 'Aerosmith', 'Rock', 1973, '/music/aerosmith/dream_on.mp3', TRUE),
('Bohemian Rhapsody', 'Queen', 'A Night at the Opera', 'Rock', 1975, '/music/queen/bohemian.mp3', TRUE),
('Stairway to Heaven', 'Led Zeppelin', 'Led Zeppelin IV', 'Rock', 1971, '/music/led_zeppelin/stairway.mp3', TRUE),
('Hotel California', 'Eagles', 'Hotel California', 'Rock', 1976, '/music/eagles/hotel_california.mp3', TRUE),
('Sweet Child O Mine', 'Guns N Roses', 'Appetite for Destruction', 'Rock', 1987, NULL, FALSE);
```

## Project Structure

```
MusicSearchEngine/
├── MusicSearchUI.java          # Main UI with Spotify-inspired design
├── DatabaseManager.java         # Database operations
├── Song.java                    # Song model class
├── DownloadItem.java           # Download queue model
├── TorrentDownloader.java      # Torrent search and download
└── lib/
    └── mysql-connector-j-8.0.33.jar  # MySQL JDBC driver
```

## Dependencies

### Required JARs

1. **MySQL Connector/J** (JDBC Driver)
   - Download from: [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/)
   - Version: 8.0.33 or later
   - Add to project classpath

2. **Java Development Kit (JDK)**
   - JDK 8 or later

### Optional Libraries (For Advanced Features)

- **libtorrent4j** - For programmatic torrent downloading
- **Gson/Jackson** - For better JSON parsing
- **JLayer** - For MP3 playback

## Running the Application

### Option 1: Command Line

```bash
# Compile
javac -cp ".;lib/mysql-connector-j-8.0.33.jar" MusicSearchEngine/*.java

# Run
java -cp ".;lib/mysql-connector-j-8.0.33.jar" MusicSearchEngine.MusicSearchUI
```

### Option 2: IDE (IntelliJ IDEA / Eclipse)

1. Create new Java project
2. Add all `.java` files to `src/MusicSearchEngine/` folder
3. Add MySQL Connector JAR to project libraries
4. Run `MusicSearchUI.java`

## Usage Guide

### Searching Music

1. **Search Local Library**
   - Type song name, artist, or leave blank for all songs
   - Press Enter or click "Search"
   - Results show with download status

2. **Download from Torrents**
   - Find song without download status
   - Click download button (⬇)
   - Select from available torrents
   - Download opens in your torrent client

3. **External Links**
   - If no local results found, YouTube/Spotify links appear
   - Click link button (🔗) to open in browser

### Playing Music

- Click play button (▶) next to any song
- Song appears in "Now Playing" section
- Automatically added to queue

### Queue Management

- View queue in left sidebar
- Shows upcoming songs
- Displays title and artist

## Configuration

### Database Connection

Edit in `DatabaseManager.java`:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/music_db";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "";  // Change if you set MySQL password
```

### Download Directory

Edit in `MusicSearchUI.java`:

```java
String downloadDir = System.getProperty("user.home") + "/Music/Downloads";
```

### Torrent Search Sites

Add more sources in `TorrentDownloader.java`:

```java
private static final String[] TORRENT_SEARCH_SITES = {
    "https://apibay.org/q.php?q=",
    // Add more APIs here
};
```

## Features Explained

### 1. Torrent Download Feature

**How it works:**
- Searches The Pirate Bay API for music torrents
- Presents results with size and seeders count
- Opens magnet link in system's default torrent client
- Tracks downloads in database queue

**Requirements:**
- Internet connection
- Torrent client installed (qBittorrent, uTorrent, etc.)
- Torrent client configured to handle magnet links

**Note:** Downloaded files need to be manually moved to your music library and added to the database.

### 2. External Recommendations

When searching for songs not in your library:
- **YouTube**: Opens YouTube search for the query
- **Spotify**: Opens Spotify search for the query

### 3. Download Status Indicators

- `✓ Local` - Song is downloaded and available
- `Not Downloaded` - Song record exists but file not available
- `⬇ Downloading` - Download in progress
- `🔗 External` - External link/recommendation

## Troubleshooting

### Database Connection Issues

**Problem:** "SQLException: Access denied for user 'root'"
**Solution:**
```sql
-- In phpMyAdmin, run:
ALTER USER 'root'@'localhost' IDENTIFIED BY '';
FLUSH PRIVILEGES;
```

**Problem:** "No suitable driver found"
**Solution:**
- Ensure MySQL Connector JAR is in classpath
- Check JAR version compatibility

### Torrent Download Issues

**Problem:** "Cannot open magnet link"
**Solution:**
- Install a torrent client (qBittorrent recommended)
- Configure torrent client as default for magnet links
- On Windows: Settings → Apps → Default apps → Choose default apps by protocol

**Problem:** "No torrents found"
**Solution:**
- Check internet connection
- Try different search terms
- API might be temporarily unavailable

### UI Display Issues

**Problem:** UI looks different or poorly scaled
**Solution:**
```java
// Add to main method before creating UI
System.setProperty("sun.java2d.uiScale", "1.0");
```

## Advanced Features

### Adding Programmatic Torrent Downloads

To download torrents directly within the application:

1. Add **libtorrent4j** to your project:
```xml
<!-- Maven -->
<dependency>
    <groupId>org.libtorrent4j</groupId>
    <artifactId>libtorrent4j</artifactId>
    <version>2.0.9-1</version>
</dependency>
```

2. Implement in `TorrentDownloader.java`:
```java
SessionManager sessionManager = new SessionManager();
sessionManager.start();

byte[] data = sessionManager.fetchMagnet(magnetLink, 30);
File torrentFile = new File(downloadDirectory, title + ".torrent");
Files.write(torrentFile.toPath(), data);

TorrentInfo ti = TorrentInfo.bdecode(data);
sessionManager.download(ti, new File(downloadDirectory));
```

### Adding Music Playback

Install **JLayer** for MP3 playback:

```java
import javazoom.jl.player.Player;

public void playSong(String filePath) {
    try {
        FileInputStream fis = new FileInputStream(filePath);
        Player player = new Player(fis);
        player.play();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

### Database Backup

Regular backups recommended:

```bash
# Command line backup
mysqldump -u root music_db > backup.sql

# Restore
mysql -u root music_db < backup.sql
```

## Customization

### Changing Color Scheme

Edit colors in `MusicSearchUI.java`:

```java
private final Color BACKGROUND_COLOR = new Color(18, 18, 18);
private final Color SIDEBAR_COLOR = new Color(0, 0, 0);
private final Color CARD_COLOR = new Color(40, 40, 40);
private final Color ACCENT_COLOR = new Color(30, 215, 96); // Spotify green
```

### Adding More Menu Items

In `createSidebar()` method:

```java
String[] menuItems = {"Playlists", "Downloaded", "Search", "Favorites", "Recent"};
```

### Custom Table Columns

Modify in `createMainContent()`:

```java
String[] columnNames = {"Title", "Artist", "Album", "Genre", "Year", "Status", "Rating", "Actions"};
```

## Security Considerations

### Torrent Downloads
- Only download from trusted sources
- Check file sizes and seeders count
- Scan downloaded files with antivirus
- Respect copyright laws in your jurisdiction

### Database Security
- Change default MySQL password
- Use prepared statements (already implemented)
- Regular database backups
- Limit database user permissions

## Performance Optimization

### Large Libraries
For databases with 10,000+ songs:

1. **Add Indexes:**
```sql
CREATE INDEX idx_title ON songs(title);
CREATE INDEX idx_artist ON songs(artist);
CREATE INDEX idx_genre ON songs(genre);
```

2. **Implement Pagination:**
```java
// Add LIMIT to queries
String query = "SELECT * FROM songs WHERE title LIKE ? LIMIT 100 OFFSET ?";
```

3. **Lazy Loading:**
Load results as user scrolls instead of all at once

## Known Limitations

1. **Torrent API Availability**
   - The Pirate Bay API may be blocked in some regions
   - Consider adding alternative torrent sources

2. **No Built-in Music Player**
   - Currently relies on external players
   - Can be extended with JLayer library

3. **Manual File Management**
   - Downloaded files must be manually added to library
   - Future: Auto-import from download folder

4. **No Playlist Management**
   - UI shows "Playlists" but feature not implemented
   - Can be added with additional database table

## Future Enhancements

### Planned Features
- [ ] Built-in music player with controls
- [ ] Playlist creation and management
- [ ] Auto-import from download folder
- [ ] Album artwork display
- [ ] Lyrics integration
- [ ] Last.fm scrobbling
- [ ] Equalizer and audio effects
- [ ] Cloud sync capabilities
- [ ] Mobile companion app

### Contributing
This is an educational project. Feel free to:
- Add new features
- Improve UI/UX
- Add more torrent sources
- Implement missing features

## License & Legal

**Important:** 
- This software is for personal use and education
- Downloading copyrighted material without permission may be illegal
- Users are responsible for compliance with local laws
- Use torrent features only for legal content

## Support & Resources

### Documentation
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [Java Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/)
- [JDBC Guide](https://docs.oracle.com/javase/tutorial/jdbc/)

### Community
- Stack Overflow for Java/MySQL questions
- GitHub Issues for bug reports
- Reddit: r/javahelp

### Torrent Clients
- **qBittorrent** (Recommended, Open Source)
- **Transmission** (Lightweight)
- **Deluge** (Cross-platform)

## Changelog

### Version 1.0.0 (Current)
- Initial release
- Spotify-inspired UI
- Local music library search
- Torrent integration
- External recommendations (YouTube/Spotify)
- Queue management
- Database integration

## Credits

**Design Inspiration:** Spotify
**Technologies:** Java Swing, MySQL, XAMPP
**APIs Used:** The Pirate Bay API (apibay.org)

---

## Quick Start Checklist

- [ ] Install XAMPP and start MySQL
- [ ] Create `music_db` database
- [ ] Run SQL scripts to create tables
- [ ] Add sample data
- [ ] Download MySQL Connector JAR
- [ ] Configure database connection in code
- [ ] Install torrent client
- [ ] Compile and run application
- [ ] Test search functionality
- [ ] Test torrent download
- [ ] Test external links

**Need Help?** Check the Troubleshooting section or create an issue with:
- Java version
- XAMPP version
- Error messages
- Steps to reproduce

---

**Enjoy your Spotify-inspired music search engine! 🎵**