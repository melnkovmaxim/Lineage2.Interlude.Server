/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package ru.agecold.gameserver.model.actor.instance;

//import java.util.Map;
import ru.agecold.Config;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.model.L2Clan;
import ru.agecold.gameserver.model.PcFreight;
import ru.agecold.gameserver.network.serverpackets.EnchantResult;
import ru.agecold.gameserver.network.serverpackets.PackageToList;
import ru.agecold.gameserver.network.serverpackets.WareHouseDepositList;
import ru.agecold.gameserver.network.serverpackets.WareHouseWithdrawalList;
import ru.agecold.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.10 $ $Date: 2005/04/06 16:13:41 $
 */
public final class L2WarehouseInstance extends L2FolkInstance {
    //private static Logger _log = Logger.getLogger(L2WarehouseInstance.class.getName());

    /**
     * @param template
     */
    public L2WarehouseInstance(int objectId, L2NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(L2PcInstance player, String command) {
        // lil check to prevent enchant exploit
        if (player.getActiveEnchantItem() != null) {
            //_log.info("Player "+player.getName()+" trying to use enchant exploit, ban this player!");
            //player.closeNetConnection();
            player.setActiveEnchantItem(null);
            player.sendPacket(new EnchantResult(0, true));
            //player.sendActionFailed();
            // return;
        }

        if (player.getActiveTradeList() != null) {
            player.cancelActiveTrade();
            //activeChar.sendActionFailed();
            //return;
        }

        if (isOutOfService(command)) {
            player.sendPacket(Static.DISABLED);
            player.sendActionFailed();
            return;
        }

        // Alt game - Karma punishment
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE
                && player.getKarma() > 0) {
            player.sendPacket(Static.NO_KARRMA_TELE);
            return;
        }

        player.setFreightTarget(0);

        if (command.equals("DepositP")) {
            showDepositWindow(player);
        } else if (command.startsWith("WithdrawP")) {
            showRetrieveWindow(player);
        } else if (command.equals("DepositC")) {
            if (player.getClan() == null) {
                player.sendHtmlMessage("<font color=\"009900\">Warehouse Keeper " + getName() + "</font>", "������ �� ������ � ���� ������������.");
            } else {
                showDepositWindowClan(player);
            }
        } else if (command.equals("WithdrawC")) {
            if (player.getClan() == null) {
                player.sendHtmlMessage("<font color=\"009900\">Warehouse Keeper " + getName() + "</font>", "���, � ������� ����� �������? ����� ������ ��� �� ���� ���, ����� ����� ����-������ � ������ �����!");
            } else {
                showWithdrawWindowClan(player);
            }
        } else if (command.startsWith("WithdrawF")) {
            showWithdrawWindowFreight(player);
        } else if (command.startsWith("DepositF")) {
            showDepositWindowFreight(player);
        } else {
            super.onBypassFeedback(player, command);
        }

        player.sendActionFailed();
    }

    private boolean isOutOfService(String command) {
        if (command.equals("DepositP") || command.startsWith("WithdrawP") || command.equals("DepositC") || command.equals("WithdrawC")) {
            return !Config.ALLOW_WAREHOUSE;
        }

        if (command.startsWith("WithdrawF") || command.startsWith("DepositF") || command.startsWith("FreightChar")) {
            return !Config.ALLOW_FREIGHT;
        }
        return false;
    }

    @Override
    public String getHtmlPath(int npcId, int val) {
        String pom = "";
        if (val == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + val;
        }
        return "data/html/warehouse/" + pom + ".htm";
    }

    private void showRetrieveWindow(L2PcInstance player) {
        player.setActiveWarehouse(player.getWarehouse(), getNpcId());
        player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
    }

    private void showDepositWindow(L2PcInstance player) {
        player.tempInvetoryDisable();
        player.setActiveWarehouse(player.getWarehouse(), getNpcId());
        player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.PRIVATE));
    }

    private void showDepositWindowFreight(L2PcInstance player) {
        player.sendPacket(new PackageToList(player));
    }

    private void showDepositWindowClan(L2PcInstance player) {
        if (player.getClan() != null) {
            if (player.getClan().getLevel() == 0) {
                player.sendPacket(Static.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
            } else {
                player.setActiveWarehouse(player.getClan().getWarehouse(), getNpcId());
                player.tempInvetoryDisable();
                WareHouseDepositList dl = new WareHouseDepositList(player, WareHouseDepositList.CLAN);
                player.sendPacket(dl);
            }
        }
    }

    private void showWithdrawWindowClan(L2PcInstance player) {
        if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE) {
            player.sendPacket(Static.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
        } else {
            if (player.getClan().getLevel() == 0) {
                player.sendPacket(Static.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
            } else {
                player.setActiveWarehouse(player.getClan().getWarehouse(), getNpcId());
                player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
            }
        }
    }

    private void showWithdrawWindowFreight(L2PcInstance player) {
        PcFreight freight = player.getFreight();
        if (freight == null) {
            player.sendPacket(Static.NO_ITEM_DEPOSITED_IN_WH);
            return;
        }
        if (freight.getSize() > 0) {
            if (Config.ALT_GAME_FREIGHTS) {
                freight.setActiveLocation(0);
            } else {
                freight.setActiveLocation(getWorldRegion().hashCode());
            }
            player.setActiveWarehouse(freight, getNpcId());
            player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.FREIGHT));
        } else {
            player.sendPacket(Static.NO_ITEM_DEPOSITED_IN_WH);
        }
    }

}
