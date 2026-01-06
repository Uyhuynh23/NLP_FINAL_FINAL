Sherpa ONNX TTS (Android)

Short description

This repository contains an Android example app (SherpaOnnxTts) that demonstrates on-device text-to-speech using an ONNX-based TTS model. The project includes an assets/exported helper script (fix_metadata.py) used to ensure required metadata is present in an exported Piper/VITS ONNX model so the Android app can load and run it.

Features

- Android app that loads an ONNX TTS model from app assets
- Helper Python script to add/fix ONNX model metadata for compatibility
- Intended to run on-device (no cloud inference required)

Prerequisites

- Android Studio (or a recent Gradle toolchain)
- A device or emulator with ABI matching bundled native libraries
- Java / Kotlin toolchain installed by Android Studio
- Python 3.x for running the metadata-fix script
- Python package: onnx (pip install onnx)

Project layout (relevant paths)

- app/ — Android app module
- app/src/main/assets/exported/ — exported ONNX models and helper script
  - fix_metadata.py — Python script that adds required metadata fields to a model

Using the metadata fix script

1. Install the onnx package:

   pip install onnx

2. Place your exported model file in the same directory as the script and name it model.onnx, or update the script to point to your filename.

3. Run the script from that directory:

   python fix_metadata.py

4. The script will write model_fixed.onnx (and print the metadata it added). Copy or rename model_fixed.onnx to the assets/exported model filename expected by the Android app.

Note: the included fix_metadata.py adds metadata keys commonly expected by Piper/VITS-based TTS backends (sample_rate, model_type, language, voice, noise_scale, length_scale, etc.). If your model requires different keys, adjust the metadata dictionary in the script.

Build and run (Android)

1. Open the project in Android Studio.
2. Let Gradle sync and resolve dependencies.
3. Ensure the intended ONNX model is present under app/src/main/assets/exported/ (named as the app expects).
4. Connect a device or start an emulator and run the app.

Troubleshooting

- If the app fails to load the model, check logcat for errors and verify the ONNX model is in the correct assets path and contains the metadata keys required by the runtime.
- If the Python script fails, confirm you installed the onnx package and that the script is run from the directory containing model.onnx (or update the path in the script).


