package commands.voice;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PlayableInstance;
import ru.agecold.gameserver.network.serverpackets.NpcHtmlMessage;
import scripts.commands.IVoicedCommandHandler;
import scripts.commands.VoicedCommandHandler;
import scripts.items.ItemHandler;
import scripts.items.IItemHandler;
import java.util.logging.Logger;
import ru.agecold.util.Rnd;
import ru.agecold.gameserver.Announcements;
import ru.agecold.gameserver.datatables.ItemTable;

public class VipBag implements IVoicedCommandHandler, IItemHandler
{
	private final static int ITEM_ACTIVE_ID = 12001; //Ид предмета активации.
	
    private final static boolean VB_HERO = true; // Включить выдачу Геройства.
	private final static int VB_HERO_CHANCE = 70; // Шанс получения Геройства.
	private final static int VB_HERO_DAYS = 1; // На сколько дней выдавать Геройство.
		
	private final static boolean VB_PA = true; // Включить выдачу Премиум Аккаунта.
	private final static int VB_PA_CHANCE = 70; // Шанс получения Премиум Аккаунта.
	private final static int VB_PA_DAYS = 1; // На сколько дней выдавать Премиум Аккаунта.
		
	private final static boolean VB_ARMOR = true; // Включить выдачу одной из Брони по рандуму.
	private final static int VB_ARMOR_CHANCE = 10; // Шанс получения Брони.
	private static final int VB_ARMOR_IDS[] = {11221,11222,11223,11224,11225}; // ИДы Брони указывать через запятую.
	
	private final static boolean VB_COL = true; // Включить выдачу Монеты.
	private final static int VB_COL_CHANCE = 30; // Шанс получения Монет.
	private final static int VB_COL_ID = 12222; // ИД Монет.
	private final static int VB_COL_COUNT = 5; // Количество Монет.
	
	private final static boolean VB_TATOO = true; // Включить выдачу Донат Тату.
	private final static int VB_TATOO_CHANCE = 5; // Шанс получения Донат Тату.
	private final static int VB_TATOO_ID = 13336; // ИД Донат Тату.
	
	
	
	private static final Logger _log = Logger.getLogger(VipBag.class.getName());
	private static int ITEM_IDS[] = {ITEM_ACTIVE_ID};
	public VipBag()
	{
		_log.info("");
		_log.info("      #############################################################");
		_log.info("      #############  For LA2-ARES.PW  ##############");
		_log.info("      #############################################################");
		_log.info("");
		ItemHandler.getInstance().registerItemHandler(this);
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}
	
	public boolean useVoicedCommand(String command, L2PcInstance player, String target)
    {
        if(command.startsWith("ag_vipbag_"))
        {
            String choise = command.substring(10).trim();
            if(choise.startsWith("edit"))
            {
			    int chance = Rnd.get(100);
		        L2ItemInstance coin = player.getInventory().getItemByItemId(ITEM_IDS[0]);
                int flag = Integer.parseInt(choise.substring(4).trim());
				//_log.info("hello2: "+flag+"");
				if (coin != null && coin.getCount() > 0)
				{
					if(flag == 1 && VB_HERO != false)
					{
						if (player.isHero() != false)
						{
							player.sendMessage("Вы Уже имеете Геройство."); 
							showWelcome(player);
						}
						else
						{
							if (chance < VB_HERO_CHANCE)    
							{
								player.setHero(VB_HERO_DAYS);
								Announcements.getInstance().announceToAll("Игрок " + player.getName() + " везунчик и получил шанс испытать удачу он выбрал \"Геройство\" и ему повезло!");
								player.destroyItemByItemId("ag_vipbag_", ITEM_IDS[0], 1, player, true);
							}
							else
							{
								Announcements.getInstance().announceToAll("Игрок " + player.getName() + " везунчик и получил шанс испытать удачу он выбрал \"Геройство\" и ему не повезло");
								player.destroyItemByItemId("ag_vipbag_", ITEM_IDS[0], 1, player, true);
							}
						}
					}
					if(flag == 2 && VB_PA != false)
					{
						if (player.isPremium() != false)
						{
							player.sendMessage("Вы Уже имеете Премиум Аккаунт.");
							showWelcome(player);
						}
						else
						{
							if (chance < VB_PA_CHANCE)    
							{
								player.storePremium(VB_PA_DAYS);
								Announcements.getInstance().announceToAll("Игрок " + player.getName() + " везунчик и получил шанс испытать удачу он выбрал \"Премиум Аккаунт\" и ему повезло!");
								player.destroyItemByItemId("ag_vipbag_", ITEM_IDS[0], 1, player, true);
							}
							else
							{
								Announcements.getInstance().announceToAll("Игрок " + player.getName() + " везунчик и получил шанс испытать удачу он выбрал \"Премиум Аккаунт\" и ему не повезло");
								player.destroyItemByItemId("ag_vipbag_", ITEM_IDS[0], 1, player, true);
							}
						}
					}
					if(flag == 3 && VB_ARMOR != false)
					{
						if (chance < VB_ARMOR_CHANCE)    
						{
							int armor_id = Rnd.get(VB_ARMOR_IDS.length);
							String _ItemName = ItemTable.getInstance().getItemName(VB_ARMOR_IDS[armor_id]);
						    player.addItem("ag_vipbag_", VB_ARMOR_IDS[armor_id], 1, player, true);
						    Announcements.getInstance().announceToAll("Игрок " + player.getName() + " везунчик и получил шанс испытать удачу он выбрал \"Топ Бижа\" и он получил "+_ItemName+"!");
							player.destroyItemByItemId("ag_vipbag_", ITEM_IDS[0], 1, player, true);
						}
						else
						{
							Announcements.getInstance().announceToAll("Игрок " + player.getName() + " везунчик и получил шанс испытать удачу он выбрал \"Топ Бижа\" и ему не повезло");
							player.destroyItemByItemId("ag_vipbag_", ITEM_IDS[0], 1, player, true);
						}
					}
					if(flag == 4 && VB_COL != false)
					{
						if (chance < VB_COL_CHANCE)    
						{
							String _ItemName = ItemTable.getInstance().getItemName(VB_COL_ID);
						    player.addItem("ag_vipbag_", VB_COL_ID, VB_COL_COUNT, player, true);
						    Announcements.getInstance().announceToAll("Игрок " + player.getName() + " везунчик и получил шанс испытать удачу он выбрал \"Donate Монеты\" и он получил "+_ItemName+" "+VB_COL_COUNT+" штук!");
							player.destroyItemByItemId("ag_vipbag_", ITEM_IDS[0], 1, player, true);
						}
						else
						{
							Announcements.getInstance().announceToAll("Игрок " + player.getName() + " везунчик и получил шанс испытать удачу он выбрал \"Donate Монету\" и ему не повезло");
							player.destroyItemByItemId("ag_vipbag_", ITEM_IDS[0], 1, player, true);
						}
					}
					if(flag == 5 && VB_TATOO != false)
					{
						if (chance < VB_TATOO_CHANCE)    
						{
							String _ItemName = ItemTable.getInstance().getItemName(VB_TATOO_ID);
						    player.addItem("ag_vipbag_", VB_TATOO_ID, 1, player, true);
						    Announcements.getInstance().announceToAll("Игрок " + player.getName() + " везунчик и получил шанс испытать удачу он выбрал \"Донат Тату\" и он получил "+_ItemName+"!");
							player.destroyItemByItemId("ag_vipbag_", ITEM_IDS[0], 1, player, true);
						}
						else
						{
							Announcements.getInstance().announceToAll("Игрок " + player.getName() + " везунчик и получил шанс испытать удачу он выбрал \"Донат Тату\" и ему не повезло");
							player.destroyItemByItemId("ag_vipbag_", ITEM_IDS[0], 1, player, true);
						}
					}
				}
				else
				{
					player.sendMessage("Не достаточное количество предметов!");	
				}
				
				return true;
					
			}
			else
			{
				return false;
			}
        }
        return true;
    }

    private void showWelcome(L2PcInstance player)
    {
        if(player.isParalyzed() || player.getUserKey().on == 1)
        {
            player.sendActionFailed();
            return;
        }
        NpcHtmlMessage nhm = NpcHtmlMessage.id(5);
        String build = "<html><body><center>";
		build += "Получить приз!<br1>";
        if(VB_HERO)
        {
		    build +=("<button value=\"Геройство на сутки 70%\" action=\"bypass -h vch_ag_vipbag_edit 1\" width=135 height=24 back=\"L2UI_CH3.bigbutton3_down\" fore=\"L2UI_CH3.bigbutton3\"><br1>");
        }
        if(VB_PA)
        {
		    build +=("<button value=\"Па на сутки 70%\" action=\"bypass -h vch_ag_vipbag_edit 2\" width=135 height=24 back=\"L2UI_CH3.bigbutton3_down\" fore=\"L2UI_CH3.bigbutton3\"><br1>");
        }
        if(VB_ARMOR)
        {
		    build +=("<button value=\"Любая Топ Бижа 25%\" action=\"bypass -h vch_ag_vipbag_edit 3\" width=135 height=24 back=\"L2UI_CH3.bigbutton3_down\" fore=\"L2UI_CH3.bigbutton3\"><br1>");
        }
        if(VB_COL)
        {
		    build +=("<button value=\"5 Donate Coin 40%\" action=\"bypass -h vch_ag_vipbag_edit 4\" width=135 height=24 back=\"L2UI_CH3.bigbutton3_down\" fore=\"L2UI_CH3.bigbutton3\"><br1>");
        }
        if(VB_TATOO)
        {
		    build +=("<button value=\"Донат Тату 10%\" action=\"bypass -h vch_ag_vipbag_edit 5\" width=135 height=24 back=\"L2UI_CH3.bigbutton3_down\" fore=\"L2UI_CH3.bigbutton3\"><br1>");
        }
		build +=("</center></body></html>");
        nhm.setHtml(build.toString());
        player.sendPacket(nhm);
        build = null;
    }

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
   	{
		if (!playable.isPlayer())
			return;

		L2PcInstance player = (L2PcInstance) playable;
		showWelcome(player);	
   	}


	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

    private static final String VOICED_COMMANDS[] = {
		"ag_","ag_vipbag_"
    };
	
	public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
	
	public static void main (String... arguments )
	{
		new VipBag();
	}
}
