import sys
from java.util.concurrent import TimeUnit
from ru.agecold.gameserver import GeoData
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold.util import Rnd
from java.lang import System

qn = "antharas1"

#респаун в минутах, min max
RESPAWN_MIN = 300
RESPAWN_MAX = 300

#задержка на появление после рестарта, минуты
SPAWN_DELAY = TimeUnit.MINUTES.toMillis(1)

class antharas (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def spawnMe(self):
   self.deleteGlobalQuestVar("respawn")
   self.saveGlobalQuestVar("status", "1")
   self.addSpawn(29019,181466,114847,GeoData.getInstance().getSpawnHeight(181466,114847, -7677, -7677, None),30000,False,0)
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

QUEST = antharas1(-1, qn, "ai")

QUEST.addKillId(29019)
