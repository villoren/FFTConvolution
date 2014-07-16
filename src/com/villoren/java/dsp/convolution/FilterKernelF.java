/*
 * FilterKernelF
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

package com.villoren.java.dsp.convolution;

import com.villoren.java.dsp.window.AbstractWindowF;
import com.villoren.java.dsp.fft.ComplexBufferF;

/**
 * Provides a <code>ComplexBuffer</code> representing a filter kernel.
 *
 * @author Renato Villone
 */
public class FilterKernelF extends ComplexBufferF {

    /**
     * <code>Convolution</code> for which this instance was created.
     */
    private final AbstractConvolutionF mConvolution;

    /**
     * Window for impulse response.
     */
    private AbstractWindowF mWindow;

    /**
     * Constructs a filter kernel to be used by the given convolution.
     * <p>
     * The kernel will be initialized to the 'identity' delta function.
     *
     * @param convolution Convolution that will use this kernel.
     */
    public FilterKernelF(AbstractConvolutionF convolution) {

        super(convolution.getFftSize());

        mConvolution = convolution;
        mWindow = mConvolution.getDefaultWindow();

        // Init to identity delta function
        real[0] = 1.0f;
    }

    /**
     * Sets the class of a window's implementation to be used for the impulse response.
     *
      * @param windowClass A class extending <code>AbstractWindow</code>.
     */
    public void setWindowClass(Class<? extends AbstractWindowF> windowClass) {

        try {

            mWindow = windowClass.getConstructor(int.class).newInstance(mConvolution.getWindowSize());

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    /**
     * Updates this kernel's impulse response to reflect the given frequency response.
     * <p>
     * The IR will be centered in the first half of this signal, windowed by size / 2 + 1 and
     * remaining points will be zeroed. This is to prevent aliasing, circular convolution.
     * <p>
     * After this process, the kernel is ready to be convolved.
     * <p>
     * You might want to zero the imaginary part, to take advantage of both real and imaginary parts in
     * each convolution step, for instance, to convolveFreqDomain 2-channel (stereo) audio signals.
     *
     * @param frequencyResponse Desired frequency response.
     * @return Reference to this instance with updated, anti-aliased impulse response.
     */
    public FilterKernelF setFrequencyResponse(FrequencyResponseF frequencyResponse) {

        // Return to time domain
        mConvolution.getFourierTransform().transform(frequencyResponse, this, true);

        // Impulse response in centered 'around' index 0,
        // center in the first half
        shift(-size / 4);

        // Apply window and zero-pad remaining points
        mWindow.apply(this);
        fillReal(mWindow.getSize(), mConvolution.getFftSize(), 0.0f);
        fillImag(mWindow.getSize(), mConvolution.getFftSize(), 0.0f);

        return this;
    }

    AbstractConvolutionF getConvolution() { return mConvolution; }
}
