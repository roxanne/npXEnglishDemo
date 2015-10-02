package englishdemo;
import java.util.*;

/**
   An experimental class that provides static methods for testing
   and computing various properties of strings that are presumed
   to be English words.
   @author James A. Mason
   @version 1.02 2003 Jun
 */
public class EnglishWord
{
   /**
      Tests whether a given String can be an English preposition.
    */
   public static boolean isPreposition(String word)
   {  word = word.trim().toLowerCase();
      return PREPOSITIONS.contains(word);
   }

   /**
      Tests and processes a given String (assumed to be a word with no
      space in it) which may have apostrophes in it.
If the string has an apostrophe, the method tries to expand the string into a string of one or
more lexical items, separated by spaces, and identifies the string, to the extent possible,
as one or more of "<tt>C</tt>", "<tt>L</tt>", "<tt>P</tt>" or "<tt>U</tt>" for
"<tt>Contraction</tt>", "<tt>pLural</tt>", "<tt>Possessive</tt>", or "<tt>Unknown</tt>".&nbsp;
If the identifying string has length 2 or more, the given string is ambiguous
among the types indicated in the identifying string.&nbsp;
Examples are:
<br><tt>&nbsp;&nbsp; "car's" -> ("car APOSTROPHEs", "CP")</tt>
<br><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
's could be possessive or "is" or "has"</tt>
<br><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
(as in "the car's been in the shop")</tt>
<br><tt>&nbsp;&nbsp; "cars'" -> ("cars APOSTROPHE", "P")</tt>
<br><tt>&nbsp;&nbsp; "a's" -> ("a APOSTROPHEs", "CLP")</tt>
<br><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
's could be possessive or "is" or "has" or</tt>
<br><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
plural ending</tt>
<br><tt>&nbsp;&nbsp; "I'm" -> ("I am", "C")</tt>
<br><tt>&nbsp;&nbsp; "it's" -> ("it is", "C")</tt>
<br><tt>&nbsp;&nbsp; "'t's" -> ("it is", "C")</tt>
<br><tt>&nbsp;&nbsp; "they'll" -> ("they will", "C")</tt>
<br><tt>&nbsp;&nbsp; "goin'" -> ("going", "C")</tt>
<br><tt>&nbsp;&nbsp; "we're" -> ("we are", "C")</tt>
<br><tt>&nbsp;&nbsp; "'til" -> ("until", "C")</tt>
<br><tt>&nbsp;&nbsp; "'tis" -> ("it is", "C")</tt>
<br><tt>&nbsp;&nbsp; "'tisn't" -> ("it is not", "C")</tt>
<br><tt>&nbsp;&nbsp; "'taint" -> ("it aint", "C")</tt>
<br><tt>&nbsp;&nbsp; "'twill" -> ("it will", "C")</tt>
<br><tt>&nbsp;&nbsp; "'twould" -> ("it would", "C")</tt>
<br><tt>&nbsp;&nbsp; "'twouldn't" -> ("it would not", "C")</tt>
<br><tt>&nbsp;&nbsp; "girl'd" -> ("girl 'd", "C")</tt>
<br><tt>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Note: "'d" could represent "had" or "would".</tt>
<br><tt>&nbsp;&nbsp; "you'd" -> ("you 'd", "C")</tt>
<br><tt>&nbsp;&nbsp; "you'ld" -> ("you would", "C")</tt>
<br><tt>&nbsp;&nbsp; "can't" -> ("cannot", "C")</tt>
<br><tt>&nbsp;&nbsp; "don't" -> ("do not", "C")</tt>
<p>If there is no apostrophe in the given String, the method returns null.
If the string (e.g. "o'clock", "O'Reilly") has an apostrophe but is not
one of the types that the method recognizes, the "expanded" String in the
returned value is the same as the given String, and the description in the
returned value is "U" for Unknown.
<p>Note: This method retains the initial capitalization of the first letter
of the given string.
<br><br>
    @param givenWord a presumed English word that may have apostrophes in it.
    @return an ArrayList containing the expanded String and a String that
     describes it; the returned value is null if the given word has no
     apostrophes.
    */
   public static ArrayList processApostrophe(String givenWord)
   {  givenWord = givenWord.trim();
      int length = givenWord.length();
      char firstLetter = givenWord.charAt(0);
      if (firstLetter == '\'' && length >= 2)
         firstLetter = givenWord.charAt(1);
      int apostrophePosition = givenWord.indexOf('\'');
      if (apostrophePosition < 0) return null; // no apostrophe
      ArrayList result = new ArrayList(2);
      String expandedForm = givenWord;
      String description = "U";  // default is Unknown
      if (length <= 2)
      {  result.add(expandedForm);
         result.add(description);
         return result;
      }

      String word = givenWord.toLowerCase();

      // Check for initial "'t":
      if (word.substring(0,2).equals("'t"))
      {  if (word.equals("'til"))
         {  if (Character.isLowerCase(firstLetter))
               expandedForm = "until";
            else
               expandedForm = "Until";
            description = "C";
            result.add(expandedForm);
            result.add(description);
            return result;
         }
         // The initial "'t" is a contraction for "it".
         // Expand it first:
         if (Character.isLowerCase(firstLetter))
         {  word = "it " + word.substring(2);
            firstLetter = 'i';
         }
         else
         {  word = "It " + word.substring(2);
            firstLetter = 'I';
         }
         // Check for special case of 't's or 'T's:
         if (givenWord.equalsIgnoreCase("'t's"))
         {  expandedForm = firstLetter + "t is";
            description = "C";
            result.add(expandedForm);
            result.add(description);
            return result;
         }
         // Now see if there is another apostrophe:
         apostrophePosition = word.indexOf('\'');
         if (apostrophePosition < 0)  // no other apostrophe
         {  expandedForm = word;
            description = "C";
            result.add(expandedForm);
            result.add(description);
            return result;
         }
         length = word.length();
      }

      if (apostrophePosition == length-1) // final apostrophe
      {  if (word.substring(length-2).equals("s'"))
         {  expandedForm = firstLetter + word.substring(1, apostrophePosition)
                           + " APOSTROPHE";
            description = "P";
         }
         else if (word.substring(length-3).equals("in'"))
         {  expandedForm = firstLetter + word.substring(1, length-1) + "g";
            description = "C";
         }
         else  // Unknown form
         {  expandedForm = givenWord;
            description = "U";
         }
      }
      else if (apostrophePosition == length-2) // penultimate apostrophe
      {  if (word.substring(length-1).equals("s"))
         {  String beforeApostrophe
               = firstLetter + word.substring(1, apostrophePosition);
            if (length <= 3)
            {  expandedForm = beforeApostrophe + " APOSTROPHEs";
               description = "CLP";  // e.g. x's
            }
            else if (word.equals("he's") || word.equals("she's")
                  || word.equals("it's"))
            {  expandedForm = beforeApostrophe + " is";
               description = "C";
            }
            else
            {  expandedForm = beforeApostrophe + " APOSTROPHEs";
               description = "CP";
            }
         }
         else if (word.equals("i'm"))
         {  expandedForm = firstLetter + " am";
            description = "C";
         }
         else
         {  description = "C";
            if (word.substring(length-3).equals("n't"))
            {  if (word.equals("can't"))
                  expandedForm = firstLetter + "annot";
               else
                  expandedForm
                     = firstLetter + word.substring(1, length-3) + " not";
            }
            else if (word.substring(length-2).equals("'d"))
               expandedForm
                  = firstLetter + word.substring(1, length-2) + " 'd";
            else
            {  expandedForm = givenWord;
               description = "U";
            }
         }
      }
      else if (apostrophePosition == length-3) // antepenultimate apostrophe
      {  String lastThree = word.substring(length-3);
         String allButLastThree
            = firstLetter + word.substring(1, length-3);
         description = "C";
         if (lastThree.equals("'re"))
            expandedForm = allButLastThree + " are";
         else if (lastThree.equals("'ld"))
            expandedForm = allButLastThree + " would";
         else if (lastThree.equals("'ll"))
            expandedForm = allButLastThree + " will";
         else
         {  expandedForm = givenWord;
            description = "U";
         }
      }
      else                          // unknown apostrophe form
      {  expandedForm = givenWord;
         description = "U";
      }

      result.add(expandedForm);
      result.add(description);
      return result;
   }  // end of processApostrophe

   private static HashSet PREPOSITIONS;

   static
   {  PREPOSITIONS = new HashSet(61);
      PREPOSITIONS.add("aboard");
      PREPOSITIONS.add("about");
      PREPOSITIONS.add("above");
      PREPOSITIONS.add("across");
      PREPOSITIONS.add("after");
      PREPOSITIONS.add("against");
      PREPOSITIONS.add("along");
      PREPOSITIONS.add("alongside");
      PREPOSITIONS.add("among");
      PREPOSITIONS.add("around");
      PREPOSITIONS.add("at");
      PREPOSITIONS.add("athwart");
      PREPOSITIONS.add("atop");
      PREPOSITIONS.add("before");
      PREPOSITIONS.add("behind");
      PREPOSITIONS.add("below");
      PREPOSITIONS.add("beneath");
      PREPOSITIONS.add("beside");
      PREPOSITIONS.add("besides");
      PREPOSITIONS.add("between");
      PREPOSITIONS.add("beyond");
      PREPOSITIONS.add("but");
      PREPOSITIONS.add("by");
      PREPOSITIONS.add("despite");
      PREPOSITIONS.add("down");
      PREPOSITIONS.add("during");
      PREPOSITIONS.add("except");
      PREPOSITIONS.add("for");
      PREPOSITIONS.add("from");
      PREPOSITIONS.add("in");
      PREPOSITIONS.add("inside");
      PREPOSITIONS.add("into");
      PREPOSITIONS.add("like");
      PREPOSITIONS.add("near");
      PREPOSITIONS.add("notwithstanding");
      PREPOSITIONS.add("of");
      PREPOSITIONS.add("off");
      PREPOSITIONS.add("on");
      PREPOSITIONS.add("onto");
      PREPOSITIONS.add("opposite");
      PREPOSITIONS.add("out");
      PREPOSITIONS.add("outside");
      PREPOSITIONS.add("over");
      PREPOSITIONS.add("past");
      PREPOSITIONS.add("round");
      PREPOSITIONS.add("since");
      PREPOSITIONS.add("through");
      PREPOSITIONS.add("throughout");
      PREPOSITIONS.add("till");
      PREPOSITIONS.add("to");
      PREPOSITIONS.add("toward");
      PREPOSITIONS.add("under");
      PREPOSITIONS.add("underneath");
      PREPOSITIONS.add("until");
      PREPOSITIONS.add("unto");
      PREPOSITIONS.add("up");
      PREPOSITIONS.add("with");
      PREPOSITIONS.add("within");
      PREPOSITIONS.add("without");
   }
} // end class EnglishWord