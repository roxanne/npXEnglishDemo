/*

Copyright 2001, 2010 James A. Mason

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
import java.util.*;

/**
 A class for representing nodes of directed graphs.

 @author James A. Mason
 @version 1.04 2010 Aug
 */
public class DigraphNode
{
   /**
    Initializes a new DigraphNode with a default estimated
    number of in-edges and of out-edges per node.
    */
   public DigraphNode()
   {  this(EDGES_PER_NODE);
   }

   /**
    Initializes a new DigraphNode with an estimated number of
    in-edges and of out-edges per node.
    */
   public DigraphNode(int edgesPerNode)
   {  inEdges = new ArrayList<DigraphEdge>(edgesPerNode);
      outEdges = new ArrayList<DigraphEdge>(edgesPerNode);
   }

   /**
    Adds given DigraphEdge to the list of inEdges, after
    checking that the DigraphEdge is connected TO the receiver.
    @param aDigraphEdge the DigraphEdge to be added to the node.
    @return the DigraphEdge if successful; null if the edge
    does not enter the node or has already been included as
    an incoming edge
    */
   public DigraphEdge addInEdge(DigraphEdge aDigraphEdge)
   {  if (aDigraphEdge.getToNode() != this)
      return null;  // the edge does not enter this node
      for (int j=0; j < inEdges.size(); ++j)
         if (inEdges.get(j) == aDigraphEdge)
            return null;  // the edge is already included
      inEdges.add(aDigraphEdge);
      return aDigraphEdge;
   }

   /**
    Adds given DigraphEdge to the list of outEdges, after
    checking that the DigraphEdge is connected FROM the receiver.
    @param aDigraphEdge the DigraphEdge to be added to the node.
    @return the DigraphEdge if successful; null if the edge
    does not leave the node or has already been included as
    an outgoing edge
    */
   public DigraphEdge addOutEdge(DigraphEdge aDigraphEdge)
   {  if (aDigraphEdge.getFromNode() != this)
      return null;  // the edge does not leave this node
      for (int j=0; j < outEdges.size(); ++j)
         if (outEdges.get(j) == aDigraphEdge)
            return null;  // the edge is already included
      outEdges.add(aDigraphEdge);
      return aDigraphEdge;
   }

   /**
    Returns an ArrayList of all DigraphNodes from which the receiver
    can be reached by traversal of one or more edges out.
    */
   public ArrayList<DigraphNode> ancestors()
   {  HashSet<DigraphNode> set = new HashSet<DigraphNode>(2*inDegree());
      set = ancestors(set);
      Iterator<DigraphNode> it = set.iterator();
      ArrayList<DigraphNode> result = new ArrayList<DigraphNode>(set.size());
      while(it.hasNext())
         result.add(it.next());
      return result;
   }

   /**
    Adds to the HashSet aSet all DigraphNodes that are NOT already
    in aSet but from which the receiver is reachable by traversal of
    one or more edges out.  Returns the result of updating aSet.
    @param aSet a HashSet of DigraphNodes.
    @return the updated HashSet of DigraphNodes.
    */
   public HashSet<DigraphNode> ancestors(HashSet<DigraphNode> aSet)
   {  Iterator<DigraphNode> it = neighboursIn().iterator();
      DigraphNode neighbour;
      // For each neighbour on incoming edges:
      while(it.hasNext())
      {  neighbour = it.next();
         if (!aSet.contains(neighbour))
         // neighbour is not already in the set of ancestors;
         // so add it to the set of ancestors and add all of
         // its ancestors that are not already in the set
         {  aSet.add(neighbour);
            neighbour.ancestors(aSet);
         }
      }
      return aSet;
   } // end ancestors(aSet)

   /**
    Returns an ArrayList of all DigraphNodes from which the receiver
    can be reached by traversal of one or more edges out without regard
    to direction.
    */
   public ArrayList<DigraphNode> connectedNodes()
   {  HashSet<DigraphNode> set
           = new HashSet<DigraphNode>(2*(inDegree()+outDegree()));
      set = connectedNodes(set);
      Iterator<DigraphNode> it = set.iterator();
      ArrayList<DigraphNode> result
              = new ArrayList<DigraphNode>(set.size());
      while(it.hasNext())
         result.add(it.next());
      return result;
   }

   /**
    Adds to the HashSet aSet all DigraphNodes that are NOT already
    in aSet but from which the receiver is reachable by traversal of
    one or more edges without regard to edge direction.
    Returns the result of updating aSet.
    @param aSet a HashSet of DigraphNodes.
    @return the updated HashSet of DigraphNodes.
    */
   public HashSet<DigraphNode> connectedNodes(HashSet<DigraphNode> aSet)
   {  Iterator<DigraphNode> it = neighboursIn().iterator();
      DigraphNode neighbour;
      // For each neighbour on incoming edges:
      while(it.hasNext())
      {  neighbour = it.next();
         if (!aSet.contains(neighbour))
         // neighbour is not already in the set of connected
         // nodes; so add it to the set and add all nodes
         // connected to it that are not already in the set
         {  aSet.add(neighbour);
            neighbour.connectedNodes(aSet);
         }
      }
      // For each neighbour on outgoing edges:
      it = neighboursOut().iterator();
      while(it.hasNext())
      {  neighbour = it.next();
         if (!aSet.contains(neighbour))
         // neighbour is not already in the set of connected
         // nodes; so add it to the set and add all nodes
         // connected to it that are not already in the set
         {  aSet.add(neighbour);
            neighbour.connectedNodes(aSet);
         }
      }
      return aSet;
   } // end connectedNodes(aSet)

   /**
    Returns an ArrayList of all DigraphNodes that are reachable
    from the receiver by traversal of one or more edges out.
    */
   public ArrayList<DigraphNode> descendants()
   {  HashSet<DigraphNode> set
           = new HashSet<DigraphNode>(2*inDegree());
      set = descendants(set);
      Iterator<DigraphNode> it = set.iterator();
      ArrayList<DigraphNode> result
              = new ArrayList<DigraphNode> (set.size());
      while(it.hasNext())
         result.add(it.next());
      return result;
   }

   /**
    Adds to the HashSet aSet all DigraphNodes that are NOT already
    in aSet but are reachable from the receiver by traversal of
    one or more edges out.  Returns the result of updating aSet.
    @param aSet a HashSet of DigraphNodes.
    @return the updated HashSet of DigraphNodes.
    */
   public HashSet<DigraphNode> descendants(HashSet<DigraphNode> aSet)
   {  Iterator<DigraphNode> it = neighboursOut().iterator();
      DigraphNode neighbour;
      // For each neighbour on incoming edges:
      while(it.hasNext())
      {  neighbour = it.next();
         if (!aSet.contains(neighbour))
         // neighbour is not already in the set of descendants;
         // so add it to the set of descendants and add all of
         // its descendants that are not already in the set
         {  aSet.add(neighbour);
            neighbour.descendants(aSet);
         }
      }
      return aSet;
   } // end descendants(aSet)

   /**
    Returns an ArrayList of the DigraphEdges that enter the DigraphNode.
    */
   public ArrayList<DigraphEdge> getInEdges() { return inEdges; }

   /**
    Returns an ArrayList of the DigraphEdges that leave the DigraphNode.
    */
   public ArrayList<DigraphEdge> getOutEdges() { return outEdges; }

   /**
    Returns the number of DigraphEdges that enter the DigraphNode.
    */
   public int inDegree() { return inEdges.size(); }

   /**
    Returns an ArrayList of the DigraphNodes at the beginnings of
    all incoming edges.  (Some may be identical.)
    */
   public ArrayList<DigraphNode> neighboursIn()
   {
      ArrayList<DigraphNode> result
              = new ArrayList<DigraphNode>(inDegree());
      for (int j = 0; j < inDegree(); ++j)
         result.add(
                 ((DigraphEdge)inEdges.get(j)).getFromNode());
      return result;
   }

   /**
    Returns an ArrayList of the DigraphNodes at the ends of all
    outgoing edges.  (Some may be identical.)
    */
   public ArrayList<DigraphNode> neighboursOut()
   {
      ArrayList<DigraphNode> result
              = new ArrayList<DigraphNode>(outDegree());
      for (int j = 0; j < outDegree(); ++j)
         result.add(
                 ((DigraphEdge)outEdges.get(j)).getToNode());
      return result;
   }

   /**
    Returns the number of DigraphEdges that leave the DigraphNode.
    */
   public int outDegree() { return outEdges.size(); }

   /**
    Removes given DigraphEdge from the list of inEdges, after
    checking that the DigraphEdge is connected TO the receiver.
    @param aDigraphEdge the DigraphEdge to be removed from the node.
    @return the DigraphEdge if successful; null if the edge
    does not enter the node
    */
   public DigraphEdge removeInEdge(DigraphEdge aDigraphEdge)
   {  if (aDigraphEdge.getToNode() != this)
      return null;  // the edge does not enter this node
      for (int j=0; j < inEdges.size(); ++j)
         if (inEdges.get(j) == aDigraphEdge)
         {  inEdges.remove(j);
            return aDigraphEdge;
         }
      return null;
   }

   /**
    Removes given DigraphEdge from the list of outEdges, after
    checking that the DigraphEdge is connected FROM the receiver.
    @param aDigraphEdge the DigraphEdge to be removed from the node.
    @return the DigraphEdge if successful; null if the edge
    does not leave the node
    */
   public DigraphEdge removeOutEdge(DigraphEdge aDigraphEdge)
   {  if (aDigraphEdge.getFromNode() != this)
      return null;  // the edge does not leave this node
      for (int j=0; j < outEdges.size(); ++j)
         if (outEdges.get(j) == aDigraphEdge)
         {  outEdges.remove(j);
            return aDigraphEdge;
         }
      return null;
   }

   private static final int EDGES_PER_NODE = 5; // initial estimate
   private ArrayList<DigraphEdge> inEdges;
   private ArrayList<DigraphEdge> outEdges;
} // end class DigraphNode
