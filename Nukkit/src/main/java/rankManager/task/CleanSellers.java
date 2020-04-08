package rankManager.task;

import cn.nukkit.scheduler.Task;
import rankManager.RankManager;

public class CleanSellers extends Task {
	protected RankManager owner;

	public CleanSellers(RankManager pl) {
		this.owner = pl;
	}
	@Override
	public void onRun(int currentTick) {
		this.owner.cleanSellers();
	}

}
