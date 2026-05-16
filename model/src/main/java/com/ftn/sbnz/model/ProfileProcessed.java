package com.ftn.sbnz.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfileProcessed {
    Player player;
    public ProfileProcessed(Player player) {
        this.player = player;
    }
}