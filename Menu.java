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
import java.awt.Color;
import static java.awt.Color.WHITE;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

/**
 *
 * @author s-hardy
 */
public class Menu{
    
    protected MainPanel parent;
    protected int height, width;
    protected int startX, startY;
    protected final HashMap<String, JComponent> components = new HashMap<>();
    
    public Menu(MainPanel parent){
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
    
    public void addComponent(String name, JComponent comp){
      components.put(name, comp);
    }
    public final JComponent getComponent(String name){
        return components.get(name);
    }
    
    public boolean isMouseOver(int mouseX, int mouseY){
        return (mouseX > startX && mouseX < startX + width && mouseY > startY && mouseY < startY + height);
    }
}
