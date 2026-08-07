package com.example.api

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

data class ToolchainInstallResult(
    val success: Boolean,
    val message: String
)

object ToolchainInstaller {

    suspend fun installFromZipUri(context: Context, zipUri: Uri): ToolchainInstallResult = withContext(Dispatchers.IO) {
        val manager = BundledToolchainManager(context)
        val stagingDir = File(context.cacheDir, "toolchain_staging")
        val destinationDir = manager.toolchainDir

        try {
            if (stagingDir.exists()) stagingDir.deleteRecursively()
            stagingDir.mkdirs()

            context.contentResolver.openInputStream(zipUri)?.use { inputStream ->
                unzipSecure(inputStream, stagingDir)
            } ?: return@withContext ToolchainInstallResult(false, "Unable to open the selected ZIP file.")

            val root = findToolchainRoot(stagingDir)
                ?: return@withContext ToolchainInstallResult(
                    false,
                    "The ZIP does not contain a valid toolchain layout. Expected bin/, lib/, and include/."
                )

            if (destinationDir.exists()) destinationDir.deleteRecursively()
            destinationDir.mkdirs()

            copyChildren(root, destinationDir)
            ensureExecutableBits(destinationDir)

            val verifiedManager = BundledToolchainManager(context)
            if (!verifiedManager.isToolchainBundled) {
                return@withContext ToolchainInstallResult(
                    false,
                    "Toolchain extracted, but validation failed. Please check that clang, ld.lld, lib, and include are present."
                )
            }

            ToolchainInstallResult(true, "Offline toolchain installed successfully.")
        } catch (e: Exception) {
            ToolchainInstallResult(false, "Toolchain install failed: ${e.message}")
        } finally {
            stagingDir.deleteRecursively()
        }
    }

    private fun unzipSecure(inputStream: InputStream, destDir: File) {
        val canonicalDestPath = destDir.canonicalPath + File.separator
        ZipInputStream(BufferedInputStream(inputStream)).use { zipInput ->
            var entry = zipInput.nextEntry
            while (entry != null) {
                val outFile = File(destDir, entry.name)
                val canonicalOutPath = outFile.canonicalPath
                if (!canonicalOutPath.startsWith(canonicalDestPath)) {
                    throw SecurityException("Blocked suspicious ZIP entry: ${entry.name}")
                }

                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { output ->
                        zipInput.copyTo(output)
                    }
                }
                zipInput.closeEntry()
                entry = zipInput.nextEntry
            }
        }
    }

    private fun findToolchainRoot(dir: File): File? {
        fun score(candidate: File): Int {
            if (!candidate.isDirectory) return 0
            var points = 0
            if (File(candidate, "bin/clang").isFile) points += 4
            if (File(candidate, "bin/ld.lld").isFile) points += 4
            if (File(candidate, "lib/libLLVM.so").isFile) points += 3
            if (File(candidate, "lib/libclang-cpp.so").isFile) points += 3
            if (File(candidate, "lib/libc++_shared.so").isFile) points += 2
            if (File(candidate, "include").isDirectory) points += 2
            if (File(candidate, "lib/clang").isDirectory) points += 2
            return points
        }

        val candidates = dir.walkTopDown()
            .filter { it.isDirectory }
            .map { it to score(it) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .toList()

        return candidates.firstOrNull()?.first
    }

    private fun copyChildren(sourceDir: File, destinationDir: File) {
        sourceDir.listFiles()?.forEach { child ->
            val target = File(destinationDir, child.name)
            if (child.isDirectory) {
                target.mkdirs()
                copyChildren(child, target)
            } else {
                target.parentFile?.mkdirs()
                child.inputStream().use { input ->
                    FileOutputStream(target).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    private fun ensureExecutableBits(toolchainDir: File) {
        val binDir = File(toolchainDir, "bin")
        if (!binDir.exists()) return

        binDir.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                file.setExecutable(true, true)
                file.setReadable(true, true)
            }
    }
}
