package com.watson.devoxx;

import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;

import javax.sound.sampled.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by nfriedly on 11/3/16.
 */
public class MicToConsole {

    public static void main(String[] args) throws LineUnavailableException, InterruptedException, IOException {
        MicToConsole watsonExample = new MicToConsole();
        watsonExample.transcribeMic();
    }

    Properties props = new Properties();
    SpeechToText stt = new SpeechToText();

    public MicToConsole() throws IOException, LineUnavailableException, InterruptedException {
        props.load(new FileInputStream("watson.properties"));
        stt.setUsernameAndPassword(props.getProperty("SPEECH_TO_TEXT_USERNAME"), props.getProperty("SPEECH_TO_TEXT_PASSWORD"));
        transcribeMic();
    }

    public void transcribeMic() throws LineUnavailableException, InterruptedException, IOException {
        SpeechToText service = stt;

        // Signed PCM AudioFormat with 16kHz, 16 bit sample size, 1 chanel (mono), signed, littleEndian
        int sampleRate = 16000;
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line not supported");
            System.exit(0);
        }

        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        AudioInputStream audio = new AudioInputStream(line);

        RecognizeOptions options = new RecognizeOptions.Builder()
                .continuous(true)
                .interimResults(true)
                //.inactivityTimeout(5) // use this to stop listening when the speaker pauses, i.e. for 5s
                .contentType(HttpMediaType.AUDIO_RAW + "; rate=" + sampleRate)
                .build();

        // logs raw json results to console
//        service.recognizeUsingWebSocket(audio, options, new BaseRecognizeCallback() {
//            @Override
//            public void onTranscription(SpeechResults speechResults) {
//                System.out.println(speechResults);
//            }
//        });

        // logs word-by-word output to the console, with occasional corrections
        stt.recognizeUsingWebSocket(audio, options, new BaseRecognizeCallback() {
                private int lastLineLength = 0;

                @Override
                public void onTranscription(SpeechResults speechResults) {
                    // first backspace whatever was leftover
                    System.out.print(new String(new char[lastLineLength]).replace("\0", "\b"));

                    // pull the first transcript out
                    String line = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();

                    if (speechResults.getResults().get(0).isFinal()) {
                        // for final results
                        System.out.println(line);
                        // reset for the next line
                        lastLineLength = 0;
                        // now translate it to spanish
                        // for now, we're just going to block things so that the translation and results don't clobber eachother
                    } else {
                        // print the current results so far
                        System.out.print(line);
                        lastLineLength = line.length();
                    }
                }
            });

        System.out.println("Listening to your voice for the next 30s...");
        Thread.sleep(30 * 1000);

// closing the WebSockets underlying InputStream will close the WebSocket itself.
        line.stop();
        line.close();

        System.out.println("Fin.");


    }
}
