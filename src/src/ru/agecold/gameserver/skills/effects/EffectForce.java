/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package ru.agecold.gameserver.skills.effects;

import ru.agecold.gameserver.datatables.SkillTable;
import ru.agecold.gameserver.model.L2Effect;
import ru.agecold.gameserver.skills.Env;

/**
 * @author kombat
 */
public final class EffectForce extends L2Effect
{
    public int forces;
    
    public EffectForce(Env env, EffectTemplate template) 
    {
        super(env, template);
        forces = getSkill().getLevel();
    }
    
    @Override
    public boolean onActionTime()
    {
        return true;
    }
    
    @Override
    public EffectType getEffectType()
    {
        return EffectType.BUFF;
    }
    
    public void increaseForce()
    {
        if (forces < 3)
        {
            forces++;
            updateBuff();
        }
    }
    
    public void decreaseForce()
    {
        forces--;
        if (forces < 1)
            exit();
        else
            updateBuff();
    }
    
    private void updateBuff()
    {
        exit();
        SkillTable.getInstance().getInfo(getSkill().getId(), forces).getEffects(getEffector(), getEffected());
    }
}