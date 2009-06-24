 /*
  * Unitex
  *
  * Copyright (C) 2001-2009 Universit� Paris-Est Marne-la-Vall�e <unitex@univ-mlv.fr>
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
  *
  */

package fr.umlv.unitex;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import fr.umlv.unitex.tfst.Bounds;

/**
 * This class describes a component on which a sentence graph can be drawn. 
 * @author S�bastien Paumier
 *
 */
public class TfstGraphicalZone
   extends GenericGraphicalZone
   implements Printable {

   /**
    * Constructs a new <code>FstGraphicalZone</code>. 
    * @param w width of the drawing area
    * @param h height of the drawing area
    * @param t text field to edit box contents
    * @param p frame that contains the component
    * @param listeners indicates if mouse listeners must be added to the component
    */
   public TfstGraphicalZone(
      int w,
      int h,
      TfstTextField t,
      TextAutomatonFrame p,
      boolean listeners) {
      super(w, h, t, p);
      if (listeners) {
         addMouseListener(new FstGraphMouseListener());
      }
   }

   protected void init() {
      for (int i= 0; i < graphBoxes.size(); i++) {
         TfstGraphBox g= (TfstGraphBox)graphBoxes.get(i);
         g.context= (Graphics2D)this.getGraphics();
         g.parentGraphicalZone= this;
         g.update();
      }
   }


   protected GenericGraphBox createBox(int x, int y) {
      TfstGraphBox g= new TfstGraphBox(x, y, 2,this);
      g.setContent("<E>");
      addBox(g);
      return g;
   }

   protected GenericGraphBox newBox(
      int x,
      int y,
      int type,
      GenericGraphicalZone p) {
      return new TfstGraphBox(x, y, type, (TfstGraphicalZone)p);
   }
   class FstGraphMouseListener extends MouseAdapter {

      MouseMotionListener motionListener= new FstGraphMouseMotionListener();

      public void mouseClicked(MouseEvent e) {
         int boxSelected;
         TfstGraphBox b;
         if (e.isShiftDown()) {
            // Shift+click
            // reverse transitions
            boxSelected=
               getSelectedBox(
                  (int) (e.getX() / scaleFactor),
                  (int) (e.getY() / scaleFactor));
            if (boxSelected != -1) {
               // if we click on a box
               b= (TfstGraphBox)graphBoxes.get(boxSelected);
               if (!selectedBoxes.isEmpty()) {
                  // if there are selected boxes, we rely them to the current
                  addReverseTransitionsFromSelectedBoxes(b);
                  unSelectAllBoxes();
                  setModified(true);
               }
            } else {
               // simple click not on a box
               initText("");
               TextAutomatonFrame.frame.text.select(0,0);
               TextAutomatonFrame.frame.bounds.reset();
               unSelectAllBoxes();
            }
         } else
            if (e.isControlDown()) {
               // Control+click
               // creation of a new box
               b=
                  (TfstGraphBox)createBox((int) (e.getX() / scaleFactor),
                     (int) (e.getY() / scaleFactor));
               setModified(true);
               // if some boxes are selected, we rely them to the new one
               if (!selectedBoxes.isEmpty()) {
                  addTransitionsFromSelectedBoxes(b,true);
               }
               // then, the only selected box is the new one
               unSelectAllBoxes();
               b.setSelected(true);
               b.setContent("<E>");
               selectedBoxes.add(b);
               initText("<E>");
            } else {
               boxSelected=
                  getSelectedBox(
                     (int) (e.getX() / scaleFactor),
                     (int) (e.getY() / scaleFactor));
               if (boxSelected != -1) {
                  // if we click on a box
                  b= (TfstGraphBox)graphBoxes.get(boxSelected);
                  if (!selectedBoxes.isEmpty()) {
                     // if there are selected boxes, we rely them to the current
                     addTransitionsFromSelectedBoxes(b,true);
                     unSelectAllBoxes();
                     setModified(true);
                  } else {
                     // if not, we just select this one
                     b.setSelected(true);
                     selectedBoxes.add(b);
                     initText(b.getContent());
                  }
               } else {
                  // simple click not on a box
                  unSelectAllBoxes();
                  TextAutomatonFrame.frame.text.select(0,0);
                  TextAutomatonFrame.frame.bounds.reset();
                  initText("");
                  TextAutomatonFrame.frame.bounds.reset();
               }
            }
         repaint();
         return;
      }

      public void mousePressed(MouseEvent e) {
         int selectedBox;
         addMouseMotionListener(motionListener);
         if (e.isShiftDown() || e.isAltDown() || e.isControlDown()) {
            return;
         }
         /*if (!validateTextField()) {
             return;
         }*/
         validateTextField();
         X_start_drag= (int) (e.getX() / scaleFactor);
         Y_start_drag= (int) (e.getY() / scaleFactor);
         X_end_drag= X_start_drag;
         Y_end_drag= Y_start_drag;
         X_drag= X_start_drag;
         Y_drag= Y_start_drag;
         dragWidth= 0;
         dragHeight= 0;
         selectedBox= getSelectedBox(X_start_drag, Y_start_drag);
         singleDragging= false;
         dragging= false;
         selecting= false;
         // TODO bug: les boites s�lectionn�es disparaissent quand on clique pour les d�s�lectionner
         if (selectedBox != -1) {
            // if we start dragging a box
            singleDraggedBox= graphBoxes.get(selectedBox);
            initText(singleDraggedBox.content);
            if (!singleDraggedBox.isSelected()) {
               dragging= true;
               singleDragging= true;
               singleDraggedBox.singleDragging= true;
            }
         }
         if (!selectedBoxes.isEmpty()) {
            dragging= true;
         }
         if ((selectedBox == -1) && selectedBoxes.isEmpty()) {
            // being drawing a selection rectangle
            dragging= false;
            selecting= true;
            initText("");
         }
         repaint();
      }

      public void mouseReleased(MouseEvent e) {
         removeMouseMotionListener(motionListener);
         if (e.isShiftDown() || e.isAltDown() || e.isControlDown())
            return;
         dragging= false;
         initText("");
         TextAutomatonFrame.frame.bounds.reset();
         if (singleDragging) {
            singleDragging= false;
            singleDraggedBox.singleDragging= false;
         } else
            if (selecting == true) {
               selectByRectangle(X_drag, Y_drag, dragWidth, dragHeight);
               text.setEditable(true);
               selecting= false;
            }
         repaint();
      }

   }

   class FstGraphMouseMotionListener extends MouseMotionAdapter {
      public void mouseDragged(MouseEvent e) {
         int Xtmp= X_end_drag;
         int Ytmp= Y_end_drag;
         X_end_drag= (int) (e.getX() / scaleFactor);
         Y_end_drag= (int) (e.getY() / scaleFactor);
         int dx= X_end_drag - Xtmp;
         int dy= Y_end_drag - Ytmp;

         if (singleDragging) {
            // translates the single dragged box
            singleDraggedBox.translate(dx, dy);
            setModified(true);
         }
         if (dragging) {
            // translates all the selected boxes
            setModified(true);
            translateAllSelectedBoxes(dx, dy);
            // if we were dragging, we have nothing else to do
            repaint();
            return;
         }

         if (X_start_drag < X_end_drag) {
            X_drag= X_start_drag;
            dragWidth= X_end_drag - X_start_drag;
         } else {
            X_drag= X_end_drag;
            dragWidth= X_start_drag - X_end_drag;
         }
         if (Y_start_drag < Y_end_drag) {
            Y_drag= Y_start_drag;
            dragHeight= Y_end_drag - Y_start_drag;
         } else {
            Y_drag= Y_end_drag;
            dragHeight= Y_start_drag - Y_end_drag;
         }
         repaint();
      }

   }


   /* end of events handling */


   /**
    * Draws the graph. This method should only be called by the virtual machine.
    * @param f_old the graphical context 
    */
   public void paintComponent(Graphics f_old) {
      Graphics2D f= (Graphics2D)f_old;
      if (pref.antialiasing) {
         f.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
      } else {
         f.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_OFF);
      }
      f.setColor(new Color(205, 205, 205));
      f.fillRect(0, 0, getWidth(), getHeight());
      f.setColor(pref.backgroundColor);
      f.fillRect(0, 0, Width, Height);
      f.setColor(pref.foregroundColor);
      f.drawRect(10, 10, Width - 20, Height - 20);
      f.drawRect(9, 9, Width - 18, Height - 18);
      f.setColor(pref.foregroundColor);
      if (graphBoxes.size() == 0 || graphBoxes.isEmpty()) {
         return;
      }
      if (!is_initialised) {
         this.init();
         is_initialised= true;
      }
      f.setColor(new Color(205, 205, 205));
      f.fillRect(0, 0, getWidth(), getHeight());
      f.setColor(pref.backgroundColor);
      f.fillRect(0, 0, Width, Height);
      f.setColor(pref.foregroundColor);
      f.drawRect(10, 10, Width - 20, Height - 20);
      f.drawRect(9, 9, Width - 18, Height - 18);
      f.setColor(pref.foregroundColor);
      drawAllTransitions(f);
      drawAllBoxes(f);
      if (selecting) {
         // here we draw the selection rectangle
         f.setColor(pref.foregroundColor);
         f.drawRect(X_drag, Y_drag, dragWidth, dragHeight);
      }
   }

   
   /**
    * Prints the graph.
    * @param g the graphical context
    * @param p the page format
    * @param pageIndex the page index 
    */
   public int print(Graphics g, PageFormat p, int pageIndex) {
      if (pageIndex != 0)
         return Printable.NO_SUCH_PAGE;
      Graphics2D f= (Graphics2D)g;
      double DPI= 96.0;
      //(double)Toolkit.getDefaultToolkit().getScreenResolution();
      double WidthInInches= p.getImageableWidth() / 72;
      double realWidthInInches= (Width / DPI);
      double HeightInInches= p.getImageableHeight() / 72;
      double realHeightInInches= (Height / DPI);

      double scale_x= WidthInInches / realWidthInInches;
      double scale_y= HeightInInches / realHeightInInches;

      f.translate(p.getImageableX(), p.getImageableY());
      if (scale_x < scale_y)
         f.scale(0.99 * 0.72 * scale_x, 0.99 * 0.72 * scale_x);
      else
         f.scale(0.99 * 0.72 * scale_y, 0.99 * 0.72 * scale_y);
      f.setColor(pref.backgroundColor);
      f.fillRect(0, 0, Width, Height);
      f.setColor(pref.foregroundColor);
      f.drawRect(10, 10, Width - 20, Height - 20);
      f.drawRect(9, 9, Width - 18, Height - 18);
      f.setColor(pref.foregroundColor);
      drawAllTransitions(f);
      drawAllBoxes(f);
      if (selecting) {
         // here we draw the selection rectangle
         f.drawRect(X_drag, Y_drag, dragWidth, dragHeight);
      }
      return Printable.PAGE_EXISTS;
   }



/*
public void startBoxEdition(GenericGraphBox box) {
    TfstGraphBox box2=(TfstGraphBox)box;
    ((FstTextField)text).initText(box.getContent());
    TextAutomatonFrame.frame.bounds.setEnabled(true);
    TextAutomatonFrame.frame.validateBoxEdition.setEnabled(true);
    TextAutomatonFrame.frame.text.getCaret().setSelectionVisible(true);
    TextAutomatonFrame.frame.bounds.setEditable(true);
    if (box2.getStart()!=-1 && box2.getEnd()!=-1) {
        TextAutomatonFrame.frame.text.select(box2.getStart(),box2.getEnd());
    }
    modified=false;
}*/

/*
public void boxEditionHasEnded() {
    text.setText("");
    text.setEnabled(false);
    TextAutomatonFrame.frame.bounds.setText("");
    TextAutomatonFrame.frame.bounds.setEnabled(false);
    TextAutomatonFrame.frame.validateBoxEdition.setEnabled(false);
    TextAutomatonFrame.frame.text.select(0,0);
    modified=false;
}*/

   public void setBoundsForSelected(Bounds b) {
       int i, L;
       TfstGraphBox g;
       if (selectedBoxes.isEmpty()) return;
       L = selectedBoxes.size();
       for (i = 0; i < L; i++) {
           g = (TfstGraphBox) selectedBoxes.get(i);
           g.setBounds(b);
       }
   }

} /* end of TfstGraphicalZone */