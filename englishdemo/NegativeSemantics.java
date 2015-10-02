package englishdemo;


import semanticvalues.SemanticValue;
import semanticvalues.ModifiableSemantics;

public class NegativeSemantics extends ModifiableSemantics
{
   public SemanticValue modifyBy(SemanticValue modifier)
   {  if (modifier instanceof ProbabilitySemantics)
      {  ModifiableSemantics result
            = modifyBy((ProbabilitySemantics) modifier);
         if (result == null)
         {  // unsuccessful conversion to ProbabilitySemantics
            result = (NegativeSemantics) clone();
            result = (ModifiableSemantics)
               result.modifyBy("probability", modifier);
         }
         return result;
      }
      return super.modifyBy(modifier);
   }

   public ProbabilitySemantics modifyBy(ProbabilitySemantics prob)
   {  // Normally prob will be from the ProbabilitySemantics component
      // of the qualifier instance variable below.
      ProbabilitySemantics result = null;
      int judgment = prob.judgment.value;
      int newJudgment = ProbabilitySemantics.INDEFINITE;
      if (judgment == ProbabilitySemantics.PROBABLE) // "probably not"
         newJudgment = ProbabilitySemantics.UNLIKELY;
      else if (judgment == ProbabilitySemantics.FAIRLY_PROBABLE)
         newJudgment = ProbabilitySemantics.UNLIKELY;
      else if (judgment == ProbabilitySemantics.VERY_PROBABLE)
         // "quite/very probably not"
         newJudgment = ProbabilitySemantics.VERY_UNLIKELY;
      else if (judgment == ProbabilitySemantics.ALMOST_CERTAIN)
         // "almost certainly not"
         newJudgment = ProbabilitySemantics.EXTREMELY_UNLIKELY;
      else if (judgment == ProbabilitySemantics.CERTAIN)
         // "certainly/definitely not"
         newJudgment = ProbabilitySemantics.IMPOSSIBLE;
      if (newJudgment != ProbabilitySemantics.INDEFINITE)
         result = new ProbabilitySemantics(newJudgment);
      return result;
         // null indicates unsuccessful attempt to convert to
         // ProbabilitySemantics.
   }

   public boolean negative = true;
   public boolean extreme = false;  // true for "not at all"

} // end class NegativeSemantics
