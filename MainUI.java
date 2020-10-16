/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barebones;

import static barebones.GUIManager.getColour;
import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author seanjhardy
 */
public class MainUI extends Menu{
  
    public MainUI(JPanel parent){
        super(parent);
        createComponents();
    }
    
    public final void createComponents(){
        createImageButton("load", "Load", "LoadHighlighted", "LoadHighlighted", "Load", ((MainPanel)parent).getOpenFileBool(), getColour("button")).setBorder(null);
        createImageButton("new", "New", "NewHighlighted", "NewHighlighted", "New", ((MainPanel)parent).getNewFileBool(), getColour("button")).setBorder(null);
        createImageButton("save", "Save", "SaveHighlighted", "SaveHighlighted", "Save", ((MainPanel)parent).getSaveFileBool(), getColour("button")).setBorder(null);    
        createImageButton("run", "Stop", "StopHighlighted", "RunHighlighted", "Run", ((MainPanel)parent).getRunBool(), getColour("button")).setBorder(null);    
        
        createImageButton("settings", "Settings", "SettingsHighlighted", "SettingsHighlighted", "Settings", ((MainPanel)parent).getDebuggerBool(), getColour("button")).setBorder(null);    
        createImageButton("exit", "Exit", "ExitHighlighted", "ExitHighlighted", "Exit", ((MainPanel)parent).getExitBool(), getColour("button")).setBorder(null);    
        
        JTextField filename = createTextField("filename", "filename", getColour("background"));
        filename.setForeground(new Color(200,200,200));
        filename.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                if(filename.getText().isEmpty()) {
                    filename.setText("filename");
                    filename.setForeground(new Color(200,200,200));
                }
            }
            
            @Override
            public void focusGained(FocusEvent e) {
                if(filename.getText().equals("filename")) {
                    filename.setText("");
                    filename.setForeground(new Color(255,255,255));
                }
            }
        });
        createSourceArea("source", "Code goes here...", getColour("background")); 
        createConsoleArea("console", getColour("background"));
        
        JTextField consoleInput = createTextField("consoleInput", "", getColour("background"));
        consoleInput.addKeyListener(new KeyAdapter(){
          public void keyPressed(KeyEvent evt){
            if(evt.getKeyCode() == KeyEvent.VK_ENTER){
              ((MainPanel)parent).parseConsoleInput(consoleInput.getText());
              consoleInput.setText("");
              parent.repaint();
            }
          }
        });
        
        JLabel label = createLabel("debugInfo", "DEBUGGER:", SwingConstants.LEFT, SwingConstants.TOP, 14, getColour("consoleBackground"));
        Border margin = new EmptyBorder(10,10,10,10);
        label.setBorder(new CompoundBorder(label.getBorder(), margin));
    }
    
    public void render(Graphics2D g){
        g.setColor(getColour("banner"));
        g.fillRect(0,0,width, 30);
        getComponent("load").setBounds(startX, startY, 30, 30);
        getComponent("save").setBounds(startX+30, startY, 30, 30);
        getComponent("filename").setBounds(startX+60, startY, 100, 30);
        getComponent("new").setBounds(startX+160, startY, 30, 30);
        getComponent("run").setBounds(startX+190, startY, 30, 30);
        
        getComponent("settings").setBounds(startX+width-75, startY, 30, 30);
        getComponent("exit").setBounds(startX+width-45, startY, 30, 30);
        
        //code
        int endWidth = width-35;
        if(((MainPanel)parent).getDebuggerBool().getValue()){
          endWidth -= 200;
        }
        getComponent("source").setBounds(startX+10, startY+40, endWidth, height-215);
        JTextPane source = (JTextPane) ((JScrollPane)getComponent("source")).getViewport().getView();
        source.setBounds(0,0,width-45,height-55);

        getComponent("console").setBounds(startX+10, height-165, endWidth, 80);
        JLabel console = (JLabel) ((JScrollPane)getComponent("console")).getViewport().getView();
        console.setBounds(0,0,width-45, 80);

        getComponent("consoleInput").setBounds(startX+10, height-80, endWidth, 30);
        
        getComponent("debugInfo").setBounds(endWidth+20, startY+40, width-endWidth-50, height-100);
    }
}
