package com.k2fsa.sherpa.onnx

import android.animation.ObjectAnimator
import android.content.res.AssetManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.WindowCompat
import com.airbnb.lottie.LottieAnimationView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.Normalizer

const val TAG = "sherpa-onnx"

class MainActivity : AppCompatActivity() {
    private var tts: OfflineTts? = null
    private lateinit var text: EditText
    private lateinit var sid: EditText
    private lateinit var speed: EditText
    private lateinit var generate: Button
    private lateinit var play: Button
    private lateinit var stop: Button
    private var stopped: Boolean = false
    private var mediaPlayer: MediaPlayer? = null
    private var isInitialized = false

    // UI Animation elements
    private lateinit var lottieMic: LottieAnimationView
    private lateinit var lottieWaveform: LottieAnimationView
    private lateinit var statusText: TextView
    private lateinit var mainCard: CardView

    // see
    // https://developer.android.com/reference/kotlin/android/media/AudioTrack
    private var track: AudioTrack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()
        startEntranceAnimations()

        // Set default values for hidden fields
        sid.setText("0")
        speed.setText("1.0")

        // Vietnamese sample text for testing TTS
        val sampleText = "Xin chào, tôi là trợ lý ảo. Rất vui được gặp bạn. Hôm nay thời tiết rất đẹp."
        text.setText(sampleText)

        play.isEnabled = false
        generate.isEnabled = false
        updateButtonStates()

        // Initialize TTS in background thread
        Thread {
            try {
                Log.i(TAG, "Start to initialize TTS")
                initTts()
                Log.i(TAG, "Finish initializing TTS")

                Log.i(TAG, "Start to initialize AudioTrack")
                initAudioTrack()
                Log.i(TAG, "Finish initializing AudioTrack")

                isInitialized = true

                runOnUiThread {
                    generate.isEnabled = true
                    updateButtonStates()
                    showStatus("✅ Ready to generate speech")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize TTS: ${e.message}")
                runOnUiThread {
                    showStatus("❌ Failed to initialize TTS")
                    Toast.makeText(this, "Failed to initialize TTS engine", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun initViews() {
        text = findViewById(R.id.text)
        sid = findViewById(R.id.sid)
        speed = findViewById(R.id.speed)
        generate = findViewById(R.id.generate)
        play = findViewById(R.id.play)
        stop = findViewById(R.id.stop)

        // Animation elements
        lottieMic = findViewById(R.id.lottie_mic)
        lottieWaveform = findViewById(R.id.lottie_waveform)
        statusText = findViewById(R.id.status_text)
        mainCard = findViewById(R.id.main_card)
    }

    private fun setupClickListeners() {
        generate.setOnClickListener {
            animateButtonPress(it)
            onClickGenerate()
        }
        play.setOnClickListener {
            animateButtonPress(it)
            onClickPlay()
        }
        stop.setOnClickListener {
            animateButtonPress(it)
            onClickStop()
        }
    }

    private fun animateButtonPress(view: View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun startEntranceAnimations() {
        // Card entrance animation
        mainCard.alpha = 0f
        mainCard.translationY = 100f
        mainCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun showStatus(message: String) {
        statusText.visibility = View.VISIBLE
        statusText.text = message
        statusText.alpha = 0f
        statusText.animate().alpha(1f).setDuration(200).start()
    }

    private fun showGeneratingState() {
        runOnUiThread {
            lottieWaveform.visibility = View.VISIBLE
            lottieWaveform.playAnimation()
            showStatus("🎙️ Generating speech...")
        }
    }

    private fun hideGeneratingState() {
        runOnUiThread {
            lottieWaveform.pauseAnimation()
            lottieWaveform.visibility = View.GONE
            showStatus("✅ Speech generated successfully!")
            statusText.postDelayed({
                statusText.animate().alpha(0f).setDuration(300).withEndAction {
                    statusText.visibility = View.GONE
                }.start()
            }, 2000)
        }
    }

    private fun updateButtonStates() {
        play.alpha = if (play.isEnabled) 1f else 0.5f
        generate.alpha = if (generate.isEnabled) 1f else 0.5f
    }

    private fun initAudioTrack() {
        val sampleRate = tts?.sampleRate() ?: 22050
        val bufLength = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        Log.i(TAG, "sampleRate: $sampleRate, buffLength: $bufLength")

        val attr = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        val format = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setSampleRate(sampleRate)
            .build()

        track = AudioTrack(
            attr, format, bufLength, AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        track?.play()
    }

    // this function is called from C++
    private fun callback(samples: FloatArray): Int {
        if (!stopped) {
            track?.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
            return 1
        } else {
            track?.stop()
            return 0
        }
    }

    private fun onClickGenerate() {
        val sidInt = sid.text.toString().toIntOrNull()
        if (sidInt == null || sidInt < 0) {
            Toast.makeText(
                applicationContext,
                "Please input a non-negative integer for speaker ID!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val speedFloat = speed.text.toString().toFloatOrNull()
        if (speedFloat == null || speedFloat <= 0) {
            Toast.makeText(
                applicationContext,
                "Please input a positive number for speech speed!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        var textStr = text.text.toString().trim()
        if (textStr.isBlank() || textStr.isEmpty()) {
            Toast.makeText(applicationContext, "Please input a non-empty text!", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Normalize Vietnamese text to NFC form for proper diacritic handling
        textStr = Normalizer.normalize(textStr, Normalizer.Form.NFC)

        track?.pause()
        track?.flush()
        track?.play()

        play.isEnabled = false
        generate.isEnabled = false
        stopped = false
        showGeneratingState()
        Thread {
            val audio = tts?.generateWithCallback(
                text = textStr,
                sid = sidInt,
                speed = speedFloat,
                callback = this::callback
            )

            val filename = application.filesDir.absolutePath + "/generated.wav"
            val ok = audio?.samples?.size ?: 0 > 0 && audio?.save(filename) == true
            if (ok) {
                runOnUiThread {
                    hideGeneratingState()
                    play.isEnabled = true
                    generate.isEnabled = true
                    track?.stop()
                }
            }
        }.start()
    }

    private fun onClickPlay() {
        val filename = application.filesDir.absolutePath + "/generated.wav"
        mediaPlayer?.stop()
        mediaPlayer = MediaPlayer.create(
            applicationContext,
            Uri.fromFile(File(filename))
        )
        mediaPlayer?.start()
    }

    private fun onClickStop() {
        stopped = true
        play.isEnabled = true
        generate.isEnabled = true
        track?.pause()
        track?.flush()
        mediaPlayer?.stop()
        mediaPlayer = null
    }

    private fun initTts() {
        var modelDir: String?
        var modelName: String?
        var acousticModelName: String?
        var vocoder: String?
        var voices: String?
        var ruleFsts: String?
        var ruleFars: String?
        var lexicon: String?
        var dataDir: String?
        var assets: AssetManager? = application.assets
        var isKitten = false

        // The purpose of such a design is to make the CI test easier
        // Please see
        // https://github.com/k2-fsa/sherpa-onnx/blob/master/scripts/apk/generate-tts-apk-script.py

        // VITS -- begin
        modelName = null
        // VITS -- end

        // Matcha -- begin
        acousticModelName = null
        vocoder = null
        // Matcha -- end

        // For Kokoro -- begin
        voices = null
        // For Kokoro -- end


        modelDir = null
        ruleFsts = null
        ruleFars = null
        lexicon = null
        dataDir = null

//
//        modelDir = "exported"
//        modelName = "model_fixed.onnx"
//        dataDir = "exported/espeak-ng-data"

        // Working Piper model (backup):
         modelDir = "vits-piper-vi_VN-vais1000-medium"
         modelName = "vi_VN-vais1000-medium.onnx"
         dataDir = "vits-piper-vi_VN-vais1000-medium/espeak-ng-data"

        // Example 1:
        // modelDir = "vits-vctk"
        // modelName = "vits-vctk.onnx"
        // lexicon = "lexicon.txt"

        // Example 2:
        // https://github.com/k2-fsa/sherpa-onnx/releases/tag/tts-models
        // https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-piper-en_US-amy-low.tar.bz2
        // modelDir = "vits-piper-en_US-amy-low"
        // modelName = "en_US-amy-low.onnx"
        // dataDir = "vits-piper-en_US-amy-low/espeak-ng-data"

        // Example 3:
        // https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-icefall-zh-aishell3.tar.bz2
        // modelDir = "vits-icefall-zh-aishell3"
        // modelName = "model.onnx"
        // ruleFars = "vits-icefall-zh-aishell3/rule.far"
        // lexicon = "lexicon.txt"

        // Example 4:
        // https://k2-fsa.github.io/sherpa/onnx/tts/pretrained_models/vits.html#csukuangfj-vits-zh-hf-fanchen-c-chinese-187-speakers
        // modelDir = "vits-zh-hf-fanchen-C"
        // modelName = "vits-zh-hf-fanchen-C.onnx"
        // lexicon = "lexicon.txt"

        // Example 5:
        // https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/vits-coqui-de-css10.tar.bz2
        // modelDir = "vits-coqui-de-css10"
        // modelName = "model.onnx"

        // Example 6
        // vits-melo-tts-zh_en
        // https://k2-fsa.github.io/sherpa/onnx/tts/pretrained_models/vits.html#vits-melo-tts-zh-en-chinese-english-1-speaker
        // modelDir = "vits-melo-tts-zh_en"
        // modelName = "model.onnx"
        // lexicon = "lexicon.txt"

        // Example 7
        // matcha-icefall-zh-baker
        // https://k2-fsa.github.io/sherpa/onnx/tts/pretrained_models/matcha.html#matcha-icefall-zh-baker-chinese-1-female-speaker
        // modelDir = "matcha-icefall-zh-baker"
        // acousticModelName = "model-steps-3.onnx"
        // vocoder = "vocos-22khz-univ.onnx"    // Vocoder should be downloaded separately; place in the **root directory of your resources folder**, not under modelDir.
        // lexicon = "lexicon.txt"

        // Example 8
        // matcha-icefall-en_US-ljspeech
        // https://k2-fsa.github.io/sherpa/onnx/tts/pretrained_models/matcha.html#matcha-icefall-en-us-ljspeech-american-english-1-female-speaker
        // modelDir = "matcha-icefall-en_US-ljspeech"
        // acousticModelName = "model-steps-3.onnx"
        // vocoder = "vocos-22khz-univ.onnx"
        // dataDir = "matcha-icefall-en_US-ljspeech/espeak-ng-data"

        // Example 9
        // kokoro-en-v0_19
        // modelDir = "kokoro-en-v0_19"
        // modelName = "model.onnx"
        // voices = "voices.bin"
        // dataDir = "kokoro-en-v0_19/espeak-ng-data"

        // Example 10
        // kokoro-multi-lang-v1_0
        // modelDir = "kokoro-multi-lang-v1_0"
        // modelName = "model.onnx"
        // voices = "voices.bin"
        // dataDir = "kokoro-multi-lang-v1_0/espeak-ng-data"
        // lexicon = "kokoro-multi-lang-v1_0/lexicon-us-en.txt,kokoro-multi-lang-v1_0/lexicon-zh.txt"
        // ruleFsts = "$modelDir/phone-zh.fst,$modelDir/date-zh.fst,$modelDir/number-zh.fst"

        // Example 11
        // kitten-nano-en-v0_1-fp16
        // modelDir = "kitten-nano-en-v0_1-fp16"
        // modelName = "model.fp16.onnx"
        // voices = "voices.bin"
        // dataDir = "kokoro-multi-lang-v1_0/espeak-ng-data"
        // isKitten = true

        // Example 12
        // matcha-icefall-zh-en
        // https://k2-fsa.github.io/sherpa/onnx/tts/all/Chinese-English/matcha-icefall-zh-en.html
        // modelDir = "matcha-icefall-zh-en"
        // acousticModelName = "model-steps-3.onnx"
        // vocoder = "vocos-16khz-univ.onnx"    // Vocoder should be downloaded separately; place in the **root directory of your resources folder**, not under modelDir.
        // dataDir = "matcha-icefall-zh-en/espeak-ng-data"
        // lexicon = "lexicon.txt"

        if (dataDir != null) {
            val newDir = copyDataDir(dataDir!!)
            dataDir = "$newDir/$dataDir"
        }

        val config = getOfflineTtsConfig(
            modelDir = modelDir!!,
            modelName = modelName ?: "",
            acousticModelName = acousticModelName ?: "",
            vocoder = vocoder ?: "",
            voices = voices ?: "",
            lexicon = lexicon ?: "",
            dataDir = dataDir ?: "",
            dictDir = "",
            ruleFsts = ruleFsts ?: "",
            ruleFars = ruleFars ?: "",
            isKitten = isKitten,
        )!!

        tts = OfflineTts(assetManager = assets, config = config)
    }


    private fun copyDataDir(dataDir: String): String {
        Log.i(TAG, "data dir is $dataDir")
        copyAssets(dataDir)

        val newDataDir = application.getExternalFilesDir(null)!!.absolutePath
        Log.i(TAG, "newDataDir: $newDataDir")
        return newDataDir
    }

    private fun copyAssets(path: String) {
        val assets: Array<String>?
        try {
            assets = application.assets.list(path)
            if (assets!!.isEmpty()) {
                copyFile(path)
            } else {
                val fullPath = "${application.getExternalFilesDir(null)}/$path"
                val dir = File(fullPath)
                dir.mkdirs()
                for (asset in assets.iterator()) {
                    val p: String = if (path == "") "" else path + "/"
                    copyAssets(p + asset)
                }
            }
        } catch (ex: IOException) {
            Log.e(TAG, "Failed to copy $path. $ex")
        }
    }

    private fun copyFile(filename: String) {
        try {
            val istream = application.assets.open(filename)
            val newFilename = application.getExternalFilesDir(null).toString() + "/" + filename
            val ostream = FileOutputStream(newFilename)
            // Log.i(TAG, "Copying $filename to $newFilename")
            val buffer = ByteArray(1024)
            var read = 0
            while (read != -1) {
                ostream.write(buffer, 0, read)
                read = istream.read(buffer)
            }
            istream.close()
            ostream.flush()
            ostream.close()
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to copy $filename, $ex")
        }
    }
}
