package ru.agecold.gameserver.model.actor.instance;

import java.util.logging.Logger;

import ru.agecold.Config;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.datatables.ItemTable;
import ru.agecold.gameserver.instancemanager.EventManager;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.entity.FightClub;
import ru.agecold.gameserver.model.entity.TvTEvent;
import ru.agecold.gameserver.model.entity.olympiad.Olympiad;
import ru.agecold.gameserver.network.serverpackets.ExMailArrived;
import ru.agecold.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.agecold.gameserver.templates.L2Item;
import ru.agecold.gameserver.templates.L2NpcTemplate;
import ru.agecold.util.log.AbstractLogger;
import scripts.autoevents.basecapture.BaseCapture;
import scripts.autoevents.encounter.Encounter;
import scripts.autoevents.fighting.Fighting;
import scripts.autoevents.lasthero.LastHero;
import scripts.autoevents.masspvp.massPvp;
import scripts.autoevents.openseason.OpenSeason;

import javolution.util.FastTable;

public class L2EventerInstance extends L2NpcInstance {

    private static Logger _log = AbstractLogger.getLogger(L2EventerInstance.class.getName());
    private FastTable<L2PcInstance> _winners;
    private static final FastTable<Integer> _allowItems = Config.FC_ALLOWITEMS;

    public L2EventerInstance(int objectId, L2NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(L2PcInstance player, String command) {
        /*
         * if (Olympiad.getInstance().inCompPeriod()) { showError(player, "��
         * ����� ��������� ��� �� ����������"); return;
		}
         */
        //System.out.println("#### " + command);

        if (player.getKarma() > 0 || player.isCursedWeaponEquiped()) {
            player.sendHtmlMessage("� ��� ������ �����.");
            return;
        }

        if (!(command.equalsIgnoreCase("fc_delme")) && (player.inFClub() || FightClub.isRegged(player.getObjectId()))) {
            showError(player, "�� ��� ���������������� � ���������� �����.<br><a action=\"bypass -h npc_" + getObjectId() + "_fc_delme\">���������� �� �������</a>");
            return;
        }

        if (command.equalsIgnoreCase("massPvpReg")) {
            if (!Config.MASS_PVP) {
                showError(player, Static.EVENT_DISABLED);
                return;
            }
            massPvp.getEvent().regPlayer(player);
        } else if (command.equalsIgnoreCase("massPvpStat")) {
            if (!Config.MASS_PVP) {
                showError(player, Static.EVENT_DISABLED);
                return;
            }
            if (System.currentTimeMillis() - player.getMPVPLast() < 1000) {
                showError(player, "���������� ��� � �������.");
                return;
            }
            player.setMPVPLast();
            showMassPvP(player);
        } else if (command.equalsIgnoreCase("lastHeroReg")) {
            if (!Config.ELH_ENABLE) {
                showError(player, Static.EVENT_DISABLED);
                return;
            }
            LastHero.getEvent().regPlayer(player);
        } else if (command.equalsIgnoreCase("lastHeroDel")) {
            if (!Config.ELH_ENABLE) {
                showError(player, Static.EVENT_DISABLED);
                return;
            }
            LastHero.getEvent().delPlayer(player);
        } else if (command.equalsIgnoreCase("fightingReg")) {
            if (!Config.FIGHTING_ENABLE) {
                showError(player, Static.EVENT_DISABLED);
                return;
            }
            Fighting.getEvent().regPlayer(player);
        } else if (command.equalsIgnoreCase("fightingDel")) {
            if (!Config.FIGHTING_ENABLE) {
                showError(player, Static.EVENT_DISABLED);
                return;
            }
            Fighting.getEvent().delPlayer(player);
        } else if (command.equalsIgnoreCase("baseCaptureReg")) {
            if (!Config.EBC_ENABLE) {
                showError(player, Static.EVENT_DISABLED);
                return;
            }
            BaseCapture.getEvent().regPlayer(player);
        } else if (command.equalsIgnoreCase("baseCaptureDel")) {
            if (!Config.EBC_ENABLE) {
                showError(player, Static.EVENT_DISABLED);
                return;
            }
            BaseCapture.getEvent().delPlayer(player);
        } else if (command.equalsIgnoreCase("openSeasonReg")) {
            if (!Config.OPEN_SEASON) {
                showError(player, Static.EVENT_DISABLED);
                return;
            }
            OpenSeason.getEvent().regPlayer(player);
        } else if (command.equalsIgnoreCase("encounterReg")) {
            if (!Config.EENC_ENABLE) {
                showError(player, Static.EVENT_DISABLED);
                return;
            }
            Encounter.getEvent().regPlayer(player);
        } else if (command.equalsIgnoreCase("encounterDel")) {
            if (!Config.EENC_ENABLE) {
                showError(player, Static.EVENT_DISABLED);
                return;
            }
            Encounter.getEvent().delPlayer(player);
        } else if (command.startsWith("fc_")) {
            if (!EventManager.getInstance().checkPlayer(player)) {
                showError(player, "Oops!");
                return;
            }

            String choise = command.substring(3).trim();

            if (choise.equalsIgnoreCase("show")) // ����� ����������
            {
                FightClub.showFighters(player, getObjectId());
            } else if (choise.equalsIgnoreCase("items")) // ����� ����������
            {
                showAllowItems(player);
            } else if (choise.equalsIgnoreCase("delme")) // ����� ����������
            {
                player.setFClub(false);
                player.setFightClub(false);
                player.setEventWait(false);
                FightClub.unReg(player.getObjectId(), false);
                player.sendCritMessage("����������� ��������, ������ ���������� �� �����.");
                player.sendPacket(new ExMailArrived());
            } else if (choise.startsWith("reg")) // �����������, ����� ������
            {
                int type = 0;
                try {
                    type = Integer.parseInt(choise.substring(3).trim());
                } catch (Exception e) {
                    //
                }
                FightClub.showInventoryItems(player, type, getObjectId());
            } else if (choise.startsWith("item_")) // �����������, ����� ������
            {
                try {
                    String[] opaopa = choise.split("_");
                    int type = Integer.parseInt(opaopa[1]);
                    int obj = Integer.parseInt(opaopa[2]);
                    if (obj == 0) {
                        showError(player, "������ �� �������. 1");
                        return;
                    }
                    FightClub.showItemFull(player, obj, type, getObjectId());
                } catch (Exception e) //catch (ArrayIndexOutOfBoundsException e)
                {
                    showError(player, "������ �� �������. 2");
                    return;
                }
            } else if (choise.startsWith("add")) // ������, ����������
            {
                int obj = 0;
                int count = 0;
                String pass = "";
                //System.out.println("#||" + choise + "||");
                try {
                    String[] opaopa = choise.split(" ");
                    obj = Integer.parseInt(opaopa[1]);
                    count = Integer.parseInt(opaopa[2]);
                    //pass = opaopa[3];
                    //System.out.println("#### 1: " + obj + "//"+count + " ||"+pass);
                } catch (Exception e) //catch (ArrayIndexOutOfBoundsException e)
                {
                    //showError(player, "������ �� �������?");
                    //return;
                    obj = 0;
                    count = 0;
                }

                if (obj == 0 || count == 0) {
                    showError(player, "������ �� �������. 3");
                    return;
                }

                FightClub.finishItemFull(player, obj, count, pass, getObjectId());
            } else if (choise.startsWith("enemy")) // �������� �����
            {
                int id = 0;
                try {
                    id = Integer.parseInt(choise.substring(5).trim());
                } catch (Exception e) {
                    //
                }
                FightClub.showEnemyDetails(player, id, getObjectId());
            } else if (choise.startsWith("accept")) // �������� �����
            {
                int id = 0;
                try {
                    id = Integer.parseInt(choise.substring(6).trim());
                } catch (Exception e) {
                    //
                }
                FightClub.startFight(player, id, getObjectId());
            } else if (choise.equalsIgnoreCase("view")) // ����� ����������
            {
                FightClub.viewFights(player, getObjectId());
            } else if (choise.startsWith("arview")) // ����� ����������
            {
                int arena = 0;
                try {
                    arena = Integer.parseInt(choise.substring(6).trim());
                } catch (Exception e) {
                    //
                }
                FightClub.viewArena(player, arena, getObjectId());
            }
        } else {
            super.onBypassFeedback(player, command);
        }

        player.sendActionFailed();
    }

    /**
     * �������
     */
    private void showMassPvP(L2PcInstance player) {
        _winners = new FastTable<L2PcInstance>();
        _winners.addAll(massPvp.getEvent().getWinners());

        String swinner = massPvp.getEvent().getWinner();
        int round = massPvp.getEvent().getRound();

        NpcHtmlMessage reply = NpcHtmlMessage.id(getObjectId());
        StringBuffer replyMSG = new StringBuffer("<html><body><font color=LEVEL>-���� ���-</font>");
        if (round > 0) {
            replyMSG.append("<table width=280><tr><td></td><td align=right><font color=336699>��������� �����:</font> <font color=33CCFF>" + round + " </font></td></tr>");
        } else {
            replyMSG.append("<table width=280><tr><td></td><td align=right><font color=336699>��������� ������ ������</font> <font color=33CCFF> </font></td></tr>");
        }
        replyMSG.append("<tr><td>����������:</td><td align=right></td></tr>");

        for (int i = 0, n = _winners.size(); i < n; i++) {
            L2PcInstance winner = _winners.get(i);
            replyMSG.append("<tr><td>����� " + (i + 1) + "</td><td align=right> " + winner.getName() + " </td></tr>");
        }

        if (!swinner.equalsIgnoreCase("d")) {
            replyMSG.append("<tr><td>������� </td><td align=right> ����������: " + swinner + "</td></tr>");
        }

        replyMSG.append("</table><br><br>");
        replyMSG.append("<a action=\"bypass -h npc_" + getObjectId() + "_massPvpStat\">��������</a><br>");
        replyMSG.append("</body></html>");

        reply.setHtml(replyMSG.toString());
        player.sendPacket(reply);
        replyMSG = null;
        reply = null;
    }

    /**
     * ���������� ����
     */
    private void showAllowItems(L2PcInstance player) {
        NpcHtmlMessage reply = NpcHtmlMessage.id(0);
        StringBuilder replyMSG = new StringBuilder("<html><body>");
        replyMSG.append("���������� ����:<br>����������� ������<br><table width=300>");

        for (int i = 0, n = _allowItems.size(); i < n; i++) {
            Integer itemId = _allowItems.get(i);
            if (itemId == null) {
                continue;
            }

            L2Item item = ItemTable.getInstance().getTemplate(itemId);
            if (item == null) {
                continue;
            }

            replyMSG.append("<tr><td><img src=\"" + item.getIcon() + "\" width=32 height=32></td><td>" + item.getName() + "</td></tr>");
        }

        replyMSG.append("</table><br><br><a action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">���������</a><br>");
        replyMSG.append("</body></html>");
        reply.setHtml(replyMSG.toString());
        player.sendPacket(reply);
        player.sendActionFailed();
        replyMSG = null;
        reply = null;
    }

    @Override
    public void showError(L2PcInstance player, String errorText) {
        player.setFCItem(0, 0, 0, 0);
        player.sendHtmlMessage("-��������� ����-", errorText);
        player.sendActionFailed();
    }
    /*
     * UPDATE `npc` SET
     * `id`='80010',`idTemplate`='20788',`name`='Master',`title`='Mass
     * PvP',`type`='L2Eventer' WHERE `id`='70022';
     */
}
