import sys
from ru.agecold.gameserver.model.quest import State
from ru.agecold.gameserver.model.quest import QuestState
from ru.agecold.gameserver.model.quest.jython import QuestJython as JQuest
from ru.agecold.gameserver.datatables import SkillTable
from ru.agecold.gameserver.model import L2Effect
from ru.agecold.util import Rnd
from java.lang import System

qn = "q8009_HotSpringsBuffs"

HSMOBS = [21316, 21321, 21314, 21319]

# список баффов
BUFFS = [4552,4553,4554]

# шанс словить бафф
BUFF_CHANCE = 30

# задержка перед взятием следующего баффа, 1 сек = 1000
BUFF_DELAY = 5000

class Quest (JQuest) :
 
 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
    
 def onAttack (self,npc,player,damage,isPet):
    npcId = npc.getNpcId()
    if npcId in HSMOBS:
      chance = Rnd.get(50) + Rnd.get(50)
      st = SkillTable.getInstance()
      for buff in BUFFS:
        if chance < BUFF_CHANCE:
          hsbuff = player.getFirstEffect(buff)
          if hsbuff == None:
            st.getInfo(buff, 1).getEffects(player, player)
            return
          else:
            lvl = hsbuff.getLevel()
            if lvl == 10:
              continue
            player.stopSkillEffects(buff);
            st.getInfo(buff, (lvl + 1)).getEffects(player, player)
    return
        
QUEST = Quest(8009,qn,"custom")

for i in HSMOBS:
  QUEST.addAttackId(i)
