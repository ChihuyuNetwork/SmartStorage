# SmartStorage
A cloud storage for group, personal, everyone.

## Depends
* [CommandAPI](https://www.spigotmc.org/resources/api-commandapi-1-13-1-19-2.62353/)

## Commands

* `/serverstorage[ss]`  
open server storage.

* `/personalstorage[ps]`  
open your private storage.

* `/personalstorageopen[pso]`  
make player to open personal storage.

* `/groupstorage[gs]`  
open group storage.

* `/groupstorageopen[gso] <player> <storage>`  
make player to open `storage`.

* `/gsedit[gse] create <storageName>`  
create new group storage.

* `/gsedit[gse] delete <storageName>`  
delete group storage that you created. (Don't forget to collect items in storage before delete.)

* `/gsedit[gse] addmember <storageName> <owner> <member>`  
add `member` to specify storage's member.

* `/gsedit[gse] removemember <storageName> <owner> <member>`  
remove `member` from specify storage's member.

* `/gsedit[gse] list`  
show joined group storages.

* `/smartstorage save`  
save storage data to file. (OP only)

## Permissions

* `smartstorage.serverstorage`  
use /serverstorage

* `smartstorage.personalstorage`  
use /personalstorage

* `smartstorage.personalstorageopen`  
use /personalstorageopen

* `smartstorage.groupstorage`  
use /groupstorage

* `smartstorage.groupstorageopen`  
use /groupstorageopen

* `smartstorage.groupstorageedit.create`  
use /gsedit create

* `smartstorage.groupstorageedit.delete`  
use /gsedit delete

* `smartstorage.groupstorageedit.addmember`  
use /gsedit addmember

* `smartstorage.groupstorageedit.removemember`  
use /gsedit removemember

* `smartstorage.groupstorageedit.list`  
use /gsedit list

* `smartstorage.smartstorage`  
use /smartstorage save