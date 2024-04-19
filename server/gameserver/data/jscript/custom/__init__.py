__all__ = [
'q1000_ColorManager',
'q8016_HeroWeapons',
'q1001_ColorManager1',
'q3995_echo',
'q6050_KetraOrcSupport',
'q6051_VarkaSilenosSupport',
'q7000_HeroItems',
'q8000_RaidbossInfo',
'q8001_NpcLocationInfo',
'q8008_ArenaCP',
'q8009_HotSpringsBuffs',
'q8018_CngNick',
'q8019_SetHero',
'q8782_ExpressPost',
'q8014_LifeStone',
'purchase',
'partydrop3',
'partydrop2',
'q8014_LifeStone',
#'partydrop', - Ротус - пати дроп убран = добавлен просто в базу
'partydrop4',
'partydrop5',
'partydrop6',
#'partydrop7', - Королева - пати дроп убран = добавлен просто в базу
'partydrop13',
'partydrop8'


]
print ""
print "importing custom data ..."
for name in __all__ :
    try :
        __import__('data.jscript.custom.'+name,globals(), locals(), ['__init__'], -1)
    except:
        print "failed to import custom : ",name
print "... done"
print ""
