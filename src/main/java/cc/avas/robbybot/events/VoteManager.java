package cc.avas.robbybot.events;

import java.util.ArrayList;
import java.util.List;

public class VoteManager {

    static List<Vote> votesCast = new ArrayList<>();

    public static void resetVotes() { votesCast = new ArrayList<>(); }

    public static class Vote {
        long discordID;
        long messageID;

        public Vote(long discordID, long messageID) {
            this.discordID = discordID;
            this.messageID = messageID;
        }

        public long getDiscordID() {
            return discordID;
        }

        public long getMessageID() {
            return messageID;
        }

        public void setMessageID(long messageID) { this.messageID = messageID; }
    }

    public static void addVote(long discordID, long messageID) {
        votesCast.add(new Vote(discordID, messageID));
    }

    public static void removeVote(long discordID) {
        Vote vote = getVote(discordID);
        if (vote == null) return;
        votesCast.remove(vote);
    }

    public static void removeVotes(long messageID) {
        List<Vote> votesToRemove = new ArrayList<>();
        for (Vote vote : votesCast) {
            if (vote.getMessageID() == messageID) votesToRemove.add(vote);
        }
        for (Vote vote : votesToRemove) votesCast.remove(vote);
    }

    public static Vote getVote(long discordID) {
        for (Vote vote : votesCast) {
            if (vote.getDiscordID() == discordID) return vote;
        }
        return null;
    }

    public static List<Vote> getVotes() { return votesCast; }
}