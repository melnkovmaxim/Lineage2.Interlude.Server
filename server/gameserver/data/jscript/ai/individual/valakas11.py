import sys
from java.util.concurrent import TimeUnit
from ru.agecold.gameserver import GeoData
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold.util import Rnd
from java.lang import System

qn = "valakas1"

#респаун в минутах, min max
RESPAWN_MIN = 360
RESPAWN_MAX = 360

#задержка на появление после рестарта, минуты
SPAWN_DELAY = TimeUnit.MINUTES.toMillis(1)

class valakas1 (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def spawnMe(self):
   self.deleteGlobalQuestVar("respawn")
   self.saveGlobalQuestVar("status", "1")
   self.addSpawn(29028,212864,-114431,GeoData.getInstance().getSpawnHeight(212864, -114431, -1636, -1636, None),30000,False,0)
   return

 def sheduleSpawn(self, respawn):
   self.saveGlobalQuestVar("status", "0")
   self.startQuestTimer("resp", respawn, None, None)
   self.saveGlobalQuestVar("respawn", str(System.currentTimeMillis() + respawn))
   return

 def getRespawnTime(self):
   if self.loadGlobalQuestVar("status") == "0":
     return long(self.loadGlobalQuestVar("respawn")) - System.currentTimeMillis()
   return 0

 def init_LoadGlobalData(self):
   self.startQuestTimer("resp", max(SPAWN_DELAY, self.getRespawnTime()), None, None)
   return

 def onAdvEvent (self,event,npc,player):
   if event == "resp":
     self.spawnMe()
     self.cancelQuestTimer("resp",None,None)
   return

 def onKill(self,npc,player,isPet):
   self.sheduleSpawn(TimeUnit.MINUTES.toMillis(Rnd.get(RESPAWN_MIN, RESPAWN_MAX)))
   return

QUEST = valakas1(-1, qn, "ai")

QUEST.addKillId(29028)
