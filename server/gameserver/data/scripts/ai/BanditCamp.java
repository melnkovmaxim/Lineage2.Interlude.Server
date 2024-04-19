package ai;

import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import scripts.clanhalls.BanditStronghold;
 
public class BanditCamp extends QuestJython
{
	private static final int[] CAMPS = {35423,35424,35425,35426,35427};
	private static final int[] BOSSES = {35428,35429,35430,35431,35432};
	private L2NpcInstance camp = null;
	public BanditCamp(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		for (int i: CAMPS)
		{
			this.addSpawnId(i);
		}
		for (int k: BOSSES)
		{
			this.addKillId(k);
		}
	}
	
    public String onSpawn(L2NpcInstance npc) 
	{ 
		camp = npc;
		ThreadPoolManager.getInstance().scheduleGeneral(new CheckSiege(), 5000);
		return null; 
	} 
	
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{ 
		BanditStronghold.getCH().notifyDeath(npc.getObjectId());
		return null; 
	}
	
	public class CheckSiege implements Runnable
	{
		public CheckSiege()
		{
		}
		
		public void run()
		{	
			for (int i = 0; i < 7200000; i += 3000)
			{
				try
				{
					Thread.sleep(3000);
					if (BanditStronghold.getCH().inProgress())
						break;
				}
				catch (InterruptedException e)
				{
				}
			}
			camp.deleteMe();
		}
	}
	
	public static void main (String... arguments )
	{
		new BanditCamp(-1,"BanditCamp","BanditCamp");
	}
}
