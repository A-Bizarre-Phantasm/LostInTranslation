package translation;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class GUI {

    // Helper class to store code (display and code are the same)
    static class Item {
        String code;
        Item(String code) { this.code = code; }
        @Override
        public String toString() { return code; } // display code directly
    }

    // Read country 3-letter codes from resource
    private static Item[] readCountryCodes(String resourcePath) {
        ArrayList<Item> list = new ArrayList<>();
        try (InputStream is = GUI.class.getResourceAsStream(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("Country")) continue;
                String[] parts = line.split("\\t");
                list.add(new Item(parts[2])); // 3-letter code
            }
        } catch (Exception e) { e.printStackTrace(); }
        list.sort(Comparator.comparing(i -> i.code));
        return list.toArray(new Item[0]);
    }

    // Read language 2-letter codes from resource
    private static Item[] readLanguageCodes(String resourcePath) {
        ArrayList<Item> list = new ArrayList<>();
        try (InputStream is = GUI.class.getResourceAsStream(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("ISO")) continue;
                String[] parts = line.split("\\t");
                list.add(new Item(parts[1])); // 2-letter code
            }
        } catch (Exception e) { e.printStackTrace(); }
        list.sort(Comparator.comparing(i -> i.code));
        return list.toArray(new Item[0]);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Countries
            Item[] countries = readCountryCodes("/country-codes.txt");
            JList<Item> countryList = new JList<>(countries);
            countryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Pre-select CAN
            for (int i = 0; i < countries.length; i++) {
                if (countries[i].code.equalsIgnoreCase("CAN")) {
                    countryList.setSelectedIndex(i);
                    break;
                }
            }

            JScrollPane countryScroll = new JScrollPane(countryList);
            JPanel countryPanel = new JPanel();
            countryPanel.add(new JLabel("Country:"));
            countryPanel.add(countryScroll);

            // Languages
            Item[] languages = readLanguageCodes("/language-codes.txt");
            JComboBox<Item> languageDropdown = new JComboBox<>(languages);

            // Pre-select English
            for (int i = 0; i < languages.length; i++) {
                if (languages[i].code.equalsIgnoreCase("en")) {
                    languageDropdown.setSelectedIndex(i);
                    break;
                }
            }

            JPanel languagePanel = new JPanel();
            languagePanel.add(new JLabel("Language:"));
            languagePanel.add(languageDropdown);

            // Result
            JLabel resultLabel = new JLabel("");
            JPanel resultPanel = new JPanel();
            resultPanel.add(new JLabel("Translation:"));
            resultPanel.add(resultLabel);

            // Translator
            Translator translator = new CanadaTranslator();

            // Update function
            Runnable updateTranslation = () -> {
                Item country = countryList.getSelectedValue();
                Item language = (Item) languageDropdown.getSelectedItem();
                if (country != null && language != null) {
                    String result = translator.translate(country.code.toLowerCase(), language.code.toLowerCase());
                    resultLabel.setText(result != null ? result : "no translation found!");
                }
            };

            // Listeners
            languageDropdown.addActionListener(e -> updateTranslation.run());
            countryList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) updateTranslation.run();
            });

            // Main panel
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.add(countryPanel);
            mainPanel.add(languagePanel);
            mainPanel.add(resultPanel);

            JFrame frame = new JFrame("Country Name Translator");
            frame.setContentPane(mainPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);

            // Initial translation
            updateTranslation.run();
        });
    }
}
