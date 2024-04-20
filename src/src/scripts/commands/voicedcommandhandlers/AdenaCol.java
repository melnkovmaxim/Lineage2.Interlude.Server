package scripts.commands.voicedcommandhandlers;

import ru.agecold.Config;
import ru.agecold.Config.EventReward;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.util.Util;
import scripts.commands.IVoicedCommandHandler;

public class AdenaCol implements IVoicedCommandHandler {

    private static final String[] VOICED_COMMANDS = {"adena", "col"};
    private static final EventReward ADENA = Config.CMD_AC_ADENA;
    private static final EventReward COL = Config.CMD_AC_COL;

    /* (non-Javadoc)
     * @see ru.agecold.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, ru.agecold.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
     */
    @Override
    public boolean useVoicedCommand(String command, L2PcInstance player, String target) {
        if (command.equalsIgnoreCase("col")) {
            if (player.getItemCount(ADENA.id) >= ADENA.count) {
                player.destroyItemByItemId(".col", ADENA.id, ADENA.count, player, true);
                player.addItem(".col", COL.id, ADENA.chance, player, true);
            } else {
                player.sendCritMessage("���� ������: " + Util.formatAdena(ADENA.count) + " Adena �� " + Util.formatAdena(ADENA.chance) + " Coin");
            }
        } else if (command.equalsIgnoreCase("adena")) {
            if (player.getItemCount(COL.id) >= COL.count) {
                player.destroyItemByItemId(".col", COL.id, COL.count, player, true);
                player.addItem(".adena", ADENA.id, COL.chance, player, true);
            } else {
                player.sendCritMessage("���� ������: " + Util.formatAdena(COL.count) + " Coin �� " + Util.formatAdena(COL.chance) + " Adena");
            }
        } else if (command.startsWith("col")) {
            int count = 0;
            try {
                count = Integer.parseInt(command.substring(4));
            } catch (Exception e) {
                player.sendMessage("������� ����� �����.");
                return true;
            }
            if (Config.CMD_AC_COL_LIMIT == 0 || count > Config.CMD_AC_COL_LIMIT) {
                player.sendMessage("�������� " + Config.CMD_AC_COL_LIMIT + " �� ���� �����.");
                return true;
            }

            if (player.getItemCount(ADENA.id) >= ADENA.count * count) {
                player.destroyItemByItemId(".col", ADENA.id, ADENA.count * count, player, true);
                player.addItem(".col", COL.id, ADENA.chance * count, player, true);
            } else {
                player.sendCritMessage("���� ������: " + Util.formatAdena(ADENA.count) + " Adena �� " + Util.formatAdena(ADENA.chance) + " Coin");
            }
        } else if (command.startsWith("adena")) {
            int count = 0;
            try {
                count = Integer.parseInt(command.substring(6));
            } catch (Exception e) {
                player.sendMessage("������� ����� �����.");
                return true;
            }
            if (Config.CMD_AC_ADENA_LIMIT == 0 || count > Config.CMD_AC_ADENA_LIMIT) {
                player.sendMessage("�������� " + Config.CMD_AC_ADENA_LIMIT + " �� ���� �����.");
                return true;
            }

            if (player.getItemCount(COL.id) >= COL.count * count) {
                player.destroyItemByItemId(".col", COL.id, COL.count * count, player, true);
                player.addItem(".adena", ADENA.id, COL.chance * count, player, true);
            } else {
                player.sendCritMessage("���� ������: " + Util.formatAdena(COL.count) + " Coin �� " + Util.formatAdena(COL.chance) + " Adena");
            }
        }
        return true;
    }

    @Override
    public String[] getVoicedCommandList() {
        return VOICED_COMMANDS;
    }
}