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
import java.awt.image.VolatileImage;

abstract class BufferedComponent extends JComponent {
    private VolatileImage myBuffer;
    private boolean myIsBufferDirty;

    @Override
    protected void paintComponent(final Graphics g) {
        createOrValidateBuffer();

        final Rectangle r = g.getClipBounds();

        final Graphics bufferGraphics = myBuffer.getGraphics();

        paintComponent(bufferGraphics, r, myIsBufferDirty);

        if (myIsBufferDirty) {
            myIsBufferDirty = false;
        }

        bufferGraphics.dispose();

        g.drawImage(myBuffer,
                r.x, r.y, r.x + r.width, r.y + r.height,
                r.x, r.y, r.x + r.width, r.y + r.height,
                null);
    }

    protected abstract void paintComponent(final Graphics g, final Rectangle r, final boolean complete);

    private void createOrValidateBuffer() {
        if (myBuffer == null) {
            myBuffer = createVolatileImage(getWidth(), getHeight());
            myIsBufferDirty = true;
        } else if (myBuffer.getWidth() != getWidth() || myBuffer.getHeight() != getHeight()) {
            myBuffer.flush();
            myBuffer = createVolatileImage(getWidth(), getHeight());
            myIsBufferDirty = true;
        } else {
            switch (myBuffer.validate(getGraphicsConfiguration())) {
                case VolatileImage.IMAGE_INCOMPATIBLE:
                    myBuffer.flush();
                    myBuffer = createVolatileImage(getWidth(), getHeight());
                case VolatileImage.IMAGE_RESTORED:
                    myIsBufferDirty = true;
            }
        }
    }
}
