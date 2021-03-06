#!/usr/bin/env groovy
/*
**        DroidPlugin Project
**
** Copyright(c) 2015 bb.s <bangbang.song@gmail.com>
**
** This file is part of DroidPlugin.
**
** DroidPlugin is free software: you can redistribute it and/or
** modify it under the terms of the GNU Lesser General Public
** License as published by the Free Software Foundation, either
** version 3 of the License, or (at your option) any later version.
**
** DroidPlugin is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
** Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public
** License along with DroidPlugin.  If not, see <http://www.gnu.org/licenses/lgpl.txt>
**
**/


def create_activity(int processCount, int serviceCount, int providerCount, Map activityCount) {
    lineCount = 0;
    comment = "do NOT modify, this code is auto-generated by create_stub.groovy."
    className = "com.morgoo.droidplugin.stub.Stub\$"
    def activityCodeFragment = ""
    def serviceCodeFragment = ""
    def providerCodeFragment = ""
    def androidManifestFragment = ""

    // if you modify this, please update PluginProcessManager#initProcessList()
    ACTIVITY_STEM = 'ActivityStub_P'
    SERVICE_STEM = 'StubService_P'
    PROVIDER_STEM = "PrividerStub_P"
    (1..processCount).each { p ->
//        println "P: $p"
        ["", "Dialog"].each { dialog ->
//            println "dailog: $dialog"

            for (e in activityCount) {
//                println "entry ${e.key} --> ${e.value}"
                (1..e.value).each { count ->
//                    println count
                    if (lineCount++ % 7 == 0) {
                        lineCount += 1
                        activityCodeFragment += "\n" + "//" + comment;
                    }
                    def frag = "public static class ${ACTIVITY_STEM}${p}${dialog}${e.key.capitalize()}${count} extends ActivityStub {}"
                    activityCodeFragment += "\n" + frag


//                    xmlFrag =
//                    "       <activity
//            android:name=".stub.ActivityStub$Dialog${p}$SingleTop03"
//            android:allowTaskReparenting="true"
//            android:excludeFromRecents="true"
//            android:exported="false"
//            android:hardwareAccelerated="true"
//            android:label="@string/stub_name_activity"
//            android:launchMode="singleTop"
//            android:process=":PluginP05"
//            android:theme="@style/DroidPluginThemeDialog">
//            <intent-filter>
//                <action android:name="android.intent.action.MAIN" />
//                <category android:name="com.morgoo.droidplugin.category.PROXY_STUB" />
//            </intent-filter>
//        </activity>"
//
//                    println "xml: $xmlFrag"


                    def writer = new StringWriter()
                    def builder = new groovy.xml.MarkupBuilder(writer)
                    builder.setDoubleQuotes(true)
                    builder.activity('android:name': "${className}${ACTIVITY_STEM}${p}${dialog}${e.key.capitalize()}${count}",
                                'android:launchMode': "${e.key}",
                                'android:process':":PluginP${p}",
                                'android:theme':"@style/DroidPluginTheme${dialog}",
                                'android:allowTaskReparenting': "false",
                                'android:excludeFromRecents': "true",
                                'android:exported': "false",
                                'android:hardwareAccelerated':"true",
                                'android:label':"@string/stub_name_activity",) {
                           'intent-filter' {
                            action ('android:name': "android.intent.action.MAIN")
                            category ('android:name': "com.morgoo.droidplugin.category.PROXY_STUB")
                        }
                    }

                    //println writer.toString()
                    androidManifestFragment += "\n" + "<!--" + comment + "-->" + "\n" + writer.toString();
                }


            }
        }

        lineCount = 0;
        (1..serviceCount).each { s ->
            if (lineCount++ % 7 == 0) {
                lineCount += 1
                serviceCodeFragment += "\n" + "//" + comment;
            }
            def frag = "public static class ${SERVICE_STEM}${p}_${s} extends ServiceStub {}"
            serviceCodeFragment += "\n" + frag

            def writer = new StringWriter()
            def builder = new groovy.xml.MarkupBuilder(writer)
            builder.setDoubleQuotes(true)
            builder.service('android:name': "${className}${SERVICE_STEM}${p}_${s}",
                    'android:process':":PluginP${p}",
                    'android:exported': "false",) {
                'intent-filter' {
                    action ('android:name': "android.intent.action.MAIN")
                    category ('android:name': "com.morgoo.droidplugin.category.PROXY_STUB")
                }
            }

//            println writer.toString()
            androidManifestFragment += "\n" + "<!--" + comment + "-->" + "\n" + writer.toString();
        }

        lineCount = 0;
        (1..providerCount).each { s ->
            if (lineCount++ % 7 == 0) {
                lineCount += 1
                providerCodeFragment += "\n" + "//" + comment;
            }
            def frag = "public static class ${PROVIDER_STEM}${p}_${s} extends ProviderStub {}"
            providerCodeFragment += "\n" + frag

            def writer = new StringWriter()
            def builder = new groovy.xml.MarkupBuilder(writer)
            builder.setDoubleQuotes(true)
            builder.provider('android:name': "${className}${PROVIDER_STEM}${p}_${s}",
                    'android:process':":PluginP${p}",
                    'android:authorities':"com.morgoo.droidplugin_stub_P${p}",
                    'android:exported': "false",
                    'android:label':"@string/stub_name_povider",)

//            println writer.toString()
            androidManifestFragment += "\n" + "<!--" + comment + "-->" + "\n" + writer.toString();
        }

    }

//    new File("StubActivity.java.template").append(codeFragment);
//    println "codeFragment: $codeFragment"

    destStubFile = new File("src/com/morgoo/droidplugin/stub/Stub.java")
    destStubFile.delete()
    new File("Stub.java.template").eachLine {
        if (it.contains("ACTIVITY_STUB")) {
            it = activityCodeFragment
        }
        if (it.contains("SERVICE_STUB")) {
            it = serviceCodeFragment
        }
        if (it.contains("PROVIDER_STUB")) {
            it = providerCodeFragment
        }
        destStubFile.append(it)
        destStubFile.append("\n")
    }

    AndroidManifestFile = "AndroidManifest.xml"
    BAK_AndroidManifestFile = "AndroidManifest.xml.bak"
    "cp -f ${AndroidManifestFile} ${BAK_AndroidManifestFile}".execute();
    destManifestFile = new File(AndroidManifestFile);
    destManifestFile.delete()
    startStub = false
    endStub = false;
    startComment = ""
    endComment = ""
    ignore = false
    new File(BAK_AndroidManifestFile).eachLine {
        if (it.contains("STUB_COMPONENT_START")) {
            startStub = true
            startComment = it;
        }
        if (it.contains("STUB_COMPONENT_END")) {
            endStub = true;
            endComment = it;
        }

        if (startStub && !endStub){
            ignore = true
        } else if(startStub && endStub){
            it = startComment + "\n" + androidManifestFragment + "\n" + endComment
            startStub = false
            ignore = false
        }

        if (!ignore) {
            destManifestFile.append(it)
            destManifestFile.append("\n")
        }
    }

    "rm -f ${BAK_AndroidManifestFile}".execute();
}

// key launchmode for activity
// value how many stub activities for this type
activityCount = ["standard"      : 10,
              "singleInstance": 10,
              "singleTop"     : 10,
              "singleTask"    : 10]

create_activity(4, // process count
                6, // service count
                1, // provider count
                 [  "standard"      : 10,
                     "singleInstance": 10,
                     "singleTop"     : 10,
                    "singleTask"    : 17
                 ]
                 )

