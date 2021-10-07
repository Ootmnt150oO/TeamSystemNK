package yuziouo.teams;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Team {
    ArrayList<String> team;
    public static HashMap<String,ArrayList<String>> list = new HashMap<>();
    HashMap<String,HashMap<String,Boolean>> setting = new HashMap<>();
    public static Team api;
    Config config,setc;
    File file;
    public Team (){
        api = this;
        file = new File(Loader.getInstance().getDataFolder()+"/隊伍");
        if (!file.exists())
            file.mkdirs();
        config = new Config(file+"/team.yml",Config.YAML);
        config.save();
        setc = new Config(file+"/settings.yml",Config.YAML);
        setc.save();
    }
    public void save(){
        for (String s:config.getKeys()){
            config.remove(s);
        }
        for (Map.Entry<String, ArrayList<String>> entry : list.entrySet()) {
            config.set(entry.getKey(),entry.getValue());
        }
        config.save();
        for (Map.Entry<String, HashMap<String, Boolean>> entry : setting.entrySet()) {
          setc.set(entry.getKey(),entry.getValue());
        }
        setc.save();
    }
//    public void load(){
//        HashMap<String,Object> map = (HashMap<String, Object>) config.getAll();
//        list = (HashMap<String, ArrayList<String>>) map.clone();
//        map = (HashMap<String, Object>) setc.getAll();
//        setting = (HashMap<String, HashMap<String, Boolean>>) map.clone();
//    }
public void load(){
 for (String s:config.getKeys()){
     list.put(s, (ArrayList<String>) config.getStringList(s));
     HashMap<String,Boolean> hashMap = new HashMap<>();
    hashMap.put("團隊傷害",false);
     hashMap.put("掉落物平均分配",false);
     setting.put(s,hashMap);
 }
}
    //https://www.trinea.cn/android/hashmap-loop-performance/ 效能最快演算法
    public boolean inTeam(Player player){
        for (Map.Entry<String, ArrayList<String>> entry : list.entrySet()) {
            if (entry.getValue().contains(player.getName()))
                return true;
        }
        return false;
    }
    public void createTeam(String name,Player player){
        if (!inTeam(player)){
            if (list.containsKey(name)) {
                player.sendMessage("此隊伍名已經被使用過");
                return;
            }
            team = new ArrayList<>();
            team.add(0,player.getName());
            list.put(name,team);
            HashMap<String,Boolean> map = new HashMap<>();
            map.put("團隊傷害",false);
            map.put("掉落物平均分配",false);
            setting.put(name,map);
        }
    }
    public void joinTeam(String string,Player player){
        if (!inTeam(player)){
            if (list.containsKey(string)) {
                if (list.get(string).size() <= 5) {
                    list.get(string).add(player.getName());
                }
            }
        }
    }
    public ArrayList<String> getTeam(Player player){
        if (inTeam(player)) {
            for (ArrayList<String> team:list.values()){
                if (team.contains(player.getName()))
                    return team;
            }
        }
        return null;
    }
    public ArrayList<String> getTeam(String name){
        if (list.containsKey(name))
            return list.get(name);
            return null;
    }
    public String getTeamLeader(String string){
        if (list.containsKey(string)){
            return list.get(string).get(0);
        }
        return null;
    }
    public String getTeamLeader(Player player){
        if (getTeam(player)!=null){
            return getTeam(player).get(0);
        }
        return null;
    }
    public boolean isTeamLeader(Player player){
        if (getTeamLeader(player) != null){
            return getTeamLeader(player).equals(player.getName());
        }
        return false;
    }
    public void quiteTeam(Player player){
        if (inTeam(player)){
            getTeam(player).remove(player.getName());
        }
    }
    public int getTeamSize(String name){
        return list.get(name).size();
    }
    public String getTeamName(Player player){
        if (inTeam(player)) {
            for (Map.Entry<String, ArrayList<String>> entry : list.entrySet()) {
                if (entry.getValue().equals(getTeam(player))) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
    public void removeTeam(){
        for (String key : list.keySet()) {
            if (list.get(key).isEmpty()){
                list.remove(key);
                setting.remove(key);
            }
        }
    }

    public static Team getApi() {
        return api;
    }
}
