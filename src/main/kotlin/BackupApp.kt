package org.example

import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.Timer
import javax.swing.*


class BackupApp : JFrame("Backup App") {
    private var pathField: JTextField? = null
    private var logArea: JTextArea? = null
    private val settings = Properties()
    private var backupTimer : Timer? = null
    private var uploadTimer : Timer? = null

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(600, 400)
        setLocationRelativeTo(null)
        initComponents()
        // 可改变大小
        isResizable = true
    }

    private fun initComponents() {
        // Path field
        pathField = JTextField()
        pathField!!.isEditable = false

        // Log area
        logArea = JTextArea()
        logArea!!.isEditable = false
        val logScrollPane = JScrollPane(logArea)

        // Button to choose file/directory
        val chooseButton = JButton("Choose File/Directory")
        chooseButton.addActionListener { e: ActionEvent? -> chooseFile() }

        // Button to set upload URL
        val urlButton = JButton("Set Upload URL")
        urlButton.addActionListener { e: ActionEvent? -> setInputURL() }

        // Layout
        layout = BorderLayout()
        add(pathField, BorderLayout.NORTH)
        add(logScrollPane, BorderLayout.CENTER)
        val buttonPanel = JPanel()
        buttonPanel.add(chooseButton)
        buttonPanel.add(urlButton)
        add(buttonPanel, BorderLayout.SOUTH)

        // Load settings
        loadSettings()
    }

    private fun chooseFile() {
        val chooser = JFileChooser()
        chooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
        val result = chooser.showOpenDialog(this)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedPath = chooser.selectedFile.absolutePath
            pathField!!.text = selectedPath
            log("Selected path: $selectedPath")
            scheduleBackupTask()
            // Save selected path to settings...
        }
    }

    private fun setInputURL() {
        val url = JOptionPane.showInputDialog(this, "Enter Upload URL:")
        if (url != null && url.isNotEmpty()) {
            // Save URL to settings...
            log("Upload URL set: $url")
        }
    }

    private fun log(message: String) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss ").format(Date())
        logArea!!.append(time + message + "\n")
    }

    private fun loadSettings() {
        // Load settings from properties file...
    }

    private fun saveSettings() {
        // Save settings to properties file...
    }

    /**
     * 计划备份任务
     * 首次执行5秒钟后，然后每隔10分钟执行一次
     */
    private fun scheduleBackupTask() {
        cancelBackupTask()
        backupTimer = Timer()
        TimerUtil.startTimer(backupTimer!!, 5 * 1000, 10 * 60 * 1000) {
            backupSelectedPath()
        }
        log("Backup task scheduled.")
    }

    /**
     * 取消备份任务
     */
    private fun cancelBackupTask() {
        if (backupTimer != null) {
            TimerUtil.stopTimer(backupTimer!!)
            log("Backup task cancelled.")
        }
    }

    private fun backupSelectedPath() {
        val selectedPath = pathField?.text ?: return
        if (selectedPath.isEmpty()) {
            log("No path selected for backup.")
            return
        }

        try {
            val srcPath = Paths.get(selectedPath)
            if (!Files.exists(srcPath)) {
                log("Path does not exist: $selectedPath")
                cancelBackupTask()
                return
            }
            val backupDir = srcPath.parent.resolve("backup")
            Files.createDirectories(backupDir) // Ensure backup directory exists

            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
            val zipFileName = "${FileUtil.getName(selectedPath)}-$timestamp.zip"
            val zipPath = backupDir.resolve(zipFileName)

            FileUtil.zipFileOrFolder(selectedPath, zipPath.toString())
            log("Backup created: $zipPath")
        } catch (e: Exception) {
            log("Error creating backup: ${e.message}")
        }
    }

    companion object {
        // Add window listener to save settings on close
        // Add timer tasks for backup and upload
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater { BackupApp().isVisible = true }
        }
    }
}
