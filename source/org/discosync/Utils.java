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

    /**
     * Check if the srcEntry is in dstFileMap, and compare the entries.
     * Can create a fileOperation that is added to the fileOperations list.
     *
     * @param srcEntry  source entry
     * @param dstFileMap  map with files
     * @param fileOperations list to add created file operations to
     */
    public static void doFileListEntryCompare(FileListEntry srcEntry, Map<String,FileListEntry> dstFileMap, List<FileListEntry> fileOperations) {

        FileListEntry dstEntry = dstFileMap.get(srcEntry.getPath());

        if (srcEntry.isDirectory()) {

            if (dstEntry != null) {
                // exists on target - is it a directory?
                if (dstEntry.isDirectory()) {
                    // yes, already exists on target - KEEP
                    dstFileMap.remove(dstEntry.getPath());
                } else {
                    // dir exists on target, but is a FILE ! FIXME Remove file, create directory!
                    System.out.println("Internal Error: file name same as a directory name!");
                }
            } else {
                // dir does not exist on target - CREATE
                srcEntry.setOperation(FileOperations.COPY);
                fileOperations.add(srcEntry);
            }

        } else {

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
    }

    /**
     * Delete the directory recursively.
     *
     * @param directory  directory to delete
     */
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

    /**
     * Check if the directory is empty.
     *
     * @param directory directory to check
     * @return true when empty
     */
    public static boolean isDirectoryEmpty(Path directory) throws IOException {
        boolean retval = true;
        DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory);
        if (dirStream.iterator().hasNext()) {
            retval = false;
        }
        dirStream.close();
        return retval;
    }

    /**
     * Visualize fileOperations.
     *
     * @param fileOperations List of file operations
     * @param verbose when true each operation is printed
     */
    public static void showSyncResult(List<FileListEntry> fileOperations, boolean verbose) {
        long copySize = 0;
        long deleteSize = 0;
        System.out.println("Operations: "+String.format("%,d", fileOperations.size()));
        for (FileListEntry e : fileOperations) {
            if (verbose) {
                System.out.println(e.toString());
            }
            if (e.getOperation() == FileOperations.COPY || e.getOperation() == FileOperations.REPLACE) {
                copySize += e.getSize();
            } else if (e.getOperation() == FileOperations.DELETE) {
                deleteSize += e.getSize();
            }
        }

        System.out.println("Bytes to copy to target    : "+String.format("%,d", copySize));
        System.out.println("Bytes to delete from target: "+String.format("%,d", deleteSize));
    }
}
