package semanticvalues;

/**
   Allows optional modifiers to be represented as pairs of names
   and values, computed as semantic values by functions invoked from
   semantic value augmentations to ASDGrammarNode instances,
   or assigned to ASD semantic feature variables by functions which
   are invoked from semantic action augmentations to ASDGrammarNode
   instances.  It has been made a subclass of SemanticValue so
   that it inherits the latter's toString member function.
   @author James A. Mason
   @version 1.01 2005 Mar.
 */
public class Qualifier extends SemanticValue
{
   /**
      Creates an instance with given name and value.
    */
   public Qualifier(String givenName, SemanticValue givenValue)
   {  qualifierName = givenName;
      qualifierValue = givenValue;
   }

   public String qualifierName;
   public SemanticValue qualifierValue;
}