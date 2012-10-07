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
import java.util.Deque;
import java.util.LinkedList;

abstract class AbstractPictureModel {
    private final Deque<ModelListener> myListeners = new LinkedList<ModelListener>();

    public void addModelListener(final ModelListener listener) {
        myListeners.addFirst(listener);
    }

    protected void fireMoved(final int dx, final int dy) {
        for (final ModelListener listener : myListeners) {
            listener.onMove(dx, dy);
        }
    }

    protected void fireChanged() {
        for (final ModelListener listener : myListeners) {
            listener.onChange();
        }
    }

    protected void fireResized(final Dimension oldSize, final Dimension newSize) {
        for (final ModelListener listener : myListeners) {
            listener.onResize(oldSize, newSize);
        }
    }
}
