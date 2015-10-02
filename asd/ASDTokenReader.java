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

/**
   A character-stream reader that gets tokens needed
   for creating an ASDGrammar.  The tokens are strings:
   ( - a left parenthesis;
   ) - a right parenthesis;
   a string of contiguous non-whitespace characters not
     including parentheses or initial quotes;
   a quoted substring surrounded by either "s or 's,
     with quotes of the same kind preceded by \ treated
     as included in the string; or
   an empty string if the end of the stream has been reached.

   @author  James A. Mason
   @version 1.03 2000 Mar 22, 26; 2001 Feb 5; Nov 23;2002 Jul 22;
      2004 Jan 29
 */
class ASDTokenReader extends PushbackReader
{  /**
      Creates a new ASDTokenReader with a one-character pushback buffer.
      @param  in  The reader from which characters will be read
    */
   public ASDTokenReader(Reader in)
   {  super(in);
   }

   /**
      Gets the next token, if any, from the stream.
      @return a String containing the token, empty string if none.
    */
   public String getToken()
      throws IOException
   {  skipWhitespace();
      int ch = read();
      if (ch < 0 || ch == 65535)  // no more tokens
         return "";
      char cha = (char)ch;
      if (cha == '(') return "(";
      if (cha == ')') return ")";
      // if character is not a parenthesis, put it back
      // on the input stream;
      unread(ch);
      if (cha == '\'' || cha == '"')
         return getQuotedString();
      return getPseudoWord();
   } // end getToken

   /** Advances the reader past any initial whitespace characters.
    */
   private void skipWhitespace()
      throws IOException
   {  int ch;
      ch = read();
      while(ch >= 0) {
         if (!Character.isWhitespace((char)ch))
            break;  // not a whitespace character
         ch = read();
         }
      unread(ch); // put back the non-whitespace character
   } // end skipWhitespace

   /** Gets a left parenthesis.  Throws an ASDInputException
       if a left parenthesis is not found next in the input.
       @return the left parenthesis, if found.
    */
   public char getLeftParenthesis()
      throws IOException, ASDInputException
   {  int ch = read();
      char cha = (char)ch;
      if (cha == '(') return cha;
      unread(ch);
      throw new ASDInputException("missing ( where expected");
   //   return cha;
   } // end getLeftParenthesis

   /** Gets a right parenthesis.  Throws an ASDInputException
       if a right parenthesis is not found next in the input.
       @return the right parenthesis, if found.
    */
   public char getRightParenthesis()
      throws IOException, ASDInputException
   {  int ch = read();
      char cha = (char)ch;
      if (cha == ')') return cha;
      unread(ch);
      throw new ASDInputException("missing ) where expected");
   //   return cha;
   } // end getRightParenthesis

   /** Gets a quoted string beginning with ' or ", including
      the quotes around the resulting string.  Throws an
      ASDInputException if there is not a quote character next
      in the input.
      @return the quoted string
    */
   public String getQuotedString()
      throws IOException, ASDInputException
   {  StringBuffer buffer = new StringBuffer();
      skipWhitespace();
      int ch = read();
      char quote = (char)ch;
      if (quote != '\"' && quote != '\'')
         throw new ASDInputException("missing quote character " +
            "at beginning of expected quoted string");
       return getQuotedString(quote);
   } // end getQuotedString

   /** Gets the rest of a quoted string beginning with a given
      quote character; assumes the given quote is ' or ".
      Throws an ASDInputException if the ending quote is missing
      when the end of the input stream is reached.
      @return the resulting string, including the surrounding quotes.
    */
   public String getQuotedString(char quote)
      throws IOException, ASDInputException
   {  StringBuffer buffer = new StringBuffer();
      buffer.append(quote);
      int ch;
      char cha;
      while(true) {
         ch = read();
         if (ch < 0)
            throw new ASDInputException("missing quote at end "
               + "of quoted string ending at end of input");
         cha = (char)ch;
         if (cha == quote) break;
         buffer.append(cha);
         if (cha == '\\') { // escape char
            ch = read();
            if (ch < 0) throw new ASDInputException(
               "unexpected end of input after \\ character");
            buffer.append((char)ch);
            }
         }
         buffer.append((char)ch);    // final quote
      return buffer.toString();
   } // end getQuotedString

   /** Gets a contiguous string of non-whitespace characters
      not including any parentheses or " characters.
      Throws an ASDInputException if a parenthesis or double quote
      character is found next in the input.
      @return the string obtained.
    */
   public String getPseudoWord()
      throws IOException, ASDInputException
   {  StringBuffer buffer = new StringBuffer();
      skipWhitespace();
      int ch = read();
      char cha = (char)ch;
      if (cha == '(' || cha == ')' || cha == '"')
         throw new ASDInputException("character " + cha +
            " found where a word or number was expected");
      buffer.append(cha);
      ch = read();
      while (ch >= 0) {
         cha = (char)ch;
         if (Character.isWhitespace(cha) || cha == '(' ||
             cha == ')' || cha == '"') break;
         buffer.append(cha);
         ch = read();
         }
      if (ch >= 0)  // should this be unconditional?
         unread(ch);
      return buffer.toString();
   } // end getPseudoWord

   /**
      Reads the next character (if any) from the stream,
      allowing for a short delay if the stream is not ready.
      @return the int value of the character read, -1 if none.
    */
   public int read()
      throws IOException
   {  int j = 10000;       // definitely a magic number!
      while(!ready() && j > 0)
         --j;
      return super.read();
   }

} // end class ASDTokenReader