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

import com.nativelibs4java.util.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class Utilities {
    private Utilities() {
    }

    public static String load(final Class aClass, final String file) {
        try {
            final URL url = aClass.getResource(file);
            if (url == null) {
                throw new FileNotFoundException(file);
            }
            return IOUtils.readText(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
