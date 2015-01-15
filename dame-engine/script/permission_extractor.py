#! /usr/bin/env python
# coding: iso-8859-15


__author__ = 'Roberto Falzarano'

# inserire in questa variabile il percorso della directory di androguard
__androguard__ = '/home/android/tools/androguard'

import sys, hashlib, os

sys.path.append(__androguard__)

from androguard.core.analysis import analysis
from androguard.core.bytecodes.apk import APK
from androguard.core.bytecodes.dvm import DalvikVMFormat
from androguard.core.analysis.analysis import TAINTED_PACKAGE_CALL, PathVar
from androguard.core.analysis.analysis import PathP



""" funzione che prende in ingresso un oggetto DalvikVirtualMachineFormato e un oggetto che rappresenta un permesso
e ne restituisce le classi e i metodi dove viene utilizzato"""
def showPermission(dvmFormatObject, permission):
    toReturn = []

    for callMethod in permission:
        toReturn.append(showPermissionPathUsage(dvmFormatObject, callMethod))

    return toReturn

def showPermissionPathUsage(vm, path) :
    cm = vm.get_class_manager()

    if isinstance(path, PathVar) :
        dst_class_name, dst_method_name, dst_descriptor =  path.get_dst( cm )
        info_var = path.get_var_info()
        return [dst_class_name,
                dst_method_name]
                #dst_descriptor]
    else:
        src_class_name, src_method_name, src_descriptor =  path.get_src( cm )

        return [src_class_name,
                src_method_name]
                #src_descriptor]

""" funzione che prende in ingresso un oggetto DalvikVirtualMachineFormato e un oggetto che rappresenta i permessi
 e ne crea un dizionario in cui per ogni permesso, che è la chiave, viene indicato in quale classe e metodo viene usato"""
def toDictionary(dvmFormatObject, permissions):
    toReturn = {}
    keys = permissions.keys()

    for key in keys:
        temp = showPermission(dvmFormatObject, permissions[key])
        toReturn[key] = temp

    return toReturn

""" partendo da un dizionario dei permessi ne crea una stringa formattata in formatoo json """
def dictToJson(permissionDictionary):
    toReturn = "{\"permissions\":["

    keys = permissionDictionary.keys()

    # contatore per le virgole
    i = 0


    for key in keys:
        i += 1
        toReturn += "\n\t{\"type\":\""+key+"\",\"usage-point\":["
        #altro contatore per le virgole
        k=0

        for usagePoint in permissionDictionary[key]:
            k += 1
            toReturn += "\n\t\t{\"class\":\"" + usagePoint[0] + "\", \"method\":\"" + usagePoint[1] +"\"}"
            if k < len(permissionDictionary[key]):# inserisce la virgola fin tanto che non è l'ultimo oggetto
                toReturn += ","

        toReturn += "\n\t\t]\n\t}"
        if i < len(permissionDictionary):# inserisce la virgola fin tanto che non è l'ultimo oggetto
            toReturn += ","

    toReturn += "]\n}"
    return toReturn



__extention__ = ".apk"

if len(sys.argv) == 1:
    print "percorso del file apk non inserito"

elif sys.argv[1][len(sys.argv[1])-4:len(sys.argv[1])] != __extention__:
    print "E' stato indicato un file che non ha estensione apk"
    print sys.argv

else:
    # cattura del percorso del file apk
    apkToBeAnalyzed = sys.argv[1]

    #variabile che rappresenta il file apk
    a = APK(apkToBeAnalyzed)

    #variabile che rappresenta il file dex
    dexFile = DalvikVMFormat(a.get_dex())

    #variabile che rappresenta il file dex dopo essere stato analizzato
    dexAnalyzed = analysis.uVMAnalysis(dexFile)

    # print a.show()
    # print "package name " + a.get_package()
    # print a.get_permissions()
    # print "\n"

    # mostra dove vengono usati i permessi
    permissions = dexAnalyzed.get_permissions([])

    permissionDictionary = toDictionary(dexFile, permissions)

    # stampa dei permessi usati in formato json
    print dictToJson(permissionDictionary)