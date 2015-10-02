/*

Copyright 2003-2007 James A. Mason

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

import java.awt.*;       // Container, Graphics,
                         // Graphics2D, Rectangle, Color
import java.awt.event.*; // ActionEvent, ActionListener,
                         // WindowAdapter, WindowEvent
import javax.swing.*;    // JFrame, JPanel, JButton
import javax.swing.event.*;  // ListSelectionListener
import javax.swing.filechooser.*;

/**
   An ASDEditor permits a user to edit an ASD grammar.  It provides
   a graphical user interface, including display of the grammar as a
   network of nodes and edges.
<BR><BR>
   Command-line usage:
<BR><tt><b> java -cp asddigraphs.jar asd/ASDEditor
<BR>java -cp asddigraphs.jar asd/ASDEditor grammarFileName</b></tt>
<BR>or, if asddigraphs.jar has been put in the system classpath:
<BR><tt><b> java asd/ASDEditor
<BR>java asd/ASDEditor grammarFileName</b></tt>

   @author James A. Mason
   @version 1.09a  2003 Jul; 2004 Jan; 2005 Oct; 2007 Aug
 */
public class ASDEditor
{  /**
      The main driver for the grammar editor
    */
   public static void main(String[] args)
      throws IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  String inFile = null;
      if (args.length > 0)
      {  inFile = args[0];
         new ASDEditor(inFile);
      }
      else
         new ASDEditor();
   }

  /**
      Open an ASDEditor window for a new ASD grammar.
    */
   ASDEditor()
      throws // IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  grammarDigraph = new ASDDigraph(new ASDGrammar(), null);
      inputFileName = DEFAULT_FILE_NAME;
      setGrammarChanged(false);

      // Create two WordInstanceChoosers for the editor and grammar:
      chooser1 = new WordInstanceChooser(this, getGrammar());
      chooser2 = new WordInstanceChooser(this, getGrammar());

      // Create a panel in which to display the grammar network:
      netPanel = new NetPanel(this);
      netPanel.setLayout(null);  // uses absolute layout
      grammarDigraph.setPanel(netPanel);

      // Create JRadioButton instances:
      leftListButton = new JRadioButton("left list/node");
      leftListButton.setActionCommand("left");
      leftListButton.setSelected(true);
      rightListButton = new JRadioButton("right list/node");
      rightListButton.setActionCommand("right");
      ButtonGroup buttonGroup = new ButtonGroup();
      buttonGroup.add(leftListButton);
      buttonGroup.add(rightListButton);
      RadioListener buttonListener = new RadioListener(this);
      leftListButton.addActionListener(buttonListener);
      rightListButton.addActionListener(buttonListener);

      // Create a new phrase type field and edgeList for the editor:
      phraseTypeField = new JTextField(10);
      phraseTypeField.addActionListener(new PhraseTypeFieldListener(this));
      setPhraseTypeField(null);
      edgeList = new EdgeList(this);

      currentDirectory = new File(".", "");
         // directory from which the editor was invoked
      setGrammarChanged(false);

      // Create a window for the editor, and open it:
      window = new ASDEditorFrame(this,
         chooser1, chooser2, netPanel, leftListButton, rightListButton);
      window.setTitle(
         WINDOW_TITLE
            + inputFileName);
      window.setVisible(true);
   }

   /**
      Open an ASDEditor window on a specified ASD grammar file
      @param fileName the name of the ASD grammar file
    */
   ASDEditor(String fileName)
      throws // IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  this();  // invoke the default constructor

      // Load the grammar and create an ASDDigraph for it:
      inputFileName = fileName;
      boolean successfulLoad = true;
      try
      {  grammarDigraph = new ASDDigraph(inputFileName, netPanel);
      }
      catch (ASDInputException e)
      {  JOptionPane.showMessageDialog(window, e.getMessage(),
            "Syntax error in grammar file " + inputFileName,
            JOptionPane.ERROR_MESSAGE);
         successfulLoad = false;
      }
      catch(IOException e)
      {  JOptionPane.showMessageDialog(window,
            inputFileName + " not found",
            "Couldn't load grammar file.",
            JOptionPane.ERROR_MESSAGE);
         successfulLoad = false;
      }
      if (successfulLoad)
         window.newOrUpdatedGrammar(true);
   }

   void addEdge()
      throws ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {
      if (eNodeSelected1 != null &&
          eNodeSelected1.getDigraphNode().getGrammarNode().isFinal())
      {  JOptionPane.showMessageDialog(window,
            "Edges cannot be added from a final node.\n" +
            "(The left node selected is a final node.)");
         return;
      }
      if (eNodeSelected1 == null || eNodeSelected2 == null)
      {  JOptionPane.showMessageDialog(window,
            "First select two nodes that the edge should connect.\n"
            + "Then try again.");
         return;
      }
      ASDDigraphEdge dEdge = grammarDigraph.addEdgeFromNodeToNode(
         eNodeSelected1.getDigraphNode(), eNodeSelected2.getDigraphNode());
      setGrammarChanged(true);
      ASDEditEdge eEdge = dEdge.getEditEdge();
      eEdge.setContext(netPanel);
      netPanel.addEdge(eEdge);
      eEdge.setDefaultCoordinates();
      edgeList = new EdgeList(this,
         new EdgeListModel(eNodeSelected1.getDigraphNode()));
      edgeList.getSelectionModel().addListSelectionListener(
         new EdgeListSelectionListener(this));
      ArrayList edges = eNodeSelected1.getDigraphNode().getOutEdges();
      int index = edges.indexOf(eEdge.getDigraphEdge());
      edgeList.setSelectedIndex(index);
      window.edgeListChanged();
   }

   void addInstance()
   {  // invoked from EditMenu
      if (listSelected == LEFT_LIST)
      {  if (wordSelected1 == null)
         {  JOptionPane.showMessageDialog(window,
               "First select a word.\nThen try again.");
            return;
         }
         addInstanceOf(wordSelected1, chooser1);
      }
      else if (listSelected == RIGHT_LIST)
      {  if (wordSelected2 == null)
         {  JOptionPane.showMessageDialog(window,
               "First select a word.\nThen try again.");
            return;
         }
         addInstanceOf(wordSelected2, chooser2);
      }
   }

   void addInstanceOf(String word, WordInstanceChooser chooser)
   {  // invoked from InstanceListMenu and WordListMenu
      if (word == null)  // no word selected
         {  JOptionPane.showMessageDialog(window,
               "A word must be selected first.");
            return;
         }
      boolean initialInstance = false;
      boolean finalInstance = false;
      String phraseTypeEnded = null;

      ArrayList wordEntry = getGrammar().lookupWord(word);
      if (wordEntry == null)
      {  JOptionPane.showMessageDialog(window,
            "A serious error has occurred in " +
            "ASDEditor.addInstanceOf\n" +
            "Please inform jmason@yorku.ca of this error.");
         return;
      }

      if (chooser == chooser1)
      {  if (!leftListButton.isSelected())
            leftListButton.doClick();
      }
      else if (chooser == chooser2)
      {  if (!rightListButton.isSelected())
            rightListButton.doClick();
      }

      // Get from user:
      // * whether the instance is initial or not
      int initialOption = JOptionPane.showConfirmDialog(window,
         "Can the new instance BEGIN a phrase?");
      if (initialOption == JOptionPane.CANCEL_OPTION)
         return;
      if (initialOption == JOptionPane.YES_OPTION)
         initialInstance = true;

      // * whether the instance is final or not, and if so,
      // * what phrase type it ends.
      do
      {  finalInstance = false;
         phraseTypeEnded = JOptionPane.showInputDialog(window,
            "If the instance ends a phrase, " +
            "what TYPE of phrase does it end?");
         if (phraseTypeEnded == null) break; // not a final instance
         phraseTypeEnded = phraseTypeEnded.trim();
         if (phraseTypeEnded.length() == 0)
         {  phraseTypeEnded = null; // empty phrase type treated as none
            break;
         }
         finalInstance = true;
         if (Character.isLetter(phraseTypeEnded.charAt(0))) break;
         JOptionPane.showMessageDialog(window,
            "Phrase type names must begin with letters");
      } while(true);

      // Create new instance of the word in the ASDGrammar:
      ArrayList successors = null;  // indicates a FINAL node
      String instanceNumber = (wordEntry.size()+1)+"";
      if (!finalInstance)
         successors = new ArrayList();
      ASDGrammarNode gNode = new ASDGrammarNode(
         word, instanceNumber, initialInstance,
         null,            // unspecified beginsTypes
         successors,
         null,            // unspecified successorTypes
         phraseTypeEnded,
         null,            // no semantic value
         null);           // no semantic action

      // Create new node for the word instance in the ASDDigraph,
      // including a new ASDEditNode displayed in the NetPanel.
      ASDDigraphNode dNode = grammarDigraph.addDigraphNode(gNode);
      ASDEditNode eNode = null;
      if (listSelected == LEFT_LIST && eNodeSelected1 != null)
         eNode = new ASDEditNode(gNode, getNetPanel(),
                    eNodeSelected1.getX() + 10, eNodeSelected1.getY() + 10);
      else if (listSelected == RIGHT_LIST && eNodeSelected2 != null)
         eNode = new ASDEditNode(gNode, getNetPanel(),
                    eNodeSelected2.getX() + 10, eNodeSelected2.getY() + 10);
      else
         eNode = new ASDEditNode(gNode, getNetPanel(), 5, 5);
      dNode.setEditNode(eNode);
      eNode.setDigraphNode(dNode);
      netPanel.addNode(eNode);
      wordEntry.add(gNode);
      setGrammarChanged(true);
      if (chooser == chooser1 && eNodeSelected1 != null)
      {  if (eEdgeSelected != null &&
             eEdgeSelected.getFromNode() == eNodeSelected1)
         {  eEdgeSelected.setSelected(false);
            eEdgeSelected.repaint();
            eEdgeSelected = null;
         }
         if (eNodeSelected1 != eNodeSelected2)
         {  eNodeSelected1.setSelected(false);
            eNodeSelected1.repaint();
         }
      }
      else if (chooser == chooser2 && eNodeSelected2 != null)
      {  if (eEdgeSelected != null &&
             eEdgeSelected.getToNode() == eNodeSelected2)
         {  eEdgeSelected.setSelected(false);
            eEdgeSelected.repaint();
            eEdgeSelected = null;
         }  if (eNodeSelected2 != eNodeSelected1)
         {  eNodeSelected2.setSelected(false);
            eNodeSelected2.repaint();
         }
      }

      if (chooser == chooser1)
      {  wordSelected1 = word;
         instanceSelected1 = instanceNumber;
         eNodeSelected1 = eNode;
      }
      else if (chooser == chooser2)
      {  wordSelected2 = word;
         instanceSelected2 = instanceNumber;
         eNodeSelected2 = eNode;
      }

      window.choosersChanged();

      // Restore the left or right list button appropriately:
      if (chooser == chooser1)
      {  if (!leftListButton.isSelected())
            leftListButton.doClick();
      }
      else if (chooser == chooser2)
      {  if (!rightListButton.isSelected())
            rightListButton.doClick();
      }
      eNode.setSelected(true);
      eNode.repaint();

   } // end addInstanceOf

   String addWord()
   {  // invoked from EditMenu
      String newWord = null;
      do
      {  newWord = JOptionPane.showInputDialog(window,
           "What is the new word or PHRASE TYPE?");
         if (newWord == null)  // User cancelled the operation.
            return null;
         newWord = newWord.trim();
      } while(newWord.length() == 0);

      if (getGrammar().lookupWord(newWord) != null)
      {  JOptionPane.showMessageDialog(window,
            "\"" + newWord + "\" is already in the grammar");
         return null;
      }
      getGrammar().lexicon().put(newWord, new ArrayList());
      setGrammarChanged(true);
      window.newOrUpdatedGrammar(true);
      return newWord;
   }

   void addWord(WordInstanceChooser chooser)
   {  // invoked from WordListMenu
      String newWord = addWord();
      if (newWord != null)
      {  chooser.setWordSelected(newWord, true);
         wordInstanceChooserChanged(chooser);
      }
   }

   void changedText(String newText)
   {  if (grammarNodeBeingEdited == null) return; // for safety
      if (oldTextBeingEdited.equals(newText)) return; // no change
      if (fieldBeingEdited.equals("action"))
         grammarNodeBeingEdited.setSemanticAction(newText);
      else if (fieldBeingEdited.equals("value"))
         grammarNodeBeingEdited.setSemanticValue(newText);
      setGrammarChanged(true);
      grammarNodeBeingEdited = null;
      fieldBeingEdited = null;
      oldTextBeingEdited = null;
   }

   void clearNetPanel()
   {  netPanel.clear();
      boolean choosersChanged = false;
      if (eNodeSelected1 != null)
      {  eNodeSelected1.setSelected(false);
         eNodeSelected1 = null;
         instanceSelected1 = null;
         choosersChanged = true;
      }
      if (eNodeSelected2 != null)
      {  eNodeSelected2.setSelected(false);
         eNodeSelected2 = null;
         instanceSelected2 = null;
         choosersChanged = true;
      }
      if (eEdgeSelected != null)
      {  eEdgeSelected.setSelected(false);
         eEdgeSelected = null;
         choosersChanged = true;
      }
      if (choosersChanged)
         window.choosersChanged();
   }

   boolean closingGrammar()
      throws // IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  int option = JOptionPane.showConfirmDialog(window,
         "Do you want to save the grammar?",
         "The grammar has changed.",
         JOptionPane.YES_NO_CANCEL_OPTION);
      if (option == JOptionPane.CANCEL_OPTION)
         return false;  // do not close the old grammar edit session
      else if (option == JOptionPane.YES_OPTION)
      {  if (saveFile())
            return true; // old grammar edit session has ended
         else
            return false; // invalid save attempt
      }
      else
         return true; // old grammar edit session has ended
                      // without saving to the old file
   }

   void closingWindow()
      throws // IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  int option = JOptionPane.showConfirmDialog(window,
         "Do you want to save the grammar?",
         "The grammar has changed.",
         JOptionPane.YES_NO_OPTION);
      if (option == JOptionPane.YES_OPTION)
         saveFile();
   }

  void deleteEdge()
      // invoked by NetPanelMenu actionPerformed
   {  deleteEdge(eEdgeSelected);
   }

   void deleteEdge(ASDDigraphEdge d)
   {  grammarDigraph.removeEdge(d);
      setGrammarChanged(true);
      // Create an updated edgelist for the left node:
      edgeList = new EdgeList(this,
         new EdgeListModel(eNodeSelected1.getDigraphNode()));
      edgeList.getSelectionModel().addListSelectionListener(
         new EdgeListSelectionListener(this));
      window.edgeListChanged();
   }

   void deleteEdge(ASDEditEdge e)
   {  if (e == null)
      {  JOptionPane.showMessageDialog(window,
            "An edge must be selected for deletion first.");
         return;
      }
      if (e != eEdgeSelected)
         e.doClick();
      netPanel.removeEdge(e);
      eEdgeSelected = null;
      deleteEdge(e.getDigraphEdge());
   }

   void deleteEdge(int edgeIndex)
      // invoked by EdgeListMenu actionPerformed
   {  ASDEditEdge eEdge = null;
      if (edgeIndex >= 0)  // edge actually selected
      {  ArrayList edges = eNodeSelected1.getDigraphNode().getOutEdges();
         ASDDigraphEdge dEdge = (ASDDigraphEdge)edges.get(edgeIndex);
         eEdge = dEdge.getEditEdge();
      }
      deleteEdge(eEdge);
   }

   void deleteInstance(String word, String givenInstance,
         WordInstanceChooser chooser)
      // invoked by InstanceListMenu actionPerformed
   {  if (givenInstance == null)  // no instance selected
      {  JOptionPane.showMessageDialog(window,
            "An instance/node must be selected for deletion first.");
         return;
      }
      ASDDigraphNode dNode
         = getGrammarDigraph().lookupNode(word, givenInstance);
      if (dNode.inDegree() > 0 || dNode.outDegree() > 0)
      {  JOptionPane.showMessageDialog(window,
            "All edges connected with the node must be deleted first");
         return;
      }
      ASDEditNode eNode = dNode.getEditNode();
      boolean eNodeDeleted = deleteNode(eNode);
      if (eNodeDeleted)
         chooser.setWordSelected(word, true);
   }

   private void deleteNode(ASDDigraphNode dNode)
   {  if (dNode == null)
      {  JOptionPane.showMessageDialog(window,
            "A serious error has occurred in " +
            "ASDEditor.deleteNode(ASDDigraphNode).\n" +
            "Please inform jmason@yorku.ca of this error.");
      }
      else
      {  grammarDigraph.removeNode(dNode);
         setGrammarChanged(true);
         window.choosersChanged();
      }
   }

   boolean deleteNode(ASDEditNode eNode)
   {  if (eNode == null)
      {  JOptionPane.showMessageDialog(window,
            "No node (instance) has been selected for deletion.");
         return false;
      }
      ASDDigraphNode dNode = eNode.getDigraphNode();
      if (dNode.inDegree() > 0 || dNode.outDegree() > 0)
      {  JOptionPane.showMessageDialog(window,
            "All edges connected with the node must be deleted first");
         return false;
      }
      nodeSelected(eNode); // prevents a null pointer error
         // in trying to select a word after a node has been deleted
         // before any words or instances have been selected.
      ASDGrammarNode gNode = eNode.getGrammarNode();
      if (eNode == eNodeSelected1)
      {  eNodeSelected1 = null;
         instanceSelected1 = null;
      }
      else if (eNodeSelected1 != null && gNode.word().equals(wordSelected1))
      {  if (eEdgeSelected != null &&
               (eEdgeSelected.getFromNode() == eNodeSelected1
               || eEdgeSelected.getToNode() == eNodeSelected1))
         {  eEdgeSelected.setSelected(false);
            eEdgeSelected.repaint();
            eEdgeSelected = null;
         }
         eNodeSelected1.setSelected(false);
         eNodeSelected1.repaint();
         eNodeSelected1 = null;
         instanceSelected1 = null;
      }
      if (eNode == eNodeSelected2)
      {  eNodeSelected2 = null;
         instanceSelected2 = null;
      }
      else if (eNodeSelected2 != null && gNode.word().equals(wordSelected2))
      {  if (eEdgeSelected != null &&
               (eEdgeSelected.getFromNode() == eNodeSelected2
               || eEdgeSelected.getToNode() == eNodeSelected2))
         {  eEdgeSelected.setSelected(false);
            eEdgeSelected.repaint();
            eEdgeSelected = null;
         }
         eNodeSelected2.setSelected(false);
         eNodeSelected2.repaint();
         eNodeSelected2 = null;
         instanceSelected2 = null;
      }
      netPanel.removeNode(eNode);
      deleteNode(eNode.getDigraphNode());
      return true;
   } // end deleteNode

   void deleteWord(String word)
   {  // invoked from WordListMenu
      if (word == null)
      {  JOptionPane.showMessageDialog(window,
            "A word must be selected for deletion first.");
         return;
      }
      ArrayList wordEntry = getGrammar().lookupWord(word);
      if (wordEntry == null)
      {  JOptionPane.showMessageDialog(window,
            "\"" + word + "\" is not in the grammar");
         return;
      }
      if (wordEntry.size() > 0)
      {  JOptionPane.showMessageDialog(window,
            "All instances of the word must be deleted first.");
         return;
      }
      // Note: wordSelected1 or wordSelected2 may be null when the
      // following statements are executed.  So the comparisons
      // wordSelected1.equals(word) and wordSelected2.equals(word)
      // can yield null pointer exceptions and are NOT appropriate.
      if (word.equals(wordSelected1))
         wordSelected1 = null;
      if (word.equals(wordSelected2))
         wordSelected2 = null;
      getGrammar().lexicon().remove(word);
      setGrammarChanged(true);
      window.newOrUpdatedGrammar(true);
   }

   private void deselectEdge(boolean updateEdgeList)
   {  // used to deselect the currently selected eEdgeSelected;
      // it also updates the currently displayed edge list,
      // if updateEdgeList is true
      if (eEdgeSelected == null) return;
      eEdgeSelected.setSelected(false);
      eEdgeSelected.repaint();
      eEdgeSelected = null;
      if (updateEdgeList)
         if (eNodeSelected1 != null)
         {  edgeList = new EdgeList(this, new EdgeListModel(
                  eNodeSelected1.getDigraphNode()));
            edgeList.getSelectionModel().addListSelectionListener(
               new EdgeListSelectionListener(this));
            // Set the phrase type that ends at the selected instance
            // (null if none):
            setPhraseTypeField(
               eNodeSelected1.getGrammarNode().phraseType());
         }
         else
         {  edgeList = new EdgeList(this);
            setPhraseTypeField(null);
         }
            window.edgeListChanged();
   }

   private void deselectNodeHelp(ASDEditNode eNode)
   {  // also deselect the eEdgeSelected, if it's adjacent
      // to the given eNode
      if (eNode == null ) return;
      if (eEdgeSelected != null)
      {  ASDEditNode fromNode = eEdgeSelected.getFromNode();
         ASDEditNode toNode = eEdgeSelected.getToNode();
         if (eNode == toNode)
            deselectEdge(true);  // update the edge list also
         else if (eNode == fromNode)
         {  if (fromNode != toNode)
               deselectEdge(false);  // don't try to update the
               // edge list displayed; it will be changed anyway
            else
               deselectEdge(true); // update the edge list also
         }
      }
   }

   void edgeSelected(ASDEditEdge edge)
   {  if (eEdgeSelected == edge)
         return;  // already selected
      if (eEdgeSelected != null)
      {  eEdgeSelected.getModel().setSelected(false);
         eEdgeSelected.getModel().setPressed(false);
      }
      // Remember the newly selected edge;
      eEdgeSelected = edge;

      // Set the corresponding nodes in the left and right choosers:
      ASDDigraphNode fromDNode
         = (ASDDigraphNode)eEdgeSelected.getDigraphEdge().getFromNode();
      ASDEditNode fromNode = fromDNode.getEditNode();
      ASDDigraphNode toDNode
         = (ASDDigraphNode)eEdgeSelected.getDigraphEdge().getToNode();
      ASDEditNode toNode = toDNode.getEditNode();

      rightListButton.doClick();
      if (toNode != eNodeSelected2) // not already selected as right node
      {  if (!toNode.isSelected())
            nodeSelectedIndirectly(toNode);
         else // node has already been clicked; so just select it
            nodeSelected(toNode);
      }
      leftListButton.doClick();
      if (fromNode != eNodeSelected1) // not already selected as left node
      {  if (fromNode != toNode)
            nodeSelectedIndirectly(fromNode);
         else // node has already been clicked, so just select it
            nodeSelected(fromNode);
      }

      // Set the corresponding edge in the edgeList:
      ASDDigraphNode dNode = fromNode.getDigraphNode();
      ArrayList outEdges = dNode.getOutEdges();
      int j = 0;
      for (j = 0; j < dNode.outDegree(); j++)
         if ( ((ASDDigraphEdge)outEdges.get(j)).getEditEdge()
               == eEdgeSelected)
            break;
      edgeList.setSelectedValue((j+1)+"", true);
      window.edgeListChanged(); // added 2007 Aug 27 to fix bug that
         // prevented the choosers and edge list in the upper panel of
         // the editor window from being displayed properly after an
         // edge was selected going out of a previously selected
         // right node
   } // end edgeSelected

   private void edgeSelectedFromList(ASDEditEdge edge)
   {  if (!edge.isSelected()
          // Alternative:  !edge.getModel().isPressed()
         ) // If the edge has not been selected by a direct click,
         edge.doClick();  // click it down.
   }

   void editAction(ASDEditNode eNode)
   {  if (eNode == null) return;
      ASDGrammarNode gNode = eNode.getGrammarNode();
      grammarNodeBeingEdited = gNode;
      fieldBeingEdited = "action";
      String semanticAction = gNode.semanticAction();
      if (semanticAction == null)
         semanticAction = "";
      oldTextBeingEdited = semanticAction;
      ASDTextEditorFrame textWindow
         = new ASDTextEditorFrame(this, semanticAction);
      textWindow.setTitle("Editing semantic ACTION of instance: "
         + gNode.word() + " " + gNode.instance());
      textWindow.setVisible(true);
   }

   void editAction(String word, String instance)
   {  if (word == null || instance == null)
      {  JOptionPane.showMessageDialog(window,
            "A particular instance must be selected first");
         return;
      }
      ASDDigraphNode dNode
         = getGrammarDigraph().lookupNode(word, instance);
      ASDEditNode eNode = dNode.getEditNode();
      editAction(eNode);
   }

   void editValue(ASDEditNode eNode)
   {  if (eNode == null)
      {  JOptionPane.showMessageDialog(window,
            "A particular instance must be selected first");
         return;
      }

      ASDGrammarNode gNode = eNode.getGrammarNode();
      if (!gNode.isFinal())
      {  JOptionPane.showMessageDialog(window,
            "The instance is NOT FINAL; so it cannot have a semantic value.");
         return;
      }
      grammarNodeBeingEdited = gNode;
      fieldBeingEdited = "value";
      String semanticValue = gNode.semanticValue();
      if (semanticValue == null)
         semanticValue = "";
      oldTextBeingEdited = semanticValue;
      ASDTextEditorFrame textWindow
         = new ASDTextEditorFrame(this, semanticValue);
      textWindow.setTitle("Editing semantic VALUE of instance: "
         + gNode.word() + " " + gNode.instance());
      textWindow.setVisible(true);
   }

   void editValue(String word, String instance)
   {  if (word == null || instance == null) return;
      ASDDigraphNode dNode
         = getGrammarDigraph().lookupNode(word, instance);
      ASDEditNode eNode = dNode.getEditNode();
      editValue(eNode);
   }

   void exitEditor()
      throws // IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  if (getGrammarChanged())
         // Give user the option of saving the
         // old grammar or cancelling the loadFile.
      {  if (closingGrammar())
         {  window.dispose();
            System.exit(0);        // stop the program
         }
      }
      else
      {  window.dispose();
         System.exit(0);
      }
   }

   ASDGrammar getGrammar() { return grammarDigraph.getGrammar(); }

   ASDDigraph getGrammarDigraph() { return grammarDigraph; }

   JList getEdgeList() { return edgeList; }

   String getInputFileName() { return inputFileName; }

   String getInstanceSelected1() { return instanceSelected1; }

   String getInstanceSelected2() { return instanceSelected2; }

   JPanel getPhraseTypeField()
   {  JPanel result = new JPanel();
      JLabel fill = new JLabel("");
      fill.setPreferredSize(phraseTypeField.getPreferredSize());
      result.setLayout(new BorderLayout());
      if (instanceSelected1 == null)
      {  result.add(new JLabel("no left node selected"),
            BorderLayout.NORTH);
         result.add(fill);
      }
      else if (eNodeSelected1.getGrammarNode().isFinal())
      {  result.add(new JLabel("ends phrase of type  "),
            BorderLayout.NORTH);
         result.add(phraseTypeField);
      }
      else
      {  result.add(new JLabel("left node  is  not  final"),
            BorderLayout.NORTH);
         result.add(fill);
      }
      return result;
   }

   ASDEditNode getENodeSelected1() { return eNodeSelected1; }

   ASDEditNode getENodeSelected2() { return eNodeSelected2; }

   boolean getGrammarChanged() { return grammarChanged; }

   NetPanel getNetPanel() { return netPanel; }

   ASDEditorFrame getWindow() { return window; }

   String getWordSelected1() { return wordSelected1; }

   String getWordSelected2() { return wordSelected2; }

   private void hideComponentIncluding(ASDEditNode eNode)
   {  if (eNode == null) return; // nothing selected
      ASDDigraphNode dNode1 = eNode.getDigraphNode();
      ArrayList connectedNodes = dNode1.connectedNodes();
         // method defined in class DigraphNode
      for (Iterator it = connectedNodes.iterator(); it.hasNext(); )
      {  ASDDigraphNode dNode2 = (ASDDigraphNode)it.next();
         ASDEditNode eNode2 = dNode2.getEditNode();
         ArrayList outEdges = dNode2.getOutEdges();
         for (Iterator eIt = outEdges.iterator(); eIt.hasNext(); )
         {  ASDDigraphEdge dEdge = (ASDDigraphEdge)eIt.next();
            ASDEditEdge eEdge = dEdge.getEditEdge();
            if (eEdge == eEdgeSelected)
            {  eEdgeSelected.setSelected(false);
               eEdgeSelected = null;
            }
            netPanel.removeEdge(eEdge);
         }
         ArrayList inEdges = dNode2.getInEdges();
         for (Iterator eIt = inEdges.iterator(); eIt.hasNext(); )
         {  ASDDigraphEdge dEdge = (ASDDigraphEdge)eIt.next();
            ASDEditEdge eEdge = dEdge.getEditEdge();
            if (eEdge == eEdgeSelected)
            {  eEdgeSelected.setSelected(false);
               eEdgeSelected = null;
            }
            netPanel.removeEdge(eEdge);
         }
         netPanel.removeNode(eNode2);
      }
      netPanel.removeNode(eNode);
      netPanel.repaint();
   } // end hideComponentIncluding

   void hideNonSingletons()
   {  ArrayList dNodes = grammarDigraph.getNodes();
      for (Iterator it = dNodes.iterator(); it.hasNext(); )
      {  ASDDigraphNode dNode = (ASDDigraphNode)it.next();
         if (!dNode.isSingleton())
         {  ASDEditNode eNode = dNode.getEditNode();
            if (netPanel.isDisplaying(eNode))
               hideComponentIncluding(eNode);
         }
      }
      boolean choosersChanged = false;
      if (eNodeSelected1 != null && !eNodeSelected1.isSingleton())
      {  eNodeSelected1.setSelected(false);
         eNodeSelected1 = null;
         instanceSelected1 = null;
         choosersChanged = true;
      }
      if (eNodeSelected2 != null && !eNodeSelected2.isSingleton())
      {  eNodeSelected2.setSelected(false);
         eNodeSelected2 = null;
         instanceSelected2 = null;
         choosersChanged = true;
      }
      if (choosersChanged)
         window.choosersChanged();
   } // end hideNonSingletons

   void hideSelectedComponents()
   {  boolean choosersChanged = false;
      if (eNodeSelected1 != null)
      {  hideComponentIncluding(eNodeSelected1);
         eNodeSelected1.setSelected(false);
         eNodeSelected1 = null;
         instanceSelected1 = null;
         choosersChanged = true;
      }
      if (eNodeSelected2 != null)
      {  hideComponentIncluding(eNodeSelected2);
         eNodeSelected2.setSelected(false);
         eNodeSelected2 = null;
         instanceSelected2 = null;
         choosersChanged = true;
      }
      if (eEdgeSelected != null)
      {  eEdgeSelected.setSelected(false);
         eEdgeSelected = null;
         choosersChanged = true;
      }
      if (choosersChanged)
         window.choosersChanged();
   } // end hideSelectedComponents

   void hideSingletons()
   {  ArrayList dNodes = grammarDigraph.getNodes();
      for (Iterator it = dNodes.iterator(); it.hasNext(); )
      {  ASDDigraphNode dNode = (ASDDigraphNode)it.next();
         if (dNode.isSingleton())
            netPanel.removeNode(dNode.getEditNode());
      }
      netPanel.repaint();
      boolean choosersChanged = false;
      if (eNodeSelected1 != null && eNodeSelected1.isSingleton())
      {  eNodeSelected1.setSelected(false);
         eNodeSelected1 = null;
         instanceSelected1 = null;
         choosersChanged = true;
      }
      if (eNodeSelected2 != null && eNodeSelected2.isSingleton())
      {  eNodeSelected2.setSelected(false);
         eNodeSelected2 = null;
         instanceSelected2 = null;
         choosersChanged = true;
      }
      if (choosersChanged)
         window.choosersChanged();
   }

   private void loadFile(String fileName)
      throws // IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  // Load the grammar and create an ASDDigraph for it.
      inputFileName = fileName;
      boolean successfulLoad = true;
      try
      {  grammarDigraph = new ASDDigraph(inputFileName, netPanel);
      }
      catch (ASDInputException e)
      {  JOptionPane.showMessageDialog(window, e.getMessage(),
            "Syntax error in grammar file " + inputFileName,
            JOptionPane.ERROR_MESSAGE);
         successfulLoad = false;
      }
      catch(IOException e)
      {  JOptionPane.showMessageDialog(window,
            inputFileName + " not found",
            "Couldn't load grammar file.",
            JOptionPane.ERROR_MESSAGE);
         successfulLoad = false;
      }
      if (successfulLoad)
      {
         setPhraseTypeField(null);
         edgeList = new EdgeList(this);
         // Create a panel in which to display the grammar network:
         netPanel = new NetPanel(this);
         netPanel.setLayout(null);  // uses absolute layout
         netPanel.setPreferredSize(new Dimension(800, 500));
         // Update the editor's window for the new grammar:
         setSelectedNull();
         window.newOrUpdatedGrammar(true);
         setGrammarChanged(false);
      }
   }

   void loadNewFile()
      throws // IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  File file = null;
      String newFileName = null;
      // If the previous grammar has been changed, prompt for
      // whether to save it before loading the new one:
      boolean oldGrammarClosed = true;
      if (grammarChanged) // Give user the option of saving the
         // old grammar or cancelling the loadFile.
         oldGrammarClosed = closingGrammar();
      if (oldGrammarClosed)
      {  JFileChooser chooser = new JFileChooser(currentDirectory);
         chooser.addChoosableFileFilter(new ASDFileFilter());
         int returnValue = chooser.showOpenDialog(window);
         if (returnValue == JFileChooser.APPROVE_OPTION)
         {  file = chooser.getSelectedFile();
            currentDirectory = chooser.getCurrentDirectory();
            newFileName = file.getName();
         }
         if (file != null && newFileName != null)
            loadFile(
               currentDirectory.toString() + File.separator + newFileName);
      }
   }

   void mergeInGrammar()
      throws // IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  File file = null;
      String newFileName = null;
      JFileChooser chooser = new JFileChooser(currentDirectory);
      File directory = null;
      chooser.addChoosableFileFilter(new ASDFileFilter());
      int returnValue = chooser.showOpenDialog(window);
      if (returnValue == JFileChooser.APPROVE_OPTION)
      {  file = chooser.getSelectedFile();
         directory = chooser.getCurrentDirectory();
         newFileName = file.getName();
      }
      if (file != null && newFileName != null)
      {  if ( newFileName.equals(inputFileName) )
         {  JOptionPane.showMessageDialog(window,
               "The grammar file chosen is the same as\n"
              + "the one currently open.  They cannot be merged.");
            return;
         }
         mergeInGrammar(
            directory.toString() + File.separator + newFileName);
      }
   }

   void mergeInGrammar(String fileName)
      throws // IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  boolean successfulLoad = true;
      try
      {  grammarDigraph.mergeInGrammar(fileName, netPanel);
      }
      catch (ASDInputException e)
      {  JOptionPane.showMessageDialog(window, e.getMessage(),
            "Syntax error in grammar file " + fileName,
            JOptionPane.ERROR_MESSAGE);
         successfulLoad = false;
      }
      catch(IOException e)
      {  JOptionPane.showMessageDialog(window,
            fileName + " not found",
            "Couldn't load grammar file.",
            JOptionPane.ERROR_MESSAGE);
         successfulLoad = false;
      }
      if (successfulLoad)
      {  // Update the editor's window for the new grammar:
         setSelectedNull();
         edgeList = new EdgeList(this);
         netPanel.clear();
         window.newOrUpdatedGrammar(true);
         setGrammarChanged(true);
      }
   }

   void newEdgeSelected()
   {  int edgeIndex = Integer.parseInt(
         (String)edgeList.getSelectedValue())-1;
      ArrayList edges = eNodeSelected1.getDigraphNode().getOutEdges();
      ASDDigraphEdge dEdge = (ASDDigraphEdge)edges.get(edgeIndex);
      ASDEditEdge eEdge = dEdge.getEditEdge();
      edgeSelectedFromList(eEdge);
   }

   void newGrammar()
      throws // IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  // responds to New option in File menu
      boolean oldGrammarClosed = true;
      if (grammarChanged) // Give user the option of saving the
         // old grammar or cancelling the loadFile.
         oldGrammarClosed = closingGrammar();
      if (!oldGrammarClosed)
         return;
      grammarDigraph = new ASDDigraph(new ASDGrammar(), netPanel);
      inputFileName = DEFAULT_FILE_NAME;
      setPhraseTypeField(null);
      edgeList = new EdgeList(this);
      // Create a panel in which to display the grammar network:
      netPanel = new NetPanel(this);
      netPanel.setLayout(null);  // uses absolute layout
      netPanel.setPreferredSize(new Dimension(800, 500));
      setGrammarChanged(false);
      setSelectedNull();
      window.newOrUpdatedGrammar(true);
   } // end newGrammar

   void nodeSelected(ASDEditNode node)
   {  if (listSelected == LEFT_LIST)
      {  // If the node is the same as the previous one selected,
         // leave it selected and do nothing else:
         if (eNodeSelected1 != null && eNodeSelected1 == node)
            return;
         if (eNodeSelected1 != null )
         {  deselectNodeHelp(eNodeSelected1);
            if (eNodeSelected1 != eNodeSelected2)
            // Show the previously selected node unselected:
            {  eNodeSelected1.setSelected(false);
               eNodeSelected1.repaint();
            }
         }

         // Remember the newly selected node:
         eNodeSelected1 = node;
         eNodeSelected1.doClick();

         // Set the corresponding word and instance
         // in the upper left chooser:
         wordSelected1 = node.getGrammarNode().word();
         instanceSelected1 = node.getGrammarNode().instance();
         chooser1.setWordSelected(wordSelected1, false);
         chooser1.setInstanceSelected(instanceSelected1, false);
         // Create an edgelist for the selected instance:
         edgeList
            = new EdgeList(this, new EdgeListModel(node.getDigraphNode()));
         edgeList.getSelectionModel().addListSelectionListener(
            new EdgeListSelectionListener(this));
         // Set the phrase type that ends at the selected instance
         // (null if none):
         setPhraseTypeField(eNodeSelected1.getGrammarNode().phraseType());
         window.edgeListChanged();
      }
      else if (listSelected == RIGHT_LIST)
      {  // If the node is the same as the previous one selected,
         // leave it selected and do nothing else:
         if (eNodeSelected2 != null && eNodeSelected2 == node)
            return;
         if (eNodeSelected2 != null )
         {  deselectNodeHelp(eNodeSelected2);
            if (eNodeSelected2 != eNodeSelected1)
            // Show the previously selected node unselected:
            {  eNodeSelected2.setSelected(false);
               eNodeSelected2.repaint();
            }
         }

         // Remember the newly selected node:
         eNodeSelected2 = node;
         eNodeSelected2.doClick();

         // Set the corresponding word and instance
         // in the upper right chooser:
         wordSelected2 = node.getGrammarNode().word();
         instanceSelected2 = node.getGrammarNode().instance();
         chooser2.setWordSelected(wordSelected2, false);
         chooser2.setInstanceSelected(instanceSelected2, false);
      }
   } // end nodeSelected

   private void nodeSelectedIndirectly(ASDEditNode node)
   {  if (!node.isSelected()
          // Alternative:  !node.getModel().isPressed()
         )
         // If the node has not been selected by a direct click,
         // select it.
      {
           nodeSelected(node);
      }
   }

   void phraseTypeFieldChanged()
      // invoked by a PhraseTypeFieldListener
   {  String text = phraseTypeField.getText();
      // Change the phrase type of the currently selected
      // left node, if final, to the new phrase type:
      ASDEditNode eNode = eNodeSelected1;
      if (eNode == null) // no appropriate node selected
         return;
      ASDGrammarNode gNode = eNode.getGrammarNode();
      if (!gNode.isFinal()) // node can't have a phrase type
         return;
      gNode.setPhraseType(text);
      eNode.setRightLabel(text);
      setGrammarChanged(true);
   }

   void revert()
      throws // IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  if (grammarDigraph.numberOfNodes() > 0 && !getGrammarChanged())
      {  JOptionPane.showMessageDialog(window,
            "The grammar has not changed since it was last loaded or saved.");
         return;
      }
      // revert to the most recently saved copy of the grammar file
      if (getGrammarChanged())
      {  int existsOption = JOptionPane.showConfirmDialog(window,
            "The grammar has changed.  Are you sure you want to\n"
          + "replace it with the last saved version?");
         if (existsOption == JOptionPane.CANCEL_OPTION
             || existsOption == JOptionPane.NO_OPTION)
            return;
      }
      if (inputFileName == null)
      {  JOptionPane.showMessageDialog(window,
            "There is no file currently being edited.");
         return;
      }
      FileReader reader;
      try
      {  reader = new FileReader(inputFileName);
      }
      catch(IOException e)
      {  JOptionPane.showMessageDialog(window,
            "There is no file with the current file name.");
         return;
      }
      try
      {  reader.close();
      }
      catch(IOException e) {}
      setGrammarChanged(false);
      loadFile(inputFileName);
   } // end revert

   void saveAsFile()
      throws // IOException,
         ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  if (grammarDigraph.getGrammar().lexicon().size() == 0
         )
      {  JOptionPane.showMessageDialog(window,
            "There's nothing to save; the grammar is empty.");
         return;
      }
      String fileName = null;
      File file = null;
      File directory = null;
      JFileChooser chooser = new JFileChooser(currentDirectory);
      chooser.addChoosableFileFilter(new ASDFileFilter());
      int returnValue = chooser.showSaveDialog(window);
      if (returnValue == JFileChooser.APPROVE_OPTION)
      {  file = chooser.getSelectedFile();
         directory = chooser.getCurrentDirectory();
         fileName = file.getName();
      }
      if (file != null && fileName != null)
      {  if (fileName.equals(inputFileName) && directory == currentDirectory)
            saveFile(false);
         else  // different file name
         {  FileReader reader = null;
            try
            {  reader = new FileReader(
                  directory.toString() + File.separator + fileName);
            }
            catch (IOException e)
            {  reader = null;
            }
            if (reader != null)
            {  try
               {  reader.close();
               }
               catch(IOException e) {}
               int existsOption = JOptionPane.showConfirmDialog(window,
                  "Replace existing file with that name?");
               if (existsOption == JOptionPane.CANCEL_OPTION
                   || existsOption == JOptionPane.NO_OPTION)
                  return;
            }
         }
         File oldDirectory = currentDirectory;
         currentDirectory = directory;
         String oldFileName = inputFileName;
         inputFileName = fileName;
         if (saveFile(true))
         {  // Re-open the file so the default directory is changed:
            loadFile(currentDirectory.toString()
               + File.separator + inputFileName);
         }
         else // unsuccessful save attempt
         {  currentDirectory = oldDirectory;
            inputFileName = oldFileName;
         }
      }
   } // end saveAsFile

  boolean saveFile()
      throws ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  return saveFile(false);
   }

   boolean saveFile(boolean fileNameChanged)
      throws ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  if (grammarDigraph.getGrammar().lexicon().size() == 0
         )
      {  JOptionPane.showMessageDialog(window,
            "There's nothing to save; the grammar is empty.");
         return false;
      }
      if (!fileNameChanged && !getGrammarChanged())
      {  JOptionPane.showMessageDialog(window,
            "The grammar has not changed since it was last loaded or saved.");
         return false;
      }
// Option to save or not save the file in a form optimized for parsing
// was added on 2005 Oct 8:
      int optimizeOption = JOptionPane.showConfirmDialog(window,
         "Optimize the grammar file for parsing?");
      boolean optimize = true; // optimization is the default
      if (optimizeOption == JOptionPane.CANCEL_OPTION)
      {  JOptionPane.showMessageDialog(window,
            "The file save has been cancelled.");
         return false;
      }
      else if (optimizeOption == JOptionPane.NO_OPTION)
         optimize = false;

      boolean successfulSave = true;
      try
      {  String directory = currentDirectory.toString();
         String fullFileName = directory + File.separator + inputFileName;
         if (!fileNameChanged)
            grammarDigraph.saveToFile(inputFileName, optimize);
         else
            grammarDigraph.saveToFile(
               directory + File.separator + inputFileName, optimize);
      }
      catch(IOException e)
      {  JOptionPane.showMessageDialog(window,
            "File name: " + inputFileName,
            "Error trying to save grammar file.",
            JOptionPane.ERROR_MESSAGE);
         successfulSave = false;
      }
      if (successfulSave)
         setGrammarChanged(false);
      return successfulSave;
   } // end saveFile

   void setGrammarChanged(boolean b) { grammarChanged = b; }

   void setListSelected(int listNumber)
   {  listSelected = listNumber;
   }

   void setPhraseTypeField(String t)
   {  if (t != null)
         phraseTypeField.setText(t);
      else
         phraseTypeField.setText("");
   }

   private void setSelectedNull()
   {  deselectEdge(false);
      if (eNodeSelected1 != null)
      {  eNodeSelected1.setSelected(false);
         eNodeSelected1.repaint();
      }
      if (eNodeSelected2 != null && eNodeSelected2 != eNodeSelected1)
      {  eNodeSelected2.setSelected(false);
         eNodeSelected2.repaint();
      }
      eNodeSelected1 = null;
      eNodeSelected2 = null;
      wordSelected1 = null;
      instanceSelected1 = null;
      wordSelected2 = null;
      instanceSelected1 = null;
      setPhraseTypeField(null);
   }

   void showAboutInfo()
   {  // responds to About ASDEditor choice in Help menu
      JOptionPane.showMessageDialog(window,
         "ASDEditor version " + VERSION +
         "\nAuthor: James A. Mason" +
         "\nEmail: jmason@yorku.ca" +
         "\nhttp://www.yorku.ca/jmason/");
   }

   void showAll()
   {  netPanel.clear();
         // clearNetPanel(); does too much, resetting
         // currently selected nodes and edge, if any
      // Put the ASDEditNodes in the panel for the network:
      ArrayList dNodes = grammarDigraph.getNodes();
      for (Iterator it = dNodes.iterator(); it.hasNext(); )
      {  ASDDigraphNode dNode = (ASDDigraphNode) it.next();
         ASDEditNode eNode = dNode.getEditNode();
         netPanel.addNode(eNode);
      }
      // Put the ASDEditEdges in the panel also:
      ArrayList dEdges = grammarDigraph.getEdges();
      for (Iterator it = dEdges.iterator(); it.hasNext(); )
      {  ASDDigraphEdge dEdge = (ASDDigraphEdge) it.next();
         ASDEditEdge eEdge = dEdge.getEditEdge();
         netPanel.addEdge(eEdge);
      }
   }

   void showAllNonSingletons()
   {  // Put the non-singleton ASDEditNodes in the panel for the network:
      ArrayList dNodes = grammarDigraph.getNodes();
      for (Iterator it = dNodes.iterator(); it.hasNext(); )
      {  ASDDigraphNode dNode = (ASDDigraphNode) it.next();
         if (!dNode.isSingleton())
         {  ASDEditNode eNode = dNode.getEditNode();
            if (!netPanel.isDisplaying(eNode))
            {
               showComponentConnectedTo(eNode);
            }
         }
      }
   }

   void showAllSingletons()
   {  // Put the singleton ASDEditNodes in the panel for the network:
      ArrayList dNodes = grammarDigraph.getNodes();
      for (Iterator it = dNodes.iterator(); it.hasNext(); )
      {  ASDDigraphNode dNode = (ASDDigraphNode) it.next();
         if (dNode.isSingleton())
         {  ASDEditNode eNode = dNode.getEditNode();
            netPanel.addNode(eNode);
         }
      }
      netPanel.repaint();
   }

   void showComponentConnectedTo(ASDEditNode eNode)
   {  if (netPanel.isDisplaying(eNode)) // already displayed
         return;
      ASDDigraphNode dNode = eNode.getDigraphNode();
      ArrayList connectedNodes = dNode.connectedNodes();
         // method defined in class DigraphNode
      for (Iterator it = connectedNodes.iterator(); it.hasNext(); )
      {  ASDDigraphNode dNode2 = (ASDDigraphNode)it.next();
         ASDEditNode eNode2 = dNode2.getEditNode();
         if (!netPanel.isDisplaying(eNode2))
         {  netPanel.addNode(eNode2);
            ArrayList outEdges = dNode2.getOutEdges();
            for (Iterator eIt = outEdges.iterator(); eIt.hasNext(); )
            {  ASDDigraphEdge dEdge = (ASDDigraphEdge)eIt.next();
               ASDEditEdge eEdge = dEdge.getEditEdge();
               netPanel.addEdge(eEdge);
            }
            ArrayList inEdges = dNode2.getInEdges();
            for (Iterator eIt = inEdges.iterator(); eIt.hasNext(); )
            {  ASDDigraphEdge dEdge = (ASDDigraphEdge)eIt.next();
               ASDEditEdge eEdge = dEdge.getEditEdge();
               netPanel.addEdge(eEdge);
            }
         }
      }
      if (!netPanel.isDisplaying(eNode))
         netPanel.addNode(eNode);
      netPanel.repaint();
   } // end showComponentConnectedTo

   boolean toggleFinal(ASDEditNode eNode)
   {  ASDGrammarNode gNode = eNode.getGrammarNode();
      nodeSelectedIndirectly(eNode);
      if (gNode.isFinal())
      {  gNode.setSuccessors(new ArrayList());
         gNode.setSuccessorTypes(null);
         gNode.setSemanticValue(null);
         gNode.setPhraseType(null);
         eNode.setRightLabel(null);
      }
      else
      {  if (gNode.successors().size() > 0)
         {  JOptionPane.showMessageDialog(window,
               "All outgoing edges must be deleted first");
            return false;
         }
         String phraseTypeEnded = JOptionPane.showInputDialog(window,
            "What TYPE of phrase ends at the node?");
         if (phraseTypeEnded != null)
         {  phraseTypeEnded = phraseTypeEnded.trim();
            if (phraseTypeEnded.length() == 0)
               phraseTypeEnded = null;
         }
         if (phraseTypeEnded == null)  // change cancelled
            return false;
         gNode.setSuccessors(null);
         gNode.setSuccessorTypes(null);
         gNode.setSemanticValue("");
         gNode.setPhraseType(phraseTypeEnded);
         if (eNode == eNodeSelected1)
            setPhraseTypeField(phraseTypeEnded);
         eNode.setRightLabel(phraseTypeEnded);
      }

      if (eNodeSelected1 == eNode)
         window.edgeListChanged();
      setGrammarChanged(true);
      return true;
   } // end toggleFinal

   void toggleFinal(String word, String instance,
         WordInstanceChooser chooser)
   {  if (word == null || instance == null)
      {  JOptionPane.showMessageDialog(window,
            "A particular instance must be selected first");
         return;
      }
      ASDDigraphNode dNode
         = getGrammarDigraph().lookupNode(word, instance);
      ASDEditNode eNode = dNode.getEditNode();
      if (toggleFinal(eNode))
      {  chooser.setWordSelected(word, true);
         chooser.setInstanceSelected(instance, true);
      }
   }

   void toggleInitial(ASDEditNode eNode)
   {  nodeSelectedIndirectly(eNode);
      ASDGrammarNode gNode = eNode.getGrammarNode();
      gNode.setBegins(!gNode.isInitial());
      eNode.updateColor();
      eNode.repaint();
      setGrammarChanged(true);
   }

   void toggleInitial(String word, String instance,
         WordInstanceChooser chooser)
   {  if (word == null || instance == null)
      {  JOptionPane.showMessageDialog(window,
            "A particular instance must be selected first");
         return;
      }
      ASDDigraphNode dNode
         = getGrammarDigraph().lookupNode(word, instance);
      ASDEditNode eNode = dNode.getEditNode();
      toggleInitial(eNode);
      chooser.setWordSelected(word, true);
      chooser.setInstanceSelected(instance, true);
   }

   void toggleListSelected()
   {  if (listSelected == LEFT_LIST)
         rightListButton.doClick();
      else
         leftListButton.doClick();
   }

   void wordInstanceChooserChanged(WordInstanceChooser chooser)
   {  ASDEditNode eNodeSelected = null;
      String wordSelected = chooser.getWordSelected();
      String instanceSelected = chooser.getInstanceSelected();
         // may be null
      if (instanceSelected == null) // word, but not new instance selected
      {  if (chooser == chooser1)
         {  wordSelected1 = wordSelected;
            instanceSelected1 = instanceSelected;
            if (!leftListButton.isSelected())
               leftListButton.doClick();
            if (eNodeSelected1 != null)
            {  deselectNodeHelp(eNodeSelected1);
               if (eNodeSelected1 != eNodeSelected2)
               {  eNodeSelected1.setSelected(false);
                  eNodeSelected1.repaint();
               }
               eNodeSelected1 = null;
            }
            edgeList = new EdgeList(this);
            window.edgeListChanged();
         }
         else if (chooser == chooser2)
         {  wordSelected2 = wordSelected;
            instanceSelected2 = instanceSelected;
            if (!rightListButton.isSelected())
               rightListButton.doClick();
            if (eNodeSelected2 != null)
            {  deselectNodeHelp(eNodeSelected2);
               if (eNodeSelected2 != eNodeSelected1)
               {  eNodeSelected2.setSelected(false);
                  eNodeSelected2.repaint();
               }
               eNodeSelected2 = null;
            }
         }
      }
      else // instanceSelected != null
      {  ASDDigraphNode dNode
            = getGrammarDigraph().lookupNode(wordSelected, instanceSelected);
         ASDEditNode eNode = dNode.getEditNode();
         if (!netPanel.isDisplaying(eNode))
            showComponentConnectedTo(eNode);
         if (chooser == chooser1)
         {  eNodeSelected = eNodeSelected1; // old eNode
            wordSelected1 = wordSelected;
            instanceSelected1 = instanceSelected;
            eNodeSelected1 = eNode;         // new eNode
            if (!leftListButton.isSelected())
               leftListButton.doClick();
         }
         else if (chooser == chooser2)
         {  eNodeSelected = eNodeSelected2; // old eNode
            wordSelected2 = wordSelected;
            instanceSelected2 = instanceSelected;
            eNodeSelected2 = eNode;         // new eNode
            if (!rightListButton.isSelected())
               rightListButton.doClick();
         }

         if (eNodeSelected != null)
         {  deselectNodeHelp(eNodeSelected);
            if (eNodeSelected != eNodeSelected1
                && eNodeSelected != eNodeSelected2)
            {  // Unselect the previously selected node, if it's not one
               // of the currently selected nodes:
               eNodeSelected.setSelected(false);
               eNodeSelected.repaint();
            }
         }
         if (!eNode.isSelected()) // If not shown selected, show it so.
         {  eNode.setSelected(true);
            eNode.repaint();
         }

         if (chooser == chooser1)
         {  // Create an edge list for the selected instance:
            edgeList = new EdgeList(this, new EdgeListModel(dNode));
            edgeList.getSelectionModel().addListSelectionListener(
               new EdgeListSelectionListener(this));
            // Set the phrase type that ends at the selected instance
            // (null if none):
            setPhraseTypeField(eNode.getGrammarNode().phraseType());
            window.edgeListChanged();
//            window.newOrUpdatedGrammar(false);
         }
      }
   } // end wordInstanceChooserChanged

   private File currentDirectory;
   private String inputFileName;
   static final String VERSION = "1.09a";
   static final String WINDOW_TITLE = "ASD Grammar Editor (v.1.09a): ";
   private static final String DEFAULT_FILE_NAME = "newGrammar.grm";
   private ASDDigraph grammarDigraph;
   private boolean grammarChanged; // indicates whether the
      // user has changed the current grammar in the current session
   private ASDEditorFrame window;
   private NetPanel netPanel;
   private WordInstanceChooser chooser1;
   private WordInstanceChooser chooser2;
   private int listSelected = LEFT_LIST;
      // indicates 1 (first) or 2 (second) list
   private JRadioButton leftListButton;
   private JRadioButton rightListButton;
   static final int LEFT_LIST = 1;
   static final int RIGHT_LIST = 2;
   private JList edgeList;  // list of edges between two selected nodes
   private JTextField phraseTypeField;
   private ASDEditEdge eEdgeSelected; // the current ASDEditEdge selected
                                      // if any
   private ASDEditNode eNodeSelected1; // first ASDEditNode selected
   private ASDEditNode eNodeSelected2; // second ASDEditNode selected
   private String wordSelected1; // word selected in wordList1
   private String instanceSelected1; // instance of wordSelected1 selected
   private String wordSelected2; // word selected in wordList2
   private String instanceSelected2; // instance of wordSelected2 selected
   // The following three variables are used by the editAction, editValue,
   // and changedText methods:
   private ASDGrammarNode grammarNodeBeingEdited;
   private String fieldBeingEdited;
   private String oldTextBeingEdited;
} // end class ASDEditor

class ASDFileFilter extends javax.swing.filechooser.FileFilter
{  public boolean accept(File f)
   {  if (f.isDirectory())
         return true;
      String fileName = f.getName();
      String extension = null;
      int i = fileName.lastIndexOf('.');
      if (i > 0 && i < fileName.length() - 1)
         extension = fileName.substring(i+1).toLowerCase();
      if (extension == null)
         return false;
      if (extension.equals("asd") || extension.equals("grm"))
         return true;
      return false;
   }

   public String getDescription()
   {  return "ASD grammar files (*.asd or *.grm)";
   }
}

class PhraseTypeFieldListener implements ActionListener
{
   PhraseTypeFieldListener(ASDEditor e)
   {  editor = e;
   }

   public void actionPerformed(ActionEvent e)
   {  editor.phraseTypeFieldChanged();
   }

   private ASDEditor editor;
}
