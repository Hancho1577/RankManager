package rankManager;

import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.event.Listener;
import hancho.BossbarManager.BossbarManager;
import hancho.plugin.nukkit.mailbox.mailbox;
import rankManager.listener.EventListener;
import rankManager.listener.other.ListenerLoader;
import rankManager.rank.RankData;
import rankManager.rank.RankLoader;
import rankManager.rank.RankProvider;
import rankManager.task.AutoSaveTask;
import rankManager.task.CleanSellers;
import cn.nukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandMap;
import cn.nukkit.command.PluginCommand;

public class RankManager extends PluginBase implements Listener {
	public static final String PREFIX = "§l§f《 서버 》§f";
	public BossbarManager bossbarManager;
	public mailbox mailbox;
	private static RankManager instance;
	private  Config config;
	private Map<String, Object> data;
	private Config test;
	private EventListener eventListener;
	private RankLoader rankLoader;
	private RankProvider rankProvider;
	private ListenerLoader listenerLoader;

	@Override
	public void onEnable() {
		if(instance == null) instance = this;
		this.saveDefaultConfig();
		this.config = new Config(this.getDataFolder().getPath() + "/list.yml", Config.YAML);
		this.data = config.getAll();
		this.rankLoader = new RankLoader(this);
		this.rankProvider = new RankProvider(this);
		this.listenerLoader = new ListenerLoader(this);
		this.eventListener = new EventListener(this);
		
		this.getServer().getPluginManager().registerEvents(this, this);
		this.getServer().getScheduler().scheduleRepeatingTask(new AutoSaveTask(this), 12000, true);
		this.getServer().getScheduler().scheduleRepeatingTask(new CleanSellers(this), 1200, true);
		
		if(getServer().getPluginManager().getPlugin("mailbox") != null) {
			this.mailbox = (hancho.plugin.nukkit.mailbox.mailbox) getServer().getPluginManager().getPlugin("mailbox");
		}
		if(getServer().getPluginManager().getPlugin("BossbarManager") != null) {
			this.bossbarManager = (BossbarManager) getServer().getPluginManager().getPlugin("BossbarManager");
		}
		
		this.registerCommand("칭호", "rankmanager.rank.manage", "칭호를 관리합니다.", "칭호");
	}
	
	@Override
	public void onDisable() {
		save(false);
	}
	
	public void addMail(String name, String mail) {
		if(this.mailbox != null) {
			mailbox.addMail(name, mail);
		}
	}
	
	public void addNameTag(String pluginName , String playerName, String nameTag) {
		RankData rank = this.rankLoader.getRankByName(playerName);
		if(rank == null) {
			this.getLogger().warning(playerName + "님을 찾을 수 없습니다.");
			return;
		}
		rank.addNameTag(pluginName, nameTag);
	}
	
	public void removeNameTag(String pluginName , String playerName) {
		RankData rank = this.rankLoader.getRankByName(playerName);
		if(rank == null) {
			this.getLogger().warning(playerName + "님을 찾을 수 없습니다.");
			return;
		}
		rank.removeNameTag(pluginName);
	}
	
	public void showBossbar(String content) {
		if(this.bossbarManager != null) {
			this.bossbarManager.createBossbar(content);
		}
	}
	
	public void cleanSellers() {
		this.rankProvider.cleanSellers();
	}
	
	public void save(boolean async) {
		this.rankLoader.save(async);
		this.rankProvider.save(async);
		config.setAll((LinkedHashMap<String, Object>) data);
		config.save();
	}
	
	public RankProvider getRankProvider() {
		return this.rankProvider;
	}
	
	public RankLoader getRankLoader() {
		return this.rankLoader;
	}
	
	public EventListener getEventListener() {
		return eventListener;
	}
	
	public ListenerLoader getListenerLoader() {
		return listenerLoader;
	}
	
	public void korea(String name, String a) {
		data.put(name.toLowerCase(), a);
	}
	
	public String kc(String name) {
		name = name.toLowerCase();
		if(! data.containsKey(name)) {
			return name;
		}
		return  data.get(name.toLowerCase()).toString();
	}
	
	public void registerCommand(String name, String permission, String description, String usage) {
		CommandMap commandMap = this.getServer().getCommandMap();
		PluginCommand<Plugin> command = new PluginCommand<>(name,this);
		command.setDescription(description);
		command.setPermission(permission);
		command.setUsage(usage);
		commandMap.register(name, command);
	}
	
	public void message(Player player, String text) {
		player.sendMessage(PREFIX + " " + text);
	}

	public void message(CommandSender player, String text) {
		player.sendMessage(PREFIX + " " + text);
	}
	
	public void alert(Player player, String text) {
		player.sendMessage(PREFIX + " " + text);
	}

	public void alert(CommandSender player, String text) {
		player.sendMessage(PREFIX + " " + text);
	}
	
	public static RankManager getInstance() {
		return instance;
	}
	
	public boolean onCommand(CommandSender player, Command command, String label, String[] args) {
		return eventListener.onCommand(player, command, label, args);
	}
}
