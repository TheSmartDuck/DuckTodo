<template>
  <el-drawer
    :title="childTask && (childTask.name || '子任务')"
    :visible.sync="visible"
    direction="rtl"
    :size="drawerSize"
    custom-class="child-drawer"
    :before-close="handleBeforeClose"
    :append-to-body="true"
  >
    <div class="child-drawer-body">
      <div class="child-drawer-tags" v-if="childTask">
        <el-tag size="small" type="info">指派人员：{{ displayUserNameById(childTask.assigneeId) || '未指派' }}</el-tag>
        <el-tag size="small" :type="isChildOverdue(childTask) ? 'danger' : 'info'">截止日期：{{ formatTime(childTask.dueDate) || '-' }}</el-tag>
        <el-tag size="small" :type="statusTagType(childTask.status)" :class="statusTagClass(childTask.status)">子任务状态：{{ formatStatus(childTask.status) }}</el-tag>
      </div>
      <el-divider class="child-divider"></el-divider>
      <div class="chain-box" v-if="childTask">
        <div class="chain-title"><i class="el-icon-link chain-title-icon"></i>子任务链</div>
        <el-timeline v-if="Array.isArray(childTask.processChains)" class="process-chain-timeline">
          <el-timeline-item v-for="(chain, index) in childTask.processChains" :key="chain.childTaskProcessChainId || index">
            <el-card class="chain-card">
              <div class="chain-name">
                <el-input v-model="chain.childTaskProcessChainName" size="mini" placeholder="链名称" />
                <div class="chain-name-actions">
                  <el-button type="text" :icon="isChainChanged(chain) || isChainNew(chain) ? 'el-icon-check' : 'el-icon-info'" size="mini" @click="saveProcessChain(chain)" :disabled="!isChainChanged(chain) && !isChainNew(chain)">{{ chainSaveLabel(chain) }}</el-button>
                </div>
              </div>
              <el-divider class="chain-item-divider"></el-divider>
              <div class="chain-note">
                <el-input type="textarea" v-model="chain.childTaskProcessChainNote" size="mini" :autosize="{ minRows: 2, maxRows: 6 }" placeholder="链描述" style="font-size: 10px;"/>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-button class="add-chain-btn" type="primary" size="mini" @click="addProcessChain">新增链</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script>
import { getChildTaskProcessChainList, addChildTaskProcessChain, updateChildTaskProcessChain } from '@/api/childtaskprocesschain-api'

const statusDict = {
  '0': '已取消',
  '1': '未开始',
  '2': '进行中',
  '3': '已完成',
  '4': '已延迟',
  '5': '暂停中'
}

function formatStatus (val) {
  const v = String(val)
  return statusDict[v] || '未知'
}

export default {
  name: 'ChildTaskDrawer',
  props: {
    childTask: { type: Object, default: null },
    helpers: { type: Array, default: () => [] },
    drawerSize: { type: String, default: '40%' }
  },
  data () {
    return {
      visible: false,
      statusDict,
      chainOriginalMap: {},
      newProcessChain: {
        childTaskId: '',
        childTaskProcessChainName: '',
        childTaskProcessChainNote: ''
      }
    }
  },
  watch: {
    childTask: {
      handler (val) {
        if (val) {
          if (!Array.isArray(val.processChains)) {
            this.$set(val, 'processChains', [])
          }
          if (val.childTaskId) {
            this.fetchProcessChains(String(val.childTaskId))
          }
        }
      },
      immediate: true
    }
  },
  methods: {
    formatStatus,
    open () {
      this.visible = true
    },
    close () {
      this.visible = false
    },
    handleBeforeClose (done) {
      this.$confirm('确认关闭子任务详情？', '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
        .then(() => { 
          this.visible = false
          if (done) done() 
        })
        .catch(() => {})
    },
    displayUserNameById (uid) {
      if (!uid) return ''
      const u = this.helpers.find(h => String(h.userId || h.userID || h.id) === String(uid))
      return u ? (u.userName || u.name) : uid
    },
    formatTime (v) {
      if (!v) return ''
      return String(v).slice(0, 10)
    },
    isChildOverdue (ct) {
      if (!ct.dueDate) return false
      const due = String(ct.dueDate).slice(0, 10)
      const now = new Date()
      const today = now.toISOString().slice(0, 10)
      return due < today && String(ct.status) !== '3'
    },
    statusTagType (status) {
      const s = String(status)
      if (s === '3') return 'success'
      if (s === '2') return 'primary'
      return 'info'
    },
    statusTagClass (status) {
      return 'st-' + String(status || '1')
    },
    
    // Chain methods
    async fetchProcessChains (childTaskId) {
      try {
        const res = await getChildTaskProcessChainList(childTaskId)
        const list = Array.isArray(res && res.list) ? res.list : (Array.isArray(res && res.data) ? res.data : (Array.isArray(res) ? res : []))
        const normalized = (Array.isArray(list) ? list : []).map(it => ({ ...it, _lid: String(it.childTaskProcessChainId || '') }))
        this.$set(this.childTask, 'processChains', normalized)
        this.buildChainOriginalSnapshot()
      } catch (e) {
        this.$set(this.childTask, 'processChains', [])
      }
    },
    buildChainOriginalSnapshot () {
      const list = Array.isArray(this.childTask && this.childTask.processChains) ? this.childTask.processChains : []
      const map = {}
      list.forEach(it => {
        const key = String(it._lid || it.childTaskProcessChainId || '')
        if (!key) return
        map[key] = {
          childTaskProcessChainName: String(it.childTaskProcessChainName || ''),
          childTaskProcessChainNote: String(it.childTaskProcessChainNote || '')
        }
      })
      this.chainOriginalMap = map
    },
    chainKey (chain) {
      return String(chain._lid || chain.childTaskProcessChainId || '')
    },
    isChainNew (chain) {
      return !chain.childTaskProcessChainId
    },
    isChainChanged (chain) {
      if (this.isChainNew(chain)) return true
      const key = this.chainKey(chain)
      const origin = this.chainOriginalMap[key]
      if (!origin) return false
      return (chain.childTaskProcessChainName !== origin.childTaskProcessChainName) ||
             (chain.childTaskProcessChainNote !== origin.childTaskProcessChainNote)
    },
    chainSaveLabel (chain) {
      if (this.isChainNew(chain)) return '新增'
      if (this.isChainChanged(chain)) return '保存'
      return '已保存'
    },
    async saveProcessChain (chain) {
      try {
        const hasId = String(chain && chain.childTaskProcessChainId || '')
        if (hasId) {
          const resp = await updateChildTaskProcessChain(chain)
          const updated = this.extractChainFromResp(resp) || chain
          this.mergeSavedChain(chain, updated)
        } else {
          const cid = this.childTask && this.childTask.childTaskId
          chain.childTaskId = String(cid || '')
          const resp = await addChildTaskProcessChain(chain)
          const created = this.extractChainFromResp(resp) || chain
          this.mergeSavedChain(chain, created)
        }
        this.$message.success('链保存成功')
      } catch (e) {
        this.$message.error('链保存失败')
      }
    },
    extractChainFromResp (resp) {
      if (resp && resp.data) return resp.data
      if (resp && resp.childTaskProcessChainId) return resp
      return null
    },
    mergeSavedChain (chain, saved) {
      if (!saved) return
      Object.assign(chain, saved)
      chain._lid = String(saved.childTaskProcessChainId)
      this.buildChainOriginalSnapshot()
    },
    addProcessChain () {
      const cid = this.childTask && this.childTask.childTaskId
      this.newProcessChain.childTaskId = String(cid || '')
      const list = Array.isArray(this.childTask.processChains) ? this.childTask.processChains.slice() : []
      list.push({
        childTaskId: this.newProcessChain.childTaskId,
        childTaskProcessChainName: '',
        childTaskProcessChainNote: '',
        _lid: String(Date.now())
      })
      this.$set(this.childTask, 'processChains', list)
      this.buildChainOriginalSnapshot()
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/task-group-detail/child-task-drawer.css';
</style>
