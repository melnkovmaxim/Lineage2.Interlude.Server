package events.q9118_Valakas;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import javolution.util.FastList;

import ru.agecold.Config.EventReward;
import ru.agecold.gameserver.Announcements;
import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.instancemanager.GrandBossManager;
import ru.agecold.gameserver.model.actor.instance.L2RaidBossInstance;
import ru.agecold.gameserver.model.quest.State;
import ru.agecold.gameserver.model.quest.jython.QuestJython;

public class q9118_Valakas extends QuestJython {

    //рб
    private final static int BOSS_ID = 41002;
    // локация
    private final static int[] BOSS_LOC = {212990, -114665, -1637};

    //  чтобы был анонс (текст вывод в конфиг)
    private final static String ANNOUNCE_TEXT = "Через 5 минут появится Valakas, Global GK > ТОП РБ > Valakas";
    private final static String ANNOUNCE_SPAWN = "Valakas появился, Global GK > ТОП РБ > Valakas";

    // который будет появляться каждый день в 20:00 по мск (т.е., чтобы запускался как осада по времени компа)
    private final static String BOSS_SPAWN_TIME = "22:30";

    //на сколько времени он появляется, минуты
    private final static int DURATION = 30;

     //перед появленим анонс идет, за сколько минут?
    private final static int ANNOUNCE = 5;

    //// //// Не трогать
    private static long BOSS_DURATION;
    private static long ANNOUNCE_DELAY;
    private static L2RaidBossInstance _boss = null;
    private static FastList<EventReward> _spawns = new FastList<EventReward>();
    //////////

    public static void main(String... arguments) {
        new q9118_Valakas(9118, "q9118_Valakas", "Valakas");
    }

    public q9118_Valakas(int questId, String name, String descr) {
        super(questId, name, descr, 1);
        this.setInitialState(new State("Start", this));

        BOSS_DURATION = TimeUnit.MINUTES.toMillis(DURATION);
        ANNOUNCE_DELAY = TimeUnit.MINUTES.toMillis(ANNOUNCE);

        String[] data = BOSS_SPAWN_TIME.split(";");
        for (String hour : data) {
            String[] time = hour.split(":");
            try {
                _spawns.add(new EventReward(Integer.parseInt(time[0]), Integer.parseInt(time[1]), 0));
            } catch (NumberFormatException nfe) {
				//
            }
        }

		EventReward task  = null;
		for (FastList.Node<EventReward> n = _spawns.head(), end = _spawns.tail(); (n = n.getNext()) != end;) {
			task = n.getValue();
			if (task == null)
				continue;

			ThreadPoolManager.getInstance().scheduleAi(new AnnounceSpawn(), ((getNextSpawnTime(0, false, task.id, task.count) - System.currentTimeMillis()) - ANNOUNCE_DELAY), false);
		}
    }

    private class AnnounceSpawn implements Runnable {

        public void run() {
			announce(ANNOUNCE_TEXT);
            ThreadPoolManager.getInstance().scheduleAi(new BossSpawn(), ANNOUNCE_DELAY, false);
        }
    }

    private class BossSpawn implements Runnable {

        public void run() {
            _boss = (L2RaidBossInstance) GrandBossManager.getInstance().createOnePrivateEx(BOSS_ID, BOSS_LOC[0], BOSS_LOC[1], BOSS_LOC[2], 0);
            _boss.setRunning();

			announce(ANNOUNCE_SPAWN);
            ThreadPoolManager.getInstance().scheduleAi(new EventFinish(), BOSS_DURATION, false);
        }
    }

    private class EventFinish implements Runnable {

        public void run() {
			_boss.deleteMe();
			_boss = null;
			//ThreadPoolManager.getInstance().scheduleAi(new AnnounceSpawn(), ((getNextSpawnTime(0, false, task.id, task.count) - System.currentTimeMillis()) - ANNOUNCE_DELAY), false);
        }
    }

    private static long getNextSpawnTime(long respawn, boolean nextday, int hour, int minute) {
        Calendar tomorrow = new GregorianCalendar();
        if (nextday) {
            tomorrow.add(Calendar.DATE, 1);
        }
        Calendar result = new GregorianCalendar(
                tomorrow.get(Calendar.YEAR),
                tomorrow.get(Calendar.MONTH),
                tomorrow.get(Calendar.DATE),
                hour,
                minute);
        respawn = result.getTimeInMillis();
        if (respawn < System.currentTimeMillis()) {
            return getNextSpawnTime(0, true, hour, minute);
        }
        return respawn;
    }

    private static void announce(String text) {
        Announcements.getInstance().announceToAll(text);
    }
}