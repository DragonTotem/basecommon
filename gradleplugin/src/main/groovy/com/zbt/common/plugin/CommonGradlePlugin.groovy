package com.zbt.common.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project

class CommonGradlePlugin implements Plugin<Project> {


    void apply(Project project) {
        println("-----apply---CommonPlugin----1.0.4--------")
        handleProject(project)

    }


    void handleProject(Project project) {
        boolean hasAppPlugin = project.plugins.hasPlugin(AppPlugin)
        boolean hasLibPlugin = project.plugins.hasPlugin(LibraryPlugin)

        if (hasAppPlugin || hasLibPlugin) {

            String pkg = pkgName(new File(project.projectDir, "src/main/java"))
            println("-----pkg name---${pkg}----------")

            project.apply {
                plugin 'kotlin-android'
                plugin 'kotlin-parcelize'
                plugin 'kotlin-kapt'
                plugin 'dagger.hilt.android.plugin'
                plugin 'android-aspectjx'
            }
            if (project.hasProperty("aspectjx")) {
                project.aspectjx.with {
                    include 'com.zbt.common.aop'
                    include pkg
                }
            }


            project.android.with {
                compileOptions {
                    encoding "utf-8"
                    sourceCompatibility JavaVersion.VERSION_1_8
                    targetCompatibility JavaVersion.VERSION_1_8
                }
                kotlinOptions {
                    jvmTarget = JavaVersion.VERSION_1_8
                }
                buildFeatures {
                    viewBinding = true
                }
            }


            project.dependencies {
                api 'com.alibaba:arouter-api:1.5.1'
                kapt 'com.alibaba:arouter-compiler:1.5.1'
                api("com.google.dagger:hilt-android:2.28-alpha") {
                    exclude group: 'androidx.activity', module: 'activity'
                    exclude group: 'androidx.fragment', module: 'fragment'
                }
                kapt "com.google.dagger:hilt-android-compiler:2.28-alpha"

            }


            if (project.hasProperty("kapt")) {
                project.findProperty("kapt").with {
                    arguments {
                        arg("AROUTER_MODULE_NAME", project.getName())
                    }
                }
            }
        } else {
            if (project.is(project.rootProject)) {
                project.subprojects(new Action<Project>() {
                    @Override
                    void execute(Project a) {

                        a.plugins.whenPluginAdded(new Action<Plugin>() {
                            @Override
                            void execute(Plugin plugin) {
                                if (plugin instanceof AppPlugin || plugin instanceof LibraryPlugin) {
                                    a.plugins.apply("zbt-android")
                                }

                            }
                        })

                    }
                })
            }
        }
    }

    static String pkgName(File file) {
        String path = ""
        file.eachDir { p1 ->
            p1.eachDir { p2 ->
                p2.eachDir { p3 ->
                    path = "${p1.name}.${p2.name}.${p3.name}"
                    return path
                }
            }
        }
        return path
    }
}