package semanticvalues;

/**
   Subclasses define semantic values that can have variable
   numbers of optional modifiers (as well as fixed numbers
   of semantic parameters corresponding to instance variables).
   @author James A. Mason
   @version 1.01 2005 Mar.
 */
public class ModifiableSemantics extends SemanticValue
{
   /**
      First it tries to assign a given SemanticValue to
      a semantic parameter (i.e., member variable) whose
      name matches a given string; if that is unsuccessful,
      it adds a modifier with a given name and value to the
      beginning of the list of optional modifiers for the
      current instance, provided the instance has not been
      flagged as unmodifiable.
      @param modifierName a string naming the modifier
      @param modifierValue the value of the modifier
      @return the instance after assignment of the modifier
      name and value; null if the modifierName is null
      or if the instance is not modifiable.
    */
   public SemanticValue modifyBy(String modifierName,
         SemanticValue modifierValue)
   {  if (modifierName == null)
         return null;
      SemanticValue result
         = super.modifyBy(modifierName, modifierValue);
      if (result != null)
         return result;
      if (! isModifiable)
         return null;
      return modifyBy(
         new ModifierSemantics(modifierName, modifierValue));
   }

   /*
      Adds a given ModifierSemantics instance to the beginning
      of the list of optional modifiers, in a clone of the
      receiver instance.  Returns the updated clone.
    */
   protected SemanticValue modifyBy(ModifierSemantics mod)
   {  if (mod == null) // defensive; shouldn't happen
         return this;
      ModifiableSemantics result
         = (ModifiableSemantics) this.clone();
      ModifierSemantics mods
         = (ModifierSemantics) mod.clone();
      mods.otherModifiers = modifiers;
      result.modifiers = mods;
      return result;
   }

   /**
      @return the first modifier in the list of optional modifiers;
      null if there list is empty.
    */
   public ModifierSemantics firstModifier()
   {  return modifiers;
   }

   /*
      Returns from the list of optional modifiers, the first modifier
      whose name matches the given String; null if there is no match.
    */
   protected ModifierSemantics getModifierNamed(String name)
   {  if (name == null || modifiers == null)
         return null;
      return modifiers.getModifierNamed(name);
   }

   /**
      @return the list of modifiers (if any) after the first modifier
      (if any); null if there is no such list.
    */
   public ModifierSemantics getOtherModifiers()
   {  if (modifiers == null)
         return null;
      ModifierSemantics other = modifiers.otherModifiers;
      return other;
   }

   /**
      Gets the value of the modifier (if any) with specified name.
      @param name the name of the modifier whose value is to be returned.
      @return from the list of optional modifiers, the value of first
      modifier whose name matches the given String;
      null if there is no match.
    */
   public SemanticValue getValueOfModifierNamed(String name)
   {  ModifierSemantics modifier = getModifierNamed(name);
      if (modifier == null)
         return null;
      return modifier.getValue();
   }

   /**
      Removes the first occurrence, if any, of a modifier with
      specified name from the list of optional modifiers.
      @param name the name of the modifier to be removed.
    */
   public void removeModifierNamed(String name)
   {  if (name == null || modifiers == null)
         return;
      if (modifiers.modifierName.equals(name))
      {  // match on first modifier; link around it:
         modifiers = modifiers.otherModifiers;
         return;
      }
      modifiers.removeOtherModifierNamed(name);
   }

   /**
      Associates a new value in the list of optional modifiers
      with an existing modifier whose name is given.
      @param name the name of the modifier whose value is to be changed
      @param newValue the new value for the named modifier
      @return true if a modifier with the given name was found,
      false otherwise.
    */
   public boolean setValueOfModifierNamed(String name,
                      SemanticValue newValue)
   {  ModifierSemantics modifier = getModifierNamed(name);
      if (modifier == null)
         return false;
      modifier.setValue(newValue);
      return true;
   }

   /** true by default but can be set false to indicate that an
       instance of a subclass of ModifiableSemantics is not to
       be considered modifiable by optional modifiers
    */
   public boolean isModifiable = true;

   //protected ModifierSemantics modifiers = null;
   public ModifierSemantics modifiers = null;
      // head of the list of optional modifiers
}
