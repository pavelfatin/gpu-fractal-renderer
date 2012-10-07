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

import com.nativelibs4java.opencl.*;
import com.pavelfatin.fractal.Utilities;
import org.bridj.Pointer;

class OpenCLRenderer implements Renderer {
    private static final String FILE_NAME = "OpenCLRenderer.cl";
    private static final String FUNCTION_NAME = "generate";

    private final CLContext myContext;
    private final CLProgram myProgram;
    private final CLKernel myKernel;
    private final CLQueue myQueue;
    private final CLBuffer<Integer> myResultsBuffer;
    private final Pointer<Integer> myPointer;

    OpenCLRenderer(final int maxWidth, final int maxHeight) {
        myContext = JavaCL.createBestContext();

        myProgram = myContext.createProgram(Utilities.load(getClass(), FILE_NAME));
        myKernel = myProgram.createKernel(FUNCTION_NAME);
        myQueue = myContext.createDefaultQueue();
        myResultsBuffer = myContext.createIntBuffer(CLMem.Usage.Output, maxWidth * maxHeight);
        myPointer = Pointer.allocateInts(maxWidth * maxHeight);
    }

    @Override
    public void render(final int[] buffer,
                       final int width,
                       final int height,
                       final double realOffset,
                       final double imaginaryOffset,
                       final double resolution) {
        myKernel.setArg(0, new float[]{(float) realOffset, (float) imaginaryOffset});
        myKernel.setArg(1, new float[]{(float) resolution, (float) resolution});
        myKernel.setArg(2, width);
        myKernel.setArg(3, myResultsBuffer);

        final CLEvent event = myKernel.enqueueNDRange(myQueue, new int[]{width, height});

        final int length = width * height;

        myResultsBuffer.read(myQueue, 0, length, myPointer, true, event);
        myPointer.getIntBuffer().get(buffer, 0, length);

        event.release();
    }

    @Override
    public void dispose() {
        myPointer.release();
        myResultsBuffer.release();
        myQueue.release();
        myKernel.release();
        myProgram.release();
        myContext.release();
    }

    public static boolean isSupported() {
        try {
            JavaCL.listPlatforms();
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }
}
