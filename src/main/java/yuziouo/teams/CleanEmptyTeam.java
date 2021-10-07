package yuziouo.teams;

import cn.nukkit.scheduler.Task;

public class CleanEmptyTeam extends Task {
    Team team = Team.getApi();
    @Override
    public void onRun(int i) {
        team.removeTeam();
    }
}
