package yuziouo.teams;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.ItemID;
import cn.nukkit.plugin.PluginBase;
import yuziouo.teams.cmds.TeamCmd;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Loader extends PluginBase implements Listener {
    public static HashMap<String, String> req = new HashMap<>();
    static final int f = 98378734;
    static final int all = 98378735;
    static final int cr = 98378736;
    static final int info = cr+1;
    static final int pinfo = info+1;
    static final int quite = pinfo+1;
    static final int invite = quite+1;
    static final int kick = invite+1;
    static final int set = kick+1;
    public static Loader instance;
    Team team;
    @Override
    public void onEnable() {
        instance = this;
        team = new Team();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this,this);
        getServer().getCommandMap().register(getConfig().getString("指令"),new TeamCmd());
        getServer().getLogger().alert("插件開啟");
        team.load();
        getServer().getScheduler().scheduleRepeatingTask(this,new CleanEmptyTeam(),20);
    }

    @Override
    public void onDisable() {
        team.save();
    }
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player){
            Player player = (Player) event.getDamager(),entity = (Player) event.getEntity();
            if (team.inTeam(player)&&team.inTeam(entity)){
                if (team.getTeamName(player).equals(team.getTeamName(entity))){
                    if (team.setting.get(team.getTeamName(player)).get("團隊傷害")) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    public  void sendUI(Player player){
        FormWindowSimple form = new FormWindowSimple(getConfig().getString("未加入隊伍系統表單標題"),"");
        form.addButton(new ElementButton(getConfig().getString("未加入隊伍系統表單查看可加入隊伍按鈕")));
        form.addButton(new ElementButton(getConfig().getString("未加入隊伍系統表單創建新隊伍按鈕")));
        player.showFormWindow(form,f);
    }
    public void createUi(Player player){
        FormWindowCustom form = new FormWindowCustom(getConfig().getString("創建隊伍表單標題"));
        form.addElement(new ElementInput(getConfig().getString("創建隊伍表單輸入提示")));
        player.showFormWindow(form,cr);
    }
    public void AllTeamUi(Player player){
        FormWindowSimple form = new FormWindowSimple(getConfig().getString("伺服器所有可加入隊伍表單標題"),getConfig().getString("伺服器所有可加入隊伍表單提示"));
        for (String key : Team.list.keySet()){
            if (team.getTeamSize(key)>=5)
                continue;
            form.addButton(new ElementButton(key));
        }
        player.showFormWindow(form,all);
    }
    public  void quiteUI(Player player){
        FormWindowSimple form = new FormWindowSimple(getConfig().getString("退出隊伍表單標題"),"");
        form.addButton(new ElementButton(getConfig().getString("退出隊伍表單退出按鈕")));
        form.addButton(new ElementButton("返回"));
        player.showFormWindow(form,quite);
    }
    public void TeamInfo(Player player){
        FormWindowSimple formWindowSimple = new FormWindowSimple(getConfig().getString("隊伍系統表單標題"),"");
        formWindowSimple.addButton(new ElementButton(getConfig().getString("隊伍系統查看隊伍人數按鈕")));
        formWindowSimple.addButton(new ElementButton(getConfig().getString("隊伍系統退出隊伍按鈕")));
        if (team.isTeamLeader(player)){
            formWindowSimple.addButton(new ElementButton(getConfig().getString("隊長查看隊伍邀請按鈕")));
            formWindowSimple.addButton(new ElementButton(getConfig().getString("隊長踢出成員按鈕")));
            formWindowSimple.addButton(new ElementButton(getConfig().getString("隊長設定隊伍功能按鈕")));
        }
        player.showFormWindow(formWindowSimple,info);
    }
    public void playerteaminfo(Player player){
        String old = getConfig().getString("成員名單"), young = old.replace("@a",team.getTeamName(player));
        FormWindowSimple formWindowSimple = new FormWindowSimple(young,"");
        for (int i=0; i<team.getTeam(player).size();i++){
            formWindowSimple.addButton(new ElementButton(team.getTeam(player).get(i)));
        }
        player.showFormWindow(formWindowSimple,pinfo);
    }
    public void inviteUi(Player player){
        FormWindowSimple formWindowSimple = new FormWindowSimple(getConfig().getString("隊長邀請函標題"),"點擊按鈕同意");
        for (Map.Entry<String, String> entry : req.entrySet()) {
            if (!entry.getValue().equals(team.getTeamName(player)))
                continue;
            formWindowSimple.addButton(new ElementButton(entry.getKey()));
        }
        player.showFormWindow(formWindowSimple,invite);
    }
    public void kickUi(Player player){
        FormWindowSimple formWindowSimple = new FormWindowSimple(getConfig().getString("隊長踢出玩家表單標題"),"點擊按鈕踢出玩家");
        for (int i=0; i<team.getTeam(player).size();i++){
            //排除隊長
            if (player.getName().equals(team.getTeam(player).get(i)))
                continue;
            formWindowSimple.addButton(new ElementButton(team.getTeam(player).get(i)));
        }
        player.showFormWindow(formWindowSimple,kick);
    }
    public void setUi(Player player){
        FormWindowCustom form = new FormWindowCustom(getConfig().getString("隊伍設置表單標題"));
        form.addElement(new ElementToggle("是否開啟隊伍傷害"));
        form.addElement(new ElementToggle("是否開啟掉落物平均分配"));
        player.showFormWindow(form,set);
    }
    @EventHandler
    public void onPickItem(InventoryPickupItemEvent event){
        if (event.getItem().getItem().getId()== 205||event.getItem().getItem().getId() == 218||event.getItem().getItem().getId() == ItemID.BOAT)
            return;
        if (event.getInventory() instanceof PlayerInventory) {
            Player player = null;
            for (Player players : event.getViewers()) {
                player = players;
            }
            if (player != null) {
                if (team.inTeam(player)) {
                    if (team.setting.get(team.getTeamName(player)).get("掉落物平均分配")) {
                        Random random = new Random();
                        int i = random.nextInt(team.getTeamSize(team.getTeamName(player)));
                        Player player1 = Server.getInstance().getPlayer(team.getTeam(player).get(i));
                        if (player1 != null) {
                            if (player1.getInventory().canAddItem(event.getItem().getItem())) {
                                player1.getInventory().addItem(event.getItem().getItem());
                            } else {
                                player1.dropItem(event.getItem().getItem());
                            }
                        } else {
                            player.getInventory().addItem(event.getItem().getItem());
                        }

                        Player finalPlayer = player;
                        getServer().getScheduler().scheduleDelayedTask(this, new Runnable() {
                            @Override
                            public void run() {
                                finalPlayer.getInventory().removeItem(event.getItem().getItem());
                                finalPlayer.sendMessage("掉落物隨機分配所以此掉落物會被回收優");
                            }
                        }, 1);
                    }
                }
            }
        }
    }
//    @EventHandler
//    public void addExp(PlayerAddExpEvent event){
//        Player player = event.getPlayer();
//        double exp =event.getExp();
//        int i = 0;
//        if (Team.api.inTeam(player)){
//            if (DamageMath.getAddPlayerAttribute(player, baseAPI.ItemADDType.EXP) > 0)
//                exp += Float.parseFloat(String.valueOf(exp * DamageMath.getAddPlayerAttribute(player, baseAPI.ItemADDType.EXP) / 100.0F));
//        }
//    }
    @EventHandler
    public void onForm(PlayerFormRespondedEvent event){
        Player player = event.getPlayer();
        int id = event.getFormID(); //这将返回一个form的唯一标识`id`
        if (event.wasClosed())
            return;
        if(id == f) { //判断出这个UI界面是否是我们上面写的`menu`
            FormResponseSimple response = (FormResponseSimple) event.getResponse(); //这里需要强制类型转换一下
            int clickedButtonId = response.getClickedButtonId();
            if (event.wasClosed())
                return;
            if (clickedButtonId == 0) {
                AllTeamUi(player);
            }else if (clickedButtonId ==1){
                createUi(player);
            }
        }
        if (id == all){
            FormResponseSimple response = (FormResponseSimple) event.getResponse(); //这里需要强制类型转换一下
            if (event.wasClosed())
                return;
            req.put(player.getName(),response.getClickedButton().getText());
            String old = getConfig().getString("玩家發送申請給隊伍") ,young = old.replace("@a",response.getClickedButton().getText());
            player.sendMessage(young);
            old = getConfig().getString("隊長收到訊邀請訊息通知");
            young = old.replace("@p",player.getName());
            Server.getInstance().getPlayer(team.getTeamLeader(response.getClickedButton().getText())).sendMessage(young);
        }
        if (id == cr){
            FormResponseCustom response = (FormResponseCustom) event.getResponse();
            if (event.wasClosed())
                return;
            if (response.getInputResponse(0).equals("")){
                player.sendMessage("隊伍名稱不能為空白");
                return;
            }
            team.createTeam(response.getInputResponse(0),player);
            if (req.containsKey(player.getName()))
                req.remove(player.getName());
                player.sendMessage("隊伍創建成功");
        }
        if (id == info){
            FormResponseSimple response = (FormResponseSimple) event.getResponse();
            if (event.wasClosed())
                return;
            int clickedButtonId = response.getClickedButtonId();
            switch (clickedButtonId){
                case 0:
                    playerteaminfo(player);
                    break;
                case 1:
                    quiteUI(player);
                    break;
                case 2:
                    inviteUi(player);
                    break;
                case 3:
                    kickUi(player);
                    break;
                case 4:
                    setUi(player);
                    break;
            }
        }
        if (id == pinfo){
            return;
        }
        if (id == invite){
            if (event.wasClosed())
                return;
            FormResponseSimple response = (FormResponseSimple) event.getResponse();
            team.joinTeam(team.getTeamName(player), Server.getInstance().getPlayer(response.getClickedButton().getText()));
            String old =getConfig().getString("加入隊伍玩家發送訊息"),young = old.replace("@a",team.getTeamName(player));
            Server.getInstance().getPlayer(response.getClickedButton().getText()).sendMessage(young);
            for (int i = 0; i<team.getTeam(player).size();i++) {
                Player player1 = Server.getInstance().getPlayer(team.getTeam(player).get(i));
                if (player1 != null) {
                    if (!player1.getName().equals(player.getName())) {
                        old = getConfig().getString("其餘玩家發送加入訊息");
                        young = old.replace("@p", Server.getInstance().getPlayer(response.getClickedButton().getText()).getName());
                        player1.sendMessage(young);
                    }
                }
            }
            req.remove(Server.getInstance().getPlayer(response.getClickedButton().getText()).getName());
        }
        if (id == quite){
            if (event.wasClosed())
                return;
            for (int i = 0; i<team.getTeam(player).size();i++) {
                Player player1 = Server.getInstance().getPlayer(team.getTeam(player).get(i));
                if (player1!=null) {
                    if (!player1.getName().equals(player.getName())) {
                        player1.sendMessage(getConfig().getString("退出隊伍訊息").replace("@p", player.getName()).replace("@a", team.getTeamName(player1)));
                    }
                }
            }
            team.quiteTeam(player);
            player.sendMessage(getConfig().getString("退出隊伍玩家發送訊息"));
        }
        if (id == kick){
            if (event.wasClosed())
                return;
            FormResponseSimple response = (FormResponseSimple) event.getResponse();
            team.quiteTeam(Server.getInstance().getPlayer(response.getClickedButton().getText()));
        }
        if (id == set){
            if (event.wasClosed())
                return;
            FormResponseCustom response = (FormResponseCustom) event.getResponse();
            HashMap<String,Boolean> map = new HashMap<>();
            map.put("團隊傷害",response.getToggleResponse(0));
            map.put("掉落物平均分配",response.getToggleResponse(1));
            team.setting.put(team.getTeamName(player),map);
            player.sendMessage("設定完成");
        }
    }

    public static Loader getInstance() {
        return instance;
    }
}
