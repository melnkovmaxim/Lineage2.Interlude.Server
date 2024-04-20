/*
 * This program is free software; you can redistribute it and/or modify
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

import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.actor.instance.L2SiegeFlagInstance;
import ru.agecold.gameserver.model.actor.instance.L2BossInstance;
import ru.agecold.gameserver.model.actor.instance.L2GrandBossInstance;
import ru.agecold.gameserver.model.actor.instance.L2MinionInstance;
import ru.agecold.gameserver.model.L2Effect;
import ru.agecold.gameserver.skills.Env;

final class EffectHalishaPara extends L2Effect 
{

	public EffectHalishaPara(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.PARALYZE;
	}

	@Override
	public void onStart()
	{
		getEffected().startAbnormalEffect(L2Character.ABNORMAL_EFFECT_FLOATING_ROOT);
		getEffected().setIsParalyzed(true);
	}

	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_FLOATING_ROOT);
		getEffected().setIsParalyzed(false);
	}

    @Override
	public boolean onActionTime()
    {
    	return false;
    }
}
