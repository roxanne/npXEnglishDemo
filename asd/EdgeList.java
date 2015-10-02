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
import java.lang.reflect.*;
import java.awt.event.*; // ActionEvent, ActionListener,
import javax.swing.*;    // JFrame, JPanel, JButton
import javax.swing.event.*;  // ListSelectionListener

class EdgeList extends JList
{  EdgeList(ASDEditor ed, EdgeListModel model)
   {  super(model);
      editor = ed;
      EdgeListMenu menu = new EdgeListMenu(this, ed);
      MouseListener popupListener = new PopupListener(menu);
      addMouseListener(popupListener);
   }

   EdgeList(ASDEditor ed)
   {  this(ed, new EdgeListModel(null));
   }

   private ASDEditor editor;
} // end class EdgeList

class EdgeListMenu extends JPopupMenu implements ActionListener
{  EdgeListMenu(EdgeList eList, ASDEditor ed)
   {  edgeList = eList;
      editor = ed;
      setInvoker(eList);
      JMenuItem addMenuItem = new JMenuItem("Add edge");
//         , KeyEvent.VK_A);
//      addMenuItem.setAccelerator(KeyStroke.getKeyStroke(
//         KeyEvent.VK_A, ActionEvent.CTRL_MASK));
      addMenuItem.addActionListener(this);
      add(addMenuItem);
      JMenuItem deleteMenuItem = new JMenuItem("Delete edge");
//         , KeyEvent.VK_D);
//      deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(
//         KeyEvent.VK_D, ActionEvent.CTRL_MASK));
      deleteMenuItem.addActionListener(this);
      add(deleteMenuItem);
   }

   public void actionPerformed(ActionEvent e)
   {  if (editor == null) return;
      if (e.getActionCommand().equals("Add edge"))
      {  try
         {  editor.addEdge();
         }
         catch(ClassNotFoundException ex) {}
         catch(IllegalAccessException ex) {}
         catch(InstantiationException ex) {}
         catch(InvocationTargetException ex) {}
      }
      else if (e.getActionCommand().equals("Delete edge"))
      {  String selected = (String)edgeList.getSelectedValue();
         int edgeIndex = -1;
         if (selected != null)
            edgeIndex = Integer.parseInt(selected)-1;
         editor.deleteEdge(edgeIndex);
         // edgeIndex -1 means no edge selected
      }
   }

   ASDEditor editor;
   EdgeList edgeList; // the edge list in the upper middle pane to
      // which the menu is attached.
} // end class EdgeListMenu

/**
   Defines list models for a list of edges out of an ASDDigraph node
 */
class EdgeListModel extends DefaultListModel
{
   EdgeListModel(ASDDigraphNode dNode)
   {  if (dNode != null)
      {  int numberOfEdges = dNode.outDegree();
         for (int j = 1; j <= numberOfEdges; j++ )
            this.addElement(j + "");
      }
   }
}

/**
   Defines a selection listener for a word's list of instances.
 */
class EdgeListSelectionListener implements ListSelectionListener
{  EdgeListSelectionListener(ASDEditor e)
   {  editor = e;
   }

   public void valueChanged(ListSelectionEvent e)
   {  if (!e.getValueIsAdjusting())
         editor.newEdgeSelected();
   }

   private ASDEditor editor;
}
