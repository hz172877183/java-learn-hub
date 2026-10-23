<!-- 笔记悬浮窗：可关闭，关闭时自动保存，按学习项加载。 -->
<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchNote, saveNote } from '@/api/client'

const props = defineProps<{ slug: string; title: string }>()

const OPEN_KEY = 'learnhub-note-open'
// 记住开合状态：用户在 A 学习项打开后，切到 B 仍然保持打开
const open = ref(localStorage.getItem(OPEN_KEY) === '1')
const content = ref('')
const status = ref('')
const loading = ref(false)
const saving = ref(false)
const dirty = ref(false)

async function load() {
  loading.value = true
  dirty.value = false
  try {
    const note = await fetchNote(props.slug)
    content.value = note.content
    status.value = note.updatedAt
      ? `上次保存：${note.updatedAt.replace('T', ' ').slice(0, 16)}`
      : '还没有笔记'
  } catch {
    content.value = ''
    status.value = ''
  } finally {
    loading.value = false
  }
}

async function persist(silent = false) {
  saving.value = true
  try {
    const saved = await saveNote(props.slug, content.value)
    dirty.value = false
    status.value = saved.updatedAt
      ? `已保存：${saved.updatedAt.replace('T', ' ').slice(0, 16)}`
      : '已清空'
    if (!silent) {
      ElMessage.success('笔记已保存')
    }
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    saving.value = false
  }
}

// 关闭时若有未保存内容，先静默保存再收起，避免丢笔记
async function close() {
  if (dirty.value) {
    await persist(true)
  }
  open.value = false
  localStorage.setItem(OPEN_KEY, '0')
}

function toggle() {
  open.value = !open.value
  localStorage.setItem(OPEN_KEY, open.value ? '1' : '0')
}

// 切换学习项时重新加载对应笔记，不清空面板开合状态
watch(() => props.slug, load, { immediate: true })
</script>

<template>
  <Teleport to="body">
    <button v-if="!open" class="note-fab" title="打开笔记" @click="toggle">
      <span class="icon">✎</span>
      记笔记
    </button>

    <aside v-else class="note-panel" aria-label="我的笔记">
      <header class="note-head">
        <div class="titles">
          <strong>我的笔记</strong>
          <span class="sub">{{ title }}</span>
        </div>
        <button class="close" title="收起（自动保存）" @click="close">✕</button>
      </header>

      <textarea
        v-model="content"
        :disabled="loading"
        class="note-input"
        placeholder="想到什么写什么，只有你能看到。支持 Markdown。"
        @input="dirty = true"
      />

      <footer class="note-foot">
        <span class="status" :class="{ dirty }">{{ dirty ? '未保存' : status }}</span>
        <el-button type="primary" size="small" :loading="saving" @click="persist()">保存</el-button>
      </footer>
    </aside>
  </Teleport>
</template>

<style scoped>
.note-fab {
  position: fixed;
  right: 22px;
  bottom: 22px;
  z-index: 1200;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  border-radius: 999px;
  background: var(--accent);
  color: #fff;
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(15, 110, 110, 0.28);
}
.note-fab:hover {
  filter: brightness(1.08);
}
.note-fab .icon {
  font-size: 15px;
}
.note-panel {
  position: fixed;
  right: 22px;
  top: 96px;
  bottom: 22px;
  width: 360px;
  z-index: 1200;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(28, 36, 48, 0.14);
}
.note-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}
.titles {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.titles strong {
  font-size: 14px;
}
.sub {
  font-size: 11px;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.close {
  border: none;
  background: transparent;
  color: var(--muted);
  font-size: 14px;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 6px;
}
.close:hover {
  background: var(--accent-soft);
  color: var(--accent);
}
.note-input {
  flex: 1;
  width: 100%;
  resize: none;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 10px 12px;
  font-family: inherit;
  font-size: 13px;
  line-height: 1.65;
  color: var(--ink);
  background: #fff;
  outline: none;
}
.note-input:focus {
  border-color: var(--accent);
}
.note-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.status {
  font-size: 11px;
  color: var(--muted);
}
.status.dirty {
  color: #b45f35;
}
@media (max-width: 700px) {
  .note-fab {
    right: 14px;
    bottom: calc(78px + env(safe-area-inset-bottom));
    padding: 9px 15px;
  }
  .note-panel {
    left: 0;
    right: 0;
    bottom: 0;
    top: auto;
    width: auto;
    max-height: 78vh;
    border-radius: 18px 18px 0 0;
    padding: 14px 14px calc(14px + env(safe-area-inset-bottom));
    box-shadow: 0 -8px 30px rgba(28, 36, 48, 0.18);
  }
  .note-input {
    min-height: 26vh;
  }
}
</style>
