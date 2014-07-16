/*
 * ConvolutionDemo
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

import com.villoren.java.dsp.convolution.ConvolutionComplexF;
import com.villoren.java.dsp.convolution.ConvolutionRealF;
import com.villoren.java.dsp.convolution.FilterKernelF;
import com.villoren.java.dsp.convolution.FrequencyResponseF;
import com.villoren.java.dsp.fft.ComplexBufferF;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Demonstrate the use of <code>ConvolutionReal</code> and <code>ConcolutionComplex</code>.
 *
 * @author Renato Villone
 */
public class ConvolutionDemo {

    /**
     * @param args First argument is a path to a mono or stereo .wav file
     */
    public static void main(String[] args) {

        // First arg is path to a .wav file
        if (args.length < 1) {

            System.out.println("Missing argument: Path to a PCM signed 16-bit mono or stereo wav file.");
            return;
        }

        try {

            // Open file as AudioInputStream
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(args[0]));

            // Check if it's format is supported by this demo.
            // In a real application, you might want to use a FormatConversionProvider.
            AudioFormat audioFormat = audioInputStream.getFormat();

            if (audioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                showFormatError(audioFormat);
                return;
            }

            if (audioFormat.getSampleSizeInBits() != 16) {
                showFormatError(audioFormat);
                return;
            }

            // Call specific method upon number of channels
            switch (audioFormat.getChannels()) {

                case 1: // Mono file
                    System.out.println("Demo mono file.");
                    demoMonoFile(audioInputStream);
                    break;

                case 2: // Stereo file
                    System.out.println("Demo stereo file.");
                    demoStereoFile(audioInputStream);
                    break;

                default: // More than 2 channels not supported by this demo
                    showFormatError(audioFormat);
                    break;
            }

        } catch (UnsupportedAudioFileException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        } catch (LineUnavailableException e) {

            e.printStackTrace();
        }
    }

    /**
     * Use a mono file.
     *
     * @param audioInputStream Input stream of the file specified in command line:
     * @throws IOException
     * @throws LineUnavailableException
     */
    private static void demoMonoFile(AudioInputStream audioInputStream) throws IOException, LineUnavailableException {

        // Number of samples to process in each convolution pass
        final int N = 4096;

        // Open the audio output line for playback
        AudioFormat audioFormat = audioInputStream.getFormat();
        SourceDataLine outLine = AudioSystem.getSourceDataLine(audioFormat);
        outLine.open(audioFormat, 524288); // 1/2 megabytes

        // We are going to use a ByteBuffer to read from the input stream
        // and write to the output line, which helps represent the raw
        // bytes as shorts (since we are using 16-bit data)
        ByteBuffer byteBuffer = ByteBuffer.allocate(N * audioFormat.getFrameSize());
        byteBuffer.order(audioFormat.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

        // And two float arrays, to 'feed' samples to Convolution,
        // and to retrieve the convolved samples.
        // The input and output arrays could be the same array, we use two now for clarity
        float[] inSamples = new float[N];
        float[] outSamples = new float[N];

        // Create a new convolution instance to process the samples.
        // We use ConvolutionReal, since we are only going to feed it with 'real' numbers.
        ConvolutionRealF convolution = new ConvolutionRealF(N);

        // Now we create our 'desired' frequency response for the convolution's kernel
        FrequencyResponseF frequencyResponse = new FrequencyResponseF(convolution);

        // We want to study the bass-line for the given track.
        // We are going to let only pass the frequencies between:
        // 41.20 Hz (E1) to 392.00 Hz (G4)
        // ...corresponding to the frequency range of a 4-string 24-fret bass (leaving out harmonics).
        // First we set everything to zero (muting the whole frequency range),
        // then, we let the selected band pass by setting it to 1.
        frequencyResponse.fill(0.0f);
        frequencyResponse.fillReal(audioFormat.getSampleRate(), 41.20f, 392.00f, 1.0f);
        System.out.println("Keeping only frequency range: 41.2 Hz - 392.0 Hz");

        // We have now the desired frequency response
        // and need to create a filter kernel for convolution.
        FilterKernelF filterKernel = new FilterKernelF(convolution);

        // The convolution's filter kernel is set by default to a identity delta function,
        // so convolving something with it now wouldn't change the signal at all.
        // This method updates the kernel's impulse response to reflect the desired frequency response.
        // The IR is automatically centered in the first half of this signal, windowed by N/2 + 1
        // and zero-padded.
        filterKernel.setFrequencyResponse(frequencyResponse);

        // If we wanted to take a look at the *actual* frequency response obtained
        // from our now anti-aliased filter kernel, we could reuse our de desired FrequencyResponse:
        // frequencyResponse.setFilterKernel(filterKernel);

        // Use this filter kernel
        convolution.setFilterKernel(filterKernel);

        // Start playback
        outLine.start();
        System.out.println("Started playback...");

        // Loop through the entire input file
        final int frameSize = audioFormat.getFrameSize();
        int bytesRead;

        while ( (bytesRead = audioInputStream.read(byteBuffer.array())) != -1) {

            // Since each frame is composed of several bytes
            final int framesRead = bytesRead / frameSize;

            // Retrieve samples contained in byteBuffer as shorts (16-bit)
            // and store them as floats in the input array
            for (int i = 0; i < framesRead; i++) {

                int channel1 = i * frameSize;
                inSamples[i] = byteBuffer.getShort(channel1);
            }

            // If byteBuffer wasn't filled (because of EOF, for instance),
            // pad remaining samples with zeros
            Arrays.fill(inSamples, framesRead, N, 0.0f);

            // Convolve the input signal to obtain 'outSamples'.
            // (Could actually use the same array for in and output)
            convolution.convolve(inSamples, outSamples);

            // We reuse the byteBuffer to set the short values
            // which are queued for playback
            for (int i = 0; i < N; i++) {

                int channel1 = i * frameSize;
                byteBuffer.putShort(channel1, (short) Math.round(outSamples[i]));
            }

            // Queue them for playback
            outLine.write(byteBuffer.array(), 0, byteBuffer.array().length);
        }

        // The end of file has been reached.
        // However, there are still some already convolved samples left, since:
        // Size of convolved signal = size of original signal + size of filter kernel - 1

        // Get remaining samples
        convolution.drain(outSamples);

        // As before, we use the byteBuffer to play those remaining samples
        for (int i = 0; i < N; i++) {

            int channel1 = i * frameSize;
            byteBuffer.putShort(channel1, (short) Math.round(outSamples[i]));
        }

        // Write the last samples and close
        outLine.write(byteBuffer.array(), 0, byteBuffer.array().length);
        outLine.drain();
        outLine.close();
        audioInputStream.close();
    }

    /**
     * Use a stereo file.
     * <p>
     * Use ConvolutionComplex, to take advantage of both real and imag data,
     * to process both channels in a single pass.
     *
     * @param audioInputStream
     * @throws IOException
     * @throws LineUnavailableException
     */
    private static void demoStereoFile(AudioInputStream audioInputStream) throws IOException, LineUnavailableException {

        // Number of samples to process in each convolution pass
        final int N = 4096;

        // Open the audio output line for playback
        AudioFormat audioFormat = audioInputStream.getFormat();
        SourceDataLine outLine = AudioSystem.getSourceDataLine(audioFormat);
        outLine.open(audioFormat, 524288); // 1/2 megabytes

        // We are going to use a ByteBuffer to read from the input stream
        // and write to the output line, which helps represent the raw
        // bytes as shorts (since we are using 16-bit data)
        ByteBuffer byteBuffer = ByteBuffer.allocate(N * audioFormat.getFrameSize());
        byteBuffer.order(audioFormat.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

        // And two ComplexBuffers, to 'feed' samples to Convolution,
        // and to retrieve the convolved samples.
        // The input and output buffers could be the same, we use two now for clarity
        ComplexBufferF inSamples = new ComplexBufferF(N);
        ComplexBufferF outSamples = new ComplexBufferF(N);

        // Create a new convolution instance to process the samples.
        // We use ConvolutionComplex, to take advantage of both real and imag data,
        // so we can process both channels in a single pass.
        ConvolutionComplexF convolution = new ConvolutionComplexF(N);

        // Now we create our 'desired' frequency response for the convolution's kernel
        FrequencyResponseF frequencyResponse = new FrequencyResponseF(convolution);

        // We want to play a bass-line over the given track.
        // We filter out the track's bass frequencies between:
        // 41.20 Hz (E1) to 392.00 Hz (G4)
        // ...corresponding to the frequency range of a 4-string 24-fret bass (leaving out harmonics).
        // The whole 'real' part of the frequency response is 1 by default,
        // we filter out the bass band by setting it to 0.
        frequencyResponse.fillReal(audioFormat.getSampleRate(), 41.20f, 392.00f, 0.0f);
        System.out.println("Removing frequency range: 41.2 Hz - 392.0 Hz");

        // We have now the desired frequency response
        // and need to create a filter kernel for convolution.
        FilterKernelF filterKernel = new FilterKernelF(convolution);

        // The convolution's filter kernel is set by default to a identity delta function,
        // so convolving something with it now wouldn't change the signal at all.
        // This method updates the kernel's impulse response to reflect the desired frequency response.
        // The IR is automatically centered in the first half of this signal, windowed by N/2 + 1
        // and zero-padded.
        filterKernel.setFrequencyResponse(frequencyResponse);

        // The kernel has data in it's imaginary part that we don't want for this kind of convolution
        filterKernel.fillImag(0.0f);

        // If we wanted to take a look at the *actual* frequency response obtained
        // from our now anti-aliased filter kernel, we could reuse our de desired FrequencyResponse:
        // frequencyResponse.setFilterKernel(filterKernel);

        // Use this filter kernel
        convolution.setFilterKernel(filterKernel);

        // Start playback
        outLine.start();
        System.out.println("Started playback...");

        // Loop through the entire input file
        final int frameSize = audioFormat.getFrameSize();
        final int sampleSize = audioFormat.getSampleSizeInBits() / 8;
        int bytesRead;

        while ( (bytesRead = audioInputStream.read(byteBuffer.array())) != -1) {

            // Since each frame is composed of several bytes
            final int framesRead = bytesRead / frameSize;

            // Retrieve interleaved samples contained in byteBuffer as shorts (16-bit)
            // and store them as floats in both real and imag parts of the input buffer
            for (int i = 0; i < framesRead; i++) {

                int channel1 = i * frameSize;
                int channel2 = channel1 + sampleSize;

                inSamples.real[i] = byteBuffer.getShort(channel1);
                inSamples.imag[i] = byteBuffer.getShort(channel2);
            }

            // If byteBuffer wasn't filled (because of EOF, for instance),
            // pad remaining samples with zeros
            inSamples.fill(framesRead, N, 0.0f);

            // Convolve the input signal to obtain 'outSamples'.
            // (Could actually use the same array for in and output)
            convolution.convolve(inSamples, outSamples);

            // We reuse the byteBuffer to set the short values
            // which are queued for playback
            for (int i = 0; i < N; i++) {

                int channel1 = i * frameSize;
                int channel2 = channel1 + sampleSize;

                byteBuffer.putShort(channel1, (short) Math.round(outSamples.real[i]));
                byteBuffer.putShort(channel2, (short) Math.round(outSamples.imag[i]));
            }

            // Queue them for playback
            outLine.write(byteBuffer.array(), 0, byteBuffer.array().length);
        }

        // The end of file has been reached.
        // However, there are still some already convolved samples left, since:
        // Size of convolved signal = size of original signal + size of filter kernel - 1

        // Get remaining samples
        convolution.drain(outSamples);

        // As before, we use the byteBuffer to play those remaining samples
        for (int i = 0; i < N; i++) {

            int channel1 = i * frameSize;
            int channel2 = channel1 + sampleSize;

            byteBuffer.putShort(channel1, (short) Math.round(outSamples.real[i]));
            byteBuffer.putShort(channel2, (short) Math.round(outSamples.imag[i]));
        }

        // Write the last samples and close
        outLine.write(byteBuffer.array(), 0, byteBuffer.array().length);
        outLine.drain();
        outLine.close();
        audioInputStream.close();
    }

    private static void showFormatError(AudioFormat audioFormat) {

        System.out.printf(
                "Invalid file format:\n%s\n\nThis demo only accepts PCM signed 16-bit mono or stereo wav files.",
                audioFormat.toString()
        );
    }
}
