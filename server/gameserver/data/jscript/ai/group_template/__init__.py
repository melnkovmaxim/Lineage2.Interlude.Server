__all__ = [
'feedable_beasts',
'polymorphing_angel',
'retreat_onattack'
]

for name in __all__ :
    try :
        __import__(name,globals(), locals(), [], -1)
    except:
        print "failed to import group_template : ",name
