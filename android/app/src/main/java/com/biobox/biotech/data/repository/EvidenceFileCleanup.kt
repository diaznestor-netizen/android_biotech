package com.biobox.biotech.data.repository

import java.io.File

internal fun deleteSyncedEvidence(file: File): Boolean {
    val canonical = runCatching { file.canonicalFile }.getOrNull() ?: return false
    val evidenceDir = canonical.parentFile ?: return false
    if (evidenceDir.name != "evidence" || evidenceDir.parentFile?.name != "files") return false
    return !canonical.exists() || canonical.delete()
}
