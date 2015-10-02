package englishdemo;

import asd.ASDPhraseNode;

import java.io.*;
import java.util.*;
import java.awt.*;       // Font
import java.awt.event.*; // ActionEvent, ActionListener,
                         // WindowAdapter, WindowEvent
import javax.swing.*;    // JFrame, JPanel

/**
   NPXDemoTester illustrates parsing with the grammar npXdemo.grm for English
   noun phrases.  It provides a graphical user interface that permits the
   user to initialize a parse, to attempt complete parses of a given phrase,
   and, when a parse is successful, to display the phrase structure and
   the semantic values computed for the phrase.  Of the public methods in
   this class, the only one that the user need be concerned with
   directly is the main method.
<BR><BR>
   Command-line usage:
   <BR>In an MS-Windows command-line window:
   <BR><tt><b> java -cp asddigraphs.jar;englishdemo.jar;. englishdemo/NPXDemoTester</b></tt>
   <BR>Or under UNIX:
   <BR><tt><b> java -cp asddigraphs.jar:englishdemo.jar:. englishdemo/NPXDemoTester</b></tt>
   <BR>OR if asddigraphs.jar and englishdemo.jar have been put in the system classpath:
   <BR><tt><b> java englishdemo/NPXDemoTester</b></tt>

   @author James A. Mason
   @version 1.01 2005 February
 */
public class NPXDemoTester
{  public static void main(String[] args)
   {  new NPXDemoTester();
   }

   NPXDemoTester()
   {  semantics = new NpXDemoSemantics(this);
      window = new NPXDemoTesterWindow(this);
      window.setTitle("NPXDemoTester - version " + VERSION);
      window.setVisible(true);
   }

   public boolean completeParse()
   {  parseCompleted = false;
      boolean result = semantics.completeParse();
      window.getOutputPane().append(
         "\n" + semantics.getResultMessage() + "\n");
      if (result)
      {  parseCompleted = true;
         showBracketedPhrase();
         showSemanticValue();
         return true;
      }
      else
         return false;
   }

   public NpXDemoSemantics getSemantics()
   {  return semantics;
   }

   public boolean initializeParse(boolean strictFlag)
   {  if (originalUtterance == null || originalUtterance.length() == 0)
      {  JOptionPane.showMessageDialog(window,
            "The phrase to be parsed must not be empty.");
         return false;
      }
      parseCompleted = false;
      semantics.initializePhrase(originalUtterance, strictFlag);
      if (strictFlag)
         window.getOutputPane().append(
            "\n\"" + originalUtterance
            + "\" initialized for strict parsing.\n");
      else
         window.getOutputPane().append(
            "\n\"" + originalUtterance
            + "\" re-initialized for non-strict parsing.\n");

      return true;
   } // end initializeParse

   void setExpectedTypeList(String types)
   {  expectedTypes = new ArrayList();
      if (types == null || types.length() == 0)
      {  JOptionPane.showMessageDialog(window,
            "The list of expected phrase types must not be empty.");
         return;
      }
      StringTokenizer st = new StringTokenizer(types);
      while (st.hasMoreTokens())
         expectedTypes.add(st.nextToken());
      semantics.setExpectedTypes(expectedTypes);
      if (originalUtterance != null && originalUtterance.length() > 0)
         initializeParse(true);  // initialize for strict parsing
   } // end setExpectedTypeList

   void setSaveUniquelyParsedSubphrases(boolean value)
   {  semantics.setSaveUniquelyParsedSubphrases(value);
   }

   void setUtterance(String newUtterance)
   {  originalUtterance = newUtterance;
      if (originalUtterance == null || originalUtterance.length() == 0)
      {  JOptionPane.showMessageDialog(window,
            "The phrase to be parsed must not be empty.");
         return;
      }
      initializeParse(true);  // initialize for strict parsing
   } // end setUtterance

   void setUtteranceNull() { originalUtterance = null; }

   void showAboutInfo()
   {  // responds to About NPXDemoTester choice in Help menu
      JOptionPane.showMessageDialog(window,
         "NPXDemoTester version " + VERSION +
         "\nAuthor: James A. Mason" +
         "\nEmail: jmason@yorku.ca" +
         "\nhttp://www.yorku.ca/jmason/");
   }

   private void showBracketedPhrase()
   {  String result = semantics.bracketPhrase();
      window.getOutputPane().append(result + "\n");
   }

   void showPhraseStructure()
   {//  window.getOutputPane().append(semantics.bracketPhrase());
      ASDPhraseNode head = semantics.phraseStructure();
      ASDPhraseNode currentNode = semantics.currentNode();
      window.showTree(head, currentNode);
   } // end showPhraseStructure

   void showSemanticValue()
   {  if (parseCompleted)
      {  window.getOutputPane().append("\n");
         window.getOutputPane().append(semantics.phraseMeaning() + "\n");
      }
      else
         JOptionPane.showMessageDialog(window,
            "The phrase must be completely parsed first.");
   }
   static final String VERSION = "1.01";
   static final Font FONT
      = new Font("Monospaced", Font.PLAIN, 12);
   private NPXDemoTesterWindow window;
   private NpXDemoSemantics semantics;
   private String originalUtterance;
   private ArrayList expectedTypes;
   private boolean parseCompleted = false;

   private class NPXDemoTesterWindow extends JFrame
   {  NPXDemoTesterWindow(NPXDemoTester givenDriver)
      {  driver = givenDriver;
         grammarFileNameField = new JTextField(40);
         grammarFileNameField.setText(
            driver.getSemantics().getGrammarFileName());
         grammarFileNameField.addActionListener(
            new GrammarFileNameFieldListener(this));
         expectedTypeListField = new JTextField(40);
         String expected = "";
         for (Iterator it =
              driver.getSemantics().getExpectedTypes().iterator();
              it.hasNext(); )
         {  String type = (String)it.next();
            expected += type + " ";
         }
         expectedTypeListField.setText(expected);
         expectedTypeListField.addActionListener(
            new ExpectedTypeListFieldListener(this));
         utteranceField = new JTextField(40);
         utteranceField.addActionListener(
            new UtteranceFieldListener(this));
         uniquelyParsedSubphrasesBox = new JCheckBox(
            "Save all uniquely-parsed subphrases");
         uniquelyParsedSubphrasesBox.addActionListener(
            new UniquelyParsedSubphrasesBoxListener(this));
         uniquelyParsedSubphrasesBox.setSelected(true);
         JPanel pane = new JPanel();
         pane.setLayout(
            new BoxLayout(pane, BoxLayout.Y_AXIS));
         pane.add(
            new LabeledTextField("Grammar file:    ", grammarFileNameField));
         pane.add(
            new LabeledTextField("Expected types:", expectedTypeListField));
         pane.add(
            new LabeledTextField("Phrase parsed: ", utteranceField));
         pane.add(uniquelyParsedSubphrasesBox);
         outputPane = new JTextArea();
         outputPane.setMinimumSize(new Dimension(DEFAULT_WIDTH,
            DEFAULT_HEIGHT));
         outputPane.setFont(NPXDemoTester.FONT);
         OutputPaneMenu menu = new OutputPaneMenu(outputPane, driver);
         MouseListener popupListener = new PopupListener(menu);
         outputPane.addMouseListener(popupListener);

         pane.add(new JScrollPane(outputPane));
         getContentPane().add(pane, BorderLayout.CENTER);
         addWindowListener(new WindowCloser(this));
            // listens for window closing events (see below)
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

         JMenuBar menuBar = new JMenuBar();
         setJMenuBar(menuBar);
         ActionMenu aMenu = new ActionMenu(this);
         aMenu.setMnemonic(KeyEvent.VK_A);
         menuBar.add(aMenu);
         HelpMenu hMenu = new HelpMenu(this);
         hMenu.setMnemonic(KeyEvent.VK_H);
         menuBar.add(hMenu);
      } // end NPXDemoTesterWindow(NPXDemoTester givenDriver)

      void clearExpectedTypeListField() { expectedTypeListField.setText(""); }
      void clearGrammarFileNameField() { grammarFileNameField.setText(""); }
      void clearUtteranceField() { utteranceField.setText(""); }

      JTextField getExpectedTypeListField() { return expectedTypeListField; }
      JTextField getGrammarFileNameField() { return grammarFileNameField; }
      JTextArea getOutputPane() { return outputPane; }
      NPXDemoTester getDriver() { return driver; }
      JTextField getUtteranceField() { return utteranceField; }

      void grammarFileNameFieldChanged()
      {  grammarFileNameField.setText(semantics.getGrammarFileName());
      } // end grammarFileNameChanged

      void expectedTypeListFieldChanged()
      {  driver.setExpectedTypeList(expectedTypeListField.getText().trim());
      }

      void uniquelyParsedSubphrasesBoxChanged()
      {  driver.setSaveUniquelyParsedSubphrases(
            uniquelyParsedSubphrasesBox.isSelected());
      }

      void utteranceFieldChanged()
      {  driver.setUtterance(utteranceField.getText().trim());
      }

      /**
         Displays the tree rooted at the given head node,
         with node currentNode indicated by an asterisk and an arrow.
         @param head the header node of the phrase structure
         @param currentNode the current node at the top level
         in the phrase structure
       */
      void showTree(ASDPhraseNode head, ASDPhraseNode currentNode)
      {  showTreeMark(head, "", currentNode);
         outputPane.append("\n");
      } // end showTree

      /**
         Displays the portion of the tree starting at the
         given node and indented with the given indentString as
         prefix for each line that does not represent a top-
         level node.  Top-level nodes are prefixed with three
         blanks or, in the case of the given aNode, an asterisk
         and an arrow whose purpose is to indicate the node
         which is the current node during a parse.
         @param indentString prefix for indenting of the
         current subtree
         @param aNode the node to be marked with an arrow
       */
      private void showTreeMark(ASDPhraseNode givenNode, String indentString,
                               ASDPhraseNode markNode)
      {  outputPane.append("\n");
         if (givenNode == markNode)
            outputPane.append("*->");
         else
            outputPane.append("   ");
         outputPane.append(indentString + givenNode.word() + " ");
         if (givenNode.instance() != null)
            outputPane.append(givenNode.instance().instance());
         else
            outputPane.append("nil");
         if (givenNode.subphrase() != null)
            showTreeMark(givenNode.subphrase(),indentString + "   ",
               markNode);
         if (givenNode.nextNode() != null)
            showTreeMark(givenNode.nextNode(), indentString, markNode);
      } // end showTreeMark

      static final int DEFAULT_WIDTH = 800;  // window width
      static final int DEFAULT_HEIGHT = 600; // window height
      private NPXDemoTester driver;
      private JTextField grammarFileNameField;
      private JTextField expectedTypeListField;
      private JTextField utteranceField;
      private JCheckBox uniquelyParsedSubphrasesBox;
      private JTextArea outputPane;
   } // end class NPXDemoTesterWindow

   private class LabeledTextField extends JPanel
   {  LabeledTextField(String labelText, JTextField textField)
      {  setMaximumSize(new Dimension(800,10));
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         JLabel label = new JLabel(labelText);
         textField.setFont(NPXDemoTester.FONT);
         this.add(label);
         this.add(textField);
      }
   } // end class LabeledTextField

   /**
      An instance defines what should happen when a window
      closes.
    */
   class WindowCloser extends WindowAdapter
   {  WindowCloser(NPXDemoTesterWindow w)
      {  window = w;
      }

      public void windowClosing(WindowEvent e)
      {
         System.exit(0);        // stop the program
      }

      NPXDemoTesterWindow window;
   } // end class WindowCloser

   private class GrammarFileNameFieldListener implements ActionListener
   {
      GrammarFileNameFieldListener(NPXDemoTesterWindow w)
      {  window = w;
      }

      public void actionPerformed(ActionEvent e)
      {  window.grammarFileNameFieldChanged();
      }

      private NPXDemoTesterWindow window;
   } // end class GrammarFileNameFieldListener

   private class ExpectedTypeListFieldListener implements ActionListener
   {
      ExpectedTypeListFieldListener(NPXDemoTesterWindow w)
      {  window = w;
      }

      public void actionPerformed(ActionEvent e)
      {  window.expectedTypeListFieldChanged();
      }

      private NPXDemoTesterWindow window;
   } // end class ExpectedTypeListFieldListener

   private class UtteranceFieldListener implements ActionListener
   {
      UtteranceFieldListener(NPXDemoTesterWindow w)
      {  window = w;
      }

      public void actionPerformed(ActionEvent e)
      {  window.utteranceFieldChanged();
      }

      private NPXDemoTesterWindow window;
   } // end class UtteranceFieldListener

   private class UniquelyParsedSubphrasesBoxListener implements
      ActionListener
   {  UniquelyParsedSubphrasesBoxListener(
         NPXDemoTesterWindow w)
      {  window = w;
      }

      public void actionPerformed(ActionEvent e)
      {  window.uniquelyParsedSubphrasesBoxChanged();
      }

      private NPXDemoTesterWindow window;
   } // end class UniquelyParsedSubphrasesBoxListener

   private class OutputPaneMenu extends JPopupMenu implements ActionListener
   {  OutputPaneMenu(JTextArea p, NPXDemoTester t)
      {  pane = p;
         driver = t;
         setInvoker(pane);

         JMenuItem initializeItem = new JMenuItem("Initialize parse");
         initializeItem.addActionListener(this);
         add(initializeItem);
         addSeparator();
         JMenuItem completeParseItem = new JMenuItem("Complete parse");
         completeParseItem.addActionListener(this);
         add(completeParseItem);
         addSeparator();
         JMenuItem showPhraseStructureItem
            = new JMenuItem("Show phrase structure");
         showPhraseStructureItem.addActionListener(this);
         add(showPhraseStructureItem);
         JMenuItem showSemanticValueItem
            = new JMenuItem("Show semantic value");
         showSemanticValueItem.addActionListener(this);
         add(showSemanticValueItem);
         addSeparator();
         JMenuItem selectAllItem = new JMenuItem("Select all");
         selectAllItem.addActionListener(this);
         add(selectAllItem);
         JMenuItem copyItem = new JMenuItem("Copy selection");
         copyItem.addActionListener(this);
         add(copyItem);
         addSeparator();
         JMenuItem clearItem = new JMenuItem("Erase output pane");
         clearItem.addActionListener(this);
         add(clearItem);
      } // end OutputPaneMenu(JTextArea p, NPXDemoTester t)

      public void actionPerformed(ActionEvent e)
      {  if (driver == null) return;
         String command = e.getActionCommand();
         if (command.equals("Initialize parse"))
            driver.initializeParse(true);  // initialize for strict parsing
         else if (command.equals("Complete parse"))
            driver.completeParse();
         else if (command.equals("Show phrase structure"))
            driver.showPhraseStructure();
         else if (command.equals("Show semantic value"))
            driver.showSemanticValue();
         else if (command.equals("Select all"))
         {  pane.requestFocus();
            pane.selectAll();
         }
         else if (command.equals("Copy selection"))
            pane.copy();
         else if (command.equals("Erase output pane"))
            pane.setText("");
      } // end actionPerformed

      NPXDemoTester driver;
      JTextArea pane; // the pane to which the menu is attached.
   } // end class OutputPaneMenu

   class ActionMenu extends JMenu implements ActionListener
   {  ActionMenu(NPXDemoTesterWindow w)
      {  super("Action");
         window = w;
         driver = window.getDriver();
         outputPane = window.getOutputPane();
         JMenuItem initializeMenuItem = new JMenuItem("Initialize parse",
            KeyEvent.VK_I);
         initializeMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_I, ActionEvent.ALT_MASK));
         add(initializeMenuItem);
         initializeMenuItem.addActionListener(this);
         JMenuItem completeParseMenuItem = new JMenuItem("Complete Parse",
            KeyEvent.VK_P);
         completeParseMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_P, ActionEvent.ALT_MASK));
         add(completeParseMenuItem);
         completeParseMenuItem.addActionListener(this);
         JMenuItem showPhraseStructureMenuItem
            = new JMenuItem("Show phrase structure",
            KeyEvent.VK_S);
         showPhraseStructureMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_S, ActionEvent.ALT_MASK));
         showPhraseStructureMenuItem.addActionListener(this);
         add(showPhraseStructureMenuItem);
         JMenuItem showSemanticValueMenuItem
            = new JMenuItem("Show semantic value",
            KeyEvent.VK_V);
         showSemanticValueMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_V, ActionEvent.ALT_MASK));
         showSemanticValueMenuItem.addActionListener(this);
         add(showSemanticValueMenuItem);
         addSeparator();
         JMenuItem copyAllMenuItem = new JMenuItem("Select All of output pane",
            KeyEvent.VK_A);
         copyAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_A, ActionEvent.CTRL_MASK));
         copyAllMenuItem.addActionListener(this);
         add(copyAllMenuItem);
         JMenuItem copySelectionMenuItem = new JMenuItem("Copy Selection",
            KeyEvent.VK_C);
         copySelectionMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_C, ActionEvent.CTRL_MASK));
         copySelectionMenuItem.addActionListener(this);
         add(copySelectionMenuItem);
         addSeparator();
         JMenuItem eraseMenuItem = new JMenuItem("Erase output pane",
            KeyEvent.VK_E);
         eraseMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_E, ActionEvent.CTRL_MASK));
         eraseMenuItem.addActionListener(this);
         add(eraseMenuItem);
      }

      /**
        Listens for menu item events.
       */
      public void actionPerformed(ActionEvent e)
      {  String command = e.getActionCommand();
         if (command.equals("Initialize parse"))
            driver.initializeParse(true);   // initialize for strict parsing
         else if (command.equals("Complete Parse"))
            driver.completeParse();
         else if (command.equals("Show phrase structure"))
            driver.showPhraseStructure();
         else if (command.equals("Show semantic value"))
            driver.showSemanticValue();
         else if (command.equals("Select All of output pane"))
         {  outputPane.requestFocus();
            outputPane.selectAll();
         }
         else if (command.equals("Copy Selection"))
            outputPane.copy();
         else if (command.equals("Erase output pane"))
            outputPane.setText("");
      }

      NPXDemoTester driver;
      NPXDemoTesterWindow window;
      JTextArea outputPane;
   } // end class ActionMenu

   class HelpMenu extends JMenu implements ActionListener
   {  HelpMenu(NPXDemoTesterWindow w)
      {  super("Help");
         window = w;
         driver = window.getDriver();
         JMenuItem aboutMenuItem = new JMenuItem("About NPXDemoTester",
            KeyEvent.VK_A);
         add(aboutMenuItem);
         aboutMenuItem.addActionListener(this);
      }

      /**
         Listens for menu item events.
       */
      public void actionPerformed(ActionEvent e)
      {  String command = e.getActionCommand();
         if (command.equals("About NPXDemoTester"))
            driver.showAboutInfo();
      }

      NPXDemoTester driver;
      NPXDemoTesterWindow window;
   } // end class HelpMenu

} // end class NPXDemoTester

/**
   This class can be used by any others in the englishdemo package,
   to pop up a given menu in response to a right mouse click.
 */
class PopupListener extends MouseAdapter
{  PopupListener(JPopupMenu m)
   {  menu = m;
   }

   public void mousePressed(MouseEvent e)
   {  maybeShowPopup(e);
   }

   public void mouseReleased(MouseEvent e)
   {  maybeShowPopup(e);
   }

   private void maybeShowPopup(MouseEvent e)
   {  if (e.isPopupTrigger())
        menu.show(e.getComponent(), e.getX(), e.getY());
   }

   private JPopupMenu menu;
} // end class PopupListener