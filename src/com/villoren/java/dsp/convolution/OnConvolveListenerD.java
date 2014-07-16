/*
 * OnConvolveListenerD
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
 * Listener for the Convolution process.
 * <p>
 * Gives access to the frequency domain data before and after the convolution.
 * This is useful, for instance, if you want to display spectra.
 *
 * @author Renato Villone
 */
public interface OnConvolveListenerD {

    /**
     * Called after the input signal was transformed to the frequency domain,
     * but the convolution wasn't yet performed.
     * <p>
     * The time domain signal will be zero-padded, and might have been
     * rearranged by different implementations of <code>Convolution</code>.
     * <p>
     * It is possible to change the convolution's filter kernel inside this method,
     * for instance, to a adjust it in some meaningful way by learning from <code>freqDomain</code>.
     * <p>
     * <i>Buffers provided by this method are not copies.
     * Changing them affects the outcome of the running convolution.</i>
     *
     * @param timeDomain Zero-padded and possibly rearranged time-domain input signal.
     * @param freqDomain Spectrum of the input signal.
     */
    void onPreConvolve(ComplexBufferD timeDomain, ComplexBufferD freqDomain);

    /**
     * Called after the input signal was convolved and transformed back to de time domain,
     * but before overlap-adding it to the pending samples from the las convolution step.
     * <p>
     * <i>The buffers provided by this method are not copies.
     * Changing them affects the outcome of the running convolution.</i>
     *
     * @param timeDomain Convolved time-domain signal.
     * @param freqDomain Spectrum of the convolved signal.
     */
    void onPostConvolve(ComplexBufferD timeDomain, ComplexBufferD freqDomain);
}
