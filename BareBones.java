/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barebones;

import static barebones.GUIManager.getMainPanel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author seanjhardy
 */
public class BareBones {

  /**
   * @param args the command line arguments
   */
  
  private static ArrayList<String> keywords = new ArrayList<>();
  private static File cd = new File("saves");
  private static GUIManager frame;
  
  private static String[] operatorList = {"EQL", "AND", "NOT", "OR", "NAND", "XOR", "NOT"};
  private static ArrayList<String> operators = new ArrayList<>();
  private static HashMap<String, Integer> variables = new HashMap<>();
  
  private static boolean programRunning = false;
  private static int curInstr = -1;
  private static long startTime = 0;
  //Stores the current location of the error
  private static ArrayList<String[]> location;
  //stores the while loop level
  private static ArrayList<Integer> whileLoops;
  //stores the currently loaded instruction set
  private static Object[][] instructions;
  private static String instruction;
  //stores the current line to be highlighted
  private static int lineNum, startIndex;
  
  public static void main(String[] args) throws IOException{
    frame = new GUIManager();
    //add the operators into the arraylist
    for(String s : operatorList){
      operators.add(s);
    }
    keywords.add("while");
    keywords.add("clear");
    keywords.add("incr");
    keywords.add("decr");
  }
  
  public static void saveFile(String filename, String source){
    PrintWriter out = null;
    try {
      //write the contents of the file to filename.txt
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
      //return the file given by filename, removing newlines and adding
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
    instructions = new Object[rawInstructions.length][2];
    int currentLine = 1;
    int lineIndex = 0;
    for(int i = 0; i < rawInstructions.length; i++){
      if(rawInstructions[i].contains("\n")){
        lineNum += 1;
        rawInstructions[i] = rawInstructions[i].replace("\r", "");
        rawInstructions[i] = rawInstructions[i].replace("\n", "");
      }
      instructions[i] = new Object[]{lineNum, lineIndex, rawInstructions[i].trim()};
      currentLine += ((String)rawInstructions[i]).length()+2;
    }
    
    //reset variables
    programRunning = true;
    variables = new HashMap();
    location = new ArrayList<>();
    whileLoops = new ArrayList<>();
    curInstr = 0;
    startTime = System.currentTimeMillis();
  }
  
  public static void parse(){
    //get the current instruction to be executed
    lineNum = (int) instructions[curInstr][0];
    startIndex = (int) instructions[curInstr][1];
    instruction = (String) instructions[curInstr][2];
    
    //highlight the current line
    int nextInstrStart = (curInstr+1 < instructions.length) ? (Integer)instructions[curInstr+1][1] : instruction.length();
    getMainPanel().highlightLine(lineNum, startIndex, nextInstrStart-startIndex);

    //ask user for new variable
    if(instruction.startsWith("assign ")){
      String variableName = instruction.substring(6);
      //Test if the variable name is undefined
      if(variableName.split(" ").length == 0 || variableName.length() == 0){
          location.add(new String[]{instruction, Integer.toString(lineNum)});
          getMainPanel().throwErr("SYNTAX ERROR: Invalid variable name", location);
          programRunning = false;
      }
      //Test if the variable name is invalid
      else if(keywords.contains(variableName)){
        location.add(new String[]{instruction, Integer.toString(lineNum)});
        getMainPanel().throwErr("SYNTAX ERROR: Variable name cannot be keyword", location);
        programRunning = false;
      } else{
        getMainPanel().printToLog("", false);
        variables.put(variableName, 0);
      }
    }
    //create a new variable
    else if(instruction.startsWith("clear ")){
      String variableName = instruction.substring(6);
      //Test if the variable name is undefined
      if(variableName.split(" ").length == 0 || variableName.length() == 0){
          location.add(new String[]{instruction, Integer.toString(lineNum)});
          getMainPanel().throwErr("SYNTAX ERROR: Invalid variable name", location);
          programRunning = false;
      }
      //Test if the variable name is invalid
      else if(keywords.contains(variableName)){
        location.add(new String[]{instruction, Integer.toString(lineNum)});
        getMainPanel().throwErr("SYNTAX ERROR: Variable name cannot be keyword", location);
        programRunning = false;
      }else{
        variables.put(variableName, 0);
      }
    }
    //increment the variable
    else if(instruction.startsWith("incr ")){
      String variableName = instruction.substring(5);
      
      //Test if the variable name is invalid
      if(!variables.containsKey(variableName)){
        location.add(new String[]{instruction, Integer.toString(lineNum)});
        getMainPanel().throwErr("SYNTAX ERROR: Variable " + variableName + " not found", location);
        programRunning = false;
      }else{
        variables.put(variableName, variables.get(variableName)+1);
      }

    }else if(instruction.startsWith("decr ")){
      String variableName = instruction.substring(5);
      
      //Test if the variable name is invalid
      if(!variables.containsKey(variableName)){
        location.add(new String[]{instruction, Integer.toString(lineNum)});
        getMainPanel().throwErr("SYNTAX ERROR: Variable " + variableName + " not found", location);
        programRunning = false;
      }else{
        variables.put(variableName, variables.get(variableName)-1);
      }
    }else if(instruction.startsWith("while ")){
      String whileExpression = instruction.substring(6);
      
      if(!parseExpression(whileExpression)){
        curInstr = getEndStatement();
      }else{
        whileLoops.add(curInstr);
      }
      //pass the new location to future errors
      location.add(new String[]{"while", Integer.toString(lineNum)});
      
    }else if(instruction.startsWith("end") && (location.get(location.size()-1)[0]).equals("while")){
      curInstr = whileLoops.get(whileLoops.size() - 1) - 1;
      whileLoops.remove(whileLoops.size() - 1);
    }else if(instruction.startsWith("if ")){
      ArrayList<Object[]> cases = getEndIfStatement();
      boolean caseFound = false;
      for(Object[] c : cases){
        if(parseExpression((String) c[0]) && !caseFound){
          curInstr = (int) c[1];
          caseFound = true;
        }
      }
      //pass the new location to future errors
      location.add(new String[]{"if", Integer.toString(lineNum)});
      
    }else if(instruction.startsWith("end") && (location.get(location.size()-1)[0]).equals("if")){
    }else if(instruction.startsWith("else")){
      curInstr = getEndStatement();
    }else if(instruction.length() != 0){
      location.add(new String[]{instruction, Integer.toString(lineNum)});
      getMainPanel().throwErr("SYNTAX ERROR: Invalid Instruction", location);
      programRunning = false;
    }
    
    getMainPanel().updateDebugger();
    curInstr += 1;
    if(curInstr >= instructions.length || programRunning == false){
      curInstr = -1;
      //calculate execution time
      long durationInMillis = System.currentTimeMillis() - startTime;
      long second = (durationInMillis / 1000) % 60;
      long minute = (durationInMillis / (1000 * 60)) % 60;
      long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

      String time = String.format("%02d:%02d:%02d", hour, minute, second);
      if(programRunning == true){
        getMainPanel().printToLog("BUILD COMPLETED. TIME: " + time, false);
      }else{
        getMainPanel().printToLog("BUILD FAILED. TIME: " + time, false);
      }
      getMainPanel().highlightLine(0, 0, 0);
      programRunning = false;
    }
  }
  
  
  public static boolean parseExpression(String expression){
    String buffer = "";
    ArrayList<Integer> expressionVariables = new ArrayList<>();
    String operator = "";
    
    for(int i = 0; i < expression.length(); i++){
      String character = Character.toString(expression.charAt(i));
      buffer += character;
      if(character.equals(" ")){
        buffer = buffer.substring(0, buffer.length() - 1);
      }
      if(character.equals(" ") || i == expression.length()-1){
        if(variables.containsKey(buffer)){
          expressionVariables.add(variables.get(buffer));
        }else if (operators.contains(buffer)) {
          operator = buffer;
        }else if(buffer.matches("-?[0-9]+")){
          expressionVariables.add(Integer.parseInt(buffer));
        }
        buffer = "";
        if(expressionVariables.size() == 2 && !operator.equals("")){
          switch (operator) {
            case "EQL":
              return Objects.equals(expressionVariables.get(0),
                      expressionVariables.get(1));
            case "AND":
              return expressionVariables.get(0) != 0 && expressionVariables.get(1) != 0;
            case "NOT":
              return !Objects.equals(expressionVariables.get(0),
                      expressionVariables.get(1));
            case "XOR":
              return (expressionVariables.get(0) != 0 || expressionVariables.get(1) != 0) && 
                      (expressionVariables.get(0) != 0 && expressionVariables.get(0) != 0);
            case "NAND":
              return !(expressionVariables.get(0) != 0 && expressionVariables.get(1) != 0);
            default:
              break;
          }
        }
      }
    }
    location.add(new String[]{instruction, Integer.toString(lineNum)});
    getMainPanel().throwErr("SYNTAX ERROR: Invalid Expression", location);
    programRunning = false;
    return false;
  }
  
  public static int getEndStatement(){
    String nextInstruction = "";
    boolean endFound = false;
    int level = 1;
    int x = curInstr;
    do{
      x += 1;
      if(x < instructions.length){
        nextInstruction = (String) instructions[x][2]; 
      }else{
        endFound = true;
      }

      if(nextInstruction.startsWith("while") || nextInstruction.startsWith("if")){
        level += 1;
      }else if(nextInstruction.startsWith("end")){
        level -= 1;
        if(level == 0){
          endFound = true;
        }
      }
    }while(!endFound);
    return x;
  }
  
  public static ArrayList<Object[]> getEndIfStatement(){
    ArrayList<Object[]> cases = new ArrayList<>();
    String nextInstruction = "";
    boolean endFound = false;
    int level = 0;
    int x = curInstr-1;
    do{
      x += 1;
      if(x < instructions.length){
        nextInstruction = (String) instructions[x][2]; 
      }else{
        endFound = true;
      }
      if(nextInstruction.startsWith("while")){
        level += 1;
      }else if(nextInstruction.startsWith("if")){
        level += 1;
        cases.add(new Object[]{nextInstruction.substring(3), x});
      }else if(nextInstruction.startsWith("else if")){
        cases.add(new Object[]{nextInstruction.substring(7), x});
      }else if(nextInstruction.startsWith("else")){
        cases.add(new Object[]{"0 EQL 0", x});
      }else if(nextInstruction.startsWith("end")){
        cases.add(new Object[]{"0 EQL 0", x});
        level -= 1;
        if(level == 0){
          endFound = true;
        }
      }
    }while(!endFound);
    
    return cases;
  }
  
  public static boolean isProgramRunning(){
    return programRunning;
  }
  
  public static void setProgramRunning(boolean running){
    programRunning = running;
  }
  
  public static void setCD(File cd){
    BareBones.cd = cd;
  }
  
  public static File getCD(){
    return cd;
  }
  
  public static HashMap<String, Integer> getVariables(){
    return variables;
  }
  public static ArrayList<String> getOperators(){
    return operators;
  }
}