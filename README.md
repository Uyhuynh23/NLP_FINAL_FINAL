<<<<<<< Updated upstream
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

=======
# Vietnamese TTS - Android App

A beautiful, modern Android app for Vietnamese Text-to-Speech using on-device ONNX models.

## ✨ Features

- **🎨 Modern UI/UX** - Beautiful gradient backgrounds, glassmorphism cards, and smooth animations
- **🚀 Landing Page** - Stunning animated introduction with floating decorative elements
- **🎙️ On-device TTS** - No internet required, runs completely offline
- **🇻🇳 Vietnamese Voice** - Natural-sounding Vietnamese speech synthesis
- **⚡ Fast Processing** - Optimized ONNX runtime for quick generation
- **🎬 Lottie Animations** - Smooth microphone, waveform, and voice animations

## 📱 Screenshots

The app features two main screens:

### Landing Page
- Animated voice visualization using Lottie
- Floating decorative circles with smooth animations
- Feature highlights with icons
- Gradient CTA button with press animations
- Smooth page transition to main activity

### Main TTS Screen
- Purple/pink gradient background
- Glassmorphism-style card container
- Animated microphone header
- Multi-line text input area
- Waveform animation during speech generation
- Gradient action buttons (Generate, Play, Stop)

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Animation Library**: [Lottie](https://github.com/airbnb/lottie-android) 6.3.0
- **UI Components**: Material Design Components, CardView, ConstraintLayout
- **TTS Engine**: Sherpa-ONNX with Piper VITS model

## 📦 Dependencies

```groovy
// Lottie for animations
implementation 'com.airbnb.android:lottie:6.3.0'

// Material Design
implementation 'com.google.android.material:material:1.11.0'

// CardView
implementation 'androidx.cardview:cardview:1.0.0'

// ConstraintLayout
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
```

## 🚀 Getting Started

### Prerequisites
- Android Studio (Arctic Fox or later)
- JDK 8 or higher
- Android device/emulator with matching ABI for native libraries

### Build & Run

1. Clone the repository
2. Open the project in Android Studio
3. Wait for Gradle sync to complete
4. Ensure the TTS model is placed in `app/src/main/assets/`:
   - `vits-piper-vi_VN-vais1000-medium/` folder with model files
5. Connect a device or start an emulator
6. Click **Run** ▶️

## 📁 Project Structure

```
app/src/main/
├── java/com/k2fsa/sherpa/onnx/
│   ├── LandingActivity.kt      # Animated landing page
│   ├── MainActivity.kt          # Main TTS interface
│   ├── Tts.kt                   # TTS engine wrapper
│   └── OfflineTts.kt            # Native TTS bindings
├── res/
│   ├── layout/
│   │   ├── activity_landing.xml # Landing page layout
│   │   └── activity_main.xml    # Main TTS layout
│   ├── drawable/
│   │   ├── bg_gradient_main.xml      # Main gradient background
│   │   ├── bg_landing_gradient.xml   # Landing dark gradient
│   │   ├── btn_primary_gradient.xml  # Generate button style
│   │   ├── btn_success_gradient.xml  # Play button style
│   │   ├── btn_danger_gradient.xml   # Stop button style
│   │   └── bg_glow_*.xml             # Decorative glow effects
│   ├── raw/
│   │   ├── voice_animation.json      # Landing page Lottie
│   │   ├── mic_animation.json        # Header mic animation
│   │   └── waveform_animation.json   # Generating waveform
│   └── values/
│       ├── colors.xml           # Color palette
│       ├── themes.xml           # App themes
│       └── strings.xml          # String resources
└── assets/
    └── vits-piper-vi_VN-vais1000-medium/  # TTS model files
```

## 🎨 Color Palette

| Color | Hex | Usage |
|-------|-----|-------|
| Primary | `#6C63FF` | Main accent color |
| Gradient Start | `#667eea` | Background gradients |
| Gradient End | `#764ba2` | Background gradients |
| Accent | `#FF6B9D` | Secondary highlights |
| Success | `#11998e` → `#38ef7d` | Play button |
| Danger | `#eb3349` → `#f45c43` | Stop button |

## 🔧 Customization

### Change TTS Model
Edit `MainActivity.kt` and update the model configuration:

```kotlin
modelDir = "your-model-folder"
modelName = "your-model.onnx"
dataDir = "your-model-folder/espeak-ng-data"
```

### Modify Animations
Lottie animations are stored in `res/raw/`. You can:
- Replace with your own Lottie JSON files
- Download animations from [LottieFiles](https://lottiefiles.com/)
- Create custom animations in After Effects

### Update Colors
Edit `res/values/colors.xml` to change the app's color scheme.

## 📄 License

See LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please open an issue or submit a pull request.

## 📞 Support

Open an issue with reproduction steps and logs if you encounter problems.
>>>>>>> Stashed changes

