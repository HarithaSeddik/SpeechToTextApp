# AudioTranscriberApp

An app that is used for listening to always listen to audio and then sending over to the backend and transcribing into text to be shown on the screen.

# Initial Approach

Initially, I wanted to implement the audio-recording on Kotlin, and send 5-second worth of audio chunks to Flutter over method channels. Using method channels is ideal for more discrete forms of data. For this challenge where we need continuous data streams, EventChannels are streams that work bests for such sensor-data applications.

# Challenges, and points for improvement.

1. Initially, I started with a 'AlwaysListeningService.kt' kotlin class where I had implemented my EventChannel.
   However, I faced a run-time issue for hours regarding having a 'missing plugin exception' when I called the `_audioStreamChannel.receiveBroadcastStream().listen` from Flutter. I knew the issue had to do with accessing the same Flutter engine instance in my MainActivity, but I didn't have enough time to implement Flutter-engine cacheing with a unique engine ID. For that reason, I did all my implementation under MainActivity.kt. Not ideal but if I had time I would improve this.
2. Better error handling,and more rich and meaningful logging
3. Unit tests for the api service class.
4. Fix issues with audio buffers and make sure they are error-free
5. I would ideally not have all my UI widgets on my main.dart, I would have a UI-domain and I would also break my Widgets into smaller widgets rather than using \_build methods like \_buildTranscriptionSection. Having separate Widgets improves rendering performance
6. Implement a Foreground service on Kotlin so that my audio-recording would have a higher priority and persist the recording during background activity.

# Example for how to use the 'wav' Dart plugin

https://flutterawesome.com/dart-package-for-reading-and-writing-wav-files/

## Note:

faced an error while using API:
"I/flutter (23374): HandshakeException: Handshake error in client (OS Error:
I/flutter (23374): CERTIFICATE_VERIFY_FAILED: self signed certificate(handshake.cc:393))
E/flutter (23374): [ERROR:flutter/runtime/dart_vm_initializer.cc(41)] Unhandled Exception: Exception: Failed to upload file: HandshakeException: Handshake error in client (OS Error:
E/flutter (23374): CERTIFICATE_VERIFY_FAILED: self signed certificate(handshake.cc:393))
E/flutter (23374): #0 SttApiService.sendAudioFile
api_service.dart:44
E/flutter (23374): <asynchronous suspension>""

In order to bypass the SSL verification, I added an http override in api_service.dart:
"class CustomHttpOverrides extends HttpOverrides {
@override
HttpClient createHttpClient(SecurityContext? context) {
return super.createHttpClient(context)
..badCertificateCallback =
(X509Certificate cert, String host, int port) => true;
}
}"

This is strictly for debugging
