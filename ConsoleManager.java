/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barebones;

import static barebones.BareBones.getExpressionStartEnd;
import static barebones.BareBones.parseExpression;
import static barebones.BareBones.setProgramCrashed;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.swing.JLabel;

/**
 *
 * @author seanjhardy
 */
public class ConsoleManager {
    
    
    private static ArrayList<String> consoleText = new ArrayList<>();
    private static JLabel console;
    private static int consoleLineNum = 0;
    
    public ConsoleManager(JLabel console){
        this.console = console;
    }
    
    public static void render(){
        String newText = consoleText.stream().collect(Collectors.joining("<br>"));
        console.setText("<html><body style=\"text-align: justify;  text-justify: inter-word;\">" + 
                newText + "</body></html>");
    }
    
    public static void printStartupMessage(){
        printToLog("BareBones STARTING CONSOLE", false);
        printToLog("BareBones V1.0.0", false);
        printToLog("By Seanjhardy", false);
        printToLog("===============", false);
    }
    
    public static void parseConsoleInput(String input){
      if(input.startsWith("EVAL")){
        int[] startEnd = getExpressionStartEnd(input);
        String result = parseExpression(input.substring(startEnd[0], startEnd[1]));
        printToLog(input + " = " + result, false);
      }else if(input.equals("clear")){
        consoleText = new ArrayList<>();
        printStartupMessage();
      }else if(input.equals("author")){
        printToLog("Author: Seanjhardy", false);
      }else{
        printToLog(input, false);
      }
    }
    
    public static void throwErr(String errorType, ArrayList<String[]> location){
      printToLog(errorType, true);
      String tab = "&#9";
      for(int i = 0; i < location.size(); i++){
        String tabs = String.join("", Collections.nCopies(i, tab));
        String[] loc = location.get(i);
        printToLog(tabs + "at >>" + loc[0] + " on line " + loc[1], true);
      }
      setProgramCrashed(true);
    }
    
    public static void printToLog(String log, boolean err){
      DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
      Calendar cal = Calendar.getInstance();
      String time = dateFormat.format(cal.getTime());
      String text = "";
      if(err){
        text += "<span style=\"color:red;\">";
      }
      text += time + "> " + log;
      if(err){
        text += "</span>";
      }
      consoleText.add(text);
      consoleLineNum += 1;
    }
    
}
