#
# Made by Vice [L2JOneo]
#
# Modified by RusTeam.
#
import sys
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

qn = "q641_AttackSailren"

#NPC
STATUE = 32109

#MOBS
VEL1 = 22196
VEL2 = 22197
VEL3 = 22198
VEL4 = 22218
VEL5 = 22223
PTE = 22199

#ITEMS 
FRAGMENTS = 8782
GAZKH = 8784

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
   htmltext = event
   if event == "32109-03.htm" :
     st.set("cond","1")
     st.setState(STARTED)
     st.playSound("ItemSound.quest_accept")
   if event == "32109-05.htm" :
     st.playSound("ItemSound.quest_finish")
     st.takeItems(FRAGMENTS,50)
     st.giveItems(GAZKH,1)
     st.exitQuest(1)

   return htmltext

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   htmltext = "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
   if not st : return htmltext
   npcId = npc.getNpcId()
   id = st.getState()
   cond=st.getInt("cond")
   if npcId == STATUE :
      if id == CREATED:
         st2 = player.getQuestState("q126_TheNameOfEvilPart2")
         if st2 :
            if st2.getState().getName() == 'Completed' :
               if player.getLevel() >= 77 :
                  htmltext = "32109-01.htm"
               else:
                  htmltext = "lvl.htm"
                  st.exitQuest(1)
         else :
            htmltext = "no.htm"
            st.exitQuest(1)
      elif cond == 1 :
            htmltext = "32109-03.htm"
      elif cond == 2 :
            htmltext = "32109-04.htm"
            
   return htmltext

 def onKill (self,npc,player,isPet):
   st = player.getQuestState(qn)
   if not st: return   
   if st.getState() == STARTED :
       npcId = npc.getNpcId()
       if npcId in [VEL1,VEL2,VEL3,VEL4,VEL5,PTE] :
          if st.getQuestItemsCount(FRAGMENTS) < 30 :
             st.giveItems(FRAGMENTS,1)
             if st.getQuestItemsCount(FRAGMENTS) == 30 :
                 st.playSound("ItemSound.quest_middle")
                 st.set("cond","2")
             else:
                 st.playSound("ItemSound.quest_itemget")

   return

QUEST       = Quest(641,qn,"Attack Sailren!")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(STATUE)
QUEST.addTalkId(STATUE)

QUEST.addKillId(VEL1)
QUEST.addKillId(VEL2)
QUEST.addKillId(VEL3)
QUEST.addKillId(VEL4)
QUEST.addKillId(VEL5)
QUEST.addKillId(PTE)

STARTED.addQuestDrop(VEL1,FRAGMENTS,1)
STARTED.addQuestDrop(VEL2,FRAGMENTS,1)
STARTED.addQuestDrop(VEL3,FRAGMENTS,1)
STARTED.addQuestDrop(VEL4,FRAGMENTS,1)
STARTED.addQuestDrop(VEL5,FRAGMENTS,1)
STARTED.addQuestDrop(PTE,FRAGMENTS,1)
