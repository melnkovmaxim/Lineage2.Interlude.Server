import sys
from ru.agecold import Config
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

qn = "q902_FeatherGathering"

## нпц
NPC = 41112

## Мобы
MOBS = [15078]
#босс
BOSS = 25450

## Дроп
#id перьев с мобов в мос
FEATHER = 12001
#сколько нужно?
FEATHER_NEED = 50
#шанс дропа, %
FEATHER_CHANCE = 90
#id итема с босса
BOSS_ITEM = 2125

##Награда
#id крыльев
WINGS = 12000

class Quest (JQuest) :

 def __init__(self,id,name,descr) : JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
   htmltext = event
   if event == "accept.htm":
     st.setState(State.STARTED)
     st.playSound("ItemSound.quest_accept")
     st.set("cond","1")
   elif event == "finish.htm":
     if st.getQuestItemsCount(FEATHER) >= FEATHER_NEED and st.getQuestItemsCount(BOSS_ITEM) >= 1:
       st.takeItems(FEATHER, -1)
       st.takeItems(BOSS_ITEM, -1)
       st.giveItems(WINGS, 1)
       st.set("cond","0")
       st.playSound("ItemSound.quest_finish")
       st.setState(State.COMPLETED)
     else:
       htmltext = "mobs.htm"
   return htmltext

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   if not st: 
     return "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
   npcId = npc.getNpcId()
   idd = st.getState()
   cond = st.getInt("cond")
   if npcId == NPC:
     if idd == State.COMPLETED :
       htmltext = "completed.htm"
     if idd == State.CREATED :
       htmltext = "hello1.htm"
     elif cond == 1:
       if st.getQuestItemsCount(FEATHER) >= FEATHER_NEED and st.getQuestItemsCount(BOSS_ITEM) >= 1:
         htmltext = "mobs2.htm"
       else:
         htmltext = "mobs.htm"
   return htmltext

 def onKill(self,npc,player,isPet):
   st = player.getQuestState(qn)
   if not st or st.getInt("cond") != 1:
     return
   npcId = npc.getNpcId()
   reward = 0
   limit = 1
   chance = 100
   if npcId in MOBS:
     reward = FEATHER
     limit = FEATHER_NEED
     chance = FEATHER_CHANCE
   if npcId == BOSS:
     reward = BOSS_ITEM
     limit = 1
   if reward > 0:
     party = player.getParty()
     if party:
       for member in party.getPartyMembers():
         if not member.isAlikeDead():
           st = member.getQuestState("q902_FeatherGathering")
           if st and st.getQuestItemsCount(reward) < limit and st.getRandom(100) <= chance:
             st.giveItems(reward, 1)
             st.playSound("ItemSound.quest_itemget")
             if st.getQuestItemsCount(reward) >= limit:
               st.playSound("ItemSound.quest_middle")
     else:
       if st.getQuestItemsCount(reward) < limit and st.getRandom(100) <= chance:
         st.giveItems(reward, 1)
         st.playSound("ItemSound.quest_itemget")
   return  

QUEST = Quest(902, qn, "Feather Gathering")

QUEST.addStartNpc(NPC)
QUEST.addTalkId(NPC)
QUEST.addKillId(BOSS)

for m in MOBS:
  QUEST.addKillId(m)
