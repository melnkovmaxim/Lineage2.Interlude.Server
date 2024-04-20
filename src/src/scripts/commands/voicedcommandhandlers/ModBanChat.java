package scripts.commands.voicedcommandhandlers;

import ru.agecold.Config;
import ru.agecold.gameserver.model.L2World;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IVoicedCommandHandler;

public class ModBanChat implements IVoicedCommandHandler {

    private static final String[] VOICED_COMMANDS = {"banchat", "unbanchat"};

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target) {
        if (activeChar.isModerator()) {
            String name = "";
            String[] cmdParams = command.split(" ");
            try {
                name = cmdParams[1];
            } catch (Exception e) {
                name = "_npe";
            }

            if (name.equalsIgnoreCase("_npe")) {
                activeChar.sendModerResultMessage("������ ��� ������ ����");
                return false;
            }

            L2PcInstance targetPlayer = L2World.getInstance().getPlayer(name);
            if (targetPlayer == null) {
                activeChar.sendModerResultMessage("����� �� ������ ��� ������ ��� ������ ����");
                return false;
            }

            //int obj = activeChar.getObjectId();
            String Moder = activeChar.getForumName();

            if (command.startsWith("banchat")) {
                long banLengthMins = Integer.parseInt(cmdParams[2]);

                if (targetPlayer.isChatBanned()) {
                    activeChar.sendModerResultMessage("��� " + targetPlayer.getName() + " ��� ������������ ���-�� ������");
                    return false;
                }

                if (targetPlayer.isModerator() && activeChar.getModerRank() > 1) {
                    return false;
                }

                if (banLengthMins > Config.MAX_BAN_CHAT) {
                    banLengthMins = Config.MAX_BAN_CHAT;
                }

                long banLength = banLengthMins * 60;

                activeChar.sendModerResultMessage("��� " + targetPlayer.getName() + " ������������ �� " + banLength + " ������. (" + banLengthMins + " �����)");
                targetPlayer.sendMessage("* * * * * * * * * * * * * *");
                targetPlayer.setChatBanned(true, banLength, "");
                targetPlayer.sendMessage("������� " + activeChar.getName() + "(" + Moder + ")");
                targetPlayer.sendMessage("��� �����������, ��������� ��� � �������� �����,");
                targetPlayer.sendMessage("������ �� ������ - ��������.");
                targetPlayer.sendMessage("* * * * * * * * * * * * * *");
                //
                activeChar.logModerAction(Moder, "��� ���� " + targetPlayer.getName() + " �� " + banLengthMins + " �����");
            } else if (command.startsWith("unbanchat")) {
                activeChar.sendModerResultMessage("��� " + targetPlayer.getName() + " �������������");
                targetPlayer.sendMessage("* * * * * * * * * * * * * *");
                targetPlayer.setChatBanned(false, 0, "");
                targetPlayer.sendMessage("������������ " + activeChar.getName() + "(" + Moder + ")");
                targetPlayer.sendMessage("* * * * * * * * * * * * * *");
                //
                activeChar.logModerAction(Moder, "���� ��� ���� " + targetPlayer.getName());
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String[] getVoicedCommandList() {
        return VOICED_COMMANDS;
    }
}