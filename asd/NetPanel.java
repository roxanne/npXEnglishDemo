/*

Copyright 2003 James A. Mason

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

import java.util.*;        // HashSet, Iterator
import java.awt.*;         // Graphics, Color
import javax.swing.*;
import java.awt.event.*; // ActionEvent, ActionListener,
import java.lang.reflect.*; // for various exceptions

/**
   Defines panels in which an ASD Grammar network is displayed.
 */
class NetPanel extends JPanel
{
   /**
      Constructs an empty NetPanel with a green background.
    */
   public NetPanel(ASDEditor e)
   {  backgroundColor = Color.green;
      setBackground(backgroundColor);
      editor = e;
      displayedEdges = new HashSet();
      displayedNodes = new HashSet();
      NetPanelMenu menu = new NetPanelMenu(this, editor);
      MouseListener popupListener = new PopupListener(menu);
      addMouseListener(popupListener);
   }

   public void addEdge(ASDEditEdge eEdge)
   {  eEdge.setEditor(editor);
      add(eEdge);
      displayedEdges.add(eEdge);
      eEdge.setContext(this);
   }

   public void addNode(ASDEditNode eNode)
   {  eNode.setEditor(editor);
      add(eNode, 0);
      if (eNode.getRightLabel() != null)
      {  add(eNode.getRightLabel(), 0);
      }
      displayedNodes.add(eNode);
      eNode.setContext(this);
   }

   void clear()
   {  removeAll();
      displayedEdges = new HashSet();
      displayedNodes = new HashSet();
      repaint();
   }

   public Set getDisplayedEdges()
   {  return displayedEdges;
   }

   public ASDEditor getEditor() { return editor; }
//   public ASDEditorFrame getWindow() { return window; }

   boolean isDisplaying(ASDEditEdge e)
   {  return displayedEdges.contains(e);
   }

   boolean isDisplaying(ASDEditNode n)
   {  return displayedNodes.contains(n);
   }

   public void paintComponent(Graphics g)
   {  super.paintComponent(g);
      if (displayedEdges != null)
      {  int buttonRadius = ASDEditEdge.SIZE / 2;
         for (Iterator it = displayedEdges.iterator(); it.hasNext(); )
         {  ASDEditEdge eEdge = (ASDEditEdge) it.next();
            ASDEditNode fromNode = eEdge.getFromNode();
            ASDEditNode toNode = eEdge.getToNode();

            g.drawLine(fromNode.rightConnectorX(),
               fromNode.rightConnectorY(),
               eEdge.getX()+ buttonRadius, eEdge.getY()+buttonRadius);
            //   eEdge.getX()+ (3*buttonRadius/2), eEdge.getY()+buttonRadius);
            // 3*buttonRadius/2 needed for some systems
            // g.drawLine(eEdge.getX()+ (3*buttonRadius/2), eEdge.getY()+buttonRadius,
            g.drawLine(eEdge.getX()+ buttonRadius, eEdge.getY()+buttonRadius,
               toNode.leftConnectorX(),
               toNode.leftConnectorY());
         }
      }
   }

   void print()
   {  PrintJob job
         = Toolkit.getDefaultToolkit().getPrintJob(editor.getWindow(),
            editor.getInputFileName(), null);
      if (job != null)
      {  Graphics g = job.getGraphics();
         if (g != null)
         {  setBackground(Color.white);
            paintComponent(g);
            print(g);
            g.dispose();
         }
         job.end();
         setBackground(Color.green);
      }
   }

   public void removeEdge(ASDEditEdge eEdge)
   {  displayedEdges.remove(eEdge);
      remove(eEdge);
      eEdge.setContext(null);
      repaint();
   }

   public void removeNode(ASDEditNode eNode)
   {  remove(eNode);
      if (eNode.getRightLabel() != null)
         remove(eNode.getRightLabel());
      displayedNodes.remove(eNode);
      eNode.setContext(null);
      repaint();
   }

   void straightenEdges()
   {  for (Iterator it = displayedEdges.iterator(); it.hasNext(); )
         ((ASDEditEdge) it.next()).setDefaultCoordinates();
      editor.setGrammarChanged(true);
      repaint();
   }

   void toggleBackgroundColor()
   {  if (backgroundColor == Color.green)
         backgroundColor = Color.white;
      else
         backgroundColor = Color.green;
      setBackground(backgroundColor);
   }

   private ASDEditor editor;
   private HashSet displayedEdges; // the ASDEditEdges currently
                                   // displayed in the NetPanel
   private HashSet displayedNodes; // the ASDEditNodes currently
                                   // displayed in the NetPanel
   Color backgroundColor;  // current color of the panel background
} // end class NetPanel

class NetPanelMenu extends JPopupMenu implements ActionListener
{  NetPanelMenu(NetPanel p, ASDEditor ed)
   {  panel = p;
      editor = ed;
      setInvoker(panel);

      JMenu showSubMenu = new JMenu("Show");
      JMenu hideSubMenu = new JMenu("Hide");

      JMenuItem addEdgeMenuItem = new JMenuItem("Add edge");
      addEdgeMenuItem.addActionListener(this);
      add(addEdgeMenuItem);
      JMenuItem deleteEdgeMenuItem = new JMenuItem("Delete selected edge");
      deleteEdgeMenuItem.addActionListener(this);
      add(deleteEdgeMenuItem);
      addSeparator();
      JMenuItem straightenMenuItem = new JMenuItem("Straighten edges");
      straightenMenuItem.addActionListener(this);
      add(straightenMenuItem);

      add(showSubMenu);
      JMenuItem allMenuItem = new JMenuItem("All nodes and edges");
      showSubMenu.add(allMenuItem);
      allMenuItem.addActionListener(this);
      JMenuItem nonSingletonItem = new JMenuItem(
         "all Non-singleton nodes and edges");
      showSubMenu.add(nonSingletonItem);
      nonSingletonItem.addActionListener(this);
      JMenuItem singletonItem = new JMenuItem("all Singleton nodes");
      showSubMenu.add(singletonItem);
      singletonItem.addActionListener(this);

      add(hideSubMenu);
      JMenuItem entireMenuItem = new JMenuItem("Entire network display");
      hideSubMenu.add(entireMenuItem);
      entireMenuItem.addActionListener(this);
      hideSubMenu.addSeparator();
      JMenuItem nonSingletonMenuItem = new JMenuItem("Non-singleton nodes");
      hideSubMenu.add(nonSingletonMenuItem);
      nonSingletonMenuItem.addActionListener(this);
      JMenuItem singletonMenuItem = new JMenuItem("Singleton nodes");
      hideSubMenu.add(singletonMenuItem);
      singletonMenuItem.addActionListener(this);
      JMenuItem selectedMenuItem = new JMenuItem("selected Component(s)");
      hideSubMenu.add(selectedMenuItem);
      selectedMenuItem.addActionListener(this);

      addSeparator();
      JMenuItem toggleMenuItem = new JMenuItem("Toggle background color");
      add(toggleMenuItem);
      toggleMenuItem.addActionListener(this);

      addSeparator();
      JMenuItem printMenuItem = new JMenuItem("Print the diagram");
      add(printMenuItem);
      printMenuItem.addActionListener(this);
   }

   public void actionPerformed(ActionEvent e)
   {  if (editor == null) return;
      String command = e.getActionCommand();
      if (command.equals("Add edge"))
         try
         {  editor.addEdge();
         }
         catch(ClassNotFoundException ex) {}
         catch(InvocationTargetException ex) {}
         catch(InstantiationException ex) {}
         catch(IllegalAccessException ex) {}
      else if (command.equals("Delete selected edge"))
         editor.deleteEdge();
      else if (command.equals("All nodes and edges"))
         editor.showAll();
      else if (command.equals("all Non-singleton nodes and edges"))
         editor.showAllNonSingletons();
      else if (command.equals("all Singleton nodes"))
         editor.showAllSingletons();
      else if (command.equals("Entire network display"))
         editor.clearNetPanel();
      else if (command.equals("selected Component(s)"))
         editor.hideSelectedComponents();
      else if (command.equals("Singleton nodes"))
         editor.hideSingletons();
      else if (command.equals("Non-singleton nodes"))
         editor.hideNonSingletons();
      else if (command.equals("Straighten edges"))
         panel.straightenEdges();
      else if (command.equals("Toggle background color"))
         panel.toggleBackgroundColor();
      else if (command.equals("Print the diagram"))
         panel.print();
   }

   ASDEditor editor;
   NetPanel panel; // the lower pane to which the menu is attached.
} // end class NetPanelMenu
