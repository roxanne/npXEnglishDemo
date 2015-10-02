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
//import digraphs.*;
import digraphs.*;
/**
   Instances represent edges in an ASD grammar for the grammar
   optimizer and graphical grammar editor.  Access of the class and
   constructor must be public in order for it to be used by reflection
   in Digraph.java method addEdgeFromNodeToNode.
   @author James A. Mason
   @version 1.01 2001 Sep 29; Oct 1-2, 10; Nov 23
 */
public class ASDDigraphEdge extends DigraphEdge
{  public ASDDigraphEdge(ASDDigraphNode origin, ASDDigraphNode destination)
   {  super(origin, destination);
   }

   ASDGrammarSuccessor getGrammarSuccessor()
   {  return successor;
   }

   void setGrammarSuccessor(ASDGrammarSuccessor s)
   {  successor = s;
   }

   ASDEditEdge getEditEdge()
   {  return eEdge;
   }

   void setEditEdge(ASDEditEdge e)
   {  eEdge = e;
   }

   private ASDEditEdge eEdge;     // the corresponding edit edge

   private ASDGrammarSuccessor successor;
      // the corresponding grammar successor
} // end class ASDDigraphEdge
