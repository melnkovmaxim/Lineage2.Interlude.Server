package ai;

import javolution.util.FastList;

import ru.agecold.Config;
import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.datatables.DoorTable;
import ru.agecold.gameserver.datatables.SkillTable;
import ru.agecold.gameserver.instancemanager.GrandBossManager;
import ru.agecold.gameserver.instancemanager.GrandBossManager.BossInfo;
import ru.agecold.gameserver.model.L2ItemInstance;
import ru.agecold.gameserver.model.L2Party;
import ru.agecold.gameserver.model.actor.instance.L2GrandBossInstance;
import ru.agecold.gameserver.model.actor.instance.L2MonsterInstance;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.State;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.gameserver.network.serverpackets.CreatureSay;
import ru.agecold.gameserver.network.serverpackets.MagicSkillUser;
import ru.agecold.gameserver.network.serverpackets.SocialAction;
import ru.agecold.util.Rnd;

public class Frintezza extends QuestJython
{
	//private static long _lastHit = 0;
	private static int _partysEntered = 0;
	private static int _killCount = 0;
	//анспавн
	private static final int _life = 2400000;
	private static FastList<L2PcInstance> _players = new FastList<L2PcInstance>();
	//анимация камер
	private static L2MonsterInstance camera = null;
	private static L2MonsterInstance camera2 = null;
	
	private static L2GrandBossInstance _frint = null;
	private static final int BOSS = 29045;
	private static L2GrandBossInstance _halisha = null;
	//портреты
	public static int _prts = 4;
	private static FastList<L2MonsterInstance> _pghosts = new FastList<L2MonsterInstance>();
	private static FastList<L2MonsterInstance> _privates = new FastList<L2MonsterInstance>();
	//клан
	private static final int _frintezza = 29045;
	private static final int _hall_alarm_device = 18328;
	private static final int _hall_keeper_captain = 18329;
	private static final int _dark_choir_player = 18339;
	private static final int _dark_choir_captain = 18334;
	private static final int _undeadband_member_leader = 18335;
	private static final int _follower_dummy = 32011;
	
	private static final int _breaking_arrow = 8192;
	
	public Frintezza(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		State st = new State("Start", this);
		this.setInitialState(st);
		
		this.addSpawnId(_frintezza);
		this.addStartNpc(_follower_dummy);
		this.addTalkId(_follower_dummy);
		this.addKillId(_frintezza);
		this.addKillId(_dark_choir_captain);
		this.addKillId(_dark_choir_player);
		this.addKillId(_hall_alarm_device);
	}
	
	@Override
	protected void init_LoadGlobalData()
	{
		GrandBossManager gb = GrandBossManager.getInstance();	
		BossInfo bi = getInfo();
	    if (bi.status == 0)
		{
			if (bi.respawn - System.currentTimeMillis() < 0)
			{
				gb.setStatus(BOSS, 1);
				gb.setRespawn(BOSS, 0);
			}
		}	
	    else
		{
			gb.setStatus(BOSS, 1);
			gb.setRespawn(BOSS, 0);
		}
	}
	
	@Override
    public String onSpawn(L2NpcInstance npc) 
	{ 
		//System.out.println("33333@@@@@@@@@@@@@@@@");
		_frint = (L2GrandBossInstance)npc;
		GrandBossManager.getInstance().setFrintezza(_frint);
		_players.addAll(_frint.getKnownList().getKnownPlayersInRadius(2100));
		if (!_players.isEmpty())
		{
			for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
			{
				L2PcInstance pl = n.getValue();
				if (pl == null || _players.contains(pl))
					continue;
				_players.add(pl);
			}
		}	
		_frint.setRunning();
		_frint.setIsImobilised(true);
		_frint.setIsInvul(true);
		
		closeAllDoors();
		StartFrintezzaAnim();
		if (getInfo().respawn > 0)
			return null; 
		setStatus(2);	
		return null; 
	} 
	
	@Override
	public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{ 
		switch(npc.getNpcId())
		{
			case _hall_alarm_device:
			case _dark_choir_player:
				doEvent(2);
				break;
			case _dark_choir_captain:
				((L2MonsterInstance)npc).dropItem(_breaking_arrow, Rnd.get(10,20));
				doEvent(2);
				break;
			case _frintezza:
				_players.clear();
				deletePrivates();
				GrandBossManager.getInstance().setFrintezza(null);
				break;
		}
		return null; 
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance talker) 
	{
		int npcId = npc.getNpcId();
		if (npcId == _follower_dummy) 
		{
			if (getStatus() == 2)
				return "<html><head><body>[Whispering]<br><font color=\"LEVEL\">Busy, please wait.</font></body></html>";
			if (selfAlive())
			{
				if (getPartyCount() >= 5)
					return "<html><head><body>[Whispering]<br><font color=\"LEVEL\">Already included 5 groups, can no longer be.</font></body></html>";
				else
				{
					L2Party party = talker.getParty();
					if(party == null || party.getMemberCount() < Config.FRINTA_MMIN_PLAYERS)
						return "<html><head><body>[Whispering]<br><font color=\"LEVEL\">Only complete group can sign.</font></body></html>";

					if(!party.isInCommandChannel() || !party.getCommandChannel().getChannelLeader().equals(talker))
						return "<html><head><body>[Whispering]<br><font color=\"LEVEL\">Only Command Chanell leader can sign.</font></body></html>";

					if(party.getCommandChannel().getPartys().size() < Config.FRINTA_MMIN_PARTIES)
						return "<html><head><body>[Whispering]<br><font color=\"LEVEL\">Minimum 2 partys in Command Chanell can sign.</font></body></html>";

					if (haveTicket(talker)) 
					{
						//GrandBossManager gb = GrandBossManager.getInstance();
						for (L2Party rparty : party.getCommandChannel().getPartys())
						{
							if (rparty == null)
								continue;

							if (getPartyCount() == 0)
								doEvent(1);

							if (getPartyCount() == 1 || getPartyCount() == 3)
								rparty.teleTo(173240, -76950, -5104);
							else
								rparty.teleTo(174108, -76197, -5104);

							updatePartyCount();

							/*for (L2PcInstance member : rparty.getPartyMembers())
							{
								if (member == null)
									continue;

								gb.getZone(174239, -89805, -5020).allowPlayerEntry(member, 9000000);
							}*/
						}
						return null;
					}
					return "<html><head><body>[Whispering]<br><font color=\"LEVEL\">Your leader of the group must Force Field Removal Scroll to sign.</font></body></html>";
				}
				/*if (haveTicket(talker))
					System.out.println("#1");

				doEvent(1);
				talker.teleToLocation(173240, -76950, -5104);
				GrandBossManager.getInstance().getZone(174239, -89805, -5020).allowPlayerEntry(talker, 9000000);
				return null;*/
			}
			return "<html><head><body>[Whispering]<br><font color=\"LEVEL\">Now you can not fight with him. Go away!</font></body></html>";
		}
		return null;
	}
	
	private static boolean selfAlive()
	{
		GrandBossManager gb = GrandBossManager.getInstance();	
		BossInfo bi = getInfo();
	    if (bi.status == 0)
		{
			if (bi.respawn - System.currentTimeMillis() > 0)
				return false;
			else
			{
				gb.setStatus(BOSS, 1);
				gb.setRespawn(BOSS, 0);
				return true;
			}
		}
		return true;
	}
	
	private static int getStatus()
	{
		return GrandBossManager.getInstance().get(BOSS).status;
	}
	
	private static boolean haveTicket(L2PcInstance talker)
	{
		if (!Config.NOEPIC_QUESTS)
			return true;
			
		L2ItemInstance coin = talker.getInventory().getItemByItemId(8073);
		if (coin == null || coin.getCount() < 1)
			return false;
		
		if (!talker.destroyItemByItemId("RaidBossTele", 8073, 1, talker, true))
			return false;
			
		return true;	
	}
	
	public static void doEvent(int event)
	{
		L2MonsterInstance privat = null;
		DoorTable dt = DoorTable.getInstance();
		GrandBossManager gb = GrandBossManager.getInstance();
		switch(event)
		{
			case 1:
				privat = (L2MonsterInstance)gb.createOnePrivateEx(_hall_alarm_device, 175100,-76870,-5104,0);
				privat.setIsImobilised(true);
				_privates.add(privat);
				privat = (L2MonsterInstance)gb.createOnePrivateEx(_hall_alarm_device, 175400,-76000,-5104,0);
				privat.setIsImobilised(true);
				_privates.add(privat);
				privat = (L2MonsterInstance)gb.createOnePrivateEx(_hall_alarm_device, 175030,-75175,-5104,0);
				privat.setIsImobilised(true);
				_privates.add(privat);
				privat = (L2MonsterInstance)gb.createOnePrivateEx(_hall_alarm_device, 174190,-74800,-5104,0);
				privat.setIsImobilised(true);
				_privates.add(privat);
				privat = (L2MonsterInstance)gb.createOnePrivateEx(_hall_alarm_device, 172870,-76000,-5104,0);
				privat.setIsImobilised(true);
				_privates.add(privat);
				for (int i = 0; i <=5; i++)
				{
					privat = (L2MonsterInstance)gb.createOnePrivateEx(_hall_keeper_captain,173990,-75920,-5104,0);
					_privates.add(privat);
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new TimerTask(2002), _life);
				break;
			case 2:
				_killCount+=1;
				switch(_killCount)
				{
					case 5:
						dt.getDoor(25150051).openMe();
						dt.getDoor(25150052).openMe();
						dt.getDoor(25150053).openMe();
						dt.getDoor(25150054).openMe();
						dt.getDoor(25150055).openMe();
						dt.getDoor(25150056).openMe();
						dt.getDoor(25150057).openMe();
						dt.getDoor(25150058).openMe();
						dt.getDoor(25150042).openMe();
						dt.getDoor(25150043).openMe();
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_dark_choir_player, 173940,-81600,-5120,0);
						_privates.add(privat);
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_dark_choir_player, 174040,-81700,-5120,0);
						_privates.add(privat);
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_dark_choir_player, 174140,-81800,-5120,0);
						_privates.add(privat);
						break;
					case 8:
						dt.getDoor(25150061).openMe();
						dt.getDoor(25150062).openMe();
						dt.getDoor(25150063).openMe();
						dt.getDoor(25150064).openMe();
						dt.getDoor(25150065).openMe();
						dt.getDoor(25150066).openMe();
						dt.getDoor(25150067).openMe();
						dt.getDoor(25150068).openMe();
						dt.getDoor(25150069).openMe();
						dt.getDoor(25150070).openMe();
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_dark_choir_captain, 173400,-81100,-5072,0);
						_privates.add(privat);
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_dark_choir_captain, 173260,-81700,-5072,0);
						_privates.add(privat);
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_dark_choir_captain, 173360,-82400,-5072,0);
						_privates.add(privat);
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_dark_choir_captain, 174720,-82530,-5072,32768);
						_privates.add(privat);
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_dark_choir_captain, 174900,-81800,-5072,32768);
						_privates.add(privat);
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_dark_choir_captain, 174750,-81150,-5072,32768);
						_privates.add(privat);
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_undeadband_member_leader,174000,-81830,-5120,0);
						_privates.add(privat);
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_undeadband_member_leader,174000,-81830,-5120,0);
						_privates.add(privat);
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_undeadband_member_leader,174000,-81830,-5120,0);
						_privates.add(privat);
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_undeadband_member_leader,174000,-81830,-5120,0);
						_privates.add(privat);
						privat = (L2MonsterInstance)gb.createOnePrivateEx(_undeadband_member_leader,174000,-81830,-5120,0);
						_privates.add(privat);
						break;
					case 14:
						dt.getDoor(25150045).openMe();
						dt.getDoor(25150046).openMe();
						ThreadPoolManager.getInstance().scheduleGeneral(new TimerTask(1), 600000);
						break;
				}
				break;
		}
	}
	
	private static class TimerTask implements Runnable
	{
		private int _taskId = 0;

		public TimerTask(final int taskId)
		{
			_taskId = taskId;
		}

		public void run()
		{
			GrandBossManager gb = GrandBossManager.getInstance();
			switch(_taskId)
			{
				case 1:
					L2GrandBossInstance fint = (L2GrandBossInstance)gb.createOnePrivateEx(BOSS, 174239, -89805, -5020, 16000);
					fint.setRunning();
					break;
				case 1002:			
					if(gb.getFrintezza() == null)
						return;			
					if(getInfo().status == 1 || getInfo().status == 0)
						return;	
					int i0 = Rnd.get(4);
					i0 = ( i0 + 1 );
					switch(i0)
					{
						case 1:
							playSong(i0);
							sayShout("Frintezza's Healing Rhapsody");
							ThreadPoolManager.getInstance().scheduleGeneral(new TimerTask(1002), 36000);
							break;
						case 2:
							playSong(i0);
							sayShout("Frintezza's Rampaging Opus");
							ThreadPoolManager.getInstance().scheduleGeneral(new TimerTask(1002), 36000);
							break;
						case 3:
							playSong(i0);
							sayShout("Frintezza's Power Concerto");
							ThreadPoolManager.getInstance().scheduleGeneral(new TimerTask(1002), 36000);
							break;
						case 4:
							playSong(i0);
							sayShout("Frintezza's Plagued Concerto");
							ThreadPoolManager.getInstance().scheduleGeneral(new TimerTask(1002), 35000);
							break;
						case 5:
							playSong(i0);
							sayShout("Frintezza's Psycho Symphony");
							ThreadPoolManager.getInstance().scheduleGeneral(new TimerTask(1002), 39000);
							break;
					}
					//ThreadPoolManager.getInstance().scheduleGeneral(new TimerTask(i0), 1000);
					break;
				case 2002:
					if (gb.getFrintezza() == null)
						return;		
					if(getInfo().status == 1 || getInfo().status == 0)
						return;	
						
					sayShout("Time is out! Raid failed and finished!");
					gb.getZone(174239, -89805, -5020).oustAllPlayers();

					setStatus(1);	
					_frint.decayMe();
					_players.clear();
					if (gb.getHalisha() != null)
						gb.getHalisha().deleteMe();
					_frint.deleteMe();
					_frint = null;
					closeAllDoors();
					deletePrivates();
					break;
			}
		}
	}
	
	private static void closeAllDoors()
	{
		DoorTable dt = DoorTable.getInstance();
		dt.getDoor(25150051).closeMe();
		dt.getDoor(25150052).closeMe();
		dt.getDoor(25150053).closeMe();
		dt.getDoor(25150054).closeMe();
		dt.getDoor(25150055).closeMe();
		dt.getDoor(25150056).closeMe();
		dt.getDoor(25150057).closeMe();
		dt.getDoor(25150058).closeMe();
		dt.getDoor(25150042).closeMe();
		dt.getDoor(25150043).closeMe();
		dt.getDoor(25150061).closeMe();
		dt.getDoor(25150062).closeMe();
		dt.getDoor(25150063).closeMe();
		dt.getDoor(25150064).closeMe();
		dt.getDoor(25150065).closeMe();
		dt.getDoor(25150066).closeMe();
		dt.getDoor(25150067).closeMe();
		dt.getDoor(25150068).closeMe();
		dt.getDoor(25150069).closeMe();
		dt.getDoor(25150070).closeMe();
		dt.getDoor(25150045).closeMe();
		dt.getDoor(25150046).closeMe();
	}	
	
	private static void deletePrivates()
	{				
		L2MonsterInstance mob = null;
		for (FastList.Node<L2MonsterInstance> n = _privates.head(), end = _privates.tail(); (n = n.getNext()) != end;)
		{
			mob = n.getValue();
			if (mob == null)
				continue;
			mob.decayMe();
			mob.deleteMe();
		}
		_privates.clear();
		
		for (FastList.Node<L2MonsterInstance> n = _pghosts.head(), end = _pghosts.tail(); (n = n.getNext()) != end;)
		{
			mob = n.getValue();
			if (mob == null)
				continue;
			mob.decayMe();
			mob.deleteMe();
		}
		_pghosts.clear();
		clearPartyCount();
		_killCount = 0;
	}
	
	public static void StartFrintezzaAnim()
	{
		_frint.setTarget(_frint);
		
		GrandBossManager gb = GrandBossManager.getInstance();
		camera = (L2MonsterInstance)gb.createOnePrivateEx(80003,174235,-88023,-4820,40240);
		L2PcInstance pc = null;
		//  UPDATE `npc` SET `type`='L2Monster' WHERE (`id`='80003');
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			pc.specialCamera(camera, 500, 160, -45, 4000, 4400);
		}
		
		try
		{
			Thread.sleep(3800);
		}
		catch (InterruptedException e)
		{
		}
		if (camera != null)
			camera.deleteMe();
		camera = null;
		
		// 
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			pc.specialCamera(_frint, 1600, 95 , 7, 2000, 2000);
		}
		
		try
		{
			Thread.sleep(2200);
		}
		catch (InterruptedException e)
		{
		}
		
		// 
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			pc.specialCamera(_frint, 200, 90, 0, 3000, 5000);
		}
		
		try
		{
			Thread.sleep(4900);
		}
		catch (InterruptedException e)
		{
		}
		
		// 
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
		//  motor5
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			_frint.broadcastPacket(new SocialAction(_frint.getObjectId(), 1));
		}
		try
		{
			Thread.sleep(8900);
		}
		catch (InterruptedException e)
		{
		}
		//  motor6
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			pc.specialCamera(_frint, 340, 182, 30, 12000, 25000);
		}
		try
		{
			Thread.sleep(4900);
		}
		catch (InterruptedException e)
		{
		}
		//  motor7
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			_frint.broadcastPacket(new SocialAction(_frint.getObjectId(), 3));
			//pc.specialCamera(this, 1200, 90, 20, 12000, 3000);
		}
		
		try
		{
			Thread.sleep(3900);
		}
		catch (InterruptedException e)
		{
		}
		//  motor8
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			_frint.broadcastPacket(new MagicSkillUser(_frint, _frint, 5006, 1, 15800, 0));
			pc.specialCamera(_frint, 1200, 90, 20, 12000, 3000);
		}
		try
		{
			Thread.sleep(2900);
		}
		catch (InterruptedException e)
		{
		}
		//  motor9
		/*for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			L2PcInstance pc = n.getValue();
			if (pc == null)
				continue;
			pc.specialCamera(camera, 700, 140, 40, 15000, 6000);
		}*/
		camera = (L2MonsterInstance)gb.createOnePrivateEx(29053,175882,-88703,-5134,40240);
		try
		{
			Thread.sleep(5400);
		}
		catch (InterruptedException e)
		{
		}
		//  motor10
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			pc.specialCamera(camera, 650, 0, 0, 11500, 15000);
		}
		L2MonsterInstance p1 = (L2MonsterInstance)gb.createOnePrivateEx(29048, 175882, -88703, -5134,0);
		L2MonsterInstance p2 = (L2MonsterInstance)gb.createOnePrivateEx(29048, 175820, -87184, -5108,0);
		L2MonsterInstance p3 = (L2MonsterInstance)gb.createOnePrivateEx(29048, 172629, -87175, -5108,0);
		L2MonsterInstance p4 = (L2MonsterInstance)gb.createOnePrivateEx(29048, 172596, -88706, -5134,0);
		_pghosts.add(p1);
		_pghosts.add(p2);
		_pghosts.add(p3);
		_pghosts.add(p4);
		try
		{
			Thread.sleep(5900);
		}
		catch (InterruptedException e)
		{
		}
		//  motor11
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			pc.specialCamera(p1, 400, 0, 40, 5000, 12000);
		}
		try
		{
			Thread.sleep(8900);
		}
		catch (InterruptedException e)
		{
		}
		//  motor12
		camera = (L2MonsterInstance)gb.createOnePrivateEx(80003,174235,-88023,-4820,40240);
		try
		{
			Thread.sleep(1900);
		}
		catch (InterruptedException e)
		{
		}
		//  motor13
		camera2 = (L2MonsterInstance)gb.createOnePrivateEx(29053,174235,-88023,-4820,40240);
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			pc.specialCamera(camera, 500, 160, -45, 4000, 4400);
		}
		try
		{
			Thread.sleep(2500);
		}
		catch (InterruptedException e)
		{
		}
		//  anim
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			camera2.broadcastPacket(new MagicSkillUser(camera2, camera2, 5004, 1, 5800, 0));
			//pc.specialCamera(this, 1200, 90, 20, 12000, 3000);
		}
		try
		{
			Thread.sleep(1400);
		}
		catch (InterruptedException e)
		{
		}
		//  motor14
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			pc.specialCamera(camera, 500, 120, -35, 14000, 6400);
		}
		_halisha = (L2GrandBossInstance)gb.createOnePrivateEx(29046, 174235,-88023,-5108,0);
		try
		{
			Thread.sleep(900);
		}
		catch (InterruptedException e)
		{
		}
		//  motor16
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			pc.specialCamera(_halisha, 500, 160, 45, 14000, 14400);
			_halisha.broadcastPacket(new SocialAction(_halisha.getObjectId(), 1));
		}
		try
		{
			Thread.sleep(5300);
		}
		catch (InterruptedException e)
		{
		}
		//  motor17
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			_halisha.broadcastPacket(new MagicSkillUser(_halisha, _halisha, 5004, 1, 5800, 0));
		}
		if (camera != null)
			camera.deleteMe();
		camera = null;
		if (camera2 != null)
			camera2.deleteMe();
		camera2 = null;
		
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
				
			pc.setTarget(null);
			pc.leaveMovieMode();
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new TimerTask(1002), 16000);
	}
	
	private static void playSong(int lvl)
	{
		_frint.broadcastPacket(new MagicSkillUser(_frint, _frint, 5007, lvl, 35800, 0));
		if (Rnd.get(100) < 50)
		{
			SkillTable.getInstance().getInfo(5007,Rnd.get(1,4)).getEffects(_halisha,_halisha);  
			return;
		}

		L2PcInstance pc = null;
		for (FastList.Node<L2PcInstance> n = _players.head(), end = _players.tail(); (n = n.getNext()) != end;)
		{
			pc = n.getValue();
			if (pc == null)
				continue;
			if (Rnd.get(100)<45)
				SkillTable.getInstance().getInfo(5008,1).getEffects(pc,pc);  
		}
	}

	private static void sayShout(String text)
	{
		CreatureSay cs = new CreatureSay(_frint.getObjectId(), 1, "Frintezza", text);
		_frint.broadcastPacket(cs);
	}

	public static synchronized int getPartyCount()
	{
		return _partysEntered;
	}

	public static synchronized void updatePartyCount()
	{
		_partysEntered+=1;
	}

	public static synchronized void clearPartyCount()
	{
		_partysEntered = 0;
	}

	private static BossInfo getInfo()
	{
		return GrandBossManager.getInstance().get(BOSS);
	}

	private static void setStatus(int i)
	{
		GrandBossManager.getInstance().setStatus(BOSS, i);
	}
	//

	public static void main (String... arguments )
	{
		new Frintezza(-1,"Frintezza","Frintezza");
	}
}
