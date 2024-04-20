package ru.agecold.gameserver.model.actor.instance;

import ru.agecold.Config;
import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.instancemanager.CoupleManager;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.L2Party;
import ru.agecold.gameserver.model.entity.Wedding;
import ru.agecold.gameserver.templates.L2NpcTemplate;

//������ ���� [s]�����������[/s] �������������; �������� ���� �������.
public class L2WeddingManagerInstance extends L2NpcInstance {

    private boolean _active = false;
    private Wedding wed = null;

    public L2WeddingManagerInstance(int objectId, L2NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(L2PcInstance player, String command) {
        if (!Config.L2JMOD_ALLOW_WEDDING) {
            player.sendHtmlMessage("� ������ ������ � � �������.");
            return;
        }

        if (_active) {
            player.sendHtmlMessage("� ������ ������ � ������.<br> �������� ���������� ������: " + (Config.L2JMOD_WEDDING_INTERVAL / 1000) + " ������.");
            return;
        }

        if (command.equalsIgnoreCase("engage")) {
            L2Party party = player.getParty();
            if (party == null) {
                player.sendHtmlMessage("�� ������ ���� ������ � ���� �� ����� ���������.");
                return;
            }

            if (player.getAppearance().getSex()) {
                party.broadcastHtmlToPartyMembers("� ���� ������������� ������ � �������.");
                return;
            }

            if (party.getMemberCount() != 2) {
                party.broadcastHtmlToPartyMembers("������ ����� � ������� ������ ���� � ����.");
                return;
            }

            if (!party.isLeader(player)) {
                party.broadcastHtmlToPartyMembers("����� ������ ���� ������� ����.");
                return;
            }

            if (player.getAppearance().getSex() == party.getPartyMembers().get(1).getAppearance().getSex()) {
                party.broadcastHtmlToPartyMembers("��������� ����� ���������!");
                return;
            }

            if (player.isMarried() || party.getPartyMembers().get(1).isMarried()) {
                String married = party.getPartyMembers().get(1).getName();
                if (player.isMarried()) {
                    married = player.getName();
                }

                party.broadcastHtmlToPartyMembers(married + " ��� � �����!!");
                return;
            }

            if (!isWearedFormal(player) || !isWearedFormal(party.getPartyMembers().get(1))) {
                party.broadcastHtmlToPartyMembers("�� ������ ���� � ��������� �������.");
                return;
            }

            if (Config.L2JMOD_WEDDING_COIN > 0) {
                L2ItemInstance coin = player.getInventory().getItemByItemId(Config.L2JMOD_WEDDING_COIN);
                if (coin == null || coin.getCount() < Config.L2JMOD_WEDDING_PRICE) {
                    party.broadcastHtmlToPartyMembers("����� ������ ��������: " + Config.L2JMOD_WEDDING_PRICE + " " + Config.L2JMOD_WEDDING_COINNAME + ".");
                    return;
                }

                if (!player.destroyItemByItemId("WEDDING", Config.L2JMOD_WEDDING_COIN, Config.L2JMOD_WEDDING_PRICE, player, true)) {
                    party.broadcastHtmlToPartyMembers("����� ������ ��������: " + Config.L2JMOD_WEDDING_PRICE + " " + Config.L2JMOD_WEDDING_COINNAME + ".");
                    return;
                }
            }

            _active = true;
            wed = new Wedding(player, party.getPartyMembers().get(1), this);
            CoupleManager.getInstance().regWedding(player.getObjectId(), wed);

            //wed.broadcastHtml("�� ����, ������. <br><br>(��� ���� ����� �������)");
            ThreadPoolManager.getInstance().scheduleAi(new Finish(), Config.L2JMOD_WEDDING_INTERVAL, false);
        } else if (command.equalsIgnoreCase("divorce")) {
            if (!player.isMarried() || player.getPartnerId() == 0) {
                player.sendHtmlMessage("�� �� �������� � �����.");
                return;
            }

            L2ItemInstance coin = player.getInventory().getItemByItemId(Config.L2JMOD_WEDDING_DIVORCE_COIN);
            if (coin == null || coin.getCount() < Config.L2JMOD_WEDDING_DIVORCE_PRICE) {
                player.sendHtmlMessage("��������� �������: " + Config.L2JMOD_WEDDING_DIVORCE_PRICE + " " + Config.L2JMOD_WEDDING_DIVORCE_COINNAME + ".");
                return;
            }

            if (!player.destroyItemByItemId("WEDDING", Config.L2JMOD_WEDDING_DIVORCE_COIN, Config.L2JMOD_WEDDING_DIVORCE_PRICE, player, true)) {
                player.sendHtmlMessage("��������� �������: " + Config.L2JMOD_WEDDING_DIVORCE_PRICE + " " + Config.L2JMOD_WEDDING_DIVORCE_COINNAME + ".");
                return;
            }

            CoupleManager.getInstance().deleteCouple(player.getCoupleId());
        }
    }

    private boolean isWearedFormal(L2PcInstance player) {
        if (player == null) {
            return false;
        }

        if (!Config.L2JMOD_WEDDING_FORMALWEAR) {
            return true;
        }

        return hasFormalWear(player.getInventory().getItemByItemId(6408));
    }

    private boolean hasFormalWear(L2ItemInstance formal) {
        if (formal == null) {
            return false;
        }

        return formal.isEquipped();
    }

    public void finish(boolean no) {
        if (wed != null) {
            if (no && !wed.married) {
                wed.broadcastHtml("������� ��������.");
                sayString("������� ��������!!!", 18);
                if (Config.L2JMOD_WEDDING_COIN > 0) {
                    L2PcInstance groom = wed.getGroom();
                    if (groom != null) {
                        giveItem(groom, Config.L2JMOD_WEDDING_COIN, Config.L2JMOD_WEDDING_PRICE);
                    }
                }
            }
            wed.clear();
        }
        wed = null;
        _active = false;
        sayString("����� ��������� ���� ������� �� ���.", 18);
    }

    private class Finish implements Runnable {

        Finish() {
        }

        public void run() {
            finish(true);
        }
    }
}
