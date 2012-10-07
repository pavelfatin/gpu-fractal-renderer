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

import java.awt.*;

import static java.lang.Math.*;

class PictureModel extends AbstractPictureModel {
    private static final double LEFT = -2.5D;
    private static final double RIGHT = 1.0D;

    private static final double TOP = -1.7D;
    private static final double BOTTOM = 1.7D;

    private static final double SCALE_FACTOR = 1.2D;
    private static final double SHIFT_FACTOR = 0.1D;

    private Dimension myViewSize = new Dimension(800, 700);

    private double myRealPartCenter;
    private double myImaginaryPartCenter;
    private double myResolution;

    PictureModel() {
        reset();
    }

    public Dimension getFullSize() {
        final double width = (RIGHT - LEFT) / myResolution;
        final double height = (BOTTOM - TOP) / myResolution;

        return new Dimension((int) round(width), (int) round(height));
    }

    public Dimension getViewSize() {
        return myViewSize;
    }

    public void setViewSize(final Dimension size) {
        final Dimension oldSize = getViewSize();

        myViewSize = size;
        myResolution /= ((double) size.width / oldSize.width);

        fireResized(oldSize, getViewSize());
    }

    public Point getViewLocation() {
        final double x = (myRealPartCenter - LEFT) / myResolution - myViewSize.width / 2.0D;
        final double y = (myImaginaryPartCenter - TOP) / myResolution - myViewSize.height / 2.0D;

        return new Point((int) round(x), (int) round(y));
    }

    public Rectangle getViewBounds() {
        return new Rectangle(getViewLocation(), getViewSize());
    }

    public void setViewLocation(final int x, final int y) {
        final Point location = getViewLocation();
        move(location.x - x, location.y - y);
    }

    public double getRealPartCenter() {
        return myRealPartCenter;
    }

    public double getImaginaryPartCenter() {
        return myImaginaryPartCenter;
    }

    public double getResolution() {
        return myResolution;
    }

    public double getRealOffset(final int x) {
        return myRealPartCenter - myResolution * (myViewSize.width / 2.0D - x);
    }

    public double getImaginaryOffset(final int y) {
        return myImaginaryPartCenter - myResolution * (myViewSize.height / 2.0D - y);
    }

    public void move(final int dx, final int dy) {
        final Dimension size = getFullSize();
        final Rectangle r = getViewBounds();

        final int xShift = dx > 0 ? Math.min(r.x, dx) : Math.max(r.width + r.x - size.width, dx);
        final int yShift = dy > 0 ? Math.min(r.y, dy) : Math.max(r.height + r.y - size.height, dy);

        doMove(xShift, yShift);
    }

    private void doMove(final int dx, final int dy) {
        if (dx == 0 && dy == 0) return;

        myRealPartCenter -= myResolution * dx;
        myImaginaryPartCenter -= myResolution * dy;

        fireMoved(dx, dy);
    }

    public void refresh() {
        fireChanged();
    }

    public void zoomOut() {
        final double maxResolution = (RIGHT - LEFT) / getViewSize().width;

        if (myResolution < maxResolution) {
            myResolution = min(myResolution * SCALE_FACTOR, maxResolution);

            final Dimension size = getFullSize();
            final Rectangle r = getViewBounds();

            myRealPartCenter -= myResolution * excess(r.x, 0, size.width - r.width);
            myImaginaryPartCenter -= myResolution * excess(r.y, 0, size.height - r.height);

            fireChanged();
        }
    }

    private static int excess(final int value, final int lowerBound, final int upperBound) {
        return value - Math.min(Math.max(value, lowerBound), upperBound);
    }

    public void zoomIn() {
        final double minResolution = (RIGHT - LEFT) / Integer.MAX_VALUE;

        if (myResolution > minResolution) {
            myResolution = max(myResolution / SCALE_FACTOR, minResolution);

            fireChanged();
        }
    }

    public void reset() {
        myRealPartCenter = (RIGHT + LEFT) / 2.0D;
        myImaginaryPartCenter = (BOTTOM + TOP) / 2.0D;
        myResolution = (RIGHT - LEFT) / myViewSize.getWidth();

        fireChanged();
    }

    public void moveLeft() {
        move(shift(), 0);
    }

    public void moveRight() {
        move(-shift(), 0);
    }

    public void moveUp() {
        move(0, shift());
    }

    public void moveDown() {
        move(0, -shift());
    }

    private int shift() {
        return (int) (myViewSize.width * SHIFT_FACTOR);
    }
}
