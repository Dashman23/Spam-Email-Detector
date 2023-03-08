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
//       TODO: return the test results list of TestFile, return in a Response object

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
//      TODO: return the accuracy of the detector, return in a Response object
        double correctGuesses = 0;


        for(TestFile testFile : testFiles) {
            if (testFile.getSpamProbability() >= threshold && testFile.getActualClass() == "spam") {
                correctGuesses++;
            } else if (testFile.getSpamProbability() < threshold && testFile.getActualClass() == "ham") {
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
       //      TODO: return the precision of the detector, return in a Response object

        double numTruePositives = 0;
        double numFalsePositives = 0;

        for (TestFile testFile : testFiles) {
            if (testFile.getSpamProbability() >= threshold && testFile.getActualClass() == "spam") {
                numTruePositives++;
            } else if (testFile.getSpamProbability() >= threshold && testFile.getActualClass() == "ham") {
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
        File mainDirectory = new File("/Users/dashman/Desktop/Year_2/Software_Dev/Project/w23-csci2020u-assignment01-its-shrimple-rly/spamDetectorServer/src/main/resources/data");
        return this.detector.trainAndTest(mainDirectory);
    }
}