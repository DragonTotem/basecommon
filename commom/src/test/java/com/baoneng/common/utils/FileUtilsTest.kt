package com.zbt.common.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.io.FileInputStream

/**
 * Description:
 *
 * @Author: xuwd11
 * Date: 2021/1/20 16:17
 */
class FileUtilsTest {
    private var path = "abc"
    private var path2 = "efg"
    private var fileName1 = "fileName1"
    private var fileName2 = "fileName2.png"
    private var fileNoFile = "nofile"

    @org.junit.Before
    fun setUp() {
        File(path).mkdirs()
        File(path, fileName1).createNewFile()
        File(path, fileName2).createNewFile()
        File(fileNoFile).delete()
    }

    @org.junit.After
    fun tearDown() {
        FileUtils.deleteAllFileByDirectory(path)
        FileUtils.deleteAllFileByDirectory(path2)
    }


    @Test
    fun fileExists() {
        assertThat(FileUtils.fileExists(path)).isTrue()
        assertThat(FileUtils.fileExists(fileNoFile)).isFalse()
    }

    @Test
    fun deleteFile() {
        val filePath = File(path, fileName1).absolutePath
        assertThat(FileUtils.deleteFile(filePath)).isTrue()

        assertThat(FileUtils.fileExists(filePath)).isFalse()
    }

    @Test
    fun deleteAllFileByDirectory() {
        assertThat(FileUtils.deleteAllFileByDirectory(path)).isTrue()
        assertThat(FileUtils.fileExists(path)).isFalse()

    }

    @Test
    fun deleteFileByDirectory() {
        FileUtils.deleteFileByDirectory(path, ".png")
        val filePath1 = File(path, fileName1).absolutePath
        val filePath2 = File(path, fileName2).absolutePath
        assertThat(FileUtils.fileExists(filePath1)).isTrue()
        assertThat(FileUtils.fileExists(filePath2)).isFalse()
    }


    @Test
    fun copyFileFromInputStream() {
        assertThat(FileUtils.copyFileFromInputStream(FileInputStream(File(path, fileName1)), File(fileNoFile))).isTrue()

        assertThat(FileUtils.fileExists(fileNoFile)).isTrue()
    }

    @Test
    fun createFileParent() {
        FileUtils.createFileParent(File(path2, fileName1))
        assertThat(FileUtils.fileExists(path2)).isTrue()
    }

    @Test
    fun copyFile() {
        FileUtils.copyFile(File(path, fileName1), File(path, fileNoFile))
        assertThat(FileUtils.fileExists(File(path, fileNoFile).absolutePath)).isTrue()
    }

    @Test
    fun copyFileDirectory() {
        FileUtils.copyFileDirectory(File(path), File(path2))
        assertThat(FileUtils.fileExists(File(path2, fileName2).absolutePath)).isTrue()
    }


}