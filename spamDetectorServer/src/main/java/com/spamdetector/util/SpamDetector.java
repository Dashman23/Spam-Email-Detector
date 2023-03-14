package com.spamdetector.util;

import com.spamdetector.domain.TestFile;

import java.io.*;
import java.lang.Math;
import java.util.*;


/**
 * TODO: This class will be implemented by you
 * You may create more methods to help you organize you strategy and make you code more readable
 */
public class SpamDetector {

    /**
     * @param mainDirectory the directory that contains your train and test email folders
     * @return List of TestFile objects containing the probability that each of the emails in the test
     * folder are spam
     */
    public List<TestFile> trainAndTest(File mainDirectory) throws FileNotFoundException {
//      TODO: main method of loading the directories and files, training and testing the model

        //storing each file directory in a file object
        File spamDir = new File(mainDirectory, "/train/spam");
        File ham1Dir = new File(mainDirectory, "/train/ham");
        File ham2Dir = new File(mainDirectory, "/train/ham2");

        //collecting each training file in file arrays
        File[] spamFiles = spamDir.listFiles();
        File[] ham1Files = ham1Dir.listFiles();
        File[] ham2Files = ham2Dir.listFiles();

        //combining the two ham files arrays into one array
        int count = 0;
        File[] hamFiles = new File[ham1Files.length + ham2Files.length];
        for (File ham1File : ham1Files) {
            hamFiles[count++] = ham1File;
        }
        for (File ham2File : ham2Files) {
            hamFiles[count++] = ham2File;
        }

        //creating a word map of all words in the spam and ham emails, and in how many emails they appear
        //HashCount class has two instance variables, so we can return the count and hashmap
        HashCount spam = createWordMap(spamFiles);
        HashCount ham = createWordMap(hamFiles);

        //unpacking the variables from the custom class
        HashMap<String, Integer> trainSpamFreq = spam.hashMap;
        int spamFileCount = spam.count;
        HashMap<String, Integer> trainHamFreq = ham.hashMap;
        int hamFileCount = ham.count;

        //calculating the probability a file is spam given a word
        HashMap<String, Double> ProbSpamGivenWord = calculateProbSpamGivenWord(trainSpamFreq, trainHamFreq, spamFileCount, hamFileCount);

        //calculating the full probability that each file is spam, returning a list of testFile objects
        return calculateProbSpamGivenFile(mainDirectory, ProbSpamGivenWord);
    }

    /**
     * @param files a list of files for which
     * @return a HashCount object, containing of all the words in a file array, and in how many files they appear in,
     *  as well as how many files are in the array
     */
    public HashCount createWordMap(File[] files) throws FileNotFoundException {
        //this will be the full map for each directory, storing (spam or ham)
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

    public HashMap<String, Double> calculateProbSpamGivenWord(HashMap<String, Integer> trainSpamFreq, HashMap<String, Integer> trainHamFreq, int spamCount, int hamCount) {
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
                        else if (ProbSpamGivenWord.get(word) == 0.0) {
                            sum += Math.log(1-0.15) - Math.log(0.15);
                        }
                        else {
                            sum += Math.log(1-0.85) - Math.log(0.85);
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