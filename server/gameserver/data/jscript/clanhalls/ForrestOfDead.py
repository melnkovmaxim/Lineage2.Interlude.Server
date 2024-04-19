# -*- coding: cp1251 -*-
import sys
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold.gameserver.datatables import DoorTable
from ru.agecold import L2DatabaseFactory
from java.lang import System
from ru.agecold.gameserver.instancemanager import ClanHallManager
from ru.agecold.gameserver.network.serverpackets import Ride
from ru.agecold.gameserver.model.entity import Castle
from ru.agecold.gameserver.instancemanager import CastleManager

qn = "ForrestOfDead"

INNPC   = 35641
INNPCC   = 35642
WYV   = 35638

class ForrestOfDead (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def init_LoadGlobalData(self) :
   print "Devastated Castle loaded."
 
 def onAdvEvent (self,event,npc,player):
   if event == "open1":
     DoorTable.getInstance().getDoor(21170001).openMe()
     DoorTable.getInstance().getDoor(21170002).openMe()
   elif event == "close1":
     DoorTable.getInstance().getDoor(21170001).closeMe()
     DoorTable.getInstance().getDoor(21170002).closeMe()
   elif event == "open2":
     DoorTable.getInstance().getDoor(21170005).openMe()
     DoorTable.getInstance().getDoor(21170006).openMe()
     DoorTable.getInstance().getDoor(21170004).openMe()
     DoorTable.getInstance().getDoor(21170003).openMe()
   elif event == "close2":
     DoorTable.getInstance().getDoor(21170005).closeMe()
     DoorTable.getInstance().getDoor(21170006).closeMe()
     DoorTable.getInstance().getDoor(21170004).closeMe()
     DoorTable.getInstance().getDoor(21170003).closeMe()
   return

 def onTalk (self,npc,player):
   npcId = npc.getNpcId()
   if not player.getClanId():
     return "Go away no0b"
   clanHall = ClanHallManager.getInstance().getClanHallById(64);
   if npcId == INNPC:
     if clanHall.getOwnerId() != player.getClanId():
       return "Go away no0b"
     htmltext = "<html><body><a action=\"bypass -h Quest ForrestOfDead open\">Открыть</a><br>"
     htmltext += "<a action=\"bypass -h Quest ForrestOfDead close\">Закрыть</a></body></html>"
     return htmltext
   if npcId == INNPCC:
     if clanHall.getOwnerId() != player.getClanId():
       return "Go away no0b"
     htmltext = "<html><body><a action=\"bypass -h Quest ForrestOfDead open1\">Открыть</a><br>"
     htmltext += "<a action=\"bypass -h Quest ForrestOfDead close1\">Закрыть</a></body></html>"
     return htmltext
   elif npcId == WYV:
     if clanHall.getOwnerId() != player.getClanId():
       return "Go away no0b"
     else:
       if player.isClanLeader():
         if player.isMounted():
           return "Слезьте со страйдера или отзовите пета"    
         mount = Ride(player.getObjectId(), Ride.ACTION_MOUNT, 12621);
         player.sendPacket(mount);
         player.broadcastPacket(mount);
         player.setMountType(mount.getMountType());
         return
       else:
         return "Сервис только для кланлидера"
   return

QUEST       = ForrestOfDead(-1,qn,"clanhalls")
CREATED     = State('Start',QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(WYV)
QUEST.addTalkId(WYV)
QUEST.addStartNpc(INNPC)
QUEST.addTalkId(INNPC)
QUEST.addStartNpc(INNPCC)
QUEST.addTalkId(INNPCC)
