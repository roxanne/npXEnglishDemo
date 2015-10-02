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

/**
   Instances are successor links from ASDGrammarNodes.
   They correspond to edges in an ASD grammar diagram
   and point to successor ASDGrammarNodes.
   @author James A. Mason
   @version 1.05 2000 Apr 25, 27; 2001 Feb 5; Oct 1; Nov 9;
    2004 Aug
 */
public class ASDGrammarSuccessor
{
   /**
      Initializes a new ASDGrammarSuccessor.
      @param word the word in the successor node
      @param instance the instance index in the successor node
    */
   ASDGrammarSuccessor(String word, String instance)
   {  successorWord = word;
      successorInstance = instance;
      successorNode = null;
      xCoordinate = 0;
      yCoordinate = 0;
   }

   // Accessor and mutator methods:

   /**
      Returns the word string of the successor node.
    */
   public String getWord() { return successorWord; }

   /**
      Returns the instance index of the successor node.
    */
   public String getInstance() { return successorInstance; }

   /**
      Returns the ASDGrammarNode that the successor goes to.
    */
   public ASDGrammarNode getNode() { return successorNode; }

   /**
      Returns the horizontal pixel coordinate of the handle of the
      digraph edge that corresponds to the successor.
    */
   public short getXCoordinate() { return xCoordinate; }

   /**
      Returns the vertical pixel coordinate of the handle of the
      digraph edge that corresponds to the successor.
    */
   public short getYCoordinate() { return yCoordinate; }

   /**
      Sets the successorInstance to a new value
    */
   void setInstance(String newInstance) { successorInstance = newInstance; }

   /**
      Sets the successorNode of the successor to a given
      ASDGrammarNode.
    */
   void setNode(ASDGrammarNode node) { successorNode = node; }

   /**
      Sets the horizontal pixel coordinate of the handle of the
      digraph edge that corresponds to the successor.
    */
   void setXCoordinate(short x) { xCoordinate = x; }

   /**
      Sets the vertical pixel coordinate of the handle of the
      digraph edge that corresponds to the successor.
    */
   void setYCoordinate(short y) { yCoordinate = y; }

   protected String successorWord;
      // the word in the node to which this successor links
   protected String successorInstance;
      // the index of the node to which this successor links
   protected ASDGrammarNode successorNode;
      // the node to which this successor links; this link is
      // set the first time the node (instance) is looked up
      // in the lexicon by the ASDGrammar lookupInstance method.
   protected short xCoordinate;
      // horizontal coordinate of the "handle" of the edge
   protected short yCoordinate;
      // vertical coordinate of the "handle" of the edge
} // end class ASDGrammarSuccessor
