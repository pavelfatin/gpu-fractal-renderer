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

import com.pavelfatin.fractal.picture.*;
import com.pavelfatin.fractal.renderer.Renderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;

import static java.lang.Math.abs;

class RenderedPicture extends AbstractPicture {
    private final PictureModel myModel;
    private final int[] myBuffer;
    private Renderer myRenderer;
    private boolean myIncremental = true;

    RenderedPicture(final PictureModel model, final Dimension maxSize) {
        myModel = model;
        myBuffer = new int[maxSize.width * maxSize.height];

        myModel.addModelListener(new MyModelListener());
    }

    public Renderer getRenderer() {
        return myRenderer;
    }

    public void setRenderer(final Renderer renderer) {
        myRenderer = renderer;
    }

    public boolean isIncremental() {
        return myIncremental;
    }

    public void setIncremental(final boolean incremental) {
        myIncremental = incremental;
    }

    private Update createChangeUpdate() {
        return createDrawUpdate(new Rectangle(new Point(0, 0), myModel.getViewSize()));
    }

    private Update createMoveUpdate(final int dx, final int dy) {
        final int w = abs(dx);
        final int h = abs(dy);

        final int width = myModel.getViewSize().width;
        final int height = myModel.getViewSize().height;

        if (w >= width || h >= height) {
            return createChangeUpdate();
        }

        final Rectangle source = new Rectangle(0, 0, width - w, height - h);
        final Rectangle hBlock = new Rectangle(0, 0, width, h);
        final Rectangle vBlock = new Rectangle(0, h, w, height - h);

        if (dx < 0) {
            hFlip(source);
            hFlip(vBlock);
        }

        if (dy < 0) {
            vFlip(source);
            vFlip(hBlock);
            vFlip(vBlock);
        }

        final Point destination = source.getLocation();
        destination.translate(dx, dy);

        final Collection<Update> updates = new LinkedList<Update>();
        updates.add(new CopyUpdate(source, destination));

        if (!hBlock.isEmpty()) updates.add(createDrawUpdate(hBlock));
        if (!vBlock.isEmpty()) updates.add(createDrawUpdate(vBlock));

        return new CompoundUpdate(updates);
    }

    private void hFlip(final Rectangle r) {
        r.setLocation(myModel.getViewSize().width - r.width - r.x, r.y);
    }

    private void vFlip(final Rectangle r) {
        r.setLocation(r.x, myModel.getViewSize().height - r.height - r.y);
    }

    private DrawUpdate createDrawUpdate(final Rectangle r) {
        final double realOffset = myModel.getRealOffset(r.x);
        final double imaginaryOffset = myModel.getImaginaryOffset(r.y);
        final double resolution = myModel.getResolution();

        myRenderer.render(myBuffer, r.width, r.height, realOffset, imaginaryOffset, resolution);
        colorize(myBuffer, r.width * r.height);

        final BufferedImage image = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, r.width, r.height, myBuffer, 0, r.width);

        return new DrawUpdate(image, r.getLocation());
    }

    private static void colorize(final int[] buffer, final int length) {
        final int alpha = 0xFF << 24;

        for (int i = 0; i < length; i++) {
            final int v = buffer[i];
            buffer[i] = (v > 0 ? (128 - 2 * v) << 8 : 0) | alpha;
        }
    }

    @Override
    public Update update(final Rectangle bounds) {
        return createDrawUpdate(bounds);
    }

    private class MyModelListener implements ModelListener {
        @Override
        public void onMove(final int dx, final int dy) {
            final long before = System.currentTimeMillis();
            final Update update = myIncremental ? createMoveUpdate(dx, dy) : createChangeUpdate();
            fireUpdate(update, System.currentTimeMillis() - before);
        }

        @Override
        public void onChange() {
            final long before = System.currentTimeMillis();
            final Update update = createChangeUpdate();
            fireUpdate(update, System.currentTimeMillis() - before);
        }

        @Override
        public void onResize(final Dimension oldSize, final Dimension newSize) {
            onChange();
        }
    }
}
