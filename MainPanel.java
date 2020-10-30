/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barebones;

import static barebones.BareBones.getCurrentInstruction;
import static barebones.BareBones.runFile;
import static barebones.ConsoleManager.throwErr;
import static barebones.FileManager.getCD;
import static barebones.FileManager.loadFile;
import static barebones.FileManager.saveFile;
import static barebones.FileManager.setCD;
import static barebones.GUIManager.getColour;
import static barebones.GUIManager.getDefaultFont;
import static barebones.Variable.getVariables;
import java.awt.Color;
import static java.awt.Color.WHITE;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 *
 * @author seanjhardy
 */
public final class MainPanel extends JPanel{
    
    private GUIManager parent;
    private MainUI mainUI;
    private ConsoleManager console;
    
    private Rectangle frameBounds;
    
    private ButtonVariable openFile = new ButtonVariable(false);
    private ButtonVariable newFile = new ButtonVariable(false);
    private ButtonVariable saveFile = new ButtonVariable(false);
    private ButtonVariable runFile = new ButtonVariable(false);
    
    private ButtonVariable debugMode = new ButtonVariable(false);
    private ButtonVariable step = new ButtonVariable(false);
    private ButtonVariable exit = new ButtonVariable(false);
    
    private int lineStart, lineLength;
    
    public MainPanel(GUIManager parent){
        this.parent = parent;
        setBackground(getColour("background"));
        mainUI = new MainUI(this);
        console = new ConsoleManager((JLabel) ((JScrollPane) mainUI.getComponent("console")).getViewport().getView());
        ConsoleManager.printStartupMessage();
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        //draw background
        g.setColor(getColour("background"));
        g.fillRect(0,0,1920,1080);
        RenderComponents(g2);
        processComponentUpdate();
    }
    
    public void RenderComponents(Graphics2D g2){
        frameBounds = parent.getBounds();
        console.render();
        mainUI.resize(0, 0, (int)frameBounds.getWidth(), (int)frameBounds.getHeight());
        mainUI.render(g2);
        
    }
    
    public void updateDebugger(){
      HashMap<String, Variable> variables = getVariables();
      JLabel debugger = (JLabel) mainUI.getComponent("debugInfo");
      String debugText = "<html><span>Current Instruction: <br>";
      debugText += getCurrentInstruction() + "<br>";
      debugText += "VARIABLES:<br>";
      
      for(Map.Entry<String, Variable> entry : variables.entrySet()){
        String key = entry.getKey();
        Variable variable = entry.getValue();
        debugText += (key+" : " + variable.getValue() + "<br>");
      }
      
      debugText += "</span></html>";
      debugger.setText(debugText);
    }
    
    public void highlightLine(int lineNum){
      //highlight line
      StyleContext style = StyleContext.getDefaultStyleContext();
      AttributeSet textStyle = style.addAttribute(style.getEmptySet(), StyleConstants.Background, getColour("noColour"));
      getSource().getStyledDocument().setCharacterAttributes(this.lineStart, this.lineLength, textStyle, false);
      
      if(lineNum == -1){
        return;
      }
      
      String text = getSource().getText();
      String[] splitString = text.split("\n");
      this.lineStart = 0;
      for(int i = 0; i < splitString.length; i++){
        if(i == lineNum){
          lineLength = splitString[i].length();
        }else if(i < lineNum){
          lineStart += splitString[i].length()+1;
        }
      }
      
      textStyle = style.addAttribute(style.getEmptySet(), StyleConstants.Background, getColour("highlightedLine"));
      getSource().getStyledDocument().setCharacterAttributes(this.lineStart, this.lineLength, textStyle, false);
      
      updateDebugger();
      mainUI.getComponent("debugInfo").paintImmediately(mainUI.getComponent("debugInfo").bounds());
      getSource().paintImmediately(getSource().bounds());
      try {
        Thread.sleep(50);
      } catch (InterruptedException ex) {
        Logger.getLogger(BareBones.class.getName()).
                log(Level.SEVERE, null, ex);
      }
    }
    
    public void processComponentUpdate(){
      if(runFile.getValue() && !runFile.getLastState()){
        runFile.setLastState(runFile.getValue());
        saveHelper(getFilename(), getSource().getText(), fileSaved());
        if(fileSaved()){
          runFile(getFilename());
        }
      }
      if(!BareBones.isProgramRunning()){
        runFile.setValue(false);
      }
      
      if(step.getValue()){
        System.out.println("yo");
        step.setValue(false);
      }
      if(!runFile.getValue() && BareBones.isProgramRunning()){
        throwErr("User Termination", new ArrayList<>());
      }
      if(newFile.getValue()){
        newFile.setValue(false);
        saveHelper(getFilename(), getSource().getText(), fileSaved());
        ((JTextField)mainUI.getComponent("filename")).setText("filename");
        ((JTextPane)((JScrollPane)mainUI.getComponent("source"))
              .getViewport().getView()).setText("");
      }
      if(saveFile.getValue()){
        saveFile.setValue(false);
        saveHelper(getFilename(), getSource().getText(), fileSaved());
      }
      if(exit.getValue()){
        exit.setValue(false);
        System.exit(0);
      }
      if(openFile.getValue()){
        openFile.setValue(false);
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text" , "txt");
        fc.setFileFilter(filter);
        fc.setCurrentDirectory(getCD());
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION){
            File file = fc.getSelectedFile();
            setCD(file.getParentFile());
            String filename = file.getName().substring(0, file.getName().length() - 4);
            ((JTextField)mainUI.getComponent("filename")).setText(filename);
            ((JTextPane)((JScrollPane)mainUI.getComponent("source"))
              .getViewport().getView()).setText(loadFile(filename));
        }
      }
    }
    
    public boolean fileSaved(){
      File f = new File(getCD() + "\\" + getFilename() + ".txt");
      return (f.exists() && !f.isDirectory());
    }
    
    public void saveHelper(String filename, String source, boolean savedBefore){
      if(savedBefore){
        if(!loadFile(filename).equals(source)){
          saveFile(filename, source);
        }
      }else{
        int result = JOptionPane.showConfirmDialog(parent,
              "Do you want to save the current file?", 
              "Save file",
              JOptionPane.OK_CANCEL_OPTION,
              JOptionPane.QUESTION_MESSAGE);
        switch (result) {
          case JOptionPane.OK_OPTION:
            saveFile(filename, source);
          case JOptionPane.CANCEL_OPTION:
            break;
          default:
            break;
        }
      }
    }

    public JTextPane getSource(){
      return (JTextPane)((JScrollPane)mainUI.getComponent("source"))
              .getViewport().getView();
    }
    public String getFilename(){
      return ((JTextField)mainUI.getComponent("filename")).getText();
    }
    
    
    public ButtonVariable getOpenFileBool(){
      return openFile;
    }
    public ButtonVariable getNewFileBool(){
      return newFile;
    }
    public ButtonVariable getSaveFileBool(){
      return saveFile;
    }
    public ButtonVariable getRunBool(){
      return runFile;
    }
    
    public ButtonVariable getDebugModeBool(){
      return debugMode;
    }
    
    public ButtonVariable getStepBool(){
      return step;
    }
    
    public ButtonVariable getExitBool(){
      return exit;
    }
    
    
    
   
}
