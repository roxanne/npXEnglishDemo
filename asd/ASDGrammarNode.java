/*

Copyright 2000-2005 James A. Mason

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
import java.util.*;

/**
   Instances are nodes in an ASDGrammar

   @author James A. Mason
   @version 1.05 2000 Mar-Apr; 2001 Feb, Oct, Nov; 2003 Jul
    2004 Aug (minor comment fix); 2005 Oct
 */
public class ASDGrammarNode
{
   /**
      Initializes a new ASDGrammarNode.  Pixel coordinates
      are set to 0, 0 by default.
      @param word the vocabulary element for which
       the node represents an instance
      @param instance the instance index of the node
      @param begins indicates whether or not the node
       can begin a phrase
      @param beginsTypes indicates what types of
       phrase (if any) can begin at the node; null
       indicates unspecified types
      @param successors lists pairs (word instance-index)
       of successors of the node in the grammar, if any;
       null indicates that the node is a final node
      @param successorTypes lists the phrase types in
       the successor nodes (if any); if successors is
       not null, null here indicates that there are
       successor phrase types with unspecified names
      @param phraseType names the phrase type that
       ends at the node, if the node is a final one
      @param semanticValue a String to be evaluated as
       the semantic value of the completed phrase,
       if the node is a final one; null indicates no
       semantic value to be computed
      @param semanticAction a String to be evaluated
       when the node is entered during a parse; null
       indicates no semantic action to be performed
     */
   public ASDGrammarNode(String word, String instance,
      boolean begins, ArrayList beginsTypes,
      ArrayList successors, ArrayList successorTypes,
      String phraseType, String semanticValue,
      String semanticAction)
   {
      nodeWord = word;
      nodeInstance = instance;
      nodeBegins = begins;
      nodeBeginsTypes = beginsTypes;
      nodeSuccessors = successors;
      nodeSuccessorTypes = successorTypes;
      nodePhraseType = phraseType;
      nodeSemanticValue = semanticValue;
      nodeSemanticAction = semanticAction;
      xCoordinate = 0;
      yCoordinate = 0;
   }

   /**
      Returns a list of strings that name the phrase types
      that can begin, directly or indirectly, at the node
      (if it is an initial node);
      returns null if the node is not initial or if the
      phrase types that it can begin are not known
      specifically.
    */
   public ArrayList beginsTypes()
   {  if (nodeBegins)
         return nodeBeginsTypes;
      else
         return null;
   }

   /**
      Returns the horizontal pixel coordinate of the node.
    */
   public short getXCoordinate() { return xCoordinate; }

   /**
      Returns the vertical pixel coordinate of the node.
    */
   public short getYCoordinate() { return yCoordinate; }

   /**
      Returns the String that indicates the instance of the
      word in the grammar that is represented by the node.
    */
   public String instance() { return nodeInstance; }

   /**
      Indicates whether or not the node is a final one
      in the grammar -- that is, whether it ends a phrase.
    */
   public boolean isFinal() { return nodeSuccessors == null; }

   /**
      Indicates whether or not the node is an initial
      one in the grammar -- that is, whether it can begin
      a phrase.
    */
   public boolean isInitial() { return nodeBegins; }

   /**
      Returns a String that names the phrase type that
      ends at the node, if the node is a final node;
      returns null if the string is not a final node.
    */
   public String phraseType()
   {  if (isFinal())
         return nodePhraseType;
      else
         return null;
   }

   /**
      Returns the semantic action String for the node,
      or null if it has no semantic action to perform.
    */
   public String semanticAction() { return nodeSemanticAction; }

   /**
      Returns the semantic value String for the node,
      or null if it has no semantic value to compute
    */
   public String semanticValue() { return nodeSemanticValue; }

   /**
      Returns a list of successor instances of the node,
      if it is not a final node in the grammar (each
      successor instance is represented by an
      ASDGrammarSuccessor instance);
      returns null if the node is a final node.
    */
   public ArrayList successors() { return nodeSuccessors; }

   /**
      Returns a list of Strings that name the phrase
      types that occur in successors of the node, if
      it is not a final node; returns null if the node
      has some unspecified phrase types in successors,
      or if the node is a final node.
    */
   public ArrayList successorTypes() { return nodeSuccessorTypes; }

   /**
      Returns a String that represents an ASDGrammarNode in character
      form optimized for parsing.
    */
   public String toString()
   {  return toString(true);
   }

   /**
      Returns a String that represents an ASDGrammarNode in character
      form, as used for the representation of a word instance in the
      character file representation of an ASD grammar.
      @param optimize indicates whether or not the node should be
      converted to a form that is optimized for parsing.
    */
   public String toString(boolean optimize)
   {  StringBuffer buffer = new StringBuffer(BUFFER_CAPACITY);
      buffer.append("  (").append(instance()).append(' ');
      if (isInitial())
      {  ArrayList beginsTypes = beginsTypes();
         if (!optimize || beginsTypes == null) // begins unspecified types
            buffer.append("T ");
         else  // Create a list of names of types the node can begin:
         {  buffer.append('(');
            for (Iterator j = beginsTypes.iterator(); j.hasNext(); )
            {  buffer.append((String) j.next());
               if (j.hasNext())
                  buffer.append(' ');
            }
            buffer.append(") ");
         }
      }
      else // the grammar node is not an initial one
         buffer.append("nil ");

      if (isFinal())
      {  buffer.append(phraseType());
         // Add the semantic value string in quotes:
         String value = semanticValue();
         buffer.append(" '");
         if (value != null && value.length() > 0)
            buffer.append(value);
         buffer.append("' ");
      }
      else // The node is not a final one.
      {  // Add the list of its successors:
         buffer.append('(');
         for (Iterator it = successors().iterator(); it.hasNext(); )
         {  ASDGrammarSuccessor s = (ASDGrammarSuccessor) it.next();
            buffer.append('(').append(s.getWord()).append(' ')
                  .append(s.getInstance()).append(' ')
                  .append(s.getXCoordinate()).append(' ')
                  .append(s.getYCoordinate()).append(')');
            if (it.hasNext())
               buffer.append(' ');
         }
         buffer.append(')');

         if (optimize)
         {  // Add the list of successor types:
            buffer.append(" (");
            for (Iterator it = successorTypes().iterator(); it.hasNext(); )
            {  buffer.append((String)it.next());
               if (it.hasNext())
                  buffer.append(' ');
            }
            buffer.append(") ");
         }
         else if (successorTypes().size() > 0)
            // Add T to indicate there are unspecified successor types
            buffer.append(" T ");
         else
            // Add nil to indicate there are no unspecified successor types
            buffer.append(" nil ");
      }

      // Add the semantic action string in quotes:
      String action = semanticAction();
      buffer.append('\'');
      if (action != null && action.length() > 0)
         buffer.append(action);
      buffer.append("' ");

      // Add the horizontal and vertical pixel coordinates:
      buffer.append(getXCoordinate()).append(' ')
            .append(getYCoordinate());
      buffer.append(")\n");  // close final bracket and add newline

      return buffer.toString();
   }  // end toString

   /**
      Returns the word in the node
     */
   public String word() { return nodeWord; }

   /* accessor method for hasIncoming flag; it has package scope
    */
   boolean hasIncomingEdges() { return hasIncoming; }

   /* mutator methods to be used by the ASDGrammarEditor and
      ASDGrammarReader; they all have package scope */

   void setBegins(boolean begins)
      { nodeBegins = begins; }
   void setBeginsTypes(ArrayList beginsTypes)
      { nodeBeginsTypes = beginsTypes; }
   void setHasIncoming(boolean has)
      { hasIncoming = has; }
   void setInstance(String instance)
      { nodeInstance = instance; }
   void setPhraseType(String phraseType)
      { nodePhraseType = phraseType; }
   void setSemanticAction(String semanticAction)
      { nodeSemanticAction = semanticAction; }
   void setSemanticValue(String semanticValue)
      { nodeSemanticValue = semanticValue; }
   void setSuccessors(ArrayList successors)
      { nodeSuccessors = successors; }
   void setSuccessorTypes(ArrayList successorTypes)
      { nodeSuccessorTypes = successorTypes; }
   void setWord(String word) {nodeWord = word;}
   void setXCoordinate(short x) {xCoordinate = x;}
   void setYCoordinate(short y) {yCoordinate = y;}

   private final static int BUFFER_CAPACITY = 80;

   protected String nodeWord;
      // the word of which this is an instance
   protected String nodeInstance;
      // index of the instance for its word in the grammar
   protected boolean nodeBegins;
      // indicates whether or not the node begins a phrase in the grammar
   protected ArrayList nodeBeginsTypes;
      /* if nodeBegins is true, this list of Strings lists the names
         of the phrase types that can begin, directly or indirectly
         at this node.  The value null indicates that the instance
         can begin phrases of unspecified types.
       */
   protected ArrayList nodeSuccessors;
      /* a list of successors of the instance in the grammar, each
         represented by an ASDGrammarSuccessor instance;
         null if the instance is a final instance.
       */
   protected ArrayList nodeSuccessorTypes;
      /* if nodeSuccessors is not null, a list of Strings representing
         phrase types that occur in successors of the instance;
         also in that case, a value null here indicates that some
         unspecified phrase types occur in successors of the instance.
       */
   protected String nodePhraseType;
      /* if nodeSuccessors is null, a String that names the phrase type
         that ends at the node
       */
   protected String nodeSemanticValue;
      /* If nodeSuccessors is null, this String contains an expression
         to be evaluated as the semantic value of the subphrase completed
         at this instance.  A value null here means that there is no
         semantic value to be computed.
       */
   protected String nodeSemanticAction;
      /* an expression to be evaluated upon entry to this instance in the
         grammar during a parse.  A value of null means that there is no
         semantic value to be computed.
       */
   protected short xCoordinate;
      // horizontal pixel coordinate of the node
   protected short yCoordinate;
      // vertical pixel coordinate of the node
   protected boolean hasIncoming = false;  // false until known to be true
      // used to indicate whether the node has any incoming edges
} // end class ASDGrammarNode