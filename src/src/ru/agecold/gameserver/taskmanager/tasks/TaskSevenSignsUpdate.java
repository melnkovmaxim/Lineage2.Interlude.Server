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
package ru.agecold.gameserver.taskmanager.tasks;

import java.util.logging.Logger;

import ru.agecold.gameserver.SevenSigns;
import ru.agecold.gameserver.SevenSignsFestival;
import ru.agecold.gameserver.taskmanager.Task;
import ru.agecold.gameserver.taskmanager.TaskManager;
import ru.agecold.gameserver.taskmanager.TaskTypes;
import ru.agecold.gameserver.taskmanager.TaskManager.ExecutedTask;
import ru.agecold.util.log.AbstractLogger;

/**
 * Updates all data for the Seven Signs and Festival of Darkness engines,
 * when time is elapsed.
 *
 * @author Tempy
 */
public class TaskSevenSignsUpdate extends Task
{
    private static final Logger _log = AbstractLogger.getLogger(TaskSevenSignsUpdate.class.getName());
    public static final String NAME = "SevenSignsUpdate";

    @Override
	public String getName()
    {
        return NAME;
    }

    @Override
	public void onTimeElapsed(ExecutedTask task)
    {
        try {
            SevenSigns.getInstance().saveSevenSignsData(null, true);

            if (!SevenSigns.getInstance().isSealValidationPeriod())
                SevenSignsFestival.getInstance().saveFestivalData(false);

            _log.info("SevenSigns: Data updated successfully.");
        }
        catch (Exception e) {
            _log.warning("SevenSigns: Failed to save Seven Signs configuration: " + e);
        }
    }

    @Override
	public void initializate()
    {
        super.initializate();
        TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "1800000", "1800000", "");
    }
}
