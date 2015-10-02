package englishdemo;
//import semanticvalues.*;
import semanticvalues.*;
//import semanticvalues.SemanticValue;


/**
   Instances represent quantities which are described vaguely
   rather than numerically, but which can be represented by
   values on a MagnitudeSemantics scale.
   Examples are the meanings of "fairly few", "several",
   "some", and "very many".
   @author James A. Mason
   @version 1.01 2005 Mar.
 */
public class QuantityVagueSemantics extends QuantitySemantics
{
   /**
      Creates an instance which represents a quantity described by
      a given instance of MagnitudeSemantics.
    */
   public QuantityVagueSemantics(MagnitudeSemantics givenSize)
   {  magnitude = givenSize;
   }

   /**
      Modifies the receiver by a given instance of a subclass of
      DegreeSemantics.  The result is a new instance of
      QuantityVagueSemantics.  Examples: The meanings of "very many",
      "fairly many", "quite few", etc.
    */
   public SemanticValue modifyBy(DegreeSemantics givenDegree)
   {  // This may need further refinement.
      if (givenDegree instanceof ThresholdSemantics)
      {  // e.g. "too few/many"
         MagnitudeSemantics newSize
            = (MagnitudeSemantics) magnitude.modifyBy(
                 "degree", givenDegree);
         QuantityVagueSemantics result
            = (QuantityVagueSemantics) this.clone();
         result.magnitude = newSize;
         return result;
      }

      if ((! (givenDegree instanceof GraderSemantics)) ||
          givenDegree.firstModifier() != null)
         return super.modifyBy("degree", givenDegree);
      QuantityVagueSemantics result
         = (QuantityVagueSemantics) this.clone();
      GraderSemantics grader = (GraderSemantics) givenDegree;
      int graderStrength = grader.graderStrength.value;
      if (graderStrength == MagnitudeSemantics.INDEFINITE)
         return super.modifyBy("degree", givenDegree);
      if (magnitude.value == MagnitudeSemantics.LARGE) // ... many/much
      {  if (graderStrength == MagnitudeSemantics.HIGH)
            result.magnitude // "very many/much"
               = new MagnitudeSemantics(MagnitudeSemantics.VERY_LARGE);
         else
            result.magnitude = new MagnitudeSemantics(graderStrength);
      }
      else if (magnitude.value == MagnitudeSemantics.SMALL)
      // ... few/little
      {  if (graderStrength == MagnitudeSemantics.HIGH)
            result.magnitude // "very few/little"
               = new MagnitudeSemantics(MagnitudeSemantics.VERY_SMALL);
         else if (graderStrength > MagnitudeSemantics.HIGH)
            result.magnitude
               = new MagnitudeSemantics(10-graderStrength);
         else if (graderStrength >= MagnitudeSemantics.MODERATE)
            result.magnitude
               = new MagnitudeSemantics(9 - graderStrength);
         else // e.g. ? "slightly few"
            // or maybe this should return null;
            return super.modifyBy("degree", givenDegree);
      }
      else
         return super.modifyBy("degree", givenDegree);
      return result;
   }

   /**
      a representation of the quantity on an ordinal scale
    */
   public MagnitudeSemantics magnitude;
}
