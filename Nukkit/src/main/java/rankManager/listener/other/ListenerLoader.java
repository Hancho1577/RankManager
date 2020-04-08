package rankManager.listener.other;

import cn.nukkit.event.Listener;
import cn.nukkit.plugin.Plugin;
import me.onebone.economyapi.EconomyAPI;

public class ListenerLoader implements Listener{
	private ListenerLoader instance = null;
	private EconomyAPIListener economyAPI;
	public ListenerLoader(Plugin plugin) {
		if(instance == null) instance = this;
		this.economyAPI = new EconomyAPIListener(plugin);
	}
	public EconomyAPI getEconomyAPI() {
		return this.economyAPI.getEconomyApi();
	}
	public ListenerLoader getInstance() {
		return this.instance;
	}
}
