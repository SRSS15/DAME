#! /usr/bin/env python
#
# Questo script si occupa di ricompilare, a partire dallo smali, un apk.
# Deve essere eseguito dalla shell dei comandi in quanto fa uso delle variabili di ambiente $PATH
# Es. di esecuzione ./smaliToSignedApk.py /path/to/decompiled/apk/directory

__author__ = 'Roberto Falzarano'

import sys, os

# da modificare nel caso il tool zipalign si trovasse in altra directory
__zipalign__ = '/home/android/tools/android/android-sdk-linux_x86/build-tools/21.1.2/zipalign'
__apktool__ = 'apktool'
__jarsigner__ = 'jarsigner'
__keytool__ = 'keytool'
__keystore_path__ = 'srss_key.keystore'
__alias_name__ = 'srss'
__extention__ = '.apk'

def getCompiledApkPath(decompiledDir):
    searchPath = os.path.abspath(decompiledDir + '/dist/')
    fileList = os.listdir(searchPath)
    for file in fileList:
        if file[len(file)-4:len(file)] == __extention__:
            return os.path.abspath(searchPath + '/' + file)

if len(sys.argv) == 1:
    print "inserire la directory dove e' presente l'apk decompilato"
else:
    decompiledDir = os.path.abspath(sys.argv[1])
    os.system(__apktool__+ ' b ' + decompiledDir)

    #recupera il path del nuovo apk creato
    compiledApkPath = getCompiledApkPath(decompiledDir)

    if not compiledApkPath:
        sys.exit('apk compilato non trovato')

    # creazione della chiave per la firma
    os.system(__keytool__ + ' -genkey' + ' -v' + ' -keystore ' + __keystore_path__ +
             ' -alias ' + __alias_name__ + ' -keyalg' + ' RSA' + ' -keysize ' + str(2048) + ' -validity ' + str(10000))

    # firma dell'apk
    os.system(__jarsigner__ + ' -verbose' + ' -sigalg' + ' SHA1withRSA' + ' -digestalg' + ' SHA1' +
             ' -keystore ' + __keystore_path__ + " " + compiledApkPath + " " + __alias_name__)

    os.system('rm ' + __keystore_path__)

    # allineamento dell'apk
    os.system(__zipalign__ + ' -v' + ' 4 ' + compiledApkPath + " " + compiledApkPath + '_aligned')