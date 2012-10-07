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

inline unsigned int bound(const float2 seed) {
    float2 value = seed;

    unsigned int i = 1;

    while (i < 256) {
        const float2 sqr = value * value;

        value = (float2) (sqr.x - sqr.y, 2.0F * value.x * value.y) + seed;

        if (sqr.x + sqr.y >= 4.0F) return i;

        i++;
    }

    return 0;
}

kernel void generate(const float2 offset,
                     const float2 resolution,
                     const unsigned int width,
                     global int* out) {
    const int2 id = (int2) (get_global_id(0), get_global_id(1));

    const float2 z = offset + resolution * (float2)(id.x, id.y);

    out[id.y * width + id.x] = bound(z);
}

