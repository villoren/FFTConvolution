/*
 * ComplexBufferD
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

import java.io.Serializable;
import java.util.Arrays;

/**
 * Provides a convenient method of storing complex vectors.
 * <p>
 * Data arrays <code>real[]</code> and <code>imag[]</code> are not encapsulated,
 * for the sake of loose coupling with other systems such as analog-digital converters.
 * <p>
 * Unlike java's <code>Complex</code> class, all operations are performed in-place.
 * No objects are created or destroyed.
 * <p>
 * For convenience, all modifying methods return a reference to themselves.
 *
 * @author Renato Villone
 */
public class ComplexBufferD implements Serializable {

    public final int size;
    public final double[] real;
    public final double[] imag;

    /**
     * Creates a new buffer of <code>size</code> complex vectors.
     *
     * @param size Number of complex vectors to hold.
     */
    public ComplexBufferD(int size) {

        this.size = size;
        real = new double[size];
        imag = new double[size];
    }

    /**
     * Creates a new buffer by using the given arrays references.
     * <p>
     * Since the arrays are not copied, any changes to their values
     * from outside the scope of this buffer will be reflected here.
     *
     * @param real Array reference to store for real elements.
     * @param imag Array reference to store for imaginary elements.
     */
    public ComplexBufferD(double[] real, double[] imag) {

        if (real.length != imag.length)
            throw new IllegalArgumentException("ComplexBufferD(): real and imag arrays must be of equal length.");

        size = real.length;
        this.real = real;
        this.imag = imag;
    }

    /**
     * Creates a deep copy of the given buffer.
     *
     * @param copyFrom Buffer to copy from.
     */
    public ComplexBufferD(ComplexBufferD copyFrom) {

        this(copyFrom.size);
        System.arraycopy(copyFrom.real, 0, this.real, 0, size);
        System.arraycopy(copyFrom.imag, 0, this.imag, 0, size);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof ComplexBufferD)) return false;

        ComplexBufferD that = (ComplexBufferD) o;

        if (size != that.size) return false;
        if (!Arrays.equals(imag, that.imag)) return false;
        return Arrays.equals(real, that.real);
    }

    // ---------------------------------------------------- Common Methods
    /**
     * @param index Index of the complex vector.
     * @return Squared magnitude of the complex vector at the given index.
     */
    public double squared(int index) {

        return real[index] * real[index] + imag[index] * imag[index];
    }

    /**
     * @param index Index of the complex vector.
     * @return Magnitude of the complex vector at the given index.
     */
    public double magnitude(int index) {

        return (double) Math.sqrt(squared(index));
    }

    /**
     * @param index Index of the complex vector.
     * @return Phase in radians of the complex vector at the given index.
     */
    public double phase(int index) {

        return (double) Math.atan2(imag[index], real[index]);
    }

    /**
     * Fill this buffers real and imaginary elements with the given value.
     *
     * @param value Value to be stored in every real and imaginary element.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fill(double value) {

        Arrays.fill(real, value);
        Arrays.fill(imag, value);
        return this;
    }

    /**
     * Fill a range of both real and imaginary elements with the given value.
     *
     * @param fromIndex Index of the first complex vector (inclusive).
     * @param toIndex Index of the last complex vector (exclusive).
     * @param value Value to be stored in every real and imaginary element of the given range.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fill(int fromIndex, int toIndex, double value) {

        Arrays.fill(real, fromIndex, toIndex, value);
        Arrays.fill(imag, fromIndex, toIndex, value);
        return this;
    }

    /**
     * Fill this buffer's real elements with the given value.
     *
     * @param value Value to be stored in every real element.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fillReal(double value) {

        Arrays.fill(real, value);
        return this;
    }

    /**
     * Fill a range of this buffer's real elements with the given value.
     *
     * @param fromIndex Index of the first complex vector (inclusive).
     * @param toIndex Index of the last complex vector (exclusive).
     * @param value Value to be stored in every real element of the given range.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fillReal(int fromIndex, int toIndex, double value) {

        Arrays.fill(real, fromIndex, toIndex, value);
        return this;
    }

    /**
     * Fill this buffer's imaginary elements with the given value.
     *
     * @param value Value to be stored in every imaginary element.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fillImag(double value) {

        Arrays.fill(imag, value);
        return this;
    }

    /**
     * Fill a range of this buffer's imaginary elements with the given value.
     *
     * @param fromIndex Index of the first complex vector (inclusive).
     * @param toIndex Index of the last complex vector (exclusive).
     * @param value Value to be stored in every imaginary element of the given range.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fillImag(int fromIndex, int toIndex, double value) {

        Arrays.fill(imag, fromIndex, toIndex, value);
        return this;
    }

    /**
     * Stores the cross product of the given operands in this buffer.
     *
     * @param inLeft Left side operand.
     * @param inRigh Right side operand.
     * @return Reference to this buffer.
     */
    public ComplexBufferD cross(ComplexBufferD inLeft, ComplexBufferD inRigh) {

        for (int i = 0; i < size; i++) {

            double lr = inLeft.real[i];
            double li = inLeft.imag[i];

            double rr = inRigh.real[i];
            double ri = inRigh.imag[i];

            real[i] = lr * rr - li * ri;
            imag[i] = lr * ri + li * rr;
        }

        return this;
    }

    /**
     * Swaps real and imaginary values in every complex vector of this buffer,
     * so that elements in <code>real[]</code> become elements in <code>imag[]</code>, and vice versa.
     *
     * @return Reference to this buffer.
     */
    public ComplexBufferD swap() {

        for (int i = 0; i < size; i++) {

            double temp = real[i];
            real[i] = imag[i];
            imag[i] = temp;
        }

        return this;
    }

    // ---------------------------------------------------- Time Domain

    /**
     * Circular shift this buffer by <code>delta</code> indices, so that:
     * <p><code>real[i] = real[i + delta]</code></p>
     * <p><code>imag[i] = imag[i + delta]</code></p>
     * Positive <code>delta</code> values shift to the left, while negative values shift to the right.
     *
     * @param delta Number of indices to shift. Sign provides direction.
     * @return Reference to this buffer.
     */
    public ComplexBufferD shift(int delta) {

        delta = delta % size;

        if (delta == 0)
            return this;

        double[] tempR = new double[size];
        double[] tempI = new double[size];
        System.arraycopy(real, 0, tempR, 0, size);
        System.arraycopy(imag, 0, tempI, 0, size);

        for (int i = 0; i < size; i++) {

            int j = i + delta;
            if (j >= size) j -= size;
            if (j < 0) j += size;

            real[i] = tempR[j];
            imag[i] = tempI[j];
        }

        return this;
    }

    // ---------------------------------------------------- Frequency Domain

    /**
     * Calculate the energy at <code>bin</code>.
     * <p>
     * Contributions from both positive and negative frequencies are taken into account.
     * <p>
     * <i>This method assumes this buffer represents values in the frequency domain.</i>
     *
     * @param bin Bin index to take the energy from. Bins from positive or negative frequencies can be used.
     * @return The sum of energy contained in the positive plus negative bin.
     */
    public double energy(int bin) {

        // Handle RC and Nyquist bins,
        // inherently contribute only half
        if (bin == 0 || bin == size / 2)
            return squared(bin);

        // Positive and negative frequencies
        return squared(bin) + squared(size - bin);
    }

    /**
     * Calculates the energy in the range of the given frequencies.
     * <p>
     * Frequencies must be both positive or negative to prevent overlapping,
     * and are expressed as absolute frequencies (-1/2 sampleRate to 1/2 sampleRate).
     * <p>
     * <i>This method assumes this buffer represents values in the frequency domain.</i>
     *
     * @param startFreq Band start frequency as a fraction of the sample rate (-0.5 to 0.5).
     * @param endFreq Band end frequency as a fraction of the sample rate (-0.5 to 0.5).
     * @return Sum of energy contained in the negative and positive sides of the given band.
     */
    public double energy(double startFreq, double endFreq) {

        double result = 0.0;

        // Assert both frequencies are the same sign to prevent overlapping
        if (startFreq < 0.0 != endFreq < 0.0)
            throw new IllegalArgumentException(
                    "ComplexBuffer.energy(): startFreq and endFreq are overlapping. "
                            +   "Must be both positive or negative."
            );

        // Work only with positive frequencies,
        // negative frequencies are handled by 'energy(int bin)'
        startFreq = Math.abs(startFreq);
        endFreq = Math.abs(endFreq);

        // Swap to make startFreq < endFreq
        if (endFreq < startFreq) {

            double temp = startFreq;
            startFreq = endFreq;
            endFreq = temp;
        }

        // Assert both frequencies are <= 0.5
        if (endFreq > 0.5)
            throw new IllegalArgumentException(
                    "ComplexBuffer.energy(): Frequencies can't be greater than Nyquist frequency, "
                            +   "-0.5 or +0.5 the sampling rate."
            );

        // Calculate contribution from limit indices and get their energies
        double binFraction, remainder, contribution;

        // Start index
        binFraction  = startFreq * size;                 // 204.8 = 0.2 * 1024
        int startBin = (int) Math.round(binFraction);    // 205
        remainder    = Math.abs(binFraction - startBin); // 0.2 = |204.8 - 205.0|
        contribution = 1.0 - remainder;                 // 0.8 = 1.0 - 0.2

        result += energy(startBin) * contribution;

        // End index
        binFraction  = endFreq * size;
        int endBin   = (int) Math.round(binFraction);
        remainder    = Math.abs(binFraction - endBin);
        contribution = 1.0 - remainder;

        result += energy(endBin) * contribution;

        // Bins in-between
        for (int i = startBin + 1; i < endBin; i++)
            result += energy(i);

        return result;
    }

    /**
     * Calculates the energy in the range of the given frequencies.
     * <p>
     * Frequencies must be both positive or negative to prevent overlapping,
     * and are expressed as absolute frequencies (-1/2 sampleRate to 1/2 sampleRate).
     * <p>
     * <i>This method assumes this buffer represents values in the frequency domain.</i>
     *
     * @param sampleRate Sample rate in Hz.
     * @param startFreq Band start frequency in Hz.
     * @param endFreq Band end frequency in Hz.
     * @return Sum of energy contained in the negative and positive sides of the given band.
     */
    public double energy(double sampleRate, double startFreq, double endFreq) {

        return energy(startFreq / sampleRate, endFreq / sampleRate);
    }

    /**
     * Even-Odd decompose this spectrum to reflect the spectra of the real and imaginary time domain signals.
     * <p>
     * If this buffer represents the spectrum (frequency domain) of a complex (time domain) signal
     * - that is, a time domain signal with data both in the real and imaginary parts, then:
     * <p>-The spectrum of the real[] signal will have</p>
     * <li><i>even</i> symmetry in this buffer's <code>real[]</code>.
     * <li><i>odd</i> symmetry in this buffer's <code>imag[]</code>.
     * </ul>
     * <p>-The spectrum of the imag[] signal will have</p>
     * <li><i>odd</i> symmetry in this buffer's <code>real[]</code>.
     * <li><i>even</i> symmetry in this buffer's <code>imag[]</code>.
     * </ul>
     * <p>
     * Both output buffers must be at least the same {@link #size} as this buffer.
     *
     * @param outRealSpectrum Upon return, contains the spectrum of the real elements of the signal.
     * @param outImagSpectrum Upon return, contains the spectrum of the imag elements of the signal.
     */
    public void decomposeEvenOdd(ComplexBufferD outRealSpectrum, ComplexBufferD outImagSpectrum) {

        // Handle index 0 and size / 2
        final int n2 = size / 2;

        // Even symmetry in real, odd in imag
        outRealSpectrum.real[0] = real[0];
        outRealSpectrum.imag[0] = 0.0;
        outRealSpectrum.real[n2] = real[n2];
        outRealSpectrum.imag[n2] = 0.0;

        // Odd symmetry in real, even in imag
        outImagSpectrum.real[0] = 0.0;
        outImagSpectrum.imag[0] = imag[0];
        outImagSpectrum.real[n2] = 0.0;
        outImagSpectrum.imag[n2] = imag[n2];

        // Handle index 1 to n2 - 1
        for (int i = 1; i < n2; i++) {

            int k = size - i;

            double realEven = (real[i] + real[k]) / 2.0;
            double realOdd  = (real[i] - real[k]) / 2.0;

            double imagEven = (imag[i] + imag[k]) / 2.0;
            double imagOdd  = (imag[i] - imag[k]) / 2.0;

            // Even symmetry in real, odd in imag
            outRealSpectrum.real[i] = realEven;
            outRealSpectrum.imag[i] = imagOdd;
            outRealSpectrum.real[k] = realEven;
            outRealSpectrum.imag[k] = -imagOdd;

            // Odd symmetry in real, even in imag
            outImagSpectrum.real[i] = realOdd;
            outImagSpectrum.imag[i] = imagEven;
            outImagSpectrum.real[k] = -realOdd;
            outImagSpectrum.imag[k] = imagEven;
        }
    }

    /**
     * Stores the provided real and imaginary values in the bin at the given index.
     * <p>
     * Symmetry between positive and negative frequencies is handled automatically.
     *
     * @param bin Index of bin to set.
     * @param realValue Cosine amplitude for the given bin.
     * @param imagValue Sine amplitude for the given bin.
     * @return Reference to this buffer.
     */
    public ComplexBufferD setBin(int bin, double realValue, double imagValue) {

        setBinReal(bin, realValue);
        setBinImag(bin, imagValue);

        return this;
    }

    /**
     * Stores the provided real value in the bin at the given index.
     * <p>
     * Symmetry between positive and negative frequencies is even and handled automatically.
     *
     * @param bin Index of bin to set.
     * @param value Cosine amplitude for the given bin.
     * @return Reference to this buffer.
     */
    public ComplexBufferD setBinReal(int bin, double value) {

        real[bin] = value;

        // Force symmetry for bins
        // other than RC bin (at index 0)
        // and Nyquist frequency (at index size / 2)
        if (bin != 0 && bin != size / 2) {

            // Even symmetry for real part
            real[size - bin] = value;
        }

        return this;
    }

    /**
     * Stores the provided imaginary value in the bin at the given index.
     * <p>
     * Symmetry between positive and negative frequencies is odd and handled automatically.
     *
     * @param bin Index of bin to set.
     * @param value Sine amplitude for the given bin.
     * @return Reference to this buffer.
     */
    public ComplexBufferD setBinImag(int bin, double value) {

        real[bin] = value;

        // Force symmetry for bins
        // other than RC bin (at index 0)
        // and Nyquist frequency (at index size / 2)
        if (bin != 0 && bin != size / 2) {

            // Odd symmetry for imaginary part
            real[size - bin] = -value;
        }

        return this;
    }

    /**
     * Same as {@link #setBin(int, double, double)} but values are given in polar notation.
     *
     * @param bin Index of bin to set.
     * @param magnitude Magnitude of both positive and negative complex vectors.
     * @param phase Phase in radians.
     * @return Reference to this buffer.
     */
    public ComplexBufferD setBinPolar(int bin, double magnitude, double phase) {

        double realValue = magnitude * (double) Math.cos(phase);
        double imagValue = magnitude * (double) Math.sin(phase);

        setBin(bin, realValue, imagValue);
        return this;
    }

    /**
     * Fills the band between (and including) the given start- and end-frequency
     * with the provided real and imaginary values.
     * <p>
     * Frequencies must be both positive or negative to prevent overlapping,
     * and are expressed as fractions of the sample rate (-0.5 to 0.5).
     * <p>
     * Symmetry between positive and negative frequencies is handled automatically.
     * <p>
     * <i>This method assumes this buffer represents values in the frequency domain.</i>
     *
     * @param startFreq Band start frequency as a fraction of the sample rate (-0.5 to 0.5).
     * @param endFreq Band end frequency as a fraction of the sample rate (-0.5 to 0.5).
     * @param realValue Cosine amplitude for the given range.
     * @param imagValue Sine amplitude for the given range.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fill(double startFreq, double endFreq, double realValue, double imagValue) {

        fillBandImpl(startFreq, endFreq, realValue, imagValue, true, true);
        return this;
    }

    /**
     * Fills the band between (and including) the given start- and end-frequency
     * with the provided magnitude and phase.
     * <p>
     * Frequencies must be both positive or negative to prevent overlapping,
     * and are expressed as fractions of the sample rate (-0.5 to 0.5).
     * <p>
     * Symmetry between positive and negative frequencies is handled automatically.
     * <p>
     * <i>This method assumes this buffer represents values in the frequency domain.</i>
     *
     * @param startFreq Band start frequency as a fraction of the sample rate (-0.5 to 0.5).
     * @param endFreq Band end frequency as a fraction of the sample rate (-0.5 to 0.5).
     * @param magnitude Magnitude of the complex vectors.
     * @param phase Phase in radians.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fillPolar(double startFreq, double endFreq, double magnitude, double phase) {

        double realValue = magnitude * (double) Math.cos(phase);
        double imagValue = magnitude * (double) Math.sin(phase);

        fillBandImpl(startFreq, endFreq, realValue, imagValue, true, true);
        return this;
    }

    /**
     * Fills the band between (and including) the given start- and end-frequency
     * with the provided real value.
     * <p>
     * Frequencies must be both positive or negative to prevent overlapping,
     * and are expressed as fractions of the sample rate (-0.5 to 0.5).
     * <p>
     * Symmetry between positive and negative frequencies is handled automatically.
     * <p>
     * <i>This method assumes this buffer represents values in the frequency domain.</i>
     *
     * @param startFreq Band start frequency as a fraction of the sample rate (-0.5 to 0.5).
     * @param endFreq Band end frequency as a fraction of the sample rate (-0.5 to 0.5).
     * @param value Cosine amplitude for the given range.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fillReal(double startFreq, double endFreq, double value) {

        fillBandImpl(startFreq, endFreq, value, 0.0, true, false);
        return this;
    }

    /**
     * Fills the band between (and including) the given start- and end-frequency
     * with the provided imaginary value.
     * <p>
     * Frequencies must be both positive or negative to prevent overlapping,
     * and are expressed as fractions of the sample rate (-0.5 to 0.5).
     * <p>
     * Symmetry between positive and negative frequencies is handled automatically.
     * <p>
     * <i>This method assumes this buffer represents values in the frequency domain.</i>
     *
     * @param startFreq Band start frequency as a fraction of the sample rate (-0.5 to 0.5).
     * @param endFreq Band end frequency as a fraction of the sample rate (-0.5 to 0.5).
     * @param value Sine amplitude for the given range.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fillImag(double startFreq, double endFreq, double value) {

        fillBandImpl(startFreq, endFreq, 0.0, value, false, true);
        return this;
    }

    /**
     * Fills the band between (and including) the given start- and end-frequency
     * with the provided real and imaginary values.
     * <p>
     * Frequencies must be both positive or negative to prevent overlapping,
     * and are expressed as absolute frequencies (-1/2 sampleRate to 1/2 sampleRate).
     * <p>
     * Symmetry between positive and negative frequencies is handled automatically.
     * <p>
     * <i>This method assumes this buffer represents values in the frequency domain.</i>
     *
     * @param sampleRate Sample rate in Hz.
     * @param startFreq Band start frequency in Hz.
     * @param endFreq Band end frequency in Hz.
     * @param realValue Cosine amplitude for the given range.
     * @param imagValue Sine amplitude for the given range.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fill(double sampleRate, double startFreq, double endFreq, double realValue, double imagValue) {

        fill(startFreq / sampleRate, endFreq / sampleRate, realValue, imagValue);
        return this;
    }

    /**
     * Fills the band between (and including) the given start- and end-frequency
     * with the provided magnitude and phase.
     * <p>
     * Frequencies must be both positive or negative to prevent overlapping,
     * and are expressed as absolute frequencies (-1/2 sampleRate to 1/2 sampleRate).
     * <p>
     * Symmetry between positive and negative frequencies is handled automatically.
     * <p>
     * <i>This method assumes this buffer represents values in the frequency domain.</i>
     *
     * @param sampleRate Sample rate in Hz.
     * @param startFreq Band start frequency in Hz.
     * @param endFreq Band end frequency in Hz.
     * @param magnitude Magnitude of the complex vectors.
     * @param phase Phase in radians.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fillPolar(double sampleRate, double startFreq, double endFreq, double magnitude, double phase) {

        fillPolar(startFreq / sampleRate, endFreq / sampleRate, magnitude, phase);
        return this;
    }

    /**
     * Fills the band between (and including) the given start- and end-frequency
     * with the provided real value.
     * <p>
     * Frequencies must be both positive or negative to prevent overlapping,
     * and are expressed as absolute frequencies (-1/2 sampleRate to 1/2 sampleRate).
     * <p>
     * Symmetry between positive and negative frequencies is handled automatically.
     * <p>
     * <i>This method assumes this buffer represents values in the frequency domain.</i>
     *
     * @param sampleRate Sample rate in Hz.
     * @param startFreq Band start frequency in Hz.
     * @param endFreq Band end frequency in Hz.
     * @param value Cosine amplitude for the given range.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fillReal(double sampleRate, double startFreq, double endFreq, double value) {

        fillReal(startFreq / sampleRate, endFreq / sampleRate, value);
        return this;
    }

    /**
     * Fills the band between (and including) the given start- and end-frequency
     * with the provided imaginary value.
     * <p>
     * Frequencies must be both positive or negative to prevent overlapping,
     * and are expressed as absolute frequencies (-1/2 sampleRate to 1/2 sampleRate).
     * <p>
     * Symmetry between positive and negative frequencies is handled automatically.
     * <p>
     * <i>This method assumes this buffer represents values in the frequency domain.</i>
     *
     * @param sampleRate Sample rate in Hz.
     * @param startFreq Band start frequency in Hz.
     * @param endFreq Band end frequency in Hz.
     * @param value Sine amplitude for the given range.
     * @return Reference to this buffer.
     */
    public ComplexBufferD fillImag(double sampleRate, double startFreq, double endFreq, double value) {

        fillImag(startFreq / sampleRate, endFreq / sampleRate, value);
        return this;
    }

    /**
     * Fills the band between (and including) the given start- and end-frequency
     * with the provided real and imaginary values.
     * <p>
     * Frequencies must be both positive or negative to prevent overlapping,
     * and are expressed as fractions of the sample rate (-0.5 to 0.5).
     * <p>
     * Symmetry between positive and negative frequencies is handled automatically.
     * <p>
     * <i>This method assumes this buffer represents values in the frequency domain.</i>
     *
     * @param startFreq Band start frequency as a fraction of the sample rate (-0.5 to 0.5).
     * @param endFreq Band end frequency as a fraction of the sample rate (-0.5 to 0.5).
     * @param realValue Cosine amplitude for the given range.
     * @param imagValue Sine amplitude for the given range.
     * @param useReal Use given real value.
     * @param useImag Use given imag value.
     */
    private void fillBandImpl(double startFreq, double endFreq, double realValue, double imagValue, boolean useReal, boolean useImag) {

        // Assert both frequencies are the same sign to prevent overlapping
        if (startFreq < 0.0 != endFreq < 0.0)
            throw new IllegalArgumentException(
                    "ComplexBuffer.fillBandImpl(): startFreq and endFreq are overlapping. "
                            +   "Must be both positive or negative."
            );

        // Work only with positive frequencies,
        // inverting imaginary part for negative ones (odd symmetry)
        double sign = startFreq < 0.0 ? -1.0 : 1.0;
        startFreq *= sign;
        endFreq   *= sign;
        imagValue *= sign;

        // Swap to make startFreq < endFreq
        if (endFreq < startFreq) {

            double temp = startFreq;
            startFreq = endFreq;
            endFreq = temp;
        }

        // Assert both frequencies are <= 0.5
        if (endFreq > 0.5)
            throw new IllegalArgumentException(
                    "ComplexBuffer.fillBandImpl(): Frequencies can't be greater than Nyquist frequency, "
                            +   "-0.5 or +0.5 the sampling rate."
            );

        // Calculate innovations from limit indices and set their bins
        double indexFraction;

        // Start index
        indexFraction         = startFreq * size;                         // 204.8 = 0.2 * 1024
        int startIndex        = (int) Math.round(indexFraction);          // 205
        double startRemainder  = Math.abs(indexFraction - startIndex);    // 0.2 = |204.8 - 205.0|
        double startInnovation = 1.0 - startRemainder;                   // 0.8 = 1.0 - 0.2

        // End index
        indexFraction         = endFreq * size;
        int endIndex          = (int) Math.round(indexFraction);
        double endRemainder    = Math.abs(indexFraction - endIndex);
        double endInnovation   = 1.0 - endRemainder;

        // Setting both, real or imag ?
        if (useReal && useImag) {

            // Set start bin
            setBin(
                    startIndex,
                    real[startIndex] * startRemainder + realValue * startInnovation,
                    imag[startIndex] * startRemainder + imagValue * startInnovation
            );

            // Set end bin
            setBin(
                    endIndex,
                    real[endIndex] * endRemainder + realValue * endInnovation,
                    imag[endIndex] * endRemainder + imagValue * endInnovation
            );

            // Set all bins in between
            for (int i = startIndex + 1; i < endIndex; i++)
                setBin(i, realValue, imagValue * sign);

        } else if (useReal) {

            // Set start bin
            setBinReal(startIndex, real[startIndex] * startRemainder + realValue * startInnovation);

            // Set end bin
            setBinReal(endIndex, real[endIndex] * endRemainder + realValue * endInnovation);

            // Set all bins in between
            for (int i = startIndex + 1; i < endIndex; i++)
                setBinReal(i, realValue);

        } else if (useImag) {

            // Set start bin
            setBinImag(startIndex, imag[startIndex] * startRemainder + imagValue * startInnovation);

            // Set end bin
            setBinImag(endIndex, imag[endIndex] * endRemainder + imagValue * endInnovation);

            // Set all bins in between
            for (int i = startIndex + 1; i < endIndex; i++)
                setBinImag(i, imagValue);

        } else {

            throw new IllegalArgumentException(
                    "ComplexBuffer.fillBandImpl(): at least one of useReal or useImag must be true"
            );
        }
    }
}

