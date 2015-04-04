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

import org.apache.commons.cli.*;

/*
 * Flow: createsyncinfo, comparesyncinfo, createsyncpack, applysyncpack
 */
public class DiscoSync {
    
    public DiscoSync() {
    }
    
    public static void main(String[] args) throws Exception {
        new DiscoSync().run(args);
    }
    
    protected Options getCmdlineOptions() {

        Options options = new Options();

        options.addOption("createsyncinfo", false, "create sync info of <basedir> in <syncinfo>");
        options.addOption("comparesyncinfo", false, "compare a <targetsyncinfo> with a <sourcesyncinfo> or a <basedir>");
        options.addOption("createsyncpack", false, "create a <syncpack> for <targetsyncinfo> using a <basedir> and optionally <sourcesyncinfo>."
                +"when <sourcesyncinfo> is specified the process is faster, but it must be in sync with the <basedir> contents!");
        options.addOption("applysyncpack", false, "apply <syncpack> to <basedir>"); // FIXME: optional targetsyncinfo for pre-check!
        
        options.addOption("basedir", true, "base directory");
        options.addOption("syncinfo", true, "sync info location");
        
        options.addOption("sourcesyncinfo", true, "source sync info location directory");
        options.addOption("targetsyncinfo", true, "target sync info location directory");
        
        options.addOption("syncpack", true, "sync pack location directory");
        
        options.addOption("verbose", false, "verbose output");

        return options;
    }
    
    /**
     * Ensure valid combinations, print errors.
     */
    protected IInvokable evalCmdline(CommandLine cmd) {

        int count = (cmd.hasOption("createsyncinfo")?1:0)
                + (cmd.hasOption("comparesyncinfo")?1:0)
                + (cmd.hasOption("createsyncpack")?1:0)
                + (cmd.hasOption("applysyncpack")?1:0);
        if (count == 0 || count > 1) {
            System.out.println("Syntax error: One single command of createsyncinfo, comparesyncinfo, createsyncpack or applysyncpack must be specified.");
            return null;
        }
        
        if (cmd.hasOption("createsyncinfo")) {
            return new CreateSyncInfo();
        }
        if (cmd.hasOption("comparesyncinfo")) {
            return new CompareSyncInfo();
        }
        if (cmd.hasOption("createsyncpack")) {
            return new CreateSyncPack();
        }
        if (cmd.hasOption("applysyncpack")) {
            return new ApplySyncPack();
        }
        
        System.out.println("evalCmdline: Internal error.");
        return null;
    }
    
    public void printUsage(Options options) {
        new HelpFormatter().printHelp("DiscoSync", options);
    }
    
    private void run(String[] args) throws Exception {
        
        System.out.println("DiscoSync - Copyright (C) 2015, 2015 leitwolf7 - Home: github.com, leitwolf7/discosync");
        System.out.println("This program comes with ABSOLUTELY NO WARRANTY.");
        System.out.println("This is free software, and you are welcome to redistribute it under certain conditions.");
        System.out.println();
        
        Options cmdlineOpts = getCmdlineOptions();
        
        CommandLineParser parser = new GnuParser();
        
        CommandLine cmd = null;
        try {
            cmd = parser.parse(cmdlineOpts, args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(4);
        }
        
        IInvokable invoker = evalCmdline(cmd);
        if (invoker == null) {
            System.exit(4);
        }

        long start = System.currentTimeMillis();
        
        invoker.invoke(cmd);
        
        long dura = System.currentTimeMillis() - start;
        System.out.println("Duration: "+(dura/1000)+" seconds");
        System.out.println("Ready.");
        
//        List<FileListEntry> fileOperations;
//        
//        createSyncInfo("c:\\#\\1", "c:\\#\\sync-1");
//        createSyncInfo("c:\\#\\2", "c:\\#\\sync-2");
//        
//        fileOperations = compareSyncInfo("c:\\#\\sync-2", "c:\\#\\sync-1");
//        Utils.showSyncResult(fileOperations, true);
//
//        fileOperations = compareSyncInfoAndFiles("c:\\#\\2", "c:\\#\\sync-1");
//        Utils.showSyncResult(fileOperations, true);
//        
//        // syncPack to make 1 like 2
//        createSyncPack("c:\\#\\2", fileOperations, "C:\\#\\syncpack-2-to-1");
//
//        applySyncPack("C:\\#\\syncpack-2-to-1", "c:\\#\\1-apply");
//
//        // -------------------------
//        
//        fileOperations = compareSyncInfo("c:\\#\\sync-1", "c:\\#\\sync-2");
//        Utils.showSyncResult(fileOperations, true);
//
//        fileOperations = compareSyncInfoAndFiles("c:\\#\\1", "c:\\#\\sync-2");
//        Utils.showSyncResult(fileOperations, true);
//        
//        // syncPack to make 2 like 1
//        createSyncPack("c:\\#\\1", fileOperations, "C:\\#\\syncpack-1-to-2");
//        
//        applySyncPack("C:\\#\\syncpack-1-to-2", "c:\\#\\2-apply");
    }
}
