__all__ = [
'q1100_teleport_with_charm',
'q1101_teleport_to_race_track',
'q1102_toivortex_green',
'q1102_toivortex_blue',
'q1102_toivortex_red',
'q1103_OracleTeleport',
'q1104_NewbieTravelToken',
'q1630_PaganTeleporters',
'q2211_HuntingGroundsTeleport',
'q2400_toivortex_exit',
'q6111_ElrokiTeleporters',
'q1203_4thSepulcher',
'q1204_ElmoredenCemetery',
'2018_HNYTP',
'q3100_PrimIsle'
]
print ""
print "importing teleport data ..."
for name in __all__ :
    try :
        __import__('data.jscript.teleports.'+name,globals(), locals(), ['__init__'], -1)
    except:
        print "failed to import quest : ",name
print "... done"
print ""
