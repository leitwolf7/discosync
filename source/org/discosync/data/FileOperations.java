/*
 * This file is part of DiscoSync (home: github.com, leitwolf7/discosync)
 *
 * Copyright (C) 2015, 2015 leitwolf7
 *
 *  DiscoSync is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DiscoSync is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DiscoSync.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.discosync.data;

/*
 * Created on 01.04.2015
 */
public enum FileOperations {

    KEEP,
    COPY,
    REPLACE,
    DELETE;

    private static int maxLen = -1;

    public String toPadString() {

        if (maxLen < 0) {
            for (FileOperations op : FileOperations.values()) {
                maxLen = Math.max(maxLen, op.toString().length());
            }
        }

        return String.format("%1$-"+maxLen+"s", this);
    }
}