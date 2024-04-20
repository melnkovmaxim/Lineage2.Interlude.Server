package scripts.ai;

import javolution.util.FastList;

import ru.agecold.Config;
import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.ai.CtrlIntention;
import ru.agecold.gameserver.datatables.SpawnTable;
import ru.agecold.gameserver.instancemanager.GrandBossManager;
import ru.agecold.gameserver.instancemanager.bosses.BaiumManager;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.actor.instance.L2GrandBossInstance;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.State;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.gameserver.network.serverpackets.SocialAction;
import ru.agecold.gameserver.templates.L2NpcTemplate;
import ru.agecold.util.Rnd;

public class Baium extends L2GrandBossInstance
{
	//���������
	private long _lastHit = 0;
	//�� ������
	private static final long _sklChk = 60000;
	//����� �������
	private static final long _trgChk = 40000;

	//������ - ����
	private static int sself_normal_attack = 4127;
	private static int s_energy_wave = 4128;
	private static int s_earth_quake = 4129;
	private static int s_thunderbolt = 4130;
	private static int s_group_hold = 4131;
	//������
	private FastList<L2GrandBossInstance> _angels = new FastList<L2GrandBossInstance>();

	//�������
	public static final long _sleepLife = Config.BAIUM_UPDATE_LAIR;
	//
	private static final int BOSS = 29020;

	private static GrandBossManager _gb = GrandBossManager.getInstance();

	public Baium(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

    @Override
	public void onSpawn()
	{ 
    	super.onSpawn();
		_lastHit = System.currentTimeMillis();
		ThreadPoolManager.getInstance().scheduleAi(new Task(1), 12000, false);

		if (getSpawn() != null)
		{
			getSpawn().setLocx(115852);
            getSpawn().setLocy(17265);
            getSpawn().setLocz(10079);
		}
	}

	private class Task implements Runnable
	{
		int id;
		Task(int id)
		{
			this.id = id;
		}

		public void run()
		{	
			switch (id)
			{
				case 1: // ����� �������
					spawnAngels();
					ThreadPoolManager.getInstance().scheduleAi(new Task(2), _sklChk, false);
					ThreadPoolManager.getInstance().scheduleAi(new Task(3), _trgChk, false);
					break;
				case 2: // ���� ������
					castRandomSkill();
					ThreadPoolManager.getInstance().scheduleAi(new Task(2), _sklChk, false);
					break;
				case 3: // ����� �������
					changeTarget();
					ThreadPoolManager.getInstance().scheduleAi(new Task(3), _trgChk, false);
					break;
				case 4:// un����� �������
					clearAngels();
					break;
				case 5:// ������ �������
					angelTarget();
					break;
			}
		}
	}

    @Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
    {
		_lastHit = System.currentTimeMillis();
        super.reduceCurrentHp(damage, attacker, awake);
	} 

    @Override
	public boolean doDie(L2Character killer)
    {
		super.doDie(killer);
		unSpawnAngels(false);
		BaiumManager.getInstance().notifyDie();
		return true;
	}

    @Override
	public void deleteMe()
    {
        super.deleteMe();
    }
	
	private void changeTarget()
	{
		FastList<L2PcInstance> _players = getKnownList().getKnownPlayersInRadius(1200);
		if (_players.isEmpty())
			return;
			
		L2PcInstance trg = _players.get(Rnd.get(_players.size()-1));
		if (trg != null)
		{
			setTarget(trg);
			addDamageHate(trg,0,999);
			getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, trg);
		}
	}
	
	private void castRandomSkill()
	{
		int rnd = Rnd.get(100);
		if(getCurrentHp() > ( ( getMaxHp() * 3.000000 ) / 4.000000 ) )
		{
			if(rnd < 10)
				addUseSkillDesire(s_energy_wave,1);
			else if(rnd < 15)
				addUseSkillDesire(s_earth_quake,1);
			else
				addUseSkillDesire(sself_normal_attack,1);
		}
		else if(getCurrentHp() > ( ( getMaxHp() * 2.000000 ) / 4.000000 ) )
		{
			if( Rnd.get(100) < 10 )
				addUseSkillDesire(s_group_hold,1);
			else if( Rnd.get(100) < 15 )
				addUseSkillDesire(s_energy_wave,1);
			else if( Rnd.get(100) < 20 )
				addUseSkillDesire(s_earth_quake,1);
			else
				addUseSkillDesire(sself_normal_attack,1);
		}
		else if(rnd < 10)
			addUseSkillDesire(s_thunderbolt,1);
		else if(rnd < 15)
			addUseSkillDesire(s_group_hold,1);
		else if(rnd < 20)
			addUseSkillDesire(s_energy_wave,1);
		else if(rnd < 25)
			addUseSkillDesire(s_earth_quake,1);
		else
			addUseSkillDesire(sself_normal_attack,1);	
	}
	
	//������ �����
	private void spawnAngels()
	{
		L2GrandBossInstance Angel1 = (L2GrandBossInstance)_gb.createOnePrivateEx(29021,115617,17462,10136,0);
		Angel1.setRunning();
		L2GrandBossInstance Angel2 = (L2GrandBossInstance)_gb.createOnePrivateEx(29021,116070,17130,10136,0);
		Angel2.setRunning();
		L2GrandBossInstance Angel3 = (L2GrandBossInstance)_gb.createOnePrivateEx(29021,115910,16838,10136,0);
		Angel3.setRunning();
		L2GrandBossInstance Angel4 = (L2GrandBossInstance)_gb.createOnePrivateEx(29021,115585,16954,10136,0);
		Angel4.setRunning();
		L2GrandBossInstance Angel5 = (L2GrandBossInstance)_gb.createOnePrivateEx(29021,115649,17207,10136,0);
		Angel5.setRunning();
		_angels.add(Angel1);
		_angels.add(Angel2);
		_angels.add(Angel3);
		_angels.add(Angel4);
		_angels.add(Angel5);
		ThreadPoolManager.getInstance().scheduleAi(new Task(5), 4000, false);
	}
	
	private void angelTarget()
	{
		FastList<L2PcInstance> _players = getKnownList().getKnownPlayersInRadius(1200);
		if (_players.isEmpty())
			return;
		
		L2PcInstance trg = null;
		L2GrandBossInstance angel = null;
		for (FastList.Node<L2GrandBossInstance> n = _angels.head(), end = _angels.tail(); (n = n.getNext()) != end;)
		{
			angel = n.getValue();
			if (angel == null)
				continue;

			trg = _players.get(Rnd.get(_players.size()-1));
			if (trg == null)
				continue;
			
			angel.setRunning();
			angel.setTarget(trg);
			angel.addDamageHate(trg,0,999);
			angel.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, trg);
		}
	}
	
	public void unSpawnAngels(boolean sleep)
	{
		if (_angels.isEmpty())
			return;
			
		if (sleep)
		{
			L2GrandBossInstance angel = null;
			for (FastList.Node<L2GrandBossInstance> n = _angels.head(), end = _angels.tail(); (n = n.getNext()) != end;)
			{
				angel = n.getValue();
				if (angel == null)
					continue;
				angel.setRunning();
				angel.setTarget(this);
				angel.addDamageHate(this,0,999);
				angel.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
		}
		ThreadPoolManager.getInstance().scheduleAi(new Task(4), 10000, false);
	}
	
	private void clearAngels()
	{
		if (_angels.isEmpty())
			return;
			
		L2GrandBossInstance angel = null;
		for (FastList.Node<L2GrandBossInstance> n = _angels.head(), end = _angels.tail(); (n = n.getNext()) != end;)
		{
			angel = n.getValue();
			if (angel == null)
				continue;
			angel.deleteMe();
		}
		_angels.clear();
	}
	
	public long getLastHit()
	{
		return _lastHit;
	}
	
	@Override
	public boolean checkRange()
	{
		return false;
	}
}
