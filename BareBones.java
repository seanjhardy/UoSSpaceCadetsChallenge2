/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barebones;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JFrame;

/**
 *
 * @author seanjhardy
 */
public class BareBones {

  /**
   * @param args the command line arguments
   */
  
  private static HashMap<String, Integer> variables = new HashMap<>();
  private static ArrayList<String> keywords = new ArrayList<>();
  private static File cd = new File("saves");
  private static GUIManager frame;
  
  public static void main(String[] args) throws IOException{
    frame = new GUIManager();
    keywords.add("while");
    keywords.add("clear");
    keywords.add("incr");
    keywords.add("decr");
  }
  
  public static void saveFile(String filename, String source){
    PrintWriter out = null;
    try {
      out = new PrintWriter(cd.getAbsolutePath() + "/" + filename + ".txt");
      out.println(source);
    } catch (FileNotFoundException ex) {
      Logger.getLogger(BareBones.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      out.close();
    }
  }
  
  public static String loadFile(String filename){
    try {
      File file = new File(getCD().getAbsolutePath() + "\\" + filename + ".txt");
      FileReader fr = new FileReader(file);
      BufferedReader br = new BufferedReader(fr);
      String line;
      String source = "";
      while((line = br.readLine()) != null){
        source += line + System.getProperty("line.separator");
      }
      source = source.replace("\r\n", "\n");
      fr.close();
      return source;
    } catch (IOException ex) {
      Logger.getLogger(BareBones.class.getName()).log(Level.SEVERE, null, ex);
    }
    return "";
  }
  
  public static String parseFile(String filename){
    try {
      File file = new File(getCD().getAbsolutePath() + "\\" + filename + ".txt");
      FileReader fr = new FileReader(file);
      BufferedReader br = new BufferedReader(fr);
      String line;
      String source = "";
      while((line = br.readLine()) != null){
        String[] lineNoComments = line.split("#");
        source += lineNoComments[0] + System.getProperty("line.separator");
      }
      source = source.replace("\r\n", "\n");
      fr.close();
      return source;
    } catch (IOException ex) {
      Logger.getLogger(BareBones.class.getName()).log(Level.SEVERE, null, ex);
    }
    return "";
  }
  
  public static void runFile(String filename){
    String source = parseFile(filename);
    
    String[] rawInstructions = source.split(";");
    Object[][] instructions = new Object[rawInstructions.length][2];
    int lineNum = 1;
    for(int i = 0; i < rawInstructions.length; i++){
      if(rawInstructions[i].contains("\n")){
        lineNum += 1;
        rawInstructions[i] = rawInstructions[i].replace("\r", "");
        rawInstructions[i] = rawInstructions[i].replace("\n", "");
      }
      instructions[i] = new Object[]{lineNum, rawInstructions[i]};
    }
    
    //reset variables
    variables = new HashMap();
    
    //parse text
    long startTime = System.currentTimeMillis();
    ArrayList<String[]> location = new ArrayList<>();
    parse(instructions, location);
    
    //calculate execution time
    long durationInMillis = startTime - System.currentTimeMillis();
    long second = (durationInMillis / 1000) % 60;
    long minute = (durationInMillis / (1000 * 60)) % 60;
    long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

    String time = String.format("%02d:%02d:%02d", hour, minute, second);
    frame.getMainPanel().printToLog("BUILD COMPLETED. TIME: " + time, false);
  }
  
  public static boolean parse(Object[][] instructions, ArrayList<String[]> location){
    for(int i = 0; i < instructions.length; i++){
      String instruction = ((String) instructions[i][1]).trim();
      int lineNum = (int) instructions[i][0];
      
      if(instruction.startsWith("clear ")){
        String variableName = instruction.substring(6);
        if(variableName.split(" ").length == 0 || variableName.length() == 0){
            location.add(new String[]{instruction, Integer.toString(lineNum)});
            GUIManager.getMainPanel().throwErr("SYNTAX ERROR: Invalid variable name", location);
            return false;
        }
        if(keywords.contains(variableName)){
          location.add(new String[]{instruction, Integer.toString(lineNum)});
          GUIManager.getMainPanel().throwErr("SYNTAX ERROR: Variable name cannot be keyword", location);
          return false;
        }
        variables.put(variableName, 0);
      }else if(instruction.startsWith("incr ")){
        String variableName = instruction.substring(5);
        if(!variables.containsKey(variableName)){
          location.add(new String[]{instruction, Integer.toString(lineNum)});
          GUIManager.getMainPanel().throwErr("SYNTAX ERROR: Variable " + variableName + " not found", location);
          return false;
        }
        variables.put(variableName, variables.get(variableName)+1);
        
      }else if(instruction.startsWith("decr ")){
        String variableName = instruction.substring(5);
        if(!variables.containsKey(variableName)){
          location.add(new String[]{instruction, Integer.toString(lineNum)});
          GUIManager.getMainPanel().throwErr("SYNTAX ERROR: Variable " + variableName + " not found", location);
          return false;
        }
        variables.put(variableName, variables.get(variableName)-1);
        
      }else if(instruction.startsWith("while ")){
        String whileCondition = instruction.substring(6);
        String[] formattedCondition = whileCondition.split(" ");
        if(formattedCondition.length != 4){
          location.add(new String[]{instruction, Integer.toString(lineNum)});
          GUIManager.getMainPanel().throwErr("SYNTAX ERROR: INVALID SYNTAX WRONG ARGS", location);
          return false;
        }
        String variableName = formattedCondition[0];
        String not = formattedCondition[1];
        String value = formattedCondition[2];
        int intValue = Integer.parseInt(formattedCondition[2]);
        String doVar = formattedCondition[3];
        
        if(!variables.containsKey(variableName)){
          location.add(new String[]{instruction, Integer.toString(lineNum)});
          GUIManager.getMainPanel().throwErr("SYNTAX ERROR: Variable " + variableName + " not found", location);
          return false;
        }
        
        if(!not.equals("not")){
          location.add(new String[]{instruction, Integer.toString(lineNum)});
          GUIManager.getMainPanel().throwErr("SYNTAX ERROR: INVALID SYNTAX MISSING NOT", location);
          return false;
        }
        
        if(!value.matches("[0-9]+")){
          location.add(new String[]{instruction, Integer.toString(lineNum)});
          GUIManager.getMainPanel().throwErr("SYNTAX ERROR: Value " + value + " invalid", location);
          return false;
        }
        
        if(!doVar.equals("do")){
          location.add(new String[]{instruction, Integer.toString(lineNum)});
          GUIManager.getMainPanel().throwErr("SYNTAX ERROR: INVALID SYNTAX MISSING DO", location);
          return false;
        }
        
        //pass the new location to future errors
        ArrayList<String[]> newLocation = location;
        newLocation.add(new String[]{"while loop", Integer.toString(lineNum)});
        
        String nextInstruction = "";
        boolean endFound = false;
        int x = 0;
        do{
          x += 1;
          if(i+x < instructions.length){
            nextInstruction = (String) instructions[i+x][1]; 
          }else{
            endFound = true;
          }
          
        }while(!nextInstruction.startsWith("end") && !endFound);
        
        Object[][] newInstructions = new Object[x-1][2];
        for(int z = 0; z < x-1; z++){
          newInstructions[z] = instructions[i+z+1];
          newInstructions[z][1] = ((String)newInstructions[z][1]).trim();
        }
        while(variables.get(variableName) != intValue){
          parse(newInstructions, newLocation);
        }
        i += x;
      }else if(instruction.startsWith("if")){
        if(!instruction.contains("(") || !instruction.contains(")")){
          location.add(new String[]{instruction, Integer.toString(lineNum)});
          GUIManager.getMainPanel().throwErr("SYNTAX ERROR: Invalid syntax", location);
          return false;
        }
        String expression = instruction.substring(instruction.indexOf("("), instruction.lastIndexOf(")"));
        //boolean result = parseExpression(expression);
        //if(result){
        //}
        
        
      }else if(instruction.length() != 0){
        location.add(new String[]{instruction, Integer.toString(lineNum)});
        GUIManager.getMainPanel().throwErr("SYNTAX ERROR: Invalid Instruction", location);
        return false;
      }
      GUIManager.getMainPanel().updateDebugger();
    }
    return true;
  }
  
  //public boolean parseExpression(String expression){
  //}
  
  public static void setCD(File cd){
    BareBones.cd = cd;
  }
  
  public static File getCD(){
    return cd;
  }
  
  public static HashMap<String, Integer> getVariables(){
    return variables;
  }
}