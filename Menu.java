/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barebones;

import static barebones.GUIManager.getDefaultFont;
import static barebones.GUIManager.getImage;
import static barebones.GUIManager.addAlpha;
import static barebones.GUIManager.brightness;
import static barebones.GUIManager.getColour;
import java.awt.Color;
import static java.awt.Color.WHITE;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 *
 * @author s-hardy
 */
public class Menu{
    
    protected JPanel parent;
    protected int height, width;
    protected int startX, startY;
    protected long lastUpdate;
    protected final HashMap<String, JComponent> components = new HashMap<>();
    
    public Menu(JPanel parent){
        this.parent = parent;
    }
    
    public final void resize(int x, int y, int width, int height){
        this.startX = x;
        this.startY = y;
        this.width = width;
        this.height = height;
    }
    
    public final AdvancedButton createTextButton(String name, String onState, String offState, ButtonVariable bool, Color colour){
        AdvancedButton button = new AdvancedButton("");
        if(bool.getValue()){
            button.setText(onState);
        }else{
            button.setText(offState);
        }
        button.addActionListener((ActionEvent e) -> {
            if(e.getSource() == button){
                bool.setValue(!bool.getValue());
                if(bool.getValue()){
                    button.setText(onState);
                }else{
                    button.setText(offState);
                }
            }
        });
        button.setFont(new Font(getDefaultFont(), 0, 14));
        button.addBorder(6);
        button.setForeground(WHITE);
        button.setColour(colour);
        button.setFocusPainted(false);
        parent.add(button);
        components.put(name, button);
        return button;
    }
    
    public final AdvancedButton createImageButton(String name, String onImage, String hoverOn, String hoverOff, String offImage, ButtonVariable bool, Color colour){
        BufferedImage image = bool.getValue() ? getImage(onImage) : getImage(offImage);
        AdvancedButton button = new AdvancedButton(image, false);
        button.addActionListener((ActionEvent e) -> {
            if(e.getSource() == button){
                bool.setValue(!bool.getValue());
                if(hoverOn.equals("")){
                    if(bool.getValue()){
                        button.setIcon(getImage(onImage));
                    }else{
                        button.setIcon(getImage(offImage));
                    }
                }else{
                    if(bool.getValue()){
                        button.setIcon(getImage(hoverOn));
                    }else{
                        button.setIcon(getImage(hoverOff));
                    }
                }
            }
        });
        button.setFont(new Font(getDefaultFont(), 0, 14));
        button.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(addAlpha(colour, 255));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if(!hoverOn.equals("")){
                    if(bool.getValue()){
                        button.setIcon(getImage(hoverOn));
                    }else{
                        button.setIcon(getImage(hoverOff));
                    }
                }
                button.setBackground(addAlpha(colour, 200));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if(bool.getValue()){
                    button.setIcon(getImage(onImage));
                }else{
                    button.setIcon(getImage(offImage));
                }
                button.setBackground(addAlpha(colour, 100));
            }
        });
        button.addBorder(6);
        button.setForeground(WHITE);
        button.setColour(colour);
        button.setFocusPainted(false);
        
        parent.add(button);
        components.put(name, button);
        return button;
    }
    
    public final JTextField createTextField(String name, String defaultText, Color colour){
        JTextField textField = new JTextField(defaultText, SwingConstants.TOP);
        textField.setHorizontalAlignment(SwingConstants.LEFT);
        textField.setBorder(new MatteBorder(5,5,5,5, brightness(colour,2)));
        textField.setForeground(WHITE);
        textField.setCaretColor(WHITE);
        textField.setBackground(colour);
        textField.setFont(new Font(getDefaultFont(), 0, 14));
        parent.add(textField);
        components.put(name, textField);
        return textField;
    }
    
    public final JTextArea createTextArea(String name, String defaultText, Color colour){
        JTextArea textField = new JTextArea(defaultText);
        textField.setAlignmentY(SwingConstants.TOP);
        textField.setBorder(new MatteBorder(5,5,5,5, brightness(colour,2)));
        textField.setForeground(WHITE);
        textField.setCaretColor(WHITE);
        textField.setBackground(colour);
        textField.setFont(new Font(getDefaultFont(), 0, 14));
        parent.add(textField);
        components.put(name, textField);
        return textField;
    }
    
    public final JLabel createLabel(String name, String defaultValue, int xDirection, int yDirection,
            int fontSize, Color colour){
        JLabel label = new JLabel(defaultValue);
        label.setHorizontalAlignment(xDirection);
        label.setVerticalAlignment(yDirection);
        label.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                label.setBackground(brightness(colour, 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setBackground(brightness(colour, 1));
            }
        });
        label.setForeground(WHITE);
        label.setBackground(colour);
        label.setBorder(new AdvancedBevelBorder(label, 3));
        label.setFont(new Font(getDefaultFont(), 0, fontSize));
        parent.add(label);
        components.put(name, label);
        return label;
    }
    
    public final JSlider createSlider(String name, String text, int lowerBound, int upperBound, int defaultValue, int numValues, boolean paintLabels, ButtonVariable updateBool, Color background){
        JSlider slider = new JSlider(JSlider.HORIZONTAL, lowerBound,upperBound, defaultValue);
        slider.setForeground(WHITE);
        slider.setMajorTickSpacing(upperBound-lowerBound);
        slider.setMinorTickSpacing((int)(((upperBound-lowerBound)/numValues)));
        slider.setPaintTicks(true);
        slider.setSnapToTicks(true);
        slider.setPaintLabels(paintLabels);
        slider.setBackground(background);
        slider.setFocusable(false);
        slider.addChangeListener((ChangeEvent e) -> {
            updateBool.setValue(true);
        });
        slider.setFont(new Font(getDefaultFont(), 0, 14));
        parent.add(slider);
        components.put(name, slider);
        return slider;
    }
    
    public final JComboBox createComboBox(String name){
        JComboBox comboBox = new JComboBox<>();
        comboBox.setForeground(WHITE);
        comboBox.setBackground(new Color(0, 0, 0));
        comboBox.setMaximumRowCount(6);
        comboBox.setFont(new Font(getDefaultFont(), 0, 14));
        parent.add(comboBox);
        components.put(name, comboBox);
        return comboBox;
    }
    
    public final JScrollPane createDataPlot(String name){
        DataPlot panel = new DataPlot(name);
        panel.setVisible(true);
        panel.setOpaque(false);
        panel.setBackground(new Color(0,0,0,0));
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(panel);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setOpaque(false);
        scrollPane.setBackground(new Color(0,0,0,50));
        scrollPane.getViewport().setBackground(new Color(0,0,0,0));
        scrollPane.setForeground(WHITE);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setVerticalScrollBarPolicy(
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
        parent.add(scrollPane);
        components.put(name, scrollPane);
        return scrollPane;
    }
    
    public final JScrollPane createSourceArea(String name, String defaultText, Color colour){
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBackground(getColour("background"));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        JTextPane lines = new JTextPane();
        lines.setText("1");
        lines.setBackground(Color.LIGHT_GRAY);
        lines.setFont(new Font(getDefaultFont(), 0, 14));
        lines.setEditable(false);
        
        JTextPane textField = new JTextPane();
        textField.setText(defaultText);
        textField.setAlignmentY(SwingConstants.TOP);
        textField.setForeground(new Color(255,255,255));
        textField.setCaretColor(WHITE);
        textField.setBackground(colour);
        textField.setFont(new Font(getDefaultFont(), 0, 14));
        
        textField.setForeground(new Color(200,200,200));
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                if(textField.getText().isEmpty()){
                    textField.setText(defaultText);
                    textField.setForeground(new Color(200,200,200));
                }
            }
            
            @Override
            public void focusGained(FocusEvent e){
                if(textField.getText().equals(defaultText)){
                    textField.setText("");
                    textField.setForeground(new Color(255,255,255));
                }
            }
        });
        
        textField.getDocument().addDocumentListener(new DocumentListener(){
            public String getText(){
                int caretPosition = textField.getDocument().getLength();
                Element root = textField.getDocument().getDefaultRootElement();
                String text = "1" + System.getProperty("line.separator");
                for(int i = 2; i < root.getElementIndex(caretPosition) + 2; i++){
                    text += i + System.getProperty("line.separator");
                }
                return text;
            }
            
            public void updateHighlight(){
              textField.setForeground(WHITE);
              if(System.currentTimeMillis() - lastUpdate < 10){
                return;
              }
              lastUpdate = System.currentTimeMillis();
              Runnable doHighlight = new Runnable() {
                  @Override
                  public void run() {
                      StyleContext style = StyleContext.getDefaultStyleContext();
                      String input = textField.getText();
                      ArrayList<String[]> patterns = new ArrayList<>();
                      patterns.add(new String[]{"style2", "\\b(while|end|not|do)\\b"});
                      patterns.add(new String[]{"style3", "\\b(clear)\\b"});
                      patterns.add(new String[]{"style4", "\\b(incr|decr)\\b"});
                      patterns.add(new String[]{"style5", "\\b([0-9]+)\\b"});
                      patterns.add(new String[]{"comments", "#(.*)(\\n|\\b)"});
                      
                      //highlight all to white
                      AttributeSet textStyle = style.addAttribute(style.getEmptySet(), StyleConstants.Foreground, getColour("style1"));
                      textField.getStyledDocument().setCharacterAttributes(0, textField.getDocument().getLength(), textStyle, false);
                      
                      //highlight numbers
                      for(String[] pattern : patterns){
                        textStyle = style.addAttribute(style.getEmptySet(),StyleConstants.Foreground, getColour(pattern[0]));
                        Matcher m = Pattern.compile(pattern[1]).matcher(input);
                        while(m.find()){
                            textField.getStyledDocument().setCharacterAttributes(m.start(),(m.end() - m.start()),textStyle, false);
                        }
                      }
                  }
              };  
              SwingUtilities.invokeLater(doHighlight); 
            }
            
            @Override
            public void changedUpdate(DocumentEvent de) {
                lines.setText(getText());
                //updateHighlight();
            }

            @Override
            public void insertUpdate(DocumentEvent de) {
                lines.setText(getText());
                updateHighlight();
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                lines.setText(getText());
                updateHighlight();
          }
        });
        
        scrollPane.getViewport().add(textField);
        scrollPane.setRowHeaderView(lines);

        parent.add(scrollPane);
        components.put(name, scrollPane);
        return scrollPane;
    }
    
    public final JScrollPane createConsoleArea(String name, Color colour){
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBackground(getColour("consoleBackground"));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        JLabel textField = new JLabel();
        textField.setVerticalAlignment(SwingConstants.BOTTOM);
        textField.setForeground(getColour("consoleForeground"));
        textField.setBackground(colour);
        textField.setOpaque(true);
        textField.setFont(new Font(getDefaultFont(), 0, 14));
        
        scrollPane.getViewport().add(textField);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        parent.add(scrollPane);
        components.put(name, scrollPane);
        return scrollPane;
    }
    
    
    
    public final JTable createTable(String name, Color foreground, Color background){
        DefaultTableModel tableModel = new DefaultTableModel();
        JTable table = new JTable(){
            @Override
            public boolean isCellEditable(int row, int column) {                
                return false;    
            }
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column){
                Component returnComp = super.prepareRenderer(renderer, row, column);
                JComponent jc = (JComponent)returnComp;
                
                Color backgroundColor = foreground;
                Color alternateColor = new Color(backgroundColor.getRed(), backgroundColor.getBlue(), backgroundColor.getGreen(), 100);
                Color mainColor = new Color(0,0,0,100);
                Color bg = new Color(66, 103, 125, 100);
                returnComp.setForeground(WHITE);
                if (!returnComp.getBackground().equals(getSelectionBackground())){
                    bg = (row % 2 == 0 ? alternateColor : mainColor);
                    returnComp.setBackground(bg);
                }
                returnComp.setBackground(bg);
                Color newBg = new Color(bg.getRed(),bg.getGreen(),bg.getBlue(),255);
                jc.setBorder(new MatteBorder(3, 0, 3, 0, newBg));
                return returnComp;
            }
        };
        table.getTableHeader().setReorderingAllowed(false);
        table.setModel(tableModel);
        table.getTableHeader().setBackground(foreground);
        table.getTableHeader().setForeground(background);
        table.setSelectionBackground(new Color(29, 73, 163,100));
        table.setBackground(foreground);
        table.setForeground(background);
        table.setOpaque(false);
        

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setOpaque(false);
        tableScrollPane.setBackground(foreground);
        tableScrollPane.setForeground(background);
        tableScrollPane.getViewport().setBackground(foreground);
        tableScrollPane.getViewport().setForeground(background);
        tableScrollPane.setHorizontalScrollBarPolicy(
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tableScrollPane.setVerticalScrollBarPolicy(
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
        tableScrollPane.setViewportView(table);
        tableScrollPane.setVisible(true);
        parent.add(tableScrollPane);
        components.put(name, table);
        return table;
    }
    
    public final JComponent getComponent(String name){
        return components.get(name);
    }
    public boolean isMouseOver(int mouseX, int mouseY){
        return (mouseX > startX && mouseX < startX + width && mouseY > startY && mouseY < startY + height);
    }

    public class DataPlot extends JPanel{
        private String name;
        
        public DataPlot(String name){
            super();
            this.name = name;
        }
        
        @Override
        public void paintComponent(Graphics g){
            
        }
        
    }
}
