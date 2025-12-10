import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import com.google.gson.Gson;

public class Visualizer extends JFrame {
    private Log log;
    private Config config;
    private int currentStep = 0;
    private javax.swing.Timer timer;  // 明确使用 Swing Timer
    private JPanel mainPanel;
    private JLabel statusLabel;
    private JSlider speedSlider;
    private Map<String, String> trainPositions;
    private Map<String, PassengerState> passengerStates;
    
    private static final Map<String, Color> LINE_COLORS = Map.of(
        "red", new Color(218, 41, 28),
        "orange", new Color(237, 139, 0),
        "green", new Color(0, 132, 61),
        "blue", new Color(0, 61, 165)
    );
    
    static class PassengerState {
        String location;
        String onTrain;
        
        PassengerState(String location, String onTrain) {
            this.location = location;
            this.onTrain = onTrain;
        }
    }
    
    public Visualizer() {
        setTitle("MBTA Simulation Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        trainPositions = new HashMap<>();
        passengerStates = new HashMap<>();
        
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        
        JButton loadConfigBtn = new JButton("Load Config");
        loadConfigBtn.addActionListener(e -> loadConfig());
        
        JButton loadLogBtn = new JButton("Load Log");
        loadLogBtn.addActionListener(e -> loadLog());
        
        JButton playBtn = new JButton("Play");
        playBtn.addActionListener(e -> play());
        
        JButton pauseBtn = new JButton("Pause");
        pauseBtn.addActionListener(e -> pause());
        
        JButton stepBtn = new JButton("Step");
        stepBtn.addActionListener(e -> step());
        
        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> reset());
        
        speedSlider = new JSlider(100, 2000, 500);
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        
        controlPanel.add(loadConfigBtn);
        controlPanel.add(loadLogBtn);
        controlPanel.add(playBtn);
        controlPanel.add(pauseBtn);
        controlPanel.add(stepBtn);
        controlPanel.add(resetBtn);
        controlPanel.add(new JLabel("Speed (ms):"));
        controlPanel.add(speedSlider);
        
        add(controlPanel, BorderLayout.NORTH);
        
        // Status panel
        statusLabel = new JLabel("Load config and log files to begin");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);
        
        // Main visualization panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadConfig() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Gson gson = new Gson();
                FileReader reader = new FileReader(chooser.getSelectedFile());
                config = gson.fromJson(reader, Config.class);
                reader.close();
                
                initializeState();
                updateVisualization();
                statusLabel.setText("Config loaded: " + chooser.getSelectedFile().getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading config: " + ex.getMessage());
            }
        }
    }
    
    private void loadLog() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Reader reader = new BufferedReader(new FileReader(chooser.getSelectedFile()));
                log = LogJson.fromJson(reader).toLog();
                currentStep = 0;
                
                initializeState();
                updateVisualization();
                statusLabel.setText("Log loaded: " + chooser.getSelectedFile().getName() + 
                                  " (" + log.events().size() + " events)");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading log: " + ex.getMessage());
            }
        }
    }
    
    private void initializeState() {
        trainPositions.clear();
        passengerStates.clear();
        
        if (config != null) {
            if (config.lines != null) {
                for (Map.Entry<String, List<String>> entry : config.lines.entrySet()) {
                    trainPositions.put(entry.getKey(), entry.getValue().get(0));
                }
            }
            
            if (config.trips != null) {
                for (Map.Entry<String, List<String>> entry : config.trips.entrySet()) {
                    passengerStates.put(entry.getKey(), 
                        new PassengerState(entry.getValue().get(0), null));
                }
            }
        }
    }
    
    private void play() {
        if (timer != null) timer.stop();
        
        timer = new javax.swing.Timer(speedSlider.getValue(), e -> {
            if (log != null && currentStep < log.events().size()) {
                step();
            } else {
                pause();
            }
        });
        timer.start();
    }
    
    private void pause() {
        if (timer != null) {
            timer.stop();
        }
    }
    
    private void step() {
        if (log == null || currentStep >= log.events().size()) return;
        
        Event event = log.events().get(currentStep);
        applyEvent(event);
        currentStep++;
        
        updateVisualization();
        statusLabel.setText("Step " + currentStep + " / " + log.events().size() + ": " + event);
    }
    
    private void reset() {
        pause();
        currentStep = 0;
        initializeState();
        updateVisualization();
        statusLabel.setText("Reset to initial state");
    }
    
    private void applyEvent(Event event) {
        if (event instanceof MoveEvent) {
            MoveEvent me = (MoveEvent) event;
            trainPositions.put(me.t.toString(), me.s2.toString());
        } else if (event instanceof BoardEvent) {
            BoardEvent be = (BoardEvent) event;
            PassengerState state = passengerStates.get(be.p.toString());
            if (state != null) {
                state.onTrain = be.t.toString();
            }
        } else if (event instanceof DeboardEvent) {
            DeboardEvent de = (DeboardEvent) event;
            PassengerState state = passengerStates.get(de.p.toString());
            if (state != null) {
                state.location = de.s.toString();
                state.onTrain = null;
            }
        }
    }
    
    private void updateVisualization() {
        mainPanel.removeAll();
        
        if (config == null) {
            JLabel promptLabel = new JLabel("<html><center>" +
                "<h2>No Configuration Loaded</h2>" +
                "<p>Click 'Load Config' to load sample.json</p>" +
                "</center></html>", SwingConstants.CENTER);
            mainPanel.add(promptLabel);
            mainPanel.revalidate();
            mainPanel.repaint();
            return;
        }
        
        if (config.lines != null) {
            for (Map.Entry<String, List<String>> entry : config.lines.entrySet()) {
                mainPanel.add(createLinePanel(entry.getKey(), entry.getValue()));
            }
        }
        
        // Add passenger summary panel
        mainPanel.add(createPassengerSummaryPanel());
        
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private JPanel createPassengerSummaryPanel() {
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            "PASSENGER STATUS",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            Color.DARK_GRAY));
        summaryPanel.setBackground(new Color(245, 245, 250));
        
        if (passengerStates.isEmpty()) {
            JLabel noPassengers = new JLabel("  No passengers in simulation");
            noPassengers.setFont(new Font("Arial", Font.ITALIC, 12));
            summaryPanel.add(noPassengers);
            return summaryPanel;
        }
        
        for (Map.Entry<String, PassengerState> entry : passengerStates.entrySet()) {
            String name = entry.getKey();
            PassengerState state = entry.getValue();
            
            JPanel pPanel = new JPanel();
            pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.Y_AXIS));
            pPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            pPanel.setBackground(Color.WHITE);
            pPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Passenger name
            JLabel nameLabel = new JLabel("👤 " + name);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 13));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            pPanel.add(nameLabel);
            
            // Status
            if (state.onTrain != null) {
                JLabel statusLabel = new JLabel("  ● On " + state.onTrain.toUpperCase() + " train");
                statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                statusLabel.setForeground(LINE_COLORS.getOrDefault(state.onTrain, Color.GRAY));
                statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                pPanel.add(statusLabel);
                
                JLabel locationLabel = new JLabel("  ● Current location: " + state.location);
                locationLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                locationLabel.setForeground(Color.GRAY);
                locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                pPanel.add(locationLabel);
            } else {
                JLabel statusLabel = new JLabel("  ● Waiting at station");
                statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                statusLabel.setForeground(new Color(120, 120, 120));
                statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                pPanel.add(statusLabel);
                
                JLabel locationLabel = new JLabel("  ● Location: " + state.location);
                locationLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                locationLabel.setForeground(Color.GRAY);
                locationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                pPanel.add(locationLabel);
            }
            
            summaryPanel.add(pPanel);
            summaryPanel.add(Box.createVerticalStrut(8));
        }
        
        return summaryPanel;
    }


    private JPanel createLinePanel(String lineName, List<String> stations) {
        JPanel linePanel = new JPanel();
        linePanel.setLayout(new BoxLayout(linePanel, BoxLayout.Y_AXIS));
        linePanel.setBorder(BorderFactory.createTitledBorder(
            lineName.toUpperCase() + " Line"));
        linePanel.setBackground(Color.WHITE);
        
        Color lineColor = LINE_COLORS.getOrDefault(lineName, Color.GRAY);
        
        for (String station : stations) {
            JPanel stationPanel = createStationPanel(station, lineName, lineColor);
            linePanel.add(stationPanel);
            linePanel.add(Box.createVerticalStrut(5));
        }
        
        return linePanel;
    }
    
    private JPanel createStationPanel(String station, String lineName, Color lineColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(lineColor, 2));
        panel.setBackground(new Color(250, 250, 250));
        
        JLabel nameLabel = new JLabel("  " + station);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(nameLabel);
        
        // Show trains at this station
        for (Map.Entry<String, String> entry : trainPositions.entrySet()) {
            if (entry.getValue().equals(station)) {
                JLabel trainLabel = new JLabel("    [TRAIN] " + entry.getKey().toUpperCase());
                trainLabel.setForeground(LINE_COLORS.getOrDefault(entry.getKey(), Color.GRAY));
                trainLabel.setFont(new Font("Arial", Font.BOLD, 11));
                panel.add(trainLabel);
                
                // Show passengers on this train
                for (Map.Entry<String, PassengerState> pEntry : passengerStates.entrySet()) {
                    if (entry.getKey().equals(pEntry.getValue().onTrain)) {
                        JLabel passengerLabel = new JLabel("      [ON TRAIN] " + pEntry.getKey());
                        passengerLabel.setFont(new Font("Arial", Font.PLAIN, 10));
                        panel.add(passengerLabel);
                    }
                }
            }
        }
        
        // Show passengers at this station
        for (Map.Entry<String, PassengerState> entry : passengerStates.entrySet()) {
            if (entry.getValue().location.equals(station) && entry.getValue().onTrain == null) {
                JLabel passengerLabel = new JLabel("    [WAITING] " + entry.getKey());
                passengerLabel.setFont(new Font("Arial", Font.PLAIN, 10));
                panel.add(passengerLabel);
            }
        }
        
        return panel;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Visualizer viz = new Visualizer();
            viz.setVisible(true);
        });
    }
}