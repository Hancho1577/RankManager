package rankManager.rank;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

@SuppressWarnings("unchecked")
public class RankData {
	private final String userName;
	private final String dataFolder;
	private Map<String, Object> data;
	private String nowSpecialPrefix = null;
	private final Map<String, Object> specialPrefixList = new LinkedHashMap<>();
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
		this.data = (new Config(this.dataFolder + this.userName + ".yml", Config.YAML,
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

	public void addPrefixes(String[] prefixes, int costs) {
		@SuppressWarnings("unchecked")
		LinkedHashMap<String, LinkedHashMap<String, Integer>> plist = (LinkedHashMap<String, LinkedHashMap<String, Integer>>) this.data
				.get("prefixList");
		for (String prefix : prefixes)
			plist.put(prefix, new LinkedHashMap<String, Integer>() {
				{
					put("costs", costs);
					put("isGuildPrefix", 0);
					put("date", (int) (new Date().getTime() / 1000));
				}
			});
		this.data.put("prefixList", plist);
	}

	public void addSpecialPrefixes(String[] prefixes) {
		for (String prefix : prefixes)
			this.specialPrefixList.put(prefix, true);
	}
	
	public void addNameTag(String pluginName, String nameTag) {
		TreeMap<String, String> nameTags = this.getNameTagList();
		nameTags.put(pluginName, nameTag);
		this.data.put("nameTagList", nameTags);
	}
	
	public void removeNameTag(String pluginName) {
		TreeMap<String, String> nameTags = this.getNameTagList();
		nameTags.remove(pluginName);
		this.data.put("nameTagList", nameTags);
	}

	@SuppressWarnings("unchecked")
	public void deletePrefixes(String[] prefixes) {
		LinkedHashMap<String, LinkedHashMap<String, Integer>> plist = (LinkedHashMap<String, LinkedHashMap<String, Integer>>) this.data
				.get("prefixList");
		for (String prefix : prefixes) {
			plist.remove(prefix);
		}
		this.data.put("prefixList", plist);
	}

	@SuppressWarnings("unchecked")
	public void deleteSpecialPrefixes(String[] prefixes) {
		LinkedHashMap<String, LinkedHashMap<String, Integer>> plist = (LinkedHashMap<String, LinkedHashMap<String, Integer>>) this.data
				.get("prefixList");
		for (String prefix : prefixes) {
			plist.remove(prefix);
		}
		this.data.put("prefixList", plist);
	}

	@SuppressWarnings("unchecked")
	public boolean isExistPrefix(String prefix) {
		return ((LinkedHashMap<String, LinkedHashMap<String, Integer>>) this.data.get("prefixList"))
				.containsKey(prefix);
	}

	public boolean isExistPrefixToIndex(int index) {
		return this.getPrefixByIndex(index) != null;
	}

	public boolean setPrefix(String prefix) {
		this.data.put("nowPrefix", this.getIndexByPrefix(prefix));
		return true;
	}

	public void setSpecialPrefix(String prefix) {
		this.nowSpecialPrefix = prefix;
	}

	public String getPrefix() {
		return this.getPrefixByIndex((int) data.get("nowPrefix"));
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
		if(!data.containsKey("nameTagList")) {
			return new TreeMap<>();
		}
		TreeMap<String, String> treeMap;

		if(data.get("nameTagList") instanceof ConfigSection) {
			LinkedHashMap<String, String> map =  (LinkedHashMap<String, String>) data.get("nameTagList");
			treeMap = new TreeMap<>(map);
		} else {
			treeMap = (TreeMap<String, String>) data.get("nameTagList");
		}
		return treeMap;
	}

	@SuppressWarnings("unchecked")
	public int getIndexByPrefix(String prefix) {
		index = 0;
		is = 0;
		((Map<String, LinkedHashMap<String, Integer>>) this.data.get("prefixList")).forEach((key, value) -> {
			if (key.equals(prefix))
				is = index;
			index++;
		});
		return is;
	}

	@SuppressWarnings("unchecked")
	public String getPrefixByIndex(int Key) {
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
