/*
 * ConvolutionRealD
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

import java.util.Arrays;

/**
 * Provides a tool to perform FFT Convolution on real samples.
 *
 * @author Renato Villone
 */
public class ConvolutionRealD  extends AbstractConvolutionD {

    /**
     * Time domain signal, pre convolution.
     * This is the input signal up to {@link #mSize},
     * zero padded up to {@link #mFftSize} to have enough space for the convolution.
     */
    private final ComplexBufferD mPreConvolutionTimeDomain;

    /**
     * {@link #mPostConvolutionFreqDomain} transformed back to the time-domain.
     */
    private final ComplexBufferD mPostConvolutionTimeDomain;

    /**
     * Already convolved samples, pending overlap-add.
     */
    private final double[] mPendingTimeDomain;

    /**
     * Constructor.
     * <p>
     * Size remains constant for this instance.
     * Filter kernel is set by default to the identity delta function.
     *
     * @param size Number of samples to process in each convolution.
     */
    public ConvolutionRealD(int size) {

        super(size / 2);

        // Transitional data
        mPreConvolutionTimeDomain  = new ComplexBufferD(mFftSize);
        mPostConvolutionTimeDomain = new ComplexBufferD(mFftSize);
        mPendingTimeDomain         = new double[mSize];
    }

    /**
     * Constructor.
     * <p>
     * Creates a new instance of <code>ConvolutionReal</code>,
     * inheriting the size and reusing <code>FourierTransform</code> and
     * <code>DefaultWindow</code> objects from the given instance.
     * <p>
     * This comes handy if you need more Convolution instances to process several
     * equal sized buffers at once (such as 5.1 audio channels),
     * and want to reuse <code>FourierTransform</code> object
     * with its lookup tables (since they're common to a given fft size).
     * <p>
     * <i>Other internal buffers, such as pending overlap-add samples,
     * are <u>not</u> copied.</i>
     *
     * @param convolutionReal Instance to reuse <code>FourierTransform</code> and <code>DefaultWindow</code> from.
     */
    public ConvolutionRealD(ConvolutionRealD convolutionReal) {

        super(convolutionReal);

        // Transitional data
        mPreConvolutionTimeDomain  = new ComplexBufferD(mFftSize);
        mPostConvolutionTimeDomain = new ComplexBufferD(mFftSize);
        mPendingTimeDomain         = new double[mSize];
    }

    /**
     * Convolve the time-domain complex signal contained in <code>inReal[]</code> and <code>inImag[]</code>
     * with the current {@link FilterKernelF}.
     * <p>
     * Output is stored in <code>outReal[]</code> and <code>outImag[]</code>.
     * <p>
     * Both arrays must be at least <code>getSize()</code> long.
     * Samples beyond this size will be ignored.
     * The values stored in the input arrays will remain unmodified.
     *
     * @param inReal Array filled with <i>real</i> time-domain input signal.
     * @param outReal Upon return, holds the the convolved signal.

     */
    public void convolve(double[] inReal, double[] outReal) {

        // Check array sizes
        ensure(inReal.length >= mSize * 2, "convolve", "inReal[] must be at least getSize() long.");
        ensure(outReal.length >= mSize * 2, "convolve", "outReal[] must be at least getSize() long.");

        // Use the 1st Real half to store 1st half of the input
        // and the 1st Imag half to store 2nd half of the input.
        // The second halfs of Real and Imag must remain padded with zeros.
        System.arraycopy(inReal, 0,     mPreConvolutionTimeDomain.real, 0, mSize);
        System.arraycopy(inReal, mSize, mPreConvolutionTimeDomain.imag, 0, mSize);

        // Convolve
        convolveFreqDomain(mPreConvolutionTimeDomain, mPostConvolutionTimeDomain);

        // Overlap-Add
        for (int i = 0, j = mSize; i < mSize; i++, j++) {

            // Result is:
            // Real part of the convolved time-domain signal adds to the entire output array.
            // First imaginary half of the convolved time-domain adds to the second half of the output array.
            // Second imaginary half of the convolved time-domain signal is stored to add to the first half
            // of the output array, after the next convolution.
            outReal[i] = mPostConvolutionTimeDomain.real[i] + mPendingTimeDomain[i];
            outReal[j] = mPostConvolutionTimeDomain.real[j] + mPostConvolutionTimeDomain.imag[i];

            // Just used the pending samples from last iteration,
            // now overwrite with pending samples for next iteration.
            mPendingTimeDomain[i] = mPostConvolutionTimeDomain.imag[j];
        }
    }

    /**
     * Retrieves and clears the pending samples, which would have been overlap-added during the next convolution.
     * <p>
     * This is useful when you're out of input samples to feed to <code>convolve()</code>,
     * such as in an End-Of-File situation, but still want the remaining convolved samples.
     * <p>
     * <i>The complete convolved signal is always: original signal size + kernel size - 1.</i>
     *
     * @param outReal Upon return, holds the <i>real</i> part of the already convolved pending signal.
     */
    public void drain(double[] outReal) {

        // Check array size
        ensure(outReal.length >= mSize, "drain", "outReal[] must be at least getSize() long.");

        System.arraycopy(mPendingTimeDomain, 0, outReal, 0, mSize);
        Arrays.fill(mPendingTimeDomain, 0.0);
    }

    /**
     * Discards pending samples. Next call to <code>convolve()</code> starts from scratch.
     */
    public void flush() {

        Arrays.fill(mPendingTimeDomain, 0.0);
    }

    /**
     * @return Number of samples to process in each <code>convolve()</code> pass.
     */
    public int getSize() { return mSize * 2; /* Since using real and imag as input */}
}
