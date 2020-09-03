package rankManager.task;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.Task;
import cn.nukkit.scheduler.TaskHandler;

public class ShowPrefixTitleTask extends Task {
    private int index = 0;
    private String title = "";
    private String prefix;
    private Player player;
    private TaskHandler taskHandler;

    public ShowPrefixTitleTask(Player player, String prefix){
        this.player = player;
        this.prefix = prefix;
    }

    public TaskHandler getTaskHandler() {
        return taskHandler;
    }

    public void setTaskHandler(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    @Override
    public void onRun(int currentTick) {
        if(this.taskHandler == null) return;
        if(this.player == null || !this.player.isOnline()) return;
        title += prefix.charAt(index);
        player.sendTitle(title, "", 0, 20, 0);
        index++;

        if(index >= prefix.length()) {
            player.sendTitle(title, "§o/칭호 §f명령어로 간편하게 설정하세요", 20, 40, 20);
            player.getLevel().addSound(player, Sound.RANDOM_LEVELUP, 1, 1, player);
            this.taskHandler.cancel();
        }
    }
}
