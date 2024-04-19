__all__ = [
'DevastatedCastle',
'RainbowSprings',
'ForrestOfDead'
]
print ""
print "initializing EliteClanChall Manager"
for name in __all__ :
    try :
        __import__('data.jscript.clanhalls.'+name,globals(), locals(), ['__init__'], -1)
    except:
        print "failed to import : ",name
print "... done"
print ""
