package rankManager.rank;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

public class RankData {
	private String userName;
	private String dataFolder;
	private Map<String, Object> data;
	private String nowSpecialPrefix = null;
	private Map<String, Object> specialPrefixList = new LinkedHashMap<String, Object>();
	int index;
	int is;
	String iss;

	public RankData(String userName, String dataFolder) {
		userName = userName.toLowerCase();

		this.userName = userName;
		this.dataFolder = dataFolder;
		this.load();
	}

	public void load() {
		this.data = (Map<String, Object>) (new Config(this.dataFolder + this.userName + ".yml", Config.YAML,
				new ConfigSection() {
					{
						set("nowPrefix", null);
						set("prefixList", new LinkedHashMap<String, Object>());
					}
				})).getAll();
	}

	public void save(boolean async) {
		Config data = new Config(dataFolder + userName + ".yml", Config.YAML);
		data.setAll((LinkedHashMap<String, Object>) this.data);
		data.save(async);
	}

	public void addPrefixs(String[] prefixs, int costs) {
		@SuppressWarnings("unchecked")
		LinkedHashMap<String, LinkedHashMap<String, Integer>> plist = (LinkedHashMap<String, LinkedHashMap<String, Integer>>) this.data
				.get("prefixList");
		for (String prefix : prefixs)
			plist.put(prefix, new LinkedHashMap<String, Integer>() {
				{
					put("costs", costs);
					put("isGuildPrefix", 0);
					put("date", (int) (new Date().getTime() / 1000));
				}
			});
		this.data.put("prefixList", plist);
	}

	public void addSpecialPrefixs(String[] prefixs) {
		for (String prefix : prefixs)
			this.specialPrefixList.put(prefix, true);
	}
	
	public void addNameTag(String pluginName, String nameTag) {
		TreeMap<String, String> nameTags = (TreeMap<String, String>) this.data.getOrDefault("nameTagList", new HashMap<String, String>());
		nameTags.put(pluginName, nameTag);
	}
	
	public void removeNameTag(String pluginName) {
		TreeMap<String, String> nameTags = (TreeMap<String, String>) this.data.getOrDefault("nameTagList", new HashMap<String, String>());
		nameTags.remove(pluginName);
	}

	@SuppressWarnings("unchecked")
	public void deletePrefixs(String[] prefixs) {
		LinkedHashMap<String, LinkedHashMap<String, Integer>> plist = (LinkedHashMap<String, LinkedHashMap<String, Integer>>) this.data
				.get("prefixList");
		for (String prefix : prefixs) {
			if (plist.containsKey(prefix)) {
				plist.remove(prefix);
			}
		}
		this.data.put("prefixList", plist);
	}

	@SuppressWarnings("unchecked")
	public void deleteSpecialPrefixs(String[] prefixs) {
		LinkedHashMap<String, LinkedHashMap<String, Integer>> plist = (LinkedHashMap<String, LinkedHashMap<String, Integer>>) this.data
				.get("prefixList");
		for (String prefix : prefixs) {
			if (plist.containsKey(prefix)) {
				plist.remove(prefix);
			}
		}
		this.data.put("prefixList", plist);
	}

	@SuppressWarnings("unchecked")
	public boolean isExistPrefix(String prefix) {
		return ((LinkedHashMap<String, LinkedHashMap<String, Integer>>) this.data.get("prefixList"))
				.containsKey(prefix);
	}

	public boolean isExistPrefixToIndex(int index) {
		return this.getPrefixToIndex(index) != null ? true : false;
	}

	public boolean setPrefix(String prefix) {
		this.data.put("nowPrefix", this.getIndexToPrefix(prefix));
		return true;
	}

	public void setSpecialPrefix(String prefix) {
		this.nowSpecialPrefix = prefix;
	}

	public String getPrefix() {
		return (String) this.getPrefixToIndex((int) data.get("nowPrefix"));
	}

	public String getSpecialPrefix() {
		return nowSpecialPrefix;
	}

	public LinkedHashMap<String, LinkedHashMap<String, Integer>> getPrefixList() {
		return (LinkedHashMap<String, LinkedHashMap<String, Integer>>) data.get("prefixList");
	}

	public Map<String, Object> getSpecialPrefixList() {
		return specialPrefixList;
	}
	
	public TreeMap<String, String> getNameTagList() {
		return (TreeMap<String, String>) data.getOrDefault("nameTagList", new TreeMap<String, String>());
	}

	@SuppressWarnings("unchecked")
	public int getIndexToPrefix(String Key) {
		index = 0;
		is = 0;
		((Map<String, LinkedHashMap<String, Integer>>) this.data.get("prefixList")).forEach((key, value) -> {
			if (key.equals(Key))
				is = index;
			index++;
		});
		return is;
	}

	@SuppressWarnings("unchecked")
	public String getPrefixToIndex(int Key) {
		index = 0;
		iss = null;
		((Map<String, LinkedHashMap<String, Integer>>) this.data.get("prefixList")).forEach((key, value) -> {
			if (index == Key)
				iss = key;
			index++;
		});
		return iss;
	}
}
