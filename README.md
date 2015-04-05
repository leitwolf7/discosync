# discosync
One-way synchronization tool to keep disconnected sites in sync, written in Java.

## Usage scenario
You have a large collection of pictures from your little family. You never would absorb a loss of them, so you decide to keep a backup on the computer of your parents.
So how can DiscoSync help you in this situation? What to do?

* One-time hard work - transfer a complete copy of your collection to your parents (hundreds of gigs of data!).
* Use **DiscoSync** to create a new _SyncInfo_ that contains all details about the files you transferred, including checksums. You can also create this _SyncInfo_ on your computer before you transfer the files.
* After you saved your collection at your parents PC, keep the _SyncInfo_ for later use.
* After 2 months, your collection growed, you changed and deleted files and added a lot of new nice pictures. And you plan to visit your parents again, maybe because you just have to.
* Use **DiscoSync** to create a _SyncPack_ on your PC, using the saved _SyncInfo_ from the copy at your parents PC. The _SyncPack_ will contain all changes that are needed to synchonize the backup at your parents PC with your updated collection. New files, changed files, deleted files - all is in there. But it will be much smaller than your complete collection!!
* Transfer the _SyncPack_ directory to your parents. Maybe now an USB stick is enough to hold the data!
* Use **DiscoSync** to apply the _SyncPack_ to the backup at your parents PC to synchronize it with your collection.
* Thats it, you updated your backup!
* Finally, use **DiscoSync** to create a new _SyncInfo_ of the backup on your parents PC for the next run, and take it with you.

## So how to use?

On the **remote** PC, create the _RemoteSyncInfo_ of _TheBackupDir_:

```
discosync -createsyncinfo -basedir e:\TheBackupDir -syncinfo e:\RemoteSyncInfo
```

Transfer the _SyncInfo_ to your **home** PC for later use. When the data in TheOriginalDir changed, you can compare the changes use the _RemoteSyncInfo_:

```
discosync -comparesyncinfo -targetsyncinfo c:\RemoteSyncInfo -basedir c:\TheOriginalDir -verbose
```

When you plan to transfer all changes in _TheOriginalDir_ to _TheRemoteDir_, then create _MySyncPack_ on your **home** PC:

```
discosync -createsyncpack -targetsyncinfo c:\RemoteSyncInfo -basedir c:\TheOriginalDir -syncpack c:\MySyncPack
```

Transfer _MySyncPack_ to the **remote** PC. On the **remote** PC, apply _MySyncPack_ to _TheBackupDir_: 

```
discosync -applysyncpack -syncpack c:\MySyncPack -basedir e:\TheBackupDir
```

Finally, on the **remote** PC, create a new _RemoteSyncInfo_ and transfer it to your **home** PC for the next synchronization. 

```
discosync -createsyncinfo -basedir e:\TheBackupDir -syncinfo e:\RemoteSyncInfo
```
