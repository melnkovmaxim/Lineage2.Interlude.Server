package ai;

import ru.agecold.gameserver.datatables.NpcTable;
import ru.agecold.gameserver.model.L2Attackable;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.templates.L2NpcTemplate;

public class L2Monstert extends L2Attackable
{
	public L2Monstert(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

    @Override
	public void onSpawn()
    {
        super.onSpawn();
		System.out.println("Victory)");
    }

    @Override
	public boolean doDie(L2Character killer)
    {
    	if (!super.doDie(killer))
    		return false;

        return true;
    }

    @Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
    {
        //if (!(attacker instanceof L2MonsterInstance))
        //{
            super.addDamageHate(attacker, damage, aggro);
        //}
    }

    @Override
	public void deleteMe()
    {
        super.deleteMe();
    }
	
	public static void main (String... arguments )
	{
		L2NpcTemplate template = NpcTable.getInstance().getTemplate(13016);
		new L2Monstert(13016, template);
	}
}
