package englishdemo;
//import semanticvalues.*;

/**
   Instances are semantic values that can be used as modifiers
   to express the grade (degree or extent) of another semantic value
   such as a vague quantity (QuantityVagueSemantics) which is
   described by a magnitude (MagnitudeSemantics).
   Examples of English words whose meanings can be represented by
   instances of GraderSemantics are "slightly", "somewhat", "rather"
   "quite", "very", "extremely", "so", and "how".  Some, such as
   "so" and "how" also have other components (consequential,
   exclamatory, or interrogative) to their meanings.
   @author James A. Mason
   @version 1.01 2005 Mar.
 */
public class GraderSemantics extends DegreeSemantics
{
   /**
      Creates an instance with specified strength
      @param value the strength of the grader
    */
   public GraderSemantics(MagnitudeSemantics value)
   {  graderStrength = value;
   }

   /**
      Creates an instance with specified strength
      @param value the strength of the grader
    */
   public GraderSemantics(int value)
   {  graderStrength = new MagnitudeSemantics(value);
   }

   /**
      expresses the strength or magnitude of the grader
    */
   public MagnitudeSemantics graderStrength;
   /**
      true for English "so" or "such", which may require "that ...";
      false by default
    */
   public boolean consequential = false;
   /**
      true for English exclamatory "so" or "such"; false by default
    */
   public boolean exclamatory = false;
   /**
      true for English interrogative "how ... ?"; false by default
    */
   public boolean interrogative = false;
} // end class GraderSemantics

