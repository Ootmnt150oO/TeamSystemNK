package yuziouo.teams.cmds;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import yuziouo.teams.Loader;
import yuziouo.teams.Team;

public class TeamCmd extends Command {

    public TeamCmd() {
        super(Loader.getInstance().getConfig().getString("指令"),"隊伍資訊");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender instanceof Player){
            Player player = (Player) commandSender;
            Team team =  Team.getApi();
            if (!team.inTeam(player))
                Loader.getInstance().sendUI(player);
            else Loader.getInstance().TeamInfo(player);
        }
        return true;
    }
}
