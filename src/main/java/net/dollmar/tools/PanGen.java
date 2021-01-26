package net.dollmar.tools;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import javax.swing.*;


/**
 * This is a simple GUI tool to generate a number of credit/debit card PANs.
 * Generated PANs will conform to the LUHN check formula. Also, no duplicate
 * card PANs will be generated within a single generation session. Obviously 
 * expiry dates corresponding to generated card PANs will be created.
 * 
 * Import Note: This tool is for meant for experimentation, learning & test 
 * purposes only. All forms of malicious use are not permitted.
 *   
 * The author is not responsible for any malicious (intended or unintended) use.
 * 
 * This is a free software with no copyright attached. Permission is granted 
 * for all forms of use except for any form intended for malicious damage.
 * 
 * @author Mohammad A. Rahin
 * @since 2014-03-21
 */
public class PanGen {
  private static final int MAX_PAN_COUNT = 2000000;
  //private static final String COPYRIGHT = "(c) 2011-21 Dollmar Enterprised Ltd.";

  private JFrame frame;
  private JButton generateBtn;
  private JButton quitBtn;
  private JButton clearBtn;
  private JButton exportBtn;
  private JTextField  binField;
  private JTextField  lengthField;
  private JTextField  countField;
  private JTextArea output;
  
  private String appVersion;

  private LuhnCalculator lc;
  private Random prng;
  private ArrayList<String> panList = new ArrayList<String>();



  public class IntegerTextField extends JTextField {
    private static final long serialVersionUID = -3231768355723134883L;

    final static String badchars = "`~!@#$%^&*()_+-=\\|\"':;?/>.<, ";

    public void processKeyEvent(KeyEvent ev) {

      char c = ev.getKeyChar();

      if((Character.isLetter(c) && !ev.isAltDown()) 
          || badchars.indexOf(c) > -1) {
        ev.consume();
        return;
      }
      if(c == '-' && getDocument().getLength() > 0) 
        ev.consume();
      else 
        super.processKeyEvent(ev);

    }
  }

  public class LuhnCalculator {
    protected String clearPAN;

    /**
     * LuhnCalculator constructor comment.
     */
    public LuhnCalculator(String clearPAN) {
      this.clearPAN = clearPAN;
    }

    public void setClearPAN(String clearPAN) {
      this.clearPAN = clearPAN;
    }

    /**
     * Insert the method's description here.
     * @return char
     */
    public char calculateCheckDigit() {
      int sum = sumDoubles() + sumSingles();
      int result = (10 - sum % 10) % 10;
      return (char) ('0' + result);
    }
    /**
     * Insert the method's description here.
     * @return int
     */
    protected int sumDoubles() {
      int sum = 0;
      for (int i = clearPAN.length() - 1; i >= 0; i -= 2) {
        int value = clearPAN.charAt(i) - '0';
        value *= 2;
        sum += value % 10;
        sum += value / 10;
      }
      return sum;
    }
    /**
     * Insert the method's description here.
     * @return int
     */
    protected int sumSingles() {
      int sum = 0;
      for (int i = clearPAN.length() - 2; i >= 0; i -= 2) {
        int value = clearPAN.charAt(i) - '0';
        sum += value;
      }
      return sum;
    }
  }


  /**
   * Create the application.
   */
  public PanGen() {
    appVersion = getVersion();
    if (appVersion == null || appVersion.isEmpty()) {
      appVersion = "x.yy";
    }
    initialize();
  }

  /**
   * Initialize the contents of the frame.
   */

  private void initialize() {
    lc = new LuhnCalculator("");
    prng = new Random();
    
    frame = new JFrame("PANGen");
    Container contentPane = frame.getContentPane();

    frame.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    JPanel hdrPanel = new JPanel();
    hdrPanel.setLayout(new GridLayout(2, 1, 0, 0));
    hdrPanel.setBorder(BorderFactory.createTitledBorder(""));
    contentPane.add(hdrPanel);

    JLabel header = new JLabel("<html><h3><FONT COLOR=RED>PANGEN: Synthetic Card PAN Generator</FONT></h3></html>", JLabel.CENTER);
    JLabel vInfo = new JLabel("Version: " + appVersion, JLabel.CENTER);
    hdrPanel.add(header);
    hdrPanel.add(vInfo);

    JPanel paramPanel = new JPanel();
    paramPanel.setBorder(BorderFactory.createTitledBorder("Gen Params"));
    paramPanel.setLayout(new GridLayout(2, 1, 10, 2));
    contentPane.add(paramPanel);

    paramPanel.add(new JLabel("BIN (2-10 digits)")); 
    paramPanel.add(binField = makeNumericTextField("400000", "Enter the first 2 to 10 (numeric) digits of the PAN to be generated"));

    paramPanel.add(new JLabel("PAN Length")); 
    paramPanel.add(lengthField = makeNumericTextField("16", "Enter the length (10-22) of the PAN to be generated"));

    paramPanel.add(new JLabel("PANs Needed")); 
    paramPanel.add(countField = makeNumericTextField("10", "Enter the number of PANs to generate"));
    paramPanel.add(new JPanel());

    JPanel resultsPanel = new JPanel();
    resultsPanel.setBorder(BorderFactory.createTitledBorder("Synthetic PANs"));
    resultsPanel.setLayout(new BorderLayout());
    contentPane.add(resultsPanel);

    output = new JTextArea(10, 30);
    output.setLineWrap(true);
    output.setWrapStyleWord(true);
    output.setEditable(false);
    //output.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    JScrollPane pane = new JScrollPane(output);
    resultsPanel.add(pane);

    JPanel btnPanel = new JPanel();
    btnPanel.setBorder(BorderFactory.createTitledBorder("Control Panel"));
    btnPanel.setLayout(new GridLayout(1, 4, 10, 0));
    contentPane.add(btnPanel);

    clearBtn = new JButton("Clear");
    clearBtn.setToolTipText("Clears the list of generated PAN");
    ActionListener doClearList = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        panList.clear();
        output.setText("");
      }
    };
    ActionListener cursorClearList = CursorController.createListener(frame, doClearList);
    clearBtn.addActionListener(cursorClearList);
    btnPanel.add(clearBtn);

    generateBtn = new JButton("Generate");
    generateBtn.setToolTipText("Generates a set of card PANs as per gen params");
    ActionListener doPanList = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        buildPanList();
      }
    };
    ActionListener cursorPanList = CursorController.createListener(frame, doPanList);
    generateBtn.addActionListener(cursorPanList);
    generateBtn.setActionCommand("OK");
    frame.getRootPane().setDefaultButton(generateBtn);
    btnPanel.add(generateBtn);

    exportBtn = new JButton("Export");
    exportBtn.setToolTipText("Saves the set of generated PANs to a file");
    ActionListener doExport = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        exportPanList();
      }
    }; 
    exportBtn.addActionListener(doExport);
    btnPanel.add(exportBtn);


    quitBtn = new JButton("QUIT");
    ActionListener doQuit = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        System.exit(JFrame.ABORT);
      }
    }; 
    quitBtn.addActionListener(doQuit);
    btnPanel.add(quitBtn);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(300, 400);
    frame.setResizable(false);
    frame.setVisible(true);
    frame.pack();
  }


  private JTextField makeNumericTextField(String initValue, String toolTipText) {
    JTextField jtf = new IntegerTextField();
    jtf.setToolTipText(toolTipText);
    jtf.setText(initValue);

    return jtf;
  }


  private void buildPanList() {
    int binLength = binField.getText().length();
    if (binLength < 2 || binLength > 10) {
      showMessageDialog("ERROR: Card IIN must be between 2 to 10 digits long");
      return;
    }
    int panLength = Integer.parseInt(lengthField.getText());
    if (panLength < 10 || panLength > 22) {
      showMessageDialog("ERROR: Card PAN length must be between 10 and 22");
      return;
    }
    int panCount = Integer.parseInt(countField.getText());
    if (panCount < 1 || panCount > MAX_PAN_COUNT) {
      showMessageDialog("ERROR: No more than " + MAX_PAN_COUNT + " card PAN can be generated");
      return;
    }

    panList.clear();
    BinarySearchTree bst = new BinarySearchTree( );
    int i = 0;
    while (i < panCount) {
      String pan = generatePan(this.binField.getText(), panLength);
      try {
        bst.insert(pan);
        panList.add(pan);
        i++;
      }
      catch (DuplicateItemException die) {
        System.err.println("Duplicate PAN " + pan);
      }
    }
    StringBuffer sb = new StringBuffer();
    output.setText("");  // clear the list before starting
    for (i = 0; i < panCount; i++) {
      sb.append(panList.get(i) + "\n");
    }
    output.setText(sb.toString());
    bst.makeEmpty();
    System.out.println("Total PANs generated: " + i);
  }


  public void exportPanList() {
    if (panList.isEmpty()) {
      showMessageDialog("Error: Nothing to export.");
      return;
    }
    final JFileChooser fc = new JFileChooser();
    int rc = fc.showSaveDialog(null);
    if (rc == JFileChooser.APPROVE_OPTION) {
      File expFile = fc.getSelectedFile();
      if (expFile.exists()) {
        rc = JOptionPane.showConfirmDialog(null, "Overwrite existing file?");
        if (rc == JOptionPane.YES_OPTION) {
          expFile.delete();
        }
      }
      BufferedWriter panFileWriter = null;
      try {
        panFileWriter = new BufferedWriter(new FileWriter(expFile, true));
        panFileWriter.write("# Synthetic PAN generated by PUNGENT " + appVersion + "\n");
        panFileWriter.write("# DISCLAIMER: TO BE USED FOR TESTING PURPOSES ONLY" + "\n");
        for (String p : panList) {
          panFileWriter.write(p + "\n");
        }
      }
      catch (IOException e) {
        showMessageDialog("Error: File I/O error.");
      }
      finally {
        if (panFileWriter != null) {
          try {
            panFileWriter.flush();
            panFileWriter.close();
          }
          catch (Exception e) {
          }
        }
      }
    }
  }


  public String generatePan(String bin, int panLength) {
    StringBuffer sb = new StringBuffer();
    sb.append(bin);

    int randomDigits = panLength - bin.length() - 1;
    for (int i = 0; i < randomDigits; i++) {
      sb.append(prng.nextInt(10));
    }
    lc.setClearPAN(sb.toString());
    sb.append(lc.calculateCheckDigit());

    return sb.toString();

  }


  /*
   * Extracts the version details from the jar's MANIFEST.MF file
   */
  private String getVersion() {
    try {
      Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");

      while (resources.hasMoreElements()) {
        Manifest manifest = new Manifest(resources.nextElement().openStream());
        Attributes attr = manifest.getMainAttributes();

        return attr.getValue("Implementation-Version");
      } 
    }
    catch (IOException E) {
    } 
    return "";
  }

  public static void setLook() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } 
    catch (Exception ex) {
    }
  }


  private void showMessageDialog(final String msg) {
    JOptionPane.showMessageDialog(null, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
  }


  public static void main(String[] args) {
    setLook();
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          PanGen window = new PanGen();
          window.frame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });	
  }

}

