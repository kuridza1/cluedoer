package com.ftn.sbnz.model;

import java.util.Objects;

/**
 * Profil ponašanja protivnika.
 */
public class Profile {

    private Player player;
    private ProfileType profile;
    private long timestamp;

    public Profile() {
    }

    public Profile(Player player, ProfileType profile) {
        this.player = player;
        this.profile = profile;
        this.timestamp = System.currentTimeMillis();
    }

    public Profile(Player player, ProfileType profile, long timestamp) {
        this.player = player;
        this.profile = profile;
        this.timestamp = timestamp;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public ProfileType getProfile() {
        return profile;
    }

    public void setProfile(ProfileType profile) {
        this.profile = profile;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile)) return false;
        Profile that = (Profile) o;
        return Objects.equals(player, that.player)
                && profile == that.profile;
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, profile);
    }

    @Override
    public String toString() {
        return "PlayerProfile(" + player.getName()
                + " -> " + profile + ")";
    }
}