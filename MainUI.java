/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barebones;

import static barebones.ConsoleManager.parseConsoleInput;
import static barebones.GUIManager.getColour;
import static barebones.GUIManager.getDefaultFont;
import java.awt.Color;
import static java.awt.Color.WHITE;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

/**
 *
 * @author seanjhardy
 */
public class MainUI extends Menu{
    
    private long lastUpdate;
    
    public MainUI(MainPanel parent){
        super(parent);
        createComponents();
    }
    
    public final void createComponents(){
        createImageButton("load", "Load", "LoadHighlighted", "LoadHighlighted", "Load", parent.getOpenFileBool(), getColour("button")).setBorder(null);
        createImageButton("new", "New", "NewHighlighted", "NewHighlighted", "New", parent.getNewFileBool(), getColour("button")).setBorder(null);
        createImageButton("save", "Save", "SaveHighlighted", "SaveHighlighted", "Save", parent.getSaveFileBool(), getColour("button")).setBorder(null);    
        createImageButton("run", "Stop", "StopHighlighted", "RunHighlighted", "Run", parent.getRunBool(), getColour("button")).setBorder(null);    
        
        createImageButton("debug", "Debug", "DebugHighlighted", "DebugHighlighted", "Debug", parent.getDebugModeBool(), getColour("button")).setBorder(null);    
        createImageButton("step", "Step", "StepHighlighted", "StepHighlighted", "Step", parent.getStepBool(), getColour("button")).setBorder(null);    
        
        createImageButton("exit", "Exit", "ExitHighlighted", "ExitHighlighted", "Exit", parent.getExitBool(), getColour("button")).setBorder(null);    
        
        createFilenameTextBox();
        createSourceArea("source", "Code goes here...", getColour("background")); 
        createConsoleArea("console", getColour("background"));
        JTextField consoleInput = createTextField("consoleInput", "", getColour("background"));
        consoleInput.addKeyListener(new KeyAdapter(){
          public void keyPressed(KeyEvent evt){
            if(evt.getKeyCode() == KeyEvent.VK_ENTER){
              parseConsoleInput(consoleInput.getText());
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
        
        getComponent("debug").setBounds(startX+width-75, startY, 30, 30);
        getComponent("exit").setBounds(startX+width-45, startY, 30, 30);
        
        //code
        int endWidth = width-35;
        if(parent.getDebugModeBool().getValue()){
          endWidth -= 200;
          getComponent("step").setBounds(endWidth+20, startY+40, 20, 20);
        }else{
          getComponent("step").setBounds(0,0,0,0);
        }
        getComponent("source").setBounds(startX+10, startY+40, endWidth, height-415);
        JTextPane source = (JTextPane) ((JScrollPane)getComponent("source")).getViewport().getView();
        source.setBounds(0,0,width-45,height-55);

        getComponent("console").setBounds(startX+10, height-365, endWidth, 280);
        JLabel console = (JLabel) ((JScrollPane)getComponent("console")).getViewport().getView();
        console.setBounds(0,0,width-45, 80);

        getComponent("consoleInput").setBounds(startX+10, height-80, endWidth, 30);
        
        getComponent("debugInfo").setBounds(endWidth+20, startY+60, width-endWidth-50, height-100);
    }
    
    
    public final void createFilenameTextBox(){
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
    }
    
    public final void createSourceArea(String name, String defaultText, Color colour){
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBackground(getColour("background"));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        JTextPane lines = new JTextPane();
        lines.setText("1");
        lines.setBackground(Color.LIGHT_GRAY);
        lines.setFont(new Font(getDefaultFont(), 0, 14));
        lines.setEditable(false);
        
        JTextPane textPane = new JTextPane();
        textPane.setText(defaultText);
        textPane.setAlignmentY(SwingConstants.TOP);
        textPane.setForeground(new Color(255,255,255));
        textPane.setCaretColor(WHITE);
        textPane.setBackground(colour);
        textPane.setFont(new Font(getDefaultFont(), 0, 14));
        
        Document doc = textPane.getDocument();
        doc.putProperty(PlainDocument.tabSizeAttribute, 4);
        
        MutableAttributeSet set = new SimpleAttributeSet(textPane.getParagraphAttributes());
        StyleConstants.setLeftIndent(set, 10);
        StyleConstants.setRightIndent(set, 10);
    
        textPane.setForeground(new Color(200, 200, 200));
        textPane.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                if(textPane.getText().isEmpty()){
                    textPane.setText(defaultText);
                    textPane.setForeground(new Color(200,200,200));
                }
            }
            
            @Override
            public void focusGained(FocusEvent e){
                if(textPane.getText().equals(defaultText)){
                    textPane.setText("");
                    textPane.setForeground(new Color(255,255,255));
                }
            }
        });
        textPane.getDocument().addDocumentListener(new DocumentListener(){
          
            public String getText(){
                int caretPosition = textPane.getDocument().getLength();
                Element root = textPane.getDocument().getDefaultRootElement();
                String text = "1" + System.getProperty("line.separator");
                for(int i = 2; i < root.getElementIndex(caretPosition) + 2; i++){
                    text += i + System.getProperty("line.separator");
                }
                return text;
            }
            
            public void updateHighlight(){
              textPane.setForeground(WHITE);
              if(System.currentTimeMillis() - lastUpdate < 10){
                return;
              }
              lastUpdate = System.currentTimeMillis();
              Runnable doHighlight = new Runnable() {
                @Override
                public void run() {
                  StyleContext style = StyleContext.getDefaultStyleContext();
                  String input = textPane.getText();
                  ArrayList<String[]> patterns = new ArrayList<>();
                  String functionMatch = "(def) ([a-zA-Z]+) *\\(((int|bool) *([a-zA-Z]+))*( *, * ((int|bool) *([a-zA-Z]+))+)* *\\) *\\{";
                  //find function definitions
                  
                  String keywords = "\\b(while|end|do|if|else|return|int|bool|true|false";
                  for(String s : BareBones.getOperators()){
                    keywords += "|\\Q" + s + "\\E";
                  }
                  keywords += ")\\b";
                  patterns.add(new String[]{"style2", keywords});
                  patterns.add(new String[]{"style3", "\\b(set)\\b"});
                  patterns.add(new String[]{"style4", "\\b(incr|decr)\\b"});
                  patterns.add(new String[]{"style5", "\\b([0-9]+)\\b"});
                  patterns.add(new String[]{"comments", "#(.*)(\\n|\\b)"});

                  //highlight all to white
                  AttributeSet textStyle = style.addAttribute(style.getEmptySet(), StyleConstants.Foreground, getColour("style1"));
                  textPane.getStyledDocument().setCharacterAttributes(0, textPane.getDocument().getLength(), textStyle, false);
                  
                  
                  //highlight numbers
                  for(String[] pattern : patterns){
                    textStyle = style.addAttribute(style.getEmptySet(), StyleConstants.Foreground, getColour(pattern[0]));
                    Matcher m = Pattern.compile(pattern[1]).matcher(input);
                    while(m.find()){
                        textPane.getStyledDocument().setCharacterAttributes(m.start(),(m.end() - m.start()),textStyle, false);
                    }
                  }
                  parent.highlightLine(-1);
                  
                  Matcher m = Pattern.compile(functionMatch).matcher(input);
                  while(m.find()){
                    //match def
                    textStyle = style.addAttribute(style.getEmptySet(), StyleConstants.Foreground, getColour("style3"));
                    textPane.getStyledDocument().setCharacterAttributes(m.start(1),(m.end(1) - m.start(1)),textStyle, false);
                    
                    //match parameters
                    textStyle = style.addAttribute(style.getEmptySet(), StyleConstants.Foreground, getColour("style6"));
                    textPane.getStyledDocument().setCharacterAttributes(m.start(5),(m.end(5) - m.start(5)),textStyle, false);
                    
                    textStyle = style.addAttribute(style.getEmptySet(), StyleConstants.Foreground, getColour("style6"));
                    textPane.getStyledDocument().setCharacterAttributes(m.start(9),(m.end(9) - m.start(9)),textStyle, false);
                  }
                }
              };  
              SwingUtilities.invokeLater(doHighlight); 
            }
            
            @Override
            public void changedUpdate(DocumentEvent de) {
                lines.setText(getText());
            }

            @Override
            public void insertUpdate(DocumentEvent de) {
                lines.setText(getText());
                updateHighlight();
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                lines.setText(getText());
                updateHighlight();
          }
        });
        
        scrollPane.getViewport().add(textPane);
        scrollPane.setRowHeaderView(lines);

        parent.add(scrollPane);
        components.put(name, scrollPane);
    }
    
    public final void createConsoleArea(String name, Color colour){
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBackground(getColour("consoleBackground"));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        JLabel console = new JLabel();
        console.setVerticalAlignment(SwingConstants.BOTTOM);
        console.setForeground(getColour("consoleForeground"));
        console.setBackground(colour);
        console.setOpaque(true);
        console.setFont(new Font(getDefaultFont(), 0, 14));
        
        scrollPane.getViewport().add(console);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        parent.add(scrollPane);
        components.put(name, scrollPane);
    }
}
