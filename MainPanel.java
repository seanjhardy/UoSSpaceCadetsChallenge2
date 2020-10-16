/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barebones;

import static barebones.BareBones.getCD;
import static barebones.BareBones.loadFile;
import static barebones.BareBones.runFile;
import static barebones.BareBones.saveFile;
import static barebones.BareBones.setCD;
import static barebones.GUIManager.getColour;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author seanjhardy
 */
public final class MainPanel extends JPanel{
    
    private GUIManager parent;
    private MainUI mainUI;
    
    private Rectangle frameBounds;
    
    private ButtonVariable openFile = new ButtonVariable(false);
    private ButtonVariable newFile = new ButtonVariable(false);
    private ButtonVariable saveFile = new ButtonVariable(false);
    private ButtonVariable runFile = new ButtonVariable(false);
    
    private ButtonVariable debugMode = new ButtonVariable(false);
    private ButtonVariable exit = new ButtonVariable(false);
    
    private ArrayList<String> consoleText = new ArrayList<>();
    private int consoleLineNum = 0;
    
    public MainPanel(GUIManager parent){
        this.parent = parent;
        setBackground(getColour("background"));
        createWidgets();
        printToLog("BareBones STARTING CONSOLE", false);
        printToLog("BareBones V1.0.0", false);
        printToLog("By Seanjhardy", false);
        printToLog("===============", false);
    }
    
    public final void createWidgets(){
        mainUI = new MainUI(this);
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
        String newText = consoleText.stream().collect(Collectors.joining("<br>"));
        getConsole().setText("<html><body style=\"text-align: justify;  text-justify: inter-word;\">" + 
                newText + "</body></html>");
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() { 
            JScrollPane scrollPane = ((JScrollPane)mainUI.getComponent("console"));
            JScrollBar scroll = scrollPane.getVerticalScrollBar();
            scroll.setValue(scroll.getMaximum());
          }
        });
        mainUI.resize(0, 0, (int)frameBounds.getWidth(), (int)frameBounds.getHeight());
        mainUI.render(g2);
        
    }
    
    public void parseConsoleInput(String input){
      printToLog(input, false);
    }
    
    public void throwErr(String errorType, ArrayList<String[]> location){
      printToLog(errorType, true);
      String tab = "&#9";
      for(int i = 0; i < location.size(); i++){
        String tabs = String.join("", Collections.nCopies(i, tab));
        String[] loc = location.get(i);
        printToLog(tabs + "at >>" + loc[0] + " on line " + loc[1], true);
      }
    }
    
    public void printToLog(String log, boolean err){
      DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
      Calendar cal = Calendar.getInstance();
      String time = dateFormat.format(cal.getTime());
      consoleText.add(time + "> " + log);
      consoleLineNum += 1;
    }
    
    public void updateDebugger(){
      HashMap<String, Integer> variables = BareBones.getVariables();
      JLabel debugger = (JLabel) mainUI.getComponent("debugInfo");
      String debugText = "<html><span>VARIABLES:<br>";
      
      for(Map.Entry<String, Integer> entry : variables.entrySet()){
        String key = entry.getKey();
        Integer value = entry.getValue();
        debugText += (key+" : "+value + "<br>");
      }
      
      debugText += "</span></html>";
      debugger.setText(debugText);
    }
    
    public void processComponentUpdate(){
      if(runFile.getValue()){
        runFile.setValue(false);
        saveHelper(getFilename(), getSource(), fileSaved());
        if(fileSaved()){
          runFile(getFilename());
        }
      }
      if(newFile.getValue()){
        newFile.setValue(false);
        saveHelper(getFilename(), getSource(), fileSaved());
        ((JTextField)mainUI.getComponent("filename")).setText("");
        ((JTextPane)((JScrollPane)mainUI.getComponent("source"))
              .getViewport().getView()).setText("");
      }
      if(saveFile.getValue()){
        saveFile.setValue(false);
        saveHelper(getFilename(), getSource(), fileSaved());
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
    

    public String getSource(){
      return ((JTextPane)((JScrollPane)mainUI.getComponent("source"))
              .getViewport().getView()).getText();
    }
    public String getFilename(){
      return ((JTextField)mainUI.getComponent("filename")).getText();
    }
    
    public JLabel getConsole(){
      return ((JLabel)((JScrollPane)mainUI.getComponent("console")).getViewport().getView());
    }
    
    public int getConsoleLastLineNum(){
      return consoleLineNum;
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
    
    public ButtonVariable getDebuggerBool(){
      return debugMode;
    }
    
    public ButtonVariable getExitBool(){
      return exit;
    }
    
    
    
   
}
