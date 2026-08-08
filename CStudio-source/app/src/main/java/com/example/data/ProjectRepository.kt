package com.example.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.BufferedReader
import java.io.InputStreamReader

class ProjectRepository(private val context: Context) {
    private val settings = SettingsRepository(context)

    private fun getWorkspaceRoot(): DocumentFile? {
        val uriStr = settings.workspaceUri ?: return null
        return try {
            val uri = Uri.parse(uriStr)
            DocumentFile.fromTreeUri(context, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getProjectsFolder(): DocumentFile? {
        val root = getWorkspaceRoot() ?: return null
        var projectsDir = root.findFile("Projects")
        if (projectsDir == null) {
            projectsDir = root.createDirectory("Projects")
        }
        return projectsDir
    }

    init {
        // We cannot create default project automatically if workspace is not set.
        // It's handled gracefully.
    }

    fun getProjects(): List<WorkspaceProject> {
        val projectsFolder = getProjectsFolder() ?: return emptyList()
        return projectsFolder.listFiles()
            .filter { it.isDirectory }
            .map { WorkspaceProject(it.name ?: "Unknown", it.lastModified()) }
            .sortedByDescending { it.lastModified }
    }

    fun createProject(name: String): WorkspaceProject? {
        val projectsFolder = getProjectsFolder() ?: return null
        val dir = projectsFolder.createDirectory(name) ?: return null
        return WorkspaceProject(dir.name ?: name, dir.lastModified())
    }

    fun renameProject(oldName: String, newName: String) {
        val projectsFolder = getProjectsFolder() ?: return
        val oldDir = projectsFolder.findFile(oldName)
        if (oldDir != null && projectsFolder.findFile(newName) == null) {
            oldDir.renameTo(newName)
        }
    }

    fun deleteProject(name: String) {
        val projectsFolder = getProjectsFolder() ?: return
        projectsFolder.findFile(name)?.delete()
    }

    suspend fun duplicateProject(name: String) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val projectsFolder = getProjectsFolder() ?: return@withContext
        val srcDir = projectsFolder.findFile(name) ?: return@withContext
        val newName = "${name}_copy"
        if (projectsFolder.findFile(newName) != null) return@withContext
        val destDir = projectsFolder.createDirectory(newName) ?: return@withContext
        
        fun copyDirectory(source: androidx.documentfile.provider.DocumentFile, destination: androidx.documentfile.provider.DocumentFile) {
            source.listFiles().forEach { file ->
                if (file.isDirectory) {
                    val newDir = destination.createDirectory(file.name ?: "unnamed")
                    if (newDir != null) {
                        copyDirectory(file, newDir)
                    }
                } else if (file.isFile) {
                    val newFile = destination.createFile(file.type ?: "text/plain", file.name ?: "unnamed")
                    if (newFile != null) {
                        try {
                            context.contentResolver.openInputStream(file.uri)?.use { input ->
                                context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                                    input.copyTo(output)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        
        copyDirectory(srcDir, destDir)
    }

    fun getProjectFiles(projectName: String): List<WorkspaceFile> {
        val projectsFolder = getProjectsFolder() ?: return emptyList()
        val projDir = projectsFolder.findFile(projectName) ?: return emptyList()
        return projDir.listFiles()
            .filter { it.isFile }
            .map { WorkspaceFile(it.name ?: "Unknown", projectName, it.lastModified()) }
            .sortedBy { it.name }
    }

    suspend fun createFile(projectName: String, fileName: String, content: String = ""): WorkspaceFile? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        var finalName = fileName
        if (!finalName.contains(".")) {
            finalName += ".c"
        }
        val projectsFolder = getProjectsFolder() ?: return@withContext null
        var projDir = projectsFolder.findFile(projectName)
        if (projDir == null) {
            projDir = projectsFolder.createDirectory(projectName) ?: return@withContext null
        }
        if (projDir.findFile(finalName) != null) return@withContext null
        
        val file = projDir.createFile("text/plain", finalName) ?: return@withContext null
        if (file.name != null && file.name != finalName) {
            file.renameTo(finalName)
        }
        
        try {
            context.contentResolver.openOutputStream(file.uri)?.use { output ->
                output.write(content.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return@withContext WorkspaceFile(finalName, projectName, file.lastModified())
    }

    suspend fun readFile(projectName: String, fileName: String): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val projectsFolder = getProjectsFolder() ?: return@withContext ""
        val projDir = projectsFolder.findFile(projectName) ?: return@withContext ""
        val file = projDir.findFile(fileName) ?: return@withContext ""
        
        return@withContext try {
            context.contentResolver.openInputStream(file.uri)?.use { input ->
                BufferedReader(InputStreamReader(input)).readText()
            } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun writeFile(projectName: String, fileName: String, content: String) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val projectsFolder = getProjectsFolder() ?: return@withContext
        val projDir = projectsFolder.findFile(projectName) ?: return@withContext
        val file = projDir.findFile(fileName) ?: return@withContext
        
        try {
            context.contentResolver.openOutputStream(file.uri, "wt")?.use { output ->
                output.write(content.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun renameFile(projectName: String, oldName: String, newName: String) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        var finalNewName = newName
        if (!finalNewName.contains(".")) {
            finalNewName += ".c"
        }
        val projectsFolder = getProjectsFolder() ?: return@withContext
        val projDir = projectsFolder.findFile(projectName) ?: return@withContext
        val file = projDir.findFile(oldName) ?: return@withContext
        if (projDir.findFile(finalNewName) == null) {
            file.renameTo(finalNewName)
        }
    }

    suspend fun deleteFile(projectName: String, fileName: String) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val projectsFolder = getProjectsFolder() ?: return@withContext
        val projDir = projectsFolder.findFile(projectName) ?: return@withContext
        projDir.findFile(fileName)?.delete()
    }
}
