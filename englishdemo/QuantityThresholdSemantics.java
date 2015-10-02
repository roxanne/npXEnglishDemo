package englishdemo;

public class QuantityThresholdSemantics extends QuantitySemantics
{
   public QuantityThresholdSemantics(ThresholdSemantics givenThreshold)
   {  threshold = givenThreshold;
   }

   public ThresholdSemantics threshold;
}