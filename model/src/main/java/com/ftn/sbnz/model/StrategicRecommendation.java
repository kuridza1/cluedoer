package com.ftn.sbnz.model;

import java.util.Objects;

/**
 * Tekstualna preporuka strateškog sistema.
 */
public class StrategicRecommendation {

    private String message;

    public StrategicRecommendation() {
    }

    public StrategicRecommendation(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StrategicRecommendation)) return false;
        StrategicRecommendation that = (StrategicRecommendation) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return "Recommendation: " + message;
    }
}