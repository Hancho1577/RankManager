package rankManager.listener.other;

import cn.nukkit.Server;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.Plugin;
import me.onebone.economyapi.EconomyAPI;

public class EconomyAPIListener implements Listener{
	private EconomyAPI economyAPI = null;
	public EconomyAPIListener(Plugin plugin) {
		Server server = Server.getInstance();
		if(server.getPluginManager().getPlugin("EconomyAPI") != null) {
			this.economyAPI = EconomyAPI.getInstance();
			server.getPluginManager().registerEvents(this, plugin);
		}
	}
	public EconomyAPI getEconomyApi() {
		return this.economyAPI;
	}
}
