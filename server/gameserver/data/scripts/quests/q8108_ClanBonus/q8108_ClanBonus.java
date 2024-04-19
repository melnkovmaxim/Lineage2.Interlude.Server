package quests.q8108_ClanBonus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import ru.agecold.gameserver.Announcements;

import ru.agecold.Config.EventReward;
import ru.agecold.gameserver.datatables.CustomServerData;
import ru.agecold.gameserver.model.L2Clan;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.QuestState;
import ru.agecold.gameserver.model.quest.State;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.util.Log;
import ru.agecold.util.Rnd;

public class q8108_ClanBonus extends QuestJython {

    //нпц
    private final static int NPC_FIRST = 41114;
    //проверка по hwid
    private final static boolean HWID_CHECK = true;
    //min урвоень кланлидера и сокланов
    private final static int MEMBER_LEVEL_MIN = 76;

    /////////////////////////
    private final static FastMap<Integer, ClanBonus> BONUS_LIST = new FastMap<>();
    private final static FastList<Integer> CACHE_CLANS = new FastList<>();

    public q8108_ClanBonus(int questId, String name, String descr) {
        super(questId, name, descr, 1);
        this.setInitialState(new State("Start", this));

/**
		Шаблон:
		addClanBonus(min_колво_человек, репутация, уровень, клан_скиллы, нобл_кланлидеру, шмотка_кланлидеру, анонс);
		 * клан_скиллы  - true / false
		 * нобл_кланлидеру  - true / false
		 * шмотка_кланлидеру - "итем_ид,кол-во,шанс;итем_ид,кол-во,шанс" / ""
		 * анонс - "Клан %name% добро пожаловать на сервер!" / ""
		 ** %name% - название клана
		 ** %leader% - ник кланлидера
		 ** %count% - кол-во человек
*/

        addClanBonus(5, 1, 8, true, false, "57,1,100", "Поздравляем клан %name% (кол-во людей: %count%) с получением бонуса.");
        addClanBonus(10, 1, 8, true, false, "57,1,100", "Поздравляем клан %name% (кол-во людей: %count%) с получением бонуса.");
        addClanBonus(15, 1, 8, true, false, "57,1,100", "Поздравляем клан %name% (кол-во людей: %count%) с получением бонуса.");

/**
*/

        this.addStartNpc(NPC_FIRST);
        this.addTalkId(NPC_FIRST);

        loadClanCache();
    }

    public class ClanBonus {

        public int count = 0;
        public int reputation = 0;
        public int level = 0;
        public boolean skills = true;
        public boolean noble = false;
        public FastList<EventReward> items = new FastList<>();
        public String announce = "";

        public ClanBonus(int count, int reputation, int level, boolean skills, boolean noble, String item_data, String announce) {
            this.count = count;
            this.reputation = reputation;
            this.level = level;
            this.skills = skills;
            this.noble = noble;
            this.announce = announce;

            if (!item_data.isEmpty()) {
                String[] rews = item_data.split(";");
                for (String rew : rews) {
                    if (rew == null
                            || rew.isEmpty()) {
                        continue;
                    }

                    String[] item = rew.split(",");
                    items.add(new EventReward(Integer.parseInt(item[0]), Integer.parseInt(item[1]), Integer.parseInt(item[2])));
                }
                rews = null;
            }
        }
    }

    private void addClanBonus(int count, int reputation, int level, boolean skills, boolean noble, String item_data, String announce) {
        BONUS_LIST.put(count, new ClanBonus(count, reputation, level, skills, noble, item_data, announce));
    }

    public static void main(String... arguments) {
        new q8108_ClanBonus(8108, "q8108_ClanBonus", "Clan Bonus");
    }

    @Override
    public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player) {
        if (!player.isClanLeader()) {
            return "<html><body>Нет доступных бонусов/0x01.<body><html>";
        }

        if (event.equalsIgnoreCase("home")) {
            return showWelcome(player);
        } else if (event.equalsIgnoreCase("check")) {
            return showBonusesCurrent(player);
        }

        int bonus_get = 0;
        try {
            bonus_get = Integer.parseInt(event.replaceAll("get", ""));
        } catch (Exception e) {
            bonus_get = 0;
        }

        if (bonus_get == 0) {
            return "<html><body>Нет доступных бонусов/0x02.<body><html>";
        }

        L2Clan clan = player.getClan();
        if (clan == null) {
            return "<html><body>Нет доступных бонусов/0x03.<body><html>";
        }

        int online = getCurrentOnline(player, player.getClan(), 0);
        if (online == 0) {
            return "<html><body>Нет доступных бонусов/0x04.<body><html>";
        }

        ClanBonus bonus = findClanBonus(player, online, bonus_get, null);
        if (bonus == null) {
            return "<html><body>Нет доступных бонусов/0x05.<body><html>";
        }

        TextBuilder htmltext = new TextBuilder("<html><body>");
        TextBuilder bonustext = new TextBuilder("");
        htmltext.append("Бонусер кланов<br>");

        htmltext.append("Игроков онлайн: " + online + "<br>");
        htmltext.append("Получен бонус:<br1>");

        if (bonus.level > 0
                && bonus.level > clan.getLevel()) {
            clan.changeLevel(bonus.level);
            htmltext.append("Уровень клана: " + bonus.level + ".<br1>");
            bonustext.append("Уровень клана: " + bonus.level + ";");
        }

        if (bonus.level > 0) {
            clan.addPoints(bonus.reputation);
            htmltext.append("Репутация: +" + bonus.reputation + ".<br1>");
            bonustext.append("Репутация: +" + bonus.reputation + ";");
        }

        if (bonus.skills) {
            htmltext.append("Клан скиллы: Да.<br1>");
            bonustext.append("Клан скиллы: Да;");
            CustomServerData.getInstance().addClanSkills(player, clan);
        }

        if (bonus.noble
                && !player.isNoble()) {
            player.setNoble(true);
            player.addItem("q8108_ClanBonus", 7694, 1, npc, true);
            htmltext.append("Ноблесс кланлидеру: Да.<br1>");
            bonustext.append("Ноблесс кланлидеру: Да;");
        }

        if (!bonus.items.isEmpty()) {
            for (EventReward coin : bonus.items) {
                if (coin == null) {
                    continue;
                }

                if (Rnd.get(100) > coin.chance) {
                    continue;
                }

                player.addItem("q8108_ClanBonus", coin.id, coin.count, npc, true);
                bonustext.append("Итем: " + coin.id + " " + coin.count + "шт.;");
            }
        }

        if (!bonus.announce.isEmpty()) {
            String announce = bonus.announce;
            announce = announce.replaceAll("%name%", clan.getName());
            announce = announce.replaceAll("%leader%", player.getName());
            announce = announce.replaceAll("%count%", String.valueOf(online));
            Announcements.getInstance().announceToAll(announce);
        }

        htmltext.append("</body></html>");

        CACHE_CLANS.add(clan.getClanId());
        Date date = new Date();
        SimpleDateFormat datef = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS");
        Log.add(datef.format(date) + ";" + clan.getClanId() + ";" + "Клан: " + clan.getName() + "; Бонус: " + bonustext, "q8108_ClanBonus");
        bonustext.clear();

        return htmltext.toString();
    }

    @Override
    public String onTalk(L2NpcInstance npc, L2PcInstance player) {
        QuestState st = player.getQuestState("q8108_ClanBonus");
        if (st == null) {
            return "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
        }

        if (npc.getNpcId() != NPC_FIRST) {
            return "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
        }

        if (!player.isClanLeader()) {
            return "<html><body>Только для клан-лидеров!</body></html>";
        }

        if (CACHE_CLANS.contains(player.getClanId())) {
            return "<html><body>Ваш клан уже получал бонусы.</body></html>";
        }

        if (MEMBER_LEVEL_MIN > 0
                && player.getLevel() < MEMBER_LEVEL_MIN) {
            return "<html><body>Только для игроков выше " + MEMBER_LEVEL_MIN + " уровня.</body></html>";
        }

        return showWelcome(player);
    }

    private String showWelcome(L2PcInstance player) {
        TextBuilder htmltext = new TextBuilder("<html><body>");
        htmltext.append("<title>La2-ares.pw [Бонус переходящим кланам]</title>");
        htmltext.append("<tr><td><center><img src=\"sek.gui001\" width=256 height=64></center></td><td>");

        htmltext.append("<a action=\"bypass -h Quest q8108_ClanBonus check\">Проверить бонус</a><br>");
        //htmltext.append("<a action=\"bypass -h Quest q8108_ClanBonus bonuses\">Список бонусов</a><br>");

        htmltext.append("<table width=300 border=1>");
        htmltext.append("<tr><td>Игроков</td><td>Уровень</td><td>Репутация</td><td>Скиллы</td></tr>"); //int count, int reputation, int level, boolean skills, boolean noble, String item_data, String announce
        Integer key = null;
        ClanBonus bonus = null;
        for (FastMap.Entry<Integer, ClanBonus> e = BONUS_LIST.head(), end = BONUS_LIST.tail(); (e = e.getNext()) != end;) {
            key = e.getKey();
            bonus = e.getValue();
            if (key == null
                    || bonus == null) {
                continue;
            }

            htmltext.append("<tr><td>" + bonus.count + "</td><td>" + bonus.level + "</td><td>" + bonus.reputation + "</td><td>" + (bonus.skills == true ? "Да" : "Нет") + "</td></tr>");
        }
        htmltext.append("</table>");

        htmltext.append("<br><br>Бонус можно получить 1 раз, все игроки проверяются по HWID и должны находится в игре.");
        htmltext.append("</body></html>");
        return htmltext.toString();
    }

    private String showBonusesCurrent(L2PcInstance player) {
        int online = getCurrentOnline(player, player.getClan(), 0);
        if (online == 0) {
            return "<html><body>Нет доступных бонусов/1x01.<body><html>";
        }

        ClanBonus bonus = findClanBonus(player, online, 0, null);
        if (bonus == null) {
            return "<html><body>Онлайн в клане: " + online + "<br><br>Нет доступных бонусов/1x02.<body><html>";
        }

        TextBuilder htmltext = new TextBuilder("<html><body>");
        htmltext.append("<title>La2-ares.pw [Бонус переходящим кланам]</title>");
        htmltext.append("<tr><td><center><img src=\"sek.gui001\" width=256 height=64></center></td><td>");

        htmltext.append("Игроков онлайн: " + online + "<br>");
        htmltext.append("Вам полагается:<br1>");
        htmltext.append("Уровень клана: " + bonus.level + "<br1>");
        htmltext.append("Репутация: +" + bonus.reputation + "<br1>");
        htmltext.append("Клан скиллы: " + (bonus.skills == true ? "Да" : "Нет") + "<br1>");

        htmltext.append("<br>");
        htmltext.append("<a action=\"bypass -h Quest q8108_ClanBonus get" + bonus.count + "\">Принять бонус</a>");
        htmltext.append("<br>");
        htmltext.append("<br>");
        htmltext.append("<a action=\"bypass -h Quest q8108_ClanBonus home" + bonus.count + "\">Вернуться</a>");

        htmltext.append("</body></html>");
        return htmltext.toString();
    }

    private int getCurrentOnline(L2PcInstance player, L2Clan clan, int online) {
        if (clan == null) {
            return online;
        }

        FastList<String> hwids = new FastList<>();
        for (L2PcInstance member : clan.getOnlineMembers("")) {
            if (member == null) {
                continue;
            }

            if (MEMBER_LEVEL_MIN > 0
                    && member.getLevel() < MEMBER_LEVEL_MIN) {
                continue;
            }

            if (HWID_CHECK) {
                if (hwids.contains(member.getHWID())) {
                    continue;
                }

                hwids.add(member.getHWID());
            }

            online++;
        }

        return online;
    }

    private ClanBonus findClanBonus(L2PcInstance player, int online, int count, ClanBonus bonus) {
        if (count != 0) {
            return BONUS_LIST.get(count);
        }

        if (BONUS_LIST.containsKey(online)) {
            return BONUS_LIST.get(online);
        }

        Integer key = null;
        ClanBonus value = null;
        for (FastMap.Entry<Integer, ClanBonus> e = BONUS_LIST.head(), end = BONUS_LIST.tail(); (e = e.getNext()) != end;) {
            key = e.getKey();
            value = e.getValue();
            if (key == null
                    || value == null) {
                continue;
            }

            if (online >= key) {
                bonus = value;
            }
        }

        return bonus;
    }

    private void loadClanCache() {
        CACHE_CLANS.clear();
        LineNumberReader lnr = null;
        BufferedReader br = null;
        FileReader fr = null;
        try {
            File data = new File("./log/q8108_ClanBonus.txt");
            if (!data.exists()) {
                try {
                    data.createNewFile();
                } catch (IOException e) {
                    _log.warning("[ERROR] q8108_ClanBonus, can't create ./log/q8108_ClanBonus.txt: " + e);
                }
                return;
            }

            fr = new FileReader(data);
            br = new BufferedReader(fr);
            lnr = new LineNumberReader(br);
            String line;
            while ((line = lnr.readLine()) != null) {
                if (line.trim().length() == 0 || line.startsWith("#")) {
                    continue;
                }

                String[] log = line.split(";");
                CACHE_CLANS.add(Integer.parseInt(log[1]));
            }
        } catch (final Exception e) {
            System.out.println("[ERROR] q8108_ClanBonus, loadClanCache() error: " + e);
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
                if (br != null) {
                    br.close();
                }
                if (lnr != null) {
                    lnr.close();
                }
            } catch (Exception e1) {
            }
        }
    }
}
