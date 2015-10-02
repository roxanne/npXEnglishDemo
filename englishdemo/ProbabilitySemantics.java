package englishdemo;
import semanticvalues.*;
//import semanticvalues.ModifiableSemantics;
//import semanticvalues.ModifierSemantics;
//import semanticvalues.SemanticValue;

public class ProbabilitySemantics extends ModifiableSemantics
{  public ProbabilitySemantics(int value)
   {  judgment = new MagnitudeSemantics(value);
   }

/* // Does this make sense??
   public int compareTo(ProbabilitySemantics other)
   {  if (negative == null && judgment >= 0)
         return judgment - other.judgment;
      else // incomparable
         return -100;
   }
 */

    @Override
   public boolean equals(Object otherObject)
   {  if (otherObject instanceof ProbabilitySemantics)
         return judgment.value ==
            ((ProbabilitySemantics) otherObject).judgment.value;
      else
         return false;
   }

   public SemanticValue modifyBy(String modifierName,
         SemanticValue modifier)
   {  ModifiableSemantics result = null;
      if (modifier instanceof GraderSemantics
          && ((GraderSemantics)modifier).graderStrength != null)
      {  result = modifyByStrength((MagnitudeSemantics)
                    ((GraderSemantics)modifier).graderStrength);
         if (result != null)
            // successful conversion to ProbabilitySemantics
         return result;
      }
// ? should have an else if (
//                  modifier instanceof QuantitySuperlativeSemantics)
// here to handle cases like "least probably"
// = "most [superlative] unlikely"
      else if (modifier instanceof NegativeSemantics)
      {  result = this.negative((NegativeSemantics) modifier);
         if (result != null)
            return result;
      }
      return super.modifyBy(modifierName, modifier);
   }

   /*
      Modifying by strength of a GraderSemantics instance
    */
   public ProbabilitySemantics modifyByStrength(MagnitudeSemantics strength)
   {
      int strengthValue = strength.value;
      if (strengthValue < MagnitudeSemantics.LOWEST_POSSIBLE ||
          strengthValue >= MagnitudeSemantics.HIGHEST_POSSIBLE)
         return null;
      ProbabilitySemantics result = null;
      int oldJudgment = judgment.value;
      int newJudgment;
      if (oldJudgment == POSSIBLE)
      {  newJudgment = gradedPossibleJudgment[strengthValue];
         if (newJudgment != INDEFINITE)
            result = new ProbabilitySemantics(newJudgment);
      }
      else if (oldJudgment == PROBABLE)
      {  newJudgment = gradedProbableJudgment[strengthValue];
         if (newJudgment != INDEFINITE)
            result = new ProbabilitySemantics(newJudgment);
      }
      else if (oldJudgment >= VERY_PROBABLE
               && strengthValue == MagnitudeSemantics.MODERATE)
         // e.g. "fairly clearly/certainly"
         result = new ProbabilitySemantics(oldJudgment - 2);

      return result;
   }

   public ProbabilitySemantics negative(NegativeSemantics modifier)
   {  // "not ... "
      ProbabilitySemantics result
         = new ProbabilitySemantics(judgment.value);
      ModifierSemantics mod = firstModifier();
      if (mod != null && mod.modifierName.equals("negative"))
         // double negative
         result.modifiers = modifiers.otherModifiers;
      else // true negative
      {  if (negativeJudgment[judgment.value] == INDEFINITE)
            result = null;  // no simplification; use default
         else
            result.judgment =
               new MagnitudeSemantics(negativeJudgment[judgment.value+1]);
      }
      return result;
   }
/*
   public String toString(String indentString)
   {  if (indentString.length() == 0)
         indentString = "   ";
      return "\nenglish.ProbabilitySemantics:\n" + indentString
             + "judgment = " +
             judgmentName[judgment+1];
   }
 */
   public MagnitudeSemantics judgment;
//   public GraderSemantics grade;


   // alternative names for strengths declared in class MagnitudeSemantics:
   public static final int INDEFINITE = -1;
   public static final int IMPOSSIBLE = 0;  // = "not possible"
   public static final int EXTREMELY_UNLIKELY = 1;
      // = "almost certainly not", "hardly/scarcely possibly"
   public static final int VERY_UNLIKELY = 2;
      // = "most probably not", "barely possibly"
   public static final int UNLIKELY = 3;
      // = "probably not", "just possibly"
   public static final int POSSIBLE = 4;  // = "possibly not"
   public static final int VERY_POSSIBLE = 5;
      // = "quite/very possibly", "almost probably"
   public static final int FAIRLY_PROBABLE = 6;
      // = "fairly/rather probably"
   public static final int PROBABLE = 7;
      // = "probably"
   public static final int VERY_PROBABLE = 8;
      // "most/quite/very probably"
   public static final int ALMOST_CERTAIN = 9;
      // = "almost certainly", "hardly/scarcely possibly not"
   public static final int CERTAIN = 10;
      // = "[most] certainly/definitely", "for sure"
/*
   private static final String[] judgmentName = {"UNCERTAIN", "IMPOSSIBLE",
      "EXTREMELY UNLIKELY", "VERY UNLIKELY", "UNLIKELY", "POSSIBLE",
      "VERY POSSIBLE", "FAIRLY PROBABLE", "PROBABLE", "VERY PROBABLE",
      "ALMOST CERTAIN", "CERTAIN"};
 */
   private static final int[] negativeJudgment = {
      INDEFINITE,  // not INDEFINITE
      POSSIBLE,    // not IMPOSSIBLE
      POSSIBLE,    // not EXTREMELY_UNLIKELY
      POSSIBLE,    // not VERY_UNLIKELY
      VERY_POSSIBLE,    // not UNLIKELY
      IMPOSSIBLE,  // not POSSIBLE
      INDEFINITE,  // not VERY_POSSIBLE
      INDEFINITE,  // not FAIRLY_PROBABLE
      UNLIKELY,    // not PROBABLE
      INDEFINITE,  // not VERY_PROBABLE
      INDEFINITE,  // not ALMOST_CERTAIN
      POSSIBLE     // not CERTAIN
      };
   private static final int[] gradedPossibleJudgment = {
      IMPOSSIBLE,         // not POSSIBLE
      EXTREMELY_UNLIKELY, // hardly/scarcely POSSIBLE
      VERY_UNLIKELY,      // slightly/barely POSSIBLE
      UNLIKELY,           // not very POSSIBLE
      POSSIBLE,           // somewhat POSSIBLE
      POSSIBLE,           // fairly/moderately POSSIBLE
      POSSIBLE,           // rather POSSIBLE
      VERY_POSSIBLE,      // most/quite POSSIBLE
      VERY_POSSIBLE,      // very POSSIBLE
      VERY_POSSIBLE       // extremely POSSIBLE
      };
   private static final int[] gradedProbableJudgment = {
      UNLIKELY,           // not PROBABLE
      UNLIKELY,           // hardly/scarcely PROBABLE
      INDEFINITE,         // slightly/barely PROBABLE
      POSSIBLE,           // not very PROBABLE
      VERY_POSSIBLE,      // somewhat PROBABLE
      FAIRLY_PROBABLE,    // fairly/moderately PROBABLE
      PROBABLE,           // rather PROBABLE
      VERY_PROBABLE,      // most/quite PROBABLE
      VERY_PROBABLE,      // very PROBABLE
      ALMOST_CERTAIN      // extremely PROBABLE
      };
}  // end class ProbabilitySemantics