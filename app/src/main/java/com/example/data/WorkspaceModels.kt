package com.example.data

data class WorkspaceProject(
    val name: String,
    val lastModified: Long
)

data class WorkspaceFile(
    val name: String,
    val projectName: String,
    val lastModified: Long
)
