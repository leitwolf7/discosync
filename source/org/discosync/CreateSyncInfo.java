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

package org.discosync;

import java.io.*;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.discosync.data.FileListDatabase;
import org.discosync.scanner.FileCreateSyncInfoScanner;

public class CreateSyncInfo implements IInvokable {

    public boolean invoke(CommandLine cmd) throws SQLException, IOException {
        
        boolean retval = true;
        
        // <basedir>, <syncinfo>
        if (!cmd.hasOption("basedir")) {
            System.out.println("Syntax error: Command createsyncinfo requires option basedir.");
            retval = false;
        }
        if (!cmd.hasOption("syncinfo")) {
            System.out.println("Syntax error: Command createsyncinfo requires option syncinfo.");
            retval = false;
        }
        
        if (!retval) {
            return false;
        }
        
        String baseDir = cmd.getOptionValue("basedir");
        String syncName = cmd.getOptionValue("syncinfo");
        
        createSyncInfo(baseDir, syncName);
        
        return true;
    }
    
    
    /**
     * Scan a baseDir and create a full syncInfo database.
     */
    protected void createSyncInfo(String baseDir, String syncName) throws SQLException, IOException {
        FileListDatabase db = new FileListDatabase(syncName);
        db.open();
        db.createFileListTable();
        
        new FileCreateSyncInfoScanner().scan(new File(baseDir), db);
        
        db.close();
    }
}
