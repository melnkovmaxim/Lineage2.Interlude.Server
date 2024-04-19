import sys
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest

qn = "partydrop"

BOSSID = 41005

ITEMID = 12019
ITEMCOUNT = 5

class partydrop (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onKill(self,npc,player,isPet):
   party = player.getParty()
   if party:
     for member in party.getPartyMembers():
       if not member.isAlikeDead():
         member.addItem("partydrop", ITEMID, ITEMCOUNT, member, True)
   else:
     player.addItem("partydrop", ITEMID, ITEMCOUNT, player, True)
   return

QUEST      = partydrop(-1, qn, "ai")

QUEST.addKillId(BOSSID)
