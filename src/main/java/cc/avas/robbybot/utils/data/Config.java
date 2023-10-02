package cc.avas.robbybot.utils.data;

// Stores

public class Config {
    private String guildID = "";
    private String modRoles = "";
    private String modChannel = "";
    private String pollRole = "";
    private String pollChannel = "";
    private String eventsChannel = "";
    private String leaderboardsChannel = "";
    private String submissionsChannel = "";
    private String mapartChannel = "";
    private boolean debug = false;

    public String getGuildID() { return guildID; }

    public void setGuildID(String guildID) { this.guildID = guildID; }

    public String getModRoles() {
        return modRoles;
    }

    public void setModRoles(String modRoles) {
        this.modRoles = modRoles;
    }

    public String getModChannel() {
        return modChannel;
    }

    public void setModChannel(String modChannel) {
        this.modChannel = modChannel;
    }

    public String getPollRole() {
        return pollRole;
    }

    public void setPollRole(String pollRole) {
        this.pollRole = pollRole;
    }

    public String getPollChannel() {
        return pollChannel;
    }

    public void setPollChannel(String pollChannel) {
        this.pollChannel = pollChannel;
    }

    public String getEventsChannel() {
        return eventsChannel;
    }

    public void setEventsChannel(String eventsChannel) {
        this.eventsChannel = eventsChannel;
    }

    public String getLeaderboardsChannel() {
        return leaderboardsChannel;
    }

    public void setLeaderboardsChannel(String leaderboardsChannel) {
        this.leaderboardsChannel = leaderboardsChannel;
    }

    public String getSubmissionsChannel() {
        return submissionsChannel;
    }

    public void setSubmissionsChannel(String submissionsChannel) {
        this.submissionsChannel = submissionsChannel;
    }

    public String getMapartChannel() {
        return mapartChannel;
    }

    public void setMapartChannel(String mapartChannel) {
        this.mapartChannel = mapartChannel;
    }
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}

