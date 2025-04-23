import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class QMVisualizer extends JFrame {
    private JTextField mintermsField;
    private JTextField variablesField;
    private JTextArea outputArea;
    private JButton minimizeButton;
    private JButton clearButton;
    private JPanel stepsPanel;
    private JTabbedPane tabbedPane;

    public QMVisualizer() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("Quine-McCluskey Boolean Function Minimizer");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Minterms (comma separated):"));
        mintermsField = new JTextField();
        ((AbstractDocument)mintermsField.getDocument()).setDocumentFilter(new NumericDocumentFilter());
        inputPanel.add(mintermsField);

        inputPanel.add(new JLabel("Variables (e.g., ABCD):"));
        variablesField = new JTextField();
        ((AbstractDocument)variablesField.getDocument()).setDocumentFilter(new AlphaDocumentFilter());
        inputPanel.add(variablesField);

        minimizeButton = new JButton("Minimize");
        clearButton = new JButton("Clear");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(minimizeButton);
        buttonPanel.add(clearButton);
        inputPanel.add(buttonPanel);

        add(inputPanel, BorderLayout.NORTH);

        // Output area
        tabbedPane = new JTabbedPane();
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        tabbedPane.addTab("Results", scrollPane);

        stepsPanel = new JPanel();
        stepsPanel.setLayout(new BoxLayout(stepsPanel, BoxLayout.Y_AXIS));
        JScrollPane stepsScrollPane = new JScrollPane(stepsPanel);
        tabbedPane.addTab("Steps", stepsScrollPane);

        add(tabbedPane, BorderLayout.CENTER);

        // Event handlers
        minimizeButton.addActionListener(e -> minimizeBooleanFunction());
        clearButton.addActionListener(e -> clearFields());
    }

    private void clearFields() {
        mintermsField.setText("");
        variablesField.setText("");
        outputArea.setText("");
        stepsPanel.removeAll();
        stepsPanel.revalidate();
        stepsPanel.repaint();
    }

    private void minimizeBooleanFunction() {
        outputArea.setText("");
        stepsPanel.removeAll();
        stepsPanel.revalidate();
        stepsPanel.repaint();

        try {
            String mintermsText = cleanInput(mintermsField.getText());
            String variablesText = cleanInput(variablesField.getText()).toUpperCase();

            if (mintermsText.isEmpty() || variablesText.isEmpty()) {
                throw new IllegalArgumentException("Both fields must be filled");
            }

            // Parse minterms with strict validation
            List<Integer> minterms = Arrays.stream(mintermsText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> {
                        try {
                            int num = Integer.parseInt(s);
                            if (num < 0) throw new IllegalArgumentException("Negative minterms not allowed");
                            return num;
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid number: " + s);
                        }
                    })
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            if (minterms.isEmpty()) {
                throw new IllegalArgumentException("No valid minterms provided");
            }

            // Parse variables with strict validation
            if (!variablesText.matches("[A-Z]+")) {
                throw new IllegalArgumentException("Variables must be uppercase letters (A-Z)");
            }

            List<Character> variables = variablesText.chars()
                    .mapToObj(c -> (char) c)
                    .collect(Collectors.toList());

            // Validate variable count
            int maxMinterm = Collections.max(minterms);
            int requiredVars = (int) Math.ceil(Math.log(maxMinterm + 1) / Math.log(2));
            if (variables.size() < requiredVars) {
                throw new IllegalArgumentException(String.format(
                        "Need at least %d variables for minterm %d (got %d)",
                        requiredVars, maxMinterm, variables.size()));
            }

            // Run algorithm
            QMCore qm = new QMCore(minterms, variables);
            String result = qm.minimize();
            outputArea.setText(result);
            displaySteps(qm.getSteps());

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String cleanInput(String input) {
        return input == null ? "" : input.replaceAll("[^\\dA-Za-z,]", "");
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE));
    }

    private void displaySteps(List<String> steps) {
        stepsPanel.removeAll();
        steps.forEach(step -> {
            JTextArea stepArea = new JTextArea(step);
            stepArea.setEditable(false);
            stepArea.setLineWrap(true);
            stepArea.setWrapStyleWord(true);
            stepArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            stepsPanel.add(stepArea);
            stepsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        });
        stepsPanel.revalidate();
        stepsPanel.repaint();
        tabbedPane.setSelectedIndex(1);
    }

    private static class NumericDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;
            super.insertString(fb, offset, string.replaceAll("[^\\d,]", ""), attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) return;
            super.replace(fb, offset, length, text.replaceAll("[^\\d,]", ""), attrs);
        }
    }

    private static class AlphaDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;
            super.insertString(fb, offset, string.replaceAll("[^A-Za-z]", ""), attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) return;
            super.replace(fb, offset, length, text.replaceAll("[^A-Za-z]", ""), attrs);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QMVisualizer gui = new QMVisualizer();
            gui.setVisible(true);
        });
    }
}
