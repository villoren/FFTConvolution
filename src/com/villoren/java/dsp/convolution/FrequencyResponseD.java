/*
 * FrequencyResponseD
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

import com.villoren.java.dsp.fft.ComplexBufferD;

/**
 * Provides a <code>ComplexBuffer</code> representing a frequency response.
 *
 * @author Renato Villone
 */
public class FrequencyResponseD extends ComplexBufferD {

    /**
     * <code>Convolution</code> for which this instance was created.
     */
    private final AbstractConvolutionD mConvolution;

    /**
     * Constructs a frequency response to be used by the given convolution.
     * The frequency response will be initialized to <i>identity</i>.
     *
     * @param convolution Convolution that will use this frequency response.
     */
    public FrequencyResponseD(AbstractConvolutionD convolution) {

        super(convolution.getFftSize());

        mConvolution = convolution;

        // Init to identity
        fillReal(1.0);
    }

    /**
     * Makes this instance reflect the given kernel's current frequency response.
     *
     * @param filterKernel The <code>FilterKernel</code> to take the frequency response from.
     */
    public void setFilterKernel(FilterKernelD filterKernel) {

        if (mConvolution != filterKernel.getConvolution())
            throw new IllegalArgumentException(
                    "FrequencyResponse.setFilterKernel(): filterKernel was created for another Convolution instance."
            );

        mConvolution.getFourierTransform().transform(filterKernel, this, false);
    }
}
