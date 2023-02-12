# SmartStorage
A cloud storage for group, personal, everyone.

## Depends
* [CommandAPI](https://www.spigotmc.org/resources/api-commandapi-1-13-1-19-2.62353/)

## Commands && Permissions

* `/serverstorage[ss]` : `smartstorage.serverstorage`  
open server storage.

* `/personalstorage[ps]` : `smartstorage.personalstorage`  
open your private storage.

* `/personalstorageopen[pso]` : `smartstorage.personalstorageopen`  
make player to open personal storage.

* `/groupstorage[gs]` : `smartstorage.groupstorage`  
open group storage.

* `/groupstorageopen[gso] <player> <storage>` : `smartstorage.groupstorageopen`  
make player to open `storage`.

* `/gsedit[gse] create <storageName>` : `smartstorage.groupstorageedit.create`  
create new group storage.

* `/gsedit[gse] delete <storageName>` : `smartstorage.groupstorageedit.delete`  
delete group storage that you created. (Don't forget to collect items in storage before delete.)

* `/gsedit[gse] addmember <storageName> <owner> <member>` : `smartstorage.groupstorageedit.addmember`  
add `member` to specify storage's member.

* `/gsedit[gse] removemember <storageName> <owner> <member>` : `smartstorage.groupstorageedit.removemember`  
remove `member` from specify storage's member.

* `/gsedit[gse] list` : `smartstorage.groupstorageedit.list`  
show joined group storages.

* `/smartstorage save` : `smartstorage.smartstorage.save`  
save storage data to file. (OP only)

## Config

```
mute-save-alert: false
```

* `mute-save-alert`  
Mute interval saving alert.
