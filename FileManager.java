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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author seanjhardy
 */
public class FileManager {
    //stores the current directory for ease of access to files
    private static File cd = new File("saves");
    
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
    
    public static void setCD(File cd){
        FileManager.cd = cd;
    }

    public static File getCD(){
        return cd;
    }
}
