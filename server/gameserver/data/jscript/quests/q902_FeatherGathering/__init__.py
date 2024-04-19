import sys
from ru.agecold import Config
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

qn = "q902_FeatherGathering"

#Сделано специально для сервера La2-ares.pw
#Квест писал Lacosta
#ICQ: 320-792 skype l2impuls
#Копирайты прошу не убирать а то будет больно!

#			ID's NPC используемых в квесте				#
#########################################################
#					ID NPC Для квеста					#
NPC = 300301
#			ID Мобов, с которых падает квест итем		#
MOBS = [25260,25115,25134]
#		ID Босса, которого нужно убить по квесту		#
BOSS = 25126
#########################################################


#			ID's Items используемых в квест				#
#########################################################
#		ID Итемов которые будут падать из MOBS			#
FEATHER = 9142
#	Количество итемов, которое будет падать из MOBS Min	#
FEATHER_COUNT_MIN = 1
#	Количество итемов, которое будет падать из MOBS Max	#
FEATHER_COUNT_MAX = 1
#		Количество итемов, нужное собрать с MOBS		#
FEATHER_NEED = 5000
#				Шанс дропа итемов с MOBS				#
FEATHER_CHANCE = 100
#		ID Итема которое будет падать из BOSS			#
BOSS_ITEM = 9143
#########################################################


#					Награда за квест					#
#########################################################
#					ID Награды за квест					#
WINNER = 10555
#					Количество награды Min				#
WINNER_COUNT_MIN = 1
#					Количество награды Max				#
WINNER_COUNT_MAX = 1
#			На сколько будет заточена награда			#
WINNER_ENCHANT = 0
#########################################################

class Quest (JQuest) :

 def __init__(self,id,name,descr) : JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
   htmltext = event
   if event == "accept.htm":
     st.setState(STARTED)
     st.playSound("ItemSound.quest_accept")
     st.set("cond","1")
   elif event == "finish.htm":
     if st.getQuestItemsCount(FEATHER) >= FEATHER_NEED and st.getQuestItemsCount(BOSS_ITEM) >= 1:
       st.takeItems(FEATHER, 5000)
       st.takeItems(BOSS_ITEM, -1)
       st.giveItems(WINNER, 1)
       st.set("cond","0")
       st.playSound("ItemSound.quest_finish")
       st.setState(COMPLETED)
     else:
       htmltext = "mobs.htm"
   return htmltext

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
   if not st: 
     return htmltext
   npcId = npc.getNpcId()
   idd = st.getState()
   cond = st.getInt("cond")
   if npcId == NPC:
     if idd == COMPLETED :
       htmltext = "completed.htm"
     if idd == CREATED :
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
   count = 1
   if npcId in MOBS:
     reward = FEATHER
     limit = FEATHER_NEED
     chance = FEATHER_CHANCE
   count = 200
   if npcId == BOSS:
     reward = BOSS_ITEM
     limit = 1
   count = 1
   if reward > 0:
     party = player.getParty()
     if party:
       for member in party.getPartyMembers():
         if not member.isAlikeDead():
           st = member.getQuestState("q902_FeatherGathering")
           if st and st.getQuestItemsCount(reward) < limit and st.getRandom(100) <= chance:
             st.giveItems(reward, count)
             st.playSound("ItemSound.quest_itemget")
             if st.getQuestItemsCount(reward) >= limit:
               st.playSound("ItemSound.quest_middle")
     else:
       if st.getQuestItemsCount(reward) < limit and st.getRandom(100) <= chance:
         st.giveItems(reward, count)
         st.playSound("ItemSound.quest_itemget")
   return 

QUEST = Quest(902, qn, "Feather Gathering")

CREATED = State('Start', QUEST) 
STARTED = State('Started', QUEST) 
COMPLETED = State('Completed', QUEST) 

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(NPC)
QUEST.addTalkId(NPC)
QUEST.addKillId(BOSS)

for m in MOBS:
  QUEST.addKillId(m)