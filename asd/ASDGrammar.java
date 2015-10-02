/*

Copyright 2000-2004 James A. Mason

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
import java.net.*;

/**
   Instances are ASD grammars in the internal representation
   used by the ASD Parser.
   @author James A. Mason
   @version 1.04 2000 Mar; May; 2001 Feb; Sep-Dec; 2003 Jul;
      2004 Jan
 */
public class ASDGrammar
{
   /**
      Creates a new empty ASDGrammar.
    */
   public ASDGrammar()
   {  table = new HashMap();
   }

   /**
      Creates a new ASDGrammar from the character representation
      stored in a given file.  Throws an IOException if there is
      no such file, or an ASDInputException if the representation
      of the grammar in the file is ill-formed.
      @param fileName the name of the file to be used
      @param includeCoords indicates whether or not to include
      pixel coordinates in the grammar loaded, if they are
      present.  They are needed by ASDEditor but not by ASDParser.
    */
   public ASDGrammar(String fileName, boolean includeCoords)
      throws IOException, ASDInputException, MalformedURLException
   {  ASDGrammarReader reader = null;
//      try
//      {
      reader = new ASDGrammarReader(fileName, includeCoords);
//      }
//      catch(IOException e)
//      {  System.out.println("File " + fileName +
//         " could not be opened.");
//         System.exit(0);
         // throw e;
//      }

//      try
//      {
      table = reader.getGrammar();
//      System.out.println("table size = " + table.size());
//      }
//      catch (ASDInputException e)
//      {  System.out.println(e.getMessage());
//         System.exit(0);
//      }
      reader.close(); // closes the InputStream used by the reader,
         // if it was opened using a URL.
      incomingMarked = false;
   } // end ASDGrammar(String fileName, includeCoords)

   /**
      Creates a new ASDGrammar from the character representation
      stored in a given file.  Throws an IOException if there is
      no such file, or an ASDInputException if the representation
      of the grammar in the file is ill-formed.
      @param fileName the name of the file to be used
      @param includeCoords indicates whether or not to include
      pixel coordinates in the grammar loaded, if they are
      present.  They are needed by ASDEditor but not by ASDParser.
      @param markIncoming indicates whether or not to mark grammar
      nodes that have incoming edges, for use by the ASDparser
    */
   public ASDGrammar(String fileName, boolean includeCoords,
         boolean markIncoming)
      throws IOException, ASDInputException, MalformedURLException
   {  this(fileName, includeCoords);  // invokes other constructor
      if (markIncoming)
      {  markNodesWithIncomingEdges();
      }
   } // end ASDGrammar(String fileName, includeCoords, markIncoming)

   /**
      Computes new successorTypes field values for all ASDGrammarNode
      instances in the grammar.
    */
   void computeSuccessorTypes()
   {  Set typesRecognized = phraseTypes();
      Set entrySet = table.entrySet();
      for (Iterator it = entrySet.iterator(); it.hasNext(); )
      {  Map.Entry e = (Map.Entry) it.next();
         ArrayList instances = (ArrayList) e.getValue();
         for (Iterator j = instances.iterator(); j.hasNext(); )
         {  ASDGrammarNode gNode = (ASDGrammarNode) j.next();
            if ( gNode.isFinal() )
               gNode.setSuccessorTypes(null);
            else
            {  ArrayList successors = gNode.successors();
               Set successorTypes = new HashSet(successors.size());
               for (Iterator k = successors.iterator(); k.hasNext(); )
               {  ASDGrammarSuccessor s = (ASDGrammarSuccessor) k.next();
                  String word = s.getWord();
                  if ( typesRecognized.contains(word) )
                     successorTypes.add(word);
               }
               gNode.setSuccessorTypes(new ArrayList(successorTypes));
            }
         }
      }
   }

   /**
      Returns the HashMap used to store the words and lists of
      instances in the grammar.
    */
   public HashMap lexicon()
   {  return table;
   }

   /**
      Looks up a word instance in the grammar specified by a given
      ASDGrammarSuccessor.  If the instance has already been looked
      up, the ASDGrammarSuccessor will already have a direct
      reference to it; otherwise that direct reference will be
      set when the instance is looked up the first time.
      @param successor an ASDGrammarSuccessor specifying the word instance
      @return the ASDGrammarNode representing the word instance;
       null if not found.
    */
   public ASDGrammarNode lookupInstance(ASDGrammarSuccessor successor)
   {
      ASDGrammarNode result = successor.getNode();
      if (result != null) return result;  // already looked up
      ArrayList instances = lookupWord(successor.getWord());
      if (instances == null) return null;  // word not found
      String instanceSought = successor.getInstance();
      for (int j = 0; j < instances.size(); ++j)
      {  result = (ASDGrammarNode)instances.get(j);
         if (result.instance().equals(instanceSought))
         {  successor.setNode(result);
               // have the successor remember the node found
            return result;
         }
      }
      return null;  // instance not found
   } // end lookupInstance

   /**
      Looks up a given string in the grammar and returns a list
      of instances for the word.  Each instance is an ASDGrammarNode.
      @param word the "word" string to be looked up
      @return an ArrayList of instances for the word; null if the word
      is not found in the grammar.
    */
   public ArrayList lookupWord(String word)
   {  return (ArrayList) table.get(word);
   }

   /**
      Marks all ASDGrammarNodes in the grammar that have incoming edges.
      This is needed by ASDParser to detect uniquely-parsed subphrases.
    */
   public void markNodesWithIncomingEdges()
   {  Set entrySet = table.entrySet();
      for (Iterator it = entrySet.iterator(); it.hasNext(); )
      {  Map.Entry e = (Map.Entry) it.next();
         ArrayList instances = (ArrayList) e.getValue();
         if (instances != null)
            for (Iterator j = instances.iterator(); j.hasNext(); )
            {  ASDGrammarNode gNode = (ASDGrammarNode) j.next();
               if (!gNode.isFinal() )
               {  ArrayList successors = gNode.successors();
                  if (successors != null)
                     for (Iterator k = successors.iterator(); k.hasNext(); )
                     {  ASDGrammarSuccessor s =
                           (ASDGrammarSuccessor) k.next();
                        ASDGrammarNode successorNode = lookupInstance(s);
                        if (successorNode != null)
                           successorNode.setHasIncoming(true);
                        else
                           System.out.println("(" + e.getKey() + " "
                              + gNode.instance()
                              + ") has edge to non-existent node ("
                              + s.getWord() + " " + s.getInstance() + ").");
                     }
               }
            }
      }
      incomingMarked = true;
   }

   /**
      Indicates whether all ASDGrammarNodes in the grammar that have
      incoming edges have already been marked.
    */
   public boolean nodesWithIncomingEdgesMarked()
   {  return incomingMarked;
   }

   /**
      Returns a Set containing the phrase types recognized by the grammar.
    */
   public Set phraseTypes()
   {  Set result = new HashSet();
      Set entrySet = table.entrySet();
      for (Iterator it = entrySet.iterator(); it.hasNext(); )
      {  Map.Entry e = (Map.Entry) it.next();
         ArrayList instances = (ArrayList) e.getValue();
         for (Iterator j = instances.iterator(); j.hasNext(); )
         {  ASDGrammarNode gNode = (ASDGrammarNode) j.next();
            String phraseType = gNode.phraseType();
            if (phraseType != null)  // node is a final node
               result.add(phraseType);
         }
      }
      return result;
   }

   /**
       Allows a client to reset the string which is used to represent
       a "dummy" word in the grammar -- used in the labels on dummy nodes.
       The default is "$$".
     */
   public static void setDUMMYWORD(String newValue)
   {  DUMMYWORD = newValue;
   }

   /**
      Allows a client to reset to false the flag which indicates whether
      nodes in the grammar with incoming edges have been marked.  This is
      needed if the client modifies the grammar but does not invoke the
      method markNodesWithIncomingEdges before passing the modified grammar
      to ASDParser.
    */
   public void setNodesWithIncomingEdgesNotMarked()
   {  incomingMarked = false;
   }

   /**
      Indicates whether or not a specified word has exactly
      one instance in the lexicon/grammar.
      @param word the "word" string to be looked up
      @return true if the word has one instance in the grammar,
      false if not.
    */
   public boolean uniqueInstance(String word)
   {  ArrayList wordEntry = lookupWord(word);
      return wordEntry != null && wordEntry.size() == 1;
   }

   /**
      The string used in dummy nodes in an ASD grammar.
    */
   public static String DUMMYWORD = "$$";
   private HashMap table; // to hold the words and their lists of instances
   private boolean incomingMarked = false; // indicates whether
      // ASDGrammarNodes with incoming edges have been marked
} // end class ASDGrammar
