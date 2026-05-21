package com.ftn.sbnz.model;

public class SolutionProbability {
    private Card card;
    private double probability;

    public SolutionProbability(Card c, double p) {
        this.card = c;
        this.probability = p;
    }

    public double getProbability() {
        return probability;
    }
}
