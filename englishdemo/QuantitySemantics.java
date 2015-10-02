package englishdemo;
import semanticvalues.*;
//import semanticvalues.ModifiableSemantics;

public abstract class QuantitySemantics extends ModifiableSemantics
{
   public String discreteness = null;  // e.g. for "more"
      // or "discrete" ("fewer [than ...]",
      //    "as few [as ...]")
      // or "mass" ("as much/little [as ...]")
   public String number = null; // or "singular" or "plural"
      // null means unspecified -- e.g. for "some"
}