import sys
from java.lang import System
from ru.agecold.util import Rnd
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold.gameserver.network.serverpackets import CreatureSay
from ru.agecold.gameserver.network.serverpackets import PlaySound

# Boss: Core
class core(JQuest) :

 # init function.  Add in here variables that you'd like to be inherited by subclasses (if any)
 def __init__(self,id,name,descr):
   JQuest.__init__(self,id,name,descr)
   self.Core = 29006
   self.Minions = [29007,29008,29011]
   self.FirstAttacked = False

 def init_LoadGlobalData(self) :
   status = self.loadGlobalQuestVar("status")
   if status == "0" :    
     temp = long(self.loadGlobalQuestVar("respawn")) - System.currentTimeMillis()
     if temp > 0 :
       print "Core:  dead"
       self.startQuestTimer("resp", temp, None, None)
     else :
       print "Core:  live"
       self.addSpawn(29006,17726,108915,-6480,30000,False,0)
       self.saveGlobalQuestVar("status", "1")
       self.deleteGlobalQuestVar("respawn")
   else :
     print "Core: live"
     self.addSpawn(29006,17726,108915,-6480,30000,False,0)
     self.saveGlobalQuestVar("status", "1")
     self.deleteGlobalQuestVar("respawn")
   return

 def onAdvEvent (self,event,npc,player):
   if event == "resp" :
     self.addSpawn(29006,17726,108915,-6480,30000,False,0)
     self.saveGlobalQuestVar("status", "1")
     self.deleteGlobalQuestVar("respawn")
     self.cancelQuestTimer("resp",None,None)
   return

 def onAttack (self,npc,player,damage,isPet):
   objId=npc.getObjectId()
   if self.FirstAttacked:
     if Rnd.get(100) : return
     npc.broadcastPacket(CreatureSay(objId,0,"Core","Removing intruders."))
   else :
     self.FirstAttacked = True
     npc.broadcastPacket(CreatureSay(objId,0,"Core","A non-permitted target has been discovered."))
     npc.broadcastPacket(CreatureSay(objId,0,"Core","Starting intruder removal system."))
   return 

 def onKill(self,npc,player,isPet):
   npcId = npc.getNpcId()
   if npcId == self.Core:
     objId=npc.getObjectId()
     npc.broadcastPacket(PlaySound(1, "BS02_D", 1, objId, npc.getX(), npc.getY(), npc.getZ()))
     npc.broadcastPacket(CreatureSay(objId,0,"Core","WINDOWS: FATAL ERROR DETECTED AT CORE.SYS"))
     npc.broadcastPacket(CreatureSay(objId,0,"Core","*** STOP: 0x00000059 (0xKDEE12,0x00040,0xKDEOO)"))
     npc.broadcastPacket(CreatureSay(objId,0,"Core","*** CORE.SYS - Address FBFE617PF base at FBFE500"))
     npc.broadcastPacket(CreatureSay(objId,0,"Core","WINDOWS IS SHUTTING DOWN NOW..."))
     self.FirstAttacked = False
     self.addSpawn(31842,16502,110165,-6394,0,False,900000)
     self.addSpawn(31842,18948,110166,-6397,0,False,900000)
     self.saveGlobalQuestVar("status", "0")
     respawnTime = long(24 * 3600000)
     self.saveGlobalQuestVar("respawn", str(System.currentTimeMillis() + respawnTime))
     self.startQuestTimer("resp", respawnTime, None, None)
     print "GrandBossManager:  Core was killed."
   #elif self.FirstAttacked :
     #self.addSpawn(npcId,17726,108915,-6480,npc.getHeading(),True,0)
   return 

# now call the constructor (starts up the ai)
QUEST = core(-1,"core","ai")

QUEST.addKillId(QUEST.Core)
QUEST.addAttackId(QUEST.Core)

for minion in QUEST.Minions :
    QUEST.addKillId(minion)
