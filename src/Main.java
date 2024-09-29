import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

class QuickLinkShortenerGUI extends JFrame {
    private static final String FILE_NAME = "url_mappings.txt";
    private Map<String, String> urlMappings = new HashMap<>();
    private int urlCounter = 1;  // Counter to generate unique IDs for short URLs

    // GUI components
    private JTextField longURLField;
    private JTextField shortURLField;
    private JTextArea outputArea;

    public QuickLinkShortenerGUI() {
        setTitle("QuickLink Shortener");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load URL mappings
        loadMappings();

        // Create components
        longURLField = new JTextField(30);
        shortURLField = new JTextField(30);
        JButton shortenButton = new JButton("Shorten URL");
        JButton retrieveButton = new JButton("Retrieve Original URL");
        JButton openURLButton = new JButton("Open Original URL");
        outputArea = new JTextArea(5, 40);
        outputArea.setEditable(false);

        // Layout setup
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1, 5, 5));
        panel.add(new JLabel("Enter Long URL:"));
        panel.add(longURLField);
        panel.add(shortenButton);
        panel.add(new JLabel("Enter Shortened URL:"));
        panel.add(shortURLField);
        panel.add(retrieveButton);
        panel.add(openURLButton);
        panel.add(new JScrollPane(outputArea));

        add(panel);

        // Button actions
        shortenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String longURL = longURLField.getText();
                String shortURL = shortenURL(longURL);
                outputArea.setText("Shortened URL: " + shortURL);
            }
        });

        retrieveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String shortURL = shortURLField.getText();
                String originalURL = retrieveURL(shortURL);
                if (originalURL != null) {
                    outputArea.setText("Original URL: " + originalURL);
                } else {
                    outputArea.setText("URL not found.");
                }
            }
        });

        openURLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String shortURL = shortURLField.getText();
                String originalURL = retrieveURL(shortURL);
                if (originalURL != null) {
                    try {
                        Desktop.getDesktop().browse(new URI(originalURL));
                    } catch (IOException | URISyntaxException ex) {
                        outputArea.setText("Failed to open URL.");
                    }
                } else {
                    outputArea.setText("URL not found.");
                }
            }
        });

        setVisible(true);
    }

    // Method to generate a unique short URL
    private String generateShortURL() {
        String shortURL = "http://short.ly/" + Integer.toString(urlCounter, 36);  // Base-36 encoding
        urlCounter++;
        return shortURL;
    }

    // Method to shorten the URL
    public String shortenURL(String longURL) {
        if (urlMappings.containsValue(longURL)) {
            // If URL already exists, return the existing short URL
            for (Map.Entry<String, String> entry : urlMappings.entrySet()) {
                if (entry.getValue().equals(longURL)) {
                    return entry.getKey();
                }
            }
        }
        // Generate a new short URL
        String shortURL = generateShortURL();
        urlMappings.put(shortURL, longURL);
        saveMappings();
        return shortURL;
    }

    // Method to retrieve the original URL
    public String retrieveURL(String shortURL) {
        return urlMappings.get(shortURL);
    }

    // Save URL mappings to a file
    private void saveMappings() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Map.Entry<String, String> entry : urlMappings.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            outputArea.setText("Error saving URL mappings.");
        }
    }

    // Load URL mappings from a file
    private void loadMappings() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    urlMappings.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            outputArea.setText("No previous mappings found.");
        }
    }

    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuickLinkShortenerGUI());
    }
}
