/*
 * FourierTransformD
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

package com.villoren.java.dsp.fft;

/**
 * Radix-2 Decimation in time FFT.
 *
 * @author Renato Villone
 */
public class FourierTransformD {

    /**
     * Describes how to apply scaling for transforms.
     */
    public enum Scale {

        /** No scaling, just raw fft. */
        NONE,

        /** Forward transform only. Scale factor of 1 / size. */
        FORWARD,

        /** Inverse transform only. Scale factor of 1 / size. */
        INVERSE,

        /** Symmetrical scaling between forward and inverse transforms. Scale factor is 1 / sqrt(size). */
        BOTH,
    }

    // Constant
    private static final double PI = Math.PI;

    // Instance constants
    /**
     * Number of samples.
     */
    private final int mSize;

    /**
     * Half number of samples.
     */
    private final int mHalfSize;

    /**
     * Log2(number of samples).
     */
    private final int mLog2Size;

    /**
     * Scaling type to be used.
     */
    private final Scale mScale;

    /**
     * Scale factor applied in transforms.
     */
    private final double mScaleFactor;

    /**
     * Lookup array for bit-reversed indices.
     */
    private final int[] mReversed;

    /**
     * Lookup array for real twiddle factors.
     */
    private final double[] mTwiddleReal;

    /**
     * Lookup array for imaginary twiddle factors.
     */
    private final double[] mTwiddleImag;

    /**
     * Construction.
     * <p>
     * The provided fft size remains constant for the lifetime of this instance,
     * as does the {@link FourierTransformD.Scale} type to be used.
     *
     * @param size Number of samples to process in each transform.
     * @param scale {@link FourierTransformD.Scale} type to use.
     */
    public FourierTransformD(int size, Scale scale) {

        // Init instance constants
        mSize = size;
        mHalfSize = mSize / 2;
        mLog2Size = (int) (Math.log(mSize) / Math.log(2));
        mScale = scale;

        switch (mScale) {

            case NONE:
                mScaleFactor = 1.0;
                break;

            case FORWARD:
            case INVERSE:
                mScaleFactor = 1.0 / mSize;
                break;

            case BOTH:
                mScaleFactor = 1.0 / Math.sqrt(mSize);
                break;

            default:
                mScaleFactor = Double.NaN; // Should never happen.
                break;
        }

        // Assert numSamples is power of 2
        if (mSize != (1 << mLog2Size))
            throw new RuntimeException("FourierTransform: size must be power of 2.");

        // Compute and store bit-reverse lookup
        {
            mReversed = new int[mSize];

            for (int i = 0; i < mSize; i++) {

                int reversed = 0;
                for (int j = 0, k = mLog2Size - 1; j < mLog2Size; j++, k--) {

                    if (((1 << j) & i) != 0)
                        reversed |= (1 << k);
                }

                mReversed[i] = reversed;
            }
        }

        // Store twiddle factors
        {
            mTwiddleReal = new double[mHalfSize];
            mTwiddleImag = new double[mHalfSize];

            for (int i = 0; i < mHalfSize; i++) {

                double angle = -2.0f * PI * i / mSize;
                mTwiddleReal[i] = Math.cos(angle);
                mTwiddleImag[i] = Math.sin(angle);
            }
        }
    }

    /**
     * Computes the Fourier transform of the samples given by the input arrays.
     * <p>
     * All arrays must be at least of length <code>getSize()</code>.
     *
     * @param inReal Input array holding <i>real</i> parts.
     * @param inImag Input array holding <i>imaginary</i> parts.
     * @param outReal Output array holding <i>real</i> parts after transform.
     * @param outImag Output array holding <i>imaginary</i> parts after transform.
     * @param inverse If <code>true</code>, compute the inverse transform.
     */
    public void transform(double[] inReal, double[] inImag, double[] outReal, double[] outImag, boolean inverse) {

        // Assert in and out arrays are different
        if (inReal == outReal) throw new IllegalArgumentException("FourierF.transform(): inReal == outReal");
        if (inImag == outImag) throw new IllegalArgumentException("FourierF.transform(): inImag == outImag");

        // Scale to be used
        double scaleFactor;

        switch (mScale) {

            case FORWARD:   scaleFactor = inverse ? 1.0 : mScaleFactor; break;
            case INVERSE:   scaleFactor = inverse ? mScaleFactor : 1.0; break;
            default:        scaleFactor = mScaleFactor; break;
        }

        // Bit-reverse decomposition and scaling
        for (int i = 0; i < mSize; i++) {

            int reversed = mReversed[i];
            outReal[i] = inReal[reversed] * scaleFactor;
            outImag[i] = inImag[reversed] * scaleFactor;
        }

        // Twiddle factors are conjugated for inverse fft
        final double sign = inverse ? -1.0 : 1.0;

        // Combination
        for (int i = 0; i < mLog2Size; i++) {

            int n1 = 1 << i;
            int n2 = n1 * 2;
            int twiddleStep = 1 << (mLog2Size - i - 1);

            for (int j = 0, t = 0; j < n1; j++, t += twiddleStep) {

                double twiddleReal = mTwiddleReal[t];
                double twiddleImag = mTwiddleImag[t] * sign;

                for (int k = j; k < mSize; k += n2) {

                    int k2 = k + n1;

                    double tempR = twiddleReal * outReal[k2] - twiddleImag * outImag[k2];
                    double tempI = twiddleImag * outReal[k2] + twiddleReal * outImag[k2];

                    outReal[k2] = outReal[k] - tempR;
                    outImag[k2] = outImag[k] - tempI;

                    outReal[k] += tempR;
                    outImag[k] += tempI;
                }
            }
        }
    }

    /**
     * Computes the Fourier transform of the samples given by the input ComplexBuffer.
     * <p>
     * Both buffers must be at least of length <code>getSize()</code>.
     *
     * @param in Input buffer.
     * @param out Output buffer containing the results after transform.
     * @param inverse If <code>true</code>, compute the inverse transform.
     */
    public void transform(ComplexBufferD in, ComplexBufferD out, boolean inverse) {

        transform(in.real, in.imag, out.real, out.imag, inverse);
    }

    /**
     * @return The size of this fft.
     */
    public int getSize() { return mSize; }

    /**
     * @return The scale factor applied in transforms.
     * @see com.villoren.java.dsp.fft.FourierTransformD.Scale
     */
    public double getScaleFactor() { return mScaleFactor; }
}
