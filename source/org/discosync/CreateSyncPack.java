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
import java.nio.file.*;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.discosync.data.*;

public class CreateSyncPack implements IInvokable {

    @Override
    public boolean invoke(CommandLine cmd) throws SQLException, IOException {
        boolean retval = true;
        
        // create a <syncpack> for <targetsyncinfo> using a <basedir> and optionally <sourcesyncinfo>
        if (!cmd.hasOption("syncpack")) {
            System.out.println("Syntax error: Command createsyncpack requires option syncpack.");
            retval = false;
        }
        if (!cmd.hasOption("targetsyncinfo")) {
            System.out.println("Syntax error: Command createsyncpack requires option targetsyncinfo.");
            retval = false;
        }
        if (!cmd.hasOption("basedir")) {
            System.out.println("Syntax error: Command createsyncpack requires option basedir.");
            retval = false;
        }
        if (!retval) {
            return false;
        }
        
        String syncPack = cmd.getOptionValue("syncpack");
        String baseDir = cmd.getOptionValue("basedir");
        String targetSyncInfo = cmd.getOptionValue("targetsyncinfo");
        String sourceSyncInfo = cmd.getOptionValue("sourcesyncinfo"); // optional
        
        // create fileOperations using targetSyncInfo AND (baseDir or sourcesyncinfo)
        List<FileListEntry> fileOperations = null;
        CompareSyncInfo compareSyncInfo = new CompareSyncInfo();
        if (sourceSyncInfo != null) {
            fileOperations = compareSyncInfo.compareSyncInfo(sourceSyncInfo, targetSyncInfo);
        } else {
            fileOperations = compareSyncInfo.compareSyncInfoAndFiles(baseDir, targetSyncInfo);
        }
        
        createSyncPack(baseDir, fileOperations, syncPack);
        
        return true;
    }

    /**
     * Create a syncpack in syncPackDir, using the fileOperations and taking the files from baseDir.
     */
    protected void createSyncPack(String baseDir, List<FileListEntry> fileOperations, String syncPackDir) throws SQLException, IOException {
        
        Path syncPackDirPath = Paths.get(syncPackDir);
        Files.createDirectories(syncPackDirPath);
        
        // store file operations to database
        File fileOpDbFile = new File(syncPackDirPath.toFile(), "fileoperations");
        FileOperationDatabase db = new FileOperationDatabase(fileOpDbFile.getAbsolutePath());
        db.open();
        db.createFileOperationTable();
        db.insertFileOperations(fileOperations);
        db.close();
        
        // delete 'files' directory in syncpack and create the directory again
        Path targetBaseDir = Paths.get(syncPackDirPath.toAbsolutePath().toString(), "files");
        Utils.deleteDirectoryRecursively(targetBaseDir);
        if (!Files.exists(targetBaseDir)) {
            Files.createDirectories(targetBaseDir);
        }
        String targetBaseDirStr = targetBaseDir.toAbsolutePath().toString();
        
        // copy all files that need a COPY or REPLACE to the syncpack
        for (FileListEntry e : fileOperations) {
            
            if (e.getOperation() != FileOperations.COPY && e.getOperation() != FileOperations.REPLACE) {
                continue;
            }
            
            String path = e.getPath();

            Path sourcePath = FileSystems.getDefault().getPath(baseDir, path);
            
            Path targetPath = Paths.get(targetBaseDirStr, path);
            if (!Files.exists(targetPath.getParent())) {
                Files.createDirectories(targetPath.getParent());
            }
            
            Files.copy(sourcePath, targetPath);
        }
    }

}
