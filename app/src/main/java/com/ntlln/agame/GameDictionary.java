package com.ntlln.agame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class GameDictionary {
    private static final int MIN_NUM_ANAGRAMS = 5;
    private static final int DEFAULT_WORD_LENGTH = 3;
    private static final int MAX_WORD_LENGTH = 7;
    private Random random = new Random();
    private ArrayList<String> wordList = new ArrayList<String>();
    private HashSet<String> wordSet = new HashSet<String>();
    private HashMap<String, ArrayList<String>> lettersToWord = new HashMap<String, ArrayList<String>>();
    private HashMap<Integer, ArrayList<String>> sizeToWords = new HashMap<Integer, ArrayList<String>>();
    private Integer wordLength = DEFAULT_WORD_LENGTH;

    public GameDictionary(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String word = line.trim();
            wordList.add(word);
            wordSet.add(word);
            String key = sortLetters(word);
            if (lettersToWord.get(key) == null) {
                lettersToWord.put(key, new ArrayList<String>());
            }
            lettersToWord.get(key).add(word);
            Integer lengthKey = word.length();
            if (sizeToWords.get(lengthKey) == null)
                sizeToWords.put(lengthKey, new ArrayList<String>());
            sizeToWords.get(lengthKey).add(word);

        }
    }

    public static String sortLetters(String word) {
        char tempArray[] = word.toCharArray();
        Arrays.sort(tempArray);
        return new String(tempArray);
    }

    public boolean isGoodWord(String word, String base) {
        if (wordSet.contains(word))
            return true;
        return false;
    }

    public List<String> getAnagrams(String targetWord) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < wordList.size(); i++) {
            String word = wordList.get(i);
            if (word.length() == targetWord.length())
                if (sortLetters(word).equals(sortLetters(targetWord))) {
                    result.add(word);
                }
        }
        return result;
    }

    public List<String> getAnagramsWithOneMoreLetter(String word) {
        ArrayList<String> result = new ArrayList<>();
        for (char alphabet = 'a'; alphabet <= 'z'; alphabet++) {
            String key = sortLetters(word.concat(String.valueOf(alphabet)));
            if (lettersToWord.containsKey(key)) {
                result.addAll(lettersToWord.get(key));
            }
        }
        return result;
    }

    public String pickGoodStarterWord() {
        while (true) {
            int index = random.nextInt(wordList.size());
            if (wordList.get(index).length() != wordLength)
                continue;
            List<String> anagrams;
            anagrams = getAnagramsWithOneMoreLetter(wordList.get(index));
            if (anagrams.size() >= MIN_NUM_ANAGRAMS) {
                if (wordLength < MAX_WORD_LENGTH)
                    wordLength++;
                else
                    wordLength = DEFAULT_WORD_LENGTH;
                return wordList.get(index);
            } else continue;
        }
    }
}
