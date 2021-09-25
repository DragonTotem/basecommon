package com.zbt.common.utils

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Description: 文件工具类
 *
 * @Author: xuwd
 * Date: 2020/10/14 10:10
 */
object FileUtils {
    /**
     * 文件是否存在
     * @param filePath  文件路径
     */
    @JvmStatic
    fun fileExists(filePath: String?): Boolean {
        return filePath?.let {
            File(it).exists()
        } ?: false
    }

    /**
     * 删除单个文件
     * @param filePath 文件路径
     * @return true成功
     */
    @JvmStatic
    fun deleteFile(filePath: String?): Boolean {
        return try {
            if (filePath.isNullOrBlank()) {
                false
            } else {
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()
                } else false
            }

        } catch (e: Exception) {
            false
        }
    }

    /**
     * 删除文件夹及其子文件
     * @param directory 文件路径
     */
    @JvmStatic
    fun deleteAllFileByDirectory(directory: String?): Boolean {
        if (directory.isNullOrBlank()) {
            return false
        }
        try {
            val file = File(directory)
            if (file.exists() && file.isDirectory) {
                return file.deleteRecursively()
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 删除指定文件夹下的特定后缀文件，不包含子文件夹
     * @param directory 文件夹完整路径
     * @param endName 文件后缀名 如.png .txt
     */
    @JvmStatic
    fun deleteFileByDirectory(directory: String?, endName: String?) {
        if (directory.isNullOrBlank() || endName.isNullOrBlank()) {
            return
        }
        val file = File(directory)
        if (file.exists() && file.isDirectory) {

            file.listFiles()?.filter { f -> f.isFile && f.name.endsWith(endName) }
                    ?.forEach { f -> f.delete() }

        }
    }

    /**
     * 解压zip文件到指定文件夹
     * @param asset zip文件流
     * @param desDir 解压文件路径
     */
    @JvmStatic
    fun decompressingZip(asset: File?, desDir: File?): Boolean {
        if (asset == null || desDir == null) return false
        if (!asset.exists()) return false
        if (!desDir.exists() || desDir.isFile) {
            desDir.mkdirs()
        }

        return decompressingZip(FileInputStream(asset),desDir)
    }
    /**
     * 解压zip文件到指定文件夹
     * @param asset zip文件流
     * @param desDir 解压文件路径
     */
    @JvmStatic
    fun decompressingZip(asset: InputStream?, desDir: File?): Boolean {
        if (asset == null || desDir == null) return false
        if (!desDir.exists() || desDir.isFile) {
            desDir.mkdirs()
        }


        return try {
            var file: File? = null
            val zip = ZipInputStream(asset)
            BufferedInputStream(zip).use { zipBuffer ->
                var entry: ZipEntry? = null

                while (zip.nextEntry?.also { entry = it } != null) {

                    file = File(desDir, entry!!.name)
                    if (entry!!.isDirectory) {
                        file?.mkdirs()
                        continue
                    }

                    try {

                        BufferedOutputStream(FileOutputStream(file)).use { buffer ->
                            var b: Int = zipBuffer.read()
                            while (b != -1) {
                                buffer.write(b)
                                b = zipBuffer.read()
                            }
                        }


                    } catch (e: Exception) {
                        e.printStackTrace()
                        return false
                    }


                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 从输入流复制到指定文件
     * @param inputStream 输入流
     * @param desFile 目标文件，只能是文件不能是文件夹路径
     * @param force true表示覆盖，false表示如果文件存在则直接返回
     *
     * @return 复制成功或者文件存在则返回true 其他false
     */
    @JvmStatic
    fun copyFileFromInputStream(inputStream: InputStream?, desFile: File?, force: Boolean = false): Boolean {
        if (inputStream == null || desFile == null) return false
        if (desFile.exists() && desFile.isFile && !force) return true
        if (!desFile.exists()) {
            desFile.parentFile?.let {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
        }
        if (desFile.isDirectory) {
            desFile.deleteRecursively()
        }
        try {
            inputStream.use {input->
                FileOutputStream(desFile).use { fs->
                    val buffer = ByteArray(8192)
                    var byteRead = -1
                    byteRead = input.read(buffer)
                    while (byteRead != -1) {
                        fs.write(buffer, 0, byteRead)
                        byteRead = input.read(buffer)
                    }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
            return false
        }

        return true


    }

    /**
     * 检查文件父目录，如果目录不存在则创建目录
     */
    @JvmStatic
    fun createFileParent(file: File?) {
        file?.apply {
            if (!exists()) {
                file.parentFile?.apply {
                    if (!exists()) {
                        mkdirs()
                    }
                }
            }
        }
    }

    /**
     * 文件复制
     * @param src 源文件
     * @param desFile 目标文件
     */
    @JvmStatic
    fun copyFile(src:File?,desFile: File?,force: Boolean = false){
        if (desFile==null)return
        if(src==null||!src.exists()||!src.isFile)return
        copyFileFromInputStream(FileInputStream(src),desFile,force)
    }

    /**
     * 文件夹复制 将源文件夹下的所有文件复制到目标文件夹下
     * @param src 源文件夹
     * @param desFile 目标文件夹
     */
    @JvmStatic
    fun copyFileDirectory(src:File?,desFile: File?){
        if (desFile==null||desFile.isFile)return
        if(src==null||!src.exists()||!src.isDirectory)return
        if(!desFile.exists()){
            desFile.mkdirs()
        }
        src.listFiles()?.forEach { file->
            if(file.isDirectory){
                copyFileDirectory(file,File(desFile,file.name))
            }else{
                copyFile(file,File(desFile,file.name))
            }
        }
    }
}