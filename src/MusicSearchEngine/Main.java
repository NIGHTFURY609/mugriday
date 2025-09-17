package MusicSearchEngine;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MusicSearchUI ui = new MusicSearchUI();
            ui.setVisible(true);
        });
    }
}
