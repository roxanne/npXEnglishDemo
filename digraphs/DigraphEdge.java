

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
   A class for representing edges of directed graphs.

   @author James A. Mason
   @version 1.03 2001 Feb 2
*/
public class DigraphEdge
{
   /**
      Initializes a new DigraphEdge connecting two given DigraphNodes.
      @param originNode the node at the beginning of the edge
      @param destinationNode the node at the end of the edge
   */
   public DigraphEdge(DigraphNode originNode, DigraphNode destinationNode)
   {
      fromNode = originNode;
      toNode = destinationNode;
   }

   /** Returns the DigraphNode at which the DigraphEdge begins
    */
   public DigraphNode getFromNode() { return fromNode; }

   /** Returns the DigraphNode at which the DigraphEdge ends
    */
   public DigraphNode getToNode()   { return toNode; }

   private DigraphNode fromNode;
   private DigraphNode toNode;

} // end class DigraphEdge

