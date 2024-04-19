__all__ = [
'dwarven_occupation_change',
'elven_human_mystics_1',
'elven_human_mystics_2',
'elven_human_buffers_2',
'elven_human_fighters_1',
'dark_elven_change_1',
'dark_elven_change_2',
'orc_occupation_change_1',
'orc_occupation_change_2',
'q9000_clan',
'q9001_alliance',
'q30026_bitz_occupation_change',
'q30031_biotin_occupation_change',
'q30109_hannavalt_occupation_change',
'q30154_asterios_occupation_change',
'q30358_thifiell_occupation_change',
'q30520_reed_occupation_change',
'q30525_bronk_occupation_change',
'q30565_kakai_occupation_change'
]
print ""
print "importing village master data ..."
for name in __all__ :
    try :
        __import__('data.jscript.village_master.'+name,globals(), locals(), ['__init__'], -1)
    except:
        print "failed to import quest : ",name
print "... done"
print ""
