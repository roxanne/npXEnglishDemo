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

package digraphs;

/**
   A class for representing nodes in directed graphs that have
   strings as labels for the nodes.

   @author James A. Mason
   @version 1.03 2001 Feb 2
*/
public class DigraphNodeLabeled extends DigraphNode
{
   /**
      Initializes a new DigraphNodeLabeled with a given label and
      default estimated number of in-edges and of out-edges per node.
    */
   public DigraphNodeLabeled(String label)
   {  // super(); // default
      nodeLabel = label;
   }

   /**
      Returns the current label of the node.
    */
   public String getLabel()
   {  return nodeLabel;
   }

   /**
      Changes the label of the node to a given new value.
    */
   public void setLabel(String newLabel)
   {  nodeLabel = newLabel;
   }

   private String nodeLabel;
} // end class DigraphNodeLabeled