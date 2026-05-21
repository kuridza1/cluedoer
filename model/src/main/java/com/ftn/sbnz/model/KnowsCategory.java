package com.ftn.sbnz.model;

import java.util.Date;

public class KnowsCategory {
    private Player player;
    private CardType category;
    private Date timestamp;

    public KnowsCategory(Player player, CardType category) {
        this.player = player;
        this.category = category;
        this.timestamp = new Date();
    }

}
