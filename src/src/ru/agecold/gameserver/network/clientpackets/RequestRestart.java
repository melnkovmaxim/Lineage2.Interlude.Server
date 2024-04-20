package ru.agecold.gameserver.network.clientpackets;

import ru.agecold.gameserver.LoginServerThread;
import ru.agecold.gameserver.SevenSignsFestival;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.datatables.SkillTable;
import ru.agecold.gameserver.instancemanager.EventManager;
import ru.agecold.gameserver.model.L2Party;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.entity.TvTEvent;
import ru.agecold.gameserver.network.L2GameClient;
import ru.agecold.gameserver.network.L2GameClient.GameClientState;
import ru.agecold.gameserver.network.serverpackets.CharSelectInfo;
import ru.agecold.gameserver.network.serverpackets.RestartResponse;
import ru.agecold.gameserver.network.serverpackets.SystemMessage;
import ru.agecold.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * This class ...
 *
 * @version $Revision: 1.11.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestRestart extends L2GameClientPacket {

    @Override
    protected void readImpl() {
        // trigger
    }

    @Override
    protected void runImpl() {
        L2PcInstance player = getClient().getActiveChar();
        if (player == null) {
            return;
        }

        if (System.currentTimeMillis() - player.gCPAV() < 100) {
            return;
        }

        player.sCPAV();

        if (player.isInOlympiadMode()) {
            player.sendPacket(Static.CANT_LOGOUT_OLY);
            return;
        }

        if (player.isInNoLogoutArea() || player.hasLogoutPenalty()) {
            player.sendPacket(Static.CANT_LOGOUT_ZONE);
            return;
        }

        if (player.isTeleporting()) {
            player.abortCast();
            player.setIsTeleporting(false);
        }

        player.getInventory().updateDatabase();

        if (player.getPrivateStoreType() != 0) {
            if (player.isInOfflineMode()) {
                player.closeNetConnection();
            } else {
                player.sendPacket(Static.CANT_LOGOUT_TRADE);
            }
            return;
        }

        if (player.getActiveTradeList() != null) {
            //player.getTransactionRequester().onTradeCancel(player);
            //player.onTradeCancel(player.getTransactionRequester());
            player.cancelActiveTrade();
            if (player.getTransactionRequester() != null) {
                player.getTransactionRequester().setTransactionRequester(null);
            }
            player.setTransactionRequester(null);
        }

        if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player)) {
            player.sendPacket(Static.CANT_RESTART_WHILE_FIGHTING);
            player.sendActionFailed();
            return;
        }

        if (player.isInDuel()) {
            player.sendUserPacket(Static.CANT_IN_DUEL);
            player.sendActionFailed();
            return;
        }

        // Prevent player from restarting if they are a festival participant
        // and it is in progress, otherwise notify party members that the player
        // is not longer a participant.
        if (player.isFestivalParticipant()) {
            if (SevenSignsFestival.getInstance().isFestivalInitialized()) {
                player.sendPacket(SystemMessage.sendString("You cannot restart while you are a participant in a festival."));
                player.sendActionFailed();
                return;
            }

            if (player.getParty() != null) {
                player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
            }
        }
        if (player.isFlying()) {
            player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
        }

        EventManager.onExit(player);

        L2GameClient client = getClient();
        //����2
        LoginServerThread.putToLogoutRoom(client.getAccountName(), client);
        // detach the client from the char so that the connection isnt closed in the deleteMe
        player.setClient(null);

        //RegionBBSManager.getInstance().changeCommunityBoard();

        // removing player from the world
        player.deleteMe();
        L2GameClient.saveCharToDisk(client.getActiveChar());

        getClient().setActiveChar(null);

        // return the client to the authed status
        client.setState(GameClientState.AUTHED);

        sendPacket(new RestartResponse());
        // send char list
        CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
        sendPacket(cl);
        client.setCharSelection(cl.getCharInfo());
    }

    /*
     * (non-Javadoc) @see
     * ru.agecold.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType() {
        return "C.Restart";
    }
}