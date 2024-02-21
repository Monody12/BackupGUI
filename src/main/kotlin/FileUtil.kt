package org.example

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object FileUtil {
    /**
     * 检测输入路径是否是一个文件夹
     */
    fun isDirectory(path: String): Boolean {
        return Files.isDirectory(Paths.get(path))
    }

    /**
     * 检测输入路径是否是一个文件
     */
    fun isFile(path: String): Boolean {
        return Files.isRegularFile(Paths.get(path))
    }

    /**
     * 检测输入路径是否存在
     */
    fun exists(path: String): Boolean {
        return Files.exists(Paths.get(path))
    }

    /**
     * 获取存在路径的文件名或文件夹名称
     */
    fun getName(path: String): String {
        return Paths.get(path).fileName.toString()
    }

    /**
     * 将输入的文件路径打包成zip
     */
    fun zipFileOrFolder(srcPath: String, zipPath: String) {
        val sourceFile = File(srcPath)
        FileOutputStream(zipPath).use { fos ->
            ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
                if (sourceFile.isDirectory) {
                    // 传入空字符串作为基础路径，确保相对路径正确处理
                    zipFolder(zos, sourceFile, "")
                } else {
                    zipFile(zos, sourceFile, "")
                }
            }
        }
    }

    private fun zipFolder(zipOut: ZipOutputStream, folder: File, baseName: String) {
        val basePrefix = if (baseName.isEmpty()) "" else "$baseName/"
        folder.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                // 为子文件夹创建一个条目，并递归地调用zipFolder
                val entry = ZipEntry("$basePrefix${file.name}/")
                entry.time = file.lastModified() // 设置文件夹的修改时间
                zipOut.putNextEntry(entry)
                zipOut.closeEntry()
                zipFolder(zipOut, file, "$basePrefix${file.name}")
            } else {
                zipFile(zipOut, file, basePrefix.trimEnd('/'))
            }
        }
    }

    private fun zipFile(zipOut: ZipOutputStream, file: File, baseName: String) {
        FileInputStream(file).use { fi ->
            val zipEntry = ZipEntry(if (baseName.isEmpty()) file.name else "$baseName/${file.name}")
            zipEntry.time = file.lastModified() // 设置zipEntry的修改时间为文件的最后修改时间
            zipOut.putNextEntry(zipEntry)
            fi.copyTo(zipOut)
            zipOut.closeEntry()
        }
    }



}