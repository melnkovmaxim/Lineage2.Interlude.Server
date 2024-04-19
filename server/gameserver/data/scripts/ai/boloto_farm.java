package ai;

import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.util.Rnd;
@SuppressWarnings("all")
public class boloto_farm extends QuestJython
{
	//мобы которых можно убивать
    private final static int newbay_mobs = 29065;
	//ID сертификата новичка
	private final static int newbay_item = 13020;
	//Включить или выключить дополнительный дроп
	private final static boolean Bool = true;	
	//ID фарм монет
	private final static int Farm_Coin = 13022;
	//Сколько давать за убитого моба
	//От Drop_Count_Min до Drop_Count_Max
	private final static int Drop_Count_Min = 1;
	private final static int Drop_Count_Max = 1;
	
	//Что говорит НПЦ если игрок без статуса новичка?
	private final static String mob_massage = "Я не уязвим!, Пройди квест!!!";

	public boloto_farm(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		this.addFocusId(newbay_mobs);
		this.addAttackId(newbay_mobs);
		this.addKillId(newbay_mobs);
	}
	
	public String onFocus(L2NpcInstance npc, L2PcInstance attacker)
	{
		int count = attacker.getInventory().getInventoryItemCount(newbay_item,0);
		if(count == 0)
			{
				npc.sayString(mob_massage, 0);
			}
		else
			npc.setIsInvul(false);
		return null; 
	}
    public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet) 
	{
		int count = attacker.getInventory().getInventoryItemCount(newbay_item,0);
		if(count == 0)
			npc.setIsInvul(true);
		else
			npc.setIsInvul(false);
		return null; 	
	}

	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{ 	
		int nagrada = Rnd.get(Drop_Count_Min,Drop_Count_Max);
		int count = killer.getInventory().getInventoryItemCount(newbay_item,0);
		if(count >= 1 && Bool == true)
		{
			killer.giveItem(Farm_Coin,nagrada);
		}
		return null; 
	}






	public static void main(String... arguments )
	{
		new boloto_farm(-1, "boloto_farm", "boloto_farm");
	}
}
