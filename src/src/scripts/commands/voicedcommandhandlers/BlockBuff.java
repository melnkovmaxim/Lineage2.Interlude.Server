package scripts.commands.voicedcommandhandlers;

import ru.agecold.Config;
import ru.agecold.gameserver.datatables.SkillTable;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;

import scripts.commands.IVoicedCommandHandler;

public class BlockBuff implements IVoicedCommandHandler {

    private static final String[] VOICED_COMMANDS = {"blockbuff"};

    public boolean useVoicedCommand(String command, L2PcInstance player, String target) {
        if (command.equalsIgnoreCase("blockbuff")) {
            if (player.isOutOfControl() || player.isParalyzed()) {
                return false;
            }

            if (player.underAttack()) {
                return false;
            }

            if (player.isBlockingBuffs()) {
                player.stopSkillEffects(Config.ANTIBUFF_SKILLID);
                player.sendMessage("�������� ��������.");
                return true;
            }
            SkillTable.getInstance().getInfo(Config.ANTIBUFF_SKILLID, 1).getEffects(player, player);
            player.sendMessage("�������� �����������.");
        }
        return true;
    }

    public String[] getVoicedCommandList() {
        return VOICED_COMMANDS;
    }
}