package ponjoDEV.RPG.View;

import ponjoDEV.RPG.ImageProcessing.RpgMapGeneratorController;
import ponjoDEV.RPG.ImageProcessing.TextureController;
import ponjoDEV.RPG.Model.Zone;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class RpgMapGeneratorView extends JFrame {
    private final JDesktopPane theDesktop = new JDesktopPane();
    private final JFileChooser fileChooser = new JFileChooser();
    private String path, selectedTexturePack;

    private ArrayList<Zone> zones = new ArrayList<>();
    String[] textureFolders;

    private RpgMapGeneratorController rpgController = new RpgMapGeneratorController(this);
    private TextureController textureController = new TextureController();

    private final JTextField widthField = new JTextField("1200", 5);
    private final JTextField heightField = new JTextField("720", 5);

    // Array of possible kinds of Textures
    JComboBox<String> texturesMenu = new JComboBox<>(textureController.getTextureFolders());
    private String selectedColor = "Grassland/Forest";
    private int lineThickness, test;


    public BufferedImage getCanvasImage() {
        return canvasImage;
    }

    //public void setCanvasImage(BufferedImage canvasImage) { this.canvasImage = canvasImage;}

    private BufferedImage canvasImage;

    private JInternalFrame lastGeneratedZonesFrame; // Track the last generated zones window

    public JTextField getWidthField() {
        return widthField;
    }

    public JTextField getHeightField() {
        return heightField;
    }
    public int getLineThickness() { return lineThickness; }

    public void setLineThickness(int lineThickness) { this.lineThickness = lineThickness; }

    public int getTest() { return test; }

    public void setTest(int test) { this.test = test; }

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
            return new Dimension(1200, 720);
        }
    }

    public RpgMapGeneratorView() {
        //Im kinda retarded
        super("RPG Map Generator");

        setLayout(new BorderLayout());

        // Initializing controller with this view instance
        rpgController = new RpgMapGeneratorController(this);
        textureController = new TextureController();

        setupMenu();
        setupSliders();
        setupButtons();
        setupCanvas();
        setupCanvasResizeAndTexture();
        setupColorSelection();
        createNewCanvas();


        add(theDesktop, BorderLayout.CENTER);


        setSize(1200, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setupColorSelection() {
        JPanel colorButtonPanel = new JPanel();
        colorButtonPanel.setLayout(new GridLayout(6, 1));

        JButton grassland = new JButton("Grassland");
        grassland.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = "Grassland";
            }
        });
        colorButtonPanel.add(grassland);

        JButton water = new JButton("Water");
        water.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = "Water";
            }
        });
        colorButtonPanel.add(water);

        JButton mountain = new JButton("Mountain");
        mountain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = "Mountain";
            }
        });
        colorButtonPanel.add(mountain);

        JButton desertSand = new JButton("Desert");
        desertSand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = "Desert";
            }
        });
        colorButtonPanel.add(desertSand);

        JButton construction = new JButton("Construction");
        construction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = "Construction";
            }
        });
        colorButtonPanel.add(construction);

        JButton roads = new JButton("Roads");
        roads.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = "Roads";
            }
        });
        colorButtonPanel.add(roads);

        add(colorButtonPanel, BorderLayout.WEST);
    }

    private void setupCanvasResizeAndTexture() {
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Width:"));
        inputPanel.add(widthField);
        inputPanel.add(new JLabel("Height:"));
        inputPanel.add(heightField);

        JButton refresh = new JButton("Refresh");
                refresh.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        textureFolders = textureController.refreshFolders(textureController.getPath());
                        texturesMenu.removeAllItems();
                        for (int i = 0; i < textureFolders.length; i++) {
                            texturesMenu.addItem(textureFolders[i]);
                        }

                    }
                });


        inputPanel.add(texturesMenu);

        inputPanel.add(refresh);

        refresh.doClick();

        add(inputPanel, BorderLayout.NORTH);
    }

    private void setupMenu() {
    }

    private void setupCanvas() {
        JInternalFrame frame = new JInternalFrame("Drawing Canvas", true, true, true, true);
        frame.setSize(1920, 1080);
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

        /*
        // Slider 0: Testing Slider
        JLabel testLabel = new JLabel("Testing parameter");
        JSlider testSlider = new JSlider(0, 30);
        testSlider.setValue(2);
        testSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                test = testSlider.getValue();
                System.out.println("Test parameter: " + getTest() );
            }
        });
        sliderPanel.add(testLabel);
        sliderPanel.add(testSlider);
         */

        // Slider 1: Line Thickness
        JLabel thicknessLabel = new JLabel("Line Thickness");
        JSlider thicknessSlider = new JSlider(10, 100);
        thicknessSlider.setValue(10); // Default thickness
        thicknessSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                lineThickness = thicknessSlider.getValue();
                System.out.println("Line Thickness: " + getLineThickness() );
            }
        });
        sliderPanel.add(thicknessLabel);
        sliderPanel.add(thicknessSlider);

        // Slider 2: Zone Spread
        JLabel zoneSpreadLabel = new JLabel("Zone Spread");
        JSlider zoneSpreadSlider = new JSlider(0, 10);
        zoneSpreadSlider.setValue(2); // Default Zone Spread
        zoneSpreadSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                rpgController.setZoneSpread((int) zoneSpreadSlider.getValue());
                System.out.println("Zone Spread: " + rpgController.getZoneSpread());
            }
        });
        sliderPanel.add(zoneSpreadLabel);
        sliderPanel.add(zoneSpreadSlider);

        // Slider 3: Surrouding Weight
        JLabel surroundWeight = new JLabel("Surrounding weight");
        JSlider surroundWeightSlider = new JSlider(85, 100);
        surroundWeightSlider.setValue(90);
        surroundWeightSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                rpgController.setSurroundingWeight(surroundWeightSlider.getValue()/100.0);
                System.out.println("Surrounding weight: " + rpgController.getSurroundingWeight());
            }
        });
        sliderPanel.add(surroundWeight);
        sliderPanel.add(surroundWeightSlider);


        // Slider 2: Canvas Mutation
        JLabel mutationChanceLabel = new JLabel("Mutation Chance");
        JSlider mutationChanceSlider = new JSlider(0, 99);
        mutationChanceSlider.setValue(30);
        mutationChanceSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                rpgController.setMutationChance(mutationChanceSlider.getValue()/100.0);
                System.out.println("Mutation Chance: " + rpgController.getMutationChance());
            }
        });
        sliderPanel.add(mutationChanceLabel);
        sliderPanel.add(mutationChanceSlider);

        // Slider 5: Prop density
        JLabel propDensityLabel = new JLabel("Prop Density");
        JSlider propDensitySlider = new JSlider(0, 100);
        propDensitySlider.setValue(30);
        propDensitySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                rpgController.setPropDensity(propDensitySlider.getValue()/100.0);
                System.out.println("Prop Density: " + rpgController.getPropDensity());
            }
        });

        sliderPanel.add(propDensityLabel);
        sliderPanel.add(propDensitySlider);

        //setTest(testSlider.getValue());
        rpgController.setSurroundingWeight(surroundWeightSlider.getValue()/100.0);
        rpgController.setMutationChance(mutationChanceSlider.getValue()/100.0);
        rpgController.setPropDensity(propDensitySlider.getValue()/100.0);
        setLineThickness(thicknessSlider.getValue());
        rpgController.setZoneSpread((int) zoneSpreadSlider.getValue());

        add(sliderPanel, BorderLayout.SOUTH);
    }

    private void setupButtons() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 1));

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
                        zones.clear();
                        zones = rpgController.generateZones(getCanvasImage());
                        generateImage(rpgController.getMatRCopy(),rpgController.getMatGCopy(),rpgController.getMatBCopy(),"Generated Zones");
                    }
                }
        );
        buttonPanel.add(generateZonesButton);

        //Creating and adding third button to the button panel
        JButton saveCurrentZonesButton = new JButton("Save Zone Map");
        saveCurrentZonesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Define the target directory
                    String userHome = System.getProperty("user.home");
                    String dirPath = Paths.get(userHome, "OneDrive", "Documentos", "RPGMapZones").toString();
                    File dir = new File(dirPath);

                    // Create directory if it doesn't exist
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    // Get the zone matrices from the controller
                    int[][] matR = rpgController.getMatRCopy();
                    int[][] matG = rpgController.getMatGCopy();
                    int[][] matB = rpgController.getMatBCopy();

                    if (matR != null && matG != null && matB != null) {
                        // Create BufferedImage from zone matrices
                        int[] pixels = new int[matR.length * matR[0].length * 3];
                        BufferedImage image = new BufferedImage(matR[0].length, matR.length, BufferedImage.TYPE_INT_RGB);
                        WritableRaster raster = image.getRaster();
                        int pos = 0;
                        for (int i = 0; i < matR.length; i++) {
                            for (int j = 0; j < matR[0].length; j++) {
                                pixels[pos] = matR[i][j];
                                pixels[pos + 1] = matG[i][j];
                                pixels[pos + 2] = matB[i][j];
                                pos += 3;
                            }
                        }
                        raster.setPixels(0, 0, matR[0].length, matR.length, pixels);

                        // Generate filename with timestamp
                        String fileName = "zone_map_" + System.currentTimeMillis() + ".png";
                        File outputFile = new File(dir, fileName);

                        // Save the image
                        ImageIO.write(image, "png", outputFile);

                        JOptionPane.showMessageDialog(null, "Zone map saved successfully to: " + outputFile.getAbsolutePath());
                    } else {
                        JOptionPane.showMessageDialog(null, "No zone data available. Please generate zones first.");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error saving zone map: " + ex.getMessage());
                }
            }
        });
        buttonPanel.add(saveCurrentZonesButton);

        //Creating and adding fourth button to the button panel
        JButton openZoneMapButton = new JButton("Open Zone Map");
        openZoneMapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO The source image to be copied to canvas instead of open it in a new window, or maybe open in a new window but copy it to mat
                        int result = fileChooser.showOpenDialog(null);
                        if (result == JFileChooser.CANCEL_OPTION) {
                            return;
                        }else{
                            createNewCanvas();
                        }

                        path = fileChooser.getSelectedFile().getAbsolutePath();

                        JInternalFrame frame = new JInternalFrame("Source Image", true, true, true, true);
                        Container container = frame.getContentPane();
                        MyJPanel panel = new MyJPanel();
                        container.add(panel, BorderLayout.CENTER);

                        frame.pack();
                        theDesktop.add(frame);
                        frame.setVisible(true);
                    }
        });
        buttonPanel.add(openZoneMapButton);

        //Creating and adding fifth button to the button panel
        JButton createMapButton = new JButton("Create Map");
        createMapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rpgController.texturizeZone(zones, texturesMenu.getSelectedItem().toString());
                //generateImage(rpgController.getMatRCopy(),rpgController.getMatGCopy(),rpgController.getMatBCopy(),"Generated Zones");
                //TODO using the zoneR zoneG zoneB its supposed to pick the assets from a folder and put them into the image
            }
        });
        buttonPanel.add(createMapButton);

        //Creating and adding sixth button to the button panel
        JButton saveMapButton = new JButton("Save Map");
        saveMapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO save the generated map
            }
        });
        buttonPanel.add(saveMapButton);

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
        if (lastGeneratedZonesFrame != null) { lastGeneratedZonesFrame.dispose(); }

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
                case "Grassland" -> Color.GREEN;
                case "Water" -> Color.BLUE;
                case "Mountains" -> Color.BLACK;
                case "Desert" -> Color.YELLOW;
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