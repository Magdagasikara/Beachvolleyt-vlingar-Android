package com.ngusta.cupassist.domain;

import com.ngusta.cupassist.activity.MyApplication;

import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.min;

public class Tournament implements Serializable, Comparable<Tournament> {

    public static final String TAG = Tournament.class.getSimpleName();

    private static int defaultId = 0;

    private int id;

    private Date startDate;

    private Date endDate;

    private CompetitionPeriod competitionPeriod;

    private String club;

    private String name;

    private String url;

    private Level level;

    private String levelString;

    private List<TournamentClazz> clazzes;

    private String urlName;

    private Map<Clazz, List<Team>> teams;

    private Region region;

    public Tournament(Date startDate, Date endDate, String period, String club, String name,
            String url,
            String level, String clazzes) {
        this.id = defaultId++;
        this.startDate = startDate;
        this.endDate = endDate;
        this.competitionPeriod = CompetitionPeriod.findByName(period);
        this.club = shortenName(club);
        this.name = name;
        this.url = url;
        this.levelString = level;
        this.level = Level.parse(level);
        if (this.level == Level.UNKNOWN) {
            this.level = guessLevel(name);
        }
        this.clazzes = parseClazzes(clazzes);
        this.region = Region.findRegionByClub(this.club);
    }

    private String shortenName(String club) {
        if ("KFUM Gymnastik & IA Karskrona".equals(club)) {
            return "KFUM Karlskrona";
        }
        if ("Föreningen Beachvolley-Aid".equals(club)) {
            return "Beachvolley-Aid";
        }
        return club;
    }

    private Level guessLevel(String name) {
        Level guessedLevel = Level.UNKNOWN;
        name = name.toLowerCase();
        if (name.contains("open") || name.contains("mix") || name.contains("svart") || name
                .contains("midnight")) {
            guessedLevel = Level.OPEN;
        }
        if (name.contains("grön")) {
            guessedLevel = Level.OPEN_GREEN;
        }
        if (name.contains("chall")) {
            guessedLevel = Level.CHALLENGER;
        }
        if (name.contains("ungdom") || name.contains("junior")) {
            guessedLevel = Level.YOUTH;
        }
        if (name.contains("mixed-sm")) {
            guessedLevel = Level.SWEDISH_BEACH_TOUR;
        }
        if (name.contains("senior-sm")) {
            guessedLevel = Level.SWEDISH_BEACH_TOUR;
        }
        return guessedLevel;
    }

    private List<TournamentClazz> parseClazzes(String clazzes) {
        Set<TournamentClazz> parsedClazzes = new HashSet<>();
        if (clazzes != null) {
            String[] clazzArray = clazzes.split(", ");
            for (String clazzString : clazzArray) {
                parsedClazzes.add(new TournamentClazz(Clazz.parse(clazzString)));
            }
        }
        return new ArrayList<>(parsedClazzes);
    }

    public CompetitionPeriod getCompetitionPeriod() {
        return competitionPeriod;
    }

    public String getClub() {
        return club;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Level getLevel() {
        return level;
    }

    public String getLevelString() {
        return levelString;
    }

    public List<TournamentClazz> getClazzes() {
        return clazzes != null ? clazzes : Collections.<TournamentClazz>emptyList();
    }

    public String getUrlName() {
        return urlName;
    }

    public void setUrlName(String urlName) {
        if (MyApplication.RUN_AS_ANDROID_APP) {
            Log.i(TAG, "Trying to set reg url to: " + urlName + " Old value: " + this.urlName);
        }
        this.urlName = urlName;
    }

    public Map<Clazz, List<Team>> getTeams() {
        return teams;
    }

    public List<TeamGroupPosition> getTeamGroupPositionsForClazz(TournamentClazz clazz) {
        int group = 0;
        int operator = 1;
        int numberOfGroups = getNumberOfGroupsForClazz(clazz);
        List<TeamGroupPosition> teamGroupPositions = new ArrayList<>();
        for (Team team : getSeededTeamsForClazz(clazz)) {
            group += operator;
            if (group == (numberOfGroups + 1)) {
                group = numberOfGroups;
                operator = -1;
            } else if (group == 0) {
                group = 1;
                operator = 1;
            }
            teamGroupPositions.add(new TeamGroupPosition(group, team));
        }
        return teamGroupPositions;
    }

    private int getNumberOfGroupsForClazz(TournamentClazz clazz) {
        if (clazz.getMaxNumberOfTeams() == 12) {
            return 4;
        } else {
            return (int) Math.round((clazz.getMaxNumberOfTeams() + 1.0) / 4);
        }
    }

    public List<Team> getSeededTeamsForClazz(TournamentClazz clazz) {
        if (getTeams() == null || getTeams().get(clazz.getClazz()) == null) {
            return Collections.emptyList();
        }
        List<Team> seeded = getTeams().get(clazz.getClazz());
        Collections.sort(seeded, level.getComparator());
        Collections.sort(seeded.subList(0, min(seeded.size(), clazz.getMaxNumberOfTeams())), Level.ENTRY_POINTS_COMPARATOR);
        return seeded;
    }

    public int getNumberOfCompleteTeamsForClazz(TournamentClazz clazz) {
        if (getTeams() == null || getTeams().get(clazz.getClazz()) == null) {
            return 0;
        }
        List<Team> teams = getTeams().get(clazz.getClazz());
        int numberOfCompleteTeams = 0;
        for (Team team : teams) {
            if (!team.getPlayerB().getName().trim().equals("Partner sökes")) {
                numberOfCompleteTeams++;
            }
        }
        return numberOfCompleteTeams;
    }

    public void setMaxNumberOfTeams(Map<Clazz, Integer> maxNumberOfTeams) {
        if (MyApplication.RUN_AS_ANDROID_APP) {
            Log.i(TAG, "Trying to set max number of teams: " + maxNumberOfTeams + " Old clazzes: " + this.clazzes);
        }
        clazzes.clear();
        for (Clazz clazz : maxNumberOfTeams.keySet()) {
            clazzes.add(new TournamentClazz(clazz, maxNumberOfTeams.get(clazz)));
        }
    }

    public void setTeams(List<Team> teamList) {
        teams = new HashMap<>();
        for (Team team : teamList) {
            if (!teams.containsKey(team.getClazz())) {
                teams.put(team.getClazz(), new ArrayList<Team>());
            }
            teams.get(team.getClazz()).add(team);
        }
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getFormattedStartDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(startDate);
    }

    public boolean spansOverSeveralDays() {
        return startDate.before(endDate);
    }

    public boolean isRegistrationOpen() {
        if (MyApplication.RUN_AS_ANDROID_APP) {
            Log.i(TAG, "isRegistrationOpen: " + (urlName != null) + " Reg url: " + urlName);
        }
        return urlName != null;
    }

    public int getId() {
        return id;
    }

    public Region getRegion() {
        return region;
    }

    @Override
    public String toString() {
        return "Tournament{" +
                "startDate=" + getFormattedStartDate() +
                ", period='" + competitionPeriod.getName() + '\'' +
                ", club='" + club + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", level='" + level + '\'' +
                ", classes='" + clazzes + '\'' +
                ", redirectUrl='" + urlName + '\'' +
                '}';
    }

    @Override
    public int compareTo(Tournament tournament) {
        return this.getStartDate().compareTo(tournament.getStartDate());
    }

    public enum Level implements Serializable {
        OPEN, OPEN_GREEN, CHALLENGER, SWEDISH_BEACH_TOUR, YOUTH, VETERAN, UNKNOWN;

        private static final Comparator<Team> REGISTRATION_TIME_COMPARATOR
                = new Comparator<Team>() {
            @Override
            public int compare(Team lhs, Team rhs) {
                if (lhs.isCompleteTeam() && !rhs.isCompleteTeam()) {
                    return -1;
                } else if (!lhs.isCompleteTeam() && rhs.isCompleteTeam()) {
                    return 1;
                }
                if (lhs.getRegistrationTime() == null) {
                    return rhs.getRegistrationTime() == null ? 0 : -1;
                }
                if (rhs.getRegistrationTime() == null) {
                    return 1;
                }
                return lhs.getRegistrationTime().compareTo(rhs.getRegistrationTime());
            }
        };

        private static final Comparator<Team> ENTRY_POINTS_COMPARATOR = new Comparator<Team>() {
            @Override
            public int compare(Team lhs, Team rhs) {
                return lhs.compareTo(rhs);
            }
        };

        public static Level parse(String levelString) {
            if (levelString == null) {
                return UNKNOWN;
            }
            if (levelString.contains("Open (Svart)")) {
                return OPEN;
            } else if (levelString.contains("Open (Grön)")) {
                return OPEN_GREEN;
            } else if (levelString.contains("Challenger")) {
                return CHALLENGER;
            } else if (levelString.contains("Swedish Beach Tour")) {
                return SWEDISH_BEACH_TOUR;
            } else if (levelString.contains("Veteran")) {
                return VETERAN;
            } else if (levelString.contains("Ungdom") || levelString.contains("3-beach")
                    || levelString.contains("Kidsvolley")) {
                return YOUTH;
            }
            return UNKNOWN;
        }

        public Comparator<Team> getComparator() {
            switch (this) {
                case OPEN:
                case OPEN_GREEN:
                    return REGISTRATION_TIME_COMPARATOR;
                default:
                    return ENTRY_POINTS_COMPARATOR;
            }
        }
    }

    public static class TeamGroupPosition implements Serializable {

        public final int group;

        public final Team team;

        public TeamGroupPosition(int group, Team team) {
            this.group = group;
            this.team = team;
        }
    }

    public class TournamentClazz implements Serializable {

        private Clazz clazz;

        private Integer maxNumberOfTeams;

        TournamentClazz(Clazz clazz) {
            this.clazz = clazz;
        }

        public TournamentClazz(Clazz clazz, Integer maxNumberOfTeams) {
            this.clazz = clazz;
            this.maxNumberOfTeams = maxNumberOfTeams;
        }

        public Clazz getClazz() {
            return clazz;
        }

        public Integer getMaxNumberOfTeams() {
            if (maxNumberOfTeams != null) {
                return maxNumberOfTeams;
            } else {
                int guess = level == Level.OPEN ? 16 : 12;
                Log.i(TAG, String.format("Guessing number of teams in '%s' for clazz %s: %d", name,
                        clazz, guess));
                return guess;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TournamentClazz that = (TournamentClazz) o;

            return clazz == that.clazz;

        }

        @Override
        public int hashCode() {
            return clazz.hashCode();
        }

        @Override
        public String toString() {
            String clazzString = clazz.toString();
            return clazzString;
        }
    }
}