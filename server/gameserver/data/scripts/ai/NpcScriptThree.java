package ai;

import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.gameserver.Announcements;

public class NpcScriptThree extends QuestJython
{
	//Ид боссов
	private final static int[] BossIds = {45114,29001,29028,29020,29019,25163,5004,50032,75039,15106};

	public NpcScriptThree(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		for (int BossIds1 : BossIds)
		  addSpawnId(BossIds1);
	}
	
    public String onSpawn(L2NpcInstance raidboss)
    {
        int npcId = raidboss.getNpcId();
				for (int id : BossIds)
					if (npcId == id)
    		            Announcements.getInstance().announceToAll("РейдБосс : " + raidboss.getName() + " появился в мире!");
        return null;
    }

	public static void main(String[] args)
	{
		new NpcScriptThree(-1, "NpcScriptThree", "ai");
	}
}