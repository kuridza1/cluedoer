package com.ftn.sbnz.service.config;

public class PrivateShowRow {
    private String candidateName;
    private String notOwnsCard1;
    private String notOwnsCard2;
    private String candidateCard;

    public PrivateShowRow(String candidateName, String notOwnsCard1,
                          String notOwnsCard2, String candidateCard) {
        this.candidateName = candidateName;
        this.notOwnsCard1 = notOwnsCard1;
        this.notOwnsCard2 = notOwnsCard2;
        this.candidateCard = candidateCard;
    }

    public String getCandidateName() { return candidateName; }
    public String getNotOwnsCard1()  { return notOwnsCard1; }
    public String getNotOwnsCard2()  { return notOwnsCard2; }
    public String getCandidateCard() { return candidateCard; }
}