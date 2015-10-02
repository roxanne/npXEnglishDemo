/*

Copyright 2001-2004 James A. Mason

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
import digraphs.*;
import java.util.*;  // ArrayList, Iterator
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;  // ChangeListener

/**
   Instances represent nodes in an ASD grammar for
   the graphical grammar editor.  Access is package access.
   @author James A. Mason
   @version 1.05 2001 Sep 26, 29; Oct 2, 11, 15-16, 19, 25, 30;
                      Nov 1-2, 8-9, 13-14, 19-24, 26; Dec 31;
                 2002 Jan 31; 2003 Jul; 2004 Aug
 */
class ASDEditNode extends JToggleButton
   implements MouseListener, MouseMotionListener, ChangeListener
{  ASDEditNode(ASDGrammarNode grammarNode, Container given)
   {  super(" " + grammarNode.word() + " " + grammarNode.instance() + " ");
      gNode = grammarNode;
      context = given;
      addMouseListener((MouseListener) this);
      popupListener = new PopupListener(new EditNodeMenu(this));
      addMouseListener(popupListener);
      addChangeListener((ChangeListener) this);
      addMouseMotionListener((MouseMotionListener) this);
      if (grammarNode.isInitial())
      {  setBackground(Color.yellow);
         setBorder(BorderFactory.createLineBorder(Color.yellow, 1));
           //  two pixels wide
      }
      else
      {  setBackground(Color.white);
         setBorder(BorderFactory.createLineBorder(Color.black, 1));
      }
      if (grammarNode.isFinal())
         initializeRightLabel(grammarNode.phraseType());
   }

   ASDEditNode(ASDGrammarNode grammarNode, Container given,
         int xPos, int yPos)
   {  this(grammarNode, given);
      setPosition((short)xPos, (short)yPos);
   }

   ASDEditNode(ASDGrammarNode grammarNode, Container given,
         short xPos, short yPos)
   {  this(grammarNode, given);
      setPosition(xPos, yPos);
   }

   /* This method is needed so the popup menu can change when
      a node is toggled between final and non-final -- to
      insert and remove the "Edit semantic value" option.
    */
   void changePopupMenu()
   {  removeMouseListener(popupListener);
      popupListener = new PopupListener(new EditNodeMenu(this));
      addMouseListener(popupListener);
   }

   Container getContext() { return context; }

   ASDGrammarNode getGrammarNode() { return gNode; }

   void setGrammarNode(ASDGrammarNode g) { gNode = g; }

   ASDDigraphNode getDigraphNode() { return dNode; }

   void setDigraphNode(ASDDigraphNode d) { dNode = d; }

   ASDEditor getEditor() { return editor; }

   RightLabel getRightLabel() { return rightLabel; }

   private void initializeRightLabel(String text)
   {  if(text != null && text.length() > 0)
         rightLabel = new RightLabel(this, text);
   }

   boolean isFinal() { return gNode.isFinal(); }

   boolean isSingleton()
   {  // indicates whether the node has no outgoing or incoming
      // edges.  Note: a node with an edge to itself is not considered
      // singleton by this criterion.
      return dNode.isSingleton();
   }

   short leftConnectorX() { return (short) (getX()); }

   short leftConnectorY() { return (short) (getY() + getHeight() / 2); }

   short rightConnectorX() { return (short) (getX() + getWidth()); }

   short rightConnectorY() { return (short) (getY() + getHeight() / 2); }

   public void mouseDragged(MouseEvent e)
   {  if (!e.isShiftDown())  // normal drag
         move(this, e);
      else // drag entire connected component
      {  ArrayList connectedNodes = dNode.connectedNodes();
         if (connectedNodes.size() == 0)
            move(this, e);
         else
            for (Iterator it = connectedNodes.iterator(); it.hasNext(); )
            {  ASDDigraphNode dNode2 = (ASDDigraphNode)it.next();
               ASDEditNode eNode2 = dNode2.getEditNode();
               move(eNode2, e);
            }
      }
      getEditor().setGrammarChanged(true);
      getContext().repaint();
   }

   private void move(ASDEditNode eNode, MouseEvent e)
   {  eNode.getContext().remove(eNode);
      eNode.getContext().add(eNode, 0);
      int newX = eNode.getX() + e.getX();
      int newY = eNode.getY() + e.getY();
      eNode.setLocation(newX, newY);
         // inherited from java.awt.Component
      ASDGrammarNode gNode = eNode.getGrammarNode();
      gNode.setXCoordinate((short)newX);
      gNode.setYCoordinate((short)newY);
      RightLabel rightLabel = eNode.getRightLabel();
      if (rightLabel != null)
      {  eNode.getContext().remove(rightLabel);
         eNode.getContext().add(rightLabel, 0);
         rightLabel.setLocation(
            newX + eNode.getWidth() + RightLabel.SEPARATION,
            newY + eNode.getInsets().top);
      }
      ASDDigraphNode dNode = eNode.getDigraphNode();
      Iterator it;
      for (it = dNode.getInEdges().iterator(); it.hasNext(); )
         ((ASDDigraphEdge) it.next()).getEditEdge().setDefaultCoordinates();
      for (it = dNode.getOutEdges().iterator(); it.hasNext(); )
         ((ASDDigraphEdge) it.next()).getEditEdge().setDefaultCoordinates();
   }

   public void mouseMoved(MouseEvent e)
   {  // needed for MouseMotionListener interface
   }

   public void mouseClicked(MouseEvent e)
   {  // needed for MouseListener interface
   }

   public void mouseEntered(MouseEvent e)
   {  // needed for MouseListener interface
   }

   public void mouseExited(MouseEvent e)
   {  // needed for MouseListener interface
   }

   public void mousePressed(MouseEvent e)
   {  // needed for MouseListener interface
      if (e.isControlDown())
         getEditor().toggleListSelected();
//      mouseLeftButtonDown = true;
      getEditor().nodeSelected(this);
   }

   public void mouseReleased(MouseEvent e)
   {  // needed for MouseListener interface
//      mouseLeftButtonDown = false;
   }

   public void repaint()
   {  super.repaint();
      if (rightLabel != null)
         rightLabel.repaint();
   }

   void setContext(Container newPanel) { context = newPanel; }

   void setEditor(ASDEditor newEditor)
   {  editor = newEditor;
      setContext(newEditor.getNetPanel());
   }

   void setPosition(int newX, int newY)
   {
      Insets insets = getContext().getInsets();
      insets = getContext().getInsets();
      FontMetrics metrics = getFontMetrics(getFont());
      int stringWidth = metrics.stringWidth(getText());
      int stringHeight = metrics.getHeight();
      setBounds(newX + insets.left, newY + insets.top,
         stringWidth + getInsets().left + getInsets().right,
         stringHeight + insets.top + insets.bottom + VERTICAL_PAD);
      if (rightLabel != null)
         rightLabel.setPosition(newX, newY);
   }

   void setRightLabel(String text)
   {  if (context != null && rightLabel != null)
         context.remove(rightLabel);
      if (text != null && text.length() > 0)
      {  rightLabel = new RightLabel(this, text);
         if (context != null)
         {  context.add(rightLabel, 0);
            // setPosition, not setLocation, must be used here.
            // I don't know why these arguments work, but they do!
            rightLabel.setPosition(getX(), getY());
         }
      }
      else
         rightLabel = null;
      if (context != null)
         context.repaint();
   }

   public void stateChanged(ChangeEvent e)
      // required by interface ChangeListener
   {
      if (this.getModel().isPressed() && !this.isSelected())
         // If the node is already pressed, DON'T toggle it.
         this.setSelected(true);
   /*
      if (this.isSelected() && this.getModel().isPressed())
      // the second conjunct is needed to avoid selection just by
      // rollover
      {  // setBackground(Color.blue);  // doesn't work right
         // The !mouseLeftButtonDown condition, along with having
         // this class implement the MouseListener interface,
         // prevents the nodeSelected message from being sent to
         // the editor TWICE, once when the button is pressed
         // down by the mouse, and again when the button is released
         // by releasing the mouse button.
         if (!mouseLeftButtonDown)
            getEditor().nodeSelected(this);
      }
      else if (!this.isSelected() && this.getModel().isPressed())
      {  // If the node is already pressed, DON'T toggle it.
         this.setSelected(true);
      }
   */
   }

   void updateColor()
   {  if (gNode.isInitial())
      {  setBackground(Color.yellow);
         setBorder(BorderFactory.createLineBorder(Color.yellow, 1));
           //  two pixels wide
      }
      else
      {  setBackground(Color.white);
         setBorder(BorderFactory.createLineBorder(Color.black, 1));
      }
   }

   /**
      Resets the horizontal and vertical coordinates of the
      corresponding ASDGrammarNode to equal those of this ASDEditNode.
    */
   void updateGrammarNode()
   {   gNode.setXCoordinate((short) getX());
       gNode.setYCoordinate((short) getY());
   }

   private final int VERTICAL_PAD = 10;
   private ASDDigraphNode dNode;  // the corresponding ASDDigraphNode
   private ASDGrammarNode gNode;  // the corresponding grammar node
   private ASDEditor editor; // the editor using this ASDEditNode
   private Container context;
   private RightLabel rightLabel;
   private PopupListener popupListener; // displays the popup menu
//   private boolean mouseLeftButtonDown;
} // end class ASDEditNode

class RightLabel extends JLabel
{
   RightLabel(ASDEditNode e, String text)
   {  super(text);
      eNode = e;
   }

   void setPosition(int newX, int newY)
   {  Insets insets = eNode.getContext().getInsets();
      FontMetrics metrics = getFontMetrics(getFont());
      int stringWidth = metrics.stringWidth(getText());
      int stringHeight = metrics.getHeight();
      setBounds(newX + insets.left + eNode.getWidth() + SEPARATION,
         newY + insets.top + eNode.getInsets().top,
         metrics.stringWidth(getText()),
         metrics.getHeight());
   }

   // setText(String text) method is inherited from JLabel

   static final int SEPARATION = 10;
   private ASDEditNode eNode;  // the edit node to which
      // the right label is attached
} // end class RightLabel

class EditNodeMenu extends JPopupMenu implements ActionListener
{  EditNodeMenu(ASDEditNode node)
   {  eNode = node;
      setInvoker(eNode);
      JMenuItem editActionMenuItem = new JMenuItem("Edit semantic action");
//         , KeyEvent.VK_A);
//      editActionMenuItem.setAccelerator(KeyStroke.getKeyStroke(
//         KeyEvent.VK_A, ActionEvent.ALT_MASK));
      editActionMenuItem.addActionListener(this);
      add(editActionMenuItem);
      if (eNode.getGrammarNode().isFinal())
      {  JMenuItem editValueMenuItem = new JMenuItem("Edit semantic value");
//         , KeyEvent.VK_V);
//         editValueMenuItem.setAccelerator(KeyStroke.getKeyStroke(
//            KeyEvent.VK_V, ActionEvent.ALT_MASK));
         editValueMenuItem.addActionListener(this);
         add(editValueMenuItem);
      }
      JMenuItem initialMenuItem = new JMenuItem("Toggle Initial node");
//         , KeyEvent.VK_I);
//      initialMenuItem.setAccelerator(KeyStroke.getKeyStroke(
//         KeyEvent.VK_I, ActionEvent.ALT_MASK));
      initialMenuItem.addActionListener(this);
      add(initialMenuItem);
      JMenuItem finalMenuItem = new JMenuItem("Toggle Final node");
//         , KeyEvent.VK_F);
//      finalMenuItem.setAccelerator(KeyStroke.getKeyStroke(
//         KeyEvent.VK_F, ActionEvent.ALT_MASK));
      finalMenuItem.addActionListener(this);
      add(finalMenuItem);
      JMenuItem deleteMenuItem = new JMenuItem("Delete node");
//         , KeyEvent.VK_D);
//      deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(
//         KeyEvent.VK_D, ActionEvent.ALT_MASK));
      deleteMenuItem.addActionListener(this);
      add(deleteMenuItem);
   }

   public void actionPerformed(ActionEvent e)
   {  ASDEditor editor = eNode.getEditor();
      if (editor == null) return;
      String command = e.getActionCommand();
      if (command.equals("Edit semantic action"))
         editor.editAction(eNode);
      else if (command.equals("Edit semantic value"))
         editor.editValue(eNode);
      else if (command.equals("Toggle Initial node"))
         editor.toggleInitial(eNode);
      else if (command.equals("Toggle Final node"))
      {  editor.toggleFinal(eNode);
         eNode.changePopupMenu();
      }
      else if (command.equals("Delete node"))
         editor.deleteNode(eNode);
   }

   ASDEditNode eNode;
}
