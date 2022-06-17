package io.icw.round;

public class RoundValidResult {
    private MeetingRound round;
    private boolean validResult = false;

    public MeetingRound getRound() {
        return round;
    }

    public void setRound(MeetingRound round) {
        this.round = round;
    }

    public boolean isValidResult() {
        return validResult;
    }

    public void setValidResult(boolean validResult) {
        this.validResult = validResult;
    }
}
