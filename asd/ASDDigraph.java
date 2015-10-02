/*

Copyright 2001-2005 James A. Mason

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
import java.awt.*;
import java.lang.reflect.*;
import digraphs.*;

/**
   An instance represents an ASD grammar as a Digraph, for use by
   the graphical grammar editor and grammar optimizer.
   The ASDDigraph instance has instance variables for the
   corresponding ASDGrammar instance and ASDEditor instance.
   Access is restriced to the package asd.
   @author James A. Mason
   @version 1.03 2001 Oct-Nov; 2003 Jul; 2005 Oct
 */
class ASDDigraph extends Digraph
{
   ASDDigraph()
   {  super();
      EdgeClassName = "asd.ASDDigraphEdge";
   }

   ASDDigraph(String fileName, Container panel)
      throws IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  // Include coordinates when reading the grammar from the file.
      this(new ASDGrammar(fileName, true), panel);
   }

   ASDDigraph(ASDGrammar givenGrammar, Container panel)
      throws ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
      // When it creates new ASDDigraphNodes, if panel is not null
      // it must also create corresponding ASDEditNodes, so those
      // can be displayed as needed by the ASDEditor.
   {  this();
      setGrammar(givenGrammar);
      setPanel(panel);
      // Create an ASDDigraphNode and an ASDEditNode for each
      // ASDGrammarNode in the given grammar:
      HashMap lexicon = grammar.lexicon();
      Set entrySet = lexicon.entrySet();
      for (Iterator it = entrySet.iterator(); it.hasNext(); )
      {  Map.Entry e = (Map.Entry) it.next();
         ArrayList instances = (ArrayList)e.getValue();
         for (Iterator j = instances.iterator(); j.hasNext(); )
         {  ASDGrammarNode gNode = (ASDGrammarNode) j.next();
            ASDDigraphNode dNode = addDigraphNode(gNode);
            if (panel != null)
            {  ASDEditNode eNode = new ASDEditNode(gNode, getPanel(),
                  gNode.getXCoordinate(), gNode.getYCoordinate());
               dNode.setEditNode(eNode);
               eNode.setDigraphNode(dNode);
            }
         }
      }

      // For each of the new digraph nodes, add edges to all the
      // other digraph nodes that the corresponding grammar node
      // connects to:
      ArrayList nodes = super.getNodes();
      for (Iterator it = nodes.iterator(); it.hasNext(); )
      {  ASDDigraphNode dNode = (ASDDigraphNode) it.next();
         ASDGrammarNode gNode = dNode.getGrammarNode();
         if ( !gNode.isFinal() )
         {  ArrayList successors = gNode.successors();
            for (Iterator j = successors.iterator(); j.hasNext(); )
            {  ASDGrammarSuccessor s = (ASDGrammarSuccessor) j.next();
               ASDGrammarNode nextgNode = grammar.lookupInstance(s);
               ASDDigraphNode nextdNode = null;
               for (Iterator k = nodes.iterator(); k.hasNext(); )
               {  ASDDigraphNode d = (ASDDigraphNode) k.next();
                  if (d.getGrammarNode() == nextgNode)
                  {  nextdNode = d;
                     break;
                  }
               }

               ASDDigraphEdge newEdge = (ASDDigraphEdge)
                  super.addEdgeFromNodeToNode(dNode, nextdNode);

               if (newEdge != null)
               {  newEdge.setGrammarSuccessor(s);
                  if (panel != null)
                  {  ASDEditEdge newEditEdge = new ASDEditEdge(panel,
                        s.getXCoordinate(), s.getYCoordinate());
                     newEdge.setEditEdge(newEditEdge);
                     newEditEdge.setDigraphEdge(newEdge);
                     newEditEdge.setGrammarSuccessor(s);
                  }
               }
            }
         }
      }
   }

   ASDDigraphNode addDigraphNode(ASDGrammarNode gNode)
   {  ASDDigraphNode newNode = new ASDDigraphNode(gNode);
      digraphNodes.add(newNode);
      return newNode;
   }

   /**
      Adds a new edge, corresponding to the given ASDGrammarSuccessor,
      connecting two nodes specified by their indices in the list of
      all nodes, provided the two nodes are nodes of the receiver.
    */
   ASDDigraphEdge addEdgeFromTo(ASDGrammarSuccessor successor,
         int nodeIndex1, int nodeIndex2)
      throws ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  ASDDigraphEdge newEdge
         = (ASDDigraphEdge)super.addEdgeFromTo(nodeIndex1, nodeIndex2);

      if (newEdge == null) return newEdge;
      newEdge.setGrammarSuccessor(successor);
      if (getPanel() != null)
      {
         ASDEditEdge newEditEdge = new ASDEditEdge(getPanel(),
            successor.getXCoordinate(), successor.getYCoordinate());
         newEdge.setEditEdge(newEditEdge);
         newEditEdge.setGrammarSuccessor(successor);
      }
      return newEdge;
   }

   /**
      Adds a new edge to connect two given nodes.
    */
   ASDDigraphEdge addEdgeFromNodeToNode(
      ASDDigraphNode node1, ASDDigraphNode node2)
      throws ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  if (node1 == null || node2 == null) return null;
      ASDDigraphEdge result =
         (ASDDigraphEdge) super.addEdgeFromNodeToNode(node1, node2);
      ASDGrammarNode gNode1 = node1.getGrammarNode();
      ArrayList successors = gNode1.successors();
      int newSuccessorNumber = successors.size() + 1;
      ASDGrammarNode gNode2 = node2.getGrammarNode();
      ASDEditEdge eEdge = new ASDEditEdge(null);
         // graphical context and coordinates must be set elsewhere
      eEdge.setDigraphEdge(result);
      result.setEditEdge(eEdge);
      ASDGrammarSuccessor successor = new ASDGrammarSuccessor(
         gNode2.word(), gNode2.instance());
      eEdge.setGrammarSuccessor(successor);
      result.setGrammarSuccessor(successor);
      successors.add(successor);
      return result;
   }

   /**
      Adds a temporary new edge to connect two given nodes,
      as required by expandBegins().
    */
   private ASDDigraphEdge addTemporaryEdgeFromNodeToNode(
      ASDDigraphNode node1, ASDDigraphNode node2)
      throws ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  if (node1 == null || node2 == null) return null;
      ASDDigraphEdge result =
         (ASDDigraphEdge) super.addEdgeFromNodeToNode(node1, node2);
      return result;
   }


   /**
      Expands the "begins types" fields of all initial grammar nodes
      to show what phrase types can begin, directly or indirectly,
      at that node.
    */
   void expandBegins()
      throws ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  ASDDigraph copy = shallowCopyDigraph();
      ArrayList initials = new ArrayList(numberOfNodes());
      ArrayList finals = new ArrayList(numberOfNodes());
      for (Iterator it = copy.getNodes().iterator(); it.hasNext(); )
      {  ASDDigraphNode dNode = (ASDDigraphNode) it.next();
         ASDGrammarNode gNode = dNode.getGrammarNode();
         if (gNode.isInitial())
            initials.add(dNode);
         if (gNode.isFinal())
            finals.add(dNode);
      }

      // Connect each final node by a new edge in the copy,
      // to all initial nodes for the phrase type which the given
      // final node ends:
      for (Iterator it = finals.iterator(); it.hasNext(); )
      {  ASDDigraphNode dNode = (ASDDigraphNode) it.next();
         ASDGrammarNode gNode = dNode.getGrammarNode();
         String type = gNode.phraseType();
         for (Iterator it2 = initials.iterator(); it2.hasNext(); )
         {  ASDDigraphNode dNode2 = (ASDDigraphNode) it2.next();
            ASDGrammarNode gNode2 = dNode2.getGrammarNode();
            if ( type.equals(gNode2.word()) )
               // gNode2 is an initial node for the phrase type which
               // gNode ends.  So add a (temporary) edge in the copy
               // from gNode to gNode2, BUT DON'T TRY TO UPDATE THE
               // SUCCESSORS LIST OF dNode IN THE GRAMMAR!
               copy.addTemporaryEdgeFromNodeToNode(dNode, dNode2);
         }
      }

      for (Iterator it = initials.iterator(); it.hasNext(); )
      {  ASDDigraphNode dNode = (ASDDigraphNode) it.next();
         ArrayList types = dNode.beginsDirectly();
         ASDGrammarNode gNode = dNode.getGrammarNode();
         gNode.setBeginsTypes(types);
         // Because of the edges that have been added to the copy,
         // the ASDDigraphNode beginsDirectly() method now also
         // includes phrase types which the node begins indirectly
         // in the original ASDDigraph.
      }
   }

   /**
      Looks up a node in the ASDDigraph by word and instance.
      @param word the word to be looked up.
      @param instance the instance number of word to be looked up
      @returns the ASDDigraphNode found; null if none
    */
   ASDDigraphNode lookupNode(String word, String instance)
   {  ASDGrammarNode gNode = grammar.lookupInstance(
         new ASDGrammarSuccessor(word, instance));
      if (gNode != null)
         for (Iterator it = getNodes().iterator(); it.hasNext(); )
         {  ASDDigraphNode dNode = (ASDDigraphNode) it.next();
            if (dNode.getGrammarNode() == gNode)
               return dNode;
         }
      return null;
   }

   void mergeInGrammar(String fileName, Container panel)
      throws IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  // Include coordinates when reading the grammar from the file.
      mergeInGrammar(new ASDGrammar(fileName, true), panel);
   }

   void mergeInGrammar(ASDGrammar otherGrammar, Container panel)
      throws IOException,
             ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  HashMap otherLexicon = otherGrammar.lexicon();
      Set otherEntrySet = otherLexicon.entrySet();
      // First add any new words in the otherGrammar to the lexicon
      // for this grammar, and update the instance numbers in the
      // otherGrammar, so they avoid conflicts with the ones in this grammar.
      for (Iterator it = otherEntrySet.iterator(); it.hasNext(); )
      {  Map.Entry e = (Map.Entry) it.next();
         String word = (String)e.getKey();
         ArrayList instances = (ArrayList)e.getValue();
         ArrayList wordEntry = grammar.lookupWord(word);
         if (wordEntry == null) // new word for the current grammar
         {  wordEntry = new ArrayList(instances.size());
            grammar.lexicon().put(word, wordEntry);
         }
         int increment = wordEntry.size();
         for (Iterator j = instances.iterator(); j.hasNext(); )
         {  ASDGrammarNode oldGNode = (ASDGrammarNode)j.next();
            if (increment > 0)
            {  int oldInstanceNumber = Integer.parseInt(oldGNode.instance());
               oldGNode.setInstance(
                  (oldInstanceNumber + increment) + "");
            }
            if (!oldGNode.isFinal())
            {  ArrayList successors = oldGNode.successors();
               for (Iterator k = successors.iterator(); k.hasNext(); )
               {  ASDGrammarSuccessor s = (ASDGrammarSuccessor) k.next();
                  String sWord = s.getWord();
                  ArrayList sWordEntry = grammar.lookupWord(sWord);
                  if (sWordEntry != null && sWordEntry.size() > 0)
                  {  int sIncrement = sWordEntry.size();
                     String sInstance = s.getInstance();
                     int oldSInstanceNumber = Integer.parseInt(sInstance);
                     s.setInstance((oldSInstanceNumber + sIncrement) + "");
                  }
               }
            }
         }
      }

      // Now add the old grammar entries to the current grammar and
      // create an ASDDigraphNode (with an ASDEditNode) in this
      // ASDDigraph, for each ASDGrammarNode in the given grammar:
      ArrayList newNodes = new ArrayList();
      for (Iterator it = otherEntrySet.iterator(); it.hasNext(); )
      {  Map.Entry e = (Map.Entry) it.next();
         String word = (String)e.getKey();
         ArrayList newInstances = grammar.lookupWord(word);
         ArrayList oldInstances = (ArrayList)e.getValue();
         for (Iterator j = oldInstances.iterator(); j.hasNext(); )
         {  ASDGrammarNode gNode = (ASDGrammarNode) j.next();
            newInstances.add(gNode);
            ASDDigraphNode dNode = addDigraphNode(gNode);
            newNodes.add(dNode);
            if (panel != null)
            {  ASDEditNode eNode = new ASDEditNode(gNode, getPanel(),
                  gNode.getXCoordinate(), gNode.getYCoordinate());
               dNode.setEditNode(eNode);
               eNode.setDigraphNode(dNode);
            }
         }
      }

      // For each of the new digraph nodes, add edges to all the
      // other new digraph nodes that the corresponding grammar
      // node connects to:
      for (Iterator it = newNodes.iterator(); it.hasNext(); )
      {  ASDDigraphNode dNode = (ASDDigraphNode) it.next();
         ASDGrammarNode gNode = dNode.getGrammarNode();
         if ( !gNode.isFinal() )
         {  ArrayList successors = gNode.successors();
            for (Iterator j = successors.iterator(); j.hasNext(); )
            {  ASDGrammarSuccessor s = (ASDGrammarSuccessor) j.next();
               ASDGrammarNode nextgNode = grammar.lookupInstance(s);
               ASDDigraphNode nextdNode = null;
               for (Iterator k = newNodes.iterator(); k.hasNext(); )
               {  ASDDigraphNode d = (ASDDigraphNode) k.next();
                  if (d.getGrammarNode() == nextgNode)
                  {  nextdNode = d;
                     break;
                  }
               }

               ASDDigraphEdge newEdge = (ASDDigraphEdge)
                  super.addEdgeFromNodeToNode(dNode, nextdNode);
               if (newEdge != null)
               {  newEdge.setGrammarSuccessor(s);
                  if (panel != null)
                  {  ASDEditEdge newEditEdge = new ASDEditEdge(panel,
                        s.getXCoordinate(), s.getYCoordinate());
                     newEdge.setEditEdge(newEditEdge);
                     newEditEdge.setDigraphEdge(newEdge);
                     newEditEdge.setGrammarSuccessor(s);
                  }
               }
            }
         }
      }
   } // end mergeInGrammar

   void removeEdge(ASDDigraphEdge e)
   {  if (e == null) return;
      ASDGrammarSuccessor s = e.getGrammarSuccessor();
      ASDGrammarNode gNode
         = ((ASDDigraphNode)e.getFromNode()).getGrammarNode();
      ArrayList successors = gNode.successors();
      int pos = successors.indexOf(s);
      successors.remove(pos);
      super.removeEdge(e);
   }

   void removeNode(ASDDigraphNode n)
   {  if (n == null) return;
      ASDGrammarNode gNode = n.getGrammarNode();
      if (gNode == null)
      {  System.out.println("null grammar node");
         System.exit(0);
      }
      String word = gNode.word();
      ArrayList entries = grammar.lookupWord(word);
      int entryIndex = entries.indexOf(gNode);
      if (entryIndex < 0)
      {  System.out.println("grammar node not found");
         System.exit(0);
      }
      String instance = null;
      entries.remove(entryIndex);
      super.removeNode(n);

      // Re-number the remaining instances of the word,
      // and the ASDGrammarSuccessors that refer to them:
      int instanceIndex = 1;
      for (Iterator it = entries.iterator(); it.hasNext(); )
      {  gNode = (ASDGrammarNode)it.next();
         word = gNode.word();
         ASDDigraphNode dNode = lookupNode(word, gNode.instance());
         instance = instanceIndex + "";
         gNode.setInstance(instance);
         ASDEditNode eNode = dNode.getEditNode();
         eNode.setText(word + " " + instance);
         ArrayList edgesIn = dNode.getInEdges();
        if (edgesIn != null)
            for (Iterator it2 = edgesIn.iterator(); it2.hasNext(); )
            {  ASDDigraphEdge dEdge = (ASDDigraphEdge)it2.next();
               ASDGrammarSuccessor successor = dEdge.getGrammarSuccessor();
               successor.setInstance(instance);
            }
         instanceIndex++;
      }
   }

   /**
      Writes an ASCII representation of the grammar to a file with
      given name, in the format that is read by the ASDGrammar(fileName)
      constructor.  The file is saved in a form optimized for parsing.
    */
   void saveToFile(String fileName)
      throws IOException, ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  saveToFile(fileName, true);
   }  // end saveToFile

   /**
      Writes an ASCII representation of the grammar to a file with
      given name, in the format that is read by the ASDGrammar(fileName)
      constructor.
      @param optimize indicates whether or not the saved grammar should
      be saved in a form that is optimized for parsing.
    */
   void saveToFile(String fileName, boolean optimize)
      throws IOException, ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  // Open a string for the given file name:
      FileWriter writer = new FileWriter(fileName);
      PrintWriter outStream = new PrintWriter(writer);

      // Optimize the grammar before saving it.
      // Whether or not it is actually saved in optimized form
      // is left to the ASDGrammarNode toString(optimize) function.
      expandBegins();
      grammar.computeSuccessorTypes();

      // Sort the lexicon so
      // "$$" is first and
      // words are in alphabetical order:
      ArrayList entryList = new ArrayList(grammar.lexicon().size());

      Set entrySet = grammar.lexicon().entrySet();
      for (Iterator it = entrySet.iterator(); it.hasNext(); )
      {  Map.Entry e = (Map.Entry) it.next();
         String word = (String)e.getKey();
         ArrayList instances = (ArrayList)e.getValue();
         entryList.add(new ASDWordEntry(word, instances));
      }

      Object[] sortedList = entryList.toArray();
      Arrays.sort(sortedList);

      // Output the sorted lexicon to the file:

      for (int j = 0; j < sortedList.length; j++)
      {  ASDWordEntry entry = (ASDWordEntry) sortedList[j];
         String word = (String) entry.get(0);
         ArrayList instances = (ArrayList) entry.get(1);
         outStream.println("(" + word + " (");
         for (Iterator it2 = instances.iterator(); it2.hasNext(); )
         {  ASDGrammarNode node = (ASDGrammarNode) it2.next();
            outStream.print(node.toString(optimize));
         }
         outStream.println("))");
         outStream.println();
      }

      writer.close();
   }  // end saveToFile

   /**
      Returns a copy of the receiver, without sharing of digraph nodes
      or edges but with sharing of grammar nodes.
    */
   private ASDDigraph shallowCopyDigraph()
      throws ClassNotFoundException, InvocationTargetException,
             InstantiationException, IllegalAccessException
   {  ASDDigraph result = new ASDDigraph();
      result.setGrammar(getGrammar());
      ArrayList dNodes = getNodes();
      for (Iterator it = dNodes.iterator(); it.hasNext(); )
      {  ASDDigraphNode node = (ASDDigraphNode) it.next();
         ASDGrammarNode oldGNode = node.getGrammarNode();
         result.addDigraphNode(getGrammar().lookupInstance(
            new ASDGrammarSuccessor(oldGNode.word(), oldGNode.instance()) ));
      }
      ArrayList edges = getEdges();
      for (Iterator it = edges.iterator(); it.hasNext(); )
      {  ASDDigraphEdge edge = (ASDDigraphEdge) it.next();
         ASDDigraphNode fromNode = (ASDDigraphNode) edge.getFromNode();
         ASDDigraphNode toNode = (ASDDigraphNode) edge.getToNode();
         int fromNodeIndex = -1;
         for (Iterator nodeIt = dNodes.iterator(); nodeIt.hasNext(); )
         {  ASDDigraphNode dNode = (ASDDigraphNode) nodeIt.next();
            fromNodeIndex++;
            if (dNode == fromNode) break;
         }
         int toNodeIndex = -1;
         for (Iterator nodeIt = dNodes.iterator(); nodeIt.hasNext(); )
         {  ASDDigraphNode dNode = (ASDDigraphNode) nodeIt.next();
            toNodeIndex++;
            if (dNode == toNode) break;
         }

         result.addEdgeFromTo(edge.getGrammarSuccessor(), fromNodeIndex,
            toNodeIndex);
      }
      return result;
   }

   ASDEditor getEditor() { return editor; }
   void setEditor(ASDEditor newEditor) { editor = newEditor; }

   ASDGrammar getGrammar() { return grammar; }
   void setGrammar(ASDGrammar newGrammar) { grammar = newGrammar; }

   Container getPanel() { return graphicPanel; }
   void setPanel(Container newPanel) { graphicPanel = newPanel; }

   private ASDGrammar grammar;
   private ASDEditor editor;
   private Container graphicPanel;
} // end class ASDDigraph