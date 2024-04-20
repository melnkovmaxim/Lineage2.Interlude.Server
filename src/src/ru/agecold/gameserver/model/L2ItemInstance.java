package ru.agecold.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.agecold.Config;
import ru.agecold.L2DatabaseFactory;
import ru.agecold.gameserver.GeoData;
import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.ai.CtrlIntention;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.datatables.ItemTable;
import ru.agecold.gameserver.instancemanager.ItemsOnGroundManager;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.knownlist.NullKnownList;
import ru.agecold.gameserver.network.L2GameClient;
import ru.agecold.gameserver.network.SystemMessageId;
import ru.agecold.gameserver.network.serverpackets.InventoryUpdate;
import ru.agecold.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.agecold.gameserver.network.serverpackets.SystemMessage;
import ru.agecold.gameserver.skills.funcs.Func;
import ru.agecold.gameserver.taskmanager.ExpiredItemsTaskManager;
import ru.agecold.gameserver.templates.L2Armor;
import ru.agecold.gameserver.templates.L2EtcItem;
import ru.agecold.gameserver.templates.L2Item;
import ru.agecold.gameserver.templates.L2WeaponType;
import ru.agecold.mysql.Close;
import ru.agecold.mysql.Connect;

/**
 * This class manages items.
 *
 * @version $Revision: 1.4.2.1.2.11 $ $Date: 2005/03/31 16:07:50 $
 */
public final class L2ItemInstance extends L2Object {

    private static final Logger _log = Logger.getLogger(L2ItemInstance.class.getName());
    private static final Logger _logItems = Logger.getLogger("item");

    /**
     * Enumeration of locations for item
     */
    public static enum ItemLocation {

        VOID,
        INVENTORY,
        PAPERDOLL,
        WAREHOUSE,
        CLANWH,
        PET,
        PET_EQUIP,
        LEASE,
        FREIGHT
    }
    /**
     * ID of the owner
     */
    private int _ownerId;
    /**
     * Quantity of the item
     */
    private int _count;
    /**
     * Initial Quantity of the item
     */
    private int _initCount;
    /**
     * Time after restore Item count (in Hours)
     */
    private int _time;
    /**
     * Quantity of the item can decrease
     */
    private boolean _decrease = false;
    /**
     * ID of the item
     */
    private final int _itemId;
    /**
     * Object L2Item associated to the item
     */
    private final L2Item _item;
    /**
     * Location of the item : Inventory, PaperDoll, WareHouse
     */
    private ItemLocation _loc;
    /**
     * Slot where item is stored
     */
    private int _locData;
    /**
     * Level of enchantment of the item
     */
    private int _enchantLevel;
    /**
     * Price of the item for selling
     */
    private int _priceSell;
    /**
     * Price of the item for buying
     */
    private int _priceBuy;
    /**
     * Wear Item
     */
    private boolean _wear;
    /**
     * Augmented Item
     */
    private L2Augmentation _augmentation = null;
    /**
     * Shadow item
     */
    private int _mana = -1;
    private boolean _consumingMana = false;
    private static final int MANA_CONSUMPTION_RATE = 60000;
    /**
     * Custom item types (used loto, race tickets)
     */
    private int _type1;
    private int _type2;
    private long _dropTime;
    public static final int CHARGED_NONE = 0;
    public static final int CHARGED_SOULSHOT = 1;
    public static final int CHARGED_SPIRITSHOT = 1;
    public static final int CHARGED_BLESSED_SOULSHOT = 2; // It's a realy exists? ;-)
    public static final int CHARGED_BLESSED_SPIRITSHOT = 2;
    /**
     * Item charged with SoulShot (type of SoulShot)
     */
    private int _chargedSoulshot = CHARGED_NONE;
    /**
     * Item charged with SpiritShot (type of SpiritShot)
     */
    private int _chargedSpiritshot = CHARGED_NONE;
    private boolean _chargedFishtshot = false;
    private boolean _protected;
    public static final int UNCHANGED = 0;
    public static final int ADDED = 1;
    public static final int REMOVED = 3;
    public static final int MODIFIED = 2;
    private int _lastChange = 2;	//1 ??, 2 modified, 3 removed
    private boolean _existsInDb; // if a record exists in DB.
    private boolean _storedInDb; // if DB data is up-to-date.
    // ���� ����������� ����������� ��� �������� � ��
    private boolean _whFlag = false;
    private ScheduledFuture<?> itemLootShedule = null;
    /**
     * ���������� ������ � �� ��� ���������� �������� �����
     */
    private Future<?> _lazyUpdateInDb;

    /**
     * Task of delayed update item info in database
     */
    private static class LazyUpdateInDb implements Runnable {

        private final int itemStoreId;

        public LazyUpdateInDb(L2ItemInstance item) {
            itemStoreId = item.getObjectId();
        }

        public void run() {
            updateInDb(L2World.getInstance().getItem(itemStoreId));
        }

        public void updateInDb(L2ItemInstance item) {
            if (item == null) {
                return;
            }
            try {
                item.updateInDb();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                item.stopLazyUpdateTask(false);
            }
        }
    }

    /**
     * Constructor of the L2ItemInstance from the objectId and the itemId.
     *
     * @param objectId : int designating the ID of the object in the world
     * @param itemId : int designating the ID of the item
     */
    public L2ItemInstance(int objectId, int itemId) {
        super(objectId);
        super.setKnownList(new NullKnownList(this));
        _itemId = itemId;
        _item = ItemTable.getInstance().getTemplate(itemId);
        if (_itemId == 0 || _item == null) {
            throw new IllegalArgumentException();
        }
        _count = 1;
        _loc = ItemLocation.VOID;
        _type1 = 0;
        _type2 = 0;
        _dropTime = 0;
        _life = _item.getExpire();
        _mana = _life > 0 ? -1 : _item.getDuration();
    }

    /**
     * Constructor of the L2ItemInstance from the objetId and the description of
     * the item given by the L2Item.
     *
     * @param objectId : int designating the ID of the object in the world
     * @param item : L2Item containing informations of the item
     */
    public L2ItemInstance(int objectId, L2Item item) {
        super(objectId);
        super.setKnownList(new NullKnownList(this));
        if (item == null) {
            throw new IllegalArgumentException();
        }
        _itemId = item.getItemId();
        _item = item;
        if (_itemId == 0 || _item == null) {
            throw new IllegalArgumentException();
        }
        _count = 1;
        _loc = ItemLocation.VOID;

        _life = _item.getExpire();
        _mana = _life > 0 ? -1 : _item.getDuration();
    }

    /**
     * Sets the ownerID of the item
     *
     * @param process : String Identifier of process triggering this action
     * @param owner_id : int designating the ID of the owner
     * @param creator : L2PcInstance Player requesting the item creation
     * @param reference : L2Object Object referencing current action like NPC
     * selling item or previous item in transformation
     */
    public void setOwnerId(String process, int owner_id, L2PcInstance creator, L2Object reference) {
        setOwnerId(owner_id);

        /*
         * if (Config.LOG_ITEMS && isLoggable(process)) { LogRecord record = new
         * LogRecord(Level.INFO, "CHANGE:" + process);
         * record.setLoggerName("item"); record.setParameters(new Object[]{this,
         * creator, reference}); _logItems.log(record);
         }
         */
    }

    /**
     * Sets the ownerID of the item
     *
     * @param owner_id : int designating the ID of the owner
     */
    public void setOwnerId(int owner_id) {
        if (owner_id == _ownerId) {
            return;
        }

        _ownerId = owner_id;
        _storedInDb = false;
    }

    /**
     * Returns the ownerID of the item
     *
     * @return int : ownerID of the item
     */
    public int getOwnerId() {
        return _ownerId;
    }

    /**
     * Sets the location of the item
     *
     * @param loc : ItemLocation (enumeration)
     */
    public void setLocation(ItemLocation loc) {
        setLocation(loc, 0);
    }

    /**
     * Sets the location of the item.<BR><BR> <U><I>Remark :</I></U> If loc and
     * loc_data different from database, say datas not up-to-date
     *
     * @param loc : ItemLocation (enumeration)
     * @param loc_data : int designating the slot where the item is stored or
     * the village for freights
     */
    public void setLocation(ItemLocation loc, int loc_data) {
        if (loc == _loc && loc_data == _locData) {
            return;
        }
        _loc = loc;
        _locData = loc_data;
        _storedInDb = false;
    }

    public ItemLocation getLocation() {
        return _loc;
    }

    /**
     * Returns the quantity of item
     *
     * @return int
     */
    public int getCount() {
        return _count;
    }

    /**
     * Sets the quantity of the item.<BR><BR> <U><I>Remark :</I></U> If loc and
     * loc_data different from database, say datas not up-to-date
     *
     * @param process : String Identifier of process triggering this action
     * @param count : int
     * @param creator : L2PcInstance Player requesting the item creation
     * @param reference : L2Object Object referencing current action like NPC
     * selling item or previous item in transformation
     */
    public void changeCount(String process, int count, L2PcInstance creator, L2Object reference) {
        if (count == 0) {
            return;
        }

        if (count > 0 && _count > Integer.MAX_VALUE - count) {
            count = Integer.MAX_VALUE;
        } else {
            count = _count + count;
        }

        if (count < 0) {
            count = 0;
        }

        setCount(count);

        /*
         * if (Config.LOG_ITEMS && isLoggable(process)) { LogRecord record = new
         * LogRecord(Level.INFO, "CHANGE:" + process);
         * record.setLoggerName("item"); record.setParameters(new Object[]{this,
         * creator, reference}); _logItems.log(record);
         }
         */
    }

    // No logging (function designed for shots only)
    public void changeCountWithoutTrace(String process, int count, L2PcInstance creator, L2Object reference) {
        if (count == 0) {
            return;
        }
        if (count > 0 && _count > Integer.MAX_VALUE - count) {
            count = Integer.MAX_VALUE;
        } else {
            count = _count + count;
        }
        if (count < 0) {
            count = 0;
        }
        setCount(count);
    }

    /**
     * Sets the quantity of the item.<BR><BR> <U><I>Remark :</I></U> If loc and
     * loc_data different from database, say datas not up-to-date
     *
     * @param count : int
     */
    public void setCount(int count) {
        if (_count == count) {
            return;
        }

        count = Math.max(count, 0);
        count = Math.min(count, checkMaxCount(Config.ITEM_MAX_COUNT.get(_itemId), count));

        _count = count;

        _storedInDb = false;
    }

    public void setCountOnLoad(int count)
    {
        if (_count == count) {
            return;
        }
        count = Math.max(count, 0);
        count = Math.min(count, checkMaxCount(Config.ITEM_MAX_COUNT.get(_itemId), count));

        _count = count;
    }

    private int checkMaxCount(Integer max, int current)
    {
        if (!Config.ITEM_COUNT_LIMIT || max == null || current == 1) {
            return Integer.MAX_VALUE;
        }
        sendLimitMessage(L2World.getInstance().getPlayer(_ownerId), current, max);
        return max;
    }

    private void sendLimitMessage(L2PcInstance owner, int current, int max)
    {
        if (!Config.ITEM_COUNT_LIMIT_WARN || owner == null) {
            return;
        }
        if (current < max) {
            return;
        }
        NpcHtmlMessage htm = new NpcHtmlMessage(0);
        htm.setFile("data/html/item_limit.htm");
        htm.replace("%NAME%", String.valueOf(this._item.getName()));
        htm.replace("%COUNT%", String.valueOf(current));
        htm.replace("%MAX%", String.valueOf(max));
        owner.sendPacket(htm);
    }

    /**
     * Returns if item is equipable
     *
     * @return boolean
     */
    public boolean isEquipable() {
        return !(_item.getBodyPart() == 0 || _item instanceof L2EtcItem);
    }

    /**
     * Returns if item is equipped
     *
     * @return boolean
     */
    public boolean isEquipped() {
        return _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP;
    }

    /**
     * Returns the slot where the item is stored
     *
     * @return int
     */
    public int getLocationSlot() {
        if (Config.ASSERT) {
            assert _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP || _loc == ItemLocation.FREIGHT || _loc == ItemLocation.INVENTORY;
        }
        return _locData;
    }

    /**
     * Returns the slot where the item is stored
     *
     * @return int
     */
    public int getEquipSlot() {
        if (Config.ASSERT) {
            assert _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP || _loc == ItemLocation.FREIGHT;
        }
        return _locData;
    }

    /**
     * Returns the characteristics of the item
     *
     * @return L2Item
     */
    public L2Item getItem() {
        return _item;
    }

    public int getCustomType1() {
        return _type1;
    }

    public int getCustomType2() {
        return _type2;
    }

    public void setCustomType1(int newtype) {
        _type1 = newtype;
    }

    public void setCustomType2(int newtype) {
        _type2 = newtype;
    }

    public void setDropTime(long time) {
        _dropTime = time;
    }

    public long getDropTime() {
        return _dropTime;
    }

    public boolean isWear() {
        return _wear;
    }

    public void setWear(boolean newwear) {
        _wear = newwear;
    }

    /**
     * Returns the type of item
     *
     * @return Enum
     */
    @SuppressWarnings("unchecked")
    public Enum getItemType() {
        return _item.getItemType();
    }

    /**
     * Returns the ID of the item
     *
     * @return int
     */
    public int getItemId() {
        return _itemId;
    }

    /**
     * Returns the quantity of crystals for crystallization
     *
     * @return int
     */
    public final int getCrystalCount() {
        return _item.getCrystalCount(_enchantLevel);
    }

    /**
     * Returns the reference price of the item
     *
     * @return int
     */
    public int getReferencePrice() {
        return _item.getReferencePrice();
    }

    /**
     * Returns the name of the item
     *
     * @return String
     */
    public String getItemName() {
        return _item.getName();
    }

    /**
     * Returns the price of the item for selling
     *
     * @return int
     */
    public int getPriceToSell() {
        return (isConsumable() ? (int) (_priceSell * Config.RATE_CONSUMABLE_COST) : _priceSell);
    }

    /**
     * Sets the price of the item for selling <U><I>Remark :</I></U> If loc and
     * loc_data different from database, say datas not up-to-date
     *
     * @param price : int designating the price
     */
    public void setPriceToSell(int price) {
        _priceSell = price;
        _storedInDb = false;
    }

    /**
     * Returns the price of the item for buying
     *
     * @return int
     */
    public int getPriceToBuy() {
        return (isConsumable() ? (int) (_priceBuy * Config.RATE_CONSUMABLE_COST) : _priceBuy);
    }

    /**
     * Sets the price of the item for buying <U><I>Remark :</I></U> If loc and
     * loc_data different from database, say datas not up-to-date
     *
     * @param price : int
     */
    public void setPriceToBuy(int price) {
        _priceBuy = price;
        _storedInDb = false;
    }

    /**
     * Returns the last change of the item
     *
     * @return int
     */
    public int getLastChange() {
        return _lastChange;
    }

    /**
     * Sets the last change of the item
     *
     * @param lastChange : int
     */
    public void setLastChange(int lastChange) {
        _lastChange = lastChange;
    }

    /**
     * Returns if item is stackable
     *
     * @return boolean
     */
    public boolean isStackable() {
        return _item.isStackable();
    }

    /**
     * Returns if item is dropable
     *
     * @return boolean
     */
    public boolean isDropable() {
        return isAugmented() ? false : isShadowItem() ? false : _item.isDropable();
    }

    public boolean isDropablePk() {
        return isAugmented() ? Config.CAN_DROP_AUGMENTS : isShadowItem() ? false : _item.isDropable();
    }

    /**
     * Returns if item is destroyable
     *
     * @return boolean
     */
    public boolean isDestroyable() {
        return _item.isDestroyable();
    }

    /**
     * Returns if item is tradeable
     *
     * @return boolean
     */
    public boolean isTradeable() {
        return isEnchLimited() ? false : isAugmented() ? false : isShadowItem() ? false : _item.isTradeable();
    }

    public boolean isEnchLimited() {
        return (Config.MAX_TRADE_ENCHANT > 0 && _enchantLevel >= Config.MAX_TRADE_ENCHANT);
    }

    /**
     * Returns if item is consumable
     *
     * @return boolean
     */
    public boolean isConsumable() {
        return _item.isConsumable();
    }

    /**
     * �������
     */
    public boolean isBlessedEnchantScroll() {
        switch (_itemId) {
            case 6569: // Wpn A
            case 6570: // Arm A
            case 6571: // Wpn B
            case 6572: // Arm B
            case 6573: // Wpn C
            case 6574: // Arm C
            case 6575: // Wpn D
            case 6576: // Arm D
            case 6577: // Wpn S
            case 6578: // Arm S
                return true;
            default:
                if (Config.BLESS_BONUSES.contains(_itemId) || Config.BLESS_BONUSES2.contains(_itemId)) {
                    return true;
                }
                return false;
        }
    }

    public boolean isCrystallEnchantScroll() {
        switch (_itemId) {
            case 731:
            case 732:
            case 949:
            case 950:
            case 953:
            case 954:
            case 957:
            case 958:
            case 961:
            case 962:
                return true;
        }
        return false;
    }

    public int getEnchantCrystalId(L2ItemInstance scroll) {
        if (_item.getType2() == L2Item.TYPE2_WEAPON && !scroll.getItem().getName().matches(".*Weapon.*")) {
            return 0;
        } else if ((_item.getType2() == L2Item.TYPE2_SHIELD_ARMOR || _item.getType2() == L2Item.TYPE2_ACCESSORY) && !scroll.getItem().getName().matches(".*Armor.*")) {
            return 0;
        }
        return scroll.getItem().getCrystalItemId();
    }

    public boolean isMagicWeapon() {
        switch (_itemId) {
            case 148: //Sword of Valhalla
            case 150: //Elemental Sword
            case 151: //Sword of Miracles
            case 5641: //Sword of Miracles
            case 5642: //Sword of Miracles
            case 5643: //Sword of Miracles
            case 5638: //Elemental Sword
            case 5639: //Elemental Sword
            case 5640: //Elemental Sword
            case 6579: //Arcana Mace
            case 6608: //Arcana Mace
            case 6609: //Arcana Mace
            case 6610: //Arcana Mace
            case 6366: //Imperial Staff
            case 6587: //Imperial Staff
            case 6588: //Imperial Staff
            case 6589: //Imperial Staff
            case 7722: //Sword of Valhalla
            case 7723: //Sword of Valhalla
            case 7724: //Sword of Valhalla
                return true;
            default:
                return (Config.ALT_MAGIC_WEAPONS.contains(_itemId));
        }
    }

    /**
     * ��������� ������
     */
    public boolean isWeddingRing() {
        int myid = _itemId;
        return myid == 50001 || myid == 50003;
    }

    /**
     * �������
     */
    public boolean isMagicShot() {
        switch (_itemId) {
            case 2509: //Spirits
            case 2510:
            case 2511:
            case 2512:
            case 2513:
            case 2514:
            case 3947: //Blessed Spirits
            case 3948:
            case 3949:
            case 3950:
            case 3951:
            case 3952:
            case 5790: //Beginners Spirit
                return true;
        }
        return false;
    }

    public boolean isFighterShot() {
        switch (_itemId) {
            case 1463: //Soulshot
            case 1464:
            case 1465:
            case 1466:
            case 1467:
            case 1835:
            case 5789:
                return true;
        }
        return false;
    }

    /**
     * Returns if item is available for manipulation
     *
     * @return boolean
     */
    public boolean isAvailable(L2PcInstance player, boolean allowAdena) {
        return ((!isEquipped()) // Not equipped
                && (getItem().getType2() != 3) // Not Quest Item
                && (getItem().getType2() != 4 || getItem().getType1() != 1) // TODO: what does this mean?
                && (player.getPet() == null || getObjectId() != player.getPet().getControlItemId()) // Not Control item of currently summoned pet
                && (player.getActiveEnchantItem() != this) // Not momentarily used enchant scroll
                && (allowAdena || getItemId() != 57)
                && (player.getCurrentSkill() == null || player.getCurrentSkill().getSkill().getItemConsumeId() != getItemId())
                && (isTradeable()));
    }

    /**
     * ��������� ����
     */
    public boolean isHeroItem() {
        return (_itemId >= 6611 && _itemId <= 6621) || _itemId == 6842;
    }

    public boolean notForOly() {
        if (Config.RESET_OLY_ENCH) {
            return _item.notForOly();
        }

        if (_enchantLevel > getItem().maxOlyEnch(_enchantLevel)) {
            return true;
        }

        return getItem().notForOly();
    }

    public boolean isPremiumItem() {
        return Config.PREMIUM_ITEMS_LIST.contains(_itemId);
    }
    /**
     * Return true if item isweapon-item
     *
     * @return boolean
     */
    /*
     * public boolean isWeaponItem() { switch (getItemType()) { case
     * L2WeaponType.BLUNT: case L2WeaponType.BOW: case L2WeaponType.DAGGER: case
     * L2WeaponType.DUAL: case L2WeaponType.DUALFIST: case L2WeaponType.ETC:
     * case L2WeaponType.FIST: case L2WeaponType.POLE: case L2WeaponType.SWORD:
     * case L2WeaponType.BIGSWORD: case L2WeaponType.PET: case L2WeaponType.ROD:
     * case L2WeaponType.BIGBLUNT: return true; break; default: return false; }
     }
     */

 /*
     * (non-Javadoc) @see
     * ru.agecold.gameserver.model.L2Object#onAction(ru.agecold.gameserver.model.L2PcInstance)
     * also check constraints: only soloing castle owners may pick up mercenary
     * tickets of their castle
     */
    private L2PcInstance _pickUpPriv = null;
    private long _pickUpTime = 0;

    public void setPickuper(L2PcInstance pickuper) {
        _pickUpPriv = pickuper;
        _pickUpTime = System.currentTimeMillis() + Config.PICKUP_PENALTY;
    }

    @Override
    public void onAction(L2PcInstance player) {
        //System.out.println("##onAction#1#");
        if (getLocation() != ItemLocation.VOID/*
                 * || _ownerId != 0
                 */) {
            //player.kick();
            return;
        }

        if (player.isAlikeDead()) {
            player.sendPacket(Static.CANT_LOOT_DEAD);
            return;
        }

        boolean canPickup = true;
        if (_pickUpPriv != null && _pickUpTime > System.currentTimeMillis()) {
            if (player.getParty() != null) {
                if (!player.getParty().getPartyMembers().contains(_pickUpPriv)) {
                    canPickup = false;
                }

                if (_pickUpPriv.getParty() != null && _pickUpPriv.getParty().isInCommandChannel() && !_pickUpPriv.getParty().getCommandChannel().getPartys().contains(player.getParty())) {
                    canPickup = false;
                }
            } else if (!player.equals(_pickUpPriv)) {
                canPickup = false;
            }
        }
        if (!canPickup) {
            player.sendActionFailed();
            player.sendPacket(SystemMessage.id(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(getItemId()));
            return;
        }
        //System.out.println("##onAction#2#");

        if ((_itemId == 8689 || _itemId == 8190) && (player.isMounted() || player.isInOlympiadMode() || player.getOlympiadGameId() > -1)) {
            player.sendActionFailed();
            return;
        }

        if (_mana == 0) {
            player.sendActionFailed();
            return;
        }

        // this causes the validate position handler to do the pickup if the location is reached.
        // mercenary tickets can only be picked up by the castle owner.
        if ((_itemId >= 3960 && _itemId <= 4021 && player.isInParty())
                || (_itemId >= 3960 && _itemId <= 3969 && !player.isCastleLord(1))
                || (_itemId >= 3973 && _itemId <= 3982 && !player.isCastleLord(2))
                || (_itemId >= 3986 && _itemId <= 3995 && !player.isCastleLord(3))
                || (_itemId >= 3999 && _itemId <= 4008 && !player.isCastleLord(4))
                || (_itemId >= 4012 && _itemId <= 4021 && !player.isCastleLord(5))
                || (_itemId >= 5205 && _itemId <= 5214 && !player.isCastleLord(6))
                || (_itemId >= 6779 && _itemId <= 6788 && !player.isCastleLord(7))
                || (_itemId >= 7973 && _itemId <= 7982 && !player.isCastleLord(8))
                || (_itemId >= 7918 && _itemId <= 7927 && !player.isCastleLord(9))) {
            if (player.isInParty()) //do not allow owner who is in party to pick tickets up
            {
                player.sendMessage("You cannot pickup mercenaries while in a party.");
            } else {
                player.sendMessage("Only the castle lord can pickup mercenaries.");
            }

            player.setTarget(this);
            player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
            player.sendActionFailed();
        } else {
            player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this);
        }
    }

    /**
     * Returns the level of enchantment of the item
     *
     * @return int
     */
    public int getEnchantLevel() {
        //return _enchantLevel;
        return Math.min(_enchantLevel, getMaxEnchant());
    }

    public int getMaxEnchant() {
        return getItem().getMaxEnchant();
    }

    /**
     * Sets the level of enchantment of the item
     *
     * @param int
     */
    public void setEnchantLevel(int enchantLevel, boolean game) {
        /*
         * if (_enchantLevel == enchantLevel || enchantLevel >= getMaxEnchant())
         * { return;
         }
         */
        _enchantLevel = enchantLevel;
        _storedInDb = false;
    }

    public void setEnchantLevel(int enchantLevel) {
        if (_enchantLevel == enchantLevel || enchantLevel > getMaxEnchant()) {
            return;
        }
        setEnchantLevel(enchantLevel, true);
    }

    /**
     * Returns the physical defense of the item
     *
     * @return int
     */
    public int getPDef() {
        if (_item instanceof L2Armor) {
            return ((L2Armor) _item).getPDef();
        }
        return 0;
    }

    /**
     * Returns whether this item is augmented or not
     *
     * @return true if augmented
     */
    public boolean isAugmented() {
        return _augmentation == null ? false : true;
    }

    /**
     * Returns the augmentation object for this item
     *
     * @return augmentation
     */
    public L2Augmentation getAugmentation() {
        return _augmentation;
    }

    /**
     * Sets a new augmentation
     *
     * @param augmentation
     * @return return true if sucessfull
     */
    public boolean setAugmentation(L2Augmentation augmentation) {
        // there shall be no previous augmentation..
        if (_augmentation != null) {
            return false;
        }
        _augmentation = augmentation;
        return true;
    }

    /**
     * Remove the augmentation
     *
     */
    public void removeAugmentation() {
        if (_augmentation == null) {
            return;
        }
        _augmentation.deleteAugmentationData();
        _augmentation = null;
    }

    /**
     * Used to decrease mana (mana means life time for shadow items)
     */
    public static class ScheduleConsumeManaTask implements Runnable {

        private L2ItemInstance _shadowItem;

        public ScheduleConsumeManaTask(L2ItemInstance item) {
            _shadowItem = item;
        }

        public void run() {
            try {
                // decrease mana
                if (_shadowItem != null) {
                    _shadowItem.decreaseMana(true);
                }
            } catch (Throwable t) {
            }
        }
    }

    /**
     * Returns true if this item is a shadow item Shadow items have a limited
     * life-time
     *
     * @return
     */
    public boolean isShadowItem() {
        return (_mana >= 0);
    }

    /**
     * Sets the mana for this shadow item <b>NOTE</b>: does not send an
     * inventory update packet
     *
     * @param mana
     */
    public void setMana(int mana) {
        _mana = mana;
    }

    /**
     * Returns the remaining mana of this shadow item
     *
     * @return lifeTime
     */
    public int getMana() {
        return _mana;
    }

    /**
     * Decreases the mana of this shadow item, sends a inventory update
     * schedules a new consumption task if non is running optionally one could
     * force a new task
     *
     * @param forces a new consumption task if item is equipped
     */
    public void decreaseMana(boolean resetConsumingMana) {
        if (!isShadowItem()) {
            return;
        }

        if (getLocation() != ItemLocation.PAPERDOLL) {
            return;
        }

        if (_mana > 0) {
            _mana--;
        }

        if (_storedInDb) {
            _storedInDb = false;
        }
        if (resetConsumingMana) {
            _consumingMana = false;
        }

        L2PcInstance player = L2World.getInstance().getPlayer(getOwnerId());
        if (player != null) {
            SystemMessage sm;
            switch (_mana) {
                /*
                 * case 60: player.sendCritMessage(getItemName() + ": ������� 1
                 * ���"); break; case 50: player.sendCritMessage(getItemName() +
                 * ": �������� 50 �����"); break; case 40:
                 * player.sendCritMessage(getItemName() + ": �������� 40
                 * �����"); break; case 30: player.sendCritMessage(getItemName()
                 * + ": �������� 30 �����"); break; case 20:
                 * player.sendCritMessage(getItemName() + ": �������� 20
                 * �����"); break; case 10: player.sendCritMessage(getItemName()
                 * + ": �������� 10 �����"); break; case 8:
                 * player.sendCritMessage(getItemName() + ": �������� 8 �����");
                 * break; case 5: player.sendCritMessage(getItemName() + ":
                 * �������� 5 �����"); break; case 3:
                 * player.sendCritMessage(getItemName() + ": �������� 3
                 * ������"); break; case 1: player.sendCritMessage(getItemName()
                 * + ": �������� 1 ������");
                 break;
                 */
                case 60:
                case 50:
                case 40:
                case 30:
                case 20:
                case 10:
                case 8:
                case 5:
                case 3:
                case 1:
                    player.sendCritMessage(getItemName() + ": " + _mana + " ����� �� ������������.");
                    break;
            }

            if (_mana == 0) // The life time has expired
            {
                player.sendCritMessage(getItemName() + " ���������");

                // unequip
                if (isEquipped()) {
                    player.getInventory().unEquipItemInSlot(getEquipSlot());
                }

                if (getLocation() != ItemLocation.WAREHOUSE) {
                    // destroy
                    player.getInventory().destroyItem("Shadow", this, player, null);

                    // send update
                    player.sendItems(false);
                } else {
                    player.getWarehouse().destroyItem("Shadow", this, player, null);
                }

                player.sendChanges();
                player.broadcastUserInfo();
                // delete from world
                L2World.getInstance().removeObject(this);
            } else {
                // Reschedule if still equipped
                if (!_consumingMana && isEquipped()) {
                    scheduleConsumeManaTask();
                }
                if (getLocation() != ItemLocation.WAREHOUSE) {
                    InventoryUpdate iu = new InventoryUpdate();
                    iu.addModifiedItem(this);
                    player.sendPacket(iu);
                }
            }
        }
    }

    private void scheduleConsumeManaTask() {
        _consumingMana = true;
        ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleConsumeManaTask(this), MANA_CONSUMPTION_RATE);
    }

    /**
     * Returns false cause item can't be attacked
     *
     * @return boolean false
     */
    @Override
    public boolean isAutoAttackable(L2Character attacker) {
        return false;
    }

    /**
     * Returns the type of charge with SoulShot of the item.
     *
     * @return int (CHARGED_NONE, CHARGED_SOULSHOT)
     */
    public int getChargedSoulshot() {
        return _chargedSoulshot;
    }

    /**
     * Returns the type of charge with SpiritShot of the item
     *
     * @return int (CHARGED_NONE, CHARGED_SPIRITSHOT,
     * CHARGED_BLESSED_SPIRITSHOT)
     */
    public int getChargedSpiritshot() {
        return _chargedSpiritshot;
    }

    public boolean getChargedFishshot() {
        return _chargedFishtshot;
    }

    /**
     * Sets the type of charge with SoulShot of the item
     *
     * @param type : int (CHARGED_NONE, CHARGED_SOULSHOT)
     */
    public void setChargedSoulshot(int type) {
        _chargedSoulshot = type;
    }

    /**
     * Sets the type of charge with SpiritShot of the item
     *
     * @param type : int (CHARGED_NONE, CHARGED_SPIRITSHOT,
     * CHARGED_BLESSED_SPIRITSHOT)
     */
    public void setChargedSpiritshot(int type) {
        _chargedSpiritshot = type;
    }

    public void setChargedFishshot(boolean type) {
        _chargedFishtshot = type;
    }

    /**
     * This function basically returns a set of functions from
     * L2Item/L2Armor/L2Weapon, but may add additional functions, if this
     * particular item instance is enhanched for a particular player.
     *
     * @param player : L2Character designating the player
     * @return Func[]
     */
    public Func[] getStatFuncs(L2Character player) {
        return getItem().getStatFuncs(this, player);
    }

    /**
     * Updates database.<BR><BR> <U><I>Concept : </I></U><BR>
     *
     * <B>IF</B> the item exists in database : <UL> <LI><B>IF</B> the item has
     * no owner, or has no location, or has a null quantity : remove item from
     * database</LI> <LI><B>ELSE</B> : update item in database</LI> </UL>
     *
     * <B> Otherwise</B> : <UL> <LI><B>IF</B> the item hasn't a null quantity,
     * and has a correct location, and has a correct owner : insert item in
     * database</LI> </UL>
     */
    public void updateDatabase() {
        updateDatabase(false);
    }

    public synchronized void updateDatabase(boolean commit) {
        if (isWear()) //avoid saving weared items
        {
            return;
        }

        if (_existsInDb) {
            if (_ownerId == 0 || _loc == ItemLocation.VOID || (_count == 0 && _loc != ItemLocation.LEASE)) {
                removeFromDb();
            } else if (isStackable()) {
                if (commit) {
                    // cancel lazy update task if need
                    if (stopLazyUpdateTask(true)) {
                        insertIntoDb(); // �� ������ ������...
                        return;
                    }
                    updateInDb();
                    return;
                }
                Future<?> lazyUpdateInDb = _lazyUpdateInDb;
                if (lazyUpdateInDb == null || lazyUpdateInDb.isDone()) {
                    _lazyUpdateInDb = ThreadPoolManager.getInstance().scheduleGeneral(new LazyUpdateInDb(this), 60000);
                }
            } else {
                updateInDb();
            }
        } else {
            if (_count == 0 && _loc != ItemLocation.LEASE) {
                return;
            }
            if (_loc == ItemLocation.VOID || _ownerId == 0) {
                return;
            }
            insertIntoDb();
        }
    }

    public boolean stopLazyUpdateTask(boolean interrupt) {
        boolean ret = false;
        if (_lazyUpdateInDb != null) {
            ret = _lazyUpdateInDb.cancel(interrupt);
            _lazyUpdateInDb = null;
        }
        return ret;
    }

    /**
     * Returns a L2ItemInstance stored in database from its objectID
     *
     * @param objectId : int designating the objectID of the item
     * @return L2ItemInstance
     */
    public static L2ItemInstance restoreFromDb(int objectId) {
        L2ItemInstance item = null;
        Connect con = null;
        PreparedStatement st = null;
        PreparedStatement st2 = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        try {
            con = L2DatabaseFactory.get();
            con.setTransactionIsolation(1);
            st = con.prepareStatement("SELECT owner_id, object_id, item_id, count, enchant_level, aug_id, aug_skill, aug_lvl, loc, loc_data, price_sell, price_buy, custom_type1, custom_type2, mana_left, end_time, time_of_use FROM items WHERE object_id = ? LIMIT 1");
            st.setInt(1, objectId);
            rs = st.executeQuery();
            if (rs.next()) {
                int owner_id = rs.getInt("owner_id");
                int item_id = rs.getInt("item_id");
                int count = rs.getInt("count");
                ItemLocation loc = ItemLocation.valueOf(rs.getString("loc"));
                int loc_data = rs.getInt("loc_data");
                int enchant_level = rs.getInt("enchant_level");
                int aug_id = rs.getInt("aug_id");
                int aug_skill = rs.getInt("aug_skill");
                int aug_lvl = rs.getInt("aug_lvl");
                int custom_type1 = rs.getInt("custom_type1");
                int custom_type2 = rs.getInt("custom_type2");
                int price_sell = rs.getInt("price_sell");
                int price_buy = rs.getInt("price_buy");
                int manaLeft = rs.getInt("mana_left");
                long endTime = rs.getLong("end_time");
                int timeOfUse = rs.getInt("time_of_use");
                L2Item template = ItemTable.getInstance().getTemplate(item_id);
                if (template == null) {
                    _log.severe("Item item_id=" + item_id + " not known, object_id=" + objectId);
                    return null;
                }
                item = new L2ItemInstance(objectId, template);

                item._timeOfUse = timeOfUse;
                if (item._timeOfUse > 0) {
                    item.removeFromDb();
                    return null;
                }

                item._existsInDb = true;
                item._storedInDb = true;
                item.setCountOnLoad(count);
                item._ownerId = owner_id;
                //item._count = count;
                item._enchantLevel = enchant_level;

                if (aug_id > 0) {
                    item._augmentation = new L2Augmentation(item, aug_id, aug_skill, aug_lvl, false);
                }

                item._type1 = custom_type1;
                item._type2 = custom_type2;
                item._loc = loc;
                item._locData = loc_data;
                item._priceSell = price_sell;
                item._priceBuy = price_buy;

                //��������� ������
                item._expire = endTime;
                if (item._expire > 0) {
                    if (System.currentTimeMillis() > item._expire) {
                        item.removeFromDb();
                        return null;
                    } else {
                        ExpiredItemsTaskManager.scheduleItem(owner_id, objectId, endTime);
                    }
                }

                // Setup life time for shadow weapons
                item._mana = manaLeft;

                // consume 1 mana
                if (item._mana > 0 && item.getLocation() == ItemLocation.PAPERDOLL) {
                    item.decreaseMana(false);
                }

                // if mana left is 0 delete this item
                if (item._mana == 0) {
                    item.removeFromDb();
                    return null;
                } else if (item._mana > 0 && item.getLocation() == ItemLocation.PAPERDOLL) {
                    item.scheduleConsumeManaTask();
                }

            } else {
                _log.severe("Item object_id=" + objectId + " not found");
                return null;
            }
        } catch (SQLException e) {
            _log.log(Level.SEVERE, "Could not restore item " + objectId + " from DB:", e);
        } finally {
            Close.CSR(con, st, rs);
        }
        return item;
    }

    /**
     * Init a dropped L2ItemInstance and add it in the world as a visible
     * object.<BR><BR>
     *
     * <B><U> Actions</U> :</B><BR><BR> <li>Set the x,y,z position of the
     * L2ItemInstance dropped and update its _worldregion </li> <li>Add the
     * L2ItemInstance dropped to _visibleObjects of its L2WorldRegion</li>
     * <li>Add the L2ItemInstance dropped in the world as a <B>visible</B>
     * object</li><BR><BR>
     *
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the
     * object to _allObjects of L2World </B></FONT><BR><BR>
     *
     * <B><U> Assert </U> :</B><BR><BR> <li> _worldRegion == null <I>(L2Object
     * is invisible at the beginning)</I></li><BR><BR>
     *
     * <B><U> Example of use </U> :</B><BR><BR> <li> Drop item</li> <li> Call
     * Pet</li><BR>
     *
     */
    public final void dropMe(L2Character dropper, int x, int y, int z) {
        z = 0;
        for (int i = 7; i > 0; i--) {
            switch (i) {
                case 1:
                    x += 60;
                    y += 60;
                    break;
                case 2:
                    x += 60;
                    y += 80;
                    break;
                case 3:
                    x += 59;
                    y -= 50;
                    break;
                case 4:
                    x += 50;
                    y -= 50;
                    break;
                case 5:
                    x -= 50;
                    y -= 50;
                    break;
                case 6:
                    x -= 50;
                    y += 60;
                    break;
                default:
                    x -= 50;
                    y += 60;
                    break;
            }
            if (GeoData.getInstance().canMoveFromToTarget(x, y, dropper.getZ(), dropper.getX(), dropper.getY(), dropper.getZ(), 1)) {
                z = dropper.getZ() + 10;
                break;
            }
        }

        if (z == 0) {
            x = dropper.getX();
            y = dropper.getY();
            z = GeoData.getInstance().getHeight(x, y, dropper.getZ()) + 10;
        }

        synchronized (this) {
            // Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion
            setIsVisible(true);
            getPosition().setWorldPosition(x, y, z);
            getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));

            // Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion
            getPosition().getWorldRegion().addVisibleObject(this);
        }
        setDropTime(System.currentTimeMillis());

        // this can synchronize on others instancies, so it's out of
        // synchronized, to avoid deadlocks
        // Add the L2ItemInstance dropped in the world as a visible object
        L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), dropper);
        if (Config.SAVE_DROPPED_ITEM) {
            ItemsOnGroundManager.getInstance().save(this);
        }
    }

    /**
     * Update the database with values of the item
     */
    private void updateInDb() {
        if (Config.ASSERT) {
            assert _existsInDb;
        }
        if (_wear) {
            return;
        }
        if (_storedInDb) {
            return;
        }

        if (getCount() <= 0) {
            return;
        }

        Connect con = null;
        PreparedStatement statement = null;
        try {
            con = L2DatabaseFactory.get();
            statement = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,price_sell=?,price_buy=?,custom_type1=?,custom_type2=?,mana_left=?,end_time=?,time_of_use=? WHERE object_id = ?");
            statement.setInt(1, _ownerId);
            statement.setInt(2, getCount());
            statement.setString(3, _loc.name());
            statement.setInt(4, _locData);
            statement.setInt(5, getEnchantLevel());
            statement.setInt(6, _priceSell);
            statement.setInt(7, _priceBuy);
            statement.setInt(8, getCustomType1());
            statement.setInt(9, getCustomType2());
            statement.setInt(10, getMana());
            statement.setLong(11, getExpire());
            statement.setInt(12, _timeOfUse);
            statement.setInt(13, getObjectId());
            statement.executeUpdate();
            _existsInDb = true;
            _storedInDb = true;
        } catch (SQLException e) {
            _log.log(Level.SEVERE, "Could not update item " + getObjectId() + " in DB: Reason: " + e);
            e.printStackTrace();
        } finally {
            Close.CS(con, statement);
        }
    }

    public void updateAdena(int newCount) {
        if (_storedInDb) {
            return;
        }

        Connect con = null;
        PreparedStatement statement = null;
        try {
            con = L2DatabaseFactory.get();
            statement = con.prepareStatement("UPDATE `items` SET `count`=? WHERE `object_id`=?");
            statement.setInt(1, newCount);
            statement.setInt(2, getObjectId());
            statement.executeUpdate();
            _existsInDb = true;
            _storedInDb = true;
        } catch (SQLException e) {
            _log.log(Level.SEVERE, "Could not update adena in DB" + e);
        } finally {
            Close.CS(con, statement);
        }
    }

    /**
     * Insert the item in database
     */
    private void insertIntoDb() {
        if (_wear) {
            return;
        }
        if (Config.ASSERT) {
            assert !_existsInDb && getObjectId() != 0;
        }
        Connect con = null;
        PreparedStatement statement = null;
        try {
            con = L2DatabaseFactory.get();
            statement = con.prepareStatement("REPLACE INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,aug_id,aug_skill,aug_lvl,price_sell,price_buy,object_id,custom_type1,custom_type2,mana_left,end_time,time_of_use) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statement.setInt(1, _ownerId);
            statement.setInt(2, _itemId);
            statement.setInt(3, getCount());
            statement.setString(4, _loc.name());
            statement.setInt(5, _locData);
            statement.setInt(6, getEnchantLevel());
            if (isAugmented()) {
                statement.setInt(7, getAugmentation().getAugmentationId());
                if (getAugmentation().getAugmentSkill() != null) {
                    statement.setInt(8, getAugmentation().getAugmentSkill().getId());
                    statement.setInt(9, getAugmentation().getAugmentSkill().getLevel());
                } else {
                    statement.setInt(8, -1);
                    statement.setInt(9, -1);
                }

            } else {
                statement.setInt(7, -1);
                statement.setInt(8, -1);
                statement.setInt(9, -1);
            }
            statement.setInt(10, _priceSell);
            statement.setInt(11, _priceBuy);
            statement.setInt(12, getObjectId());
            statement.setInt(13, _type1);
            statement.setInt(14, _type2);
            statement.setInt(15, getMana());

            if (_life > 0) {
                long expire = System.currentTimeMillis() + _life;
                statement.setLong(16, expire);
                setExpire(expire);
                //scheduleExpireTask(_life);
                ExpiredItemsTaskManager.scheduleItem(_ownerId, getObjectId(), expire);
            } else {
                statement.setLong(16, 0);
            }
            statement.setInt(17, _timeOfUse);
            
            statement.executeUpdate();

            _existsInDb = true;
            _storedInDb = true;
        } catch (SQLException e) {
            _log.log(Level.SEVERE, "Could not insert item " + getObjectId() + " into DB: Reason: " + e);
        } finally {
            Close.CS(con, statement);
        }
    }

    /**
     * Delete item from database
     */
    private void removeFromDb() {
        if (_wear) {
            return;
        }
        if (Config.ASSERT) {
            assert _existsInDb;
        }

        // cancel lazy update task if need
        stopLazyUpdateTask(true);

        Connect con = null;
        PreparedStatement statement = null;
        try {
            con = L2DatabaseFactory.get();
            statement = con.prepareStatement("DELETE FROM items WHERE object_id=?");
            statement.setInt(1, getObjectId());
            statement.executeUpdate();

            _existsInDb = false;
            _storedInDb = false;
        } catch (SQLException e) {
            _log.log(Level.SEVERE, "Could not delete item " + getObjectId() + " in DB:", e);
        } finally {
            Close.CS(con, statement);
        }
    }

    /**
     * Returns the item in String format
     *
     * @return String
     */
    @Override
    public String toString() {
        return "" + _item;
    }

    public void resetOwnerTimer() {
        if (itemLootShedule != null) {
            itemLootShedule.cancel(true);
        }
        itemLootShedule = null;
    }

    public void setItemLootShedule(ScheduledFuture<?> sf) {
        itemLootShedule = sf;
    }

    public ScheduledFuture<?> getItemLootShedule() {
        return itemLootShedule;
    }

    public void setProtected(boolean is_protected) {
        _protected = is_protected;
    }

    public boolean isProtected() {
        return _protected;
    }

    public boolean isNightLure() {
        return ((_itemId >= 8505 && _itemId <= 8513) || _itemId == 8485);
    }

    public void setCountDecrease(boolean decrease) {
        _decrease = decrease;
    }

    public boolean getCountDecrease() {
        return _decrease;
    }

    public void setInitCount(int InitCount) {
        _initCount = InitCount;
    }

    public int getInitCount() {
        return _initCount;
    }

    public void restoreInitCount() {
        if (_decrease) {
            _count = _initCount;
        }
    }

    public void setTime(int time) {
        if (time > 0) {
            _time = time;
        } else {
            _time = 0;
        }
    }

    public int getTime() {
        return _time;
    }

    /**
     * ������ ����� *
     */
    private boolean isLoggable(String method) {
        /*
         * if (method.equals("Adena") || method.equals("Consume") ||
         * method.equals("Arrow") || method.equals("Shot") ||
         * method.equals("Quest") || method.equals("Loot") ||
         * method.equals("Teleport") || method.equals("Skill") ||
         * method.equals("Multisell") || method.equals("CH_function_fee") ||
         * method.equals("RequestRefineCancel") ||
         * method.equals("RequestRefine") || method.equals("80lvl") ||
         * method.equals("Party") || method.equals("Enchant")) return false;
         */
        if (method.equals("Trade") || method.equals("WH finish") || method.equals("DieDrop") || method.equals("Pickup") || method.equals("depositwh") || method.equals("PrivateStore")) {
            return true;
        }

        return false;
    }

    /**
     * Return true if item can be enchanted
     *
     * @return boolean
     */
    public boolean canBeEnchanted() {
        if (isHeroItem() && !Config.ENCHANT_HERO_WEAPONS) {
            return false;
        }

        if (getItemType() == L2WeaponType.ROD) {
            return false;
        }

        if (_itemId == 8190 || _itemId == 8689) {
            return false;
        }

        if (isShadowItem()) {
            return false;
        }

        /**
         * TODO: fill conditions
         */
        return true;
    }

    // ��
    public void deleteMe() {
        removeFromDb();
        decayMe();
        L2World.getInstance().removeObject(this);
    }

    public void removeFromWorld() {
        L2World.getInstance().removeObject(this);
    }

    public void setWhFlag(boolean flag) {
        _whFlag = flag;
    }

    // ��������
    public boolean isLure() {
        switch (_itemId) {
            case 6519:
            case 6520:
            case 6521:
            case 6522:
            case 6523:
            case 6524:
            case 6525:
            case 6526:
            case 6527:
            case 7610:
            case 7611:
            case 7612:
            case 7613:
            case 7807:
            case 7808:
            case 7809:
            case 8484:
            case 8485:
            case 8486:
            case 8505:
            case 8506:
            case 8507:
            case 8508:
            case 8509:
            case 8510:
            case 8511:
            case 8512:
            case 8513:
                return true;
            default:
                return false;
        }
    }

    // �e����� � ����
    public boolean canBeStored(L2PcInstance player, boolean privatewh) {
        if (isEquipped()) {
            return false;
        }

        if (!privatewh && isShadowItem()) {
            return false;
        }

        if (!privatewh && isAugmented()) {
            return false;
        }

        if (getItem().getType2() == L2Item.TYPE2_QUEST) {
            return false;
        }

        if (isHeroItem()) {
            return false;
        }

        if (player.getPet() != null && getObjectId() == player.getPet().getControlItemId()) {
            return false;
        }

        if (_itemId == 8190 || _itemId == 8689) {
            return false;
        }

        if (isWear()) {
            return false;
        }

        if (player.getActiveEnchantItem() == this) {
            return false;
        }

        if (player.getCurrentSkill() != null && player.getCurrentSkill().getSkill().getItemConsumeId() == getItemId()) {
            return false;
        }

        if (Config.FORBIDDEN_WH_ITEMS.contains(_itemId)) {
            return false;
        }

        return privatewh || isTradeable();
    }
    // ��������� ������
    private long _expire = 0;
    private long _life = 0;

    public void setExpire(long time) {
        _expire = time;
    }

    public long getExpire() {
        return _expire;
    }

    public boolean isExpired() {
        return _expire == 555L;
    }

    public boolean canBeAugmented() {
        if (getItem().getItemGrade() < L2Item.CRYSTAL_C || getItem().getType2() != L2Item.TYPE2_WEAPON) {
            return false;
        }

        if (isShadowItem()) {
            return false;
        }

        if (isHeroItem()) {
            return Config.ALT_AUGMENT_HERO;
        }

        return isDestroyable();
    }

    @Override
    public boolean isL2Item() {
        return true;
    }

    public boolean notForBossZone() {
        if (getItem().isNotForBossZone()) {
            return true;
        }
        if (Config.BOSS_ZONE_MAX_ENCH == 0) {
            return false;
        }

        return (_enchantLevel > Config.BOSS_ZONE_MAX_ENCH);
    }

    public String getDisplayName() {
        String name = _item.getName();
        if (_enchantLevel > 0) {
            name = name + " +" + _enchantLevel;
        }
        if (_count > 0) {
            name = name + " (" + _count + ")";
        }

        return name;
    }

    public String getWeaponName() {
        String name = _item.getName();
        if (_enchantLevel > 0) {
            name = "+" + _enchantLevel + " " + name;
        }
        return name;
    }

    public boolean isBowWeapon() {
        return getItem().isBowWeapon();
    }

    public boolean isFistWeapon() {
        return getItem().isFistWeapon();
    }

    public boolean isDualWeapon() {
        return getItem().isDualWeapon();
    }
    private int _attribute = 0;
    private int _element = 0;

    public void setAttribute(int attr) {
        _attribute = attr;
    }

    public boolean incAttribute(int element) {
        _element = element;
        _attribute += 5;
        Connect con = null;
        PreparedStatement st = null;
        try {
            con = L2DatabaseFactory.get();
            if (_attribute == 5) {
                st = con.prepareStatement("INSERT INTO `z_elements_item` (`itemid`, `element`, `power`) VALUES (?,?,?)");
                st.setInt(1, getObjectId());
                st.setInt(2, _element);
                st.setInt(3, _attribute);
            } else {
                st = con.prepareStatement("UPDATE `z_elements_item` SET `power`=? WHERE `itemid`=?");
                st.setInt(1, _attribute);
                st.setInt(2, getObjectId());
            }
            st.execute();
        } catch (SQLException e) {
            _attribute -= 5;
            _log.severe("Could not update z_elements_item item " + getObjectId() + ": " + e);
            return false;
        } finally {
            Close.CS(con, st);
        }
        return true;
    }

    public void setElement(int elm) {
        _element = elm;
    }

    public int getAttribute() {
        return _attribute;
    }

    public int getElement() {
        return _element;
    }

    private String getElementName() {
        switch (_element) {
            case 1:
                return "����";
            case 2:
                return "�����";
            case 3:
                return "�����";
            case 4:
                return "�����";
            case 5:
                return "��������";
            case 6:
                return "����";
            default:
                return "";
        }
    }

    public int getAttrPower() {
        return _attribute;
    }

    public boolean checkForEquipped(L2PcInstance player) {
        if (player == null) {
            return true;
        }

        if ((Config.FORBIDDEN_EVENT_ITMES && player.isInEventChannel()) && _item.notForOly()) {
            return false;
        }
        return true;
    }

    private int _timeOfUse = 0;

    public void setTimeOfUse(int price) {
        _timeOfUse = price;
        //_storedInDb = false;
    }

    public int getTimeOfUse() {
        return _timeOfUse;
    }

    public void onActionShift(L2GameClient client)
    {
        L2PcInstance player = client.getActiveChar();
        if (player == null) {
            return;
        }
        if (!player.isGM()) {
            return;
        }
        String htm = "Item: " + getObjectId() + ", " + getItemName() + " (" + getCount() + ") +" + getEnchantLevel() + "<br>";
        htm += "Location: " + getLocation() + "<br>";
        htm += "Owner: " + getOwnerId() + "<br>";
        htm += "Player xyz: " + player.getX() + ", " + player.getY() + ", " + player.getZ() + "<br>";
        htm += "Item xyz: " + getX() + ", " + getY() + ", " + getZ() + "<br>";
        htm += "isVisible: " + isVisible() + "<br>";
        htm += "GeoData, can see: " + GeoData.getInstance().canSeeTarget(this, player) + "<br>";
        htm += "Radius, is in range 100: " + player.isInsideRadius(this, 100, false, false) + "<br>";
        htm += "<br>";
        htm += "<br>";
        htm += "<button value=\"�������\" action=\"bypass -h gmpickup " + getObjectId() + "\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">";

        player.sendHtmlMessage("item", htm);
        player.sendActionFailed();
    }
}
