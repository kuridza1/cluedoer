package com.ftn.sbnz.model;

public class StrategyContext {

    private GameStatus status;
    private Card bestSuspect;
    private Card bestWeapon;
    private Card bestRoom;

    public StrategyContext(GameStatus status, Card bestSuspect, Card bestWeapon, Card bestRoom) {
        this.status = status;
        this.bestSuspect = bestSuspect;
        this.bestWeapon = bestWeapon;
        this.bestRoom = bestRoom;
    }

    public GameStatus getStatus() { return status; }
    public Card getBestSuspect() { return bestSuspect; }
    public Card getBestWeapon() { return bestWeapon; }
    public Card getBestRoom() { return bestRoom; }
}