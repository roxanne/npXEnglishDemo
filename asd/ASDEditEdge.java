/*

Copyright 2001-2003 James A. Mason

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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;  // ChangeListener

/**
   Instances represent edges in an ASD grammar for
   the graphical grammar editor.  Access is package access.
   @author James A. Mason
   @version 1.03 2001 Oct 25, 30; Nov 1-2, 6-7, 9; Dec 31; 2003 Jul
 */
class ASDEditEdge extends JRadioButton
   implements MouseMotionListener, ChangeListener
{  ASDEditEdge(Container given)
   {  context = given;
      addMouseMotionListener((MouseMotionListener) this);
      addChangeListener((ChangeListener) this);
      addMouseListener(new PopupListener(new EditEdgeMenu(this)));
      setOpaque(false);
   }

   ASDEditEdge(Container given, int xPos, int yPos)
   {  this(given);
//      setPosition(xPos, yPos); // not appropriate because it adds offsets
      setLocation(xPos, yPos);
//      setSize(SIZE*3/2, SIZE); // SIZE*3/2 needed for some systems
      setSize(SIZE, SIZE);
   }

   Container getContext() { return context; }

   ASDEditor getEditor() { return editor; }

   ASDEditNode getFromNode()
   {  return ((ASDDigraphNode)getDigraphEdge().getFromNode()).getEditNode();
   }

   ASDEditNode getToNode()
   {  return ((ASDDigraphNode)getDigraphEdge().getToNode()).getEditNode();
   }

   public void mouseDragged(MouseEvent e)
   {  context.remove(this);
      context.add(this, 0);
      int newX = getX() + e.getX();
      int newY = getY() + e.getY();
      setLocation(newX, newY); // inherited from java.awt.Component
      successor.setXCoordinate((short)newX);
      successor.setYCoordinate((short)newY);
      editor.setGrammarChanged(true);
      context.repaint();
   }

   public void mouseMoved(MouseEvent e) { }

   void setEditor(ASDEditor givenEditor) { editor = givenEditor; }

   void setContext(Container c) { context = c; }

   void setDefaultCoordinates()
   {  ASDEditNode eNode1
         = ((ASDDigraphNode)dEdge.getFromNode()).getEditNode();
      ASDEditNode eNode2
         = ((ASDDigraphNode)dEdge.getToNode()).getEditNode();
      int x1 = eNode1.rightConnectorX();
      int y1 = eNode1.rightConnectorY();
      int x2 = eNode2.leftConnectorX();
      int y2 = eNode2.leftConnectorY();
      if (eNode1 != eNode2)
         setPosition((x1 + x2)/2 - (SIZE/2), (y1 + y2)/2 - (SIZE/2));
//         setPosition((x1 + x2)/2 - (3*SIZE/4), (y1 + y2)/2 - (SIZE/2));
// 3*SIZE/4 needed for some systems
      else
         setPosition(
            (x1 + x2)/2 - (SIZE/2), y1 - eNode1.getHeight() - (SIZE/2));
//            (x1 + x2)/2 - (3*SIZE/4), y1 - eNode1.getHeight() - (SIZE/2));
// 3*SIZE/4 needed for some systems
      successor.setXCoordinate((short)getX());
      successor.setYCoordinate((short)getY());
   }

   void setPosition(int newX, int newY)
   {  Insets insets = context.getInsets();
      setBounds(newX + insets.left, newY + insets.top, SIZE, SIZE);
//      setBounds(newX + insets.left, newY + insets.top, SIZE*3/2, SIZE);
// SIZE*3/2 needed for some systems
   }

   ASDDigraphEdge getDigraphEdge() { return dEdge; }
   void setDigraphEdge(ASDDigraphEdge newEdge) { dEdge = newEdge; }
   ASDGrammarSuccessor getGrammarSuccessor() { return successor; }
   void setGrammarSuccessor(ASDGrammarSuccessor s) { successor = s; }

   public void stateChanged(ChangeEvent e)
      // required by interface ChangeListener
   {  if (this.isSelected() && this.getModel().isPressed())
      // the second conjunct is needed to avoid selection just by
      // rollover
      {  editor.edgeSelected(this);
      }
      else if (!this.isSelected() && this.getModel().isPressed())
      {  // If the edge is already selected, DON'T toggle it.
         this.setSelected(true);
      }
   }

   static final int SIZE = 12;
   private Container context;
   private ASDEditor editor;
   private ASDGrammarSuccessor successor;
   private ASDDigraphEdge dEdge;
} // end class ASDEditEdge

class EditEdgeMenu extends JPopupMenu implements ActionListener
{  EditEdgeMenu(ASDEditEdge edge)
   {  eEdge = edge;
      setInvoker(eEdge);
      JMenuItem deleteMenuItem = new JMenuItem("Delete edge");
//         , KeyEvent.VK_D);
//      deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(
//         KeyEvent.VK_D, ActionEvent.ALT_MASK));
      deleteMenuItem.addActionListener(this);
      add(deleteMenuItem);
   }

   public void actionPerformed(ActionEvent e)
   {  if (e.getActionCommand().equals("Delete edge"))
      {  ASDEditor editor = eEdge.getEditor();
         if (editor != null)
            editor.deleteEdge(eEdge);
      }
   }

   ASDEditEdge eEdge;
}
