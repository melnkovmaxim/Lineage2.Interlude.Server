package ai;

import javolution.util.FastList;

import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.ai.CtrlIntention;
import ru.agecold.gameserver.datatables.SkillTable;
import ru.agecold.gameserver.instancemanager.GrandBossManager;
import ru.agecold.gameserver.network.serverpackets.SocialAction;
import ru.agecold.util.Rnd;

import ru.agecold.gameserver.model.actor.instance.L2GrandBossInstance;
import ru.agecold.gameserver.model.actor.instance.L2MonsterInstance;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ai.Frintezza;
@SuppressWarnings("all")
public class Portrait extends QuestJython
{
	private static long _mobs = 60000;
	
	private static L2MonsterInstance g1 = null;
	private static L2MonsterInstance g2 = null;
	private static L2MonsterInstance g3 = null;
	private static L2MonsterInstance g4 = null;
	
	public Portrait(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		this.addSpawnId(29048);
		this.addKillId(29048);
	}
	
	@Override
    public String onSpawn(L2NpcInstance npc) 
	{ 
		//System.out.println("555555555555@@@@@@@@@@@@@@@@");
		ThreadPoolManager.getInstance().scheduleGeneral(new Ghosts(npc), 3000);
		return null; 
	} 
	
	@Override
	public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{ 
		//System.out.println("555555555553333333@@@@@@@@@@@@@@@@");
		Frintezza._prts -= 1;
		return null; 
	}
	
	//
	
	static class Ghosts implements Runnable
	{
		L2NpcInstance _npc;
		
		Ghosts(L2NpcInstance npc)
		{
			_npc = npc;
		}

		public void run()
		{			
			try
			{
				int x = _npc.getX();
				GrandBossManager gb = GrandBossManager.getInstance();	
				switch(x)
				{
					case 175882:
						g1 = (L2MonsterInstance)gb.createOnePrivateEx(29050, 175799, -88751, -5108, 4);
						g1.broadcastPacket(new SocialAction(g1.getObjectId(), 1));
						g2 = (L2MonsterInstance)gb.createOnePrivateEx(29050, 175801, -88593, -5108, 4);
						g3 = (L2MonsterInstance)gb.createOnePrivateEx(29051, 175729, -88678, -5108, 4);
						g3.broadcastPacket(new SocialAction(g1.getObjectId(), 2));
						g4 = (L2MonsterInstance)gb.createOnePrivateEx(29051, 175635, -88747, -5108, 4);
						break;
					case 175820:
						g1 = (L2MonsterInstance)gb.createOnePrivateEx(29050, 175695, -87108, -5108, 4);
						g1.broadcastPacket(new SocialAction(g1.getObjectId(), 1));
						g2 = (L2MonsterInstance)gb.createOnePrivateEx(29050, 175815, -87312, -5108, 4);
						g3 = (L2MonsterInstance)gb.createOnePrivateEx(29051, 175623, -87206, -5108, 4);
						g3.broadcastPacket(new SocialAction(g1.getObjectId(), 2));
						g4 = (L2MonsterInstance)gb.createOnePrivateEx(29051, 175697, -87325, -5108, 4);
						break;
					case 172629:
						g1 = (L2MonsterInstance)gb.createOnePrivateEx(29050, 172658, -87381, -5108, 4);
						g1.broadcastPacket(new SocialAction(g1.getObjectId(), 1));
						g2 = (L2MonsterInstance)gb.createOnePrivateEx(29050, 172835, -87308, -5108, 4);
						g3 = (L2MonsterInstance)gb.createOnePrivateEx(29051, 172914, -87207, -5108, 4);
						g3.broadcastPacket(new SocialAction(g1.getObjectId(), 2));
						g4 = (L2MonsterInstance)gb.createOnePrivateEx(29051, 172840, -86995, -5108, 4);
						break;
					case 172596:
						g1 = (L2MonsterInstance)gb.createOnePrivateEx(29050, 172613, -88539, -5108, 4);
						g1.broadcastPacket(new SocialAction(g1.getObjectId(), 1));
						g2 = (L2MonsterInstance)gb.createOnePrivateEx(29050, 172692, -88610, -5108, 4);
						g3 = (L2MonsterInstance)gb.createOnePrivateEx(29051, 172765, -88739, -5108, 4);
						g3.broadcastPacket(new SocialAction(g1.getObjectId(), 2));
						g4 = (L2MonsterInstance)gb.createOnePrivateEx(29051, 172699, -88811, -5108, 4);
						break;
				}
				
				try
				{
					Thread.sleep(1900);
				}
				catch (InterruptedException e)
				{
				}
				
				if (gb.getHalisha() != null)
				{
					if (Rnd.get(100) < 10)
					{
						FastList<L2PcInstance> trgs = new FastList<L2PcInstance>();
						trgs.addAll(gb.getHalisha().getKnownList().getKnownPlayersInRadius(1200));
						if (trgs.isEmpty())
							return;
							
						L2PcInstance trg = trgs.get(Rnd.get(trgs.size()-1));
						if (trg == null)
							return;
							
						g3.setTarget(trg);
						g3.doCast(SkillTable.getInstance().getInfo(5015,1));
					}
				}
			}
			catch (Throwable e){}
			ThreadPoolManager.getInstance().scheduleGeneral(new MobsCheck(), _mobs);
		}
	}
	
	static class MobsCheck implements Runnable
	{
		MobsCheck()
		{
		}

		public void run()
		{			
			try
			{
				L2GrandBossInstance halisha = GrandBossManager.getInstance().getHalisha();
				if (halisha != null)
				{
					FastList<L2PcInstance> trgs = new FastList<L2PcInstance>();
					trgs.addAll(halisha.getKnownList().getKnownPlayersInRadius(1200));
					if (!trgs.isEmpty())
					{
						L2PcInstance trg = null;
						GrandBossManager gb = GrandBossManager.getInstance();
						if (g1 == null)
						{
							g1 = (L2MonsterInstance)gb.createOnePrivateEx(29050, trg.getX(),trg.getY(),trg.getZ(), trg.getHeading());
							trg = trgs.get(Rnd.get(trgs.size()-1));
							if (trg != null)
								g1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, trg);
						}
						if (g2 == null)
						{
							g2 = (L2MonsterInstance)gb.createOnePrivateEx(29050, trg.getX(),trg.getY(),trg.getZ(), trg.getHeading());
							trg = trgs.get(Rnd.get(trgs.size()-1));
							if (trg != null)
								g2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, trg);
						}
						if (g3 == null)
						{
							g3 = (L2MonsterInstance)gb.createOnePrivateEx(29051, trg.getX(),trg.getY(),trg.getZ(), trg.getHeading());
							trg = trgs.get(Rnd.get(trgs.size()-1));
							if (trg != null)
								g3.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, trg);
						}
						if (g4 == null)
						{
							g4 = (L2MonsterInstance)gb.createOnePrivateEx(29051, trg.getX(),trg.getY(),trg.getZ(), trg.getHeading());
							trg = trgs.get(Rnd.get(trgs.size()-1));
							if (trg != null)
								g4.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, trg);
						}
						
						try
						{
							Thread.sleep(1900);
						}
						catch (InterruptedException e)
						{
						}
						
						if (Rnd.get(100) < 10)
						{
							trg = trgs.get(Rnd.get(trgs.size()-1));
							if (trg != null)
							{
								g3.setTarget(trg);
								g3.doCast(SkillTable.getInstance().getInfo(5015,1));
							}
						}
					}
				}
			}
			catch (Throwable e){}
			ThreadPoolManager.getInstance().scheduleGeneral(new MobsCheck(), _mobs);
		}
	}
	
	//
	
	public static void main (String... arguments )
	{
		new Portrait(-1,"Portrait","Portrait");
	}
}
