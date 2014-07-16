# FFTConvolution

FFTConvolution is a pure java fft-convolution library.

Include the sources `./src` directly in your project or create a jar file.

I added an ant `build.xml` to create the jar (including sources) and generate `demo.bat` and `demo.sh` scripts.


## Build JAR + Demo
- Get ant: http://ant.apache.org/bindownload.cgi
- In the project root, run:

```shell
ant build
```
   
This compiles the library's code and stores it in `./jar/fft-convolution.jar`.

It also compiles `./demo/ConvolutionDemo.java`. This file uses [javax.sound.sampled](http://docs.oracle.com/javase/7/docs/api/javax/sound/sampled/package-summary.html) imports which unfortunately are not available in some Java APIs, notably in Android. It was intentionally left out from the jar for the sake of portability.

You will find two new files in the project root: `demo.bat` and `demo.sh`.

#### Running Demo in Windows
- From the project root, execute:

```shell
demo.bat path_to_stereo_or_mono_file.wav
```

#### Running Demo in Unix-like
- Grant execution rights to `demo.sh`
- From the project root, execute:

```shell
./demo.sh path_to_stereo_or_mono_file.wav
```

---
## Build JAR only
- Get ant: http://ant.apache.org/bindownload.cgi
- In the project root, run:

```shell
ant jar
```
   
This compiles the library's code and stores it in `./jar/fft-convolution.jar`.

---
## Usage

You will find that all classes have `float` and `double` versions, for instance `FourierTransformF` and `FourierTransformD`.

Why not use generic (and elegant) `FourierTransform<Float>` or `FourierTransform<Double>` instead? 

The cost of creating and destroying `Float` and `Double` _objects_ is simply to high.
Furthermore,  `Float[] samples` can have null elements, while `float[] samples` can't.

See example in `./demo/ConvolutionDemo.java`.

---

Copyright (c) 2014 Renato Villone.

See LICENSE.txt for license rights and limitations (MIT).