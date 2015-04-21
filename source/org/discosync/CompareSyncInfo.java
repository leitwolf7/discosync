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
import java.util.*;

import org.apache.commons.cli.CommandLine;
import org.discosync.data.*;
import org.discosync.scanner.FileCheckSyncInfoScanner;

public class CompareSyncInfo implements IInvokable {

    @Override
    public boolean invoke(CommandLine cmd) throws SQLException, IOException {

        boolean retval = true;

        // <targetsyncinfo> , <sourcesyncinfo> or <basedir>
        if (!cmd.hasOption("targetsyncinfo")) {
            System.out.println("Syntax error: Command comparesyncinfo requires option targetsyncinfo.");
            retval = false;
        }
        if (!cmd.hasOption("sourcesyncinfo") && !cmd.hasOption("basedir")) {
            System.out.println("Syntax error: Command comparesyncinfo requires option sourcesyncinfo or option basedir.");
            retval = false;
        }
        if (cmd.hasOption("sourcesyncinfo") && cmd.hasOption("basedir")) {
            System.out.println("Syntax error: Command comparesyncinfo requires option sourcesyncinfo or option basedir, but not both of them.");
            retval = false;
        }

        if (!retval) {
            return false;
        }

        String targetSyncInfo = cmd.getOptionValue("targetsyncinfo");

        List<FileListEntry> fileOperations = null;
        if (cmd.hasOption("sourcesyncinfo")) {
            String sourceSyncInfo = cmd.getOptionValue("sourcesyncinfo");

            System.out.println("Compare target syncinfo '"+targetSyncInfo+"' to source syncinfo '"+sourceSyncInfo+"'.");

            fileOperations = compareSyncInfo(sourceSyncInfo, targetSyncInfo);

        } else if (cmd.hasOption("basedir")) {
            String basedir = cmd.getOptionValue("basedir");

            System.out.println("Compare target syncinfo '"+targetSyncInfo+"' to directory '"+basedir+"'.");

            System.out.println("Scanning directory ...");
            fileOperations = compareSyncInfoAndFiles(basedir, targetSyncInfo);
        }

        Utils.showSyncResult(fileOperations, cmd.hasOption("verbose"));

        return true;
    }

    /**
     * Compare a target syncInfo database with a source baseDir.
     */
    public List<FileListEntry> compareSyncInfoAndFiles(String baseDir, String targetSyncName) throws SQLException, IOException {

        FileListDatabase db = new FileListDatabase(targetSyncName);
        db.open();
        Map<String,FileListEntry> dstFileMap = db.retrieveFileList();
        db.close();

        List<FileListEntry> fileOperations = new ArrayList<>();

        new FileCheckSyncInfoScanner().scan(new File(baseDir), dstFileMap, fileOperations);

        // all items still in dstFileMap should not be there -> DELETE
        for (FileListEntry e : dstFileMap.values()) {
            e.setOperation(FileOperations.DELETE);
            fileOperations.add(e);
        }

        return fileOperations;
    }

    /**
     * Compare two syncInfos.
     * @return fileOperations needed to make target the same as source
     */
    public List<FileListEntry> compareSyncInfo(String sourceSyncName, String targetSyncName) throws SQLException, IOException {

        FileListDatabase dstDb = new FileListDatabase(targetSyncName);
        dstDb.open();
        Map<String,FileListEntry> dstFileMap = dstDb.retrieveFileList();
        dstDb.close();

        List<FileListEntry> fileOperations = new ArrayList<>();

        FileListDatabase srcDb = new FileListDatabase(sourceSyncName);
        srcDb.open();
        Iterator<FileListEntry> it = srcDb.getFileListEntryIterator();
        while (it.hasNext()) {
            FileListEntry srcEntry = it.next();
            Utils.doFileListEntryCompare(srcEntry, dstFileMap, fileOperations);
        }
        srcDb.close();

        // all items still in dstFileMap should not be there -> DELETE
        for (FileListEntry e : dstFileMap.values()) {
            e.setOperation(FileOperations.DELETE);
            fileOperations.add(e);
        }

        return fileOperations;
    }
}
