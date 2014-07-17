/*
 * ConvolutionComplexF
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

import com.villoren.java.dsp.fft.ComplexBufferF;

/**
 * Provides a tool to perform FFT Convolution on complex samples.
 *
 * @author Renato Villone
 */
public class ConvolutionComplexF extends AbstractConvolutionF {

    /**
     * Time domain signal, pre convolution.
     * This is the input signal up to {@link #mSize},
     * zero padded up to {@link #mFftSize} to have enough space for the convolution.
     */
    private final ComplexBufferF mPreConvolutionTimeDomain;

    /**
     * {@link #mPostConvolutionFreqDomain} transformed back to the time-domain.
     */
    private final ComplexBufferF mPostConvolutionTimeDomain;

    /**
     * Already convolved samples, pending overlap-add.
     */
    private final ComplexBufferF mPendingTimeDomain;

    /**
     * Constructor.
     * <p>
     * Size remains constant for this instance.
     * Filter kernel is set by default to the identity delta function.
     *
     * @param size Number of samples to process in each convolution.
     */
    public ConvolutionComplexF(int size) {

        super(size);

        // Transitional data
        mPreConvolutionTimeDomain  = new ComplexBufferF(mFftSize);
        mPostConvolutionTimeDomain = new ComplexBufferF(mFftSize);
        mPendingTimeDomain         = new ComplexBufferF(mSize);
    }

    /**
     * Constructor.
     * <p>
     * Creates a new instance of <code>ConvolutionComplex</code>,
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
     * @param convolutionComplex Instance to reuse <code>FourierTransform</code> and <code>DefaultWindow</code> from.
     */
    public ConvolutionComplexF(ConvolutionComplexF convolutionComplex) {

        super(convolutionComplex);

        // Transitional data
        mPreConvolutionTimeDomain  = new ComplexBufferF(mFftSize);
        mPostConvolutionTimeDomain = new ComplexBufferF(mFftSize);
        mPendingTimeDomain         = new ComplexBufferF(mSize);
    }

    /**
     * Sets a <code>FilterKernel</code> to be used by this <code>Convolution</code>.
     * <p>
     * To prevent aliasing, the kernel's impulse response should be:
     * <li> <code>getFftSize()</code> / 2 + 1 samples long </li>
     * <li> centered around <code>getFftSize()</code> / 4 (ie in the first half) </li>
     * <li> windowed by a window-sinc</li>
     * </ul>
     * To prevent circular convolution, remaining samples (to the right of the impulse response) should be zero.
     * <p>
     * <b>Note:</b> This requirements are automatically met when setting a <code>FrequencyResponse</code>
     * to a <code>FilterKernel</code>.
     * <p>
     * <i>This implementation forces (sets) al <code>filterKernel.imag[]</code> values to zero.</i>
     *
     * @param filterKernel The kernel to be used by this convolution.
     * @see FilterKernelF#setFrequencyResponse(FrequencyResponseF)
     */
    public void setFilterKernel(FilterKernelF filterKernel) {

        // Force zero values in imaginary part
        filterKernel.fillImag(0.0f);

        super.setFilterKernel(filterKernel);
    }

    /**
     * Convolve the time-domain complex signal contained in <code>inReal[]</code> and <code>inImag[]</code>
     * with the current {@link FilterKernelF}.
     * <p>
     * Output is stored in <code>outReal[]</code> and <code>outImag[]</code>.
     * <p>
     * All arrays must be at least <code>getSize()</code> long.
     * Samples beyond this size will be ignored.
     * The values stored in the input arrays will remain unmodified.
     *
     * @param inReal Array filled with time-domain <i>real</i> part of the input signal.
     * @param inImag Array filled with time-domain <i>imaginary</i> part of the input signal.
     * @param outReal Upon return, holds the <i>real</i> part of the convolved signal.
     * @param outImag Upon return, holds the <i>imaginary</i> part of the convolved signal.
     */
    public void convolve(float[] inReal, float[] inImag, float[] outReal, float[] outImag) {

        // Check array sizes
        ensure(inReal.length >= mSize, "convolve", "inReal[] must be at least getSize() long.");
        ensure(inImag.length >= mSize, "convolve", "inImag[] must be at least getSize() long.");
        ensure(outReal.length >= mSize, "convolve", "outReal[] must be at least getSize() long.");
        ensure(outImag.length >= mSize, "convolve", "outImag[] must be at least getSize() long.");

        convolveImpl(inReal, inImag, outReal, outImag);
    }

    /**
     * Convolve the time-domain complex signal contained in <code>in</code>
     * with the current {@link FilterKernelF}.
     * <p>
     * Output is stored in <code>out</code>.
     * <p>
     * Both ComplexBuffers must be at least <code>getSize()</code> long.
     * Samples beyond this size will be ignored.
     * The values stored in the input buffer will remain unmodified.
     *
     * @param in Buffer filled with time-domain input signal.
     * @param out Buffer holding convolved time-domain signal upon return.
     */
    public void convolve(ComplexBufferF in, ComplexBufferF out) {

        // Check buffer sizes
        ensure(in.size >= mSize, "convolve", "Buffer 'in' must be at least getSize() long.");
        ensure(out.size >= mSize, "convolve", "Buffer 'out' must be at least getSize() long.");

        convolveImpl(in.real, in.imag, out.real, out.imag);
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
     * @param outImag Upon return, holds the <i>imaginary</i> part of the already convolved pending signal.
     */
    public void drain(float[] outReal, float[] outImag) {

        // Check array sizes
        ensure(outReal.length >= mSize, "drain", "outReal[] must be at least getSize() long.");
        ensure(outImag.length >= mSize, "drain", "outImag[] must be at least getSize() long.");

        drainImpl(outReal, outImag);
    }

    /**
     * Retrieves and clears the pending samples, which would have been overlap-added during the next convolution.
     * <p>
     * This is useful when you're out of input samples to feed to <code>convolve()</code>,
     * such as in an End-Of-File situation, but still want the remaining convolved samples.
     * <p>
     * <i>The complete convolved signal is always: original signal size + kernel size - 1.</i>
     *
     * @param out Upon return, holds the already convolved pending signal.
     */
    public void drain(ComplexBufferF out) {

        // Check buffer size
        ensure(out.size >= mSize, "drain", "Buffer 'out' must be at least getSize() long.");

        drainImpl(out.real, out.imag);
    }

    /**
     * Discards pending samples. Next call to <code>convolve()</code> starts from scratch.
     */
    public void flush() {

        mPendingTimeDomain.fill(0.0f);
    }

    /**
     * Implementation for all <code>convolve()</code> overloads.
     *
     * @param inReal Array filled with time-domain <i>real</i> part of the input signal.
     * @param inImag Array filled with time-domain <i>imaginary</i> part of the input signal.
     * @param outReal Upon return, holds the <i>real</i> part of the convolved signal.
     * @param outImag Upon return, holds the <i>imaginary</i> part of the convolved signal.
     */
    private void convolveImpl(float[] inReal, float[] inImag, float[] outReal, float[] outImag) {

        // Copy input into the first half, leaving rest zero-padded
        System.arraycopy(inReal, 0, mPreConvolutionTimeDomain.real, 0, mSize);
        System.arraycopy(inImag, 0, mPreConvolutionTimeDomain.imag, 0, mSize);

        // Convolve
        convolveFreqDomain(mPreConvolutionTimeDomain, mPostConvolutionTimeDomain);

        // Overlap-Add
        for (int i = 0, j = mSize; i < mSize; i++, j++) {

            // Result is:
            // First half of this result + second half of last result
            outReal[i] = mPostConvolutionTimeDomain.real[i] + mPendingTimeDomain.real[i];
            outImag[i] = mPostConvolutionTimeDomain.imag[i] + mPendingTimeDomain.imag[i];

            // Just used the pending samples from last iteration,
            // now overwrite with pending samples for next iteration.
            // 'j' starts at second half.
            mPendingTimeDomain.real[i] = mPostConvolutionTimeDomain.real[j];
            mPendingTimeDomain.imag[i] = mPostConvolutionTimeDomain.imag[j];
        }
    }

    /**
     * Implementation for <code>drain()</code> overloads that retrieve pending samples.
     *
     * @param outReal Upon return, holds the <i>real</i> part of the already convolved pending signal.
     * @param outImag Upon return, holds the <i>imaginary</i> part of the already convolved pending signal.
     */
    private void drainImpl(float[] outReal, float[] outImag) {

        System.arraycopy(mPendingTimeDomain.real, 0, outReal, 0, mSize);
        System.arraycopy(mPendingTimeDomain.imag, 0, outImag, 0, mSize);

        mPendingTimeDomain.fill(0.0f);
    }
}
