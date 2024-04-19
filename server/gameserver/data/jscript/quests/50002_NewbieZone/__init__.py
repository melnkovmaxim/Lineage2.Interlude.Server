# -*- coding: cp1251 -*-
import sys
from ru.agecold import Config
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

qn = "50002_NewbieZone"
#НПЦ Старт квеста - указан ИД в строке.
Starter = 41111

#Приз за успешное убийство монстра.указать ИД.
ItemID = 57
#Количество вещей дающихся за успешное убийство монстра. Указать количество - Внимание, не указывайте слишком высокие цифры если дело идет о вещах которые не складываются в одну ячейку, это может повредить серверу!
Item_AMOUNT = 1
#IУкажите ИД вещей запрещенных на вход в евент.
#Пример указывания ИД запрещенных вещей:
# RI = [ ID, ID2, ID3...]

class Quest (JQuest):
    def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

    def onKill (self,npc,player,isPet):
        npcId = npc.getNpcId()
        st = player.getQuestState(qn)
        if npc.getNpcId() in [Monsters] :
            #Выставляем ИД вещей которые НЕЛЬЗЯ использовать на квесте.
            if st.getQuestItemsCount(13336) >= 1 or st.getQuestItemsCount(9987) >= 1 or st.getQuestItemsCount(11458) >= 1 or st.getQuestItemsCount(10790) >= 1 or st.getQuestItemsCount(10791) >= 1 or st.getQuestItemsCount(10794) >= 1 or st.getQuestItemsCount(11461) >= 1 or st.getQuestItemsCount(26031) >= 1 or st.getQuestItemsCount(27802) >= 1 or st.getQuestItemsCount(9983) >= 1 or st.getQuestItemsCount(12460) >= 1 or st.getQuestItemsCount(10071) >= 1 or st.getQuestItemsCount(10072) >= 1 or st.getQuestItemsCount(10073) >= 1 or st.getQuestItemsCount(10074) >= 1 or st.getQuestItemsCount(10075) >= 1 or st.getQuestItemsCount(10076) >= 1 or st.getQuestItemsCount(10077) >= 1 or st.getQuestItemsCount(10078) >= 1 or st.getQuestItemsCount(10079) >= 1 or st.getQuestItemsCount(10080) >= 1 or st.getQuestItemsCount(10081) >= 1 or st.getQuestItemsCount(10082) >= 1 or st.getQuestItemsCount(10083) >= 1 or st.getQuestItemsCount(10084) >= 1 or st.getQuestItemsCount(10085) >= 1 or st.getQuestItemsCount(9316) >= 1 or st.getQuestItemsCount(9321) >= 1 or st.getQuestItemsCount(9341) >= 1 or st.getQuestItemsCount(9312) >= 1 or st.getQuestItemsCount(9313) >= 1 or st.getQuestItemsCount(9314) >= 1 or st.getQuestItemsCount(9315) >= 1 or st.getQuestItemsCount(9317) >= 1 or st.getQuestItemsCount(9318) >= 1 or st.getQuestItemsCount(9319) >= 1 or st.getQuestItemsCount(9320) >= 1 or st.getQuestItemsCount(9322) >= 1 or st.getQuestItemsCount(9323) >= 1 or st.getQuestItemsCount(9324) >= 1 or st.getQuestItemsCount(9325) >= 1 or st.getQuestItemsCount(11121) >= 1 or st.getQuestItemsCount(6841) >= 1 or st.getQuestItemsCount(17800) >= 1:
                return
            else:
                st.giveItems(ItemID,Item_AMOUNT)
        else :

            return
            

    def onTalk (self,npc,player):
        htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
        st = player.getQuestState(qn)
        if st:
           npcId = npc.getNpcId()
           id = st.setState(CREATED)
           cond = st.getInt("cond")
           #Выставляем ИД вещей которые НЕЛЬЗЯ использовать на квесте.
           if st.getQuestItemsCount(13336) >= 1 or st.getQuestItemsCount(9987) >= 1 or st.getQuestItemsCount(11458) >= 1 or st.getQuestItemsCount(10790) >= 1 or st.getQuestItemsCount(10791) >= 1 or st.getQuestItemsCount(10794) >= 1 or st.getQuestItemsCount(11461) >= 1 or st.getQuestItemsCount(26031) >= 1 or st.getQuestItemsCount(27802) >= 1 or st.getQuestItemsCount(9983) >= 1 or st.getQuestItemsCount(12460) >= 1 or st.getQuestItemsCount(9981) >= 1 or st.getQuestItemsCount(25001) >= 1 or st.getQuestItemsCount(25005) >= 1 or st.getQuestItemsCount(11121) >= 1 or st.getQuestItemsCount(6841) >= 1 or st.getQuestItemsCount(17800) >= 1 or st.getQuestItemsCount(14000) >= 1 or st.getQuestItemsCount(16791) >= 1 or st.getQuestItemsCount(11117) >= 1 or st.getQuestItemsCount(11031) >= 1 or st.getQuestItemsCount(11033) >= 1:
               #Введите хтмл файл который будет отображать что требования для входа в евент не подходящие.
               htmltext = "2.htm"
           else :
               #Введите координаты х,у,z на зону телепорта, координаты можно узнать из игры командой /loc
               st.getPlayer().teleToLocation(77511,245833,-10379)
               #Текст который будет появляться после успешного телепорта в локацию.
               htmltext = "3.htm"
        return htmltext
    def onAttack (self,npc,player,damage,isPet):
        st = player.getQuestState(qn)
        npcId = npc.getNpcId()
        #Выставляем ИД моба который будет участвовать в евенте, нет зависимости от конфигов.
        if npcId == 50897:
           if st.getQuestItemsCount(13336) >= 1 or st.getQuestItemsCount(9987) >= 1 or st.getQuestItemsCount(11458) >= 1 or st.getQuestItemsCount(10790) >= 1 or st.getQuestItemsCount(10791) >= 1 or st.getQuestItemsCount(10794) >= 1 or st.getQuestItemsCount(11461) >= 1 or st.getQuestItemsCount(26031) >= 1 or st.getQuestItemsCount(27802) >= 1 or st.getQuestItemsCount(9983) >= 1 or st.getQuestItemsCount(12460) >= 1 or st.getQuestItemsCount(9981) >= 1 or st.getQuestItemsCount(25001) >= 1 or st.getQuestItemsCount(25005) >= 1 or st.getQuestItemsCount(11121) >= 1 or st.getQuestItemsCount(6841) >= 1 or st.getQuestItemsCount(17800) >= 1 or st.getQuestItemsCount(14000) >= 1 or st.getQuestItemsCount(16791) >= 1 or st.getQuestItemsCount(11117) >= 1 or st.getQuestItemsCount(11031) >= 1 or st.getQuestItemsCount(11033) >= 1:
                #Введите координаты х,у,z на которых будет перемещаться игрок, если он пробует бить монстра с не разреженным еквиптом.  
                st.getPlayer().teleToLocation(17152,170144,-3490)
                #Выставляем циферку кармы, которая прибавляется при попытке наебать систему. 
                st.getPlayer().setKarma(st.getPlayer().getKarma() +500)
        return   
QUEST       = Quest(50002, qn, "NewbieZone")
CREATED     = State('Start', QUEST)
STARTING    = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)
QUEST.setInitialState(CREATED)

QUEST.addStartNpc(Starter)
QUEST.addTalkId(Starter)
#Монстры которые участвует в квесте, указать ИД.
#Таблица монстров, пример вноса ИД в таблицу:
#[1234, 33345, 3435]
for Monsters in [50897]:
    QUEST.addKillId(Monsters)
QUEST.addAttackId(50897)
