import sys
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from java.lang import System
from ru.agecold.gameserver.model import L2Party
from ru.agecold.gameserver.model import L2Character
from ru.agecold.gameserver.model.actor.instance import L2PcInstance

qn = "q3100_PrimIsle"
VERVATO = 32104
ADENA = 57

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
        
 def onTalk (self,npc,player):
   st = player.getQuestState("q3100_PrimIsle")  
   npcId = npc.getNpcId()
   party = player.getParty()
   if npcId == VERVATO :
     if st.getQuestItemsCount(ADENA) >= 2000000:
       if party:
         for player in party.getPartyMembers() :
           if player.isAlikeDead():
             st.takeItems(ADENA,2000000)
             player.teleToLocation(11563,-23429,-3643)
             htmltext = "ok.htm"
           else:
             htmltext = "netmertvih.htm"
       else :
         htmltext = "netparty.htm"
     else :
       htmltext = "nehvataet.htm"
   return htmltext

QUEST       = Quest(3100,qn,"Teleports")
CREATED     = State('Start',QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(VERVATO)
QUEST.addTalkId(VERVATO)
