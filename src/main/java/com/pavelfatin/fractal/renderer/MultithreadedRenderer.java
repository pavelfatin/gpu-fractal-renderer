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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class MultithreadedRenderer extends CPURenderer {
    private final int myThreadCount;
    private final ExecutorService myExecutor;

    MultithreadedRenderer(final int threadCount) {
        myThreadCount = threadCount;
        myExecutor = Executors.newFixedThreadPool(threadCount);
    }

    @Override
    public void render(final int[] buffer,
                       final int width,
                       final int height,
                       final double realOffset,
                       final double imaginaryOffset,
                       final double resolution) {
        final Future[] futures = new Future[myThreadCount];
        final int batchSize = height / myThreadCount;

        for (int i = 0; i < myThreadCount; i++) {
            final int y0 = batchSize * i;
            final int y1 = i < myThreadCount - 1 ? y0 + batchSize : height;

            futures[i] = myExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    render(buffer, width, y0, y1, realOffset, imaginaryOffset, resolution);
                }
            });
        }

        for (int i = 0; i < myThreadCount; i++) {
            try {
                futures[i].get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void dispose() {
        myExecutor.shutdown();
    }
}
