package englishdemo;

// How should surprisingness relate to probability semantics?
import semanticvalues.*;
//import semanticvalues.ModifiableSemantics;
//import semanticvalues.SemanticValue;

// Are they essentially inverses of each other, or is the relationship
// for subtle than that?
// Examples of their independence are: "obviously possibly more"
// "probably surprisingly many"

public class SurprisingnessSemantics extends ModifiableSemantics
{  public SurprisingnessSemantics(MagnitudeSemantics value)
   {  surprisingness = value;
   }

   public SurprisingnessSemantics(int strengthValue)
   {  surprisingness = new MagnitudeSemantics(strengthValue);
   }

   public SemanticValue modifyBy(String modifierName,
         SemanticValue modifierValue)
   {  if (modifierValue instanceof NegativeSemantics)
         return modifyBy((NegativeSemantics) modifierValue);
      else
         return super.modifyBy(modifierName, modifierValue);
   }

   public SurprisingnessSemantics modifyBy(NegativeSemantics negative)
   {  SurprisingnessSemantics result = (SurprisingnessSemantics) clone();
      if (negative.negative)
         result = new SurprisingnessSemantics(
                     MagnitudeSemantics.HIGHEST_POSSIBLE -
                     surprisingness.value);
      return result;
   }

   public MagnitudeSemantics surprisingness;
      // values represent "extremely surprisingly" at the high end
      // to "extremely obviously" at the low end
} // end class SurprisingnessSemantics

