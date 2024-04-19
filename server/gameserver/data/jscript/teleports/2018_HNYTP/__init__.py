import sys

from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from java.lang import System
from ru.agecold.gameserver.model import L2Party
from ru.agecold.gameserver.model import L2Character
from ru.agecold.gameserver.model.actor.instance import L2PcInstance

qn = "2018_HNYTP"

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)

 def onTalk (self,npc,player):
   st = player.getQuestState("2018_HNYTP")
   npcId = npc.getNpcId()
   id = st.getState()
   if npcId == 50005 :
     if player.getLevel() < 80 :
        htmltext = "50005-0.htm"
        st.exitQuest(1)
     else :
        player.teleToLocation(113877,-109271,-836)
        htmltext = ""
        st.exitQuest(1)
   return htmltext

QUEST       = Quest(2018,qn,"HNYTP")

QUEST.addStartNpc(50005)
QUEST.addTalkId(50005)
