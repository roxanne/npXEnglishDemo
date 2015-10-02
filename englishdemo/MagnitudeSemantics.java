package englishdemo;
import semanticvalues.ModifiableSemantics;
import semanticvalues.*;

public class MagnitudeSemantics extends ModifiableSemantics
{  public MagnitudeSemantics()
   {  value = UNSPECIFIED;
//      isDegreeable = true;
   }

   public MagnitudeSemantics(int givenValue)
   {  value = givenValue;
   }

   public int compareTo(MagnitudeSemantics other)
   {  return value - other.value;
   }

   public boolean equals(Object otherObject)
   {  Class thisClass;
      try
      {  thisClass
            = Class.forName("englishdemo.MagnitudeSemantics");
      }
      catch (ClassNotFoundException e) {return false;}
      if (thisClass.isAssignableFrom(otherObject.getClass()))
         return value == ((MagnitudeSemantics) otherObject).value;
      else
         return false;
   }

   public SemanticValue modifyBy(SemanticValue modifier, boolean simplify)
   {  if (modifier instanceof ThresholdSemantics)
         return (MagnitudeSemantics) modifyBy("degree", modifier);
      if (modifier instanceof GraderSemantics)
      {  if (simplify)
            return modifyBy((GraderSemantics) modifier);
         else
            return modifyBy("degree", modifier);
      }
      else if (modifier instanceof NegativeSemantics)
      {  MagnitudeSemantics result = null;
         if (simplify)
            result =
               (MagnitudeSemantics) modifyBy((NegativeSemantics) modifier);
         if (result == null)
            result = (MagnitudeSemantics) modifyBy("negative", modifier);
         return result;
      }
      else
         return (MagnitudeSemantics) super.modifyBy(modifier);
   }

   public MagnitudeSemantics modifyBy(GraderSemantics grader)
   {  MagnitudeSemantics result = (MagnitudeSemantics) this.clone();
      NegativeSemantics graderNegative = (NegativeSemantics)
         grader.getValueOfModifierNamed("negative");
      if (graderNegative != null)
      {  result
            = (MagnitudeSemantics) result.modifyBy("degree", grader);
         return result;
      }
      MagnitudeSemantics newResult
         = result.modifyByStrength(grader.graderStrength);
      if (newResult != null)
         result = newResult;
      else
         result
            = (MagnitudeSemantics) result.modifyBy("degree", grader);
      return result;
   }

   public MagnitudeSemantics modifyBy(NegativeSemantics modifier)
   {  DegreeSemantics degree =
         (DegreeSemantics) getValueOfModifierNamed("degree");
      if (degree instanceof GraderSemantics)
         // explicit grade; negation is anomalous
         return null; // e.g. "not (somewhat huge)"
/*
      if (modifier.probability != null || modifier.emotion != null
          || modifier.surprisingness != null
          || modifier.approximation != null)
 */
      if (modifier.modifiers != null)
         // don't try to simplify value if "not" is qualified
         return (MagnitudeSemantics)
                   super.modifyBy((SemanticValue) modifier);
      MagnitudeSemantics result = (MagnitudeSemantics) this.clone();
      if (modifier.extreme) // "not at all"
      {  if (value >= FAIRLY_LARGE && value < LARGEST_POSSIBLE)
            result.value = value - 3;
         else if (value > SMALLEST_POSSIBLE && value <= FAIRLY_SMALL)
            result.value = value + 3;
         else
            result = (MagnitudeSemantics) super.modifyBy(
                        (SemanticValue) modifier);
      }
      else // simple negative
      {  if (value >= FAIRLY_LARGE && value < LARGEST_POSSIBLE)
            result.value = value - 2;
         else if (value > SMALLEST_POSSIBLE && value <= FAIRLY_SMALL)
            result.value = value + 2;
         else
            result = (MagnitudeSemantics) super.modifyBy(
                        (SemanticValue) modifier);
      }
      return result;
   }

   public MagnitudeSemantics modifyByStrength(MagnitudeSemantics strength)
   {  if (strength == null)
         return null;
      int strengthValue = strength.value;

      if (strengthValue == MagnitudeSemantics.INDEFINITE)
         return null;
      if (value >= FAIRLY_SMALL && value <= FAIRLY_LARGE)
         return null;  // not modifiable by strength (?)

      MagnitudeSemantics result = this;

      if (value == SMALLEST_POSSIBLE || value == LARGEST_POSSIBLE)
      {  if (strengthValue <= MagnitudeSemantics.MODERATELY_HIGH)
            // e.g. *"fairly/rather largest/smallest"
            return null;
      }

      else if (value < FAIRLY_SMALL && value >= TINY)
      {  if (strengthValue >= MagnitudeSemantics.VERY_HIGH)
            value -= 2;
         else if (strengthValue >= MagnitudeSemantics.MODERATELY_HIGH)
            value -= 1;
         else if (value == SMALL)
         {  if (strengthValue <= MagnitudeSemantics.VERY_LOW)
               value += 2;
            else if (strengthValue <= MagnitudeSemantics.MODERATE)
               value += 1;
         }
         else if (strengthValue <= MagnitudeSemantics.MODERATE)
            return null; // e.g. "slightly/somewhat tiny"
         if (value < TINY)
            value = TINY;
      }
      else if (value > FAIRLY_LARGE && value <= HUGE)
      {  if (strengthValue >= MagnitudeSemantics.VERY_HIGH)
            value += 2;
         else if (strengthValue >= MagnitudeSemantics.MODERATELY_HIGH)
            value += 1;
         else if (value == LARGE)
         {  if (strengthValue <= MagnitudeSemantics.VERY_LOW)
               value -= 2;
            else if (strengthValue <= MagnitudeSemantics.MODERATE)
               value -= 1;
         }
         else if (strengthValue <= MagnitudeSemantics.MODERATE)
            return null; // e.g. "somewhat huge"

         if (value > HUGE)
            value = HUGE;
      }
      return result;
   }

/* Must use inherited toString instead, in order to display modifiers:
   public String toString(String indentString)
   {  String result = // "\n" + indentString +
          "englishdemo.MagnitudeSemantics:\n";
//      indentString += "   ";

      result += indentString + "value = " +
                valueName[value+1];
      return result;
   }
*/

   public int value;

   public static final int UNSPECIFIED = -1;
   public static final int SMALLEST_POSSIBLE = 0;
   public static final int TINY = 1;
   public static final int VERY_SMALL = 2;
   public static final int SMALL = 3;
   public static final int FAIRLY_SMALL = 4;
   public static final int MODERATE = 5;
   public static final int FAIRLY_LARGE = 6;
   public static final int LARGE = 7;
   public static final int VERY_LARGE = 8;
   public static final int HUGE = 9;
   public static final int LARGEST_POSSIBLE = 10;

   public static final int INDEFINITE = -1;
   public static final int LOWEST_POSSIBLE = 0;
   public static final int EXTREMELY_LOW = 1;
   public static final int VERY_LOW = 2;
   public static final int LOW = 3;
   public static final int MODERATELY_LOW = 4;

   public static final int MODERATELY_HIGH = 6;
   public static final int HIGH = 7;
   public static final int VERY_HIGH = 8;
   public static final int EXTREMELY_HIGH = 9;
   public static final int HIGHEST_POSSIBLE = 10;


   private static final String[] valueSizeName = {"UNSPECIFIED",
      "SMALLEST POSSIBLE", "TINY", "VERY SMALL", "SMALL",
      "FAIRLY SMALL", "MODERATE", "FAIRLY LARGE", "LARGE",
      "VERY LARGE", "HUGE", "LARGEST POSSIBLE"};

   private static final String[] valueStrengthName =
      {"INDEFINITE", "LOWEST_POSSIBLE", "EXTREMELY LOW", "VERY LOW",
       "LOW", "MODERATELY LOW", "MODERATE", "MODERATELY HIGH", "HIGH",
       "VERY HIGH", "EXTREMELY HIGH", "HIGHEST_POSSIBLE"};

}  // end class MagnitudeSemantics