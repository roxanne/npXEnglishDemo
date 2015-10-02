package asd;
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

import java.io.*;
import java.util.*;

/**
   Checks an ASDGrammar read from a file for well-formedness.  It also
   counts words and phrase types, nodes (instances), and edges in the grammar.
   <BR><BR>
   Usage:
   <BR><tt><b> java asd/ASDCheck grammarFileName</b></tt>

   @author James A. Mason
   @version 1.01 2003 July
 */

public class ASDCheck
{  public static void main(String[] args)
      throws IOException, ASDInputException
   {
      int wordCount = 0;  // counts word entries
      int nodeCount = 0;  // counts instances
      int edgeCount = 0;  // counts edges

      if (args.length == 0)
      {  System.out.println(
            "Usage should be: java asd/ASDCheck grammarFileName");
         System.exit(0);
      }

      String fileName = args[0];
      // Load the grammar without pixel coordinates, but mark
      // all nodes with incoming edges.  Note: Any edges to
      // non-existent nodes are detected during the marking
      // process, with messages displayed to System.out.
      ASDGrammar grammar = new ASDGrammar(fileName, false, true);

      Set entrySet = grammar.lexicon().entrySet();
      for (Iterator it = entrySet.iterator(); it.hasNext(); )
      {  Map.Entry e = (Map.Entry) it.next();
         ++wordCount;
         ArrayList instances = (ArrayList) e.getValue();
         if (instances == null || instances.size() == 0)
            System.out.println(
               "Word \"" + e.getKey() + "\" has no instances.");
         else
            for (Iterator j = instances.iterator(); j.hasNext(); )
            {  ASDGrammarNode gNode = (ASDGrammarNode) j.next();
               ++nodeCount;
               if (!gNode.isInitial() && !gNode.hasIncomingEdges())
                  System.out.println(
                     "Non-initial node (" + e.getKey() + " "
                     + gNode.instance() + ") has no incoming edges.");
               if (!gNode.isFinal() )
               {  ArrayList successors = gNode.successors();
                  if (successors == null || successors.size() == 0)
                     System.out.println(
                        "Non-final node (" + e.getKey() + " "
                        + gNode.instance() + ") has no outgoing edges.");
                  else
                     for (Iterator k = successors.iterator(); k.hasNext(); )
                     {  ASDGrammarSuccessor s =
                           (ASDGrammarSuccessor) k.next();
                        ASDGrammarNode successorNode
                           = grammar.lookupInstance(s);
                        if (successorNode != null)
                           ++edgeCount;
                        // else invalid successor was already detected when
                        // the grammar was loaded and nodes with incoming
                        // edges were marked (see above).
                     }
               }
            }
      }

      System.out.println("\nWords & phrase types:   " + wordCount);
      System.out.println("Nodes (instances):      " + nodeCount);
      System.out.println("Valid edges:            " + edgeCount);
   }
} // end class ASDCheck
