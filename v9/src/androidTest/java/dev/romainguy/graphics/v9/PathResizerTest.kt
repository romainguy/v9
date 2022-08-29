/*
 * Copyright (C) 2022 Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.romainguy.graphics.v9

import android.graphics.Rect
import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PathResizerTest {
    @Test
    fun emptySlice() {
        val slice = Slice(0.0f, 0.0f)
        assertEquals(0.0f, slice.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun startMustBeLessThanEnd() {
        val slice = Slice(2.0f, 1.0f)
        assertEquals(1.0f, slice.size)
    }

    @Test
    fun sliceFromIntRect() {
        val slices = Slices(Rect(0, 0, 4, 4))
        assertEquals(1, slices.verticalSlices.size)
        assertEquals(Slice(0.0f, 4.0f), slices.verticalSlices[0])
        assertEquals(1, slices.horizontalSlices.size)
        assertEquals(Slice(0.0f, 4.0f), slices.horizontalSlices[0])
    }

    @Test
    fun sliceFromInt() {
        val slices = Slices(0, 0, 4, 4)
        assertEquals(1, slices.verticalSlices.size)
        assertEquals(Slice(0.0f, 4.0f), slices.verticalSlices[0])
        assertEquals(1, slices.horizontalSlices.size)
        assertEquals(Slice(0.0f, 4.0f), slices.horizontalSlices[0])
    }

    @Test
    fun sliceFromFloatRect() {
        val slices = Slices(RectF(0.0f, 0.0f, 4.0f, 4.0f))
        assertEquals(1, slices.verticalSlices.size)
        assertEquals(Slice(0.0f, 4.0f), slices.verticalSlices[0])
        assertEquals(1, slices.horizontalSlices.size)
        assertEquals(Slice(0.0f, 4.0f), slices.horizontalSlices[0])
    }

    @Test
    fun sliceFromFloat() {
        val slices = Slices(0.0f, 0.0f, 4.0f, 4.0f)
        assertEquals(1, slices.verticalSlices.size)
        assertEquals(Slice(0.0f, 4.0f), slices.verticalSlices[0])
        assertEquals(1, slices.horizontalSlices.size)
        assertEquals(Slice(0.0f, 4.0f), slices.horizontalSlices[0])
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyVerticalSlices() {
        Slices(listOf(), listOf(Slice(0.0f, 1.0f)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyHorizontalSlices() {
        Slices(listOf(Slice(0.0f, 1.0f)), listOf())
    }

    @Test
    fun slices() {
        val slices = Slices(
            listOf(Slice(0.0f, 1.0f), Slice(2.0f, 3.0f)),
            listOf(Slice(0.0f, 1.0f), Slice(2.0f, 3.0f))
        )
        assertEquals(2, slices.verticalSlices.size)
        assertEquals(2, slices.horizontalSlices.size)
    }

    @Test
    fun filterEmptySlices() {
        val slices = Slices(
            listOf(Slice(0.0f, 0.0f), Slice(2.0f, 3.0f)),
            listOf(Slice(0.0f, 0.0f), Slice(2.0f, 3.0f))
        )
        assertEquals(1, slices.verticalSlices.size)
        assertEquals(1, slices.horizontalSlices.size)
    }

    // TODO: test path resizing
}
