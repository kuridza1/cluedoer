package com.ftn.sbnz.model;

import java.util.Objects;

/**
 * Trenutni status partije.
 * Strateški sistem koristi ovo za izbor strategije.
 */
public class GameState {

    private GameStatus status;

    public GameState() {
    }

    public GameState(GameStatus status) {
        this.status = status;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameState)) return false;
        GameState that = (GameState) o;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }

    @Override
    public String toString() {
        return "GameState(" + status + ")";
    }
}