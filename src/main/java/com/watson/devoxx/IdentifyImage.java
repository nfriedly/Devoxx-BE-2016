package com.watson.devoxx;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;

import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by nfriedly on 11/7/16.
 */
public class IdentifyImage {

    public static void main(String[] args) throws LineUnavailableException, InterruptedException, IOException {
        MicToConsole watsonExample = new MicToConsole();
        watsonExample.transcribeMic();
    }

    Properties props = new Properties();
    VisualRecognition service = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);


    public IdentifyImage() throws IOException, LineUnavailableException, InterruptedException {
        props.load(new FileInputStream("watson.properties"));
        service.setApiKey(props.getProperty("VISUAL_RECOGNITION_API_KEY"));
        identifyImage();
    }

    public void identifyImage() {

        System.out.println("Classify an image");
        ClassifyImagesOptions options = new ClassifyImagesOptions.Builder()
                .images(new File("src/main/resources/dog.png"))
                .build();
        VisualClassification result = service.classify(options).execute();
        System.out.println(result);
    }
}
