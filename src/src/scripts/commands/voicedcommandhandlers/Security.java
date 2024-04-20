package scripts.commands.voicedcommandhandlers;

import javolution.text.TextBuilder;

import ru.agecold.Config;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.lib.Log;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.agecold.gameserver.util.Util;
import ru.agecold.util.TimeLogger;
import scripts.commands.IVoicedCommandHandler;

public class Security implements IVoicedCommandHandler {

    private static final String[] VOICED_COMMANDS = {"security", "security_"};

    /* (non-Javadoc)
     * @see ru.agecold.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, ru.agecold.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
     */
    @Override
    public boolean useVoicedCommand(String command, L2PcInstance player, String target) {
        if (command.equalsIgnoreCase("security")) {
            showWelcome(player);
        } else if (command.startsWith("security_")) {
            String choise = command.substring(9).trim();
            if (choise.startsWith("hwid")) {
                int flag = Integer.parseInt(choise.substring(4).trim());
                player.getClient().saveHWID((flag == 1));
                showWelcome(player);
                return true;
            } else if (choise.startsWith("pwd")) {
                String pass = "";
                String passr = "";
                String error = "��������� ������ ��� ��������� ������.";
                boolean ok = false;
                try {
                    String[] pwd = choise.substring(4).split(" ");
                    pass = pwd[0];
                    passr = pwd[1];
                    if ((pass.length() < 5 || pass.length() > 15) || (passr.length() < 5 || passr.length() > 15)) {
                        error = "������ ������ ���� ����� 5 � ����� 15 ��������.";
                    } else if (!Util.isAlphaNumeric(pass) || !Util.isAlphaNumeric(passr)) {
                        error = "������ �������� ����������� �������.";
                    } else if (!pass.equalsIgnoreCase(passr)) {
                        error = "������ �� ���������.";
                    } else {
                        ok = true;
                    }
                } catch (Exception e) {
                    ok = false;
                }
                if (ok && player.updatePassword(pass)) {
                    player.sendHtmlMessage("����� ������ ����������.");
                } else {
                    player.sendHtmlMessage(error);
                }

                return true;
            } else if (choise.startsWith("email")) {
                String pass = "";
                String passr = "";
                String error = "��������� ������ ��� ��������� ������.";
                boolean ok = false;
                try {
                    String[] pwd = choise.substring(6).split(" ");
                    pass = pwd[0];
                    passr = pwd[1];
                    if ((pass.length() < 5 || pass.length() > 35) || (passr.length() < 5 || passr.length() > 35)) {
                        error = "����� ������ ���� ����� 5 � ����� 35 ��������.";
                    } else if (!Util.isValidEmail(pass) || !Util.isValidEmail(passr)) {
                        error = "����� �������� ����������� �������.";
                    } else if (!pass.equalsIgnoreCase(passr)) {
                        error = "������ �� ���������.";
                    } else {
                        ok = true;
                    }
                } catch (Exception e) {
                    ok = false;
                }
                if (ok && player.updateEmail(pass)) {
                    player.getClient().setHasEmail(true);
                    player.sendHtmlMessage("����� ����� ������� ����������.<center><br>==========<br> <font color=\"33CC00\">" + pass + "</font><br>==========</center>");
                } else {
                    //player.sendHtmlMessage(error);
                    player.sendAdmResultMessage(error);
                    player.sendUserPacket(Static.CHANGE_EMAIL);
                }
                return true;
            }
            return false;
        }
        return true;
    }

    private void showWelcome(L2PcInstance player) {
        if (player.isParalyzed() || player.getUserKey().on == 1) {
            player.sendActionFailed();
            return;
        }

        NpcHtmlMessage nhm = NpcHtmlMessage.id(5);
        TextBuilder build = new TextBuilder("<html><body>");
        build.append("<font color=\"LEVEL\">��������� ������������.</font><br>");
        build.append("<table width=290>");
        if (Config.VS_HWID) {
            build.append("<tr><td width=180><font color=66CC00>�������� ���� � ����������:</font></td>");
            if (player.getClient().getMyHWID().length() > 5) {
                build.append("<td>[���.]</td>");
                build.append("<td><button value=\"����.\" action=\"bypass -h security_hwid 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            } else {
                build.append("<td><button value=\"���.\" action=\"bypass -h security_hwid 1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
                build.append("<td>[����.]</td></tr>");
            }
        }
        if (Config.VS_PWD) {
            build.append("<tr><td width=180><font color=66CC00>����� ������:</font></td>");
            build.append("<td></td><td></td></tr><tr><td width=180>������� ����� ������:</td><td> </td><td></td></tr>");
            build.append("<tr><td><edit var=\"pwd\" width=120 length=\"16\"></td>");
            build.append("<td></td><td></td></tr><tr><td width=180>��������� ����� ������:</td><td> </td><td></td></tr>");
            build.append("<tr><td><edit var=\"pwdr\" width=120 length=\"16\"></td>");
            build.append("<td><button value=\"���.\" action=\"bypass -h security_pwd $pwd $pwdr\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td> </td></tr>");
        }
        build.append("</table></body></html>");
        nhm.setHtml(build.toString());
        player.sendPacket(nhm);
        build.clear();
        build = null;
    }

    @Override
    public String[] getVoicedCommandList() {
        return VOICED_COMMANDS;
    }
}