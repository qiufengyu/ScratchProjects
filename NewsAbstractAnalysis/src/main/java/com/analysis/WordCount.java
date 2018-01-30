package com.analysis;

public class WordCount implements Comparable<WordCount> {


    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public WordCount() {
        count = 0;

    }

    public WordCount(String word, int count) {
        this.word = word;
        this.count = count;
    }

    String word;
    int count;

    public int compareTo(WordCount o) {
        return this.count > o.count ? -1:1;
    }
}
