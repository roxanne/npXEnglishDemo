/*

Copyright 2001 James A. Mason

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
import digraphs.*;
import java.util.*;

/**
   Instances represent nodes in an ASD grammar for the grammar
   optimizer and graphical grammar editor.  Access is restricted
   to package asd.
   @author James A. Mason
   @version 1.01 2001 Sep 29; Oct 1-2, 9-10; Nov 26
 */
class ASDDigraphNode extends digraphs.DigraphNode
{  ASDDigraphNode(ASDGrammarNode grammarNode)
   {  gNode = grammarNode;
   }

   ArrayList beginsDirectly()
   {  Set result = new HashSet();
      if ( !gNode.isInitial() )
         return new ArrayList(0);
      if ( gNode.isFinal() )
         result.add(gNode.phraseType());

      // Find all final nodes reachable from the current node,
      // and add to the result the phrase types which they end.
      // (This is unconditional because during optimization
      // of the begins fields in a grammar, a node can temporarily
      // both be a final node and have descentdants.)
      ArrayList reachable = this.descendants(); // inherited from DigraphNode
      for (Iterator it = reachable.iterator(); it.hasNext(); )
      {  ASDDigraphNode dNode = (ASDDigraphNode) it.next();
         ASDGrammarNode g = dNode.getGrammarNode();
         String phraseType = g.phraseType();
         if ( phraseType != null )
            result.add(phraseType);
      }
      return new ArrayList(result);
   }

   ASDGrammarNode getGrammarNode()
   {  return gNode;
   }

   boolean isSingleton()
   {  // indicates whether the node has no outgoing or incoming
      // edges.  Note: a node with an edge to itself is not considered
      // singleton by this criterion.
      return inDegree() == 0 && outDegree() == 0;
   }

   void setGrammarNode(ASDGrammarNode g)
   {  gNode = g;
   }

   ASDEditNode getEditNode()
   {  return eNode;
   }

   void setEditNode(ASDEditNode e)
   {  eNode = e;
   }

   public String toString()
   {
      return gNode.toString();
   }

   private ASDEditNode eNode;     // the corresponding edit node
   private ASDGrammarNode gNode;  // the corresponding grammar node
} // end class ASDDigraphNode
