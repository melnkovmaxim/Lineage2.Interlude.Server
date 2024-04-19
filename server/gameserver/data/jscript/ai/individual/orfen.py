import sys
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold.gameserver.ai import CtrlIntention
from ru.agecold.gameserver.datatables import SkillTable
from ru.agecold.gameserver.datatables import ItemTable
from ru.agecold.util import Rnd
from java.lang import System

qn = "orfen"

ORFEN = 29014

class orfen (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def init_LoadGlobalData(self) :
   status = self.loadGlobalQuestVar("status")
   if status == "0" :    
     temp = long(self.loadGlobalQuestVar("respawn")) - System.currentTimeMillis()
     if temp > 0 :
       print "Orfen:  dead"
       self.startQuestTimer("resp", temp, None, None)
     else :
       print "Orfen:  live"
       self.addSpawn(ORFEN,55024,17368,-5412,30000,False,0)
       self.saveGlobalQuestVar("status", "1")
       self.deleteGlobalQuestVar("respawn")
   else :
     print "Orfen: live"
     self.addSpawn(ORFEN,55024,17368,-5412,30000,False,0)
     self.saveGlobalQuestVar("status", "1")
     self.deleteGlobalQuestVar("respawn")
   return

 def onAdvEvent (self,event,npc,player):
   if event == "resp" :
     self.addSpawn(ORFEN,55024,17368,-5412,30000,False,0)
     self.saveGlobalQuestVar("status", "1")
     self.deleteGlobalQuestVar("respawn")
     self.cancelQuestTimer("resp",None,None)
   return
   
 def onKill(self,npc,player,isPet):
   self.saveGlobalQuestVar("status", "0")
   respawnTime = long(24 * 3600000)
   self.saveGlobalQuestVar("respawn", str(System.currentTimeMillis() + respawnTime))
   self.startQuestTimer("resp", respawnTime, None, None)
   print "GrandBossManager:  Orfen was killed."
   return

QUEST      = orfen(-1, qn, "ai")

QUEST.addKillId(ORFEN)
