package scripts.commands.voicedcommandhandlers;

import javolution.text.TextBuilder;

import ru.agecold.Config;
import ru.agecold.gameserver.model.L2World;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.entity.TvTEvent;
import ru.agecold.gameserver.network.serverpackets.NpcHtmlMessage;

import scripts.autoevents.basecapture.BaseCapture;
import scripts.autoevents.lasthero.LastHero;
import scripts.autoevents.encounter.Encounter;
import scripts.autoevents.masspvp.massPvp;
import scripts.commands.IVoicedCommandHandler;

public class Events implements IVoicedCommandHandler {

    private static final String[] VOICED_COMMANDS = {"join", "leave", "eventhelp"};

    public boolean useVoicedCommand(String command, L2PcInstance player, String target) {
        if (player.isOutOfControl() || player.isParalyzed() || player.underAttack()) {
            player.sendHtmlMessage("� ������ ������ �� �� ������ ������������ ����� �������.");
            return false;
        }
        if (command.startsWith("join")) {
            String event = command.substring(4).trim();
            if (event.equalsIgnoreCase("tvt")) {
                TvTEvent.onBypass("tvt_event_participation", player);
            } else if (event.equalsIgnoreCase("lh")) {
                if (!Config.ELH_ENABLE) {
                    player.sendHtmlMessage("����� -��������� �����- ��������.");
                    return false;
                }
                LastHero.getEvent().regPlayer(player);
            } else if (event.equalsIgnoreCase("mpvp")) {
                if (!Config.MASS_PVP) {
                    player.sendHtmlMessage("����� -��������� �����- ��������.");
                    return false;
                }
                massPvp.getEvent().regPlayer(player);
            } else if (event.equalsIgnoreCase("bc")) {
                if (!Config.EBC_ENABLE) {
                    player.sendHtmlMessage("����� -������ ����- ��������.");
                    return false;
                }
                BaseCapture.getEvent().regPlayer(player);
            } else if (event.equalsIgnoreCase("enc")) {
                if (!Config.EENC_ENABLE) {
                    player.sendHtmlMessage("����� -�����- ��������.");
                    return false;
                }
                Encounter.getEvent().regPlayer(player);
            }
        } else if (command.startsWith("leave")) {
            String event = command.substring(5).trim();
            if (event.equalsIgnoreCase("tvt")) {
                if (!Config.TVT_EVENT_ENABLED) {
                    player.sendHtmlMessage("����� -���- ��������.");
                    return false;
                }
                if (TvTEvent.isParticipating()) {
                    if (!TvTEvent.isPlayerParticipant(player.getName())) {
                        player.sendHtmlMessage("�� �� ����������������.");
                        return false;
                    }
                    TvTEvent.onBypass("tvt_event_remove_participation", player);
                } else {
                    player.sendHtmlMessage("�� �� ������ �������� �����.");
                    return false;
                }
            } else if (event.equalsIgnoreCase("lh")) {
                if (!Config.ELH_ENABLE) {
                    player.sendHtmlMessage("����� -��������� �����- ��������.");
                    return false;
                }
                LastHero.getEvent().delPlayer(player);
            } else if (event.equalsIgnoreCase("mpvp")) {
                if (!Config.MASS_PVP) {
                    player.sendHtmlMessage("����� -��������� �����- ��������.");
                    return false;
                }
                massPvp.getEvent().onExit(player);
            } else if (event.equalsIgnoreCase("bc")) {
                if (!Config.EBC_ENABLE) {
                    player.sendHtmlMessage("����� -������ ����- ��������.");
                    return false;
                }
                BaseCapture.getEvent().delPlayer(player);
            } else if (event.equalsIgnoreCase("enc")) {
                if (!Config.EENC_ENABLE) {
                    player.sendHtmlMessage("����� -�����- ��������.");
                    return false;
                }
                Encounter.getEvent().delPlayer(player);
            }
        } else {
            NpcHtmlMessage nhm = NpcHtmlMessage.id(5);
            TextBuilder tb = new TextBuilder("<html><body>");
            tb.append("<center><font color=\"LEVEL\">����� �������</font></center><br><br>");
            if (Config.TVT_EVENT_ENABLED) {
                tb.append("<font color=CC3366>Team vs Team (���)</font><br1>");
                tb.append("<font color=66CC00>.join tvt</font> ������� �������.<br1>");
                tb.append("<font color=66CC00>.leave tvt</font> ����������.<br>");
            }
            if (Config.MASS_PVP) {
                tb.append("<font color=CC3366>Mass PvP (���� ���)</font><br1>");
                tb.append("<font color=66CC00>.join mpvp</font> ������� �������.<br1>");
                tb.append("<font color=66CC00>.leave mpvp</font> ����������.<br>");
            }
            if (Config.ELH_ENABLE) {
                tb.append("<font color=CC3366>Last Hero (��������� �����)</font><br1>");
                tb.append("<font color=66CC00>.join lh</font> ������� �������.<br1>");
                tb.append("<font color=66CC00>.leave lh</font> ����������.<br>");
            }
            if (Config.EBC_ENABLE) {
                tb.append("<font color=CC3366>Base Capture (������ ����)</font><br1>");
                tb.append("<font color=66CC00>.join bc</font> ������� �������.<br1>");
                tb.append("<font color=66CC00>.leave bc</font> ����������.<br>");
            }
            if (Config.EENC_ENABLE) {
                tb.append("<font color=CC3366>Encounter (�����)</font><br1>");
                tb.append("<font color=66CC00>.join enc</font> ������� �������.<br1>");
                tb.append("<font color=66CC00>.leave enc</font> ����������.<br>");
            }
            tb.append("<br><br>");
            tb.append("</body></html>");
            nhm.setHtml(tb.toString());
            player.sendPacket(nhm);
        }
        return true;
    }

    public String[] getVoicedCommandList() {
        return VOICED_COMMANDS;
    }
}