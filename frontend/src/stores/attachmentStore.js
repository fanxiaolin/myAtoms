import { ref } from 'vue'

export const pendingFiles = ref([])

const MAX_ATTACHMENT_FILES = 100
const IGNORED_DIRECTORIES = new Set([
  'node_modules', '.git', 'dist', 'build', 'target', '.next', '.nuxt', 'coverage'
])

export const addPendingFiles = (files) => {
  let added = 0
  let ignored = 0
  const existing = new Set(pendingFiles.value.map(item => `${item.path}:${item.file.size}:${item.file.lastModified}`))

  for (const file of files) {
    const path = file.webkitRelativePath || file.name
    if (path.split('/').some(segment => IGNORED_DIRECTORIES.has(segment))
        || pendingFiles.value.length >= MAX_ATTACHMENT_FILES) {
      ignored++
      continue
    }
    const key = `${path}:${file.size}:${file.lastModified}`
    if (existing.has(key)) continue
    pendingFiles.value.push({ file, path })
    existing.add(key)
    added++
  }

  return { added, ignored }
}

export const removePendingFile = (index) => {
  pendingFiles.value.splice(index, 1)
}
