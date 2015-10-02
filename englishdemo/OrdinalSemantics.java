package englishdemo;
import semanticvalues.*;
//import semanticvalues.ModifiableSemantics;

public class OrdinalSemantics extends ModifiableSemantics
{
/*
   public OrdinalSemantics()
   { //  super(phraseStructure);
   }
*/
   public OrdinalSemantics(long value)
   {  position = value;
      direction = 1;
   }

   public OrdinalSemantics(Long value)
   {  position = value.longValue();
      direction = 1;
   }

   public int direction = 1;  // or -1
   public String origin = "first";  // or "last" or "current"
   public long position;
}  // end class OrdinalSemantics