/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barebones;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author s-hardy
 */
public final class GUIManager extends JFrame{
    //panels
    private static CardLayout layoutController;
    private static JPanel panelController;
    private static MainPanel mainPanel;
    
    private static String currentPanel;
    
    //visuals and sprites
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static final File IMAGE_DIRECTORY = new File("assets");
    private static HashMap<String, BufferedImage> images;
    private static HashMap<String, Color> colourScheme;
    
    //custom variables
    private static String font;
        

    //initialisation
    public GUIManager(){
        super("BareBones");
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | 
                IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(GUIManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //load sprites
        loadColourScheme();
        loadImages();
        createPanels();
        setFrameProperties();
    }
    
    public void createPanels(){ 
        mainPanel = new MainPanel(this);
        
        layoutController = new CardLayout();
        panelController = new JPanel(layoutController);
        
        //This componentListener allows the panel to
        //dynamically resize every widget when the frame changes shape
        panelController.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                switch (currentPanel) {
                    case "mainPanel":
                        mainPanel.repaint();
                        break;
                    default:
                        break;
                }
            }
        });
        //add the panels to the cardlayout
        layoutController.addLayoutComponent(mainPanel, "mainPanel");
        panelController.add(mainPanel);
        //add the cardlayout panel to the main frame
        add(panelController);
    }
    
    public void setFrameProperties(){
        setCurrentPanel("mainPanel");
        
        setSize(1000, 800);
        setMinimumSize(new Dimension(750,1000));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setExtendedState(JFrame.MAXIMIZED_BOTH);
        //setSize((int)screenSize.getWidth(), (int)screenSize.getHeight());
        setBackground(getColour("background"));
        setVisible(true); 
    }
    
    //image/colour manipulation
    public static Color brightness(Color c, double i){
        int R = (int) Math.max(Math.min(c.getRed()*i,255),0);
        int G = (int) Math.max(Math.min(c.getGreen()*i,255),0);
        int B = (int) Math.max(Math.min(c.getBlue()*i,255),0);
        return new Color(R,G,B,c.getAlpha());
    }
    
    public static Color addAlpha(Color c, int a){
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }
    
    //getter methods
    public static Dimension getScreenSize(){
        return screenSize;
    }
    
    public static void loadImages(){
        images = new HashMap<>();
        loadImagesFromDirectory(IMAGE_DIRECTORY);
    }
    
    public static void loadImagesFromDirectory(File directory){
        try {
            for (File file : directory.listFiles()){
                //create a variable to store the sprite name
                if (file.isDirectory() ) {
                    loadImagesFromDirectory(file);
                }else{
                    String name = file.getName();
                    name = name.substring(0, name.lastIndexOf('.'));
                    BufferedImage image = ImageIO.read(file);
                    //add name image pair to the images hashmap
                    images.put(name, image);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GUIManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void loadColourScheme(){
        colourScheme = new HashMap<>();
        //Color.decode("")
        colourScheme.put("default", new Color(255,0,230));
        colourScheme.put("background", Color.decode("#1a1e24"));
        colourScheme.put("consoleBackground", Color.decode("#0d1014"));
        colourScheme.put("consoleForeground", Color.decode("#a4c3ed"));
        colourScheme.put("banner", Color.decode("#43506e"));
        colourScheme.put("button", Color.decode("#43506e"));
        
        colourScheme.put("highlightedLine", Color.decode("#54e858"));
        
        colourScheme.put("comments", Color.decode("#777777"));
        colourScheme.put("style1", Color.decode("#ffffff"));
        colourScheme.put("style2", Color.decode("#ff264a"));
        colourScheme.put("style3", Color.decode("#50afde"));
        colourScheme.put("style4", Color.decode("#7eff61"));
        colourScheme.put("style5", Color.decode("#cc6ef5"));
        
        colourScheme.put("clear", new Color(6, 65, 66));
        colourScheme.put("incr", new Color(6, 65, 66));
        colourScheme.put("decr", new Color(6, 65, 66));
        colourScheme.put("while", new Color(179, 83, 64));
        colourScheme.put("variable", new Color(6, 65, 66));
        colourScheme.put("number", new Color(6, 65, 66));
        
        colourScheme.put("noColour", new Color(0, 0, 0, 0));

    }
    
    public static BufferedImage getImage(String imageName){
        if (images.containsKey(imageName)) {
            return images.get(imageName);
        }
        return images.get("DefaultTexture");
    }
    
    public static Color getColour(String colourName){
        if (colourScheme.containsKey(colourName)) {
            return colourScheme.get(colourName);
        }
        return colourScheme.get("default");
    }
    
    public static String getDefaultFont(){
        return font;
    }
    
    //setter methods
    public static void setCurrentPanel(String panel){
        currentPanel = panel;
        layoutController.show(panelController, panel);
    }  
    
    public static MainPanel getMainPanel(){
      return mainPanel;
    }
}

