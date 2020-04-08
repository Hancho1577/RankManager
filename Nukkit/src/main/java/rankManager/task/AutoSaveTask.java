package rankManager.task;

import cn.nukkit.scheduler.Task;
import rankManager.RankManager;

public class AutoSaveTask extends Task {
	protected RankManager owner;

	public AutoSaveTask(RankManager pl) {
		this.owner = pl;
	}
	@Override
	public void onRun(int currentTick) {
		this.owner.save(true);
	}

}
