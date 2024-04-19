package commands.voice;

//import javolution.text.TextBuilder;
import javolution.text.TextBuilder;
import ru.agecold.gameserver.Announcements;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.L2Summon;
import ru.agecold.gameserver.model.L2World;
//import ru.agecold.gameserver.model.actor.instance.L2DoorInstance;
import ru.agecold.gameserver.model.actor.instance.L2DoorInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.serverpackets.MagicSkillUser;
//import ru.agecold.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.agecold.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.agecold.gameserver.taskmanager.DecayTaskManager;
import scripts.commands.IVoicedCommandHandler;
import scripts.commands.VoicedCommandHandler;
import scripts.commands.UserCommandHandler;
import scripts.scripting.CompiledScriptCache;
import scripts.scripting.L2ScriptEngineManager;
import ru.agecold.Config;
import java.io.IOException;
import java.util.logging.Level;
import java.io.File;
import ru.agecold.gameserver.instancemanager.EventManager;
import ru.agecold.gameserver.datatables.StaticObjects;

public class GM implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS = { 
			"gm", "gm_kick", "gm_kill", "gm_res", "gm_cancel", "gm_reload", "gm_get", "gm_getparty", "gm_announce", "gm_countdown", "gm_banchat",
			"gm_heal", "gm_unbanchat", "gm_jail", "gm_rlj", "gm_unjail", "gm_teleto", "gm_vis", "gm_invis", "gm_silence", "gm_close", "gm_open", "gm_goto", "gm_inv"};
	
    public GM()
    {
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
    }

    public boolean useVoicedCommand(String command, L2PcInstance gm, String target)
    {
		if (gm.getAccessLevel() < 25)
			return false;
		
        if(command.equalsIgnoreCase("gm"))
		{
			gmWelcome(gm);
            return true;
		}
        else if(command.equalsIgnoreCase("gm_kick"))
        {
	        doEvent(gm, 1);
            return true;
		}
        else if(command.equalsIgnoreCase("gm_kill"))
        {
	        doEvent(gm, 2);
            return true;
		}
        else if(command.equalsIgnoreCase("gm_res"))
        {
	        doEvent(gm, 3);
            return true;
		}
        else if(command.equalsIgnoreCase("gm_cancel"))
        {
	        doEvent(gm, 4);
            return true;
		}
        else if(command.equalsIgnoreCase("gm_reload"))
        {
	        doEvent(gm, 5);
            return true;
		}
        else if(command.equalsIgnoreCase("gm_heal"))
        {
	        doEvent(gm, 6);
            return true;
		}
        else if(command.equalsIgnoreCase("gm_vis"))
        {
	        selfEvent(gm, 1);
            return true;
		}
        else if(command.equalsIgnoreCase("gm_invis"))
        {
	        selfEvent(gm, 2);
            return true;
		}
        else if(command.equalsIgnoreCase("gm_silence"))
        {
	        selfEvent(gm, 3);
            return true;
		}
        else if(command.equalsIgnoreCase("gm_open"))
        {
	        doorEvent(gm, 1);
            return true;
		}
        else if(command.equalsIgnoreCase("gm_close"))
        {
	        doorEvent(gm, 2);
            return true;
		}
        else if(command.startsWith("gm_announce"))
        {
	        cmdEvent(gm, 1, command.substring(12));
            return true;
		}
        else if(command.startsWith("gm_get"))
        {
	        cmdEvent(gm, 2, command.substring(7) + " x");
	        return true;
		}
        else if(command.startsWith("gm_getparty"))
        {
	        cmdEvent(gm, 3, command.substring(12) + " x");
	        return true;
		}
        else if(command.startsWith("gm_banchat"))
        {
			cmdEvent(gm, 4, command.substring(11));
            return true;
		}
        else if(command.startsWith("gm_unbanchat"))
        {
			cmdEvent(gm, 5, command.substring(13) + " x");
            return true;
		}
        else if(command.startsWith("gm_jail"))
        {
			cmdEvent(gm, 6, command.substring(8));
            return true;
		}
        else if(command.startsWith("gm_unjail"))
        {
			cmdEvent(gm, 7, command.substring(9) + " x");
            return true;
		}
        else if(command.equalsIgnoreCase("gm_rlj"))
        {
			doEvent(gm, 8);
            return true;
		}
        else if(command.startsWith("gm_goto"))
        {
	        cmdEvent(gm, 8, command.substring(8) + " x");
	        return true;
		}
		
    	return false;
    }

	private void cmdEvent(L2PcInstance gm, int act, String cmd)
	{
		if(cmd.equalsIgnoreCase(""))
			return;
			
		if (act == 1) // aннонс
		{
			Announcements.getInstance().announceToAll(cmd);
			return;
		}		
			
		String[] params = cmd.split(" ");
		L2PcInstance target = L2World.getInstance().getPlayer(params[0]);
		if (target == null)
		{
			gm.sendPacket(Static.TARGET_CANT_FOUND);
			return;
		}
			
		switch(act)
		{
			case 2: // тп игрока к себе
				target.teleToLocation(gm.getX(), gm.getY(), gm.getZ());
				break;
			case 3: // тп пати к себе
				if (target.getParty() != null)
					target.getParty().teleTo(gm.getX(), gm.getY(), gm.getZ());
				break;
			case 4: // дать бч
				int bc = 120;
				try
				{
					bc = Integer.parseInt(params[1]);
				}
				catch (NumberFormatException nfe) {}
				Announcements.getInstance().announceToAll("Игрок " + target.getName() + " получает бан чата на " + bc + " секунд.");
				gm.sendAdmResultMessage("Влепили банчата игроку " + target.getName() + " на " + bc + " секунд.");
				target.setChatBanned(true, bc, "GM");
				break;
			case 5: // снять бч
				gm.sendAdmResultMessage("Сняли банчата с игрока " + target.getName());
				target.setChatBanned(false, 0, "");
				break;
			case 6: // посадить в тюрьму
				int jl = 5;
				try
				{
					jl = Integer.parseInt(params[1]);
				}
				catch (NumberFormatException nfe) {}
				gm.sendAdmResultMessage("Посадили в тюрьму игрока " + target.getName() + " на " + jl + " минут.");
				target.setInJail(true, jl);
				break;
			case 7: // освободить из тюрьмы
				gm.sendAdmResultMessage("Освободили из тюрьмы игрока " + target.getName());
				target.setInJail(false, 0);
				break;
			case 8: // тп к игроку
				gm.teleToLocation(target.getX(), target.getY(), target.getZ());
				break;
		}
	}
	
	private void doEvent(L2PcInstance gm, int act)
	{
		L2Object trg = gm.getTarget();
		if (trg == null)
		{
			gm.sendPacket(Static.TARGET_CANT_FOUND);
			return;
		}
		L2Character target = (L2Character)trg;
		
		switch(act)
		{
			case 1: // кикнуть из игры
				if (target.isPlayer())
				{
					((L2PcInstance)target).kick();
					gm.sendAdmResultMessage("Вы кикнули " + target.getName());
				}
				//else
				//	gm.sendPacket(Static.INCORRECT_TARGET);
				break;
			case 2: // убить
				if (target.isPlayer() || target instanceof L2Summon)
					target.reduceCurrentHp(999999, gm);
				break;
			case 3: // реснуть
				if(!target.isDead()) 
					return;

				if (target.isPlayer())
					((L2PcInstance)target).restoreExp(100.0);
				else if (target instanceof L2Summon)
					DecayTaskManager.getInstance().cancelDecayTask(target);
				else
					return;

				target.doRevive();
				break;
			case 4: // снять баффы
				if (target.isPlayer())
					((L2PcInstance)target).stopAllEffectsB(false);
				else if (target instanceof L2Summon)
					target.stopAllEffects();
				break;
			case 5: // откатить скиллы
				if (target.isPlayer())
					((L2PcInstance)target).reloadSkills(false);
				target.broadcastPacket(new MagicSkillUser(target,target,1012,1,1,0));
				break;
			case 6: // вылечить
				target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
				if (target.isPlayer())
					target.setCurrentCp(target.getMaxCp());
				target.broadcastPacket(new MagicSkillUser(target,target,2241,1,1,0));
				break;
			case 8: // reload quest
				//target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
				//if (target.isPlayer())
				//	target.setCurrentCp(target.getMaxCp());
				//target.broadcastPacket(new MagicSkillUser(target,target,2241,1,1,0));
				//new AntiBotPW();
				
				
				
				
				//nProtect.getInstance().setAntiBotPW();
				
				
				
				
				
				
				
				
				
				//_log.info(" "+AntiBotPW.getInstance().CheckCore()+" ");
                System.err.println("Start Reloading Quests...");
				gm.sendAdmResultMessage("Start Reloading Quests...");
                    try 
                    {
                        //QuestManager.getInstance().save();
                        L2ScriptEngineManager.getInstance();
                        //QuestManager.init();
                        //QuestJython.init();
						//QuestJython.reload("/data/jscript/");
                        //QuestManager.reload();
                        //QuestJython.reloadQuest();
                        EventManager.init();
                        UserCommandHandler.getInstance();
                        VoicedCommandHandler.getInstance();
						StaticObjects.getInstance();
                        try
                        {
                            System.out.println("GameServer: Loading Server Scripts");
                            gm.sendAdmResultMessage("GameServer: Loading Server Scripts");
                            File scripts = new File((new StringBuilder()).append(Config.DATAPACK_ROOT).append("/data/scripts.cfg").toString());
                            L2ScriptEngineManager.getInstance().executeScriptList(scripts);
                        }
                        catch(IOException ioe)
                        {
                            System.out.println("GameServer [ERROR]: Failed loading scripts.cfg, no script going to be loaded");
							gm.sendAdmResultMessage("GameServer [ERROR]: Failed loading scripts.cfg, no script going to be loaded");
                        }
                        try
                        {
                            CompiledScriptCache compiledScriptCache = L2ScriptEngineManager.getInstance().getCompiledScriptCache();
                            if(compiledScriptCache == null)
                            {
                                // System.out.println("GameServer: Compiled Scripts Cache is disabled.");
                            } 
							else
                            {
                                compiledScriptCache.purge();
                                if(compiledScriptCache.isModified())
                                {
                                    compiledScriptCache.save();
                                    System.out.println("GameServer: Compiled Scripts Cache was saved.");
                                    gm.sendAdmResultMessage("GameServer: Compiled Scripts Cache was saved.");
                                }
								else
                                {
                                        System.out.println("GameServer: Compiled Scripts Cache is up-to-date.");
                                        gm.sendAdmResultMessage("GameServer: Compiled Scripts Cache is up-to-date.");
                                }
                            }
                        }
                        catch(IOException e)
                        {
                            System.out.println(Level.SEVERE + "GameServer [ERROR]: Failed to store Compiled Scripts Cache." + e);
							gm.sendAdmResultMessage("GameServer [ERROR]: Failed to store Compiled Scripts Cache." + e);
                        }
                    System.out.println("\nJava New Scripts are Reloaded.");
                    gm.sendAdmResultMessage("Java New Scripts are Reloaded.");
                    }
					catch(NumberFormatException e)
					{
                        System.out.println("Error: "+e);
                        gm.sendAdmResultMessage("Error: "+e);
                    }
				break;
		}
		//Log.add(TimeLogger.getTime() + gm.getName() +"# target: " + target.getName() + " // action: " + act, "gm_log");	
	}
	
	private void selfEvent(L2PcInstance gm, int act)
	{
		switch(act)
		{
			case 1: // выйти из сумрака
				gm.getAppearance().setVisible();
				gm.teleToLocation(gm.getX(), gm.getY(), gm.getZ());
				break;
			case 2: // уйти в сумрак
				gm.getAppearance().setInvisible();
				gm.teleToLocation(gm.getX(), gm.getY(), gm.getZ());
				break;
			case 3: // игнор
				if (gm.getMessageRefusal())
				{
					gm.setMessageRefusal(false);
					gm.sendAdmResultMessage("ПМ открыт.");
				}
				else
				{
					gm.setMessageRefusal(true);
					gm.sendAdmResultMessage("Игнорим всех.");
				}
				break;
		}
	}
	
	private void doorEvent(L2PcInstance gm, int act)
	{
		L2Object trg = gm.getTarget();
		if (trg == null || !(trg instanceof L2DoorInstance))
		{
			gm.sendPacket(Static.TARGET_CANT_FOUND);
			return;
		}
		L2DoorInstance door = (L2DoorInstance)trg;
		switch(act)
		{
			case 1: // открыть дверь
                door.openMe();
				break;
			case 2: // закрыть дверь
                door.closeMe();
				break;
		}
	}
	
	private void gmWelcome(L2PcInstance gm)
	{
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		TextBuilder build = new TextBuilder("<html><body>");
		build.append("<center><font color=\"LEVEL\">Помощь</font></center><br><br>");
		build.append("ГМ <font color=\"LEVEL\">" + gm.getName() + "</font>; rank 3<br><br>");
		build.append("Cписок команд:<br>");
		build.append("<font color=66CC00>.синтаксис</font> (параметры); описание<br>");
		build.append("<font color=66CC00>.gm_banchat</font> ник_чара 3; Банчата в секундах<br1>");
		build.append("<font color=66CC00>.gm_unbanchat</font> ник_чара; Снять банчата<br1>");
		build.append("<font color=66CC00>.gm_jail</font> ник_чара 4; В тюрьму в минутах<br1>");
		build.append("<font color=66CC00>.gm_unjail</font> ник_чара; Вытащить из тюрьмы<br1>");
		build.append("<font color=66CC00>.gm_get</font> ник_чара; ТП игрока<br1>");
		build.append("<font color=66CC00>.gm_getparty</font> ник_чара; ТП пати<br1>");
		build.append("<font color=66CC00>.gm_goto</font> ник_чара; ТП к игроку<br1>");
		build.append("<font color=66CC00>----</font><br>");
		build.append("<font color=66CC00>.gm_kick</font> (таргет); Кикнуть<br1>");
		build.append("<font color=66CC00>.gm_kill</font> (таргет); Убить<br1>");
		build.append("<font color=66CC00>.gm_res</font> (таргет); Реснуть<br1>");
		build.append("<font color=66CC00>.gm_heal</font> (таргет); Вылечить<br1>");
		build.append("<font color=66CC00>.gm_reload</font> (таргет); Откат скиллов<br1>");
		build.append("<font color=66CC00>.gm_cancel</font> (таргет); Снять баффы<br1>");
		build.append("<font color=66CC00>.gm_open</font> (таргетДверь); Открыть<br1>");
		build.append("<font color=66CC00>.gm_close</font> (таргетДверь); Закрыть<br1>");
		build.append("<font color=66CC00>----</font><br>");
		build.append("<font color=66CC00>.gm_announce</font> текст; Анонс<br1>");
		build.append("<font color=66CC00>.gm_vis</font>; Выйти из сумрака<br1>");
		build.append("<font color=66CC00>.gm_invis</font>; Войти в сумрак<br1>");
		build.append("<font color=66CC00>.gm_silence</font>; ПМ игнор<br1>");
		build.append("<br><br>");
		build.append("</body></html>");
		nhm.setHtml(build.toString());
		gm.sendPacket(nhm);
	}

    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }
	
	public static void main (String... arguments )
	{
		new GM();
	}
}
