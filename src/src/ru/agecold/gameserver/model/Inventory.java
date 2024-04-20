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
package ru.agecold.gameserver.model;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import ru.agecold.Config;
import ru.agecold.L2DatabaseFactory;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.datatables.ArmorSetsTable;
import ru.agecold.gameserver.datatables.CustomServerData;
import ru.agecold.gameserver.datatables.ItemTable;
import ru.agecold.gameserver.datatables.SkillTable;
import ru.agecold.gameserver.instancemanager.CastleManager;
import ru.agecold.gameserver.model.L2ItemInstance.ItemLocation;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PlayableInstance;
import ru.agecold.gameserver.templates.L2Armor;
import ru.agecold.gameserver.templates.L2EtcItem;
import ru.agecold.gameserver.templates.L2EtcItemType;
import ru.agecold.gameserver.templates.L2Item;
import ru.agecold.gameserver.templates.L2Weapon;
import ru.agecold.gameserver.templates.L2WeaponType;
import ru.agecold.mysql.Close;
import ru.agecold.mysql.Connect;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * This class manages inventory
 *
 * @version $Revision: 1.13.2.9.2.12 $ $Date: 2005/03/29 23:15:15 $ rewritten
 * 23.2.2006 by Advi
 */
public abstract class Inventory extends ItemContainer {
    //protected static final Logger _log = Logger.getLogger(Inventory.class.getName());

    public interface PaperdollListener {

        public void notifyEquiped(int slot, L2ItemInstance inst);

        public void notifyUnequiped(int slot, L2ItemInstance inst);
    }

    public interface OnDisplayListener {

        Integer onDisplay(int slot, L2ItemInstance item, L2PlayableInstance playable);
    }
    public static final int PAPERDOLL_UNDER = 0;
    public static final int PAPERDOLL_LEAR = 1;
    public static final int PAPERDOLL_REAR = 2;
    public static final int PAPERDOLL_NECK = 3;
    public static final int PAPERDOLL_LFINGER = 4;
    public static final int PAPERDOLL_RFINGER = 5;
    public static final int PAPERDOLL_HEAD = 6;
    public static final int PAPERDOLL_RHAND = 7;
    public static final int PAPERDOLL_LHAND = 8;
    public static final int PAPERDOLL_GLOVES = 9;
    public static final int PAPERDOLL_CHEST = 10;
    public static final int PAPERDOLL_LEGS = 11;
    public static final int PAPERDOLL_FEET = 12;
    public static final int PAPERDOLL_BACK = 13;
    public static final int PAPERDOLL_LRHAND = 14;
    public static final int PAPERDOLL_FACE = 15;
    public static final int PAPERDOLL_HAIR = 16;
    public static final int PAPERDOLL_DHAIR = 17;
    //Speed percentage mods
    public static final double MAX_ARMOR_WEIGHT = 12000;
    private final L2ItemInstance[] _paperdoll;
    private final List<PaperdollListener> _paperdollListeners;
    private OnDisplayListener _onDisplayListener;
    // protected to be accessed from child classes only
    protected int _totalWeight;
    // used to quickly check for using of items of special type
    private int _wearedMask;

    final class FormalWearListener implements PaperdollListener {

        public void notifyUnequiped(int slot, L2ItemInstance item) {
            if (!(getOwner() != null
                    && getOwner().isPlayer())) {
                return;
            }

            if (item.getItemId() == 6408) {
                getOwner().getPlayer().setIsWearingFormalWear(false);
            }
        }

        public void notifyEquiped(int slot, L2ItemInstance item) {
            if (!(getOwner() != null
                    && getOwner().isPlayer())) {
                return;
            }

            // If player equip Formal Wear unequip weapons and abort cast/attack
            if (item.getItemId() == 6408) {
                getOwner().getPlayer().setIsWearingFormalWear(true);
            } else {
                if (!getOwner().getPlayer().isWearingFormalWear()) {
                    return;
                }
            }
        }
    }

    /**
     * Recorder of alterations in inventory
     */
    public static final class ChangeRecorder implements PaperdollListener {

        private final Inventory _inventory;
        private final List<L2ItemInstance> _changed;

        /**
         * Constructor of the ChangeRecorder
         *
         * @param inventory
         */
        ChangeRecorder(Inventory inventory) {
            _inventory = inventory;
            _changed = new FastList<L2ItemInstance>();
            _inventory.addPaperdollListener(this);
        }

        /**
         * Add alteration in inventory when item equiped
         */
        public void notifyEquiped(int slot, L2ItemInstance item) {
            if (!_changed.contains(item)) {
                _changed.add(item);
            }
        }

        /**
         * Add alteration in inventory when item unequiped
         */
        public void notifyUnequiped(int slot, L2ItemInstance item) {
            if (!_changed.contains(item)) {
                _changed.add(item);
            }
        }

        /**
         * Returns alterations in inventory
         *
         * @return L2ItemInstance[] : array of alterated items
         */
        public L2ItemInstance[] getChangedItems() {
            return _changed.toArray(new L2ItemInstance[_changed.size()]);
        }
    }

    final class BowListener implements PaperdollListener {

        public void notifyUnequiped(int slot, L2ItemInstance item) {
            if (slot != PAPERDOLL_LRHAND) {
                return;
            }
            if (Config.ASSERT) {
                assert null == getPaperdollItem(PAPERDOLL_LRHAND);
            }
            if (item.getItemType() == L2WeaponType.BOW) {
                L2ItemInstance arrow = getPaperdollItem(PAPERDOLL_LHAND);
                if (arrow != null) {
                    setPaperdollItem(PAPERDOLL_LHAND, null);
                }
            }
        }

        public void notifyEquiped(int slot, L2ItemInstance item) {
            if (slot != PAPERDOLL_LRHAND) {
                return;
            }
            if (Config.ASSERT) {
                assert item == getPaperdollItem(PAPERDOLL_LRHAND);
            }
            if (item.getItemType() == L2WeaponType.BOW) {
                L2ItemInstance arrow = findArrowForBow(item.getItem());
                if (arrow != null) {
                    setPaperdollItem(PAPERDOLL_LHAND, arrow);
                }
            }
        }
    }

    final class StatsListener implements PaperdollListener {

        public void notifyUnequiped(int slot, L2ItemInstance item) {
            if (slot == PAPERDOLL_LRHAND) {
                return;
            }
            getOwner().removeStatsOwner(item);
        }

        public void notifyEquiped(int slot, L2ItemInstance item) {
            if (!item.checkForEquipped(getOwner().getPlayer())) {
                return;
            }

            if (slot == PAPERDOLL_LRHAND) {
                return;
            }

            getOwner().addStatFuncs(item.getStatFuncs(getOwner()));
        }
    }

    final class ItemPassiveSkillsListener implements PaperdollListener {

        public void notifyUnequiped(int slot, L2ItemInstance item) {
            L2PcInstance player;

            if (getOwner().isPlayer()) {
                player = getOwner().getPlayer();
            } else {
                return;
            }

            boolean send = false;
            L2Skill passiveSkill = null;
            L2Skill enchant4Skill = null;

            L2Item it = item.getItem();

            if (it instanceof L2Weapon) {
                // Remove augementation boni on unequip
                if (item.isAugmented()) {
                    item.getAugmentation().removeBoni(player);
                }

                passiveSkill = ((L2Weapon) it).getSkill();
                enchant4Skill = ((L2Weapon) it).getEnchant4Skill();
            } else if (it instanceof L2Armor) {
                passiveSkill = ((L2Armor) it).getSkill();
                // Remove augementation boni on unequip
                if (item.isAugmented()) {
                    item.getAugmentation().removeBoni(player);
                }

                if (Config.TATOO_SKILLS) {
                    L2Skill customSkill = it.getCustomSkill();
                    if (customSkill != null) {
                        send = true;
                        player.removeSkill(customSkill, false);
                        player.stopSkillEffects(customSkill.getId());

                        L2Summon pet = player.getPet();
                        if (pet != null && pet.isSummon()) {
                            pet.unSummon(player);
                        }
                    }
                }
            }

            if (passiveSkill != null) {
                send = true;
                player.removeSkill(passiveSkill, false);
            }
            if (enchant4Skill != null) {
                send = true;
                player.removeSkill(enchant4Skill, false);
            }

            if (send) {
                player.sendSkillList();
            }
        }

        public void notifyEquiped(int slot, L2ItemInstance item) {
            L2PcInstance player;

            if (getOwner().isPlayer()) {
                player = getOwner().getPlayer();
            } else {
                return;
            }

            boolean send = false;
            L2Skill passiveSkill = null;
            L2Skill enchant4Skill = null;

            L2Item it = item.getItem();
            //System.out.println("####1#");

            if (it instanceof L2Weapon) {
                // Apply augementation boni on equip
                if (item.isAugmented() && getOwner().isPlayer()) {
                    if (player.isInOlympiadMode() && !Config.ALT_ALLOW_AUGMENT_ON_OLYMP) {
                        item.getAugmentation().removeBoni(getOwner().getPlayer());
                    } else {
                        item.getAugmentation().applyBoni(getOwner().getPlayer());
                    }
                }

                passiveSkill = ((L2Weapon) it).getSkill();
                if (item.getEnchantLevel() >= 4) {
                    enchant4Skill = ((L2Weapon) it).getEnchant4Skill();
                }
            } else if (it instanceof L2Armor) {
                //System.out.println("####2#");
                // Apply augementation boni on equip
                if (item.isAugmented() && getOwner().isPlayer()) {
                    if (player.isInOlympiadMode() && !Config.ALT_ALLOW_AUGMENT_ON_OLYMP) {
                        item.getAugmentation().removeBoni(getOwner().getPlayer());
                    } else {
                        item.getAugmentation().applyBoni(getOwner().getPlayer());
                    }
                }

                passiveSkill = ((L2Armor) it).getSkill();
                if (Config.TATOO_SKILLS) {
                    //System.out.println("####3#");
                    L2Skill customSkill = it.getCustomSkill();
                    if (customSkill != null) {
                        //System.out.println("####4#");
                        send = true;
                        player.addSkill(customSkill, false);
                    }
                }
            }
            //System.out.println("####5#");

            if (passiveSkill != null) {
                send = true;
                player.addSkill(passiveSkill, false);
            }
            if (enchant4Skill != null) {
                send = true;
                player.addSkill(enchant4Skill, false);
            }

            if (send) {
                //System.out.println("####6#");
                player.sendSkillList();
            }
        }
    }

    final class ArmorSetListener implements PaperdollListener {

        public void notifyEquiped(int slot, L2ItemInstance item) {
            if (!(getOwner().isPlayer())) {
                return;
            }

            // checks if player worns chest item
            L2ItemInstance chestItem = getPaperdollItem(PAPERDOLL_CHEST);
            if (chestItem == null) {
                return;
            }

            // checks if there is armorset for chest item that player worns
            L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(chestItem.getItemId());
            if (armorSet == null) {
                return;
            }

            boolean sendSkills = false;
            L2PcInstance player = getOwner().getPlayer();
            // checks if equiped item is part of set
            if (armorSet.containItem(slot, item.getItemId())) {
                if (armorSet.containAll(player, player.getInventory())) {
                    L2Skill skill = SkillTable.getInstance().getInfo(armorSet.getSkillId(), 1);
                    if (skill != null) {
                        sendSkills = true;
                        player.addSkill(skill, false);
                    } else {
                        _log.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getSkillId() + ".");
                    }

                    if (armorSet.containShield(player)) // has shield from set
                    {
                        L2Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
                        if (skills != null) {
                            sendSkills = true;
                            player.addSkill(skills, false);
                        } else {
                            _log.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + ".");
                        }
                    }
                    if (armorSet.isEnchanted6(player, player.getInventory())) // has all parts of set enchanted to 6 or more
                    {
                        int skillId = armorSet.getEnchant6skillId();
                        if (skillId > 0) {
                            L2Skill skille = SkillTable.getInstance().getInfo(skillId, 1);
                            if (skille != null) {
                                sendSkills = true;
                                player.addSkill(skille, false);
                            } else {
                                _log.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getEnchant6skillId() + ".");
                            }
                        }
                    }
                }
            } else if (armorSet.containShield(item.getItemId())) {
                if (armorSet.containAll(player, player.getInventory())) {
                    L2Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
                    if (skills != null) {
                        sendSkills = true;
                        player.addSkill(skills, false);
                    } else {
                        _log.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + ".");
                    }
                }
            }
            if (sendSkills) {
                player.sendSkillList();
            }
        }

        public void notifyUnequiped(int slot, L2ItemInstance item) {
            if (!(getOwner().isPlayer())) {
                return;
            }

            boolean remove = false;
            int removeSkillId1 = 0; // set skill
            int removeSkillId2 = 0; // shield skill
            int removeSkillId3 = 0; // enchant +6 skill

            if (slot == PAPERDOLL_CHEST) {
                L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(item.getItemId());
                if (armorSet == null) {
                    return;
                }

                remove = true;
                removeSkillId1 = armorSet.getSkillId();
                removeSkillId2 = armorSet.getShieldSkillId();
                removeSkillId3 = armorSet.getEnchant6skillId();
            } else {
                L2ItemInstance chestItem = getPaperdollItem(PAPERDOLL_CHEST);
                if (chestItem == null) {
                    return;
                }

                L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(chestItem.getItemId());
                if (armorSet == null) {
                    return;
                }

                if (armorSet.containItem(slot, item.getItemId())) // removed part of set
                {
                    remove = true;
                    removeSkillId1 = armorSet.getSkillId();
                    removeSkillId2 = armorSet.getShieldSkillId();
                    removeSkillId3 = armorSet.getEnchant6skillId();
                } else if (armorSet.containShield(item.getItemId())) // removed shield
                {
                    remove = true;
                    removeSkillId2 = armorSet.getShieldSkillId();
                }
            }

            L2PcInstance player = getOwner().getPlayer();
            if (remove) {
                if (removeSkillId1 != 0) {
                    L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId1, 1);
                    if (skill != null) {
                        player.removeSkill(skill);
                    } else {
                        _log.warning("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId1 + ".");
                    }
                }
                if (removeSkillId2 != 0) {
                    L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId2, 1);
                    if (skill != null) {
                        player.removeSkill(skill);
                    } else {
                        _log.warning("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId2 + ".");
                    }
                }
                if (removeSkillId3 != 0) {
                    L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId3, 1);
                    if (skill != null) {
                        player.removeSkill(skill);
                    } else {
                        _log.warning("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId3 + ".");
                    }
                }
                player.sendSkillList();
            }
        }
    }

    private static class ItemFakeAppearanceDisplayListener implements OnDisplayListener
    {
        private final Map<Integer, Integer> itemsMap;

        private ItemFakeAppearanceDisplayListener(Map<Integer, Integer> itemsMap)
        {
            this.itemsMap = itemsMap;
        }

        @Override
        public Integer onDisplay(int slot, L2ItemInstance item, L2PlayableInstance playable)
        {
            return itemsMap.get(slot);
        }
    }

    public static final class ItemFakeAppearanceEquipListener implements PaperdollListener
    {
        Inventory _inv;

        public ItemFakeAppearanceEquipListener(Inventory inv)
        {
            _inv = inv;
        }

        @Override
        public void notifyEquiped(int slot, L2ItemInstance item) {
            if(item == null)
                return;
            //if(_inv == null)
            //	return;
            if(_inv.getOwner() == null)
                return;
            if(!_inv.getOwner().isPlayer())
                return;
            setEquippedFakeItem(_inv.getOwner().getPlayer(), item);
        }

        @Override
        public void notifyUnequiped(int slot, L2ItemInstance item) {
            if(item == null)
                return;
            //if(_inv == null)
            //	return;
            if(_inv.getOwner() == null)
                return;
            if(!_inv.getOwner().isPlayer())
                return;
            if(!CustomServerData.getInstance().getFakeItems().containsKey(item.getItemId()))
                return;
            setUnequippedFakeItem(_inv.getOwner().getPlayer());
        }
    }

    public static boolean setEquippedFakeItem(L2PcInstance player, L2ItemInstance item)
    {
        Map<Integer, Integer> itemsMap = CustomServerData.getInstance().getFakeItems().get(item.getItemId());
        if(itemsMap == null || itemsMap.isEmpty())
            return false;
        player.getInventory().setOnDisplayListener(new ItemFakeAppearanceDisplayListener(itemsMap));
        return true;
    }

    private static boolean setUnequippedFakeItem(L2PcInstance player)
    {
        if(player.getInventory().getOnDisplayListener() == null)
            return false;
        player.getInventory().setOnDisplayListener(null);
        return true;
    }

    /**
     * Constructor of the inventory
     */
    protected Inventory() {
        _paperdoll = new L2ItemInstance[0x12];
        _paperdollListeners = new FastList<PaperdollListener>();
        addPaperdollListener(new ArmorSetListener());
        addPaperdollListener(new BowListener());
        addPaperdollListener(new ItemPassiveSkillsListener());
        addPaperdollListener(new StatsListener());
        addPaperdollListener(new ItemFakeAppearanceEquipListener(this));
    }

    protected abstract ItemLocation getEquipLocation();

    /**
     * Returns the instance of new ChangeRecorder
     *
     * @return ChangeRecorder
     */
    public ChangeRecorder newRecorder() {
        return new ChangeRecorder(this);
    }

    /**
     * Drop item from inventory and updates database
     *
     * @param process : String Identifier of process triggering this action
     * @param item : L2ItemInstance to be dropped
     * @param actor : L2PcInstance Player requesting the item drop
     * @param reference : L2Object Object referencing current action like NPC
     * selling item or previous item in transformation
     * @return L2ItemInstance corresponding to the destroyed item or the updated
     * item in inventory
     */
    public L2ItemInstance dropItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference) {
        synchronized (item) {
            if (!_items.contains(item)) {
                return null;
            }

            removeItem(item);
            item.setOwnerId(process, 0, actor, reference);
            item.setLocation(ItemLocation.VOID);
            item.setLastChange(L2ItemInstance.REMOVED);

            item.updateDatabase();
            refreshWeight();
        }
        return item;
    }

    /**
     * Drop item from inventory by using its <B>objectID</B> and updates
     * database
     *
     * @param process : String Identifier of process triggering this action
     * @param objectId : int Item Instance identifier of the item to be dropped
     * @param count : int Quantity of items to be dropped
     * @param actor : L2PcInstance Player requesting the item drop
     * @param reference : L2Object Object referencing current action like NPC
     * selling item or previous item in transformation
     * @return L2ItemInstance corresponding to the destroyed item or the updated
     * item in inventory
     */
    public L2ItemInstance dropItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference) {
        if (count <= 0) {
            return null;
        }
        L2ItemInstance item = getItemByObjectId(objectId);
        if (item == null) {
            return null;
        }

        // Adjust item quantity and create new instance to drop
        if (item.getCount() > count) {
            item.changeCount(process, -count, actor, reference);
            item.setLastChange(L2ItemInstance.MODIFIED);
            item.updateDatabase();

            item = ItemTable.getInstance().createItem(process, item.getItemId(), count, actor, reference);

            item.updateDatabase();
            refreshWeight();
            return item;
        } // Directly drop entire item
        else {
            return dropItem(process, item, actor, reference);
        }
    }

    /**
     * ���� ����� ��� ����������� ������� �� �����������
     */
    public L2ItemInstance dropItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference, boolean whFlag) {
        if (count <= 0) {
            return null;
        }
        L2ItemInstance item = getItemByObjectId(objectId);
        if (item == null) {
            return null;
        }

        item.setWhFlag(true);
        return dropItem(process, objectId, count, actor, reference);
    }

    /**
     * Adds item to inventory for further adjustments and Equip it if necessary
     * (itemlocation defined)<BR><BR>
     *
     * @param item : L2ItemInstance to be added from inventory
     */
    @Override
    protected void addItem(L2ItemInstance item) {
        super.addItem(item);
        if (item.isEquipped()) {
            equipItem(item);
        }
    }

    /**
     * Removes item from inventory for further adjustments.
     *
     * @param item : L2ItemInstance to be removed from inventory
     */
    @Override
    protected void removeItem(L2ItemInstance item) {
        // Unequip item if equiped
//    	if (item.isEquipped()) unEquipItemInSlotAndRecord(item.getEquipSlot());
        for (int i = 0; i < _paperdoll.length; i++) {
            if (_paperdoll[i] == item) {
                unEquipItemInSlot(i);
            }
        }

        super.removeItem(item);
    }

    /**
     * Returns the item in the paperdoll slot
     *
     * @return L2ItemInstance
     */
    public L2ItemInstance getPaperdollItem(int slot) {
        return _paperdoll[slot];
    }

    public L2ItemInstance[] getPaperdollItems() {
        return _paperdoll;
    }

    /**
     * Returns the item in the paperdoll L2Item slot
     *
     * @param slot identifier
     * @return L2ItemInstance
     */
    public L2ItemInstance getPaperdollItemByL2ItemId(int slot) {
        switch (slot) {
            case 0x01:
                return _paperdoll[0];
            case 0x04:
                return _paperdoll[1];
            case 0x02:
                return _paperdoll[2];
            case 0x08:
                return _paperdoll[3];
            case 0x20:
                return _paperdoll[4];
            case 0x10:
                return _paperdoll[5];
            case 0x40:
                return _paperdoll[6];
            case 0x80:
                return _paperdoll[7];
            case 0x0100:
                return _paperdoll[8];
            case 0x0200:
                return _paperdoll[9];
            case 0x0400:
                return _paperdoll[10];
            case 0x0800:
                return _paperdoll[11];
            case 0x1000:
                return _paperdoll[12];
            case 0x2000:
                return _paperdoll[13];
            case 0x4000:
                return _paperdoll[14];
            case 0x040000:
                return _paperdoll[15];
            case 0x010000:
                return _paperdoll[16];
            case 0x080000:
                return _paperdoll[17];
        }
        return null;
    }

    /**
     * Returns the ID of the item in the paperdol slot
     *
     * @param slot : int designating the slot
     * @return int designating the ID of the item
     */
    public int getPaperdollItemId(int slot) {
        L2ItemInstance item = _paperdoll[slot];
        if (item != null) {
            if(getOnDisplayListener() != null)
            {
                Integer displayId = getOnDisplayListener().onDisplay(slot, item, ((L2PlayableInstance) getOwner()));
                if(displayId != null)
                    return displayId;
            }
            return item.getItemId();
        } else if (slot == PAPERDOLL_HAIR) {
            item = _paperdoll[PAPERDOLL_DHAIR];
            if (item != null) {
                return item.getItemId();
            }
        } else if(getOnDisplayListener() != null)
        {
            Integer displayId = getOnDisplayListener().onDisplay(slot, null, ((L2PlayableInstance) getOwner()));
            if(displayId != null)
                return displayId;
        }
        return 0;
    }

    public int getPaperdollAugmentationId(int slot) {
        L2ItemInstance item = _paperdoll[slot];
        if (item != null) {
            if (item.getAugmentation() != null) {
                return item.getAugmentation().getAugmentationId();
            } else {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Returns the objectID associated to the item in the paperdoll slot
     *
     * @param slot : int pointing out the slot
     * @return int designating the objectID
     */
    public int getPaperdollObjectId(int slot) {
        L2ItemInstance item = _paperdoll[slot];
        if (item != null) {
            if(getOnDisplayListener() != null)
            {
                Integer displayId = getOnDisplayListener().onDisplay(slot, item, ((L2PlayableInstance) getOwner()));
                if(displayId != null)
                    return displayId;
            }
            return item.getObjectId();
        } else if (slot == PAPERDOLL_HAIR) {
            item = _paperdoll[PAPERDOLL_DHAIR];
            if (item != null) {
                return item.getObjectId();
            }
        } else if(getOnDisplayListener() != null)
        {
            Integer displayId = getOnDisplayListener().onDisplay(slot, null, ((L2PlayableInstance) getOwner()));
            if(displayId != null)
                return displayId;
        }
        return 0;
    }

    /**
     * Adds new inventory's paperdoll listener
     *
     * @param listener pointing out the listener
     */
    public synchronized void addPaperdollListener(PaperdollListener listener) {
        if (Config.ASSERT) {
            assert !_paperdollListeners.contains(listener);
        }
        _paperdollListeners.add(listener);
    }

    /**
     * Removes a paperdoll listener
     *
     * @param listener pointing out the listener to be deleted
     */
    public synchronized void removePaperdollListener(PaperdollListener listener) {
        _paperdollListeners.remove(listener);
    }

    public OnDisplayListener getOnDisplayListener()
    {
        return _onDisplayListener;
    }

    public void setOnDisplayListener(OnDisplayListener onDisplayListener)
    {
        _onDisplayListener = onDisplayListener;
    }

    /**
     * Equips an item in the given slot of the paperdoll.
     * <U><I>Remark :</I></U> The item <B>HAS TO BE</B> already in the inventory
     *
     * @param slot : int pointing out the slot of the paperdoll
     * @param item : L2ItemInstance pointing out the item to add in slot
     * @return L2ItemInstance designating the item placed in the slot before
     */
    public L2ItemInstance setPaperdollItem(int slot, L2ItemInstance item) {
        L2ItemInstance old = _paperdoll[slot];
        if (old != item) {
            if (old != null) {
                _paperdoll[slot] = null;
                // Put old item from paperdoll slot to base location
                old.setLocation(getBaseLocation());
                old.setLastChange(L2ItemInstance.MODIFIED);
                // Get the mask for paperdoll
                int mask = 0;
                for (int i = 0; i < PAPERDOLL_LRHAND; i++) {
                    L2ItemInstance pi = _paperdoll[i];
                    if (pi != null) {
                        mask |= pi.getItem().getItemMask();
                    }
                }
                _wearedMask = mask;
                // Notify all paperdoll listener in order to unequip old item in slot
                for (PaperdollListener listener : _paperdollListeners) {
                    if (listener == null) {
                        continue;
                    }
                    listener.notifyUnequiped(slot, old);
                    manageHippy(old.getItem().isHippy(), false);
                }
                old.updateDatabase();
            }
            // Add new item in slot of paperdoll
            if (item != null) {
                _paperdoll[slot] = item;
                item.setLocation(getEquipLocation(), slot);
                item.setLastChange(L2ItemInstance.MODIFIED);
                _wearedMask |= item.getItem().getItemMask();
                for (PaperdollListener listener : _paperdollListeners) {
                    listener.notifyEquiped(slot, item);
                    manageHippy(item.getItem().isHippy(), true);
                }
                item.updateDatabase();
            }
        }
        return old;
    }

    /**
     * Return the mask of weared item
     *
     * @return int
     */
    public int getWearedMask() {
        return _wearedMask;
    }

    public int getSlotFromItem(L2ItemInstance item) {
        int slot = -1;
        int location = item.getEquipSlot();

        switch (location) {
            case PAPERDOLL_UNDER:
                slot = L2Item.SLOT_UNDERWEAR;
                break;
            case PAPERDOLL_LEAR:
                slot = L2Item.SLOT_L_EAR;
                break;
            case PAPERDOLL_REAR:
                slot = L2Item.SLOT_R_EAR;
                break;
            case PAPERDOLL_NECK:
                slot = L2Item.SLOT_NECK;
                break;
            case PAPERDOLL_RFINGER:
                slot = L2Item.SLOT_R_FINGER;
                break;
            case PAPERDOLL_LFINGER:
                slot = L2Item.SLOT_L_FINGER;
                break;
            case PAPERDOLL_HAIR:
                slot = L2Item.SLOT_HAIR;
                break;
            case PAPERDOLL_FACE:
                slot = L2Item.SLOT_FACE;
                break;
            case PAPERDOLL_DHAIR:
                slot = L2Item.SLOT_DHAIR;
                break;
            case PAPERDOLL_HEAD:
                slot = L2Item.SLOT_HEAD;
                break;
            case PAPERDOLL_RHAND:
                slot = L2Item.SLOT_R_HAND;
                break;
            case PAPERDOLL_LHAND:
                slot = L2Item.SLOT_L_HAND;
                break;
            case PAPERDOLL_GLOVES:
                slot = L2Item.SLOT_GLOVES;
                break;
            case PAPERDOLL_CHEST:
                slot = item.getItem().getBodyPart();
                break;// fall through
            case PAPERDOLL_LEGS:
                slot = L2Item.SLOT_LEGS;
                break;
            case PAPERDOLL_BACK:
                slot = L2Item.SLOT_BACK;
                break;
            case PAPERDOLL_FEET:
                slot = L2Item.SLOT_FEET;
                break;
            case PAPERDOLL_LRHAND:
                slot = L2Item.SLOT_LR_HAND;
                break;
        }

        return slot;
    }

    public void unEquipItem(L2ItemInstance item) {
        unEquipItemInBodySlot(item.getItem().getBodyPart());
    }

    /**
     * Unequips item in body slot and returns alterations.
     *
     * @param slot : int designating the slot of the paperdoll
     * @return L2ItemInstance[] : list of changes
     */
    public L2ItemInstance[] unEquipItemInBodySlotAndRecord(int slot) {
        Inventory.ChangeRecorder recorder = newRecorder();
        try {
            unEquipItemInBodySlot(slot);
        } finally {
            removePaperdollListener(recorder);
        }
        return recorder.getChangedItems();
    }

    /**
     * Sets item in slot of the paperdoll to null value
     *
     * @param pdollSlot : int designating the slot
     * @return L2ItemInstance designating the item in slot before change
     */
    public synchronized L2ItemInstance unEquipItemInSlot(int pdollSlot) {
        return setPaperdollItem(pdollSlot, null);
    }

    /**
     * Unepquips item in slot and returns alterations
     *
     * @param slot : int designating the slot
     * @return L2ItemInstance[] : list of items altered
     */
    public synchronized L2ItemInstance[] unEquipItemInSlotAndRecord(int slot) {
        Inventory.ChangeRecorder recorder = newRecorder();
        try {
            unEquipItemInSlot(slot);
            if (getOwner().isPlayer()) {
                getOwner().getPlayer().refreshExpertisePenalty();
            }
        } finally {
            removePaperdollListener(recorder);
        }
        return recorder.getChangedItems();
    }

    /**
     * Unequips item in slot (i.e. equips with default value)
     *
     * @param slot : int designating the slot
     */
    private void unEquipItemInBodySlot(int slot) {
        int pdollSlot = -1;

        switch (slot) {
            case L2Item.SLOT_L_EAR:
                pdollSlot = PAPERDOLL_LEAR;
                break;
            case L2Item.SLOT_R_EAR:
                pdollSlot = PAPERDOLL_REAR;
                break;
            case L2Item.SLOT_NECK:
                pdollSlot = PAPERDOLL_NECK;
                break;
            case L2Item.SLOT_R_FINGER:
                pdollSlot = PAPERDOLL_RFINGER;
                break;
            case L2Item.SLOT_L_FINGER:
                pdollSlot = PAPERDOLL_LFINGER;
                break;
            case L2Item.SLOT_HAIR:
                pdollSlot = PAPERDOLL_HAIR;
                break;
            case L2Item.SLOT_FACE:
                pdollSlot = PAPERDOLL_FACE;
                break;
            case L2Item.SLOT_DHAIR:
                setPaperdollItem(PAPERDOLL_HAIR, null);
                setPaperdollItem(PAPERDOLL_FACE, null);// this should be the same as in DHAIR
                pdollSlot = PAPERDOLL_DHAIR;
                break;
            case L2Item.SLOT_HEAD:
                pdollSlot = PAPERDOLL_HEAD;
                break;
            case L2Item.SLOT_R_HAND:
                pdollSlot = PAPERDOLL_RHAND;
                break;
            case L2Item.SLOT_L_HAND:
                pdollSlot = PAPERDOLL_LHAND;
                break;
            case L2Item.SLOT_GLOVES:
                pdollSlot = PAPERDOLL_GLOVES;
                break;
            case L2Item.SLOT_CHEST:		// fall through
            case L2Item.SLOT_FULL_ARMOR:
                pdollSlot = PAPERDOLL_CHEST;
                break;
            case L2Item.SLOT_LEGS:
                pdollSlot = PAPERDOLL_LEGS;
                break;
            case L2Item.SLOT_BACK:
                pdollSlot = PAPERDOLL_BACK;
                break;
            case L2Item.SLOT_FEET:
                pdollSlot = PAPERDOLL_FEET;
                break;
            case L2Item.SLOT_UNDERWEAR:
                pdollSlot = PAPERDOLL_UNDER;
                break;
            case L2Item.SLOT_LR_HAND:
                setPaperdollItem(PAPERDOLL_LHAND, null);
                setPaperdollItem(PAPERDOLL_RHAND, null);// this should be the same as in LRHAND
                pdollSlot = PAPERDOLL_LRHAND;
                break;
        }
        if (pdollSlot >= 0) {
            setPaperdollItem(pdollSlot, null);
        }
    }

    /**
     * Equips item and returns list of alterations
     *
     * @param item : L2ItemInstance corresponding to the item
     * @return L2ItemInstance[] : list of alterations
     */
    public L2ItemInstance[] equipItemAndRecord(L2ItemInstance item) {
        Inventory.ChangeRecorder recorder = newRecorder();

        try {
            equipItem(item);
        } finally {
            removePaperdollListener(recorder);
        }

        return recorder.getChangedItems();
    }

    /**
     * Equips item in slot of paperdoll.
     *
     * @param item : L2ItemInstance designating the item and slot used.
     */
    public synchronized void equipItem(L2ItemInstance item) {
        /// if((getOwner().isPlayer()) && ((L2PcInstance)getOwner()).getPrivateStoreType() != 0)
        //return;

        if (getOwner().isPlayer() && getOwner().getName() != null) {

            //if(!player.isGM())
            //{
            L2PcInstance player = getOwner().getPlayer();
            if (item.isHeroItem() && !player.isHero() && Config.HERO_ITEMS_PENALTY) {
                return;
            }

            L2Clan cl = player.getClan();
            int itemId = item.getItemId();

            if ((cl == null || cl.getHasCastle() == 0) && itemId == 7015 && Config.CASTLE_SHIELD) {
                //A shield that can only be used by the members of a clan that owns a castle.
                player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                return;
            }

            if ((cl == null || cl.getHasHideout() == 0) && itemId == 6902 && Config.CLANHALL_SHIELD) {
                //A shield that can only be used by the members of a clan that owns a clan hall.
                player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                return;
            }

            if ((itemId >= 7860 && itemId <= 7879) && Config.APELLA_ARMORS && (cl == null || player.getPledgeClass() < 5)) {
                //Apella armor used by clan members may be worn by a Baron or a higher level Aristocrat.
                player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                return;
            }

            if ((itemId >= 7850 && itemId <= 7859) && Config.OATH_ARMORS && (cl == null)) {
                //Clan Oath armor used by all clan members
                player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                return;
            }

            if (itemId == 6841 && Config.CASTLE_CROWN && (cl == null || (cl.getHasCastle() == 0 || !player.isClanLeader()))) {
                //The Lord's Crown used by castle lords only
                player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                return;
            }

            //Castle circlets used by the members of a clan that owns a castle, academy members are excluded.
            if (Config.CASTLE_CIRCLETS && ((itemId >= 6834 && itemId <= 6840) || itemId == 8182 || itemId == 8183)) {
                if (cl == null) {
                    player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                    return;
                } else {
                    int circletId = CastleManager.getInstance().getCircletByCastleId(cl.getHasCastle());
                    if (player.getPledgeType() == -1 || circletId != itemId) {
                        player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
                        return;
                    }
                }
            }

            //}	
        }

        int targetSlot = item.getItem().getBodyPart();

        switch (targetSlot) {
            case L2Item.SLOT_LR_HAND: {
                if (setPaperdollItem(PAPERDOLL_LHAND, null) != null) {
                    // exchange 2h for 2h
                    setPaperdollItem(PAPERDOLL_RHAND, null);
                    setPaperdollItem(PAPERDOLL_LHAND, null);
                } else {
                    setPaperdollItem(PAPERDOLL_RHAND, null);
                }

                setPaperdollItem(PAPERDOLL_RHAND, item);
                setPaperdollItem(PAPERDOLL_LRHAND, item);
                break;
            }
            case L2Item.SLOT_L_HAND: {
                if (!(item.getItem() instanceof L2EtcItem) || item.getItem().getItemType() != L2EtcItemType.ARROW) {
                    L2ItemInstance old1 = setPaperdollItem(PAPERDOLL_LRHAND, null);

                    if (old1 != null) {
                        setPaperdollItem(PAPERDOLL_RHAND, null);
                    }
                }

                setPaperdollItem(PAPERDOLL_LHAND, null);
                setPaperdollItem(PAPERDOLL_LHAND, item);
                break;
            }
            case L2Item.SLOT_R_HAND: {
                if (_paperdoll[PAPERDOLL_LRHAND] != null) {
                    setPaperdollItem(PAPERDOLL_LRHAND, null);
                    setPaperdollItem(PAPERDOLL_LHAND, null);
                    setPaperdollItem(PAPERDOLL_RHAND, null);
                } else {
                    setPaperdollItem(PAPERDOLL_RHAND, null);
                }

                setPaperdollItem(PAPERDOLL_RHAND, item);
                break;
            }
            case L2Item.SLOT_L_EAR:
            case L2Item.SLOT_R_EAR:
            case L2Item.SLOT_L_EAR | L2Item.SLOT_R_EAR: {
                if (_paperdoll[PAPERDOLL_LEAR] == null) {
                    setPaperdollItem(PAPERDOLL_LEAR, item);
                } else if (_paperdoll[PAPERDOLL_REAR] == null) {
                    setPaperdollItem(PAPERDOLL_REAR, item);
                } else {
                    setPaperdollItem(PAPERDOLL_LEAR, null);
                    setPaperdollItem(PAPERDOLL_LEAR, item);
                }

                break;
            }
            case L2Item.SLOT_L_FINGER:
            case L2Item.SLOT_R_FINGER:
            case L2Item.SLOT_L_FINGER | L2Item.SLOT_R_FINGER: {
                if (_paperdoll[PAPERDOLL_LFINGER] == null) {
                    setPaperdollItem(PAPERDOLL_LFINGER, item);
                } else if (_paperdoll[PAPERDOLL_RFINGER] == null) {
                    setPaperdollItem(PAPERDOLL_RFINGER, item);
                } else {
                    setPaperdollItem(PAPERDOLL_LFINGER, null);
                    setPaperdollItem(PAPERDOLL_LFINGER, item);
                }

                break;
            }
            case L2Item.SLOT_NECK:
                setPaperdollItem(PAPERDOLL_NECK, item);
                break;
            case L2Item.SLOT_FULL_ARMOR:
                setPaperdollItem(PAPERDOLL_CHEST, null);
                setPaperdollItem(PAPERDOLL_LEGS, null);
                setPaperdollItem(PAPERDOLL_CHEST, item);
                break;
            case L2Item.SLOT_CHEST:
                setPaperdollItem(PAPERDOLL_CHEST, item);
                break;
            case L2Item.SLOT_LEGS: {
                // handle full armor
                L2ItemInstance chest = getPaperdollItem(PAPERDOLL_CHEST);
                if (chest != null && chest.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR) {
                    setPaperdollItem(PAPERDOLL_CHEST, null);
                }

                setPaperdollItem(PAPERDOLL_LEGS, null);
                setPaperdollItem(PAPERDOLL_LEGS, item);
                break;
            }
            case L2Item.SLOT_FEET:
                setPaperdollItem(PAPERDOLL_FEET, item);
                break;
            case L2Item.SLOT_GLOVES:
                setPaperdollItem(PAPERDOLL_GLOVES, item);
                break;
            case L2Item.SLOT_HEAD:
                setPaperdollItem(PAPERDOLL_HEAD, item);
                break;
            case L2Item.SLOT_HAIR:
                if (setPaperdollItem(PAPERDOLL_DHAIR, null) != null) {
                    setPaperdollItem(PAPERDOLL_DHAIR, null);
                    setPaperdollItem(PAPERDOLL_HAIR, null);
                    setPaperdollItem(PAPERDOLL_FACE, null);
                } else {
                    setPaperdollItem(PAPERDOLL_HAIR, null);
                }
                setPaperdollItem(PAPERDOLL_HAIR, item);
                break;
            case L2Item.SLOT_FACE:
                if (setPaperdollItem(PAPERDOLL_DHAIR, null) != null) {
                    setPaperdollItem(PAPERDOLL_DHAIR, null);
                    setPaperdollItem(PAPERDOLL_HAIR, null);
                    setPaperdollItem(PAPERDOLL_FACE, null);
                } else {
                    setPaperdollItem(PAPERDOLL_FACE, null);
                }
                setPaperdollItem(PAPERDOLL_FACE, item);
                break;
            case L2Item.SLOT_DHAIR:
                if (setPaperdollItem(PAPERDOLL_HAIR, null) != null) {
                    setPaperdollItem(PAPERDOLL_HAIR, null);
                    setPaperdollItem(PAPERDOLL_FACE, null);
                } else {
                    setPaperdollItem(PAPERDOLL_FACE, null);
                }
                setPaperdollItem(PAPERDOLL_DHAIR, item);
                break;
            case L2Item.SLOT_UNDERWEAR:
                setPaperdollItem(PAPERDOLL_UNDER, item);
                break;
            case L2Item.SLOT_BACK:
                setPaperdollItem(PAPERDOLL_BACK, item);
                break;
            default:
                _log.warning("unknown body slot:" + targetSlot);
        }
    }

    /**
     * Refresh the weight of equipment loaded
     */
    @Override
    protected void refreshWeight() {
        int weight = 0;

        for (L2ItemInstance item : _items) {
            if (item != null && item.getItem() != null) {
                weight += item.getItem().getWeight() * item.getCount();
            }
        }

        _totalWeight = weight;
        checkRuneSkills(this.getOwner().getPlayer());
    }

    /**
     * Returns the totalWeight.
     *
     * @return int
     */
    public int getTotalWeight() {
        return _totalWeight;
    }

    /**
     * Return the L2ItemInstance of the arrows needed for this bow.<BR><BR>
     *
     * @param bow : L2Item designating the bow
     * @return L2ItemInstance pointing out arrows for bow
     */
    public L2ItemInstance findArrowForBow(L2Item bow) {
        if (bow == null) {
            return null;
        }

        int arrowsId = 0;
        switch (bow.getCrystalType()) {
            default: // broken weapon.csv ??
            case L2Item.CRYSTAL_NONE:
                arrowsId = 17;
                break; // Wooden arrow
            case L2Item.CRYSTAL_D:
                arrowsId = 1341;
                break; // Bone arrow
            case L2Item.CRYSTAL_C:
                arrowsId = 1342;
                break; // Fine steel arrow
            case L2Item.CRYSTAL_B:
                arrowsId = 1343;
                break; // Silver arrow
            case L2Item.CRYSTAL_A:
                arrowsId = 1344;
                break; // Mithril arrow
            case L2Item.CRYSTAL_S:
                arrowsId = 1345;
                break; // Shining arrow
        }

        // Get the L2ItemInstance corresponding to the item identifier and return it
        return getItemByItemId(arrowsId);
    }

    /**
     * Get back items in inventory from database
     */
    @Override
    public void restore() {
        Connect con = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            con = L2DatabaseFactory.get();
            con.setTransactionIsolation(1);
            st = con.prepareStatement("SELECT object_id FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY object_id DESC");
            st.setInt(1, getOwner().getObjectId());
            st.setString(2, getBaseLocation().name());
            st.setString(3, getEquipLocation().name());
            rs = st.executeQuery();

            L2ItemInstance item;
            while (rs.next()) {
                int objectId = rs.getInt(1);
                item = L2ItemInstance.restoreFromDb(objectId);
                if (item == null) {
                    continue;
                }

                if (getOwner().isPlayer()) {
                    if (!getOwner().getPlayer().isGM()) {
                        if (!getOwner().getPlayer().isHero()) {
                            int itemId = item.getItemId();
                            if ((itemId >= 6611 && itemId <= 6621) || itemId == 6842) {
                                item.setLocation(ItemLocation.INVENTORY);
                            }
                        }
                    }
                }

                L2World.getInstance().storeObject(item);

                // If stackable item is found in inventory just add to current quantity
                if (item.isStackable() && getItemByItemId(item.getItemId()) != null) {
                    addItem("Restore", item, null, getOwner());
                } else {
                    addItem(item);
                }
            }
            refreshWeight();
        } catch (final SQLException e) {
            _log.warning("Could not restore inventory : " + e);
        } finally {
            Close.CSR(con, st, rs);
        }
    }

    /**
     * Re-notify to paperdoll listeners every equipped item
     */
    public void reloadEquippedItems() {

        L2ItemInstance item;
        int slot;

        for (int i = 0; i < _paperdoll.length; i++) {
            item = _paperdoll[i];
            if (item == null) {
                continue;
            }
            slot = item.getEquipSlot();

            for (PaperdollListener listener : _paperdollListeners) {
                if (listener == null) {
                    continue;
                }
                listener.notifyUnequiped(slot, item);
                listener.notifyEquiped(slot, item);
            }
        }
    }

    private void manageHippy(boolean hippy, boolean b) {
        getOwner().checkForbiddenItems();
        if (!hippy) {
            return;
        }
        if (b) {
            getOwner().setHippy(b);
            return;
        }

        getOwner().setHippy(false);
    }

    public boolean isEquippedItem(int item_id, L2ItemInstance item) {
        return isEquippedItem(item_id, item, 0);
    }

    public boolean isEquippedItem(int item_id, L2ItemInstance item, int ench) {
        for (int i = 0; i < _paperdoll.length; i++) {
            item = _paperdoll[i];
            if (item == null) {
                continue;
            }
            if (item_id == item.getItemId()) {
                if (ench != 0
                        && item.getEnchantLevel() < ench) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public static void cacheRunes()
    {
        try
        {
            File file = new File(Config.DATAPACK_ROOT, "data/runes_mod.xml");
            if (!file.exists())
            {
                _log.info("CustomServerData [ERROR]: data/runes_mod.xml doesn't exist");
                return;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            Document doc = factory.newDocumentBuilder().parse(file);
            for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
                if ("list".equalsIgnoreCase(n.getNodeName())) {
                    for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                        if ("rune".equalsIgnoreCase(d.getNodeName()))
                        {
                            NamedNodeMap attrs = d.getAttributes();
                            int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
                            int skillLvl = Integer.parseInt(attrs.getNamedItem("skillLvl").getNodeValue());
                            L2Skill check = SkillTable.getInstance().getInfo(skillId, skillLvl);
                            if (check != null)
                            {
                                RuneSkill rune = new RuneSkill(skillId, skillLvl);
                                for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                                    if ("items".equalsIgnoreCase(cd.getNodeName()))
                                    {
                                        attrs = cd.getAttributes();

                                        List<Integer> listId = new ArrayList<Integer>();
                                        String[] idS = attrs.getNamedItem("list").getNodeValue().split(",");
                                        for (String item : idS) {
                                            if (!item.equalsIgnoreCase("")) {
                                                listId.add(Integer.parseInt(item));
                                            }
                                        }
                                        rune.addRunes(listId);
                                    }
                                }
                                _runesList.add(rune);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            _log.warning("CustomServerData [ERROR]: cacheRunes() " + e.toString());
        }
        _log.info("CustomServerData: Runes Mod, loaded " + _runesList.size() + " rune skills.");
    }

    private static List<RuneSkill> _runesList = new ArrayList<RuneSkill>();

    public void checkRuneSkills(L2PcInstance player)
    {
        if (player == null) {
            return;
        }
        for (RuneSkill rune : _runesList)
        {
            L2Skill sk = SkillTable.getInstance().getInfo(rune.skill.getId(), rune.skill.getLevel());
            if (sk != null) {
                applyRuneSkill(player, rune, findRuneItems(player, rune), player.getKnownSkill(rune.skill.getId()));
            }
        }
    }

    private void applyRuneSkill(L2PcInstance player, RuneSkill rune, boolean item, L2Skill skill)
    {
        if (item)
        {
            if (skill == null)
            {
                player.addSkill(rune.skill, false);
                player.sendSkillList();
            }
            else if (skill.getLevel() < rune.skill.getLevel())
            {
                player.removeSkill(skill, false);
                player.addSkill(rune.skill, false);
                player.sendSkillList();
            }
            return;
        }
        if (skill != null && skill.getLevel() == rune.skill.getLevel())
        {
            player.removeSkill(rune.skill, false);
            player.sendSkillList();
        }
    }

    private boolean findRuneItems(L2PcInstance player, RuneSkill rune)
    {
        for (Integer item : rune.runes) {
            if (player.getInventory().getItemByItemId(item) != null && player.getItemCount(item) > 0) {
                return true;
            }
        }
        return false;
    }

    public List<RuneSkill> getRunes()
    {
        return _runesList;
    }

    public static class RuneSkill
    {
        public L2Skill skill;
        public List<Integer> runes = new ArrayList<Integer>();

        public RuneSkill(int id, int lvl)
        {
            this.skill = SkillTable.getInstance().getInfo(id, lvl);
        }

        public void addRunes(List<Integer> listId)
        {
            this.runes.addAll(listId);
        }
    }

    public static int getPaperdollIndex(int slot)
    {
        switch(slot)
        {
            case L2Item.SLOT_UNDERWEAR:
                return PAPERDOLL_UNDER;
            case L2Item.SLOT_R_EAR:
                return PAPERDOLL_REAR;
            case L2Item.SLOT_L_EAR:
                return PAPERDOLL_LEAR;
            case L2Item.SLOT_NECK:
                return PAPERDOLL_NECK;
            case L2Item.SLOT_R_FINGER:
                return PAPERDOLL_RFINGER;
            case L2Item.SLOT_L_FINGER:
                return PAPERDOLL_LFINGER;
            case L2Item.SLOT_HEAD:
                return PAPERDOLL_HEAD;
            case L2Item.SLOT_R_HAND:
                return PAPERDOLL_RHAND;
            case L2Item.SLOT_L_HAND:
                return PAPERDOLL_LHAND;
            case L2Item.SLOT_LR_HAND:
                return PAPERDOLL_RHAND;
            case L2Item.SLOT_GLOVES:
                return PAPERDOLL_GLOVES;
            case L2Item.SLOT_CHEST:
            case L2Item.SLOT_FULL_ARMOR:
            //case L2Item.SLOT_FORMAL_WEAR:
                return PAPERDOLL_CHEST;
            case L2Item.SLOT_LEGS:
                return PAPERDOLL_LEGS;
            case L2Item.SLOT_FEET:
                return PAPERDOLL_FEET;
            case L2Item.SLOT_BACK:
                return PAPERDOLL_BACK;
            case L2Item.SLOT_HAIR:
            //case L2Item.SLOT_HAIRALL:
                return PAPERDOLL_HAIR;
            case L2Item.SLOT_DHAIR:
                return PAPERDOLL_DHAIR;
        }
        return -1;
    }
}
