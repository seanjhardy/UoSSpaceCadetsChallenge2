/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barebones;

import static barebones.ConsoleManager.throwErr;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author seanjhardy
 */
public class Variable {
  
  private static HashMap<String, Variable> variables = new HashMap<>();
  private String type;
  private String value;
  
  public Variable(String value){
    this.value = value;
    if(value.equals("true") || value.equals("false")){
      type = "bool";
    }else{
      type = "int";
    }
  }
  
  public void setValue(String value){
    this.value = value;
  }
  
  public String getValue(){
    return value;
  }
  
  public String getType(){
    return type;
  }
  
  public static void setVariable(String instruction, 
      ArrayList<String[]> location, String lineNum){
      String[] arguments = instruction.substring(4).split(" ");
      //Test if the variable name is undefined
      if(arguments.length != 2){
          location.add(new String[]{instruction, lineNum});
          throwErr("SYNTAX ERROR: Invalid variable name", location);
      }
      String variableName = arguments[0];
      String value = arguments[1];
      //get the type
      variables.put(variableName, new Variable(value));
    }

  public static void incrVariable(String instruction, 
      ArrayList<String[]> location, String lineNum){
      String variableName = instruction.substring(5);

      //Test if the variable name is invalid
      if(!variables.containsKey(variableName)){
          location.add(new String[]{instruction, lineNum});
          throwErr("SYNTAX ERROR: Variable " + variableName + " not found", location);
      }else if(!(variables.get(variableName).getType().equals("int"))){
          location.add(new String[]{instruction, lineNum});
          throwErr("SYNTAX ERROR: Variable " + variableName + " not integer", location);
      }else{
          Variable variable = variables.get(variableName);
          variable.setValue(Integer.toString(Integer.parseInt(variable.getValue()) + 1));
      }
    }

  public static void decrVariable(String instruction, 
      ArrayList<String[]> location, String lineNum){
      String variableName = instruction.substring(5);

      //Test if the variable name is invalid
      if(!variables.containsKey(variableName)){
          location.add(new String[]{instruction, lineNum});
          throwErr("SYNTAX ERROR: Variable " + variableName + " not found", location);
      }else if(!(variables.get(variableName).getType().equals("int"))){
          location.add(new String[]{instruction, lineNum});
          throwErr("SYNTAX ERROR: Variable " + variableName + " not integer", location);
      }else{
          Variable variable = variables.get(variableName);
          variable.setValue(Integer.toString(Integer.parseInt(variable.getValue()) - 1));
      }
    }
  
  public static void resetVariables(){
    variables = new HashMap<>();
  }
  
  public static boolean variableExists(String variable){
    return variables.containsKey(variable);
  }
  
  public static HashMap<String, Variable> getVariables(){
    return variables;
  }
  
  public static void addVariable(String name, Variable var){
    variables.put(name, var);
  }

}
