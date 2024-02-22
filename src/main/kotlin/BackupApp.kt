package org.example

import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.Timer
import javax.swing.*
import kotlin.system.exitProcess


class BackupApp : JFrame("Backup App") {
    /**
     * 需要备份的文件、文件夹路径
     */
    private var pathField: JTextField? = null

    /**
     * 上传URL
     */
    private var uploadURL: String? = null

    /**
     * 日志区域
     */
    private var logArea: JTextArea? = null
    private lateinit var settings : Properties
    private var backupTimer: Timer? = null
    private var uploadTimer: Timer? = null

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(600, 400)
        setLocationRelativeTo(null)
        initComponents()
        // 可改变大小
        isResizable = true
        // 加载配置

        // 设置退出应用保存


        // 设置默认的关闭操作为不做任何事，因为我们想要自定义关闭操作
        defaultCloseOperation = DO_NOTHING_ON_CLOSE

        // 添加窗口监听器
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                // 在这里执行退出前的操作
                println("Executing custom operations before exit...")
                // 保存设置
                saveSettings()
                // 执行完操作后，可以安全退出程序
                exitProcess(0)
            }
        })
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

        log(FileUtil.getJarPath())
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
        settings = FileUtil.getConfigFile()
        val selectedPath = settings.getProperty("selectedPath")
        // 如果存在选中路径，则加载到输入框中
        if (selectedPath != null) {
            pathField!!.text = selectedPath
            scheduleBackupTask()
        }
        // 如果存在上传URL，则加载到输入框中
        uploadURL = settings.getProperty("uploadURL")
        if (uploadURL != null) {
            log("Upload URL set: $uploadURL")
        }
    }

    private fun saveSettings() {
        // Save settings to properties file...
        settings.setProperty("selectedPath", pathField!!.text)
        settings.setProperty("uploadURL", uploadURL.let { it ?: "" })
        FileUtil.updateConfigFile(settings)
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
            // 获取当前日期时间
            val currentDateTime = LocalDateTime.now()
            val firstFolder = "${currentDateTime.year}-${currentDateTime.monthValue}"
            val secondFolder = "${currentDateTime.dayOfMonth}"
            val fullZipFolderPath = srcPath.parent.resolve("backup").resolve(firstFolder).resolve(secondFolder)
            Files.createDirectories(fullZipFolderPath) // Ensure backup directory exists

            val timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm").format(currentDateTime)
            val zipFileName = "${FileUtil.getName(selectedPath)}-$timestamp.zip"
            val zipPath = fullZipFolderPath.resolve(zipFileName)

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
