package commands.voice;

import javolution.text.TextBuilder;
//import org.strixplatform.configs.MainConfig;
//import org.strixplatform.managers.ClientBanManager;
//import org.strixplatform.utils.BannedHWIDInfo;
import ru.agecold.gameserver.Announcements;
import ru.agecold.gameserver.GmListTable;
import ru.agecold.gameserver.LoginServerThread;
import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.L2World;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.entity.olympiad.Olympiad;
import ru.agecold.gameserver.network.gameserverpackets.ChangeAccessLevel;
import ru.agecold.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.agecold.gameserver.util.AutoBan;
import ru.agecold.gameserver.util.Util;
import ru.agecold.util.Log;
import ru.agecold.util.Rnd;
import scripts.commands.IVoicedCommandHandler;
import scripts.commands.VoicedCommandHandler;
//import smartguard.core.entity.Hwid;
//import smartguard.core.utils.LogUtils;
//import smartguard.core.xml.impl.BanParser.Ban;
//import smartguard.spi.SmartGuardSPI;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("all")
public class BotReport implements IVoicedCommandHandler {

    private static final String[] VOICED_COMMANDS = {"bot", "bot_", "reports"};

    //уровень доступа игрока
    private static final int PLAYER_ACCESS = 30;

    //в каком радиусе должен находиться игрок? 0 - откл
    private static final int REPORT_RADIUS = 1200;

    //на сколько секунд дается рандомный ник после телепорта к игроку? 0 - откл
    private static final long RANDOM_NAME_PLAYER = TimeUnit.SECONDS.toMillis(30);

    //инвиз после телепорта к игроку? false - откл
    private static final boolean INVIS_PLAYER = true;

    /**
     **Тип назакания
     */
    // 1. Тюрьма
    private static final boolean PENALTY_JAIL = true;
    //Продолжительность наказания, минуты
    private static final long PENALTY_JAIL_TIME = TimeUnit.MINUTES.toMillis(15);

    // 2. Бан аккаунта
    private static final boolean PENALTY_BAN_ACC = false;

    // 3. Бан по HWID
    private static final boolean PENALTY_BAN_HWID = false;
    //тип защиты; SG - smartguard, LG - lameguard, SXG - strixguard, ACG - Anti Cheat Guard
    private static final String GUARD_TYPE = "ACG";

    // анонс о бане
    private static final boolean PENALTY_ANNOUNCE = true;

    ////////////
    private static final boolean LOCALHOST_TEST = false; //тестирование на локалке - отключает баны
    private static final Map <Integer, Integer> _reports = new ConcurrentHashMap <> ();

    @Override
    public boolean useVoicedCommand(String command, L2PcInstance player, String target) {
        if (!(command.equalsIgnoreCase("bot") || command.equalsIgnoreCase("bot_add"))
                && player.getAccessLevel() < PLAYER_ACCESS) {
            return false;
        }

        //System.out.println("#bot#1##" + command + "####");
        if (command.equalsIgnoreCase("bot")) {
            reportBot(player, player.getTarget(), false);
        } else if (command.equalsIgnoreCase("reports")) {
            showReports(player);
        } else if (command.startsWith("bot_")) {
            String choise = command.substring(4);
            if (choise.equalsIgnoreCase("add")) {
                reportBot(player, player.getTarget(), true);
            } else if (choise.startsWith("show")) {
                //System.out.println("#bot#2##" + choise + "####");
                int id = 0;
                String p = choise.substring(5);
                try {
                    id = Integer.parseInt(p);
                } catch (Exception e) {
                    id = 0;
                }
                //System.out.println("#bot#3##" + id + "####");
                showPlayer(player, id);
            } else if (choise.startsWith("res_")) {
                String[] res = choise.substring(4).split(" ");
                int id = 0;
                try {
                    id = Integer.parseInt(res[1]);
                } catch (Exception e) {
                    id = 0;
                }
                resultPlayer(player, id, res[0].equalsIgnoreCase("yes"));
            } else if (choise.startsWith("tele")) {
                //System.out.println("#bot#2##" + choise + "####");
                int id = 0;
                String p = choise.substring(5);
                try {
                    id = Integer.parseInt(p);
                } catch (Exception e) {
                    id = 0;
                }
                //System.out.println("#bot#3##" + id + "####");
                teleToPlayer(player, id);
            }
        }
        return true;
    }

    private void reportBot(L2PcInstance player, L2Object target, boolean done) {
        if (target == null
                || !target.isPlayer()) {
            player.sendHtmlMessage("Возьмите игрока в таргет.");
            return;
        }

        L2PcInstance bot = target.getPlayer();
		if (bot == null) {
			player.sendHtmlMessage("Неправильная цель");
			return;
		}

		if (!LOCALHOST_TEST) {
			if (bot.getObjectId() == player.getObjectId()
					|| bot.isGM()) {
				player.sendHtmlMessage("Неправильная цель");
				return;
			}
		}

        if (REPORT_RADIUS > 0
                && !Util.checkIfInRange(REPORT_RADIUS, player, bot, true)) {
            player.sendHtmlMessage("Игрок слишком далеко.");
            return;
        }

        NpcHtmlMessage nhm = new NpcHtmlMessage(5);
        TextBuilder build = new TextBuilder("<html><body>");

        if (done) {
            updateReportCount(bot.getObjectId(), _reports.get(bot.getObjectId()), 0);
            build.append("<center><font color=\"LEVEL\">" + bot.getName() + "</font></center><br><br>");
            build.append("Запрос отправлен, спасибо за бдительность.");

            GmListTable.broadcastMessageToGMs("Игрок " + player.getName() + " сообщил о боте: " + bot.getName());
            Log.add("Игрок " + player.getName() + " сообщил о боте: " + bot.getFingerPrints(), "cheats/bot_report");
        } else {
            build.append("<title>Система Репортов </title><center>");
			build.append("<center><img src=\"sek.gui001\" width=\"256\" height=\"64\"><br></center>");
			build.append("<img src=\"l2ui.squaregray\" width=\"295\" height=\"1\"><table width=300><tr><td><center><font color=\"LEVEL\">Игрок:</font><font color=\"00FF00\"> "+ bot.getName() +"</font></center></td></tr></table><img src=\"l2ui.squaregray\" width=\"295\" height=\"1\"><br>");
            build.append("<center><font color=\"LEVEL\">Если ты уверен, что игрок бот - жми кнопку!</font></center><br>");
            build.append("<center><button value=\"Сообщить\" action=\"bypass -h vch_bot_add\" width=210 height=21 back=\"sek.cbui75\" fore=\"sek.cbui75\"></center><br>");
			build.append("<img src=\"l2ui.squaregray\" width=\"295\" height=\"1\"><table width=295 bgcolor=\"000000\">");
			build.append("<tr><td align=center><font color=\"d48304\">Будьте уверены в своих жалобах на игроков!</font></td><br1></tr>");
			build.append("<tr><td align=center><font color=\"ff0000\">Все репорты проверяются администрацией!</font></td><br1></tr>");
			build.append("<tr><td align=center><font color=\"LEVEL\">За злоупотребление репортами - бан железа.</font></td><br1></tr>");
			build.append("</table></center><img src=\"l2ui.squaregray\" width=\"295\" height=\"1\"><br>");
        }

        build.append("</body></html>");
        nhm.setHtml(build.toString());
        player.sendPacket(nhm);
    }

    private void updateReportCount(int id, Integer current, int count) {
        if (current != null) {
            count = current;
        }

        count += 1;
        _reports.put(id, count);
    }

    private void showReports(L2PcInstance player) {
        NpcHtmlMessage nhm = new NpcHtmlMessage(5);
        TextBuilder build = new TextBuilder("<html><body>");
        build.append("<center><font color=\"LEVEL\">Список репортов</font></center><br><br>");

        if (_reports.isEmpty()) {
            build.append("Нет активных заявок.");
            build.append("</body></html>");
            nhm.setHtml(build.toString());
            player.sendPacket(nhm);
            return;
        }

        build.append("<table width=290><tr><td width=170>Игрок</font></td><td width=60>Жалоб</td>><td></td></tr>");

        ValueComparator vc = new ValueComparator(_reports);
        TreeMap<Integer, Integer> sortedReports = new TreeMap<>(vc);
        sortedReports.putAll(_reports);

        int i = 0;
        Integer id;
        Integer count;
        L2PcInstance bot;
        for (Map.Entry<Integer, Integer> entry : sortedReports.entrySet()) {
            id = entry.getKey();
            count = entry.getValue();
            if (id == null
                    || count == null) {
                continue;
            }

            if (i > 15) {
                break;
            }

            bot = L2World.getInstance().getPlayer(id);
            if (bot == null) {
                continue;
            }

            i++;
            build.append("<tr><td width=180>" + bot.getName() + "</font></td><td>" + count + "</font></td><td><button value=\"Показать\" action=\"bypass -h vch_bot_show " + bot.getObjectId() + "\" width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        }

        build.append("</table></body></html>");
        nhm.setHtml(build.toString());
        player.sendPacket(nhm);
    }

    static class ValueComparator implements Comparator<Integer> {

        Map<Integer, Integer> base;

        public ValueComparator(Map<Integer, Integer> base) {
            this.base = base;
        }

        @Override
        public int compare(Integer a, Integer b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private void showPlayer(L2PcInstance player, int id) {
        if (id == 0) {
            player.sendHtmlMessage("Игрок не найден.");
            return;
        }

        if (!_reports.containsKey(id)) {
            player.sendHtmlMessage("Заявка уже обработана.");
            return;
        }

        L2PcInstance bot = L2World.getInstance().getPlayer(id);
        if (bot == null) {
            player.sendHtmlMessage("Игрок не в игре.");
            return;
        }

        NpcHtmlMessage nhm = new NpcHtmlMessage(5);
        TextBuilder build = new TextBuilder("<html><body>");
        build.append("<center><font color=\"LEVEL\">" + bot.getName() + "</font></center><br><br>");
        build.append("Бот?<br><br>");

        build.append("<button value=\"Телепорт\" action=\"bypass -h vch_bot_tele " + id + "\" width=75 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br><br>");
        build.append("<button value=\"Подтвердить\" action=\"bypass -h vch_bot_res_yes " + id + "\" width=75 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br><br>");
        build.append("<button value=\"Не бот\" action=\"bypass -h vch_bot_res_no " + id + "\" width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");

        build.append("</body></html>");
        nhm.setHtml(build.toString());
        player.sendPacket(nhm);
    }

    private void teleToPlayer(L2PcInstance player, int id) {
        if (id == 0) {
            player.sendHtmlMessage("Игрок не найден.");
            return;
        }

        if (!_reports.containsKey(id)) {
            player.sendHtmlMessage("Заявка уже обработана.");
            return;
        }

        L2PcInstance bot = L2World.getInstance().getPlayer(id);
        if (bot == null) {
            player.sendHtmlMessage("Игрок не в игре.");
            return;
        }

        if (INVIS_PLAYER) {
            player.setChannel(0);
        }

        player.setMaskName(sha1(String.valueOf(Rnd.get(10000, 99999))).substring(0, Rnd.get(5, 12)), sha1(String.valueOf(Rnd.get(10000, 99999))).substring(0, Rnd.get(3, 8)), true);
        player.teleToLocation(bot.getLoc(), false);

        if (RANDOM_NAME_PLAYER > 0) {
            ThreadPoolManager.getInstance().scheduleGeneral(new RestoreName(player), RANDOM_NAME_PLAYER);
        }

        showPlayer(player, id);
    }

    private void resultPlayer(L2PcInstance player, int id, boolean confirm) {
        if (id == 0) {
            player.sendHtmlMessage("Игрок не найден.");
            return;
        }

        if (!_reports.containsKey(id)) {
            player.sendHtmlMessage("Заявка уже обработана.");
            return;
        }

        L2PcInstance bot = L2World.getInstance().getPlayer(id);
        if (bot == null) {
            player.sendHtmlMessage("Игрок не в игре.");
            return;
        }

        NpcHtmlMessage nhm = new NpcHtmlMessage(5);
        TextBuilder build = new TextBuilder("<html><body>");
        build.append("<center><font color=\"LEVEL\">" + bot.getName() + "</font></center><br><br>");

        if (confirm) {
            build.append("Признан ботом и наказан.");

            if (PENALTY_ANNOUNCE) {
                String announce = bot.getName() + " " + (PENALTY_JAIL ? "тюрьма, " : "");
                if (PENALTY_BAN_ACC
                        || PENALTY_BAN_HWID) {
                    announce += "бан " + (PENALTY_BAN_ACC ? "персонажей, " : "") + "" + (PENALTY_BAN_HWID ? "железа" : "") + ".";
                }
                announce += " Фарм на боте.";
                Announcements.getInstance().announceToAll(announce);
            }

			if (!LOCALHOST_TEST) {
				if (PENALTY_JAIL) {
					bot.setInJail(true, (int) PENALTY_JAIL_TIME);
				}

				if (PENALTY_BAN_ACC) {
                    try
                    {
                        LoginServerThread.getInstance().sendPacket(new ChangeAccessLevel(bot.getAccountName(), 0));
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                    if (!PENALTY_BAN_HWID) {
                        Olympiad.clearPoints(bot.getObjectId());
                        bot.kick();
                    }
                }

				if (PENALTY_BAN_HWID) {
					Olympiad.clearPoints(bot.getObjectId());
					switch (GUARD_TYPE) {
						//case "SG":
							//BanManager.banHWID("BotReport", bot, "Фарм на боте."); // ("кто забанил", bot, "причина")
                            //Hwid hwid = new Hwid(bot.getHWID());
                            //String reason = "Фарм на боте.";
                            //SmartGuardSPI.getSmartGuardService().getBanManager().addBan(new Ban(hwid, reason), true);
                            //LogUtils.log("Admin '%s' has banned HWID %S, reason: '%s'", new Object[] { bot.getName(), hwid, reason });
							//break;
						//case "LG":
						//	Log.banHWID(bot.getHWID(), bot.getIP(), bot.getAccountName());
						//	break;
						//case "CG":
						//	CatsGuard.getInstance().ban(bot);
						//	break;
                        //case "SXG":
                        //    final Long time = System.currentTimeMillis() + MainConfig.STRIX_PLATFORM_AUTOMATICAL_BAN_TIME * 60L * 1000L;
                        //    final String reason = "Фарм на боте.";
                        //    final BannedHWIDInfo bhi = new BannedHWIDInfo(bot.getClient().getStrixClientData().getClientHWID(), time, reason, "none");
                        //    ClientBanManager.getInstance().tryToStoreBan(bhi);
                        //    final String bannedOut = "Player [Name:{" + bot.getName() + "}HWID:{" + bot.getClient().getStrixClientData().getClientHWID() + "}] banned on [" + time + "] minutes from [" + reason + "] reason.";
                        //    player.sendAdmResultMessage(bannedOut);
                        //    org.strixplatform.logging.Log.audit(bannedOut);
                        //    bot.sendMessage("You banned on [" + time + "] minutes. Reason: " + reason);
                        //    bot.kick();
                        //    break;
                        case "ACG":
                            final Long time = 1000L * 60L * 60L * 24L * 365 + System.currentTimeMillis();
                            final String reason = "Фарм на боте.";
                            if(AutoBan.addHwidBan(bot.getName(), bot.getHWID(), reason, time, "none"))
                                player.sendAdmResultMessage("Player [Name:{" + bot.getName() + "}HWID:{" + bot.getHWID() + "}] banned on [" + time + "] minutes from [" + reason + "] reason.");
                            bot.sendMessage("You banned " + bot.getName() + " by hwid.");
                            bot.kick();
                            break;
					}

					//bot.kick();
				}
            }

            Log.add("ГМ: " + player.getName() + " признал ботом игрока: " + bot.getFingerPrints(), "cheats/bot_report_result");
        } else {
            build.append("Расследование отменено.");
            Log.add("ГМ: " + player.getName() + " решил, что игрок не бот: " + bot.getFingerPrints(), "cheats/bot_report_result");
        }

        _reports.remove(id);

        build.append("</body></html>");
        nhm.setHtml(build.toString());
        player.sendPacket(nhm);
    }

    public static class RestoreName implements Runnable {

        public L2PcInstance player;

        public RestoreName(L2PcInstance player) {
            this.player = player;
        }

        @Override
        public void run() {
            if (player == null) {
                return;
            }

            if (INVIS_PLAYER && player.isInvisible()) {
                player.setChannel(0);
            }

            player.setMaskName("", "", false);
            player.broadcastUserInfo();
        }
    }

    public static String sha1(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(s.getBytes());
            s = bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException ex) {
            //
        }
        return s;
    }

    public static String bytesToHex(byte[] b) {
        char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder buf = new StringBuilder();
        for (int j = 0; j < b.length; j++) {
            buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
            buf.append(hexDigit[b[j] & 0x0f]);
        }
        return buf.toString();
    }

    @Override
    public String[] getVoicedCommandList() {
        return VOICED_COMMANDS;
    }

    public static void main(String... arguments) {
        VoicedCommandHandler.getInstance().registerVoicedCommandHandler(new BotReport());
    }
}
