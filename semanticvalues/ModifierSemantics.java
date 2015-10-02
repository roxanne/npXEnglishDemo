package semanticvalues;

/**
   Allows a Lisp-like list of modifiers, each of which can
   itself be modified.  It is used by class ModifiableSemantics.
   Instances consist of a modifier name, a modifier value,
   and a link to the next instance of ModifierSemantics in
   a list.
   @author James A. Mason
   @version 1.01 2005 Mar.
 */
public class ModifierSemantics extends SemanticValue
{
   /**
      Creates an instance with given name and value, and
      a null link.
      @param givenName the name of the modifier
      @param newMod the value of the modifier
    */
   public ModifierSemantics(String givenName, SemanticValue newMod)
   {  modifierName = givenName;
      modifierValue = newMod;
      otherModifiers = null;
   }

   /**
      Returns the first instance (if any) of ModifierSemantics
      whose modifierName matches the given String, starting with
      the receiver and searching down the list formed by
      successive otherModifier links.  If no instance of
      ModifierSemantics is found whose modifierName matches
      the given String, the value returned is null.
      @param name the sought-after modifierName
    */
   public ModifierSemantics getModifierNamed(String name)
   {  if (name == null)
         return null;
      if (name.equals(modifierName))
         return this;
      if (otherModifiers == null)
         return null;
      return otherModifiers.getModifierNamed(name);
   }

   /**
      Removes, from the list starting with the otherModifiers
      link of the receiver, the first instance (if any) of
      ModifierSemantics whose modifierName matches the given
      String.  If no such instance of ModifierSemantics is
      found, the list remains unchanged.
    */
   public void removeOtherModifierNamed(String name)
   {  if (name == null || otherModifiers == null)
         return;
      if (otherModifiers.modifierName.equals(name))
      {  // match on first of otherModifiers; link around it:
         otherModifiers = otherModifiers.otherModifiers;
         return;
      }
      otherModifiers.removeOtherModifierNamed(name);
   }

   /**
      Accessor function for the modifierName member variable.
    */
   public String getName()
   {  return modifierName;
   }

   /**
      Accessor function for the modifierValue member variable.
    */
   public SemanticValue getValue()
   {  return modifierValue;
   }

   /**
      Mutator function for the modifierValue member variable.
    */
   public void setValue(SemanticValue newValue)
   {  modifierValue = newValue;
   }

   /**
      The name of the ModifierSemantics feature.
    */
   //String modifierName;
    public String modifierName;
   /**
      The value of the ModifierSemantics feature.
    */
   SemanticValue modifierValue;

   /**
      Link to next ModifierSemantics instance in the current list.
    */
  public  ModifierSemantics otherModifiers = null;
}
