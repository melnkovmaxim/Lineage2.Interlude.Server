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

qn = "DevastatedCastle"

INNPC   = 35418
WYV   = 35419

class DevastatedCastle (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def init_LoadGlobalData(self) :
   print "Devastated Castle loaded."
 
 def onAdvEvent (self,event,npc,player):
   if event == "open":
     DoorTable.getInstance().getDoor(25170003).openMe()
     DoorTable.getInstance().getDoor(25170004).openMe()
     DoorTable.getInstance().getDoor(25170005).openMe()
     DoorTable.getInstance().getDoor(25170006).openMe()
   elif event == "close":
     DoorTable.getInstance().getDoor(25170003).closeMe()
     DoorTable.getInstance().getDoor(25170004).closeMe()
     DoorTable.getInstance().getDoor(25170005).closeMe()
     DoorTable.getInstance().getDoor(25170006).closeMe()
   return

 def onTalk (self,npc,player):
   npcId = npc.getNpcId()
   if not player.getClanId():
     return "Go away no0b"
   clanHall = ClanHallManager.getInstance().getClanHallById(34);
   if npcId == INNPC:
     if clanHall.getOwnerId() != player.getClanId():
       return "Go away no0b"
     htmltext = "<html><body><a action=\"bypass -h Quest DevastatedCastle open\">Открыть</a><br>"
     htmltext += "<a action=\"bypass -h Quest DevastatedCastle close\">Закрыть</a></body></html>"
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

QUEST       = DevastatedCastle(-1,qn,"clanhalls")
CREATED     = State('Start',QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(WYV)
QUEST.addTalkId(WYV)
QUEST.addStartNpc(INNPC)
QUEST.addTalkId(INNPC)
