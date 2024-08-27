package ponjoDEV.RPG.View;

import javax.swing.*;
import java.awt.*;

public class RpgMapGeneratorView extends JFrame {
    private JDesktopPane theDesktop = new JDesktopPane();
    private JFileChooser fileChooser = new JFileChooser();
    private JSlider deviationSlider, changeCanvasSlider, chunkDensitySlider;
    private JPanel buttonPanel = new JPanel();
    private JButton generateZonesButton = new JButton("Gerar zonas");
    private JButton createMapButton = new JButton("Criar mapa ");

    public RpgMapGeneratorView() {
        super("RPGmapGenerator");

        setLayout(new BorderLayout());

        setupMenu();
        setupSliders();
        setupButtons();

        add(theDesktop, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);

        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setupMenu() {
        JMenuBar bar = new JMenuBar();

        JMenu addMenu = new JMenu("Abrir");
        JMenuItem fileItem = new JMenuItem("Abrir uma imagem de arquivo");
        addMenu.add(fileItem);
        bar.add(addMenu);

        JMenu addMenu2 = new JMenu("Processar");
        JMenuItem regColors = new JMenuItem("Registrar cores utilizadas");
        addMenu2.add(regColors);
        bar.add(addMenu2);

        setJMenuBar(bar);
    }

    private void setupSliders() {
        JPanel sliderPanel = new JPanel(new GridLayout(3, 2));

        deviationSlider = new JSlider(0, 100, 15);
        JLabel deviationLabel = new JLabel("Deviation");
        sliderPanel.add(deviationLabel);
        sliderPanel.add(deviationSlider);

        changeCanvasSlider = new JSlider(0, 100, 30);
        JLabel changeCanvasLabel = new JLabel("Change Canvas");
        sliderPanel.add(changeCanvasLabel);
        sliderPanel.add(changeCanvasSlider);

        chunkDensitySlider = new JSlider(1, 10, 5);
        JLabel chunkDensityLabel = new JLabel("Chunk Density");
        sliderPanel.add(chunkDensityLabel);
        sliderPanel.add(chunkDensitySlider);

        add(sliderPanel, BorderLayout.SOUTH);
    }

    private void setupButtons() {
        buttonPanel.setLayout(new GridLayout(2, 1));
        buttonPanel.add(generateZonesButton);
        buttonPanel.add(createMapButton);
    }

    public JFileChooser getFileChooser() {
        return fileChooser;
    }

    public JButton getGenerateZonesButton() {
        return generateZonesButton;
    }

    public JButton getCreateMapButton() {
        return createMapButton;
    }

    public JDesktopPane getDesktop() {
        return theDesktop;
    }

    public JSlider getDeviationSlider() {
        return deviationSlider;
    }

    public JSlider getChangeCanvasSlider() {
        return changeCanvasSlider;
    }

    public JSlider getChunkDensitySlider() {
        return chunkDensitySlider;
    }
}
