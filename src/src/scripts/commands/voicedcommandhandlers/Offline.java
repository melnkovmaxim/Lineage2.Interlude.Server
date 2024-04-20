package scripts.commands.voicedcommandhandlers;

import ru.agecold.Config;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;

import scripts.commands.IVoicedCommandHandler;

public class Offline implements IVoicedCommandHandler {

    private static final String[] VOICED_COMMANDS = {"offline"};

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance player, String target) {
        if (command.equalsIgnoreCase("offline")) {
            if (player.isOutOfControl() || player.isParalyzed()) {
                return false;
            }

            if (player.underAttack()) {
                return false;
            }

            if (player.isInZonePeace()) {
                if (player.getPrivateStoreType() != 0) {
                    if (Config.OFFTRADE_COIN > 0 && !player.destroyItemByItemId(target, Config.OFFTRADE_COIN, Config.OFFTRADE_PRICE, player, true)) {
                        player.sendMessage("��������� ������ " + Config.OFFTRADE_PRICE + " " + Config.OFFTRADE_COIN_NAME);
                        return false;
                    }
                    player.setOfflineMode(true);
                } else {
                    player.sendMessage("������� ��������� �������� �� �������/�������");
                }
            } else {
                player.sendMessage("������ � ���������� ����");
            }
        }
        return true;
    }

    @Override
    public String[] getVoicedCommandList() {
        return VOICED_COMMANDS;
    }
}