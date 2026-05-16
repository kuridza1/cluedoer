package com.ftn.sbnz.model;

/**
 * Činjenica koju strateški sistem ubacuje u working memoriju kada
 * detektuje rizik na osnovu profila protivnika i faze igre (6.2.3).
 */
public class RiskWarning {

    private RiskLevel level;
    private Player cause;   // igrač koji je uzrok rizika (može biti null za LOW)
    private String message;

    public RiskWarning(RiskLevel level, Player cause, String message) {
        this.level   = level;
        this.cause   = cause;
        this.message = message;
    }

    public RiskLevel getLevel()   { return level; }
    public Player    getCause()   { return cause; }
    public String    getMessage() { return message; }

    @Override
    public String toString() {
        return "RiskWarning[" + level + ", cause=" +
                (cause != null ? cause.getName() : "none") + "]";
    }
}