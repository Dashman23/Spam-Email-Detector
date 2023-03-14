package com.spamdetector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spamdetector.domain.TestFile;
import com.spamdetector.util.SpamDetector;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import jakarta.ws.rs.core.Response;

@Path("/spam")
public class SpamResource {

//    your SpamDetector Class responsible for all the SpamDetecting logic
    SpamDetector detector = new SpamDetector();
    ObjectMapper mapper = new ObjectMapper();
    List<TestFile> testFiles;
    private final double threshold = 0.5;

    public SpamResource() throws FileNotFoundException {
//      DONE: load resources, train and test to improve performance on the endpoint calls
        System.out.print("Training and testing the model, please wait");

//      DONE: call this.trainAndTest();
        this.testFiles = this.trainAndTest();
    }
    @GET
    @Produces("application/json")
    public Response getSpamResults() throws JsonProcessingException {
//       DONE: return the test results list of TestFile, return in a Response object

        Response myResp = Response.status(200).header("Access-Control-Allow-Origin", "http://localhost:63342")
                .header("Content-Type", "application/json")
                .entity(mapper.writeValueAsString(testFiles))
                .build();

        return myResp;
    }

    @GET
    @Path("/accuracy")
    @Produces("application/json")
    public Response getAccuracy() throws JsonProcessingException {
//      DONE: return the accuracy of the detector, return in a Response object
        // calculate accuracy bu divding the number of correct guesses by number of total guesses
        // a guess is considered correct if the spam probability is above the threshold and the file is actually spam or the spam likelyhood percent
        // is less than the threshold and the file is actually ham
        double correctGuesses = 0;
        for(TestFile testFile : testFiles) {
            if (testFile.getSpamProbability() >= threshold && testFile.getActualClass().equals("spam")) {
                correctGuesses++;
            } else if (testFile.getSpamProbability() < threshold && testFile.getActualClass().equals("ham")) {
                correctGuesses++;
            }
        }
        double accuracy = correctGuesses / testFiles.size();
        Response myResp = Response.status(200).header("Access-Control-Allow-Origin", "*")
                .header("Content-Type", "application/json")
                .entity(mapper.writeValueAsString(accuracy))
                .build();

        return myResp;
    }

    @GET
    @Path("/precision")
    @Produces("application/json")
    public Response getPrecision() throws JsonProcessingException {
       //      DONE: return the precision of the detector, return in a Response object
        // calculate precision by doing the number of true positives divided by the sum of false positives and true positives
        // a true positive is counted when the spam probability is above the threshold and the file is actually spam
        // a false positive is counted when the spam probability is above the treshold and the file is actually ham
        double numTruePositives = 0;
        double numFalsePositives = 0;

        for (TestFile testFile : testFiles) {
            if (testFile.getSpamProbability() >= threshold && testFile.getActualClass().equals("spam")) {
                numTruePositives++;
            } else if (testFile.getSpamProbability() >= threshold && testFile.getActualClass().equals("ham")) {
                numFalsePositives++;
            }
        }

        double precision = (numTruePositives) / (numTruePositives + numFalsePositives);

        Response myResp = Response.status(200).header("Access-Control-Allow-Origin", "*")
                .header("Content-Type", "application/json")
                .entity(mapper.writeValueAsString(precision))
                .build();

        return myResp;
    }

    private List<TestFile> trainAndTest() throws FileNotFoundException {
        if (this.detector==null){
            this.detector = new SpamDetector();
        }

//        DONE: load the main directory "data" here from the Resources folder
        URL url = this.getClass().getClassLoader().getResource("/data");
        File mainDirectory = null;
        try {
            mainDirectory = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return this.detector.trainAndTest(mainDirectory);
    }
}