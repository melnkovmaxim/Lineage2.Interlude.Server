__all__ = [
'core',
'orfen'
]

for name in __all__ :
    try :
        __import__(name,globals(), locals(), [], -1)
    except:
        print "failed to import quest : ",name