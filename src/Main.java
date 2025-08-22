import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends JFrame {

    private JList<String> songJList;
    private DefaultListModel<String> listModel;
    private JButton playButton, stopButton, resetButton, loopButton;
    private final JLabel timerLabel;
    private JProgressBar progressBar;
    private Clip clip;
    private boolean looping = false;
    private Timer timer;

    public Main() {
        setTitle("🎵 Java Music Player");
        setSize(500, 400);
        getContentPane().setBackground(Color.BLACK);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Song List
        listModel = new DefaultListModel<>();
        String[] songs = {
                "Gotta_Love_Me.wav",
                "Style - Kendrick Lamar.wav",
                "Watch The Party Die - Kendrick Lamar.wav",
                "Kendrick Lamar - Teach Me How To Pray.wav",
                "Bodies - Kendrick Lamar.wav"
        };
        for (String song : songs) {
            listModel.addElement(song);
        }

        songJList = new JList<>(listModel);
        songJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(songJList), BorderLayout.CENTER);
        songJList.setBackground(Color.DARK_GRAY);
        songJList.setForeground(Color.white);

        // Timer Label
        timerLabel = new JLabel("00:00 / 00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(timerLabel, BorderLayout.NORTH);

        //Progress Bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        add(progressBar, BorderLayout.NORTH);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        playButton = new JButton("Play");
        stopButton = new JButton("Stop");
        resetButton = new JButton("Reset");
        loopButton = new JButton("Loop");

        buttonPanel.add(playButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(loopButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Button Actions
        playButton.addActionListener(e -> playSelectedSong());
        stopButton.addActionListener(e -> stopSong());
        resetButton.addActionListener(e -> resetSong());
        loopButton.addActionListener(e -> toggleLoop());

        //Progress bar interactable
        progressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(clip!=null && clip.isOpen()) {
                    int mouseX = e.getX();
                    double percent = mouseX / (double) progressBar.getWidth();
                    long newPosition = (long)(clip.getMicrosecondLength() *percent);
                    clip.setMicrosecondPosition(newPosition);
                }
            }
        });

        setVisible(true);
    }

    private void playSelectedSong() {
        if (songJList.getSelectedValue() == null) {
            JOptionPane.showMessageDialog(this, "Select a song first!");
            return;
        }

        String songFile = songJList.getSelectedValue();
        try {
            if (clip != null && clip.isOpen()) {
                clip.stop();
                clip.close();
            }

            File file = new File(songFile);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            // Looping
            if (looping) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }

            clip.start();
            startTimer();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            JOptionPane.showMessageDialog(this, "Error playing file: " + songFile);
        }
    }

    private void stopSong() {
        if (clip != null) clip.stop();
        stopTimer();
    }

    private void resetSong() {
        if (clip != null) {
            clip.setMicrosecondPosition(0);
            clip.start();
            startTimer();
        }
    }

    private void toggleLoop() {
        looping = !looping;
        if (clip != null) {
            if (looping) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                JOptionPane.showMessageDialog(this, "Looping enabled!");
            } else {
                clip.loop(0);
                JOptionPane.showMessageDialog(this, "Looping disabled!");
            }
        }
    }

    private void startTimer() {
        stopTimer(); // Stop previous timer if any
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (clip != null && clip.isOpen()) {
                    long currentMicro = clip.getMicrosecondPosition();
                    long totalMicro = clip.getMicrosecondLength();
                    int currentSec = (int) (currentMicro / 1_000_000);
                    int totalSec = (int) (totalMicro / 1_000_000);

                    String timeText = String.format("%02d:%02d / %02d:%02d",
                            currentSec / 60, currentSec % 60,
                            totalSec / 60, totalSec % 60);

                    int progress = (int) ((currentMicro * 100) / totalMicro);

                    SwingUtilities.invokeLater(() -> {
                        timerLabel.setText(timeText);
                        progressBar.setValue(progress);
                    });
                }
            }
        }, 0, 500);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
