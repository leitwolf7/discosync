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
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.discosync.data.*;

public class ApplySyncPack implements IInvokable {

    @Override
    public boolean invoke(CommandLine cmd) throws SQLException, IOException {
        
        boolean retval = true;
        
        // apply <syncpack> to <basedir>
        if (!cmd.hasOption("syncpack")) {
            System.out.println("Syntax error: Command applysyncpack requires option syncpack.");
            retval = false;
        }
        if (!cmd.hasOption("basedir")) {
            System.out.println("Syntax error: Command applysyncpack requires option basedir.");
            retval = false;
        }

        if (!retval) {
            return false;
        }
        
        String syncPackDir = cmd.getOptionValue("syncpack");
        String targetDir = cmd.getOptionValue("targetdir");
        
        applySyncPack(syncPackDir, targetDir);
        
        return true;
    }

    /**
     * Apply a syncpack to a target directory.
     */
    protected void applySyncPack(String syncPackDir, String targetDir) throws SQLException, IOException {

        // read file operations from database
        File fileOpDbFile = new File(syncPackDir, "fileoperations");
        FileOperationDatabase db = new FileOperationDatabase(fileOpDbFile.getAbsolutePath());
        db.open();
        
        Iterator<FileListEntry> it = db.getFileListOperationIterator();

        Path syncFileBaseDir = Paths.get(syncPackDir, "files");
        String syncFileBaseDirStr = syncFileBaseDir.toAbsolutePath().toString();
        
        int filesCopied = 0;
        int filesReplaced = 0;
        long copySize = 0L;
        int filesDeleted = 0;
        
        while (it.hasNext()) {
            
            FileListEntry e = it.next();
            
            String path = e.getPath();
            Path sourcePath = Paths.get(syncFileBaseDirStr, path); // may not exist
            Path targetPath = Paths.get(targetDir, path); // may not exist
            
            if (e.getOperation() == FileOperations.COPY) {
                // copy new file, target files should not exist
                if (Files.exists(targetPath)) {
                    System.out.println("Error: the file should not exist: "+targetPath.toAbsolutePath().toString());
                } else {
                    Files.copy(sourcePath, targetPath);
                    filesCopied++;
                    copySize += e.getSize();
                }

            } else if (e.getOperation() == FileOperations.REPLACE) {
                // replace existing file
                if (!Files.exists(targetPath)) {
                    System.out.println("Info: the file should exist: "+targetPath.toAbsolutePath().toString());
                }
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                filesReplaced++;
                copySize += e.getSize();
                
            } else if (e.getOperation() == FileOperations.DELETE) {
                // delete existing file
                if (!Files.exists(targetPath)) {
                    System.out.println("Info: the file should exist: "+targetPath.toAbsolutePath().toString());
                } else {
                    Files.delete(targetPath);
                    filesDeleted++;
                }
            }
        }

        db.close();
        
        System.out.println("Apply of SyncPack '"+syncPackDir+"' to directory '"+targetDir+"' finished.");
        System.out.println("Files copied:   "+filesCopied);
        System.out.println("Files replaced: "+filesReplaced);
        System.out.println("Files deleted:  "+filesDeleted);
        System.out.println("Bytes copied:   "+copySize);
    }
}
