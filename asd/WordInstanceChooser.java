/*

Copyright 2002 James A. Mason

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
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;    // JList, JSplitPane, JScrollPane
import javax.swing.event.*;  // ListSelectionListener

class WordInstanceChooser extends JComponent
{
   WordInstanceChooser(ASDEditor givenEditor, ASDGrammar givenGrammar)
   {  editor = givenEditor;
      setGrammar(givenGrammar);
   }

   void setGrammar(ASDGrammar newGrammar)
   {  grammar = newGrammar;
      wordList = new JList(new WordListModel(grammar));
      wordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      wordList.getSelectionModel().addListSelectionListener(
         new WordListSelectionListener(this));
      WordListMenu menu = new WordListMenu(wordList, editor, this);
      MouseListener popupListener = new PopupListener(menu);
      wordList.addMouseListener(popupListener);
      JScrollPane wordScrollPane = new JScrollPane(wordList);
      JPanel labeledWordList = new JPanel();
      labeledWordList.setLayout(new BorderLayout());
      labeledWordList.add(new JLabel("Words/Phrase Types"),
         BorderLayout.NORTH);
      labeledWordList.add(wordScrollPane, BorderLayout.CENTER);
      instanceScrollPane = new JScrollPane();
      JPanel labeledInstanceList = new JPanel();
      labeledInstanceList.setLayout(new BorderLayout());
      labeledInstanceList.add(new JLabel("Instances"),
         BorderLayout.NORTH);
      labeledInstanceList.add(instanceScrollPane, BorderLayout.CENTER);
      graphicView = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
         labeledWordList, labeledInstanceList);
      graphicView.setDividerLocation(DIVIDER_LOCATION);
      repaint();
   }

   JSplitPane getGraphic() { return graphicView; }

   String getInstanceSelected() { return instanceSelected; }

   String getWordSelected() { return wordSelected; }

   void setInstanceSelected(String newInstance, boolean tell)
   {  tellEditor = tell;
      instanceList.setSelectedValue(newInstance, true);
   }

   void setWordSelected(String newWord, boolean tell)
   {  tellEditor = tell;
      wordList.setSelectedValue(newWord, true);
   }

   void newInstanceSelected()
   {  instanceSelected = (String)instanceList.getSelectedValue();
      if (tellEditor)
         editor.wordInstanceChooserChanged(this);
      tellEditor = true;
   }

   void newWordSelected()
   {  wordSelected = (String)wordList.getSelectedValue();
      instanceList = new JList(new InstanceListModel(
         grammar, wordSelected));
      instanceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      // Create an InstanceSelectionListener and menu
      // for the list of instances:
      instanceList.getSelectionModel().addListSelectionListener(
         new InstanceSelectionListener(this));
      InstanceListMenu menu
         = new InstanceListMenu(wordList, instanceList, editor, this);
      MouseListener popupListener = new PopupListener(menu);
      instanceList.addMouseListener(popupListener);

      instanceScrollPane.getViewport().setView(instanceList);
      instanceSelected = null; // no instance currently selected
      if (tellEditor)
         editor.wordInstanceChooserChanged(this);
      tellEditor = true;
   }

   static final int DIVIDER_LOCATION = 200;
   private boolean tellEditor = true;
      // indicates whether the editor should be informed of
      // a change in state.
   private ASDGrammar grammar;
   private ASDEditor editor;
   private JList wordList;
   private String wordSelected;
   private JList instanceList;
   private String instanceSelected;
   private JScrollPane instanceScrollPane;
   private JSplitPane graphicView;

} // end class WordInstanceChooser

/**
   Defines list models for an ASD grammar's list of words
 */
class WordListModel extends DefaultListModel
{
   WordListModel(ASDGrammar grammar)
   {  Set entrySet = grammar.lexicon().entrySet();
      ArrayList words = new ArrayList(entrySet.size());
      for (Iterator it = entrySet.iterator(); it.hasNext(); )
      {  Map.Entry e = (Map.Entry) it.next();
         String word = (String) e.getKey();
         words.add(word);
      }
      Object[] wordArray = words.toArray();
      if (words.size() > 1)
//       Arrays.sort(wordArray);
         Arrays.sort(wordArray, new WordComparator());
      for (int j= 0; j < wordArray.length; j++)
      {  this.addElement((String) wordArray[j]);
      }
   }
}

class WordComparator implements Comparator
{  public int compare(Object word1, Object word2)
   {  return ((String)word1).compareTo((String)word2);
   }
}


/**
   Defines a selection listener for a WordInstanceChooser's list of words.
 */
class WordListSelectionListener implements ListSelectionListener
{  WordListSelectionListener(WordInstanceChooser chooser)
   {  currentChooser = chooser;
   }

   public void valueChanged(ListSelectionEvent e)
   {  if (currentChooser != null && !e.getValueIsAdjusting())
         currentChooser.newWordSelected();
   }

   WordInstanceChooser currentChooser;
}

/**
   Defines list models for a word's list of instances
 */
class InstanceListModel extends DefaultListModel
{
   InstanceListModel(ASDGrammar grammar, String word)
   {  ArrayList instanceList = grammar.lookupWord(word);
      for (Iterator it = instanceList.iterator(); it.hasNext(); )
      {  ASDGrammarNode n = (ASDGrammarNode) it.next();
         String instance = (String) n.instance();
         this.addElement(instance);
      }
   }
}

/**
   Defines a selection listener for a word's list of instances.
 */
class InstanceSelectionListener implements ListSelectionListener
{  InstanceSelectionListener(WordInstanceChooser chooser)
   {  currentChooser = chooser;
   }

   public void valueChanged(ListSelectionEvent e)
   {  if (currentChooser != null && !e.getValueIsAdjusting())
         currentChooser.newInstanceSelected();
   }

   WordInstanceChooser currentChooser;
}

class InstanceListMenu extends JPopupMenu implements ActionListener
{  InstanceListMenu(JList wList, JList iList, ASDEditor ed,
         WordInstanceChooser ch)
   {  wordList = wList;
      instanceList = iList;
      editor = ed;
      chooser = ch;
      setInvoker(iList);
      JMenuItem addMenuItem = new JMenuItem("Add instance");
      addMenuItem.addActionListener(this);
      add(addMenuItem);
      JMenuItem deleteMenuItem = new JMenuItem("Delete instance");
      deleteMenuItem.addActionListener(this);
      add(deleteMenuItem);
      addSeparator();
      JMenuItem initialMenuItem = new JMenuItem("Toggle Initial instance");
      initialMenuItem.addActionListener(this);
      add(initialMenuItem);
      JMenuItem finalMenuItem = new JMenuItem("Toggle Final instance");
      finalMenuItem.addActionListener(this);
      add(finalMenuItem);
      addSeparator();
      JMenuItem editActionMenuItem = new JMenuItem("Edit semantic action");
      editActionMenuItem.addActionListener(this);
      add(editActionMenuItem);
      JMenuItem editValueMenuItem = new JMenuItem("Edit semantic value");
      editValueMenuItem.addActionListener(this);
      add(editValueMenuItem);
   }

   public void actionPerformed(ActionEvent e)
   {  if (editor == null) return;
      String command = e.getActionCommand();
      String word = (String)wordList.getSelectedValue();
      if (command.equals("Add instance"))
      {  editor.addInstanceOf(word, chooser);
         return;
      }
      String instance = (String)instanceList.getSelectedValue();
      if (command.equals("Edit semantic action"))
         editor.editAction(word, instance);
      else if (command.equals("Edit semantic value"))
         editor.editValue(word, instance);
      else if (command.equals("Toggle Initial instance"))
         editor.toggleInitial(word, instance, chooser);
      else if (command.equals("Toggle Final instance"))
         editor.toggleFinal(word, instance, chooser);
      else if (command.equals("Delete instance"))
      {  editor.deleteInstance(word, instance, chooser);
      }
   }

   ASDEditor editor;
   WordInstanceChooser chooser;
   JList wordList; // the word list corresponding to the instance list.
   JList instanceList; // the instance list to which the menu is attached.
} // end class InstanceListMenu

class WordListMenu extends JPopupMenu implements ActionListener
{  WordListMenu(JList wList, ASDEditor ed, WordInstanceChooser ch)
   {  wordList = wList;
      editor = ed;
      chooser = ch;
      setInvoker(wList);
      JMenuItem addWordMenuItem = new JMenuItem("Add word or PHRASE TYPE");
      addWordMenuItem.addActionListener(this);
      add(addWordMenuItem);
      JMenuItem addInstanceMenuItem = new JMenuItem("Add instance");
      addInstanceMenuItem.addActionListener(this);
      add(addInstanceMenuItem);
      JMenuItem deleteMenuItem = new JMenuItem("Delete word");
      deleteMenuItem.addActionListener(this);
      add(deleteMenuItem);
   }

   public void actionPerformed(ActionEvent e)
   {  if (editor == null) return;
      String command = e.getActionCommand();
      if (command.equals("Add instance"))
      {  String word = (String)wordList.getSelectedValue();
         editor.addInstanceOf(word, chooser);
      }
      else if (command.equals("Add word or PHRASE TYPE"))
         editor.addWord(chooser);
      else if (command.equals("Delete word"))
      {  String word = (String)wordList.getSelectedValue();
         editor.deleteWord(word);
      }
   }

   ASDEditor editor;
   WordInstanceChooser chooser;
   JList wordList; // the word list to which the menu is attached.
} // end class WordListMenu
