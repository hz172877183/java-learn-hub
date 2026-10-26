<!-- 笔记悬浮窗：可关闭，关闭时自动保存，按学习项加载。 -->
<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchNote, saveNote } from '@/api/client'

const props = defineProps<{ slug: string; title: string }>()

const OPEN_KEY = 'learnhub-note-open'
const POS_KEY = 'learnhub-note-pos'
// 记住开合状态：用户在 A 学习项打开后，切到 B 仍然保持打开
const open = ref(localStorage.getItem(OPEN_KEY) === '1')
const content = ref('')
const status = ref('')
const loading = ref(false)
const saving = ref(false)
const dirty = ref(false)

const panelRef = ref<HTMLElement | null>(null)
const position = ref<{ x: number; y: number } | null>(readPosition())
const size = ref<{ w: number; h: number } | null>(null)
const dragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })
const viewportWidth = ref(window.innerWidth)

const isMobile = computed(() => viewportWidth.value <= 700)
const panelStyle = computed(() =>
  !isMobile.value && position.value
    ? {
        left: `${position.value.x}px`,
        top: `${position.value.y}px`,
        right: 'auto',
        bottom: 'auto',
        width: `${size.value?.w ?? 360}px`,
        height: `${size.value?.h ?? Math.min(620, window.innerHeight * 0.7)}px`,
      }
    : {},
)

function readPosition() {
  try {
    const raw = localStorage.getItem(POS_KEY)
    if (!raw) return null
    const value = JSON.parse(raw) as { x: number; y: number }
    if (typeof value.x !== 'number' || typeof value.y !== 'number') return null
    return value
  } catch {
    return null
  }
}

/** 拖拽面板：按住空白区域即可拖动，输入框、按钮、链接不触发。 */
function startDragFromPanel(event: PointerEvent) {
  const target = event.target as HTMLElement
  if (target.closest('textarea, input, button, a, select, [contenteditable="true"]')) {
    return
  }
  startDrag(event)
}

/** 拖拽移动面板；移动端保持底部抽屉，不启用拖拽。 */
function startDrag(event: PointerEvent) {
  if (isMobile.value || !panelRef.value) return
  if (event.pointerType === 'mouse' && event.button !== 0) return
  const rect = panelRef.value.getBoundingClientRect()
  size.value = { w: rect.width, h: rect.height }
  dragOffset.value = { x: event.clientX - rect.left, y: event.clientY - rect.top }
  dragging.value = true
  window.addEventListener('pointermove', onDrag)
  window.addEventListener('pointerup', endDrag, { once: true })
  event.preventDefault()
}

function onDrag(event: PointerEvent) {
  if (!dragging.value || !panelRef.value) return
  const width = panelRef.value.offsetWidth
  const height = panelRef.value.offsetHeight
  position.value = {
    x: Math.min(Math.max(0, event.clientX - dragOffset.value.x), window.innerWidth - width),
    y: Math.min(Math.max(0, event.clientY - dragOffset.value.y), window.innerHeight - height),
  }
}

function endDrag() {
  dragging.value = false
  window.removeEventListener('pointermove', onDrag)
  if (position.value) {
    localStorage.setItem(POS_KEY, JSON.stringify(position.value))
  }
}

/** 双击标题栏恢复默认位置。 */
function resetPosition() {
  position.value = null
  size.value = null
  localStorage.removeItem(POS_KEY)
}

function clampToViewport() {
  if (!position.value || !panelRef.value) return
  const width = panelRef.value.offsetWidth
  const height = panelRef.value.offsetHeight
  position.value = {
    x: Math.max(0, Math.min(position.value.x, window.innerWidth - width)),
    y: Math.max(0, Math.min(position.value.y, window.innerHeight - height)),
  }
}

function onResize() {
  viewportWidth.value = window.innerWidth
  clampToViewport()
}

onMounted(() => {
  window.addEventListener('resize', onResize)
  clampToViewport()
})

watch(open, async (value) => {
  if (value) {
    await nextTick()
    clampToViewport()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('pointermove', onDrag)
})

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

/** 供父组件（学习状态卡片里的按钮）打开面板。 */
async function openPanel() {
  open.value = true
  localStorage.setItem(OPEN_KEY, '1')
  await nextTick()
  clampToViewport()
}

defineExpose({ openPanel })

// 切换学习项时重新加载对应笔记，不清空面板开合状态
watch(() => props.slug, load, { immediate: true })
</script>

<template>
  <Teleport to="body">
    <button v-if="!open" class="note-fab" title="打开笔记" @click="toggle">
      <span class="icon">✎</span>
      记笔记
    </button>

    <aside
      v-else
      ref="panelRef"
      class="note-panel"
      :class="{ dragging }"
      :style="panelStyle"
      aria-label="我的笔记"
      @pointerdown="startDragFromPanel"
    >
      <header class="note-head" title="按住空白处拖动，双击复位" @dblclick="resetPosition">
        <div class="titles">
          <strong><span class="grip" aria-hidden="true">⠿</span> 我的笔记</strong>
          <span class="sub">{{ title }}</span>
        </div>
        <button class="close" title="收起（自动保存）" @pointerdown.stop @click="close">✕</button>
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
  gap: 7px;
  padding: 12px 18px;
  border: none;
  border-radius: 999px;
  background: var(--accent);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(15, 110, 110, 0.38);
}
.note-fab:hover {
  filter: brightness(1.08);
  transform: translateY(-1px);
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
  border-top: 3px solid var(--accent);
  border-radius: 14px;
  box-shadow: 0 14px 40px rgba(28, 36, 48, 0.22);
  cursor: move;
}
.note-panel.dragging {
  cursor: grabbing;
  user-select: none;
}
.note-panel.dragging * {
  cursor: grabbing;
}
.note-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  cursor: move;
  user-select: none;
  touch-action: none;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--line);
}
.grip {
  color: var(--accent);
  letter-spacing: -2px;
  margin-right: 6px;
  font-size: 13px;
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
    right: 16px;
    bottom: calc(110px + env(safe-area-inset-bottom));
    padding: 10px 16px;
    font-size: 13px;
  }
  .note-head {
    cursor: default;
    touch-action: auto;
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
    cursor: default;
  }
  .note-input {
    min-height: 26vh;
  }
}
</style>
