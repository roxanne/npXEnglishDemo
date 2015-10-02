/*

Copyright 2001 James A. Mason

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy
of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations
under the License.

 */

package asd;
import java.util.*;

/**
   Instances are entries for an ASDGrammar, consisting of
   a word and an ArrayList of instances for that word.
   Visibility is restricted to the asd package.
   @author James A. Mason
   @version 1.01 2001 Oct 10
 */
class ASDWordEntry extends ArrayList implements Comparable
{
   ASDWordEntry(String word, ArrayList instances)
   {  super(2);  // a list of two elements
      add(word);
      add(instances);
   }

   public int compareTo(Object other)
   {  ASDWordEntry otherEntry = (ASDWordEntry)other;
      String myWord = ((String) get(0)).toLowerCase();
      String otherWord = ((String) otherEntry.get(0)).toLowerCase();
      return myWord.compareTo(otherWord);
   }
}
