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
import java.net.*;
import java.util.*;

/**
   Instances can create an ASDGrammar from a character file.
   @author James A. Mason
   @version 1.05 2000 Mar 24-26, 30; Apr 26, 28; 2001 Feb 5-7;
   Oct 1; Nov 20, 23; 2004 Jan 20, 29
 */
public class ASDGrammarReader
{
   /**
      Initializes a new ASDGrammarReader on a given file or URL.
      Throws an IOException or malformedURLException if the file
      or URL can't be opened.
      By default, pixel coordinates of grammar nodes are NOT loaded.
      @param fileName the name of the file or URL
    */
   public ASDGrammarReader(String fileName)
      throws IOException, MalformedURLException
   {  this(fileName, false);
   }

   /**
      Initializes a new ASDGrammarReader on a given file or URL.
      Throws an IOException or malformedURLException if the file
      or URL can't be opened.
      @param fileName the name of the file or URL
      @param includeCoords indicates whether or not to include
      pixel coordinates in the grammar loaded, if they are
      present.  They are needed by the graphical grammar editor
      but not by the parser.
    */
   public ASDGrammarReader(String fileName, boolean includeCoords)
      throws IOException, MalformedURLException
   {  includePixelCoords = includeCoords;
      fileName = fileName.trim();
      urlConnection = null;
      urlStream = null;
      if (fileName.substring(0,5).equalsIgnoreCase("http:"))
      {  URL fileURL = new URL(fileName);
         urlConnection = (HttpURLConnection) fileURL.openConnection();
//         System.out.println("urlConnection opened: "
//            + urlConnection.toString());
         urlStream = urlConnection.getInputStream();
//         urlStream.reset();
         reader = new ASDTokenReader(new BufferedReader(
            new InputStreamReader(urlStream)));
      }
      else
         reader = new ASDTokenReader(new FileReader(fileName));

   }

   /**
      Closes the InputStream used by the ASDGrammarReader,
      if the InputStream was opened from a URL.
    */
   public void close()
      throws IOException
   {  if (urlConnection != null)
      {  urlStream.close();
         urlConnection.disconnect();
      }
   }

   /**
      Gets an ASD grammar from the file.
      Throws an ASDInputException if the grammar representation
      in the file is ill-formed.
      @return a HashMap of words and ArrayLists of ASDGrammarNodes
      that represent their instances.
    */
   public HashMap getGrammar()
      throws IOException, ASDInputException
   {  HashMap result = new HashMap(DEFAULT_CAPACITY);
      ArrayList wordEntry;
      while(true)
      {  wordEntry = getWordEntry();
         if (wordEntry == null) break;
         result.put(wordEntry.get(0),
                    wordEntry.get(1));
      }
      reader.close();
      return result;
   } // end getGrammar

   /**
      Gets the next word entry from the file.
      @return an ArrayList consisting of a "word" String and a
       ArrayList of instances for it, each an ASDGrammarNode;
       or null if there are no more word entries in the file.
    */
   private ArrayList getWordEntry()
      throws IOException, ASDInputException
   {  ArrayList result = new ArrayList();
      String word;
      ArrayList instances = new ArrayList();
      ASDGrammarNode node;
      currentToken = reader.getToken();
      if (currentToken.length()==0)
         return null;  // no more word entries in the file
      if (!currentToken.equals("("))
      {  reader.close();
         throw new ASDInputException(
            "missing ( at beginning of a word entry");
      }
      word = reader.getToken();
      if (word.equals("(") || word.equals(")"))
      {  reader.close();
         throw new ASDInputException(
            "missing word in a word entry");
      }
      result.add(word);
      currentToken = reader.getToken();
      if (!currentToken.equals("("))
      {  reader.close();
         throw new ASDInputException(
            "missing ( around list of instance entries\n"
            + "for word " + word);
      }
      while(true)
      {  node = getInstanceEntry(word);
            // next instance entry, if any
         if (node == null)
            break;  //  no more instances of the word
         instances.add(node);
      }
      result.add(instances);
      // Get parenthesis that ends the word entry:
      reader.getRightParenthesis();
      return result;
   } // end getWordEntry

   /**
      Gets the next instance entry for a given word from the
      file, and returns a corresponding new ASDGrammarNode, or
      returns null if there are no more instances of the word
      in the grammar.
      The instance entry must have all of the information needed
      for a well-formed ASDGrammarNode;
      otherwise an ASDInputException is thrown.
    */
   private ASDGrammarNode getInstanceEntry(String word)
      throws IOException, ASDInputException
   {  String instance;
      boolean begins;
      ArrayList beginsTypes = null;
      ArrayList successors = null;
      ArrayList successorTypes = null;
      String phraseType = null;
      String semanticValue = null;
      String semanticAction = null;
      boolean endOfEntry = false; // indicates if ")" at end of
         // the grammar instance has been found
      short xCoord = 0;  // default horizontal pixel coordinate
      short yCoord = 0;  // default vertical pixel coordinate

      currentToken = reader.getToken();
      if (currentToken.equals(")")) // there are no more instances
         return null;          // of the word in the grammar.

      // Next token should be the instance index (number):
      instance = reader.getPseudoWord();

      // Next should be the "begins" field:
      currentToken = reader.getToken();
      if (currentToken.equalsIgnoreCase("nil") ||
          currentToken.equalsIgnoreCase("null") ||
          currentToken.equalsIgnoreCase("false"))
         begins = false;
      else if (currentToken.equalsIgnoreCase("t") ||
          currentToken.equalsIgnoreCase("true"))
      {  begins = true;
         beginsTypes = null;
      }
      else if (!currentToken.equals("("))
      {  reader.close();
         throw new ASDInputException(
            "missing parenthesis at start of 'begins' field\n"
            + "for word " + word + " instance " + instance);
      }
      else // should be list of phrase types that can begin at this node:
      {  currentToken = reader.getToken();
         if (currentToken.equals(")")) // empty list
            begins = false; // treat same as nil
         else
         {  char ch;
            begins = true;
            beginsTypes = new ArrayList();
            do
            {  ch = currentToken.charAt(0);
               if (ch == '(' || ch == '"' || ch == '\''
                   || Character.isDigit(ch))
               {  reader.close();
                  throw new ASDInputException(
                      "expected phrase type name missing "
                      + "in 'begins' field\nfor word "
                      + word + " instance " + instance
                      + "\nInstead found a token beginning with "
                      + ch);
               }
               beginsTypes.add(currentToken);
               currentToken = reader.getToken();
               if (currentToken.length() == 0)
               {  reader.close();
                  throw new ASDInputException(
                     "missing ) at end of 'begins' list\n"
                     + "for word " + word + " instance "
                     + instance);
               }
            } while (!currentToken.equals(")"));
         }
      }

      // Next should be the successors field:
      currentToken = reader.getToken();
      if (currentToken.equals(")") ||
            currentToken.length() == 0)
      {  reader.close();
         throw new ASDInputException(
            "successors field missing\nfor word "
            + word + " instance " + instance);
      }
      else if (currentToken.equals("("))
         // should be followed by list of successor
         // instances as (word index) pairs:
      {  successors = new ArrayList();
         currentToken = reader.getToken();
/* This check is omitted so the ASDEditor can load incomplete grammars:
         if (currentToken.equals(")"))
         {  reader.close();
            throw new ASDInputException(
               "empty successors list\nfor word "
               + word + " instance " + instance);
         }
 */
         while (!currentToken.equals(")"))
            // get a (word index) pair:
         {  if (!currentToken.equals("("))
            {  reader.close();
               throw new ASDInputException("missing " +
                 "( at start of a (word instance ... ) " +
                 "entry\nin successors list for word "
                 + word + " instance " + instance);
            }
            ASDGrammarSuccessor successor = new ASDGrammarSuccessor(
                 reader.getPseudoWord(), reader.getPseudoWord());

            if (includePixelCoords)
            {  // Get optional pixel coordinates for center
               // of the edge from the node to its successor:
               short xEdgeCoord = 0;
               short yEdgeCoord = 0;
               currentToken = reader.getToken();
               char ch = currentToken.charAt(0);
               if (Character.isDigit(ch))
               {  // pixel coordinates should be present
                  try
                  {  xEdgeCoord = Short.parseShort(currentToken);
                  }
                  catch (NumberFormatException e)
                  {  reader.close();
                     throw new ASDInputException("invalid edge pixel " +
                        "coordinate \"" + currentToken + "\"\n" +
                        "in successors list for word "
                        + word + " instance " + instance);
                  }
                  currentToken = reader.getToken();
                  try
                  {  yEdgeCoord = Short.parseShort(currentToken);
                  }
                  catch (NumberFormatException e)
                  {  reader.close();
                     throw new ASDInputException("invalid or missing edge " +
                        "pixel coordinate\nin successors list for word "
                        + word + " instance " + instance);
                  }
                  successor.setXCoordinate(xEdgeCoord);
                  successor.setYCoordinate(yEdgeCoord);
                  currentToken = reader.getToken();
               }
            }
            else
               // Ignore pixel coordinates if present:
               for (int j = 0; j < 3; ++j)
               {  currentToken = reader.getToken();
                  if (currentToken.equals(")")) break;
               }

            if (!currentToken.equals(")"))
            {  reader.close();
               throw new ASDInputException("missing " +
                 ") at end of (word instance ... ) entry\n" +
                 "in successors list for word "
                 + word + " instance " + instance);
            }

            // add the new ASDGrammarSuccessor to successors list:
            successors.add(successor);
            currentToken = reader.getToken();
         } // end while(!currentToken.equals(")"));
      }
      else  // The instance is a final one.  The value in
            // this field should be a phrase type name;
            // (We know it is not a parenthesis here.)
         phraseType = currentToken;

      // Next can be the optional successor types
      // or semantic value field:
      currentToken = reader.getToken();
      if (currentToken.equals(")"))
         // the last two (optional) fields are omitted
      {  endOfEntry = true;
         if (successors != null)
            // the instance is not final, but none of its
            // successors are phrase types
            successorTypes = new ArrayList(0);
      }
      else if (currentToken.equalsIgnoreCase("t"))
         // The instance has some unspecified phrase types
         // as successors:
         ; // successorTypes is already null
      else
      {  successorTypes = new ArrayList();
         if (currentToken.equalsIgnoreCase("nil") ||
             currentToken.equalsIgnoreCase("null"))
            // None of the successors are phrase types.
            ;
         else if (currentToken.equals("("))
            // Get a list of the successor types:
         {  currentToken = reader.getToken();
            char ch;
            while(!currentToken.equals(")"))
            {  ch = currentToken.charAt(0);
               if (ch == '(' || ch == '"' || ch == '\''
                   || Character.isDigit(ch)
                  )
               {  reader.close();
                  throw new ASDInputException(
                     "expected phrase type name missing "
                     + "or starts with digit character "
                     + "in 'successorTypes' field\nfor word "
                     + word + " instance " + instance);
               }
               successorTypes.add(currentToken);
               currentToken = reader.getToken();
               if (currentToken.length() == 0)
               {  reader.close();
                  throw new ASDInputException("missing ) "
                     + " at end of 'successorTypes' list\n"
                     + "for word " + word + " instance "
                     + instance);
               }
            }
         }
         else if (currentToken.length() > 0)
            // Next field should be an optional
            // semantic value String:
         {  char ch = currentToken.charAt(0);
            if (ch != '\"' && ch != '\'')
            {  reader.close();
               throw new ASDInputException("missing quote " +
                  "at beginning of semantic value field\n"
                  + "for word " + word + " instance "
                  + instance);
            }
            // Remove the surrounding quotes:
            semanticValue = currentToken.substring(1,
                               currentToken.length()-1);
         }
      }

      // Next can be the optional semantic action field,
      // if the entry had a semantic value or successor types field.
      if (!endOfEntry)
      {  currentToken = reader.getToken();
         if (!currentToken.equals(")"))
            // the semantic action field is not omitted
         {  char ch = currentToken.charAt(0);
            if (currentToken.equalsIgnoreCase("nil") ||
                  currentToken.equalsIgnoreCase("null"))
               ;  // semanticAction is null
            else if (ch != '\"' && ch != '\'')
            {  reader.close();
               throw new ASDInputException("missing quote "
                  + "at beginning of semantic action field\n"
                  + "for word " + word + " instance " + instance);
            }
            else // Remove the surrounding quotes:
               semanticAction = currentToken.substring(1,
                                   currentToken.length()-1);
            if (includePixelCoords)
            {  xCoord = 0;   // default
               yCoord = 0;   // default
               currentToken = reader.getToken();
               if (currentToken.length() == 0)
               {  reader.close();
                  throw new ASDInputException(
                       "unexpected end of grammar file "
                     + "in entry for word " + word + "instance " + instance);
               }
               if (Character.isDigit(currentToken.charAt(0)))
               {  // pixel coordinates should be present
                  try
                  {  xCoord = Short.parseShort(currentToken);
                  }
                  catch (NumberFormatException e)
                  {  reader.close();
                     throw new ASDInputException("invalid edge pixel " +
                           "coordinate \"" + currentToken + "\"\n" +
                           "in entry for word "
                           + word + " instance " + instance);
                  }
                  currentToken = reader.getToken();
                  if (currentToken.length() == 0)
                  {  reader.close();
                     throw new ASDInputException(
                         "unexpected end of grammar file"
                       + " in entry for word " + word
                       + "instance " + instance);
                  }
                  try
                  {  yCoord = Short.parseShort(currentToken);
                  }
                  catch (NumberFormatException e)
                  {  reader.close();
                     throw new ASDInputException("invalid or missing " +
                        "pixel coordinate\nin entry for word "
                        + word + " instance " + instance);
                  }
               currentToken = reader.getToken();
               }
            }
            else
               // Ignore optional pixel coordinates for node:
               for (int j = 0; j < 3; ++j)
               {  currentToken = reader.getToken();
                  if (currentToken.equals(")")) break;
               }
            if (!currentToken.equals(")"))
            {  reader.close();
               throw new ASDInputException("missing "
                 + ") at end of a word instance entry\n"
                 + "for word " + word + " instance "
                 + instance);
            }
         }
      }

      ASDGrammarNode result = new ASDGrammarNode(word, instance, begins,
         beginsTypes, successors, successorTypes,
         phraseType, semanticValue, semanticAction);
      if (includePixelCoords)
      {  result.setXCoordinate(xCoord);
         result.setYCoordinate(yCoord);
      }

      return result;
   } // end getInstanceEntry

   private static final int DEFAULT_CAPACITY = 101;
   HttpURLConnection urlConnection;
   InputStream urlStream = null;  // stream to get grammar from a web site
   private ASDTokenReader reader; // used to get tokens from the file
   private String currentToken;   // the token most recently obtained
   private boolean includePixelCoords;  // indicates whether pixel
      // coordinates of grammar nodes are to be included in the
      // grammar created.  They are needed by the graphical grammar
      // editor but not by the parser.
} // end class ASDGrammarReader
