package rankManager.rank;

import java.util.Iterator;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.ConfigSection;
import rankManager.RankManager;
import rankManager.task.AutoUnloadTask;

public class RankLoader {
	private RankLoader instance = null;
	private ConfigSection users = new ConfigSection();
	private RankManager plugin;
	private Server server;
	ConfigSection temp;
	
	public RankLoader(RankManager plugin) {
		if(this.instance == null) this.instance = this;
		this.server = Server.getInstance();
		this.plugin = plugin;
		
		this.plugin.getServer().getScheduler().scheduleRepeatingTask(new AutoUnloadTask(this), 12000);
	}
	public RankData loadRank(String userName) {
		userName = userName.toLowerCase();
		
		if(this.users.containsKey(userName)) return (RankData) users.get(userName);
		users.put(userName, new RankData(userName,  this.plugin.getDataFolder().getPath() + "/player/"));
		return (RankData) users.get(userName);
	}
	public boolean unloadRank(String userName) {
		if(userName == null) {
				Iterator<String> it = users.keySet().iterator();
				String key;
					while(it.hasNext()) {
						key = it.next();
						((RankData) users.get(key)).save(true);
						it.remove();
					}
			return true;
		}
		userName = userName.toLowerCase();
		if(! users.containsKey(userName)) return false;
			((RankData)users.get(userName)).save(true);
		users.remove(userName);
		return true;
	}
	public RankData getRank(Player player) {
		String userName = player.getName().toLowerCase();
		if(! users.containsKey(userName)) loadRank(userName);
		return (RankData) users.get(userName);
	}
	public RankData getRankByName(String name) {
		String userName = name.toLowerCase();
		if(! users.containsKey(userName)) loadRank(userName);
		return (RankData) users.get(userName);
	}
	public void save(Boolean async) {
		users.forEach((Key, value)->{
				((RankData)value).save(async);
		});
	}
	public RankLoader getInstance() {
		return instance;
	}
}
