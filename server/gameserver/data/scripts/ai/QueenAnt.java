package ai;

import javolution.util.FastList;

import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.instancemanager.GrandBossManager;
import ru.agecold.gameserver.instancemanager.GrandBossManager.BossInfo;
import ru.agecold.gameserver.model.actor.instance.L2GrandBossInstance;
import ru.agecold.gameserver.model.actor.instance.L2MonsterInstance;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.gameserver.network.serverpackets.CharMoveToLocation;
import ru.agecold.gameserver.network.serverpackets.SocialAction;
import ru.agecold.gameserver.util.Util;
import ru.agecold.util.Rnd;
 
public class QueenAnt extends QuestJython
{
	private static final int BOSS = 29001;
	private static long _lastLavaHit;
	private static boolean _call = false;
	private static L2GrandBossInstance _aq = null;
	private static L2MonsterInstance _larva = null;
	private static FastList<L2MonsterInstance> _nurses = new FastList<L2MonsterInstance>();
	
	public QueenAnt(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		this.addSpawnId(BOSS);
		this.addKillId(BOSS);
		//this.addAttackId(29001);
		this.addAttackId(29002);
		this.addKillId(29002);
		this.addKillId(29003);
	}
	
	@Override
	protected void init_LoadGlobalData()
	{
		GrandBossManager gb = GrandBossManager.getInstance();	
		BossInfo bi = getInfo();
	    if (bi._status == 0)
		{
			long temp = bi._resDate - System.currentTimeMillis();
			if (temp > 0)
			{
				if (temp < 1300000)
					temp = 1300000;
				ThreadPoolManager.getInstance().scheduleGeneral(new Respawn(), temp); 
			}
			else
			{
				gb.setStatus(BOSS, 1);
				gb.setRespawn(BOSS, 0);
				ThreadPoolManager.getInstance().scheduleGeneral(new Respawn(), 1300000); 
			}
		}	
	    else
		{
			gb.setStatus(BOSS, 1);
			gb.setRespawn(BOSS, 0);
			ThreadPoolManager.getInstance().scheduleGeneral(new Respawn(), 1300000); 
		}
	}
	
	@Override
    public String onSpawn(L2NpcInstance npc) 
	{ 
		GrandBossManager gb = GrandBossManager.getInstance();	
		_aq = (L2GrandBossInstance)npc;
		_aq.setRunning();
		gb.setStatus(BOSS, 0);
		gb.setRespawn(BOSS, 0);
		
		cleanNest();
		createPrivates();
		_larva = (L2MonsterInstance)gb.createOnePrivateEx(29002, -21600, 179482, -5846, Rnd.get(65535));
		_larva.setIsImobilised(true);
		//_larva.setIsInvul(true);
		ThreadPoolManager.getInstance().scheduleGeneral(new Timer(), 10000);
		ThreadPoolManager.getInstance().scheduleGeneral(new Heal(), 5000);
		return null; 
	} 
	
	@Override
    public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet) 
	{ 
		//if (npc.getNpcId() == 29002)
			_lastLavaHit = System.currentTimeMillis();
		return null; 
	} 
	
	@Override
	public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{ 
		GrandBossManager gb = GrandBossManager.getInstance();
		switch(npc.getNpcId())
		{
			case 29001:
				_aq = null;
				if (_larva != null)
					_larva.deleteMe();
				_larva = null;
				L2MonsterInstance nurse = null;
				for (FastList.Node<L2MonsterInstance> n = _nurses.head(), end = _nurses.tail(); (n = n.getNext()) != end;)
				{
					nurse = n.getValue();
					if (nurse == null)
						continue;
					nurse.deleteMe();
				}
				_nurses.clear();
				BossInfo bi = getInfo();
				//if (bi._resDate > 0)
				//	return null;
				long resp = (System.currentTimeMillis() + Rnd.get((int)bi._resMin,(int)bi._resMax));
				gb.setStatus(BOSS, 0);
				gb.setRespawn(BOSS, resp);
				ThreadPoolManager.getInstance().scheduleGeneral(new Respawn(), resp);
				break;
			case 29003:
				_nurses.remove((L2MonsterInstance)npc);
				ThreadPoolManager.getInstance().scheduleGeneral(new RespawnNurse(), 10000);
				break;
			case 29002:
				_larva = null;
				ThreadPoolManager.getInstance().scheduleGeneral(new RespawnLarva(), 60000);
				break;
		}
		return null; 
	}
	
	//	
	private static void cleanNest()
	{
		FastList<L2PcInstance> players = _aq.getKnownList().getKnownPlayersInRadius(2500);
		L2PcInstance pc = null;
		for (FastList.Node<L2PcInstance> n = players.head(), end = players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			if(Rnd.get(100) < 33)
				pc.teleToLocation(-19480, 187344, -5600);	
			else if(Rnd.get(100) < 50)
				pc.teleToLocation(-17928, 180912, -5520);	
			else
				pc.teleToLocation(-23808, 182368, -5600);
		}
	}
			
	private static void createPrivates()
	{
		L2MonsterInstance nurse = null;
		GrandBossManager gb = GrandBossManager.getInstance();
		for (int i = 7; i > -1; i--)
		{
			nurse = (L2MonsterInstance)gb.createOnePrivateEx(29003, _aq.getX()+Rnd.get(150), _aq.getY()+Rnd.get(150), _aq.getZ(), Rnd.get(65535));
			_nurses.add(nurse);
		}
	}
	
	static class Timer implements Runnable
	{
		Timer()
		{
		}

		public void run()
		{			
			//try
			//{
				if (_aq == null)
					return;

				if(Rnd.get(100) < 1)
					_aq.broadcastPacket(new SocialAction(_aq.getObjectId(), 1));
				else if(Rnd.get(100) < 10)
					_aq.addUseSkillDesire(4017,1);
				else if(Rnd.get(100) < 20)
					_aq.addUseSkillDesire(4019,1);
				else if(Rnd.get(100) < 30)
				{
					if(Rnd.get(100) < 50)
						_aq.broadcastPacket(new SocialAction(_aq.getObjectId(), 3));
					else
						_aq.broadcastPacket(new SocialAction(_aq.getObjectId(), 4));
				}
				else if(Rnd.get(100) < 70)
					_aq.addUseSkillDesire(4018,1);
			//}
			//catch (Throwable e){}
			ThreadPoolManager.getInstance().scheduleGeneral(new Timer(), 10000);
		}
	}
	
	static class Heal implements Runnable
	{
		Heal()
		{
		}

		public void run()
		{		
			if (_aq == null)
				return;
					
			if (_call)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new Heal(), 5000);
				return;
			}		
			
			//try
			//{
				L2MonsterInstance nurse = null;
				for (FastList.Node<L2MonsterInstance> n = _nurses.head(), end = _nurses.tail(); (n = n.getNext()) != end;)
				{
					nurse = n.getValue();
					if (nurse == null)
						continue;
					nurse.setRunning();
					if(_larva != null && System.currentTimeMillis() - _lastLavaHit < 2000)
					{
						_call = true;
						nurse.moveToLocationm(-21559+Rnd.get(100),179795+Rnd.get(100), -5834, 0);
						nurse.broadcastPacket(new CharMoveToLocation(nurse));
						if (Util.checkIfInRange(400, nurse, _larva, false)) 
						{
							nurse.setTarget(_larva);
							nurse.addUseSkillDesire(4020,1);
							_call = false;
						}
						else
							ThreadPoolManager.getInstance().scheduleGeneral(new HealLarva(), 5000);
					}
					else if(_aq.getCurrentHp() < _aq.getMaxHp())
					{
						nurse.moveToLocationm(_aq.getX()+Rnd.get(250), _aq.getY()+Rnd.get(250), _aq.getZ(), 0);
						nurse.broadcastPacket(new CharMoveToLocation(nurse));
						nurse.setTarget(_aq);
						nurse.addUseSkillDesire(4024,1);
					}
				}
			//}
			//catch (Throwable e){}
			ThreadPoolManager.getInstance().scheduleGeneral(new Heal(), 5000);
		}
	}
	
	static class HealLarva implements Runnable
	{
		HealLarva()
		{
		}

		public void run()
		{		
			if (_aq == null || _larva == null)
				return;	
			
			//try
			//{
				L2MonsterInstance nurse = null;
				for (FastList.Node<L2MonsterInstance> n = _nurses.head(), end = _nurses.tail(); (n = n.getNext()) != end;)
				{
					nurse = n.getValue();
					if (nurse == null)
						continue;
					nurse.setRunning();
					nurse.setTarget(_larva);
					nurse.addUseSkillDesire(4020,1);
				}
			//}
			//catch (Throwable e){}
			_call = false;
		}
	}
	
	static class RespawnNurse implements Runnable
	{
		RespawnNurse()
		{
		}

		public void run()
		{			
			//try
			//{
				if (_aq == null)
					return;
					
				if (_nurses.size() > 8)
					return;

				L2MonsterInstance nurse = (L2MonsterInstance)GrandBossManager.getInstance().createOnePrivateEx(29003, _aq.getX()+Rnd.get(80), _aq.getY()+Rnd.get(80), _aq.getZ(), Rnd.get(65535));
				_nurses.add(nurse);
			//}
			//catch (Throwable e){}
		}
	}
	
	static class Respawn implements Runnable
	{
		Respawn()
		{
		}

		public void run()
		{			
			//try
			//{
				if (_aq != null)
					return;
				GrandBossManager gb = GrandBossManager.getInstance();
				L2GrandBossInstance boss = (L2GrandBossInstance)GrandBossManager.getInstance().createOnePrivateEx(BOSS, -21468, 181638, -5720, Rnd.get(65535));
				boss.setRunning();
				gb.setStatus(BOSS, 1);
				gb.setRespawn(BOSS, 0);
				return;
			//}
			//catch (Throwable e){}
		}
	}
	
	static class RespawnLarva implements Runnable
	{
		RespawnLarva()
		{
		}

		public void run()
		{			
			//try
			//{	
				_larva = (L2MonsterInstance)GrandBossManager.getInstance().createOnePrivateEx(29002, -21600, 179482, -5846, Rnd.get(65535));
				_larva.setIsImobilised(true);
			//}
			//catch (Throwable e){}
		}
	}
	
	private static BossInfo getInfo()
	{
		return GrandBossManager.getInstance().get(BOSS);
	}
	//
	
	public static void main (String... arguments )
	{
		new QueenAnt(-1,"QueenAnt","QueenAnt");
	}
}
