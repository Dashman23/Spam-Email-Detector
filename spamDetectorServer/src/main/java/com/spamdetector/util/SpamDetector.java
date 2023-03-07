package com.spamdetector.util;

import com.spamdetector.domain.TestFile;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;


/**
 * TODO: This class will be implemented by you
 * You may create more methods to help you organize you strategy and make you code more readable
 */
public class SpamDetector {

    public List<TestFile> trainAndTest(File mainDirectory) {
//        TODO: main method of loading the directories and files, training and testing the model

        ArrayList<TestFile> fileDataList = new ArrayList<TestFile>();
        File spamDir = new File(mainDirectory, "/spam");
        File hamDir = new File(mainDirectory, "/ham");

        File[] spamFiles = spamDir.listFiles();
        File[] hamFiles = hamDir.listFiles();

        HashMap<String, Integer> spamMap = createWordMap(spamFiles);
        HashMap<String, Integer> hamMap = createWordMap(hamFiles);

        return fileDataList;
    }

    public HashMap<String, Integer> createWordMap(File[] files) {
        return null;
    }

    public double calculateSpamProbability(File file) {
        return 0.0;
    }

}