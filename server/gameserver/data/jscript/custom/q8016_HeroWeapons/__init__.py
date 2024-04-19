from java.util import Iterator

from ru.agecold.util import Rnd
from ru.agecold.gameserver.network.serverpackets import SystemMessage
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

from java.lang import System

qn = "q8016_HeroWeapons"

NPC = 31688
ITEM = 12222
COST = 2
INAME = "Donate Coin"
HERO_ITEMS = {
6611:["weapon_the_sword_of_hero_i00","Infinity Blade","During a critical attack, decreases one's P. Def and increases de-buff casting ability, damage shield effect, Max HP, Max MP, Max CP, and shield defense power. Also enhances damage to target during PvP.","297/137","Sword"],
6612:["weapon_the_two_handed_sword_of_hero_i00","Infinity Cleaver","Increases Max HP, Max CP, critical power and critical chance. Inflicts extra damage when a critical attack occurs and has possibility of reflecting the skill back on the player. Also enhances damage to target during PvP.","361/137","Double Handed Sword"],
6613:["weapon_the_axe_of_hero_i00","Infinity Axe","During a critical attack, it bestows one the ability to cause internal conflict to one's opponent. Damage shield function, Max HP, Max MP, Max CP as well as one's shield defense rate are increased. It also enhances damage to one's opponent during PvP.","297/137","Blunt"],
6614:["weapon_the_mace_of_hero_i00","Infinity Rod","When good magic is casted upon a target, increases MaxMP, MaxCP, Casting Spd, and MP regeneration rate. Also recovers HP 100% and enhances damage to target during PvP.","238/182","Blunt"],
6615:["weapon_the_hammer_of_hero_i00","Infinity Crusher","Increases MaxHP, MaxCP, and Atk. Spd. Stuns a target when a critical attack occurs and has possibility of reflecting the skill back on the player. Also enhances damage to target during PvP.","361/137","Blunt"],
6616:["weapon_the_staff_of_hero_i00","Infinity Scepter","When casting good magic, it can recover HP by 100% at a certain rate, increases MAX MP, MaxCP, M. Atk., lower MP Consumption, increases the Magic Critical rate, and reduce the Magic Cancel. Enhances damage to target during PvP.","290/182","Blunt"],
6617:["weapon_the_dagger_of_hero_i00","Infinity Stinger","Increases MaxMP, MaxCP, Atk. Spd., MP regen rate, and the success rate of Mortal and Deadly Blow from the back of the target. Silences the target when a critical attack occurs and has Vampiric Rage effect. Also enhances damage to target during PvP.","260/137","Dagger"],
6618:["weapon_the_fist_of_hero_i00","Infinity Fang","Increases MaxHP, MaxMP, MaxCP and evasion. Stuns a target when a critical attack occurs and has possibility of reflecting the skill back on the player at a certain probability rate. Also enhances damage to target during PvP.","361/137","Dual Fist"],
6619:["weapon_the_bow_of_hero_i00","Infinity Bow","Increases MaxMP/MaxCP and decreases re-use delay of a bow. Slows target when a critical attack occurs and has Cheap Shot effect. Also enhances damage to target during PvP.","614/137","Bow"],
6620:["weapon_the_dualsword_of_hero_i00","Infinity Wing","When a critical attack occurs, increases MaxHP, MaxMP, MaxCP and critical chance. Silences the target and has possibility of reflecting the skill back on the target. Also enhances damage to target during PvP.","361/137","Dual Sword"],
6621:["weapon_the_pole_of_hero_i00","Infinity Spear","During a critical attack, increases MaxHP, Max CP, Atk. Spd. and Accuracy. Casts dispel on a target and has possibility of reflecting the skill back on the target. Also enhances damage to target during PvP.","297/137","Pole"],
}


class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onAdvEvent (self,event,npc,player):
   st = player.getQuestState(qn)
   if event == "spisok":
     if st.getQuestItemsCount(ITEM) < COST:
       htmltext = "<html><body>Обмен пушек стоит <font color=74bff5>"+str(COST)+" "+INAME+"</font></body></html>"
       return htmltext
     elif not player.isHero():
       htmltext = "<html><body>Вы не герой..)</font></body></html>"
       return htmltext
     htmltext = "<html><body><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><img src=\"L2UI.SquareBlank\" width=260 height=2><br1>"       
     htmltext += "<table width=260><tr><td><font color=LEVEL>Обмен хиро-пушек</font></td></tr></table><br>"
     count = 0
     for i in HERO_ITEMS.keys():
       if st.getQuestItemsCount(i) == 1:
         count=1
         myweapon = i
         st.set("my",str(myweapon))
         if count == 1:
           htmltext += "<table width=260><tr><td></td><td>Ваша пушка</td></tr>"
           htmltext += "<tr><td><img src=icon."+HERO_ITEMS[i][0]+" width=32 height=32></td><td><font color=7fff00>"+HERO_ITEMS[i][1]+"</font></td></tr>"
         else:
           htmltext = "<html><body>Нечего менять</font></body></html>"
           return htmltext 
     htmltext += "<tr><td></td><td>На</td></tr>"
     for k in HERO_ITEMS.keys():
       if k != myweapon:
         htmltext += "<tr><td><img src=icon."+HERO_ITEMS[k][0]+" width=32 height=32></td><td><a action=\"bypass -h Quest q8016_HeroWeapons step2_"+str(k)+"\">"+HERO_ITEMS[k][1]+"</a></td></tr>"
     htmltext += "</table><br><br>"
     htmltext += "<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></body></html>"
   elif event.startswith("step2_"):
     weapon2 = int(event.replace("step2_", ""))
     st.set("want",str(weapon2))
     htmltext = "<html><body>Обмен хиро-пушек:<br>Берем эту?<br><br><table width=300>"
     htmltext += "<tr><td><img src=\"Icon."+HERO_ITEMS[weapon2][0]+"\" width=32 height=32></td><td>"+HERO_ITEMS[weapon2][1]+"</td></tr></table><br>"
     htmltext += "P.atk/M.atk: "+HERO_ITEMS[weapon2][3]+"<br>"
     htmltext += "Описание:<br1>"
     htmltext += ""+HERO_ITEMS[weapon2][2]+"<br><br>"
     htmltext += "<a action=\"bypass -h Quest q8016_HeroWeapons step3\">Обменять.</a><br><br>"
     htmltext += "<a action=\"bypass -h Quest q8016_HeroWeapons spisok\">Вернуться.</a></body></html>"
   elif event.startswith("step3"):
     weapon1 = st.getInt("my")
     weapon2 = st.getInt("want")
     st.takeItems(ITEM,COST)
     st.takeItems(weapon1,1)
     st.giveItems(weapon2,1)
     htmltext =  "<html><body>Обмен хиро-пушек:<br>Готово.<br></body></html>"
   else:
     htmltext = "<html><body>Обмен хиро-пушек:<br>Oops!</body></html>"
   return htmltext

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()
   if npcId == NPC:
     self.startQuestTimer("spisok",100,None,player)      
   return

QUEST       = Quest(8016,qn,"custom")
CREATED     = State('Start', QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(NPC)
QUEST.addTalkId(NPC)
