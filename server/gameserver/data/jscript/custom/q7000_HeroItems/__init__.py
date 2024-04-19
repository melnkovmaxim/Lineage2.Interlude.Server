# Made by DrLecter
import sys
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold.gameserver.datatables import ItemTable
qn = "q7000_HeroItems"
MONUMENTS=[31690]+range(31769,31773)

HERO_ITEMS={
6611:["weapon_the_sword_of_hero_i00","Меч Героя","Аналог меча 4-го уровня. Можно вставить или купить ЛС.","297/137","Sword"],
6612:["weapon_the_two_handed_sword_of_hero_i00","Двурук Героя","Аналог двурука 4-го уровня. Можно вставить или купить ЛС.","361/137","Double Handed Sword"],
6613:["weapon_the_axe_of_hero_i00","Молот Героя","Аналог молота 4-го уровня. Можно вставить или купить ЛС.","297/137","Blunt"],
6614:["weapon_the_mace_of_hero_i00","Магическое Героя","Аналог магического оружия 4-го уровня. Можно вставить или купить ЛС.","238/182","Blunt"],
6615:["weapon_the_hammer_of_hero_i00","Двуручный Молот Героя","Аналог двурука 4-го уровня. Можно вставить или купить ЛС.","361/137","Blunt"],
6616:["weapon_the_staff_of_hero_i00","Двуручное Магическое Героя","Аналог магического оружия 4-го уровня. Можно вставить или купить ЛС.","290/182","Blunt"],
6617:["weapon_the_dagger_of_hero_i00","Дагер Героя","Аналог дагера 4-го уровня. Можно вставить или купить ЛС.","260/137","Dagger"],
6618:["weapon_the_fist_of_hero_i00","Кастеты Героя","Аналог кастетов 4-го уровня. Можно вставить или купить ЛС.","361/137","Dual Fist"],
6619:["weapon_the_bow_of_hero_i00","Лук Героя","Аналог лука 4-го уровня. Можно вставить или купить ЛС.","614/137","Bow"],
6620:["weapon_the_dualsword_of_hero_i00","Дуалы Героя","Аналог дуалов 4-го уровня. Можно вставить или купить ЛС.","361/137","Dual Sword"],
6621:["weapon_the_pole_of_hero_i00","Пика Героя","Аналог пики 4-го уровня. Можно вставить или купить ЛС.","297/137","Pole"],
6842:["accessory_hero_cap_i00","Корона Героя","Даёт статы шлема 3-го уровня.","0","Hair Accessory"]
}

def render_list(mode,item) :
    html = "<html><body><font color=\"LEVEL\">List of Hero Items:</font><table border=0 width=300>"
    if mode == "list" :
       for i in HERO_ITEMS.keys() :
          html += "<tr><td width=35 height=45><img src=icon."+HERO_ITEMS[i][0]+" width=32 height=32 align=left></td><td valign=top><a action=\"bypass -h Quest q7000_HeroItems "+str(i)+"\"><font color=\"FFFFFF\">"+HERO_ITEMS[i][1]+"</font></a></td></tr>"
    else :
       html += "<tr><td align=left><font color=\"LEVEL\">Item Information</font></td><td align=right>\
<button value=Back action=\"bypass -h Quest q7000_HeroItems buy\" width=40 height=15 back=sek.cbui94 fore=sek.cbui92>\
</td><td width=5><br></td></tr></table><table border=0 bgcolor=\"000000\" width=500 height=160><tr><td valign=top>\
<table border=0><tr><td valign=top width=35><img src=icon."+HERO_ITEMS[item][0]+" width=32 height=32 align=left></td>\
<td valign=top width=400><table border=0 width=100%><tr><td><font color=\"FFFFFF\">"+HERO_ITEMS[item][1]+"</font></td>\
</tr></table></td></tr></table><br><font color=\"LEVEL\">Item info:</font>\
<table border=0 bgcolor=\"000000\" width=290 height=220><tr><td valign=top><font color=\"B09878\">"+HERO_ITEMS[item][2]+"</font>\
</td></tr><tr><td><br>Type:"+HERO_ITEMS[item][4]+"<br><br>Patk/Matk: "+HERO_ITEMS[item][3]+"<br><br>\
<table border=0 width=300><tr><td align=center><button value=Obtain action=\"bypass -h Quest q7000_HeroItems _"+str(item)+"\" width=60 height=15 back=sek.cbui94 fore=sek.cbui92></td></tr></table></td></tr></table></td></tr>"
    html += "</table></body></html>"
    return html

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
     if st.getPlayer().isHero():
       if event == "buy" :
          htmltext=render_list("list",0)
       elif event.isdigit() and int(event) in HERO_ITEMS.keys():
          htmltext=render_list("item",int(event))
       elif event.startswith("_") :
          item = int(event.split("_")[1])
          if item == 6842:
            if st.getQuestItemsCount(6842):
               htmltext = "Вы не можете иметь больше одного оружия или короны."
            else :
               st.giveItems(item,1)
               htmltext = "Enjoy your Wings of Destiny Circlet"
          else :
             for i in range(6611,6622):
                if st.getQuestItemsCount(i):
                   st.exitQuest(1)
                   return "У вас уже есть "+HERO_ITEMS[i][1]
             st.giveItems(item,1)
             htmltext = "Вот ваш "+HERO_ITEMS[item][1]
             st.playSound("ItemSound.quest_fanfare_2")
          st.exitQuest(1)
     return htmltext

 def onTalk (Self,npc,player):
     st = player.getQuestState(qn)
     htmltext = "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
     if player.isHero():
        htmltext=render_list("list",0)
     else :
        st.exitQuest(1)
        htmltext = "<html><body>Вы не герой!</body></html>"
     return htmltext

QUEST       = Quest(7000,qn,"Hero Items")
CREATED     = State('Start', QUEST)

QUEST.setInitialState(CREATED)

for i in MONUMENTS:
    QUEST.addStartNpc(i)
    QUEST.addTalkId(i)
