package rankManager.task;

import cn.nukkit.scheduler.Task;
import rankManager.rank.RankLoader;

public class AutoUnloadTask extends Task {
	protected final RankLoader owner;
	public AutoUnloadTask(RankLoader pl) {
		this.owner = pl;
	}
	@Override
	public void onRun(int currentTick) {
		this.owner.unloadRank(null);
	}

}
