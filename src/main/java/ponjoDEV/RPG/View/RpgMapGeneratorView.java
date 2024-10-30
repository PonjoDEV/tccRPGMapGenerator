package ponjoDEV.RPG.View;

import ponjoDEV.RPG.ImageProcessing.RpgMapGeneratorController;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.*;

public class RpgMapGeneratorView extends JFrame {
    private final JDesktopPane theDesktop = new JDesktopPane();
    private final JFileChooser fileChooser = new JFileChooser();
    private String path;
    private RpgMapGeneratorController rpgController = new RpgMapGeneratorController(this);

    // Array of possible kinds of terrain
    private final JTextField widthField = new JTextField("1200", 5);
    private final JTextField heightField = new JTextField("800", 5);
    private final JComboBox<String> colorMenu = new JComboBox<>(new String[]{"Grassland/Forest", "Water", "Mountain", "Desert/Sand", "Construction", "Roads"});
    private String selectedColor = "Grassland/Forest";
    private int lineThickness = 1; // Default line thickness


    public BufferedImage getCanvasImage() {
        return canvasImage;
    }

    public void setCanvasImage(BufferedImage canvasImage) {
        this.canvasImage = canvasImage;
    }

    private BufferedImage canvasImage;

    private JInternalFrame lastGeneratedZonesFrame; // Track the last generated zones window

    // Panel to hold dinamic drop-downs
    private JPanel colorMenuPanel = new JPanel();

    public JTextField getWidthField() {
        return widthField;
    }

    public JTextField getHeightField() {
        return heightField;
    }

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
        super("RPG Map Generator");

        setLayout(new BorderLayout());

        // Initializing controller with this view instance
        rpgController = new RpgMapGeneratorController(this);

        setupMenu();
        setupSliders();
        setupButtons();
        setupCanvas();
        setupCanvasResize();
        createNewCanvas();


        add(theDesktop, BorderLayout.CENTER);

        colorMenu.addActionListener(e -> selectedColor = (String) colorMenu.getSelectedItem());

        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setupCanvasResize() {
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Width:"));
        inputPanel.add(widthField);
        inputPanel.add(new JLabel("Height:"));
        inputPanel.add(heightField);
        inputPanel.add(new JLabel("Color:"));
        inputPanel.add(colorMenu);
        add(inputPanel, BorderLayout.NORTH);
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

    private void setupCanvas() {
        JInternalFrame frame = new JInternalFrame("Drawing Canvas", true, true, true, true);
        frame.setSize(800, 600);
        Container container = frame.getContentPane();
        DrawingPanel drawingPanel = new DrawingPanel();
        container.add(drawingPanel, BorderLayout.CENTER);
        frame.pack();
        theDesktop.add(frame);
        try {
            frame.setMaximum(true); // Automatically maximizes the frame
        } catch (Exception e) {
            e.printStackTrace();
        }
        frame.setVisible(true);
    }

    private void setupSliders() {
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridLayout(5, 2));

        // Slider 1: Deviation
        JLabel surroundWeight = new JLabel("Surrounding weight");
        JSlider surroundWeightSlider = new JSlider(0, 100);
        surroundWeightSlider.setValue(60);
        surroundWeightSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                rpgController.setSurroundingWeight(surroundWeightSlider.getValue()/100.0);
                System.out.println("Surrounding weight: " + surroundWeightSlider.getValue()/100.0);
            }
        });
        sliderPanel.add(surroundWeight);
        sliderPanel.add(surroundWeightSlider);


        // Slider 2: Canvas Mutation
        JLabel mutationChanceLabel = new JLabel("Mutation Chance");
        JSlider mutationChanceSlider = new JSlider(0, 100);
        mutationChanceSlider.setValue(30);
        mutationChanceSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                rpgController.setMutationChance(mutationChanceSlider.getValue()/100.0);
                System.out.println("Mutation Chance: " + mutationChanceSlider.getValue()/100.0);
            }
        });
        sliderPanel.add(mutationChanceLabel);
        sliderPanel.add(mutationChanceSlider);

        /*// Slider 3: Perlin noise
        JLabel perlinNoise = new JLabel("Perlin Noise");
        JSlider perlinNoiseSlider = new JSlider(0, 100);
        perlinNoiseSlider.setValue(0);
        perlinNoiseSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                rpgController.setPerlinNoise(perlinNoiseSlider.getValue()/1000.0);
                System.out.println("Perlin Noise: " + perlinNoiseSlider.getValue()/1000.0);
            }
        });
        sliderPanel.add(perlinNoise);
        sliderPanel.add(perlinNoiseSlider);
        //*/

        // Slider 4(?): Prop density
        JLabel propDensityLabel = new JLabel("Prop Density");
        JSlider propDensitySlider = new JSlider(0, 100);
        propDensitySlider.setValue(30);
        propDensitySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                rpgController.setPropDensity(propDensitySlider.getValue()/100.0);
                System.out.println("Prop Density: " + propDensitySlider.getValue()/100.0);
            }
        });
        sliderPanel.add(propDensityLabel);
        sliderPanel.add(propDensitySlider);

        // Slider 5: Line Thickness
        JLabel thicknessLabel = new JLabel("Line Thickness");
        JSlider thicknessSlider = new JSlider(1, 100);
        thicknessSlider.setValue(1); // Default thickness
        thicknessSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                lineThickness = thicknessSlider.getValue();
                System.out.println("Line Thickness: " + lineThickness);
            }
        });
        sliderPanel.add(thicknessLabel);
        sliderPanel.add(thicknessSlider);

        // Slider 6: Zone Spread
        JLabel zoneSpreadLabel = new JLabel("Zone Spread");
        JSlider zoneSpreadSlider = new JSlider(1, 10);
        zoneSpreadSlider.setValue(1); // Default Zone Spread
        zoneSpreadSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                rpgController.setZoneSpread((int) zoneSpreadSlider.getValue());
                System.out.println("Line Thickness: " +(int) zoneSpreadSlider.getValue());
            }
        });
        sliderPanel.add(zoneSpreadLabel);
        sliderPanel.add(zoneSpreadSlider);


        add(sliderPanel, BorderLayout.SOUTH);
    }

    private void setupButtons() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4, 1));

        //Creating and adding first button to the button panel
        JButton createNewCanvas = new JButton("New Canvas");
        createNewCanvas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewCanvas();
            }
        });
        buttonPanel.add(createNewCanvas);

        //Creating and adding second button to the button panel
        JButton generateZonesButton = new JButton("Generate Zones");
        generateZonesButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        rpgController.generateZones(getCanvasImage());
                        generateImage(rpgController.getZoneR(),rpgController.getZoneG(),rpgController.getZoneB(),"Generated Zones");
                    }
                }
        );
        buttonPanel.add(generateZonesButton);

        //Creating and adding third button to the button panel
        JButton saveCurrentZonesButton = new JButton("Save Zone Map");
        saveCurrentZonesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO Its supposed to save the current zoneR zoneG zoneB into a image file and load
            }
        });
        buttonPanel.add(saveCurrentZonesButton);

        //Creating and adding fourth button to the button panel
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

        // Close the previous "Generated Zones" frame if it exists
        if (lastGeneratedZonesFrame != null) {
            lastGeneratedZonesFrame.dispose();
        }

        //Opens Image Window
        JInternalFrame frame = new JInternalFrame(windowTitle, true,true, true, true);
        Container container = frame.getContentPane();
        MyJPanel panel = new MyJPanel();
        panel.setImageIcon(new ImageIcon(image));
        container.add(panel, BorderLayout.CENTER);

        frame.pack();
        theDesktop.add(frame);
        frame.setVisible(true);

        // Update lastGeneratedZonesFrame with the new frame reference
        lastGeneratedZonesFrame = frame;
    }


    private void createNewCanvas() {
        int width = Integer.parseInt(widthField.getText());
        int height = Integer.parseInt(heightField.getText());
        canvasImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvasImage.createGraphics();
        g2d.setColor(Color.WHITE); // Fill background with white
        g2d.fillRect(0, 0, width, height); //maybe pass the getWidth and getHeight instead of 0 0?
        g2d.dispose();
    }

    private class DrawingPanel extends JPanel {
        private int prevX, prevY;
        private boolean drawing = false;

        public DrawingPanel() {
            setBackground(Color.WHITE);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    prevX = e.getX();
                    prevY = e.getY();
                    drawing = true;
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    drawing = false;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (drawing) {
                        int x = e.getX();
                        int y = e.getY();
                        Graphics2D g2d = canvasImage.createGraphics();
                        g2d.setColor(getSelectedColor());

                        // Setting the line thickness
                        g2d.setStroke(new BasicStroke(lineThickness));
                        g2d.drawLine(prevX, prevY, x, y);

                        g2d.dispose();
                        repaint();
                        prevX = x;
                        prevY = y;
                    }
                }
            });
        }

        private Color getSelectedColor() {
            return switch (selectedColor) {
                case "Grassland/Forest" -> Color.GREEN;
                case "Water" -> Color.BLUE;
                case "Mountains" -> Color.BLACK;
                case "Desert/Sand" -> Color.YELLOW;
                case "Construction" -> Color.RED;
                case "Roads" -> Color.MAGENTA;
                default -> Color.BLACK;
            };
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (canvasImage != null) {
                g.drawImage(canvasImage, 0, 0, null);
            }
        }
    }

}