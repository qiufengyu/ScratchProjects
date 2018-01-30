package com.analysis;

public class WordWeight implements Comparable<WordWeight> {

    public WordWeight() {
        weight= 0.0;
    }

    public WordWeight(String word, double weight) {
        this.word = word;
        this.weight = weight;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    String word;
    double weight;

    @Override
    public int compareTo(WordWeight o) {
        return this.weight > o.getWeight() ? -1 : 1;
    }
}
