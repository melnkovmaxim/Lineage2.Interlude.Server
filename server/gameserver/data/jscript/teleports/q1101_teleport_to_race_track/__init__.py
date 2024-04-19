import sys
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold.gameserver.datatables import MapRegionTable
from java.lang import System

qn = "q1101_teleport_to_race_track"

GK=[30006,30059,30080,30134,30146,30177,30233,30256,30320,30540,30576,30836,30848,30878,30899,31275,31320,31964]
MDTGK = 30995

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
 def onTalk (self,npc,player):
   st = player.getQuestState("q1101_teleport_to_race_track")
   npcId = npc.getNpcId()
   if npcId in GK :
     nearestTownId = str(MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()))
     st.set("town",nearestTownId)
     player.teleToLocation(12949,181696,-3562)
   elif npcId == MDTGK :
     home = st.getInt("town")
     if home == 0:
       player.teleToLocation(83400,147943,-3404) #Talking Island
     elif home == 1:
       player.teleToLocation(46934,51467,-2977)  #Elven Village
     elif home == 2:
       player.teleToLocation(9745,15606,-4574)   #Dark Elven Village
     elif home == 3:
       player.teleToLocation(-44836,-112524,-235) #Orc Village
     elif home == 4:
       player.teleToLocation(115113,-178212,-901) #Dwarven Village
     elif home == 5:
       player.teleToLocation(-12678,122776,-3116) #Gludio Castle Town
     elif home == 6:
       player.teleToLocation(-80826,149775,-3043) #Gludin Village
     elif home == 7:
       player.teleToLocation(15670,142983,-2705) #Dion Castle Town
     elif home == 8:
       player.teleToLocation(83400,147943,-3404) #Giran Castle Town
     elif home == 9:
       player.teleToLocation(82956,53162,-1495) #Oren Castle Town
     elif home == 10:
       player.teleToLocation(146331,25762,-2018) #Aden Castle Town
     elif home == 11:
       player.teleToLocation(116819,76994,-2714) #Hunters Village
     elif home == 13:
       player.teleToLocation(111409,219364,-3545) #Heine
     elif home == 14:
       player.teleToLocation(43799,-47727,-798) #Rune Castle Town
     elif home == 15:
       player.teleToLocation(147928,-55273,-2734) #Goddard Castle Town
     elif home == 16:
       player.teleToLocation(87331,-142842,-1317) #Schuttgart Castle Town
     else: #pust' budet Giran
       player.teleToLocation(83400,147943,-3404) #Giran Castle Town        
   return

QUEST       = Quest(1101,qn,"teleports")
CREATED     = State('Start',QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(MDTGK)
QUEST.addTalkId(MDTGK)

for item in GK:
  QUEST.addStartNpc(item)
  QUEST.addTalkId(item)
