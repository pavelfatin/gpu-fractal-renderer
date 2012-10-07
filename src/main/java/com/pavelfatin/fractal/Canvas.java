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

import com.pavelfatin.fractal.picture.Picture;
import com.pavelfatin.fractal.picture.PictureListener;
import com.pavelfatin.fractal.picture.Update;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;

class Canvas extends JComponent {
    private final Collection<Update> myUpdates = new LinkedList<Update>();
    private final Picture myPicture;

    Canvas(final Picture picture) {
        myPicture = picture;

        setOpaque(true);

        picture.addPictureListener(new PictureListener() {
            @Override
            public void onUpdate(final Update update, final long elapsedTime) {
                myUpdates.add(update);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(final Graphics g) {
        if (myUpdates.isEmpty()) {
            final Update update = myPicture.update(g.getClipBounds());
            update.apply(g);
        } else {
            for (final Update update : myUpdates) {
                update.apply(g);
            }
            myUpdates.clear();
        }
    }
}
