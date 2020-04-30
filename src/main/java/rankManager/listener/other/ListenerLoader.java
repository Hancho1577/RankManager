package rankManager.listener.other;

import cn.nukkit.event.Listener;
import cn.nukkit.plugin.Plugin;
import me.onebone.economyapi.EconomyAPI;

public class ListenerLoader implements Listener{
	private ListenerLoader instance = null;
	private final EconomyAPIListener economyAPI;
	public ListenerLoader(Plugin plugin) {
		instance = this;
		this.economyAPI = new EconomyAPIListener(plugin);
	}
	public EconomyAPI getEconomyAPI() {
		return this.economyAPI.getEconomyApi();
	}
	public ListenerLoader getInstance() {
		return this.instance;
	}
}
