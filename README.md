# discosync
One-way synchronization tool to keep disconnected sites in sync, written in Java.

## Usage scenario
You have a large collection of pictures from your little family. You never would absorb a loss of them, so you decide to keep a backup on the computer of your parents.
So how can DiscoSync help you in this situation? What to do?
* One-time hard work - transfer a complete copy of your collection to your parents.
* Use **DiscoSync** to create a new _SyncInfo_ that contains all details about the files you transferred, including checksums. You can also create this _SyncInfo_ on your computer before you transfer the files.
* After you saved your collection at your parents PC, keep the _SyncInfo_ for later use.
* After 2 months, your collection growed, you changed and deleted file and added a lot of new nice pictures. And you plan to visit your parents again, maybe because you have to.
* Use **DiscoSync** to create a _SyncPack_ on your PC. For this, use the saved _SyncInfo_ from the copy at your parents PC. The _SyncPack_ will contain all changes that are needed to synchonize the backup at your parents PC with your updated collection. New files, changed files, deleted files - all is in there.
* Transfer the _SyncPack_ directory to your parents. Use a harddisk, dropbox, whatever....
* Use **DiscoSync** to apply the _SyncPack_ to the backup at your parents PC to synchronize it with your collection.
* Thats it, you updated your backup!
* Finally, use **DiscoSync** to create a new _SyncInfo_ of the backup on your parents PC for the next run.
