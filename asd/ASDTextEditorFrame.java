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

import java.awt.*;       // BorderLayout, Container, etc.
                         // Graphics2D, Rectangle, Color
import java.awt.event.*; // ActionEvent, ActionListener,
                         // WindowAdapter, WindowEvent
import javax.swing.*;    // JFrame, JPanel, JButton

/**
   Defines the window in which semantic action and semantic value
   strings for nodes in an ASD grammar are displayed and edited.

   @author James A. Mason
   @version 1.02 2003 Jul
 */
class ASDTextEditorFrame extends JFrame
{  ASDTextEditorFrame(ASDEditor ed, String text)
   {  addWindowListener(new WindowCloser(this));
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      editor = ed;
      textArea = new JTextArea(text);
      JScrollPane scrollPane = new JScrollPane(textArea);
      scrollPane.setPreferredSize(new Dimension(500, 20));
      setSize(DEFAULT_FRAME_WIDTH, DEFAULT_FRAME_HEIGHT);
      setLocation(DEFAULT_X, DEFAULT_Y);
      getContentPane().add(scrollPane);
   }

   ASDEditor getEditor() { return editor; }

   String getText() { return textArea.getText(); }

   ASDEditor editor;
   JTextArea textArea;
   final static int DEFAULT_FRAME_WIDTH = 600;
   final static int DEFAULT_FRAME_HEIGHT = 70;
   final static int DEFAULT_X = 100;
   final static int DEFAULT_Y = 100;

   private class WindowCloser extends WindowAdapter
   {  public WindowCloser(ASDTextEditorFrame w)
      {  window = w;
      }

      public void windowClosing(WindowEvent e)
      {  ASDEditor editor = window.getEditor();
         editor.changedText(window.getText());
      }

      private ASDTextEditorFrame window;
   }
}


