package semanticvalues;
import java.lang.reflect.*;

/**
   Subclasses define semantic values for words and phrases
   @author James A. Mason
   @version 1.01 2005 Mar.
 */
public abstract class SemanticValue implements Cloneable
{  public Object clone()
   {  try
      {  Object result = super.clone();
         return result;
      }
      catch(CloneNotSupportedException e)
      {  // can't happen, but necessary anyway
         return null;
      }
   }

   /**
      Attempts to assign a given SemanticValue as value to a member
      variable of the same type as the class of the modifier,
      or for which the modifier's class is a subclass.
      @param modifier the value to be assigned
      @return the instance after assignment of the modifier to
      the member variable; null if the modifier cannot be
      assigned to a member variable of the instance.
    */
   public SemanticValue modifyBy(SemanticValue modifier)
   {  Class thisClass = getClass(); // class of receiver
      Field currentField = null;
      Class modifierClass = modifier.getClass();

      // Find a field, if any, of the same class as the modifier,
      // or of which the modifier's class is a subclass,
      // starting from the class of the receiver and going to
      // superclasses if necessary:
      Class semanticValueClass = null;
      try
      {  semanticValueClass
            = Class.forName("semanticvalues.SemanticValue");
      }
      catch (ClassNotFoundException e) {return null;}
      boolean fieldFound = false;
      Class currentClass = thisClass;
      while (!fieldFound && currentClass != null
             && currentClass != semanticValueClass)
      {  Field[] fields = currentClass.getDeclaredFields();
         AccessibleObject.setAccessible(fields, true);
         // Check all fields of the current class
         // for a match on the class of the given modifier:
         for (int j = 0; !fieldFound && j < fields.length; j++)
         {  currentField = fields[j];
            if (currentField.getType().isAssignableFrom(modifierClass))
               fieldFound = true;
         }
         if (!fieldFound)
            currentClass = currentClass.getSuperclass();
      }
      if (!fieldFound)
         return null;  // no appropriate field found
      SemanticValue result = (SemanticValue) clone();
      try // set the value of the field in the clone
      {
         currentField.set(result, modifier);
      }
      catch (IllegalArgumentException e) {
         System.out.println(e);
         result = null;
         }
      catch (IllegalAccessException e) {
         System.out.println(e);
         result = null;
         }
      return result;
   }
   /**
      Attempts to assign a given SemanticValue as value to a
      member variable which matches the given memberName.
      @param modifier the value to be assigned
      @return the instance after assignment of the modifier to
      the member variable; null if the modifier cannot be
      assigned to a member variable of the instance.
    */
   public SemanticValue modifyBy(
      String memberName, SemanticValue modifier)
   //   throws ClassNotFoundException
   {  // returns null if the specific modifier cannot be
      // applied to the instance to which it has been applied;
      // otherwise it returns the instance after assignment
      // of the modifier to the given field.
      Class thisClass = getClass(); // class of receiver
      Field currentField = null;

      // Find a field whose name matches the given memberName,
      // starting from the class of the receiver and going to
      // superclasses if necessary:
      Class semanticValueClass = null;
      try
      {  semanticValueClass
            = Class.forName("semanticvalues.SemanticValue");
      }
      catch (ClassNotFoundException e) {return null;}
      boolean fieldFound = false;
      Class currentClass = thisClass;
      while (!fieldFound && currentClass != null
             && currentClass != semanticValueClass)
      {  Field[] fields = currentClass.getDeclaredFields();
         AccessibleObject.setAccessible(fields, true);
         // Check all fields of the current class
         // for a match on the given memberName:
         for (int j = 0; !fieldFound && j < fields.length; j++)
         {  currentField = fields[j];
            if (currentField.getName().equals(memberName))
               fieldFound = true;
         }
         if (!fieldFound)
            currentClass = currentClass.getSuperclass();
      }
      if (!fieldFound)
         return null;  // no appropriate field found
      SemanticValue result = (SemanticValue) clone();
      try // set the value of the field in the clone
      {
         currentField.set(result, modifier);
      }
      catch (IllegalArgumentException e) {
         System.out.println(e);
         result = null;
         }
      catch (IllegalAccessException e) {
         System.out.println(e);
         result = null;
         }
      return result;
   }

   /**
      Overrides the inherited toSring function;
      see toString(String) below.
    */
   public String toString()
   {  return toString("");
   }

   /**
      Converts an instance of a subclass of SemanticValue to a
      displayable string.  The name of the subclass comes first,
      followed by the names and values of all non-static,
      non-null member variables, indented by a given String prefix.
      @param indentString the prefix with which to indent member
      variables below the subclass name.
      @return the displayable string that represents the instance.
   */
   public String toString(String indentString)
   {  Class thisClass = getClass(); // class of receiver
      Field currentField = null;
      Class semanticValueClass = null;
      try
      {  semanticValueClass
            = Class.forName("semanticvalues.SemanticValue");
      }
      catch (ClassNotFoundException e) // shouldn't happen
         {  return "Class SemanticValue not found"; }

      // Display class name and values of all non-static,
      // non-null member variables of the receiver's class and its
      // superclasses up to, but not including Class SemanticValue:
      String result = thisClass.getName() + ":";
      if (indentString.length() == 0)
         indentString = "   ";
      Class currentClass = thisClass;
      while (currentClass != null
             && currentClass != semanticValueClass)
      {  Field[] fields = currentClass.getDeclaredFields();
         if (fields != null)
         {  // Display values of non-static, non-null fields
            // of the current class:
            AccessibleObject.setAccessible(fields, true);
            for (int j = 0; j < fields.length; j++)
            {  currentField = fields[j];
               try
               {  if (currentField.get(this) == null)
                     ; // null value; suppress display of field
                  else if (Modifier.isStatic(currentField.getModifiers()))
                     ; // static field; suppress display of field
                  else
                  {  result += "\n" + indentString
                            + currentField.getName() + " = ";
                     // If the current field's class is a subclass
                     // of SemanticValue, use this method recursively
                     // to display that field's value, indented
                     // further:
                     Class currentFieldClass
                        = currentField.get(this).getClass();
                     if (semanticValueClass.isAssignableFrom(
                           currentFieldClass))
                        result +=
                           ((SemanticValue) currentField.get(this))
                                 .toString(indentString + "   ");
                     else // use the default toString method to
                          // display the field's value:
                        result += currentField.get(this) + ";";
                  }
               }
               catch (IllegalAccessException e) {
                  result = e.toString(); }
            }
         }
         currentClass = currentClass.getSuperclass();
      }
      return result;
   }

}  // end class SemanticValue
