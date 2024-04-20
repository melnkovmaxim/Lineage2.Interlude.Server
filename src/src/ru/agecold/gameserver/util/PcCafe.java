package ru.agecold.gameserver.util;

import java.util.logging.Logger;

import ru.agecold.Config;
import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.L2World;
import ru.agecold.util.log.AbstractLogger;
import ru.agecold.util.Rnd;

public class PcCafe
{
    private static final Logger _log = AbstractLogger.getLogger(PcCafe.class.getName());

	private static final int pc_min = Config.PC_CAFE_BONUS.nick;
	private static final int pc_max = Config.PC_CAFE_BONUS.title;

	private static PcCafe _instance;
	public static PcCafe getInstance()
	{
		return _instance;
	}
	public static void init()
	{
		_instance = new PcCafe();
		_instance.load();
	}

	public PcCafe()
	{
	}

	private void load()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new UpdateTask(), Config.PC_CAFE_INTERVAL);
	}

	class UpdateTask implements Runnable
	{
		UpdateTask()
		{
		}

		public void run()
		{
			updatePoints();
		}
	}
	
	private void updatePoints()
	{
		new Thread(new Runnable(){
			public void run()
			{
				try
				{
                    for (L2PcInstance player : L2World.getInstance().getAllPlayers())
                    {
                        if (player == null || player.isInOfflineMode())
							continue;

						player.updatePcPoints(Rnd.get(pc_min, pc_max), 2, (Rnd.get(100) < Config.PC_CAFE_DOUBLE_CHANCE));
                    }
				}
				catch(Exception ignored)
				{
					//
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new UpdateTask(), Config.PC_CAFE_INTERVAL);
			}
		}).start();
	}
}
