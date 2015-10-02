package englishdemo;
import semanticvalues.*;
//import semanticvalues.*;

/**
   Instances represent quantities expressed by cardinal numbers
   or their English language equivalents.  They represent
   discrete quantities by default.
   @author James A. Mason
   @version 1.01 2005 Mar.
 */
public class QuantityCardinalSemantics extends QuantitySemantics
{
   /**
      Creates an instance whose cardinal value is to be specified
      later.
    */
   public QuantityCardinalSemantics()
   {  discreteness = "discrete";
   }

   /**
      Creates an instance with a specified cardinal value.
    */
   public QuantityCardinalSemantics(long value)
   {  cardinal = value;
      spelledNumeral = true;
      discreteness = "discrete";
      if (cardinal == 1)
         number = "singular";
      else
         number = "plural";
   }

   /**
      Creates an instance with a specified cardinal value.
    */
   public QuantityCardinalSemantics(Long value)
   {  cardinal = value.longValue();
      spelledNumeral = true;
      discreteness = "discrete";
      if (cardinal == 1)
         number = "singular";
      else
         number = "plural";
   }

   /**
      the numerical value of the cardinal
    */
   public long cardinal;

   /**
      used by syntax to avoid conjoining two spelled numerals
      with "and", if they could instead form a single spelled
      numeral with the same value as the sum of the two
      numerals (e.g. "two hundred and twenty")
    */
   public boolean spelledNumeral = false;
}  // end class QuantityCardinalSemantics
