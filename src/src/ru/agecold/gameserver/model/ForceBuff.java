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

package ru.agecold.gameserver.model;

import java.util.concurrent.Future;

import ru.agecold.gameserver.ThreadPoolManager;
import ru.agecold.gameserver.datatables.SkillTable;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.skills.effects.EffectForce;
import ru.agecold.gameserver.util.Util;

/**
 * @author kombat
 *
 */
public final class ForceBuff
{
    final int _skillCastRange;
    final int _forceId;
    L2PcInstance _caster;
    L2PcInstance _target;
    Future<?> _geoCheckTask;
    
    public L2PcInstance getCaster()
    {
        return _caster;
    }
    
    public L2PcInstance getTarget()
    {
        return _target;
    }
    
    public ForceBuff(L2PcInstance caster, L2PcInstance target, L2Skill skill)
    {
        _skillCastRange = skill.getCastRange();
        _caster = caster;
        _target = target;
        _forceId = skill.getForceId();
        
        L2Effect effect = _target.getFirstEffect(_forceId);
        if (effect != null)
            ((EffectForce)effect).increaseForce();
        else
            SkillTable.getInstance().getInfo(_forceId, 1).getEffects(_caster, _target);
        
        _geoCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new GeoCheckTask(), 1000, 1000);
    }
    
    public void delete()
    {
        _caster.setForceBuff(null);
        L2Effect effect = _target.getFirstEffect(_forceId);
        if (effect != null)
            ((EffectForce)effect).decreaseForce();
        
        _geoCheckTask.cancel(true);
    }
    
    class GeoCheckTask implements Runnable
    {
        public void run()
        {
            try
            {
                if (!Util.checkIfInRange(_skillCastRange, _caster, _target, true))
                    delete();
                
                if (!_caster.canSeeTarget(_target))
                    delete();
            }
            catch (Exception e)
            {
                // ignore
            }
        }
    }
}
