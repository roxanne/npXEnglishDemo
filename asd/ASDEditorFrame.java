/*

Copyright 2003 James A. Mason

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy
of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations
under the License.

 */

package asd;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import java.awt.*;       // BorderLayout, Container, etc.
                         // Graphics2D, Rectangle, Color
import java.awt.event.*; // ActionEvent, ActionListener,
                         // WindowAdapter, WindowEvent
import javax.swing.*;    // JFrame, JPanel, JButton
import javax.swing.event.*;  // ListSelectionListener

/**
   Defines the window in which the ASD grammar is
   displayed and with which the user of an ASDEditor interacts.

   @author James A. Mason
   @version 1.09 2003 Jul
 */
class ASDEditorFrame extends JFrame // implements ActionListener
{
   public ASDEditorFrame(ASDEditor givenEditor,
      WordInstanceChooser c1, WordInstanceChooser c2,
      NetPanel panel, JRadioButton button1, JRadioButton button2)
   {
      editor = givenEditor;
      chooser1 = c1;
      chooser2 = c2;
      netPanel = panel;
      JMenuBar menuBar = new JMenuBar();
      setJMenuBar(menuBar);
      FileMenu fMenu = new FileMenu(this);
      fMenu.setMnemonic(KeyEvent.VK_F);
      menuBar.add(fMenu);
      EditMenu eMenu = new EditMenu(this);
      eMenu.setMnemonic(KeyEvent.VK_E);
      menuBar.add(eMenu);
      ViewMenu vMenu = new ViewMenu(this);
      vMenu.setMnemonic(KeyEvent.VK_V);
      menuBar.add(vMenu);
      HelpMenu hMenu = new HelpMenu(this);
      hMenu.setMnemonic(KeyEvent.VK_H);
      menuBar.add(hMenu);
      setSize(DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT);
//      netPanel.setPreferredSize(new Dimension(DEFAULT_FRAME_WIDTH,
//         DEFAULT_PANEL_HEIGHT));
      netPanel.setMinimumSize(new Dimension(DEFAULT_FRAME_WIDTH,
         DEFAULT_PANEL_HEIGHT));

      addWindowListener(new WindowCloser(this));
         // listens for window closing events (see below)
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);

      radioPanel = new JPanel();
      radioPanel.add(new JLabel("Selecting from:"));
      radioPanel.add(button1);
      radioPanel.add(button2);

      newOrUpdatedGrammar(false);  // don't inform the ASDEditor
   } // end ASDEditorFrame() constructor

   void choosersChanged()
   { // for cases in which the choosers have been changed
     // indirectly by the editor
      chooser1.setGrammar(editor.getGrammar());
      chooser2.setGrammar(editor.getGrammar());
      String word = editor.getWordSelected1();
      String instance = "";
      if (word != null)
      {  instance = editor.getInstanceSelected1();
         if (instance != null)
         {  chooser1.setWordSelected(word, false);
               // don't tell the editor, so the instance isn't
               // de-selected
            chooser1.setInstanceSelected(instance, true);
               // tell the editor, so the edge list and
               // phrase type field will be updated.
         }
         else
            chooser1.setWordSelected(word, true);
               // tell the editor
      }
      word = editor.getWordSelected2();
      if (word != null)
      {  instance = editor.getInstanceSelected2();
         if (instance != null)
         {  chooser2.setWordSelected(word, false);
            chooser2.setInstanceSelected(instance, false);
              // don't tell the editor
         }
         else
            chooser2.setWordSelected(word, true);
               // tell the editor
      }
      newOrUpdatedGrammar(true);
         // tell the editor so the proper eNode is selected
   }

   void edgeListChanged()
   {
      JPanel labeledEdgeList = new JPanel();
      labeledEdgeList.setLayout(new BorderLayout());
      labeledEdgeList.add(new JLabel(EDGES_LABEL), BorderLayout.NORTH);
      edgeListScrollPane = new JScrollPane(editor.getEdgeList());
      labeledEdgeList.add(edgeListScrollPane);
      topMiddleSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
         editor.getPhraseTypeField(), labeledEdgeList);
      topMiddleSplitPane.setDividerLocation(TOP_MIDDLE_DIVIDER_LOCATION);
      topLeftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
         chooser1.getGraphic(), topMiddleSplitPane);
      topLeftSplitPane.setDividerLocation(LEFT_DIVIDER_LOCATION);
      topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
         topLeftSplitPane, chooser2.getGraphic());
      topSplitPane.setDividerLocation(RIGHT_DIVIDER_LOCATION);
      mainSplitPane = new JSplitPane(
         JSplitPane.VERTICAL_SPLIT, topSplitPane, bottomPanel);
      mainSplitPane.setDividerLocation(MAIN_DIVIDER_LOCATION);
      Container contentPane = getContentPane();
      contentPane.removeAll();
      contentPane.add(mainSplitPane); //, "Center");
      setVisible(true);
   }

   public void newOrUpdatedGrammar(boolean tellEditor)
   {
      setTitle(ASDEditor.WINDOW_TITLE + editor.getInputFileName());
      chooser1.setGrammar(editor.getGrammar());
      chooser2.setGrammar(editor.getGrammar());
      String word = editor.getWordSelected1();
      String instance = "";
      if (word != null)
      {  chooser1.setWordSelected(word, tellEditor);
         instance = editor.getInstanceSelected1();
         if (instance != null)
            chooser1.setInstanceSelected(instance, tellEditor);
      }
      word = editor.getWordSelected2();
      if (word != null)
      {  chooser2.setWordSelected(word, tellEditor);
         instance = editor.getInstanceSelected2();
         if (instance != null)
            chooser2.setInstanceSelected(instance, tellEditor);
      }
      panelScrollPane = new JScrollPane(editor.getNetPanel());
      bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(radioPanel, BorderLayout.NORTH);
      bottomPanel.add(panelScrollPane, BorderLayout.CENTER);
      edgeListChanged();
   }

   ASDEditor getEditor() { return editor; }

   static final int DEFAULT_FRAME_WIDTH = 800;  // window width
   static final int DEFAULT_FRAME_HEIGHT = 600; // window height
   static final int DEFAULT_PANEL_HEIGHT = 1000;
   static final int MAIN_DIVIDER_LOCATION = 120;
   static final int LEFT_DIVIDER_LOCATION = 300;
   static final int TOP_MIDDLE_DIVIDER_LOCATION = 35;
   static final int RIGHT_DIVIDER_LOCATION = 450;

   private final String EDGES_LABEL = " > Edges >";
   private ASDEditor editor;
   private WordInstanceChooser chooser1;
   private WordInstanceChooser chooser2;
   private JScrollPane edgeListScrollPane;
   private JScrollPane panelScrollPane;
   private JPanel radioPanel;
   private JPanel bottomPanel;
   private JSplitPane topLeftSplitPane;
   private JSplitPane topMiddleSplitPane;
   private JSplitPane topSplitPane;
   private JSplitPane mainSplitPane;
   private NetPanel netPanel;
      // the panel in which the digraph nodes are displayed

   /**
      An instance defines what should happen when a window
      closes.
    */
   private class WindowCloser extends WindowAdapter
   {  public WindowCloser(ASDEditorFrame w)
      {  window = w;
      }

      public void windowClosing(WindowEvent e)
      {  ASDEditor editor = window.getEditor();
         if (!editor.getGrammarChanged())
            System.exit(0);
         // Give user the option of saving the
         // old grammar
         try
         {  editor.closingWindow();
         }
//         catch(IOException ex)
//         {  System.out.println("Error when trying to save grammar to a file.");
//         }
         catch(ClassNotFoundException ex) {}
         catch(InvocationTargetException ex) {}
         catch(InstantiationException ex) {}
         catch(IllegalAccessException ex) {}

         System.exit(0);        // stop the program
      }

      ASDEditorFrame window;
   } // end class WindowCloser

} // end class ASDEditorFrame

class FileMenu extends JMenu implements ActionListener
{  FileMenu(ASDEditorFrame w)
   {  super("File");
      window = w;
      editor = window.getEditor();
      JMenuItem newMenuItem = new JMenuItem("New",
         KeyEvent.VK_N);
      newMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_N, ActionEvent.ALT_MASK));
      JMenuItem openMenuItem = new JMenuItem("Open",
         KeyEvent.VK_O);
      openMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_O, ActionEvent.ALT_MASK));
      JMenuItem mergeMenuItem = new JMenuItem("Merge in another file",
         KeyEvent.VK_M);
      mergeMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_M, ActionEvent.ALT_MASK));
      JMenuItem revertMenuItem = new JMenuItem("Revert to last saved version",
         KeyEvent.VK_R);
      revertMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_R, ActionEvent.ALT_MASK));
      JMenuItem saveMenuItem = new JMenuItem("Save",
         KeyEvent.VK_S);
      saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_S, ActionEvent.ALT_MASK));
      JMenuItem saveAsMenuItem = new JMenuItem("Save As ...",
         KeyEvent.VK_A);
      saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_A, ActionEvent.ALT_MASK));
      JMenuItem exitMenuItem = new JMenuItem("Exit",
         KeyEvent.VK_X);
      exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_X, ActionEvent.ALT_MASK));
      add(newMenuItem);
      add(openMenuItem);
      add(mergeMenuItem);
      add(revertMenuItem);
      add(saveMenuItem);
      add(saveAsMenuItem);
      add(exitMenuItem);
      newMenuItem.addActionListener(this);
      openMenuItem.addActionListener(this);
      mergeMenuItem.addActionListener(this);
      revertMenuItem.addActionListener(this);
      saveMenuItem.addActionListener(this);
      saveAsMenuItem.addActionListener(this);
      exitMenuItem.addActionListener(this);
   }

   /**
     Listens for menu item events.
    */
   public void actionPerformed(ActionEvent e)
   {  String command = e.getActionCommand();
      if (command.equals("New"))
         try
         {  editor.newGrammar();
         }
//         catch(IOException ex)
//         {  System.out.println("Error when trying to save grammar to a file.");
//         }
         catch(ClassNotFoundException ex) {}
         catch(InvocationTargetException ex) {}
         catch(InstantiationException ex) {}
         catch(IllegalAccessException ex) {}
      else if (command.equals("Open"))
         try
         {  editor.loadNewFile();
         }
//         catch(IOException ex)
//         {  System.out.println("Error when trying to open a file.");
//         }
         catch(ClassNotFoundException ex) {}
         catch(InvocationTargetException ex) {}
         catch(InstantiationException ex) {}
         catch(IllegalAccessException ex) {}
      else if (command.equals("Merge in another file"))
         try
         {  editor.mergeInGrammar();
         }
//         catch(IOException ex)
//         {  System.out.println("Error when trying to open a file.");
//         }
         catch(ClassNotFoundException ex) {}
         catch(InvocationTargetException ex) {}
         catch(InstantiationException ex) {}
         catch(IllegalAccessException ex) {}
      else if (command.equals("Revert to last saved version"))
         try
         {  editor.revert();
         }
//         catch(IOException ex)
//         {  System.out.println(
//               "Error when trying to revert to last saved version.");
//         }
         catch(ClassNotFoundException ex) {}
         catch(InvocationTargetException ex) {}
         catch(InstantiationException ex) {}
         catch(IllegalAccessException ex) {}
      else if (command.equals("Save"))
         try
         {  editor.saveFile();
         }
//         catch(IOException ex)
//         {  System.out.println("Error when trying to save grammar to a file.");
//         }
         catch(ClassNotFoundException ex) {}
         catch(InvocationTargetException ex) {}
         catch(InstantiationException ex) {}
         catch(IllegalAccessException ex) {}
      else if (command.equals("Save As ..."))
         try
         {  editor.saveAsFile();
         }
//         catch(IOException ex)
//         {  System.out.println("Error when trying to save grammar to a file.");
//         }
         catch(ClassNotFoundException ex) {}
         catch(InvocationTargetException ex) {}
         catch(InstantiationException ex) {}
         catch(IllegalAccessException ex) {}

      else if (command.equals("Exit"))
         try
         {  editor.exitEditor();
         }
//         catch(IOException ex)
//         {  System.out.println("Error when trying to save grammar to a file.");
//         }
         catch(ClassNotFoundException ex) {}
         catch(InvocationTargetException ex) {}
         catch(InstantiationException ex) {}
         catch(IllegalAccessException ex) {}
   }

   ASDEditor editor;
   ASDEditorFrame window;

} // end class FileMenu

class EditMenu extends JMenu implements ActionListener
{  EditMenu(ASDEditorFrame w)
   {  super("Edit");
      window = w;
      editor = window.getEditor();
      JMenuItem addWordMenuItem = new JMenuItem("Add word or PHRASE TYPE",
         KeyEvent.VK_W);
      addWordMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_W, ActionEvent.ALT_MASK));
      add(addWordMenuItem);
      addWordMenuItem.addActionListener(this);
      JMenuItem addInstanceMenuItem = new JMenuItem("Add instance",
         KeyEvent.VK_I);
      addInstanceMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_I, ActionEvent.ALT_MASK));
      add(addInstanceMenuItem);
      addInstanceMenuItem.addActionListener(this);
      JMenuItem addEdgeMenuItem = new JMenuItem("Add edge",
         KeyEvent.VK_G);
      addEdgeMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_G, ActionEvent.ALT_MASK));
      addEdgeMenuItem.addActionListener(this);
      add(addEdgeMenuItem);
      JMenuItem deleteEdgeMenuItem = new JMenuItem("Delete selected edge",
         KeyEvent.VK_D);
      deleteEdgeMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_D, ActionEvent.ALT_MASK));
      deleteEdgeMenuItem.addActionListener(this);
      add(deleteEdgeMenuItem);
   }

   /**
     Listens for menu item events.
    */
   public void actionPerformed(ActionEvent e)
   {  String command = e.getActionCommand();
      if (command.equals("Add word or PHRASE TYPE"))
         editor.addWord();
      else if (command.equals("Add instance"))
         editor.addInstance();
      else if (command.equals("Add edge"))
         try
         {  editor.addEdge();
         }
         catch(ClassNotFoundException ex) {}
         catch(InvocationTargetException ex) {}
         catch(InstantiationException ex) {}
         catch(IllegalAccessException ex) {}
      else if (command.equals("Delete selected edge"))
         editor.deleteEdge();
   }

   ASDEditor editor;
   ASDEditorFrame window;

} // end class EditMenu

class ViewMenu extends JMenu implements ActionListener
{  ViewMenu(ASDEditorFrame w)
   {  super("View");
      window = w;
      editor = window.getEditor();

      JMenu showSubMenu = new JMenu("Show");
      showSubMenu.setMnemonic('s');
      JMenu hideSubMenu = new JMenu("Hide");
      hideSubMenu.setMnemonic('h');
      add(showSubMenu);
      JMenuItem allMenuItem = new JMenuItem("All nodes and edges",
         KeyEvent.VK_A);
      allMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_3, ActionEvent.ALT_MASK));
      showSubMenu.add(allMenuItem);
      allMenuItem.addActionListener(this);
      JMenuItem nonSingletonItem = new JMenuItem(
         "all Non-singleton nodes and edges", KeyEvent.VK_N);
      nonSingletonItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_2, ActionEvent.ALT_MASK));
      showSubMenu.add(nonSingletonItem);
      nonSingletonItem.addActionListener(this);
      JMenuItem singletonItem = new JMenuItem(
         "all Singleton nodes", KeyEvent.VK_S);
      singletonItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_1, ActionEvent.ALT_MASK));
      showSubMenu.add(singletonItem);
      singletonItem.addActionListener(this);

      add(hideSubMenu);
      JMenuItem entireMenuItem = new JMenuItem("Entire network display",
         KeyEvent.VK_E);
      entireMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_0, ActionEvent.ALT_MASK));
      hideSubMenu.add(entireMenuItem);
      entireMenuItem.addActionListener(this);
      hideSubMenu.addSeparator();
      JMenuItem nonSingletonMenuItem = new JMenuItem("Non-singleton nodes",
         KeyEvent.VK_N);
      hideSubMenu.add(nonSingletonMenuItem);
      nonSingletonMenuItem.addActionListener(this);
      JMenuItem singletonMenuItem = new JMenuItem("Singleton nodes",
         KeyEvent.VK_S);
      hideSubMenu.add(singletonMenuItem);
      singletonMenuItem.addActionListener(this);
      JMenuItem selectedMenuItem = new JMenuItem("selected Component(s)",
         KeyEvent.VK_C);
      selectedMenuItem.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_C, ActionEvent.ALT_MASK));
      hideSubMenu.add(selectedMenuItem);
      selectedMenuItem.addActionListener(this);
   }

   /**
     Listens for menu item events.
    */
   public void actionPerformed(ActionEvent e)
   {  String command = e.getActionCommand();
      if (command.equals("All nodes and edges"))
         editor.showAll();
      else if (command.equals("all Non-singleton nodes and edges"))
         editor.showAllNonSingletons();
      else if (command.equals("all Singleton nodes"))
         editor.showAllSingletons();
      else if (command.equals("Entire network display"))
         editor.clearNetPanel();
      else if (command.equals("selected Component(s)"))
         editor.hideSelectedComponents();
      else if (command.equals("Singleton nodes"))
         editor.hideSingletons();
      else if (command.equals("Non-singleton nodes"))
         editor.hideNonSingletons();
   }

   ASDEditor editor;
   ASDEditorFrame window;

} // end class ViewMenu


class HelpMenu extends JMenu implements ActionListener
{  HelpMenu(ASDEditorFrame w)
   {  super("Help");
      window = w;
      editor = window.getEditor();
      JMenuItem aboutMenuItem = new JMenuItem("About ASDEditor",
         KeyEvent.VK_A);
      add(aboutMenuItem);
      aboutMenuItem.addActionListener(this);
   }

   /**
      Listens for menu item events.
    */
   public void actionPerformed(ActionEvent e)
   {  String command = e.getActionCommand();
      if (command.equals("About ASDEditor"))
         editor.showAboutInfo();
   }

   ASDEditor editor;
   ASDEditorFrame window;
} // end class HelpMenu

/**
   Defines listeners for the radio buttons
 */
class RadioListener implements ActionListener
{  RadioListener(ASDEditor editor)
   {  currentEditor = editor;
   }

   public void actionPerformed(ActionEvent e)
   {  if (e.getActionCommand().equals("left"))
      {  if (currentEditor != null)
            currentEditor.setListSelected(ASDEditor.LEFT_LIST);
      }
      else if (e.getActionCommand().equals("right"))
      {  if (currentEditor != null)
            currentEditor.setListSelected(ASDEditor.RIGHT_LIST);
      }
   }

   ASDEditor currentEditor;
} // end class RadioListener
