package quests.q8105_LotteryCoin;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import ru.agecold.gameserver.Announcements;

import ru.agecold.gameserver.cache.HtmCache;
import ru.agecold.gameserver.datatables.ItemTable;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.QuestState;
import ru.agecold.gameserver.model.quest.State;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.agecold.util.Rnd;

public class q8105_LotteryCoin extends QuestJython {

    //Подходим к нпц
    private final static int NPC_FIRST = 100102;

    //взымается плата (item1;item2)
    //Шаблон: итем_ид,кол-во;итем_ид,кол-во;итем_ид,кол-во
    private final static String PAYMENTS = "9986,500;9985,1000";

    //нпц предлагает тебе угадать число от 1 до 5
    private final static int RANDOM_NUM_A = 1;
    private final static int RANDOM_NUM_B = 3;

    //если угадал - дает награду
    //Шаблон: итем_ид,кол-во,шанс;итем_ид,кол-во,шанс;итем_ид,кол-во,шанс
    private final static String REWARDS = "9224,1,100";

    //джек пот
    //каждый JACK_POT угадавший; 0 - Откл.
    private final static int JACK_POT = 40;
    //отдельный приз
    //Шаблон: итем_ид,кол-во,шанс;итем_ид,кол-во,шанс;итем_ид,кол-во,шанс
    private final static String JACK_POT_REWARDS = "9995,1,100";
    //и анонс на весь серв
    private final static boolean JACK_POT_ANNOUNCE = true;

    /////////////////////////
    private String JACK_POT_WINNER = "";
    private volatile int JACK_POT_COUNT = 0;
    private final static FastList<RewardPrize> REWARD_LIST = new FastList<>();
    private final static FastList<PaymentCoin> PAYMENT_LIST = new FastList<>();
    private final static FastList<RewardPrize> JACK_POT_LIST = new FastList<>();

    public class RewardPrize {

        public int id = 0;
        public int count = 0;
        public int chance = 0;
        public int chance2 = 0;
        public String name = "";
        public String icon = "";

        public RewardPrize(int id, int count, int chance, String name, String icon) {
            this.id = id;
            this.count = count;
            this.chance = chance;
            this.chance2 = chance * 1000;
            this.name = name;
            this.icon = icon;
        }
    }

    public class PaymentCoin {

        public int id = 0;
        public int count = 0;
        public String name = "";
        public String icon = "";

        public PaymentCoin(int id, int count, String name, String icon) {
            this.id = id;
            this.count = count;
            this.name = name;
            this.icon = icon;
        }
    }

    public q8105_LotteryCoin(int questId, String name, String descr) {
        super(questId, name, descr, 1);
        this.setInitialState(new State("Start", this));

        ItemTable it = ItemTable.getInstance();
        String[] rews = REWARDS.split(";");
        for (String rew : rews) {
            if (rew == null
                    || rew.isEmpty()) {
                continue;
            }

            String[] item = rew.split(",");
            addReward(Integer.parseInt(item[0]), Integer.parseInt(item[1]), Integer.parseInt(item[2]), it);
        }
        rews = null;

        rews = JACK_POT_REWARDS.split(";");
        for (String rew : rews) {
            if (rew == null
                    || rew.isEmpty()) {
                continue;
            }

            String[] item = rew.split(",");
            addRewardJackPot(Integer.parseInt(item[0]), Integer.parseInt(item[1]), Integer.parseInt(item[2]), it);
        }
        rews = null;

        rews = PAYMENTS.split(";");
        for (String rew : rews) {
            if (rew == null
                    || rew.isEmpty()) {
                continue;
            }

            String[] item = rew.split(",");
            addPayment(Integer.parseInt(item[0]), Integer.parseInt(item[1]), it);
        }

        this.addStartNpc(NPC_FIRST);
        this.addTalkId(NPC_FIRST);
    }

    private void addReward(int id, int count, int chance, ItemTable it) {
        REWARD_LIST.add(new RewardPrize(id, count, chance, it.getItemName(id), it.getTemplate(id).getIcon()));
    }

    private void addRewardJackPot(int id, int count, int chance, ItemTable it) {
        JACK_POT_LIST.add(new RewardPrize(id, count, chance, it.getItemName(id), it.getTemplate(id).getIcon()));
    }

    private void addPayment(int id, int count, ItemTable it) {
        PAYMENT_LIST.add(new PaymentCoin(id, count, it.getItemName(id), it.getTemplate(id).getIcon()));
    }

    public static void main(String... arguments) {
        new q8105_LotteryCoin(8105, "q8105_LotteryCoin", "Lottery Coin");
    }

    @Override
    public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player) {
		//System.out.println("##" + event);
        if (event.equalsIgnoreCase("home")) {
            return showWelcome(npc, player);
        } else if (event.equalsIgnoreCase("price")) {
            return showPayments(player);
        }

        int user_num = -1;
        try {
            user_num = Integer.parseInt(event.split(" ")[1]);
        } catch (Exception e) {
            user_num = -1;
        }

        if (user_num == -1) {
            return "<html><body>Произошла ошибка.<body><html>";
        }

        for (PaymentCoin coin : PAYMENT_LIST) {
            if (coin == null) {
                continue;
            }

            if (player.getItemCount(coin.id) < coin.count) {
                return showPayments(player);
            }
        }

        for (PaymentCoin coin : PAYMENT_LIST) {
            if (coin == null) {
                continue;
            }

            if (!player.destroyItemByItemId("q8105_LotteryCoin", coin.id, coin.count, player, true)) {
                return showPayments(player);
            }
        }

        int korean_random = Rnd.get(RANDOM_NUM_A, RANDOM_NUM_B);
        if (korean_random != user_num) {
            TextBuilder htmltext = new TextBuilder("<html><body>");

            htmltext.append("<title>L2MAD [Шлем Гладиатора]</title>");
            htmltext.append("<tr><td><center><img src=\"mad_l.ml\" width=256 height=128></center></td><td>");
            htmltext.append("К сожалению, Вы не угадали.<br><br>");
            htmltext.append("Выпало: " + korean_random + "<br1>");
            htmltext.append("Вы выбрали: " + user_num + "<br><br>");
            /*htmltext.append("А могли бы выиграть:<br>");
            htmltext.append("<table width=280><tr><td width=32></td><td width=132>Приз</td></tr>");

            for (RewardPrize coin : REWARD_LIST) {
                if (coin == null) {
                    continue;
                }

                if (Rnd.get(100000) > coin.chance2) {
                    continue;
                }

                htmltext.append("<tr><td><img src=\"" + coin.icon + "\" width=32 height=32></td><td>" + coin.name + "(" + coin.count + ")</td></tr>");
            }

            htmltext.append("</table><br><br>");*/

            htmltext.append("<a action=\"bypass -h Quest q8105_LotteryCoin home\">Вернуться</a>");

            htmltext.append("</body></html>");
			player.sendActionFailed();
			return htmltext.toString();
        }

        FastList<RewardPrize> rewarded = new FastList<>();
        for (RewardPrize coin : REWARD_LIST) {
            if (coin == null) {
                continue;
            }

            if (Rnd.get(100000) > coin.chance2) {
                continue;
            }

            rewarded.add(coin);
            player.addItem("q8105_LotteryCoin", coin.id, coin.count, npc, true);
        }

        if (JACK_POT > 0) {
            JACK_POT_COUNT++;
            if (JACK_POT_COUNT == JACK_POT) {
                JACK_POT_COUNT = 0;
				JACK_POT_WINNER = player.getName();
                for (RewardPrize coin : JACK_POT_LIST) {
                    if (coin == null) {
                        continue;
                    }

                    if (Rnd.get(100000) > coin.chance2) {
                        continue;
                    }

                    rewarded.add(coin);
                    player.addItem("q8105_LotteryCoin-JackPot", coin.id, coin.count, npc, true);
                }
                if (JACK_POT_ANNOUNCE) {
                    Announcements.getInstance().announceToAll("Игрок " + player.getName() + " сорвал джекпот!!!");
                }
            }
        }

        TextBuilder htmltext = new TextBuilder("<html><body>");

        htmltext.append("<title>L2MAD [Шлем Гладиатора]</title>");
        htmltext.append("<tr><td><center><img src=\"mad_l.ml\" width=256 height=128></center></td><td>");
        htmltext.append("Поздравляем, Вы угадали!<br>");
        htmltext.append("Вы выиграли:<br>");

        htmltext.append("<table width=280>");
        for (RewardPrize coin : rewarded) {
            if (coin == null) {
                continue;
            }
            htmltext.append("<tr><td width=32><img src=\"" + coin.icon + "\" width=32 height=32></td><td>" + coin.name + "(" + coin.count + ")</td></tr>");
        }
        htmltext.append("</table><br>");

        htmltext.append("<a action=\"bypass -h Quest q8105_LotteryCoin home\">Вернуться</a>");

        htmltext.append("</body></html>");
        player.sendActionFailed();
        return htmltext.toString();
    }

    @Override
    public String onTalk(L2NpcInstance npc, L2PcInstance player) {
        QuestState st = player.getQuestState("q8105_LotteryCoin");
        if (st == null) {
            return "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
        }

        if (npc.getNpcId() != NPC_FIRST) {
            return "";
        }

        return showWelcome(npc, player);
    }

    private String showWelcome(L2NpcInstance npc, L2PcInstance player) {
		String htmltext = HtmCache.getInstance().getHtm("data/scripts/quests/q8105_LotteryCoin/100102.htm");
        htmltext = htmltext.replaceAll("%MIN%", String.valueOf(RANDOM_NUM_A));
        htmltext = htmltext.replaceAll("%MAX%", String.valueOf(RANDOM_NUM_B));
        htmltext = htmltext.replaceAll("%JACK_POT%", String.valueOf(JACK_POT));
        htmltext = htmltext.replaceAll("%JACK_POT_PLAYER%", String.valueOf(JACK_POT_WINNER));
		//
        //htmltext = htmltext.replaceAll("\n", "");
        //htmltext = htmltext.replaceAll("\r", "");
        NpcHtmlMessage npcReply = NpcHtmlMessage.id(5);
        npcReply.setHtml(htmltext);
        player.sendUserPacket(npcReply);
		return null;
        //return htmltext;

        /*TextBuilder htmltext = new TextBuilder("<html><body>");

        htmltext.append("Угадай число*!<br>");
        htmltext.append("Список возможных наград:<br>");
        htmltext.append("<table width=280><tr><td width=32></td><td width=132>Приз</td><td>Шанс, %</td></tr>");

        for (RewardPrize coin : REWARD_LIST) {
            if (coin == null) {
                continue;
            }

            htmltext.append("<tr><td><img src=\"" + coin.icon + "\" width=32 height=32></td><td>" + coin.name + "(" + coin.count + ")</td><td>" + coin.chance + "</td></tr>");
        }

        htmltext.append("</table><br><br>");

        htmltext.append("Введите число:<br>");
        htmltext.append("<edit var=\"num\" width=200 length=\"1\"><br>");
        htmltext.append("<a action=\"bypass -h Quest q8105_LotteryCoin play $num\">Играть</a>");

        if (!JACK_POT_WINNER.isEmpty()) {
            htmltext.append("<br><br>Последний победитель ДжекПот: " + JACK_POT_WINNER);
        }

        htmltext.append("<br><br><a action=\"bypass -h Quest q8105_LotteryCoin price\">Стоимость участия</a>");
        htmltext.append("<br><br>* для игроков старше 18 уровня!");

        htmltext.append("</body></html>");
        return htmltext.toString();*/
    }

    private String showPayments(L2PcInstance player) {
        TextBuilder htmltext = new TextBuilder("<html><body>");

        htmltext.append("<title>L2MAD [Шлем Гладиатора]</title>");
        htmltext.append("<tr><td><center><img src=\"mad_l.ml\" width=256 height=128></center></td><td>");
        htmltext.append("Стоимость участия:<br1>");
        htmltext.append("<table width=280><tr><td width=32></td><td width=132></td></tr>");

        for (PaymentCoin coin : PAYMENT_LIST) {
            if (coin == null) {
                continue;
            }

            htmltext.append("<tr><td><img src=\"" + coin.icon + "\" width=32 height=32></td><td><font color=LEVEL>" + coin.count + " " + coin.name + "</font></td></tr>");
        }

        htmltext.append("</table><br><br>");
        htmltext.append("<a action=\"bypass -h Quest q8105_LotteryCoin home\">Вернуться</a>");
        htmltext.append("</body></html>");
        player.sendActionFailed();
        return htmltext.toString();
    }
}
