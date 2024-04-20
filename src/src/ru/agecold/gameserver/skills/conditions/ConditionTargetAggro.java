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
package ru.agecold.gameserver.skills.conditions;

import ru.agecold.gameserver.model.L2Character;
import ru.agecold.gameserver.model.actor.instance.L2MonsterInstance;
import ru.agecold.gameserver.skills.Env;

/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ConditionTargetAggro extends Condition {

    private final boolean _isAggro;

    public ConditionTargetAggro(boolean isAggro) {
        _isAggro = isAggro;
    }

    @Override
    public boolean testImpl(Env env) {
        L2Character target = env.target;

        if (target.isL2Monster()) {
            return ((L2MonsterInstance) target).isAggressive() == _isAggro;
        }

        if (target.isPlayer()) {
            return target.getKarma() > 0;
        }

        return false;
    }

}
