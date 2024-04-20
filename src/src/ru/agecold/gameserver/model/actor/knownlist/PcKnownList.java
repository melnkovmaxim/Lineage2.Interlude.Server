/* This program is free software; you can redistribute it and/or modify
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
package ru.agecold.gameserver.model.actor.knownlist;

import ru.agecold.Config;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.L2Summon;
import ru.agecold.gameserver.model.actor.instance.L2BoatInstance;
import ru.agecold.gameserver.model.actor.instance.L2DoorInstance;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PetInstance;
import ru.agecold.gameserver.model.actor.instance.L2StaticObjectInstance;
import ru.agecold.gameserver.network.serverpackets.CharInfo;
import ru.agecold.gameserver.network.serverpackets.DeleteObject;
import ru.agecold.gameserver.network.serverpackets.DoorInfo;
import ru.agecold.gameserver.network.serverpackets.DoorStatusUpdate;
import ru.agecold.gameserver.network.serverpackets.DropItem;
import ru.agecold.gameserver.network.serverpackets.GetOnVehicle;
import ru.agecold.gameserver.network.serverpackets.NpcInfo;
import ru.agecold.gameserver.network.serverpackets.PetInfo;
import ru.agecold.gameserver.network.serverpackets.PetItemList;
import ru.agecold.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import ru.agecold.gameserver.network.serverpackets.PrivateStoreMsgSell;
import ru.agecold.gameserver.network.serverpackets.RecipeShopMsg;
import ru.agecold.gameserver.network.serverpackets.RelationChanged;
import ru.agecold.gameserver.network.serverpackets.ServerObjectInfo;
import ru.agecold.gameserver.network.serverpackets.SpawnItem;
import ru.agecold.gameserver.network.serverpackets.SpawnItemPoly;
import ru.agecold.gameserver.network.serverpackets.StaticObject;
import ru.agecold.gameserver.network.serverpackets.VehicleInfo;

public class PcKnownList extends PlayableKnownList {
    // =========================================================
    // Data Field

    L2PcInstance activeChar;

    // =========================================================
    // Constructor
    public PcKnownList(L2PcInstance activeChar) {
        super(activeChar);
        this.activeChar = activeChar;
    }

    // =========================================================
    // Method - Public
    /**
     * Add a visible L2Object to L2PcInstance _knownObjects and _knownPlayer (if necessary) and send Server-Client Packets needed to inform the L2PcInstance of its state and actions in progress.<BR><BR>
     *
     * <B><U> object is a L2ItemInstance </U> :</B><BR><BR>
     * <li> Send Server-Client Packet DropItem/SpawnItem to the L2PcInstance </li><BR><BR>
     *
     * <B><U> object is a L2DoorInstance </U> :</B><BR><BR>
     * <li> Send Server-Client Packets DoorInfo and DoorStatusUpdate to the L2PcInstance </li>
     * <li> Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
     *
     * <B><U> object is a L2NpcInstance </U> :</B><BR><BR>
     * <li> Send Server-Client Packet NpcInfo to the L2PcInstance </li>
     * <li> Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
     *
     * <B><U> object is a L2Summon </U> :</B><BR><BR>
     * <li> Send Server-Client Packet NpcInfo/PetItemList (if the L2PcInstance is the owner) to the L2PcInstance </li>
     * <li> Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
     *
     * <B><U> object is a L2PcInstance </U> :</B><BR><BR>
     * <li> Send Server-Client Packet CharInfo to the L2PcInstance </li>
     * <li> If the object has a private store, Send Server-Client Packet PrivateStoreMsgSell to the L2PcInstance </li>
     * <li> Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
     *
     * @param object The L2Object to add to _knownObjects and _knownPlayer
     * @param dropper The L2Character who dropped the L2Object
     */
    @Override
    public boolean addKnownObject(L2Object object) {
        return addKnownObject(object, null);
    }

    @Override
    public boolean addKnownObject(L2Object object, L2Character dropper) {
        if (!super.addKnownObject(object, dropper)) {
            return false;
        }

        if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item")) {
            //if (object.getPolytype().equals("item"))
            activeChar.sendPacket(new SpawnItemPoly(object));
            //else if (object.getPolytype().equals("npc"))
            //    sendPacket(new NpcInfoPoly(object, this));

        } else {
            if (object.isL2Item()) {
                activeChar.sendPacket(new SpawnItem((L2ItemInstance) object));
            } else if (object.isL2Door()) {
                L2DoorInstance door = (L2DoorInstance) object;

                activeChar.sendPacket(new DoorInfo(door));
                activeChar.sendPacket(new DoorStatusUpdate(door, door.isEnemyOf(activeChar)));
                //activeChar.sendPacket(new StaticObject((L2DoorInstance) object,false));
            } else if (object instanceof L2BoatInstance) {
                if (!activeChar.isInBoat()) {
                    if (object != activeChar.getBoat()) {
                        activeChar.sendPacket(new VehicleInfo((L2BoatInstance) object));
                        ((L2BoatInstance) object).sendVehicleDeparture(getActiveChar());
                    }
                }
            } else if (object instanceof L2StaticObjectInstance) {
                activeChar.sendPacket(new StaticObject((L2StaticObjectInstance) object));
            } else if (object.isL2Npc()) {
                if (Config.CHECK_KNOWN) {
                    activeChar.sendMessage("Added NPC: " + ((L2NpcInstance) object).getName());
                }

                if (((L2NpcInstance) object).getRunSpeed() == 0) {
                    activeChar.sendPacket(new ServerObjectInfo((L2NpcInstance) object, getActiveChar()));
                } else {
                    activeChar.sendPacket(new NpcInfo((L2NpcInstance) object, getActiveChar()));
                }
            } else if (object.isL2Summon()) {
                L2Summon summon = (L2Summon) object;

                // Check if the L2PcInstance is the owner of the Pet
                if (activeChar.equals(summon.getOwner()) && !summon.isAgathion()) {
                    activeChar.sendPacket(new PetInfo(summon, 0));
                    // The PetInfo packet wipes the PartySpelled (list of active  spells' icons).  Re-add them
                    summon.updateEffectIcons(true);
                    if (summon.isPet()) {
                        activeChar.sendPacket(new PetItemList((L2PetInstance) summon));
                    }
                } else {
                    activeChar.sendPacket(new NpcInfo(summon, getActiveChar()));
                }
            } else if (object.isPlayer()) {
                L2PcInstance otherPlayer = object.getPlayer();
                if (otherPlayer.isInBoat()) {
                    otherPlayer.getPosition().setWorldPosition(otherPlayer.getBoat().getPosition().getWorldPosition());
                    activeChar.sendPacket(new CharInfo(otherPlayer));
                    int relation = otherPlayer.getRelation(getActiveChar());
                    if (otherPlayer.getKnownList().updateRelationsFor(activeChar.getObjectId(), relation)) {
                        activeChar.sendPacket(new RelationChanged(otherPlayer, relation, activeChar.isAutoAttackable(otherPlayer)));
                    }
                    activeChar.sendPacket(new GetOnVehicle(otherPlayer, otherPlayer.getBoat(), otherPlayer.getInBoatPosition().getX(), otherPlayer.getInBoatPosition().getY(), otherPlayer.getInBoatPosition().getZ()));
                    /*if(otherPlayer.getBoat().GetVehicleDeparture() == null)
                    {
                    
                    int xboat = otherPlayer.getBoat().getX();
                    int yboat= otherPlayer.getBoat().getY();
                    double modifier = Math.PI/2;
                    if (yboat == 0)
                    {
                    yboat = 1;
                    }
                    if(yboat < 0)
                    {
                    modifier = -modifier;
                    }
                    double angleboat = modifier - Math.atan(xboat/yboat);
                    int xp = otherPlayer.getX();
                    int yp = otherPlayer.getY();
                    modifier = Math.PI/2;
                    if (yp == 0)
                    {
                    yboat = 1;
                    }
                    if(yboat < 0)
                    {
                    modifier = -modifier;
                    }
                    double anglep = modifier - Math.atan(yp/xp);
                    
                    double finx = Math.cos(anglep - angleboat)*Math.sqrt(xp *xp +yp*yp ) + Math.cos(angleboat)*Math.sqrt(xboat *xboat +yboat*yboat );
                    double finy = Math.sin(anglep - angleboat)*Math.sqrt(xp *xp +yp*yp ) + Math.sin(angleboat)*Math.sqrt(xboat *xboat +yboat*yboat );
                    //otherPlayer.getPosition().setWorldPosition(otherPlayer.getBoat().getX() - otherPlayer.getInBoatPosition().x,otherPlayer.getBoat().getY() - otherPlayer.getInBoatPosition().y,otherPlayer.getBoat().getZ()- otherPlayer.getInBoatPosition().z);
                    otherPlayer.getPosition().setWorldPosition((int)finx,(int)finy,otherPlayer.getBoat().getZ()- otherPlayer.getInBoatPosition().z);
                    
                    }*/
                } else {
                    activeChar.sendPacket(new CharInfo(otherPlayer));
                    int relation = otherPlayer.getRelation(getActiveChar());
                    if (otherPlayer.getKnownList().updateRelationsFor(activeChar.getObjectId(), relation)) {
                        activeChar.sendPacket(new RelationChanged(otherPlayer, relation, activeChar.isAutoAttackable(otherPlayer)));
                    }
                }

                if (otherPlayer.getPrivateStoreType() == L2PcInstance.PS_SELL) {
                    activeChar.sendPacket(new PrivateStoreMsgSell(otherPlayer));
                } else if (otherPlayer.getPrivateStoreType() == L2PcInstance.PS_BUY) {
                    activeChar.sendPacket(new PrivateStoreMsgBuy(otherPlayer));
                } else if (otherPlayer.getPrivateStoreType() == L2PcInstance.PS_MANUFACTURE) {
                    activeChar.sendPacket(new RecipeShopMsg(otherPlayer));
                }
            }

            if (object.isL2Character()) {
                // Update the state of the L2Character object client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance
                L2Character obj = (L2Character) object;
                obj.getAI().describeStateToPlayer(getActiveChar());
            }
        }

        return true;
    }

    /**
     * Remove a L2Object from L2PcInstance _knownObjects and _knownPlayer (if necessary) and send Server-Client Packet DeleteObject to the L2PcInstance.<BR><BR>
     *
     * @param object The L2Object to remove from _knownObjects and _knownPlayer
     *
     */
    @Override
    public boolean removeKnownObject(L2Object object) {
        if (!super.removeKnownObject(object)) {
            return false;
        }
        // Send Server-Client Packet DeleteObject to the L2PcInstance
        activeChar.sendPacket(new DeleteObject(object));
        if (Config.CHECK_KNOWN && object.isL2Npc()) {
            activeChar.sendMessage("Removed NPC: " + ((L2NpcInstance) object).getName());
        }
        return true;
    }

    // =========================================================
    // Method - Private
    // =========================================================
    // Property - Public
    @Override
    public final L2PcInstance getActiveChar() {
        return activeChar;
    }

    @Override
    public int getDistanceToForgetObject(L2Object object) {
        // when knownlist grows, the distance to forget should be at least
        // the same as the previous watch range, or it becomes possible that
        // extra charinfo packets are being sent (watch-forget-watch-forget)
        int knownlistSize = getKnownObjects().size();
        if (knownlistSize <= 25) {
            return 4200;
        }
        if (knownlistSize <= 35) {
            return 3600;
        }
        if (knownlistSize <= 70) {
            return 2910;
        } else {
            return 2310;
        }
    }

    @Override
    public int getDistanceToWatchObject(L2Object object) {
        int knownlistSize = getKnownObjects().size();

        if (knownlistSize <= 25) {
            return 3500; // empty field
        }
        if (knownlistSize <= 35) {
            return 2900;
        }
        if (knownlistSize <= 70) {
            return 2300;
        } else {
            return 1700; // Siege, TOI, city
        }
    }
}
