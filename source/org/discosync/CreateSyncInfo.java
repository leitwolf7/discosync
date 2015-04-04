/*
 * Created on 04.04.2015
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
