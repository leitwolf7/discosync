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

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import org.discosync.data.*;

/*
 * Created on 01.04.2015
 */
public class Utils {

    public static void doFileListEntryCompare(FileListEntry srcEntry, Map<String,FileListEntry> dstFileMap, List<FileListEntry> fileOperations) {
        
        FileListEntry dstEntry = dstFileMap.get(srcEntry.getPath());
        
        if (dstEntry != null) {
            // file exists in dest, check size, ckSum
            if (srcEntry.getSize() == dstEntry.getSize() && srcEntry.getChecksum() == dstEntry.getChecksum()) {
                // file exists in dest, KEEP
            } else {
                // file exists, but is different -> REPLACE
                srcEntry.setOperation(FileOperations.REPLACE);
                fileOperations.add(srcEntry);
            }
            // remove processed entry
            dstFileMap.remove(dstEntry.getPath());
        } else {
            // file is missing in dest -> copy
            srcEntry.setOperation(FileOperations.COPY);
            fileOperations.add(srcEntry);
        }
    }

    public static void deleteDirectoryRecursively(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });        
    }
}
