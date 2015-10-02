package englishdemo;
import asd.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.StringTokenizer;

import semanticvalues.*;
import semanticvalues.ModifiableSemantics;
import semanticvalues.Qualifier;
import semanticvalues.SemanticValue;

/**
   NPXDemoSemantics provides methods for initializing and driving an
   instance of ASDParser to parse and interpret phrases with the
   grammar npXDemo.grm and its semantic actions and values.  In the
   package englishdemo, NPXDemoSemantics is used by NPXDemoTester.
   <BR><BR>
   NPXDemoSemantics also provides the Java functions which are to be invoked
   by semantic action and semantic value augmentations to the grammar
   npXdemo.grm for parts of English noun phrases.  A naming convention has
   been used in it, according to which each such member function begins with
   the name of the grammar module (among the modules merged to form
   npXdemo.grm) from which it is invoked.  The module name is followed by a
   designation of the node in that grammar module from which the function is
   invoked. Parts of the function names are separated by underscore
   characters. The string "_v" is added to the ends of names of functions
   which compute semantic values, to distinguish them from functions which
   perform semantic actions.
   <BR>For example, the function named cardord_NUMBER_1_v
   <BR>computes the semantic value for the node (NUMBER 1) in the module
   cardord.grm.
   <BR>Likewise, the function named grader2_NEGATIVE_1
   <BR>computes the semantic action for the node (NEGATIVE 1) in the module
   grader2.grm.

   @author James A. Mason
   @version 1.01 2005 February-March
 */
public class NpXDemoSemantics implements asd.ASDSemantics
{
   /**
      Creates an instance to be driven by a given application such
      as NPXDemoTester.
      @param givenApplication the driver application
    */
   NpXDemoSemantics(Object givenApplication)
   {  application = givenApplication;
      parser = new ASDParser(application, this);
      parser.useGrammar(GRAMMARNAME);
      expectedTypes = EXPECTEDTYPES;
   }

   /**
      Returns a representation of the current phrase structure in
      parenthesized form as provided by the ASD parser.
    */
   public String bracketPhrase()
   {  return parser.bracketPhrase();
   }

   /**
      Attempts to complete a parse of the current phrase structure.
      @return true if successful, false if unsuccessful.
    */
   public boolean completeParse()
   {  if (utterance == null || utterance.equals(""))
         return false;
      String advanceResult; // SUCCEED, NOADVANCE, or QUIT
      parseCompleted = false;
      resultMessage = "";

      while(steps < MAXSTEPS)
      {  advanceResult = parser.advance();
         if (advanceResult.equals(parser.QUIT))
         {  resultMessage +=
               "Parse quit after " + steps + " advance steps.";
            parseCompleted = false;
            return false;
         }
         else if (advanceResult.equals(parser.SUCCEED))
         {  ++steps;
            if (parser.done())
            {  resultMessage +=
                  "Successful parse in " + steps + " advance steps.";
               parseCompleted = true;
               steps = 0; // prepare for an attempt at an alternative parse
               return true;
            }
         }
         else if (advanceResult.equals(parser.NOADVANCE))
         {  if (!parser.backup())
            {  if (strict)
               {  // re-initialize for non-strict parse
                  parser.initialize(utterance, expectedTypes);
                  steps = 0;
                  strict = false;
                  resultMessage =
                     "Re-initialized for non-strict parsing.\n";
                  parseCompleted = false;
               }
               else
               {  resultMessage +=
                     "Parse failed after " + steps + " advance steps.";
                  steps = 0; // prepare for an attempt at an alternative parse
                  parseCompleted = false;
                  return false;
               }
            }
         }
         else  // this shouldn't happen
         {  resultMessage +=
               "Invalid result of ASDParser advance(maxSteps) in "
               + steps + " steps.";
            parseCompleted = false;
            return false;
         }
      }
      return false;
   } // end completeParse

   /**
      Returns the current node in the phrase structure being parsed.
    */
   public ASDPhraseNode currentNode()
   {  return parser.currentNode();
   }

   /**
      Returns the list of expected phrase types which are goals of the
      current parse.
    */
   public ArrayList getExpectedTypes()
   {  return expectedTypes;
   }

   /**
      Returns the name of the grammar file being used for parsing.
    */
   public String getGrammarFileName()
   {  return GRAMMARNAME;
   }

   /**
      Returns the message which describes the result of the current
      attempt at parsing.
    */
   public String getResultMessage()
   {  return resultMessage;
   }

   /**
      Initializes a given phrase to be parsed, using either a strict
      or a relaxed version of the grammar.
      @param phrase the phrase to be parsed
      @param strictFlag indicates whether or not to use strict grammar rules
    */
   public void initializePhrase(String phrase, boolean strictFlag)
   {  originalUtterance = phrase;
      utterance = morphologicallyAnalyze(originalUtterance);
      strict = strictFlag;
      parser.initialize(utterance, expectedTypes);
   }

   /**
      Does a preliminary analysis of the phrase to be parsed,
      separating punctuation marks, including apostrophes, from
      the words they are adjacent to or imbedded in.  Tokens
      containing apostrophes are expanded for parsing.
      @phrase the string to be analyzed
      @return the result with tokens separated by spaces
    */
   String morphologicallyAnalyze(String phrase)
   {  String result = "";
      StringTokenizer tokenizer = new StringTokenizer(phrase,
         parser.SPACECHARS + parser.SPECIALCHARS, true);
      // Punctuation marks must be separated from words before
      // the EnglishWord.processApostrophe method is applied.
      while(tokenizer.hasMoreTokens())
      {  String token = tokenizer.nextToken().trim();
         if (token.length() == 1) // punctuation or one-letter word
            result = result + " " + token;
         else if (token.length() > 1)
         {   ArrayList apostropheResult
               = (ArrayList)EnglishWord.processApostrophe(token);
            if (apostropheResult == null)
               result = result + " " + token;
            else
               result = result + " " + (String)apostropheResult.get(0);
         }
         // whitespace tokens are ignored
      }
      return result;
   } // end morphologically Analyze

   /**
      Returns the value of the current node in the phrase structure
      being parsed.
    */
   public Object nodeValue()
   {  return parser.currentNode().value();
   }

   /**
      Returns the long integer which is the value of the current node
      in the phrase structure being parsed, assuming it is a long
      integer.
    */
   public long longNodeValue()
   {  return ((Long)nodeValue()).longValue();
   }

   /**
      Returns a string representation of the semantic value of the
      current phrase, if the phrase has been completely parsed.
      Otherwise it returns a string indicating that the parse has
      not been completed.
    */
   public String phraseMeaning()
   {  if (parseCompleted)
      {  Object value = parser.phraseStructure().nextNode().value();
         if (value == null)
            return "null";
         else
            return value.toString();
         }
      else
         return "The phrase must be completely parsed first.";
   }

   /**
      Returns the current phrase structure for the phrase being parsed.
      @return the header node of the phrase structure
    */
   public ASDPhraseNode phraseStructure()
   {  return parser.phraseStructure();
   }

   /**
      Evaluates a string from a "semantic action" field in the
      ASDGrammar being used, by invoking the corresponding member
      function of this NpXDemoSemantics instance.
      @param action the semantic action string to be evaluated;
       the name of a public method in this class,
       one with no parameters that returns a String.
      @return the String returned by the method invoked in this
       class; usually null, but possibly the ASDParser.NOADVANCE
       or ASDParser.QUIT string.
    */
   public String semanticAction(String action)
   {  Method m = null;
      try
      {  m = this.getClass().getMethod(action, (Class<?>[]) null);
      }
      catch(NoSuchMethodException e)
         {  System.err.println("Error invoking method " + action);
            return null;
         }
      if (m != null)
         try
         {  return (String)m.invoke(this, (Object[]) null);
         }
         catch(InvocationTargetException e)
         {  System.err.println(
               "InvocationTargetException invoking method \"" + action + "\"");
            return null;
         }
         catch(IllegalAccessException e)
         {  System.err.println("IllegalAccessException invoking method "
               + action);
            return null;
         }
         catch(ClassCastException e)
         {  System.err.println("ClassCastException invoking method " + action);
            parser.showTree();
            return null;
         }
      else
         return null;
   }  // end semanticAction

  /**
      Evaluates a string from a "semantic value" field in the
      ASDGrammar being used, as a long integer number, a quoted
      string, or by invoking the member function of this
      NpXDemoSemantics instance which is named by the semantic
      value field.
      @param value the semantic value string to be evaluated,
       an integer, a string surrounded by double quotes, or
       the name of a public method in the this class,
       one with no parameters that returns an Object.
      @return the Long integer object, the String, or the Object
       returned by the member function invoked in this class,
       possibly the ASDParser.NOADVANCE or ASDParser.QUIT string.
    */
   public Object semanticValue(String value)
   {  boolean numericValue = true;
      long integerValue = 0;
      try  // try to interpret the value as an integer number
      {  integerValue = Long.parseLong(value);
      }
      catch(NumberFormatException e)
      {  numericValue = false;
      }
      if (numericValue)
         return new Long(integerValue);

      // See if value has quotes around it; if so, remove the
      // quotes and return the string between them:
      if (value.length() >= 2 && value.charAt(0) == '"'
          && value.charAt(value.length()-1) == '"')
         return value.substring(1, value.length()-1);

      Method m = null;
      try
      {  m = this.getClass().getMethod(value, (Class<?>[]) null);
      }
      catch(NoSuchMethodException e)
      {  System.err.println("No such method found: " + value);
         return null;
      }
      if (m != null)
         try
         {  return m.invoke(this, (Object[]) null);
         }
         catch(InvocationTargetException e)
         {  System.err.println(
               "InvocationTargetException invoking method \""
               + value + "\"");
            return null;
         }
         catch(IllegalAccessException e)
         {  System.err.println("IllegalAccessException invoking method "
               + value);
            return null;
         }
      else
         return null;
   }  // end semanticValue

   /**
      Initializes the list of expected phrase types which are goals
      of the current parse.
      @param types the expected phrase types
    */
   void setExpectedTypes(ArrayList types)
   {  expectedTypes = types;
   }

   /**
      Sets the ASDParser being used to indicate whether or not it
      should save uniquely parsed subphrases for optimization during
      parsing.
      @param save true indicates that uniquely parsed subphrases
      should be saved; false indicates they should not be saved
    */
   void setSaveUniquelyParsedSubphrases(boolean save)
   {  parser.setSaveUniquelyParsedSubphrases(save);
   }

   // helping functions to abbreviate calls to parser methods:

   private Object get(String feature)
   {  return parser.get(feature);
   }

   private void set(String feature, Object value)
   {  parser.set(feature, value);
   }

// ----- cardord.grm semantic actions and values

   public Object cardord_$$_1_v()
   {  return get("V");
   }

   public Object cardord_$$_2_v()
   {  return new QuantityCardinalSemantics(cardord_valueOfVTimesM());
   }

   public Object cardord_$$_3_v()
   {  return get("V");
   }

   public String cardord_1_1()
   {  set("V", new QuantityCardinalSemantics(1));
      return null;
   }

   public String cardord_2_1()
   {  set("V", new QuantityCardinalSemantics(2));
      return null;
   }

   public String cardord_3_1()
   {  set("V", new QuantityCardinalSemantics(3));
      return null;
   }

   public String cardord_a_1()
   {  set("V", new QuantityCardinalSemantics(1));
      return null;
   }

   public String cardord_CARDINAL_1()
   {  set("V", nodeValue());
      return null;
   }

   public String cardord_CARDINAL_2()
   {  QuantityCardinalSemantics value
         = (QuantityCardinalSemantics) nodeValue();
      if (value.cardinal >= cardord_valueOfM())
         return parser.NOADVANCE;
      set("V2", value);
      return null;
   }

   public Object cardord_CARDINAL_2_v()
   {  return new QuantityCardinalSemantics(
         cardord_valueOfVTimesM() +
         ((QuantityCardinalSemantics) get("V2")).cardinal);
   }

   public String cardord_CARDINALNUMBER_1()
   {  QuantityCardinalSemantics value
         = new QuantityCardinalSemantics((Long) nodeValue());
      long number = value.cardinal;
      if (number < 4) // 1, 2, 3 are handled separately
         return parser.NOADVANCE;
      set("V", value);
      return null;
   }

   public Object cardord_CARDTEEN_1_v()
   {  return new QuantityCardinalSemantics((Long) nodeValue());
   }

   public String cardord_DECADE_1()
   {  set("V", new QuantityCardinalSemantics((Long) nodeValue()));
      return null;
   }

   public Object cardord_nd_1_v()
   {  return new OrdinalSemantics(cardord_valueOfV());
   }

   public Object cardord_NUMBER_1_v()
   {  return new Long(parser.currentNode().word());
   }

   public Object cardord_ORD_1_v()
   {  return new OrdinalSemantics((Long) nodeValue());
   }

   public Object cardord_ORD_TENPOWER_1_v()
   {  if (get("V") == null) // subphrase starts at (ORD_TENPOWER 1)
         set("V", new QuantityCardinalSemantics(1));
      set("M", new OrdinalSemantics((Long) nodeValue()));
      if (((QuantityCardinalSemantics) get("V")).cardinal >=
          ((OrdinalSemantics) get("M")).position)
         return parser.NOADVANCE;
      return new OrdinalSemantics(cardord_ordinalValueOfVTimesM());
   }

   public String cardord_ORDINAL_1()
   {  OrdinalSemantics value = (OrdinalSemantics) nodeValue();
      if (value.position >= cardord_valueOfM())
         return parser.NOADVANCE;
      set("V2", value);
      return null;
   }

   public Object cardord_ORDINAL_1_v()
   {  return new OrdinalSemantics(
         cardord_valueOfVTimesM() +
         ((OrdinalSemantics) get("V2")).position);
   }

   public Object cardord_ORDUNIT_1_v()
   {  return new OrdinalSemantics((Long) nodeValue());
   }

   public Object cardord_ORDUNIT_2_v()
   {  return new OrdinalSemantics(
         cardord_valueOfV() + longNodeValue());
   }

   public Object cardord_rd_1_v()
   {  return new OrdinalSemantics(cardord_valueOfV());
   }

   public Object cardord_st_1_v()
   {  return new OrdinalSemantics(cardord_valueOfV());
   }

   public String cardord_TENPOWER_1()
   {  if (get("V") == null) // subphrase starts at (TENPOWER 1)
         set("V", new QuantityCardinalSemantics((long) 1));
      set("M", new QuantityCardinalSemantics((Long) nodeValue()));
      if (((QuantityCardinalSemantics) get("V")).cardinal >=
               ((QuantityCardinalSemantics) get("M")).cardinal)
         return parser.NOADVANCE;
      return null;
   }

   public Object cardord_th_1_v()
   {  return new OrdinalSemantics(cardord_valueOfV());
   }

   public Object cardord_UNIT_1_v()
   {  return new QuantityCardinalSemantics((Long) nodeValue());
   }

   public Object cardord_UNIT_2_v()
   {  return new QuantityCardinalSemantics(
         ((QuantityCardinalSemantics) get("V")).cardinal
         + longNodeValue());
   }

   public long cardord_ordinalValueOfM()
   {  return ((OrdinalSemantics) get("M")).position;
   }

   public long cardord_ordinalValueOfVTimesM()
   {  return cardord_valueOfV() * cardord_ordinalValueOfM();
   }

   public long cardord_valueOfM()
   {  return ((QuantityCardinalSemantics) get("M")).cardinal;
   }

   public long cardord_valueOfV()
   {  return ((QuantityCardinalSemantics) get("V")).cardinal;
   }

   public long cardord_valueOfVTimesM()
   {  return cardord_valueOfV() * cardord_valueOfM();
   }

// ----- cardordirp.grm semantic actions and values

   public Object cardordirp_$$_1_v()
   {  return get("ordinal");
   }

   public Object cardordirp_$$_2_v()
   {  return get("ordinal");
   }

   public Object cardordirp_$$_3_v()
   {  return get("total");
   }

   public Object cardordirp_$$_4_v()
   {  return get("total");
   }

   public String cardordirp_a_1()
   {  set("cardinal", new QuantityCardinalSemantics(1));
      return null;
   }

   public String cardordirp_ARITH_OP_1()
   {  set("op", (String) nodeValue());
      return null;
   }

   public String cardordirp_bakers_1()
   {  set("bakers", "true");
      return null;
   }

   public Object cardordirp_CARD_P_1_v()
   {  int direction = ((Integer) get("direction")).intValue();
      long cardinal = ((QuantityCardinalSemantics) nodeValue()).cardinal;
      OrdinalSemantics result = new OrdinalSemantics(cardinal + 1);
      result.direction = direction;
      result.origin = (String) get("origin");
      return result;
   }

   public String cardordirp_CARD_P1_1()
   {  QuantityCardinalSemantics totalSoFar
         = (QuantityCardinalSemantics) get("total");
      QuantityCardinalSemantics newValue
         = (QuantityCardinalSemantics) nodeValue();
      if (totalSoFar == null)  // no previous total
      {  set("total", newValue);
         if (newValue.spelledNumeral)
            set("spelled numeral", "true");
         else
            set("spelled numeral", null);
         return null;
      }
      QuantityCardinalSemantics newTotal;
      if (get("spelled numeral") != null
            && parser.currentNode().subphrase()
                  .word().equals("CARDINAL"))
      // previous cardinal was spelled and current cardinal is a plain
      // CARDINAL (see cardordirp.grm)
      {  String numeralSoFar = totalSoFar.cardinal + "";
         String newNumeral = newValue.cardinal + "";
         if (numeralSoFar.length() > newNumeral.length())
         {  int trailingZeroes = 0;
            for (int p = numeralSoFar.length()-1; p >= 0; --p)
            {  if (numeralSoFar.charAt(p) != '0')
                  break;
               ++trailingZeroes;
            }
            if (trailingZeroes >= 2
                && trailingZeroes >= newNumeral.length())
               // newNumeral could become part of numeralSoFar as a
               // spelled numeral in cardord.grm, don't allow them
               // to be conjoined here
               return parser.NOADVANCE;
         }
      }

      if (newValue.spelledNumeral) // current value is a spelled numeral
         set("spelled numeral", "true");
      else
         set("spelled numeral", null);
      newTotal = new QuantityCardinalSemantics(
                    totalSoFar.cardinal + newValue.cardinal);
      newTotal.spelledNumeral = false;
         // cardinals conjoined by "and" here are not spelled numerals
      set("total", newTotal);
      return null;
   }

   public String cardordirp_CARD_P2_1()
   {  QuantityCardinalSemantics total
         = (QuantityCardinalSemantics) get("total");
      if (total == null) // first visit to (CARD-P2 1)
      {  QuantityCardinalSemantics value
            = (QuantityCardinalSemantics) nodeValue();
         total = new QuantityCardinalSemantics(value.cardinal);
         total.spelledNumeral = value.spelledNumeral;
         set("total", total);
      }
      else // second or later visit to (CARD-P2 1)
      {  long first = total.cardinal;
         long second
            = ((QuantityCardinalSemantics) nodeValue()).cardinal;
         long combined;
         if ("plus".equals(get("op")))
            combined = first + second;
         else if ("minus".equals(get("op")))
            combined = first - second;
         else if ("times".equals(get("op")))
            combined = first * second;
         else  // purely for safety; shouldn't occur
            combined = second;
         total = new QuantityCardinalSemantics(combined);
         total.spelledNumeral = false;
         set("total", total);
      }
      return null;
   }

   public Object cardordirp_CARDINAL_1_v()
   {  return nodeValue();
   }

   public Object cardordirp_dozen_1_v()
   {  long number = 1;  // default
      QuantitySemantics quantity
         = (QuantitySemantics) get("quantity");
      if (quantity != null)
      {  if (quantity instanceof QuantityCardinalSemantics)
            number = ((QuantityCardinalSemantics) quantity).cardinal;
         else // temporary, until logic for handling vague and other
              // quantities is added
            return parser.NOADVANCE;
      }
      long dozen = 12;
      if (get("bakers") != null)
         dozen = 13;
      QuantityCardinalSemantics result
         = new QuantityCardinalSemantics(number * dozen);
      result.spelledNumeral = false;
      return result;
   }

   public Object cardordirp_last_1_v()
   {  OrdinalSemantics result = (OrdinalSemantics) get("ordinal");
      if (result == null) // "[very] last"
         result = new OrdinalSemantics(1);
      else  // came through (ORDINAL 1) or (next 1)
      {  if (result.position == 1 && "first".equals(result.origin))
            // "first [from/to/but/before] last"
            return parser.NOADVANCE;
         if ("current".equals(result.origin))  // came through (next 1)
            result.position = 2; // next [from/to/but/before] last"
      }
      result.direction = -1;
      result.origin = "last";
      return result;
   }

   public String cardordirp_last_2()
   {  set("direction", new Integer(-1));
      set("cardinal", new Long(1));
      set("origin", "last");
      return null;
   }

   public String cardordirp_next_1()
   {  OrdinalSemantics result = new OrdinalSemantics(1);
      result.direction = 1;
      result.origin = "current";
      set("ordinal", result);
      return null;
   }

   public String cardordirp_next_2()
   {  set("direction", new Integer(1));
      set("cardinal", new Long(1));
      set("origin", "current");
      return null;
   }

   public String cardordirp_ORDINAL_1()
   {  OrdinalSemantics nodeValue = (OrdinalSemantics) nodeValue();
      OrdinalSemantics result = new OrdinalSemantics(nodeValue.position);
      // result.direction = 1; // redundant, default for OrdinalSemantics
      // result.origin = "first"; // redundant, default
      set("ordinal", result);
      return null;
   }

   public Object cardordirp_ORDIR_1_v()
   {  return nodeValue();
   }

   public Object cardordirp_ORDIR_P_1_v()
   {  Qualifier qualifier = (Qualifier) get("qualifier");
      OrdinalSemantics result = (OrdinalSemantics) nodeValue();
      if (qualifier != null)
         return result.modifyBy(
            qualifier.qualifierName, qualifier.qualifierValue);
      return result.clone();
   }

   public Object cardordirp_preceding_1_v()
   {  OrdinalSemantics result = new OrdinalSemantics(1);
      result.direction = -1;
      result.origin = "current";
      return result;
   }

   public String cardordirp_preceding_2()
   {  set("direction", new Integer(-1));
      set("cardinal", new Long(1));
      set("origin", "current");
      return null;
   }

   public Object cardordirp_previous_1_v()
   {  OrdinalSemantics result = new OrdinalSemantics(1);
      result.direction = -1;
      result.origin = "current";
      return result;
   }

   public String cardordirp_previous_2()
   {  set("direction", new Integer(-1));
      set("cardinal", new Long(1));
      set("origin", "current");
      return null;
   }

   public String cardordirp_QUALIFIER_P_1()
   {  set("qualifier", nodeValue());
      return null;
   }

   public String cardordirp_QUANTITY_1()
   {  set("quantity", nodeValue());
      return null;
   }

   public Object cardordirp_score_1_v()
   {  long number = 1;  // default if subphrase begins at this node
      QuantitySemantics quantity
         = (QuantitySemantics) get("quantity");
      if (quantity != null)
      {  if (quantity instanceof QuantityCardinalSemantics)
            number = ((QuantityCardinalSemantics) quantity).cardinal;
         else // temporary, until logic for handling vague and other
              // quantities is added
            return parser.NOADVANCE;
      }
      QuantityCardinalSemantics result
         = new QuantityCardinalSemantics(number * 20);
      result.spelledNumeral = false;
      return result;
   }

   public Object cardordirp_zero_1_v()
   {  return new QuantityCardinalSemantics(0);
   }

// ----- grader1.grm semantic actions and values

   public Object grader1_absolutely_1_v()
   {  ProbabilitySemantics result = new ProbabilitySemantics(
         ProbabilitySemantics.CERTAIN);
      return new Qualifier("probability", result);
   }

   public Object grader1_certainly_1_v()
   {  ProbabilitySemantics result
         = new ProbabilitySemantics(ProbabilitySemantics.CERTAIN);
      return new Qualifier("probability", result);
   }

   public Object grader1_clearly_1_v()
   {  ProbabilitySemantics result
         = new ProbabilitySemantics(ProbabilitySemantics.ALMOST_CERTAIN);
      return new Qualifier("probability", result);
   }

   public Object grader1_definitely_1_v()
   {  ProbabilitySemantics result
         = new ProbabilitySemantics(ProbabilitySemantics.VERY_PROBABLE);
      return new Qualifier("probability", result);
   }

   public Object grader1_enough_1_v()
   {  ThresholdSemantics result
         = new ThresholdSemantics("sufficient");
      result.notApplicableTo = "sup";
      return result;
   }

   public Object grader1_excessively_1_v()
   {  ThresholdSemantics result
         = new ThresholdSemantics("excessive");
      result.notApplicableTo = "sup";
      return result;
   }

   public Object grader1_extremely_1_v()
   {  GraderSemantics result =
         new GraderSemantics(MagnitudeSemantics.EXTREMELY_HIGH);
      return result;
   }

   public Object grader1_fairly_1_v()
   {  return new GraderSemantics(MagnitudeSemantics.MODERATE);
   }

   public Object grader1_how_1_v()
   {  GraderSemantics result
         = new GraderSemantics(MagnitudeSemantics.INDEFINITE);
      result.interrogative = true;
      result.notApplicableTo = "sup";
      return result;
   }

   public Object grader1_however_1_v()
   {  GraderSemantics result
         = new GraderSemantics(MagnitudeSemantics.INDEFINITE);
      result.interrogative = true;
      result.notApplicableTo = "sup";
      return result;
   }

   public Object grader1_incredibly_1_v()
   {  SurprisingnessSemantics result
         = new SurprisingnessSemantics(MagnitudeSemantics.EXTREMELY_HIGH);
      return new Qualifier("surprisingness", result);
   }

   public Object grader1_incredibly_2_v()
   {  GraderSemantics result
         = new GraderSemantics(MagnitudeSemantics.EXTREMELY_HIGH);
      result.notApplicableTo = "compsup";
      return result;
   }

   public Object grader1_moderately_1_v()
   {  GraderSemantics result
         = new GraderSemantics(MagnitudeSemantics.MODERATE);
      result.notApplicableTo = "sup";
      return result;
   }

   public Object grader1_most_1_v()
   {  GraderSemantics result
         = new GraderSemantics(MagnitudeSemantics.HIGH);
      result.notApplicableTo = "compsup";
      return result;
   }

   public Object grader1_obviously_1_v()
   {  SurprisingnessSemantics result = new SurprisingnessSemantics(
         MagnitudeSemantics.LOWEST_POSSIBLE);
      return new Qualifier("surprisingness", result);
   }

   public Object grader1_possibly_1_v()
   {  ProbabilitySemantics result
         = new ProbabilitySemantics(ProbabilitySemantics.POSSIBLE);
      return new Qualifier("probability", result);
   }

   public Object grader1_probably_1_v()
   {  ProbabilitySemantics result
         = new ProbabilitySemantics(ProbabilitySemantics.PROBABLE);
      return new Qualifier("probability", result);
   }

   public Object grader1_quite_1_v()
   {  return new GraderSemantics(MagnitudeSemantics.HIGH);
   }

   public Object grader1_rather_1_v()
   {  GraderSemantics result
         = new GraderSemantics(MagnitudeSemantics.MODERATE);
      // or should it be MODERATELY_HIGH ?
      result.notApplicableTo = "sup";
      return result;
   }

   public Object grader1_relatively_1_v()
   {  return new GraderSemantics(MagnitudeSemantics.MODERATE);
   }

   public Object grader1_slightly_1_v()
   {  GraderSemantics result
         = new GraderSemantics(MagnitudeSemantics.VERY_LOW);
      result.notApplicableTo = "sup";
      return result;
   }

   public Object grader1_somewhat_1_v()
   {  GraderSemantics result
         = new GraderSemantics(MagnitudeSemantics.LOW);
         // or MODERATELY_LOW ?
      result.notApplicableTo = "sup";
      return result;
   }

   public Object grader1_sufficiently_1_v()
   {  ThresholdSemantics result
         = new ThresholdSemantics("sufficient");
      result.notApplicableTo = "sup";
      return result;
   }

   public Object grader1_surprisingly_1_v()
   {  SurprisingnessSemantics result
         = new SurprisingnessSemantics(MagnitudeSemantics.HIGH);
      return new Qualifier("surprisingness", result);
   }

// ----- grader2.grm semantic actions and values

   public Object grader2_$$_1_v()
   {  return get("degree");
   }

   public Object grader2_$$_2_v()
   {  return get("grader");
   }

   public Object grader2_$$_3_v()
   {  return get("grader");
   }

   public Object grader2_$$_4_v()
   { // may need revision as to how the threshold semantics
     // gets used
      DegreeSemantics degree = (DegreeSemantics) get("degree");
      DegreeSemantics graderly = (DegreeSemantics) get("graderly");
      ThresholdSemantics threshold
         = (ThresholdSemantics) get("threshold");
      DegreeSemantics result = null;
      if (degree != null)
      {  if (threshold != null) // e.g. "quite slightly enough"
         {  threshold = (ThresholdSemantics) threshold.modifyBy(
                           "degree", degree);
            result = (DegreeSemantics)graderly.modifyBy(
                           "degree", threshold);
         }
         else // e.g. "quite slightly"
            result = (DegreeSemantics)graderly.modifyBy(
                           "degree", degree);
      }
      else
      {  result = graderly;
         if (threshold != null)
            result = (DegreeSemantics) result.modifyBy(
                           "degree", threshold);
      }
      return result;
   }

   public Object grader2_$$_5_v()
   {
      DegreeSemantics degree = (DegreeSemantics) get("degree");
      Qualifier qualifier = (Qualifier) get("qualifier");
      String qualifierName = qualifier.qualifierName;
      ModifiableSemantics qualifierValue =
         (ModifiableSemantics) qualifier.qualifierValue;
      ThresholdSemantics threshold
         = (ThresholdSemantics) get("threshold");
      Qualifier result = new Qualifier(qualifierName, null);
      if (degree != null)
      {  if (threshold != null) // e.g."fairly/quite clearly enough"
         {  threshold =
               (ThresholdSemantics) threshold.modifyBy(degree);
            result.qualifierValue = (SemanticValue)
               qualifierValue.modifyBy("degree", threshold);
         }
         else // e.g. "fairly/quite clearly"
            result.qualifierValue = (SemanticValue)
               qualifierValue.modifyBy("degree", degree);
      }
      else // no pre-modifiers
      {  if (threshold != null)
            result.qualifierValue = (SemanticValue)
               qualifierValue.modifyBy("degree", threshold);
         else
            result.qualifierValue
               = (SemanticValue) qualifierValue.clone();
      }
      return result;
   }

   public Object grader2_$$_6_v()
   {  return get("negQualifier");
   }

   public Object grader2_$$_7_v()
   {  Qualifier negQualifier = (Qualifier) get("negQualifier");
      ModifiableSemantics negative = (ModifiableSemantics)
         negQualifier.qualifierValue;
      String grader = (String) get("grader");
      GraderSemantics result = null;
      if (negative instanceof NegativeSemantics)
      {  if (grader == null) // pure negative
         // shouldn't happen because of syntax network, but if it does,
         // let "not [at all]" be treated as a NEGQUALIFIER, not a GRADER
            return parser.NOADVANCE;
         // "not [so/too [really]] very":
         result = new GraderSemantics(MagnitudeSemantics.LOW);
         result.notApplicableTo = "comp";
            // *"not very more/less"
      }
      else // shouldn't happen
         return parser.NOADVANCE;
      return result;
   }

   public Object grader2_$$_8_v()
   {  NegativeSemantics result
         = (NegativeSemantics) get("negative");
      if (get("atAll") != null)
         result.extreme = true;
      return new Qualifier("negative", result);
   }

   public Object grader2_$$_9_v()
   {  Qualifier qualifier = (Qualifier) get("qualifier");
      DegreeSemantics degree = (DegreeSemantics)get("degree");
      DegreeSemantics result = (DegreeSemantics)degree.clone();
      if (qualifier != null) // came through (QUALIFIER-P 1)
      {  if (! result.isModifiable)
            // accept "clearly/surprisingly ... extremely"
            // but not "clearly/surprisingly ... quite/fairly/relatively"
            return parser.NOADVANCE;
         SemanticValue qualifierValue = (SemanticValue)
            (qualifier.qualifierValue).clone();
         if (get("atAll") != null)
         {  if (qualifierValue instanceof NegativeSemantics)
               ((NegativeSemantics)qualifierValue).extreme = true;
            else
               return parser.NOADVANCE;
         }
         String qualifierName = qualifier.qualifierName;
         return result.modifyBy(qualifierName, qualifierValue);
      }

      return result;
   }

   public Object grader2_$$_10_v()
   {  Qualifier qualifier = (Qualifier) get("qualifier");
      if (get("atAll") != null && qualifier == null)
         return parser.NOADVANCE;
      Qualifier qualifier2 = (Qualifier)get("qualifier2");
      String qualifier2Name = qualifier2.qualifierName;
      ModifiableSemantics qualifier2Value
         = (ModifiableSemantics) qualifier2.qualifierValue;
      Qualifier result
         = new Qualifier(qualifier2Name, qualifier2Value);
      if (qualifier != null)  // came through (QUALIFIER-P 1)
      {  if (! qualifier2Value.isModifiable)
            // e.g. accept "clearly not" but not *"clearly hardly"
            return parser.NOADVANCE;
         String qualifierName = qualifier.qualifierName;
         ModifiableSemantics qualifierValue =
            (ModifiableSemantics) qualifier.qualifierValue;
         qualifier2Value
            = (ModifiableSemantics) qualifier2Value.clone();
         if (get("atAll") != null)
         {  if (qualifier2Value instanceof NegativeSemantics)
               // This case is handled by another path in
               // the grader2.grm network.
               return parser.NOADVANCE;
            if (qualifierValue instanceof NegativeSemantics)
               // e.g. "not clearly at all"
               ((NegativeSemantics)qualifierValue).extreme = true;
            else
               return parser.NOADVANCE;
         }
         if (strict
             && qualifierValue instanceof NegativeSemantics
             && qualifier2Value instanceof NegativeSemantics
             ) // double negative
            return parser.NOADVANCE;
         result.qualifierValue = (SemanticValue)
            qualifier2Value.modifyBy(qualifierName, qualifierValue);
      }
      return result;
   }

   public String grader2_ATALL()
   {  set("atAll", "at all");
      return null;
   }

   public String grader2_far_1()
   {  GraderSemantics grader = (GraderSemantics) get("grader");
      if (grader == null) // first entry to node (far 1)
      {  grader = new GraderSemantics(MagnitudeSemantics.HIGH);
         grader.notApplicableTo = "abssup";
         set("grader", grader);
      }
      else // re-entry to node (far 1)
         grader.graderStrength
            = new MagnitudeSemantics(MagnitudeSemantics.VERY_HIGH);
      return null;
   }

   public Object grader2_GRADER_1_v()
   {  return nodeValue();
   }

   public String grader2_GRADER1_1()
   {  set("degree", nodeValue());
      return null;
   }

   public String grader2_GRADER_LY_1()
   {  set("graderly", nodeValue());
      return null;
   }

   public String grader2_GRADER_P_1()
   {  set("degree", nodeValue());
      return null;
   }

   public String grader2_GRADER_P_2()
   {  set("degree", nodeValue());
      return null;
   }

   public String grader2_NEGATIVE_1()
   {  set("negQualifier", nodeValue());
      return null;
   }

   public Object grader2_NEGQUALIFIER_1_v()
   {  return nodeValue();
   }

   public String grader2_not_1()
   {  set("negative", new NegativeSemantics());
      return null;
   }

   public Object grader2_QUALIFIER_1_v()
   {  return nodeValue();
   }

   public String grader2_QUALIFIER_LY_1()
   {  set("qualifier", nodeValue());
      return null;
   }

   public String grader2_QUALIFIER_P_1()
   {  set("qualifier", nodeValue());
      return null;
   }

   public String grader2_QUALIFIER1_1()
   {  set("qualifier2", nodeValue());
      return null;
   }

   public String grader2_really_1()
   {  DegreeSemantics degree = (DegreeSemantics) get("degree");
      if (degree == null)
      {  degree = new GraderSemantics(MagnitudeSemantics.HIGH);
         set("degree", degree);
      }
      else if (degree instanceof GraderSemantics) // "so really ..."
      {  GraderSemantics grader = (GraderSemantics) degree;
         if (grader.graderStrength != null
                && grader.graderStrength.value <
                   MagnitudeSemantics.EXTREMELY_HIGH)
            grader.graderStrength.value += 1;
      }
      return null;
   }

   public String grader2_so_1()
   {  GraderSemantics grader = (GraderSemantics) get("degree");
      if (grader == null)
      {  grader = new GraderSemantics(MagnitudeSemantics.HIGH);
         grader.notApplicableTo = "compsup";
         grader.consequential = true;
         set("degree", grader);
      }
      else // "so so ... "
      {  if (grader.graderStrength != null &&
                grader.graderStrength.value <
                MagnitudeSemantics.EXTREMELY_HIGH)
            grader.graderStrength.value += 1;
      }
      return null;
   }

   public String grader2_so_2()
   {  set("grader", "so");
      return null;
   }

   public String grader2_THRESH_POST_1()
   {  set("threshold", nodeValue());
      return null;
   }

   public String grader2_THRESH_POST_2()
   {  set("threshold", nodeValue());
      return null;
   }

   public String grader2_too_1()
   {  DegreeSemantics degree = (DegreeSemantics) get("degree");
      if (degree != null)
         return null; // (too 1) already visited
      ThresholdSemantics result = new ThresholdSemantics("excessive");
      result.notApplicableTo = "compsup"; // *"too more/most"
      set("degree", result);
      return null;
   }

   public String grader2_too_2()
   {  set("grader", "too");
      return null;
   }

   public String grader2_very_1()
   {  DegreeSemantics degree = (DegreeSemantics) get("degree");
      if (degree == null)
      {  degree = new GraderSemantics(MagnitudeSemantics.HIGH);
         degree.notApplicableTo = "comp";
         set("degree", degree);
      }
      else if (degree instanceof GraderSemantics)
           // "very very ... or so very ..."
      {  GraderSemantics grader = (GraderSemantics) degree;
         if (grader.graderStrength != null
                && grader.graderStrength.value <
                   MagnitudeSemantics.EXTREMELY_HIGH)
            grader.graderStrength.value += 1;
      }
      else // shouldn't happen
         return parser.NOADVANCE;
      return null;
   }

   public String grader2_very_2()
   {  set("grader", "very");
      return null;
   }

   public String grader2_way_1()
   {  if (get("grader") != null) // node (way 1) already visited
         return null;
      GraderSemantics grader =
         new GraderSemantics(MagnitudeSemantics.VERY_HIGH);
      set("grader", grader);
      return null;
   }

// ----- quant-vague.grm semantic actions and values

   public Object quant_vague_$$_1_v()
   {  QuantityVagueSemantics quantity
         = (QuantityVagueSemantics) get("quantity");
      QuantityVagueSemantics result
         = (QuantityVagueSemantics) quantity.clone();
      DegreeSemantics degree = (DegreeSemantics) get("degree");
      if (degree != null)
      {  result = (QuantityVagueSemantics) result.modifyBy(degree);
         if (result == null)
            return parser.NOADVANCE;
      }
      return result;
   }

   public Object quant_vague_$$_2_v()
   {  // or see quant_vague_lot_1_v for possible revisions
      DegreeSemantics degree = (DegreeSemantics) get("degree");
      QuantityVagueSemantics quantity =
         (QuantityVagueSemantics) get("quantity");
      QuantityVagueSemantics result =
         (QuantityVagueSemantics) quantity.clone();
      if (degree != null)
      {  if (degree instanceof GraderSemantics)
         {  MagnitudeSemantics strength
               = ((GraderSemantics) degree).graderStrength;
            if (strict && strength.value < MagnitudeSemantics.MODERATE)
               // e.g. *"somewhat whole lots"
               return parser.NOADVANCE;
         }
         return result.modifyBy("degree", degree);
      }
      return result;
   }

   public Object quant_vague_$$_3_v()
   {  int size;
      if (get("a-predecessor") != null) // "quite some"
         size = MagnitudeSemantics.FAIRLY_LARGE;
      else // "some"
         size = MagnitudeSemantics.MODERATE; // or UNSPECIFIED ?
      QuantityVagueSemantics result
         = new QuantityVagueSemantics(new MagnitudeSemantics(size));
      result.number = "plural";
      return result;
   }

   public Object quant_vague_$$_4_v()
   {  // "quite a little"
      QuantityVagueSemantics result
         = new QuantityVagueSemantics(
              new MagnitudeSemantics(MagnitudeSemantics.LARGE));
      result.discreteness = "mass";
      result.number = "singular";
      return result;
   }

   public String quant_vague_a_1()
   {  set("article", "a");
      return null;
   }

   public String quant_vague_ADJ_OR_NOUN_QUANT_1()
   {  set("quantity", nodeValue());
      return null;
   }

   public Object quant_vague_bags_1_v()
   {  QuantityVagueSemantics result
         = new QuantityVagueSemantics(
             new MagnitudeSemantics(MagnitudeSemantics.VERY_LARGE));
      result.number = "plural";
      return result;
   }

   public Object quant_vague_bit_1_v()
   {  QuantityVagueSemantics result
         = new QuantityVagueSemantics(
               new MagnitudeSemantics(MagnitudeSemantics.SMALL));
      DegreeSemantics degree = (DegreeSemantics) get("degree");
      if (degree != null)
      {  result = (QuantityVagueSemantics) result.modifyBy(degree);
         if (result == null)
            return parser.NOADVANCE;
      }
      // Can be singular or plural, discrete(?) or mass:
      // "a bit more money", ?"a bit more people"
      if (strict)
         result.discreteness = "mass";
      return result;
   }

   public Object quant_vague_bit_2_v()
   {  // "quite a [little] bit"
      QuantityVagueSemantics result
         = new QuantityVagueSemantics(
              new MagnitudeSemantics(MagnitudeSemantics.LARGE));
      result.discreteness = "mass";
      result.number = "singular";
      return result;
   }

   public Object quant_vague_couple_1_v()
   {  QuantityCardinalSemantics result
         = new QuantityCardinalSemantics(2);
      result.number = "plural";
      result.discreteness = "discrete";
      return result;
   }

   public Object quant_vague_enough_1_v()
   {  DegreeSemantics degree = (DegreeSemantics) get("degree");
      ThresholdSemantics threshold
         = new ThresholdSemantics("sufficient");
      if (degree != null)
         threshold = (ThresholdSemantics) threshold.modifyBy(
            "degree", degree);
      QuantityThresholdSemantics result
         = new QuantityThresholdSemantics(threshold);
      return result;
   }

   public Object quant_vague_few_1_v()
   {  QuantityVagueSemantics result
         = new QuantityVagueSemantics(
              new MagnitudeSemantics(MagnitudeSemantics.SMALL));
      result.discreteness = "discrete";
      result.number = "plural";
      return result;
   }

   public Object quant_vague_few_2_v()
   {  int size = MagnitudeSemantics.MODERATE;
      if (get("a-predecessor") != null)  // "quite some few"
         size = MagnitudeSemantics.LARGE;
      QuantityVagueSemantics result
         = new QuantityVagueSemantics(new MagnitudeSemantics(size));
      result.discreteness = "discrete";
      result.number = "plural";
      return result;
   }

   public Object quant_vague_few_3_v()
   {  // "quite a few"
      QuantityVagueSemantics result
         = new QuantityVagueSemantics(
              new MagnitudeSemantics(MagnitudeSemantics.LARGE));
      result.discreteness = "discrete";
      result.number = "plural";
      return result;
   }

   public String quant_vague_GRADER_P_1()
   {  DegreeSemantics degree = (DegreeSemantics) nodeValue();
      if (degree.notApplicableTo.indexOf("abs") >= 0)
         // not applicable to absolutes; e.g. "far"
         return parser.NOADVANCE;
      set("degree", degree);
      return null;
   }

   public Object quant_vague_heaps_1_v()
   {  QuantityVagueSemantics result
         = new QuantityVagueSemantics(
             new MagnitudeSemantics(MagnitudeSemantics.VERY_LARGE));
      result.number = "plural";
      return result;
   }

   public Object quant_vague_little_1_v()
   {  QuantityVagueSemantics result
         = new QuantityVagueSemantics(
              new MagnitudeSemantics(MagnitudeSemantics.SMALL));
      result.discreteness = "mass";
      result.number = "singular";
      return result;
   }

   public Object quant_vague_little_3_v()
   {  int size = MagnitudeSemantics.MODERATE;
      if (get("a-predecessor") != null)  // "quite some little"
         size = MagnitudeSemantics.LARGE;
      QuantityVagueSemantics result
         = new QuantityVagueSemantics(new MagnitudeSemantics(size));
      result.discreteness = "mass";
      result.number = "singular";
      return result;
   }

   public Object quant_vague_lot_1_v()
   {  QuantityVagueSemantics result
         = new QuantityVagueSemantics(
               new MagnitudeSemantics(MagnitudeSemantics.LARGE));
      DegreeSemantics degree = (DegreeSemantics) get("degree");
      if (degree != null)
      {  if (degree instanceof GraderSemantics)
         {  MagnitudeSemantics strength =
               ((GraderSemantics) degree).graderStrength;
            if (degree.firstModifier() != null)
               return result.modifyBy("degree", degree);
            if (strength.value < MagnitudeSemantics.MODERATE)
               // e.g. *"slightly/somewhat whole lot"
               return parser.NOADVANCE;
            else if (strict &&
                     strength.value < MagnitudeSemantics.HIGH)
               // e.g. ?"rather whole lot"
               return parser.NOADVANCE;
            else
               result.magnitude = new MagnitudeSemantics(strength.value);
         }
         else
            result = (QuantityVagueSemantics)result.modifyBy(
                        "degree", degree);
         if (result == null)
            return parser.NOADVANCE;
         return result;
      }
      return result;
   }

   public Object quant_vague_lots_1_v()
   {  QuantityVagueSemantics result
         = new QuantityVagueSemantics(
             new MagnitudeSemantics(MagnitudeSemantics.LARGE));
      result.number = "plural";
      return result;
   }

   public Object quant_vague_many_1_v()
   {  QuantityVagueSemantics result
         = new QuantityVagueSemantics(
              new MagnitudeSemantics(MagnitudeSemantics.LARGE));
      result.discreteness = "discrete";
      result.number = "plural";
      return result;
   }

   public Object quant_vague_much_1_v()
   {  QuantityVagueSemantics result
         = new QuantityVagueSemantics(
              new MagnitudeSemantics(MagnitudeSemantics.LARGE));
      result.discreteness = "mass";
      result.number = "singular";
      return result;
   }

   public String quant_vague_NEGQUALIFIER_1()
   {  set("negative", nodeValue());
      return null;
   }

   public Object quant_vague_piles_1_v()
   {  QuantityVagueSemantics result
         = new QuantityVagueSemantics(
             new MagnitudeSemantics(MagnitudeSemantics.VERY_LARGE));
      result.number = "plural";
      return result;
   }

   public Object quant_vague_QUANT_VAGUE_1_v()
   {  return nodeValue();
   }

   public String quant_vague_QUANT_VAGUE_2()
   {  set("quantity", nodeValue());
      return null;
   }

   public Object quant_vague_QUANT_VAGUE_N_1_v()
   {  return nodeValue();
   }

   public String quant_vague_QUANT_VAGUE_N_LARGE_1()
   {  set("quantity", nodeValue());
      return null;
   }

   public Object quant_vague_QUANTITY_NP_1_v()
   {  QuantitySemantics quantity = (QuantitySemantics) nodeValue();
      if (! (quantity instanceof QuantityVagueSemantics
             || quantity instanceof QuantityThresholdSemantics))
         return parser.NOADVANCE;
      if ("plural".equals(quantity.number) && get("article") != null)
         // e.g. *"[quite] a lots/piles"
         return parser.NOADVANCE;
      String aPredecessor = (String) get("a_predecessor");
      if ("quite".equals(aPredecessor))
      {  MagnitudeSemantics size
            = ((QuantityVagueSemantics) quantity).magnitude;
         if (size.value <= MagnitudeSemantics.MODERATE)
            // e.g. "quite a [bit/few/little]" -- handled elsewhere
            return parser.NOADVANCE;
      }
      QuantitySemantics result = (QuantityVagueSemantics) quantity.clone();
      GraderSemantics grader = (GraderSemantics) get("grader");
      if (grader != null)
         result = (QuantitySemantics) result.modifyBy("degree", grader);
      return result;
   }

   public Object quant_vague_QUANTITY_P_1_v()
   {  QuantitySemantics quantity = (QuantitySemantics) nodeValue();
      if (! (quantity instanceof QuantityVagueSemantics
             ||quantity instanceof QuantityThresholdSemantics))
         return parser.NOADVANCE;
      String aPredecessor = (String) get("a_predecessor");
      if ("quite".equals(aPredecessor))
      {  if (quantity instanceof QuantityVagueSemantics)
         {  MagnitudeSemantics size
               = ((QuantityVagueSemantics) quantity).magnitude;
            if (size.value <= MagnitudeSemantics.MODERATE)
               // e.g. "quite a [bit/few/little]" -- handled elsewhere
               return parser.NOADVANCE;
         }
      }
      QuantitySemantics result = (QuantitySemantics) quantity.clone();
      GraderSemantics grader = (GraderSemantics) get("grader");
      if (grader != null)
         result = (QuantitySemantics) result.modifyBy("degree", grader);
      return result;
   }

   public String quant_vague_quite_2()
   {  set("a_predecessor", "quite");
      set("grader",
         new GraderSemantics(MagnitudeSemantics.HIGH));
      return null;
   }

   public String quant_vague_rather_1()
   {  set("grader", new GraderSemantics(MagnitudeSemantics.MODERATE));
      return null;
   }

   public Object quant_vague_several_1_v()
   {  QuantityVagueSemantics result
         = new QuantityVagueSemantics(
              new MagnitudeSemantics(MagnitudeSemantics.FAIRLY_SMALL));
      result.number = "plural";
      result.discreteness = "discrete";
      return result;
   }

   public String quant_vague_such_1()
   {  GraderSemantics grader
         = new GraderSemantics(MagnitudeSemantics.HIGH);
      grader.notApplicableTo = "compsup";
      grader.consequential = true;
      set("grader", grader);
      return null;
   }

   public Object quant_vague_THRESH_POST_1_v()
   {  if (get("degree") != null)
         // e.g. *"very few/little/many/much enough"
         return parser.NOADVANCE;
      QuantityVagueSemantics quantity
         = (QuantityVagueSemantics) get("quantity");
      if ("mass".equals(quantity.discreteness) &&
          quantity.magnitude.value >= MagnitudeSemantics.FAIRLY_LARGE)
         // *"much enough"
         return parser.NOADVANCE;
      // e.g. "few/little/many enough":
      ThresholdSemantics threshold = (ThresholdSemantics) nodeValue();
      return quantity.modifyBy(threshold);
   }

   public Object quant_vague_tons_1_v()
   {  // quantity may actually be a QuantityThresholdSemantics
      // (see (enough 1)) instead of a QuantityVagueSemantics
      QuantitySemantics quantity
         = (QuantitySemantics) get("quantity");
      QuantityVagueSemantics result
         = new QuantityVagueSemantics(
               new MagnitudeSemantics(MagnitudeSemantics.VERY_LARGE));
      result.number = "plural";
      if (quantity != null)
      {  // needs elaboration
      }
      return result;
   }

   public String quant_vague_what_1()
   {  GraderSemantics grader
         = new GraderSemantics(MagnitudeSemantics.HIGH);
      grader.notApplicableTo = "compsup";
      grader.exclamatory = true;
      set("grader", grader);
      return null;
   }

// ----- quantity-np.grm semantic actions and values

   public Object quantity_np_QUANT_VAGUE_NP_1_v()
   {  return nodeValue();
   }


// ----- quantity-p.grm semantic actions and values

   public Object quantity_p_CARD_P_1_v()
   {  QuantitySemantics quantity = (QuantitySemantics) nodeValue();
      return quantity.clone();
   }

   public String quantity_p_QUALIFIER_P_1()
   {  set("qualifier", nodeValue());
      return null;
   }

   public Object quantity_p_QUANT_VAGUE_P_1_v()
   {  QuantitySemantics quantity = (QuantitySemantics) nodeValue();
      return quantity.clone();
   }

   public Object quantity_p_QUANTITY_1_v()
   {  QuantitySemantics quantity = (QuantitySemantics) nodeValue();
      return quantity.clone();
   }

   public Object quantity_p_QUANTITY_P_1_v()
   {  Qualifier qualifier = (Qualifier) get("qualifier");
      QuantitySemantics quantity = (QuantitySemantics) nodeValue();
      if (qualifier != null)
      {  quantity = (QuantitySemantics) quantity.modifyBy(
            qualifier.qualifierName, qualifier.qualifierValue);
         if (quantity == null)
            return parser.NOADVANCE;
         return quantity;
      }
      return quantity.clone();
   }

  private Object application;
   private ASDParser parser;
   private ASDGrammar grammar;
   //private static String GRAMMARNAME = "http://home.asd-networks.com/prj_asd/englishdemoproject/npXdemo.grm";
   private static String GRAMMARNAME = "npXdemo.grm";
   
   private ArrayList expectedTypes = null;
   static final ArrayList EXPECTEDTYPES = new ArrayList(3);
   static final int MAXSTEPS = 40000;
   private boolean parseCompleted = false;
   private int steps;
   private String utterance;
   private String originalUtterance;
   private String resultMessage = "";
   private static boolean strict = true; // indicates whether or not
      // parse should follow grammar rules strictly
   static
   {  EXPECTEDTYPES.add("QUANTITY-P");
      EXPECTEDTYPES.add("QUANTITY-NP");
      EXPECTEDTYPES.add("ORDIR-P");
   }

} // end class NpXDemoSemantics
