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
     * @param mainDirectory File, the directory that contains your train and test email folders.
     * @return List (TestFile), TestFile objects containing the probability that each of the emails in the test
     * folder are spam.
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
        HashMap<String, Integer> trainSpamFreq = createWordMap(spamFiles);
        HashMap<String, Integer> trainHamFreq = createWordMap(hamFiles);

        //number of files in each folder, used in calculations later
        int spamFileCount = spamFiles.length;
        int hamFileCount = hamFiles.length;

        //calculating the probability a file is spam given a word
        HashMap<String, Double> ProbSpamGivenWord = calculateProbSpamGivenWord(trainSpamFreq, trainHamFreq, spamFileCount, hamFileCount);

        //calculating the full probability that each file is spam, returning a list of testFile objects
        return calculateProbSpamGivenFile(mainDirectory, ProbSpamGivenWord);
    }

    /**
     * @param files File[],  list of files for which we will return the corresponding word map.
     * @return Hashmap (String, Integer), map containing of all the words in a file array, and in how many files they appear.
     */
    public HashMap<String, Integer> createWordMap(File[] files) throws FileNotFoundException {
        //this will be the full map for each directory, storing (spam or ham)
        HashMap<String, Integer> masterMap = new HashMap<>();
        for (File file : files) {
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
            //for each file, we will add our values to the master map, containing words from all files
            for (String key : currentFileMap.keySet()) {
                if (!masterMap.containsKey(key)) {
                    masterMap.put(key, 1);
                }
                else {
                    masterMap.put(key, masterMap.get(key)+1);
                }
            }
        }
        //return our final map
        return masterMap;
    }

    /**
     * @param trainSpamFreq a Hashmap (String, Integer), containing of all the words in a spam file array, and in how many
     * files they appear.
     * @param trainHamFreq a Hashmap (String, Integer), containing of all the words in a ham file array, and in how many
     * files they appear.
     * @param hamCount the number of ham files used to generate the hashmap trainHamFreq
     * @param spamCount the number of spam files used to generate the hashmap trainHamFreq
     * @return a Hashmap (String, Double), containing all the probabilities of spam given a single word (for all
     * words in either Hashmap provided
     */
    public HashMap<String, Double> calculateProbSpamGivenWord(HashMap<String, Integer> trainSpamFreq, HashMap<String, Integer> trainHamFreq, int spamCount, int hamCount) {
        HashMap<String, Double> probSpamGivenWord = new HashMap<>();

        //iterating through the spam words, and calculating the probability of spam given said word using a
        //simplified bayesian formula (given in the assignment instructions)
        for (String word : trainSpamFreq.keySet()) {
            double probWordGivenSpam = ((double)trainSpamFreq.get(word))/((double)spamCount);
            double probWordGivenHam = 0.0;
            if (trainHamFreq.get(word) != null) {
                probWordGivenHam = ((double)trainHamFreq.get(word))/((double)hamCount);
            }
            probSpamGivenWord.put(word, (probWordGivenSpam/(probWordGivenSpam+probWordGivenHam)));
        }
        //iterating through the words found in ham files
        for (String word : trainHamFreq.keySet()) {
            //if the word was already covered, we do not consider it
            if (!probSpamGivenWord.containsKey(word)) {
                probSpamGivenWord.put(word, 0.0);
                //in this case, if the word shows up in our ham frequency hashmap but not our spam frequency hashmap,
                //then our formula simplifies to 0, so we can save computations here
            }
        }
        return probSpamGivenWord;
    }

    /**
     * @param mainDirectory File, the directory that contains test files.
     * @param ProbSpamGivenWord HashMap (String, Double), the probability of spam given each word.
     * @return ArrayList (TestFile), ArrayList of TestFile objects, which contain the probability that each file is spam.
     */
    public ArrayList<TestFile> calculateProbSpamGivenFile(File mainDirectory, HashMap<String, Double> ProbSpamGivenWord) throws FileNotFoundException {

        File testSpamDir = new File(mainDirectory, "/test/spam");
        File testHamDir = new File(mainDirectory, "/test/ham");

        //gathering all files in test directories
        File[] spamTestFiles = testSpamDir.listFiles();
        File[] hamTestFiles = testHamDir.listFiles();

        //this array will be passed in to probability calculator function, modified, and returned
        ArrayList<TestFile> ProbSpamGivenFile = new ArrayList<>();
        ProbSpamGivenFile = probabilityCalculator(ProbSpamGivenFile, spamTestFiles, "spam", ProbSpamGivenWord);
        ProbSpamGivenFile = probabilityCalculator(ProbSpamGivenFile, hamTestFiles, "ham", ProbSpamGivenWord);

        return ProbSpamGivenFile;
    }

    /**
     * @param files File[], the files that will be considered.
     * @param actualClass String, the actual class of the list of files passed in.
     * @param probSpamGivenFile ArrayList (TestFile), the array list which we will add our values to, and later return.
     * @param ProbSpamGivenWord HashMap (String, Double), the probability of spam given a word.
     * @return the modified ArrayList of TestFile objects which was passed in.
     */
    public ArrayList<TestFile> probabilityCalculator (ArrayList<TestFile> probSpamGivenFile, File[] files, String actualClass, HashMap<String, Double> ProbSpamGivenWord) throws FileNotFoundException {
        for (File file : files) {
            if (file.exists()) {
                //iterating through each word in the file, parsing by spaces
                Scanner scanner = new Scanner(file);
                double sum = 0;
                while (scanner.hasNext()) {
                    //convert to lower case to ignore case in calculations
                    String word = (scanner.next()).toLowerCase();
                    if (ProbSpamGivenWord.containsKey(word)) {
                        //log of 0 or 1 will mess up the calculation, and a single word will define the file
                        if (ProbSpamGivenWord.get(word) != 0.0 && ProbSpamGivenWord.get(word) != 1.0) {
                            sum += Math.log(1-ProbSpamGivenWord.get(word)) - Math.log(ProbSpamGivenWord.get(word));
                        }
                        //instead of 0 and 1, we use values close, but far enough away from 0 and 1 that they are not
                        //overpowering the spam probability of the file
                        else if (ProbSpamGivenWord.get(word) == 0.0) {
                            sum += Math.log(1-0.16) - Math.log(0.16);
                        }
                        else {
                            sum += Math.log(1-0.84) - Math.log(0.84);
                        }
                    }
                    //else we do not consider this word in our algorithm since we did not encounter it in training
                }
                //now we calculate the final probability that the file is spam, and multiply it by 100 to be displayed as %
                double currentFileSpamProb = 1/(1 + (Math.pow(Math.E, sum)));
                probSpamGivenFile.add(new TestFile(file.getName(), currentFileSpamProb*100, actualClass));
            }
        }
        return probSpamGivenFile;
    }

    /**
     * @param word String, the string which you want to check.
     * @return Boolean, whether the given string is a word.
     */
    private boolean isWord(String word) {
        if (word == null) {
            return false;
        }
        String pattern = "^[a-zA-Z]*$";
        return word.matches(pattern);
    }
}
