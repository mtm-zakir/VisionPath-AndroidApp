# VisionPath

Vision Path is an Android Application which helps the Blind people to navigate their surroundings and Environment just with the help of their Mobile Phones Camera and the Internet with gemini ai.

<div style="display: flex; justify-content: space-between; margin: 20px 0;">
    <img src="https://github.com/user-attachments/assets/f5606307-8196-455a-8eea-5d98e90bd3e2" width="32%" alt="VisionPath Screenshot 3"/>
    <img src="https://github.com/user-attachments/assets/2e925378-0a37-4356-a9aa-e742abcf8910" width="32%" alt="VisionPath Screenshot 1"/>
    <img src="https://github.com/user-attachments/assets/c8201136-8f81-4dfa-a77c-94cf98020e51" width="32%" alt="VisionPath Screenshot 2"/>
</div>

## Key Features

This app has 3 main features:

1️⃣ **Blind Mode**
- Activates automatically when app opens
- Creates a new session for navigation assistance
- Provides real-time guidance using camera feed
- Uses AI to detect obstacles and provide directions

2️⃣ **Assistant Mode**
- Activated by double-tapping in Blind Mode
- Answers questions about the environment
- Identifies colors, objects, and weather conditions
- Provides general information on various topics
- Uses voice commands for interaction

3️⃣ **Reading Mode**
- Helps users read text from various sources
- Supports sign-boards, books, and documents
- Converts detected text to speech
- Easy activation through gesture controls

## Technologies Used

- **Kotlin**: The primary programming language used for Android development.
- **Android Jetpack Compose**: For building the UI components in a declarative manner.
- **CameraX**: For accessing and controlling the camera hardware.
- **TextToSpeech API**: For converting text responses into spoken words.
- **SpeechRecognizer API**: For recognizing and processing voice commands.
- **Google Generative AI**: For analyzing images and generating descriptive content.
- **Coroutines**: For managing asynchronous tasks and background operations.

## System Requirements

### Android Manifest Requirements

```xml
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.VIBRATE"/>
<uses-feature android:name="android.hardware.camera" android:required="true"/>
<uses-feature android:name="android.hardware.camera.autofocus"/>
<uses-feature android:name="android.hardware.microphone" android:required="true"/>
```

### Device Requirements
- Android 5.0 (API level 21) or higher
- Camera with autofocus capability
- Microphone
- Internet connectivity
- GPS enabled
- Minimum 2GB RAM
- 100MB available storage

## Getting Started

### Prerequisites

- Android Studio installed on your development machine.
- An Android device or emulator running Android 5.0 (Lollipop) or higher.

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/dreamspace-academy/dreamhack-2025-metro-dev.git
   cd visionpath
   ```

2. **Open the project in Android Studio**:
    - Launch Android Studio.
    - Select "Open an existing Android Studio project".
    - Navigate to the cloned repository and select the `visionpath` directory.

3. **Build the project**:
    - Click on the "Build" menu and select "Make Project" or press `Ctrl+F9`.

4. **Run the application**:
    - Connect your Android device or start an emulator.
    - Click on the "Run" menu and select "Run 'app'" or press `Shift+F10`.

### Permissions

The application requires the following permissions:

- **Camera**: To capture live frames for analysis.
- **Record Audio**: To process voice commands.
- **Internet**: To communicate with the AI services.

### Usage

- **Blind Mode**: Starts automatically upon launching the app. Provides real-time guidance.
- **Assistant Mode**: Activated by pausing navigation and using voice commands.
- **Reading Mode**: Activated by long-pressing on the screen. Reads text from captured images.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request for any improvements or bug fixes.

## Contact

For any inquiries or support, please contact Metro Dev.
