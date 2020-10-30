/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barebones;

import static barebones.ConsoleManager.printToLog;
import static barebones.ConsoleManager.throwErr;
import static barebones.FileManager.loadFile;
import static barebones.GUIManager.getMainPanel;
import static barebones.Variable.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author seanjhardy
 */
public class BareBones {

    /**
     * @param args the command line arguments
     */

    private static GUIManager frame;
    
    private static ArrayList<String> operators = new ArrayList<>();
    private static HashMap<String, String[][]> functions = new HashMap<>();
    
    private static boolean programRunning = false;
    private static boolean programCrashed = false;
    private static String instruction = "";

    public static void main(String[] args) throws IOException{
        frame = new GUIManager();
        
        //create list of operators
        String[] operatorList = {"+","-","*","/","^","AND", "EQL", "NOT", "OR", "XOR", "NAND", ""};
        for(String operator : operatorList){
          operators.add(operator);
        }
    }

    public static void runFile(String filename){
        String source = loadFile(filename);
        String[][] instructionSet = getInstructionSet(source);

        //reset variables
        resetVariables();
        ArrayList<String[]> location = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        //execute program
        programRunning = true;
        programCrashed = false;
        //find all functions
        parseFunctions(instructionSet);
        
        //if there is no main method, throw an error
        String returnValue = "null";
        if(!functions.containsKey("main")){
          throwErr("FATAL ERROR: No Main Method", location);
        }else{
          returnValue = parse(functions.get("main"), location);
        }
        
        //calculate execution time
        long durationInMillis = System.currentTimeMillis() - startTime;
        long second = (durationInMillis / 1000) % 60;
        long minute = (durationInMillis / (1000 * 60)) % 60;
        long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

        //print success/error message
        String time = String.format("%02d:%02d:%02d", hour, minute, second);
        if(programCrashed){
            printToLog("BUILD FAILED. TIME: " + time, false);
        }else{
            printToLog("BUILD COMPLETED. TIME: " + time, false);
            printToLog(filename + " RETURNED: " + returnValue, false);
        }
        programRunning = false;
    }
    
    public static void parseFunctions(String[][] instructions){
        for(int curInstr = 0; curInstr < instructions.length && programRunning; curInstr++){
          instruction = (String) instructions[curInstr][1];
          String regex = "(def) ([a-zA-Z]+) *\\(((int|bool) *([a-zA-Z]+))*( *, * ((int|bool) *([a-zA-Z]+))+)* *\\) *\\{";
          //find function definitions
          Matcher matcher = Pattern.compile(regex).matcher(instruction);
          while(matcher.find()){
            int[] startEnd = getBlockStartEnd(instructions, curInstr);
            String functionName = matcher.group(2);
            //create a subarray with instructions from the function
            String[][] functionInstructions = new String[startEnd[1] - (startEnd[0] + 1)][2];
            System.arraycopy(instructions, startEnd[0] + 1, 
                    functionInstructions, 0, functionInstructions.length);    
            //add function definition to functions
            functions.put(functionName, functionInstructions);
          }
        }
    }
    
    public static String parse(String[][] instructions, ArrayList<String[]> location){
        for(int curInstr = 0; curInstr < instructions.length && programRunning; curInstr++){
            //get the line number and current instruction to be executed
            String lineNum = instructions[curInstr][0];
            instruction = (String) instructions[curInstr][1];
            
            System.out.println(instruction);
            
            //highlight the current line
            getMainPanel().highlightLine(Integer.parseInt(lineNum));
            
            //create variable
            if(instruction.startsWith("set ")){
                setVariable(instruction, location, lineNum);
            }
            //increment the variable
            else if(instruction.startsWith("incr ")){
                incrVariable(instruction, location, lineNum);
            }
            //decrement the variable
            else if(instruction.startsWith("decr ")){
                decrVariable(instruction, location, lineNum);
            }
            
            //while loop
            else if(instruction.startsWith("while ")){
                int[] expressionStartEnd = getExpressionStartEnd(instruction);
                if(expressionStartEnd[0] == -1 || expressionStartEnd[1] == -1){
                  location.add(new String[]{instruction, lineNum});
                  throwErr("SYNTAX ERROR: Invalid expression", location);
                }
                String whileExpression = instruction.substring(expressionStartEnd[0], expressionStartEnd[1]);
                int[] whileStartEnd = getBlockStartEnd(instructions, curInstr);
                
                String expressionResult = parseExpression(whileExpression);
                if(expressionResult.equals("null")){
                  location.add(new String[]{instruction, lineNum});
                  throwErr("SYNTAX ERROR: Invalid expression", location);
                  return "null";
                }
                while(expressionResult.equals("true")){
                  //highlight the line
                  getMainPanel().highlightLine(Integer.parseInt(lineNum)-1);
                  //generate a list containing the instructions within the loop
                  String[][] whileInstructions = new String[whileStartEnd[1] - (whileStartEnd[0] + 1)][2];
                  System.arraycopy(instructions, whileStartEnd[0] + 1, whileInstructions, 0, whileInstructions.length);
                  //recursively call parse on those instruction
                  parse(whileInstructions, location);
                  expressionResult = parseExpression(whileExpression);
                }
                //set the pointer to the end of the while loop
                curInstr = whileStartEnd[1];
            }
            
            //if statement
            else if(instruction.startsWith("if ")){
                ArrayList<Object[]> cases = getEndIfStatement(instructions, curInstr);
                boolean caseFound = false;
                for(int i = 0; i < cases.size() && !caseFound; i++){
                    Object[] currentCase = cases.get(i);
                    String expression = (String) currentCase[0];
                    int caseStart = (int) currentCase[1];
                    String expressionValue = parseExpression(expression);
                    if(expressionValue.equals("null")){
                      location.add(new String[]{expression, Integer.toString(caseStart + 1)});
                      throwErr("SYNTAX ERROR: Invalid expression", location);
                      programRunning = false;
                    }
                    //if the case evaluates to true, jump to the given line
                    if(expressionValue.equals("true")){
                        String[][] conditionalInstructions = new String[((int)cases.get(i+1)[1] - 1) - caseStart][2];
                        System.arraycopy(instructions, caseStart, conditionalInstructions, 0, conditionalInstructions.length);
                        //parse the if loop
                        parse(conditionalInstructions, location);
                        
                        curInstr = (int) cases.get(cases.size() - 1)[1]-1;
                        System.out.println(curInstr + " " + instructions.length);
                        caseFound = true;
                    }
                }
            }
            //return value
            else if(instruction.startsWith("return ")){
              int[] expressionStartEnd = getExpressionStartEnd(instruction);
              if(expressionStartEnd[0] == -1 || expressionStartEnd[1] == -1){
                location.add(new String[]{instruction, lineNum});
                throwErr("SYNTAX ERROR: Invalid expression", location);
              }
              String whileExpression = instruction.substring(expressionStartEnd[0], expressionStartEnd[1]);
              return parseExpression(whileExpression);
            }
            
            //Null case
            else if(instruction.length() != 0){
                location.add(new String[]{instruction, lineNum});
                throwErr("SYNTAX ERROR: Invalid Instruction", location);
                programRunning = false;
            }
        }
        getMainPanel().updateDebugger();
        return "null";
    }
    

    public static String parseExpression(String expression){
      String buffer = "";
      String operator = "";
      ArrayList<Variable> operands = new ArrayList<>();
      for(int i = 0; i < expression.length(); i++){
        String character = Character.toString(expression.charAt(i));
        buffer += character;
        //get the character
        if(character.equals("(")){
          int[] startEnd = getExpressionStartEnd(expression);
          String replace = expression.substring(startEnd[0], startEnd[1]);
          String subExpression = expression.substring(startEnd[0]+1, startEnd[1]-1);
          String result = parseExpression(subExpression);
          if(result.equals("null")){
            return "null";
          }
          expression = expression.replace(replace, result);
          //System.out.println(expression);
          buffer = "";
          i -= 1;
        }
        if(character.equals(" ")){
          buffer = buffer.substring(0, buffer.length() - 1);
        }
        if(character.equals(" ") || i == expression.length()-1){
          if(variableExists(buffer)){
            operands.add(getVariables().get(buffer));
          }else if (operators.contains(buffer)) {
            operator = buffer;
          }else if(buffer.matches("(-?[0-9]+)|(true|false)")){//operand
            operands.add(new Variable(buffer));
          }else{
            return "null";
          }
          if(operands.size() == 2){
            //System.out.println(operands.get(0).getValue() + " " + operator + " " + operands.get(1).getValue());
            String calculatedValue = parseOperation(operands.get(0), operator, operands.get(1));
            return calculatedValue;
          }
          buffer = "";
        }
      }
      return expression;
    }
    
    public static String parseOperation(Variable operand1, String operator, Variable operand2){
      switch (operator) {
          case "+":
           if(operand1.getType().equals("int") && operand2.getType().equals("int")){
              return Integer.toString(Integer.parseInt(operand1.getValue()) + Integer.parseInt(operand2.getValue()));
            }
           break;
          case "-":
            if(operand1.getType().equals("int") && operand2.getType().equals("int")){
              return Integer.toString(Integer.parseInt(operand1.getValue()) - Integer.parseInt(operand2.getValue()));
            }
            break;
          case "*":
            if(operand1.getType().equals("int") && operand2.getType().equals("int")){
              return Integer.toString(Integer.parseInt(operand1.getValue()) * Integer.parseInt(operand2.getValue()));
            }
            break;
          case "/":
            if(operand1.getType().equals("int") && operand2.getType().equals("int")){
              return Integer.toString(Integer.parseInt(operand1.getValue()) / Integer.parseInt(operand2.getValue()));
            }
            break;
          case "^":
            if(operand1.getType().equals("int") && operand2.getType().equals("int")){
              return Integer.toString(Integer.parseInt(operand1.getValue()) ^ Integer.parseInt(operand2.getValue()));
            }
            break;
          case "EQL":
            return operand1.getValue().equals(operand2.getValue()) ? "true" : "false";
          case "AND":
            if(operand1.getType().equals("bool") && operand2.getType().equals("bool")){
              return (operand1.getValue().equals("true") && operand2.getValue().equals("true")) ? "true" : "false";
            }
            break;
          case "OR":
            if(operand1.getType().equals("bool") && operand2.getType().equals("bool")){
              return (operand1.getValue().equals("true") || operand2.getValue().equals("true")) ? "true" : "false";
            }
            break;
          case "NOT":
            return (!operand1.getValue().equals(operand2.getValue()) ? "true" : "false");
          case "XOR":
            if(operand1.getType().equals("bool") && operand2.getType().equals("bool")){
              boolean expression = (operand1.getValue().equals("true") || operand2.getValue().equals("true")) &&
                      !(operand1.getValue().equals("true") && operand2.getValue().equals("true"));
              return expression ? "true" : "false";
            }
            break;
          case "NAND":
            if(operand1.getType().equals("bool") && operand2.getType().equals("bool")){
              return !(operand1.getValue().equals("true") && operand2.getValue().equals("true")) ? "true" : "false";
            }
            break;
          default:
            break;
      }
      return "null"; // return null
    }

    public static int[] getExpressionStartEnd(String instruction){
      int start = instruction.indexOf("(");
      int end = -1;
      int currentPosition = start;
      int braceLevel = 0;
      boolean endFound = false;

      while(!endFound){
        int indexOfStartBrace = instruction.indexOf("(", currentPosition);
        int indexOfEndBrace = instruction.indexOf(")", currentPosition);
        if(indexOfEndBrace == -1){
          return new int[]{-1, -1};
        }
        if(indexOfStartBrace < indexOfEndBrace && indexOfStartBrace != -1){
          braceLevel += 1;
          currentPosition = indexOfStartBrace + 1;
        }else{
          braceLevel -= 1;
          currentPosition = indexOfEndBrace + 1;
        }
        if(braceLevel == 0){
          end = currentPosition;
          endFound = true;
        }
      }
      return new int[]{start, end};
    }
    
    public static int[] getBlockStartEnd(String[][] instructions, int lineNum){
      int start = lineNum;
      int end = -1;
      int currentPosition = start;
      int braceLevel = 0;
      boolean endFound = false;

      while(!endFound){
        String nextInstruction = instructions[currentPosition][1];
        boolean containsStartBrace = nextInstruction.contains("{");
        boolean containsEndBrace = nextInstruction.contains("}");
        if(containsStartBrace){
          braceLevel += 1;
        }else if(containsEndBrace){
          braceLevel -= 1;
        }
        if(braceLevel == 0){
          end = currentPosition;
          endFound = true;
        }
        currentPosition += 1;
        if(currentPosition == instructions.length){
          end = currentPosition - 1;
          endFound = true;
        }
      }
      return new int[]{start, end};
    }
    
    public static ArrayList<Object[]> getEndIfStatement(String[][] instructions, int curInstr){
        ArrayList<Object[]> cases = new ArrayList<>();
        String nextInstruction = "";
        boolean endFound = false;
        int braceLevel = 0;
        int x = curInstr;
        while(!endFound){
          if(x < instructions.length){
            nextInstruction = (String) instructions[x][1]; 
          }else{
            endFound = true;
          }
          boolean containsEndBrace = nextInstruction.contains("}");
          if(containsEndBrace){
            braceLevel -= 1;
          }
          if(braceLevel == 0){
            if(nextInstruction.contains("if") || nextInstruction.contains("else if")){
              int[] expressionStartEnd = getExpressionStartEnd(nextInstruction);
              cases.add(new Object[]{nextInstruction.substring(expressionStartEnd[0], expressionStartEnd[1]), x + 1});
            }else if(nextInstruction.contains("else")){
              cases.add(new Object[]{"true", x + 1});
            }else{
              cases.add(new Object[]{"true", x + 1});
              endFound = true;
            }
          }
          boolean containsStartBrace = nextInstruction.contains("{");
          if(containsStartBrace){
            braceLevel += 1;
          }
          x += 1;
        }

        return cases;
    }
    
     //returns a list of line number/instruction pairs with comments removed 
    public static String[][] getInstructionSet(String source){
      String[] rawInstructions = source.split("\n");
      String[][] instructions = new String[rawInstructions.length][2];
      int lineNum = 0;
      for(int i = 0; i < rawInstructions.length; i++){
        String[] lineNoComments = rawInstructions[i].split("#");
        rawInstructions[i] = lineNoComments[0] + System.getProperty("line.separator");
        rawInstructions[i] = rawInstructions[i].replace("\r", "");
        String currentInstruction = rawInstructions[i].trim();
        instructions[i] = new String[]{Integer.toString(lineNum + 1), currentInstruction};
        lineNum += 1;
      }
      return instructions;
    }
    
    public static void setProgramRunning(boolean running){
        programRunning = running;
    }
    
    public static void setProgramCrashed(boolean crashed){
        programCrashed = crashed;
        if(programCrashed) programRunning = false;
    }
    
    public static boolean isProgramRunning(){
        return programRunning;
    }
    
    public static String getCurrentInstruction(){
        return instruction;
    }

    public static ArrayList<String> getOperators(){
      return operators;
    }
}