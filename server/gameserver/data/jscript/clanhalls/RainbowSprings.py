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

qn = "RainbowSprings"

DOOR1 = 35601
DOOR2 = 35602

class RainbowSprings (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def init_LoadGlobalData(self) :
   print "Rainbow Springs loaded."
 
 def onAdvEvent (self,event,npc,player):
   if event == "open1":
     DoorTable.getInstance().getDoor(24140003).openMe()
     DoorTable.getInstance().getDoor(24140004).openMe()
     htmltext = "<html><body><a action=\"bypass -h Quest RainbowSprings open1\">Открыть</a><br>"
     htmltext += "<a action=\"bypass -h Quest RainbowSprings close1\">Закрыть</a></body></html>"
     return htmltext
   elif event == "close1":
     DoorTable.getInstance().getDoor(24140003).closeMe()
     DoorTable.getInstance().getDoor(24140004).closeMe()
     htmltext = "<html><body><a action=\"bypass -h Quest RainbowSprings open1\">Открыть</a><br>"
     htmltext += "<a action=\"bypass -h Quest RainbowSprings close1\">Закрыть</a></body></html>"
     return htmltext
   elif event == "open2":
     DoorTable.getInstance().getDoor(24140001).openMe()
     DoorTable.getInstance().getDoor(24140002).openMe()
     DoorTable.getInstance().getDoor(24140005).openMe()
     DoorTable.getInstance().getDoor(24140006).openMe()
     htmltext = "<html><body><a action=\"bypass -h Quest RainbowSprings open2\">Открыть</a><br>"
     htmltext += "<a action=\"bypass -h Quest RainbowSprings close2\">Закрыть</a></body></html>"
     return htmltext
   elif event == "close2":
     DoorTable.getInstance().getDoor(24140001).closeMe()
     DoorTable.getInstance().getDoor(24140002).closeMe()
     DoorTable.getInstance().getDoor(24140005).closeMe()
     DoorTable.getInstance().getDoor(24140006).closeMe()
     htmltext = "<html><body><a action=\"bypass -h Quest RainbowSprings open2\">Открыть</a><br>"
     htmltext += "<a action=\"bypass -h Quest RainbowSprings close2\">Закрыть</a></body></html>"
     return htmltext
   return

 def onTalk (self,npc,player):
   npcId = npc.getNpcId()
   if not player.getClanId():
     return "Go away no0b"
   clanHall = ClanHallManager.getInstance().getClanHallById(62);
   if clanHall.getOwnerId() != player.getClanId():
     return "Go away no0b"
   if npcId == DOOR1:
     htmltext = "<html><body><a action=\"bypass -h Quest RainbowSprings open1\">Открыть</a><br>"
     htmltext += "<a action=\"bypass -h Quest RainbowSprings close1\">Закрыть</a></body></html>"
     return htmltext
   elif npcId == DOOR2:
     htmltext = "<html><body><a action=\"bypass -h Quest RainbowSprings open2\">Открыть</a><br>"
     htmltext += "<a action=\"bypass -h Quest RainbowSprings close2\">Закрыть</a></body></html>"
     return htmltext
   return

QUEST       = RainbowSprings(-1,qn,"clanhalls")
CREATED     = State('Start',QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(DOOR1)
QUEST.addTalkId(DOOR1)
QUEST.addStartNpc(DOOR2)
QUEST.addTalkId(DOOR2)
