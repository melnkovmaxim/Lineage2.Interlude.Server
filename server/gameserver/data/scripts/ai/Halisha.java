package ai;

import javolution.util.FastList;

import ru.agecold.Config;
import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.instancemanager.GrandBossManager;
import ru.agecold.gameserver.instancemanager.GrandBossManager.BossInfo;
import ru.agecold.gameserver.model.actor.instance.L2GrandBossInstance;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.gameserver.network.serverpackets.SocialAction;
import ru.agecold.util.Rnd;
import ai.Frintezza;
 
public class Halisha extends QuestJython
{
	private static final long _hpChk = 5000;
	private static final long _sklChk = 18000;
	//private long _trgChk = 40000;

	private static L2GrandBossInstance _halisha = null;
	private static final int BOSS = 29045;
	private static boolean _trans = false;
	private static boolean _lastStage = false;
	
	public Halisha(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		this.addSpawnId(29046);
		this.addKillId(29046);
		this.addSpawnId(29047);
		this.addKillId(29047);
	}
	
	@Override
    public String onSpawn(L2NpcInstance npc) 
	{ 
		//System.out.println("@@@@@@@@@@@@@@@@");
		npc.setRunning();
		switch (npc.getNpcId())
		{
			case 29046:
				if(_lastStage && _halisha != null)
				{
					npc.decayMe();
					npc.deleteMe();
					return null;
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new ChkHp(), _hpChk);
				//ThreadPoolManager.getInstance().scheduleGeneral(new ChkTrg(), _trgChk);
				ThreadPoolManager.getInstance().scheduleGeneral(new ChkSkl(), _sklChk);
				break;
			case 29047:
				//ThreadPoolManager.getInstance().scheduleGeneral(new ChkTrg(), _trgChk);
				ThreadPoolManager.getInstance().scheduleGeneral(new ChkSkl(), _sklChk);
				break;
		}
		_halisha = (L2GrandBossInstance)npc;
		GrandBossManager.getInstance().setHalisha(_halisha);
		return null; 
	} 
	
	@Override
	public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{ 
		//System.out.println("222222222222222@@@@@@@@@@@@@@@@");
		GrandBossManager gb = GrandBossManager.getInstance();	
		switch (npc.getNpcId())
		{	
			case 29046:
				_lastStage = true;
				_halisha.broadcastPacket(new SocialAction(_halisha.getObjectId(), 2));
				_halisha.getTemplate().setRhand(8204);
				try
				{
					Thread.sleep(5500);
				}
				catch (InterruptedException e)
				{
				}
				_halisha.decayMe();
				_halisha.deleteMe();
				_halisha = (L2GrandBossInstance)gb.createOnePrivateEx(29047, _halisha.getX(), _halisha.getY(), _halisha.getZ(),0);
				break;
			case 29047:
				BossInfo bi = getInfo();
				if (bi.respawn == 0)
				{
					GrandBossManager.getInstance().setRespawn(29045, (System.currentTimeMillis() + (Rnd.get((int) Config.VALAKAS_MIN_RESPAWN, (int) Config.VALAKAS_MAX_RESPAWN))));
				}
				
				L2GrandBossInstance _frint = gb.getFrintezza();
				if (_frint != null)
				{
					FastList<L2PcInstance> _players = _frint.getKnownList().getKnownPlayersInRadius(1200);
					_players.addAll(_halisha.getKnownList().getKnownPlayersInRadius(1200));
					if (!_players.isEmpty())
					{
						L2PcInstance pc = null;
						for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
						{
							pc = n.getValue();
							if (pc == null)
								continue;
							pc.enterMovieMode(_frint);
							pc.specialCamera(_frint, 200, 90, 0, 3000, 5000);
						}
						
						try
						{
							Thread.sleep(1900);
						}
						catch (InterruptedException e)
						{
						}
						
						for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
						{
							pc = n.getValue();
							if (pc == null)
								continue;
							pc.specialCamera(_frint, 10, 70, -10, 20000, 10000);
						}
						try
						{
							Thread.sleep(1000);
						}
						catch (InterruptedException e)
						{
						}
						for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
						{
							pc = n.getValue();
							if (pc == null)
								continue;
							_frint.broadcastPacket(new SocialAction(_frint.getObjectId(), 4));
							pc.specialCamera(_frint, 50, 190, 40, 15000, 12000);
						}
						try
						{
							Thread.sleep(2900);
						}
						catch (InterruptedException e)
						{
						}
						_frint.decayMe();
						_frint.doDie(killer);
						gb.setFrintezza(null);
						
						for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
						{
							pc = n.getValue();
							if (pc == null)
								continue;
							pc.leaveMovieMode();
						}
					}
				}
				_trans = false;
				_lastStage = false;
				_halisha = null;
				gb.setFrintezza(null);
				break;
		}	
		return null; 
	}
	//
	
	private static class ChkHp implements Runnable
	{
		ChkHp()
		{
		}

		public void run()
		{		
			if (_halisha.getTemplate().npcId == 29047)
				return;
					
			if (_halisha.getTemplate().npcId == 29046 && _lastStage)
				return;	
				
			try
			{	
				if (_halisha.getCurrentHp() < (_halisha.getMaxHp() * 0.6))
				{	
					if(!_trans)
					{
						_halisha.setTarget(_halisha);
						_halisha.addUseSkillDesire(5017,1);
					}
					else
						_halisha.broadcastPacket(new SocialAction(_halisha.getObjectId(), 2));
					try
					{
						Thread.sleep(1500);
					}
					catch (InterruptedException e)
					{
					}
					if(!_trans)
						transeForm();
					else if (_trans && !_lastStage)
						finalForm();
				}
			}
			catch (Throwable e){}
			
			if (!_lastStage)
				ThreadPoolManager.getInstance().scheduleGeneral(new ChkHp(), _hpChk);
		}
	}
	
	private static void transeForm()
	{
		if (_trans)
			return;
		_trans = true;
		_halisha.getTemplate().setRhand(7903);
					
		/*try
		{
			Thread.sleep(5500);
		}
		catch (InterruptedException e)
		{
		}*/
		_halisha.decayMe();
		_halisha.deleteMe();
					
		_halisha = (L2GrandBossInstance)GrandBossManager.getInstance().createOnePrivateEx(29046, _halisha.getX(), _halisha.getY(), _halisha.getZ(),0);
					
		/*try
		{
			Thread.sleep(300);
		}
		catch (InterruptedException e)
		{
		}*/
	}
	
	private static void finalForm()
	{
		if (!_trans)
			return;
		if (_lastStage)
			return;
		_lastStage = true;
		_halisha.getTemplate().setRhand(8204);
		/*try
		{
			Thread.sleep(5500);
		}
		catch (InterruptedException e)
		{
		}*/
					
		_halisha.decayMe();
		_halisha.deleteMe();
					
		_halisha = (L2GrandBossInstance)GrandBossManager.getInstance().createOnePrivateEx(29047, _halisha.getX(), _halisha.getY(), _halisha.getZ(),0);
					
		/*try
		{
			Thread.sleep(300);
		}
		catch (InterruptedException e)
		{
		}*/
	}
	
	private static class ChkSkl implements Runnable
	{
		ChkSkl()
		{
		}

		public void run()
		{			
			try
			{
				if (_halisha.getTemplate().npcId == 29046 && _trans)
					return;
					
				int chanse = Rnd.get(100);	
				if (chanse < 55)
				{
					int lvl = Frintezza._prts;
					if (lvl <= 0)
						lvl = 1;
					else if (lvl > 2)
						lvl = 2;
						
					if(_trans && chanse < 25)
						_halisha.addUseSkillDesire(5016,1);
					else if(chanse < 35)
						_halisha.addUseSkillDesire(5018,lvl);
					else if(chanse < 45)
						if (_halisha.getTemplate().npcId == 29047)
							_halisha.addUseSkillDesire(5019,1);
					else
						_halisha.addUseSkillDesire(5014,lvl);
				}
				else if (_trans || _lastStage)
					_halisha.doTele();
			}
			catch (Throwable e){}
			ThreadPoolManager.getInstance().scheduleGeneral(new ChkSkl(), _sklChk);
		}
	}
	
	private static BossInfo getInfo()
	{
		return GrandBossManager.getInstance().get(BOSS);
	}
	
	//
	
	public static void main (String... arguments )
	{
		new Halisha(-1,"Halisha","Halisha");
	}
}
