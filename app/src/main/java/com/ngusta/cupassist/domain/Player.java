package com.ngusta.cupassist.domain;

import java.io.Serializable;
import java.util.Comparator;

public class Player implements Serializable {
    private Integer rank;

    private int entryRank;

    private int mixedEntryRank;
    private String firstName;
    private String lastName;
    private String club;
    private int rankingPoints;
    private int entryPoints;
    private int mixRankingPoints;
    private int mixEntryPoints;

    private Clazz clazz;

    public Player(String firstName, String lastName, String playerClub) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.club = playerClub;
    }

    public Player(Integer rank, String firstName, String lastName, String club, int rankingPoints, int entryPoints) {
        this.rank = rank;
        this.firstName = firstName;
        this.lastName = lastName;
        this.club = club;
        this.rankingPoints = rankingPoints;
        this.entryPoints = entryPoints;
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public String getNameAndClub() {
        return firstName + " " + lastName + " " + club;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getMixRankingPoints() {
        return mixRankingPoints;
    }

    public void setMixRankingPoints(int mixRankingPoints) {
        this.mixRankingPoints = mixRankingPoints;
    }

    public int getMixEntryPoints() {
        return mixEntryPoints;
    }

    public void setMixEntryPoints(int mixEntryPoints) {
        this.mixEntryPoints = mixEntryPoints;
    }

    public int getEntryPoints() {
        return entryPoints;
    }

    public void setEntryPoints(int entryPoints) {
        this.entryPoints = entryPoints;
    }

    public int getRankingPoints() {
        return rankingPoints;
    }

    public void setRankingPoints(int rankingPoints) {
        this.rankingPoints = rankingPoints;
    }

    public int getEntryPoints(Clazz clazz) {
        if (clazz == Clazz.MIXED) {
            return (int) Math.round(entryPoints * 0.1) + mixEntryPoints;
        }
        return entryPoints;
    }

    public int getRankingPoints(Clazz clazz) {
        if (clazz == Clazz.MIXED) {
            return (int) Math.round(rankingPoints * 0.1) + mixRankingPoints;
        }
        return rankingPoints;
    }

    public String getClub() {
        return club;
    }

    public void setClazz(Clazz clazz) {
        this.clazz = clazz;
    }

    public Clazz getClazz() {
        return clazz;
    }

    public void setEntryRank(int entryRank) {
        this.entryRank = entryRank;
    }

    public int getEntryRank() {
        return entryRank;
    }

    public void setMixedEntryRank(int mixedEntryRank) {
        this.mixedEntryRank = mixedEntryRank;
    }

    public int getMixedEntryRank() {
        return mixedEntryRank;
    }

    public String uniqueIdentifier() {
        return (firstName + " " + lastName + " " + club).replaceAll("/|\\.|#|\\$|\\[|\\]", "");
    }

    @Override
    public String toString() {
        return rank + " " +
                firstName + " " +
                lastName + " " +
                club + " " +
                rankingPoints + " " +
                entryPoints;
    }

    public static class PlayerComparator implements Comparator<Player> {

        private final Clazz clazz;

        public PlayerComparator(Clazz clazz) {
            this.clazz = clazz;
        }

        @Override
        public int compare(Player p1, Player p2) {
            int cmp = Double.compare(p2.getEntryPoints(clazz), p1.getEntryPoints(clazz));
            if (cmp != 0) {
                return cmp;
            }
            cmp = Double.compare(p2.getRankingPoints(clazz), p1.getRankingPoints(clazz));
            return cmp != 0 ? cmp : p1.getName().compareTo(p2.getName());
        }
    }
}
