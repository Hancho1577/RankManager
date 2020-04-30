package rankManager.rank;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import rankManager.RankManager;

public class RankProvider {
	private RankProvider instance = null;
	private RankManager plugin;
	private RankLoader loader;
	private Server server;
	private Map<String, Object> db;
	public RankProvider (RankManager plugin) {
		if(this.instance == null) instance = this;
		this.plugin = plugin;
		loader = plugin.getRankLoader();
		server = Server.getInstance();
		
		db = (LinkedHashMap<String, Object>) (new Config(this.plugin.getDataFolder().getPath() + "/pluginDB.yml", Config.YAML, new ConfigSection(){
			{
			set("defaultPrefix", plugin.getMessage("default-player-prefix"));
			set("defaultPrefixFormat" , "§f%prefix%");
			set("chatFormat", "%special%§6[ %prefix%§f/ %name%§6]§f:§r %message%");
			set("nameTagFormat" , "%prefix%%name%");
			set("rankShop" ,new LinkedHashMap<String, Object>() );
			set("prefixsells" ,new LinkedHashMap<String, LinkedHashMap<String, Object>>() );
			}
		})).getAll();
	}
	public void cleanSellers() {
		LinkedHashMap<String, LinkedHashMap<String, Object>> Sellers = (LinkedHashMap<String, LinkedHashMap<String, Object>>) db.get("prefixsells");

		int now = (int) (System.currentTimeMillis() / 1000);
		Iterator<String> it = Sellers.keySet().iterator();
		String key;
		LinkedHashMap<String, Object> value;
		while(it.hasNext()) {
			key = it.next();
			value = Sellers.get(key);
			if(now - (int) value.get("date") > 86400) {
				if(this.plugin.getServer().getPlayer(key) != null) {
					this.plugin.getServer().broadcastMessage("§l§g" + key + "님§f의 칭호 판매가 §c종료§f되었습니다.");
					it.remove();
				}
			}
		}
	}
	public void save(Boolean async) {
		Config config = new Config(plugin.getDataFolder().getPath() + "/pluginDB.yml", Config.YAML);
		config.setAll((LinkedHashMap<String, Object>) this.db);
		config.save(async);
	}
	public void setDefaultPrefix(String prefix) {
		db.put("defaultPrefix", prefix);
	}
	public void setDefaultPrefixFormat(String format) {
		db.put("defaultPrefixFormat", format);
	}
	public void setChatFormat(String format) {
		db.put("chatFormat", format);
	}
	public void setNameTagFormat(String format) {
		db.put("nameTagFormat", format);
	}
	public String getDefaultPrefix() {
		return (String) db.get("defaultPrefix");
	}
	public String getDefaultPrefixFormat() {
		return (String) db.get("defaultPrefixFormat");
	}
	public String getChatFormat() {
		return (String) db.get("chatFormat");
	}
	public String getNameTagFormat() {
		return (String) db.get("nameTagFormat");
	}
	@SuppressWarnings("unchecked")
	public LinkedHashMap<String, LinkedHashMap<String, Object>> getSellingList(){
		return (LinkedHashMap<String, LinkedHashMap<String, Object>>) db.get("prefixsells");
	}
	
	public LinkedHashMap<String, Object> getSellingInfo(String player){
		return ((LinkedHashMap<String, LinkedHashMap<String, Object>>) db.get("prefixsells")).get(player);
	}
	
	public LinkedHashMap<String, Object> getSellingInfoFromPrefix(String prefix){
		LinkedHashMap<String, LinkedHashMap<String, Object>> LIST = (LinkedHashMap<String, LinkedHashMap<String, Object>>) db.get("prefixsells");
		String[] keys = (String[])LIST.keySet().toArray();
		for(String key : keys) {
			if(LIST.get(key).get("prefix").equals(prefix)) return LIST.get(key);
		}
		return null;
		}
	
	public Boolean isSelling(String player) {
		LinkedHashMap<String, LinkedHashMap<String, Object>> LIST = (LinkedHashMap<String, LinkedHashMap<String, Object>>) db.get("prefixsells");
		return LIST.containsKey(player);
	}
	public Boolean isSellingFromPrefix(String prefix) {
		LinkedHashMap<String, LinkedHashMap<String, Object>> LIST = (LinkedHashMap<String, LinkedHashMap<String, Object>>) db.get("prefixsells");
		String[] keys = (String[])LIST.keySet().toArray();
		for(String key : keys) {
			if(LIST.get(key).get("prefix").equals(prefix)) return true;
		}
		return false;
	}
	
	public void addSeller(String player, int cost,int price, String prefix) {
		LinkedHashMap<String, LinkedHashMap<String, Object>> Sellers = (LinkedHashMap<String, LinkedHashMap<String, Object>>) db.get("prefixsells");
		LinkedHashMap<String, Object> data = new LinkedHashMap<>();
		data.put("cost", cost);
		data.put("price", price);
		data.put("prefix", prefix);
		data.put("date", (int) (System.currentTimeMillis()  /1000) + 86400);
		Sellers.put(player, data);
		db.put("prefixsells", Sellers);
	}
	public void removeSeller(String player) {
		LinkedHashMap<String, LinkedHashMap<String, Object>> Sellers = (LinkedHashMap<String, LinkedHashMap<String, Object>>) db.get("prefixsells");
		Sellers.remove(player);
		db.put("prefixsells", Sellers);
	}
	public String applyPrefixFormat(String prefix) {
		return StringUtils.replace(((String) db.get("defaultPrefixFormat")),"%prefix%", prefix);
	}
	public String applyChatFormat(String name, String message) {
		String special = loader.getRankByName(name).getSpecialPrefix();
		String prefix = loader.getRankByName(name).getPrefix();
		String string = (String) db.get("chatFormat");
		
		special  = (special == null) ? "" : this.applyPrefixFormat(special) + " ";
		string = StringUtils.replace(string,"%special%", special);
		
		prefix  = (prefix == null) ? "" : this.applyPrefixFormat(prefix) + " ";
		string = StringUtils.replace(string,"%prefix%", prefix);
		string = StringUtils.replace(string,"%name%", TextFormat.WHITE + plugin.kc(name) + " ");
		string = StringUtils.replace(string,"%message%", " " + message);
		return string;
		}
	
	public String applyNameTagFormat(String name) {
		StringBuilder sb = new StringBuilder();
		RankData rank = loader.getRankByName(name);
		TreeMap<String, String> nameTagList = rank.getNameTagList();
		String string = (String) db.get("nameTagFormat");
		String prefix = (String) rank.getPrefix();
		prefix  = (prefix == null) ? "" : this.applyPrefixFormat(prefix) + " ";
		string = StringUtils.replace(string,"%prefix%", prefix);
		string = StringUtils.replace(string,"%name%", TextFormat.WHITE + name);
		sb.append(string);
		
		for(String nameTag : nameTagList.values()) {
			sb.append("\n" + nameTag);
		}
		
		return sb.toString();
	}
	
	public void applyNameTag(String name) {
		Player pl = server.getPlayer(name);
		if(pl instanceof Player) {
			pl.setNameTag(this.applyNameTagFormat(name));
		}
	}
	
	public void setRankShop(String levelname, double x, double y,double z, String prefix, int price, int prePrice) {
		LinkedHashMap<String, Object> rankShop = (LinkedHashMap<String, Object>) this.db.get("rankShop");
		rankShop.put((levelname+':'+x+':'+y+':'+z), new LinkedHashMap<String, Object>(){{
			put("prefix", prefix);
			put("price", price);
			put("prePrice", prePrice);
			}});
		this.db.put("rankShop",  rankShop);
	}
	
	public LinkedHashMap<String, Object> getRankShop(String levelname, double x, double y,double z){
		if(! ((LinkedHashMap<String, Object>) this.db.get("rankShop")).containsKey(levelname+':'+x+':'+y+':'+z)) return null;
		return (LinkedHashMap<String, Object>) ((LinkedHashMap<String, Object>) this.db.get("rankShop")).get(levelname+':'+x+':'+y+':'+z);
	}
	public boolean deleteRankShop(String levelname, double x, double y,double z){
		if(! ((LinkedHashMap<String, Object>) this.db.get("rankShop")).containsKey(levelname+':'+x+':'+y+':'+z)) return false;
		((LinkedHashMap<String, Object>) this.db.get("rankShop")).remove(levelname+':'+x+':'+y+':'+z);
		return true;
	}
	
	public RankData loadRank(String userName) {
		return loader.loadRank(userName);
	}
	public Boolean unloadRank(String userName) {
		return this.loader.unloadRank(userName);
	}
	public RankData getRank(Player player) {
		return this.loader.getRank(player);
	}
	public RankData getRankByName(String name) {
		return this.loader.getRankByName(name);
	}
	public RankProvider getInstance() {
		return this.instance;
	}
}
