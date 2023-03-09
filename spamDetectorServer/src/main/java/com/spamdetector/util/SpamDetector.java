package com.spamdetector.util;

import com.spamdetector.domain.TestFile;

import java.io.*;
import java.lang.Math;
import java.lang.reflect.Array;
import java.util.*;


/**
 * TODO: This class will be implemented by you
 * You may create more methods to help you organize you strategy and make you code more readable
 */
public class SpamDetector {

    public List<TestFile> trainAndTest(File mainDirectory) throws FileNotFoundException {
//      TODO: main method of loading the directories and files, training and testing the model

        File spamDir = new File(mainDirectory, "/train/spam");
        File hamDir = new File(mainDirectory, "/train/ham");
        File ham2Dir = new File(mainDirectory, "/train/ham2");

        File[] spamFiles = spamDir.listFiles();
        File[] hamFiles = hamDir.listFiles();
        File[] ham2Files = ham2Dir.listFiles();


        HashCount spam = createWordMap(spamFiles);
        HashCount ham = createWordMap(hamFiles);
        HashCount ham2 = createWordMap(ham2Files);
        //HashCount class has two instance variables, so we can return the count and hashmap
        HashMap<String, Integer> trainSpamFreq = spam.hashMap;
        int spamFileCount = spam.count;
        HashMap<String, Integer> trainHamFreq = ham.hashMap;
        int hamFileCount = ham.count;
        HashMap<String, Integer> trainHam2Freq = ham2.hashMap;
        int ham2FileCount = ham2.count;

        HashMap<String, Double> ProbSpamGivenWord = calculateProbSpamGivenWord(trainSpamFreq, trainHamFreq, trainHam2Freq , spamFileCount, hamFileCount, ham2FileCount);

        return calculateProbSpamGivenFile(mainDirectory, ProbSpamGivenWord);
    }

    public HashCount createWordMap(File[] files) throws FileNotFoundException {
        HashMap<String, Integer> masterMap = new HashMap<>();
        int count = 0;
        for (File file : files) {
            count += 1;
            HashMap<String, Integer> currentFileMap = new HashMap<>();
            //this currentFileMap will contain a 1 for every string it contains
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                //now we iterate through the file
                while (scanner.hasNext()) {
                    String word = (scanner.next()).toLowerCase();
                    if (isWord(word)) {
                        //if currentFileMap already has the word, we do NOT want to increment the count
                        //only add new word to current file map
                        if (!currentFileMap.containsKey(word)) {
                            currentFileMap.put(word, 1);
                        }
                    }
                }
            }
            for (String key : currentFileMap.keySet()) {
                if (!masterMap.containsKey(key)) {
                    masterMap.put(key, 1);
                }
                else {
                    masterMap.put(key, masterMap.get(key)+1);
                }
            }
        }
        //return HashCount object (custom class) which has two instance variables
        return new HashCount(masterMap, count);
    }

    public HashMap<String, Double> calculateProbSpamGivenWord(HashMap<String, Integer> trainSpamFreq, HashMap<String, Integer> trainHamFreq, HashMap<String, Integer> trainHam2Freq, int spamCount, int hamCount, int ham2Count) {
        HashMap<String, Double> probSpamGivenWord = new HashMap<>();
        for (String word : trainSpamFreq.keySet()) {
            double probWordGivenSpam = ((double)trainSpamFreq.get(word))/((double)spamCount);
            double probWordGivenHam = 0.0;
            if (trainHamFreq.get(word) != null) {
                probWordGivenHam = ((double)trainHamFreq.get(word))/((double)hamCount);
            }
            probSpamGivenWord.put(word, (probWordGivenSpam/(probWordGivenSpam+probWordGivenHam)));
        }
        for (String word : trainHamFreq.keySet()) {
            if (!probSpamGivenWord.containsKey(word)) {
                probSpamGivenWord.put(word, 0.0);
                //in this case, if the word shows up in our ham frequency hashmap but not our spam frequency hashmap,
                //then our formula simplifies to 0.
            }
        }
        for (String word : trainHam2Freq.keySet()) {//LOOK OVER DANIEL I HAVE NO CLUE IF THIS IS RIGHT
            if (!probSpamGivenWord.containsKey(word)) {
                probSpamGivenWord.put(word, 0.0);
                //in this case, if the word shows up in our ham2 frequency hashmap but not our spam frequency hashmap,
                //then our formula simplifies to 0.
            }
        }
        return probSpamGivenWord;
    }

    public ArrayList<TestFile> calculateProbSpamGivenFile(File mainDirectory, HashMap<String, Double> ProbSpamGivenWord) throws FileNotFoundException {

        File testSpamDir = new File(mainDirectory, "/test/spam");
        File testHamDir = new File(mainDirectory, "/test/ham");

        File[] spamTestFiles = testSpamDir.listFiles();
        File[] hamTestFiles = testHamDir.listFiles();

        ArrayList<TestFile> ProbSpamGivenFile = new ArrayList<>();
        ProbSpamGivenFile = probabilityCalculator(ProbSpamGivenFile, spamTestFiles, "spam", ProbSpamGivenWord);
        ProbSpamGivenFile = probabilityCalculator(ProbSpamGivenFile, hamTestFiles, "ham", ProbSpamGivenWord);

        return ProbSpamGivenFile;
    }

    public ArrayList<TestFile> probabilityCalculator (ArrayList<TestFile> probSpamGivenFile, File[] files, String actualClass, HashMap<String, Double> ProbSpamGivenWord) throws FileNotFoundException {
        for (File file : files) {
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                double sum = 0;
                while (scanner.hasNext()) {
                    String word = (scanner.next()).toLowerCase();
                    if (ProbSpamGivenWord.containsKey(word)) {
                        if (ProbSpamGivenWord.get(word) != 0.0 && ProbSpamGivenWord.get(word) != 1.0) {
                            sum += Math.log(1-ProbSpamGivenWord.get(word)) - Math.log(ProbSpamGivenWord.get(word));
                        }
                    }
                    //else we do not consider this word in our algorithm since we did not encounter it in training
                }
                double currentFileSpamProb = 1/(1 + (Math.pow(Math.E, sum)));
                probSpamGivenFile.add(new TestFile(file.getName(), currentFileSpamProb*100, actualClass));
            }
        }
        return probSpamGivenFile;
    }

    private boolean isWord(String word) {
        if (word == null) {
            return false;
        }
        String pattern = "^[a-zA-Z]*$";
        return word.matches(pattern);
    }
}

class HashCount {
    HashMap<String, Integer> hashMap;
    int count;
    HashCount(HashMap<String, Integer> hashMap, int count) {
        this.hashMap = hashMap;
        this.count = count;
    }
}