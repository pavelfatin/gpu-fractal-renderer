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

package com.pavelfatin.fractal.renderer;

abstract class CPURenderer implements Renderer {
    protected static void render(final int[] buffer,
                                 final int width,
                                 final int y0,
                                 final int y1,
                                 final double xOffset,
                                 final double yOffset,
                                 final double resolution) {
        double a;
        double b = yOffset + y0 * resolution;

        int x;
        int i = y0 * width;

        for (int y = y0; y < y1; y++) {
            a = xOffset;
            for (x = 0; x < width; x++) {
                buffer[i] = bound(a, b);
                a += resolution;
                i++;
            }
            b += resolution;
        }
    }

    private static int bound(final double aSeed, final double bSeed) {
        double aValue = aSeed;
        double bValue = bSeed;

        int i = 1;

        double aSqr;
        double bSqr;

        while (i < 256) {
            aSqr = aValue * aValue;
            bSqr = bValue * bValue;

            bValue = 2.0D * aValue * bValue + bSeed;
            aValue = aSqr - bSqr + aSeed;

            if (aSqr + bSqr >= 4.0D) return i;

            i++;
        }

        return 0;
    }
}
