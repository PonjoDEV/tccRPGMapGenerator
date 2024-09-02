package ponjoDEV.RPG.View;

import ponjoDEV.RPG.ImageProcessing.RpgMapGeneratorController;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class RpgMapGeneratorView extends JFrame {
    private final JDesktopPane theDesktop = new JDesktopPane();
    private final JFileChooser fileChooser = new JFileChooser();
    private String path;
    private RpgMapGeneratorController rpgController = new RpgMapGeneratorController();


    class MyJPanel extends JPanel{
        private ImageIcon imageIcon;

        public MyJPanel(){
            imageIcon = new ImageIcon(path);
        }

        public void setImageIcon(ImageIcon i){
            imageIcon = i;
        }

        public void paintComponent(Graphics g){
            super.paintComponent(g);
            imageIcon.paintIcon(this, g, 0, 0);
        }

        public Dimension getPreferredSize(){
            return new Dimension(imageIcon.getIconWidth(),imageIcon.getIconHeight());
        }
    }

    public RpgMapGeneratorView() {
        super("RPGmapGenerator");

        setLayout(new BorderLayout());

        setupMenu();
        setupSliders();
        setupButtons();

        add(theDesktop, BorderLayout.CENTER);

        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setupMenu() {
        JMenuBar bar = new JMenuBar();

        JMenu addMenu = new JMenu("Open");
        JMenuItem fileItem = new JMenuItem("Open image file");
        addMenu.add(fileItem);
        bar.add(addMenu);

        JMenu addMenu2 = new JMenu("Process");
        JMenuItem regColors = new JMenuItem("Register used colors");
        addMenu2.add(regColors);
        bar.add(addMenu2);

        setJMenuBar(bar);

        //Reading the new image
        fileItem.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int result = fileChooser.showOpenDialog(null);
                        if(result == JFileChooser.CANCEL_OPTION){
                            return;
                        }
                        path = fileChooser.getSelectedFile().getAbsolutePath();

                        JInternalFrame frame = new JInternalFrame("Source Image", true,true, true, true);
                        Container container = frame.getContentPane();
                        MyJPanel panel = new MyJPanel();
                        container.add(panel, BorderLayout.CENTER);

                        frame.pack();
                        theDesktop.add(frame);
                        frame.setVisible(true);
                    }
                }
        );

        //Option to register used colors not actually used right now as a user input, maybe replace or erase later
        regColors.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                    }
                }
        );
    }

    private void setupSliders() {
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridLayout(3, 2));

        // Slider 1: Deviation
        JLabel deviationLabel = new JLabel("Deviation");
        JSlider deviationSlider = new JSlider(0, 100);
        deviationSlider.setValue(15);  // Valor inicial
        deviationSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                //Deviation of 0.15 being optimal for now
                rpgController.setDeviation(deviationSlider.getValue()/100.0);
                System.out.println("Deviation: " + deviationSlider.getValue() / 100.0);
            }
        });
        sliderPanel.add(deviationLabel);
        sliderPanel.add(deviationSlider);


        // Slider 2: Canvas Mutation
        JLabel canvasMutationLabel = new JLabel("Mutation Chance");
        JSlider canvasMutationSlider = new JSlider(0, 100);
        canvasMutationSlider.setValue(30);  // Valor inicial
        canvasMutationSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                //changesRation of 0.3 being optimal for now
                rpgController.setCanvasMutation(canvasMutationSlider.getValue()/100.0);
                System.out.println("Mutation Chance: " + canvasMutationSlider.getValue() / 100.0);
            }
        });
        sliderPanel.add(canvasMutationLabel);
        sliderPanel.add(canvasMutationSlider);

        // Slider 3: ChunkDensity
        JLabel chunkDensityLabel = new JLabel("Chunk Density");
        JSlider chunkDensitySlider = new JSlider(1, 10);
        chunkDensitySlider.setValue(5);  // Valor inicial
        chunkDensitySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                //TODO Value as 1 will mean there will be no division, so the screen is just 1 chunk generated,
                // if more it need to scale in a base of 4 there could also be some multithread aplied to it
                rpgController.setChunkDensity(Math.round(chunkDensitySlider.getValue()));
                System.out.println("Chunk Density: " + Math.round(chunkDensitySlider.getValue()));
            }
        });
        sliderPanel.add(chunkDensityLabel);
        sliderPanel.add(chunkDensitySlider);

        add(sliderPanel, BorderLayout.SOUTH);
    }

    private void setupButtons() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1));
        //Creating and adding first button to the button panel
        JButton generateZonesButton = new JButton("Generate Zones");
        generateZonesButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        rpgController.generateZones(path);
                        generateImage(rpgController.getZoneR(),rpgController.getZoneG(),rpgController.getZoneB(),"Generated Zones");
                    }
                }
        );
        buttonPanel.add(generateZonesButton);

        //Creating and adding second button to the button panel
        JButton saveCurrentZonesButton = new JButton("Save Zone Map");
        saveCurrentZonesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO Its supposed to save the current zoneR zoneG zoneB into a image file and load
            }
        });
        buttonPanel.add(saveCurrentZonesButton);

        //Creating and adding third button to the button panel
        JButton createMapButton = new JButton("Create Map ");
        createMapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO using the zoneR zoneG zoneB its supposed to pick the assets from a folder and put them into the image, still not sure how to do though
            }
        });
        buttonPanel.add(createMapButton);
        add(buttonPanel, BorderLayout.EAST);
    }

    private void generateImage(int matrix1[][], int matrix2[][], int matrix3[][], String windowTitle){
        int[] pixels = new int[matrix1.length * matrix1[0].length*3];
        BufferedImage image = new BufferedImage(matrix1[0].length, matrix1.length, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();
        int pos =0;
        for(int i =0; i < matrix1.length; i++){
            for(int j = 0; j < matrix1[0].length; j++){
                pixels[pos] = matrix1[i][j];
                pixels[pos+1] = matrix2[i][j];
                pixels[pos+2] = matrix3[i][j];
                pos+=3;
            }
        }
        raster.setPixels(0, 0, matrix1[0].length, matrix1.length, pixels);

        //Abre Janela da imagem
        JInternalFrame frame = new JInternalFrame(windowTitle, true,true, true, true);
        Container container = frame.getContentPane();
        MyJPanel panel = new MyJPanel();
        panel.setImageIcon(new ImageIcon(image));
        container.add(panel, BorderLayout.CENTER);

        frame.pack();
        theDesktop.add(frame);
        frame.setVisible(true);
    }
}