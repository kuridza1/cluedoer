package com.ftn.sbnz.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuggestionRecommendation {

    private Card suspect;
    private Card weapon;
    private Card room;
    private String reason;

    public SuggestionRecommendation() {}

    public SuggestionRecommendation(Card suspect, Card weapon, Card room, String reason) {
        this.suspect = suspect;
        this.weapon = weapon;
        this.room = room;
        this.reason = reason;
    }
}