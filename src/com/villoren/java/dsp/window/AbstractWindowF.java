/*
 * AbstractWindowF
 *
 * Copyright (c) 2014 Renato Villone
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.villoren.java.dsp.window;

import com.villoren.java.dsp.fft.ComplexBufferF;

/**
 * Provides a base class for Window implementation.
 *
 * @author Renato Villone
 */
public abstract class AbstractWindowF {

    /**
     * Number of samples.
     */
    protected final int mSize;

    /**
     * Pre-calculated window factors.
     */
    protected final float[] mWindow;

    /**
     * Constructor. Allocates the array leaving the setting of its values to sub classes.
     *
     * @param size Size of this window.
     */
    public AbstractWindowF(int size) {

        mSize = size;
        mWindow = new float[mSize];
    }

    /**
     * Applies this window to the provided samples.
     * <p>
     * Array must be at least of <code>getSize()</code> length.
     *
     * @param real Input samples. Contains windowed samples upon return.
     */
    public void apply(float[] real) {

        for (int i = 0; i < mSize; i++)
            real[i] *= mWindow[i];
    }

    /**
     * Applies this window to the provided samples.
     * <p>
     * Arrays must be at least of <code>getSize()</code> length.
     *
     * @param real Input samples. Contains windowed samples upon return.
     * @param imag Input samples. Contains windowed samples upon return.
     */
    public void apply(float[] real, float[] imag) {

        for (int i = 0; i < mSize; i++) {

            real[i] *= mWindow[i];
            imag[i] *= mWindow[i];
        }
    }

    /**
     * Applies this window to the provided samples.
     * <p>
     * Buffer must be at least of <code>getSize()</code> length.
     *
     * @param complex Input samples. Contains windowed samples upon return.
     */
    public void apply(ComplexBufferF complex) {

        apply(complex.real, complex.imag);
    }

    /**
     * @return Gets the size of this window:
     * the number of samples (real or complex) to process in each <code>apply()</code> pass.
     */
    public int getSize() { return mSize; }
}
