/*
 * Copyright (C) 2012 Pavel Fatin <http://pavelfatin.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.pavelfatin.fractal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class Controller {
    private final JComponent myCanvas;
    private final PictureModel myModel;
    private final JScrollBar myHorizontalBar;
    private final JScrollBar myVerticalBar;

    private boolean myProcessScrollEvents = true;

    Controller(final PictureModel model, final JComponent canvas,
               final JScrollBar horizontalBar, final JScrollBar verticalBar) {
        myCanvas = canvas;
        myModel = model;
        myHorizontalBar = horizontalBar;
        myVerticalBar = verticalBar;

        final MyResizingListener resizingListener = new MyResizingListener();
        canvas.addComponentListener(resizingListener);

        final MyKeyListener keyListener = new MyKeyListener();
        canvas.addKeyListener(keyListener);

        final MyMouseListener mouseListener = new MyMouseListener();
        canvas.addMouseListener(mouseListener);
        canvas.addMouseWheelListener(mouseListener);
        canvas.addMouseMotionListener(mouseListener);

        final MyAdjustmentListener adjustmentListener = new MyAdjustmentListener();
        horizontalBar.addAdjustmentListener(adjustmentListener);
        verticalBar.addAdjustmentListener(adjustmentListener);
    }

    public void setProcessScrollEvents(final boolean processScrollEvents) {
        myProcessScrollEvents = processScrollEvents;
    }

    private class MyMouseListener extends MouseAdapter {
        private Point myOrigin;
        private boolean mySmoothScrolling;

        @Override
        public void mousePressed(final MouseEvent e) {
            myOrigin = e.getPoint();
            mySmoothScrolling = e.getButton() == MouseEvent.BUTTON1;
            myCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            if (!mySmoothScrolling) {
                handleDrag(myOrigin, e.getPoint());
            }
            myCanvas.setCursor(Cursor.getDefaultCursor());
        }

        private void handleDrag(final Point origin, final Point point) {
            myModel.move(point.x - origin.x, point.y - myOrigin.y);
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
            if (mySmoothScrolling) {
                final Point point = e.getPoint();
                handleDrag(myOrigin, point);
                myOrigin = point;
            }
        }

        @Override
        public void mouseWheelMoved(final MouseWheelEvent e) {
            if (e.getWheelRotation() > 0) {
                myModel.zoomOut();
            } else {
                myModel.zoomIn();
            }
        }
    }

    private class MyKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(final KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_R:
                    myModel.refresh();
                    break;
                case KeyEvent.VK_ESCAPE:
                    myModel.reset();
                    break;
                case KeyEvent.VK_PLUS:
                case KeyEvent.VK_SUBTRACT:
                case KeyEvent.VK_SPACE:
                    myModel.zoomOut();
                    break;
                case KeyEvent.VK_MINUS:
                case KeyEvent.VK_ADD:
                case KeyEvent.VK_ENTER:
                    myModel.zoomIn();
                    break;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    myModel.moveLeft();
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    myModel.moveRight();
                    break;
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    myModel.moveUp();
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    myModel.moveDown();
                    break;
            }
        }
    }

    private class MyResizingListener extends ComponentAdapter {
        @Override
        public void componentResized(final ComponentEvent e) {
            final Dimension size = myModel.getViewSize();

            if (!size.equals(myCanvas.getSize())) {
                myModel.setViewSize(myCanvas.getSize());
            }
        }
    }

    private class MyAdjustmentListener implements AdjustmentListener {
        @Override
        public void adjustmentValueChanged(final AdjustmentEvent e) {
            if (myProcessScrollEvents) {
                final int x = myHorizontalBar.getValue();
                final int y = myVerticalBar.getValue();
                myModel.setViewLocation(x, y);
            }
        }
    }
}
