package ru.agecold.gameserver.network.clientpackets;

import java.util.logging.Logger;

import javolution.text.TextBuilder;

import ru.agecold.Config;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.model.ClanWarehouse;
import ru.agecold.gameserver.model.ItemContainer;
import ru.agecold.gameserver.model.L2Clan;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.L2World;
import ru.agecold.gameserver.model.actor.instance.L2FolkInstance;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.util.Log;
import ru.agecold.util.Rnd;

public final class SendWareHouseWithDrawList extends L2GameClientPacket {

    private static Logger _log = Logger.getLogger(SendWareHouseWithDrawList.class.getName());
    private int _count;
    private int[] _items;
    private int[] counts;

    @Override
    protected void readImpl() {
        if (!Config.ALLOW_WAREHOUSE) {
            _items = null;
            return;
        }
        _count = readD();
        if (_count * 8 > _buf.remaining() || _count > Short.MAX_VALUE || _count <= 0) {
            _items = null;
            return;
        }
        _items = new int[_count * 2];
        counts = new int[_count];
        for (int i = 0; i < _count; i++) {
            _items[i * 2 + 0] = readD(); //item object id
            _items[i * 2 + 1] = readD(); //count
            if (_items[i * 2 + 1] <= 0) {
                _items = null;
                break;
            }
        }
    }

    @Override
    protected void runImpl() {
        if (!Config.ALLOW_WAREHOUSE
                || _items == null) {
            return;
        }

        L2PcInstance player = getClient().getActiveChar();
        if (player == null) {
            return;
        }

        if (System.currentTimeMillis() - player.gCPAZ() < 100) {
            return;
        }

        player.sCPAZ();

        ItemContainer warehouse = player.getActiveWarehouse();
        if (warehouse == null) {
            return;
        }

        L2FolkInstance manager = findFolkTarget(player.getTarget());
        if (manager == null
                || manager.getNpcId() != warehouse.getWhId()
                || !player.isInsideRadius(manager, L2NpcInstance.INTERACTION_DISTANCE, false, false)) {
            sendPacket(player.getFreightTarget() == 0 ? Static.WAREHOUSE_TOO_FAR : Static.PACKAGE_SEND_ERROR_TOO_FAR);
            return;
        }

        // Alt game - Karma punishment
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE
                && player.getKarma() > 0) {
            sendPacket(Static.NO_KARRMA_TELE);
            return;
        }

        if (Config.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH) {
            if (warehouse instanceof ClanWarehouse
                    && ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE)
                    != L2Clan.CP_CL_VIEW_WAREHOUSE)) {
                return;
            }
        } else {
            if (warehouse instanceof ClanWarehouse && !player.isClanLeader()) {
                // this msg is for depositing but maybe good to send some msg?
                player.sendPacket(Static.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE);
                return;
            }
        }

        if (warehouse instanceof ClanWarehouse) {
            if (warehouse.getOwnerId() != player.getClan().getClanId()) {
                String[] noway = {"���������! ���������� �������� ����� ��������!", "�� �������, ��� �� ��������? � ��� �� �������� ��� ����� �����. ��������, ��� �������� ������.", "���, � ������� ����� �������? ����� ������ ��� �� ���� ���, ����� ����� ����-������ � ������ �����!"};
                player.sendHtmlMessage("<font color=\"009900\">Warehouse Keeper " + manager.getName() + "</font>", noway[Rnd.get((noway.length - 1))]);
                return;
            }
        }

        int weight = 0;
        //int finalCount = player.getInventory().getSize();
        L2ItemInstance[] olditems = new L2ItemInstance[_count];

        for (int i = 0; i < _count; i++) {
            int itemObjId = _items[i * 2 + 0];
            int count = _items[i * 2 + 1];
            L2ItemInstance oldinst = L2ItemInstance.restoreFromDb(itemObjId);

            if (count <= 0) {
                player.sendPacket(Static.WRONG_COUNT);
                return;
            }

            if (oldinst == null) {
                player.sendPacket(Static.ITEM_NOT_FOUND);
                for (int f = 0; f < i; f++) {
                    L2World.getInstance().removeObject(olditems[i]); // FIXME don't sure...
                }
                return;
            }

            if (oldinst.getCount() < count) {
                count = oldinst.getCount();
            }

            counts[i] = count;
            olditems[i] = oldinst;
            weight += oldinst.getItem().getWeight() * count;
            //finalCount++;

            if (oldinst.isShadowItem()) {
                oldinst.setOwnerId(player.getObjectId());
            }

            /*if (oldinst.getItem().isStackable() && player.getInventory().getItemByItemId(oldinst.getItemId()) != null) {
             finalCount--;
             }*/
        }

        String date = "";
        TextBuilder tb = null;
        boolean logCWH = warehouse.getWarehouseType() == 2/* && Config.LOG_CLAN_WH*/;
        if (Config.LOG_ITEMS || logCWH) {
            date = Log.getTime();
            tb = new TextBuilder();
        }
        for (int i = 0; i < olditems.length; i++) {
            //System.out.println("item: " + olditems[i].getObjectId() + "count: " + counts[i]);
            L2ItemInstance TransferItem = warehouse.getItemByObj(olditems[i].getObjectId(), counts[i], player);
            if (TransferItem == null) {
                _log.warning("Error getItem from warhouse player: " + player.getName());
                continue;
            }
            L2ItemInstance item = player.getInventory().addItem("WH finish", TransferItem, player, player.getLastFolkNPC());
            item.setLocation(L2ItemInstance.ItemLocation.INVENTORY);
            if (!item.isStackable()) {
                L2World.getInstance().updateObject(item);
            }
            if ((Config.LOG_ITEMS || logCWH) && item != null) {
                String act = "WITHDRAW " + item.getItemName() + "(" + counts[i] + ")(+" + item.getEnchantLevel() + ")(" + item.getObjectId() + ")(npc:" + manager.getTemplate().npcId + ")#(Clan: " + player.getClanName() + "," + player.getFingerPrints() + ")";
                tb.append(date + act + "\n");
            }
        }
        if ((Config.LOG_ITEMS || logCWH) && tb != null) {
            if (Config.LOG_ITEMS) {
                Log.item(tb.toString(), Log.WAREHOUSE);
            }
            if (logCWH) {
                Log.add(tb.toString(), "items/clan_warehouse");
            }
            tb.clear();
            tb = null;
        }

        player.sendItems(true);
        player.sendChanges();
    }

    private L2FolkInstance findFolkTarget(L2Object target) {
        if (target == null
                || !target.isL2Folk()) {
            return null;
        }

        return (L2FolkInstance) target;
    }
}
