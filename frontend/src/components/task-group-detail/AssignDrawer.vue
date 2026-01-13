<template>
  <el-drawer
    title="PERSONNEL SELECTION / 选择干员"
    :visible.sync="visibleInternal"
    direction="rtl"
    size="500px"
    custom-class="rhine-drawer"
    :append-to-body="true"
    @open="onOpen"
  >
    <div class="rhine-drawer-body">

      <!-- Selected Users Area -->
      <div class="selected-users-area">
        <div class="area-label">SELECTED: {{ selectedUsers.length }}</div>
        <div class="selected-users-tags" v-if="selectedUsers && selectedUsers.length">
          <div
            v-for="item in selectedUsers"
            :key="item.userId"
            class="rhine-user-tag"
          >
            <div class="tag-avatar">
              <img :src="item.userAvatar || defaultAvatar" class="avatar-img" />
            </div>
            <span class="tag-name" :title="item.userName">{{ item.userName }}</span>
            <div 
              class="tag-close" 
              v-if="String(item.userId) !== String(currentUserId)"
              @click="removeSelectedUser(item.userId)"
            >×</div>
          </div>
        </div>
        <div v-else class="empty-selection">
          NO PERSONNEL SELECTED
        </div>
      </div>

      <!-- User List -->
      <div class="drawer-list-wrapper" v-loading="loading">
        <el-table
          ref="assignTable"
          :data="filteredUserList"
          size="small"
          row-key="userId"
          :reserve-selection="true"
          @selection-change="onSelectionChange"
          height="100%"
          class="rhine-table"
          :header-cell-style="{ background: '#1a1a1a', color: '#E65D25', borderColor: '#333' }"
          :cell-style="{ background: '#222', color: '#ccc', borderColor: '#333' }"
        >
          <el-table-column type="selection" width="40" :selectable="selectionRowSelectable" />
          <el-table-column label="OPERATOR" min-width="200">
            <template slot-scope="scope">
              <div class="user-cell">
                <div class="cell-avatar">
                   <img :src="scope.row.userAvatar || defaultAvatar" />
                </div>
                <div class="cell-info">
                  <div class="name">{{ scope.row.userName }}</div>
                  <div class="uid">ID: {{ scope.row.userId }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="!filteredUserList.length" class="empty-muted">NO DATA FOUND</div>
      </div>

      <!-- Footer Actions -->
      <div class="drawer-footer">
        <div class="rhine-btn secondary" @click="closeDrawer">CANCEL // <span style="font-weight: 800;">取消选择</span></div>
        <div class="rhine-btn primary" @click="confirmDrawer">CONFIRM // <span style="font-weight: 800;">确认执行</span></div>
      </div>
    </div>
  </el-drawer>
</template>

<script>
import { getTaskGroupMembers } from '@/api/taskgroup-api'
import { getMe } from '@/api/user-api'
import defaultAvatar from '@/assets/imgs/default-user-avatar.png'
import '@/assets/css/task-group-detail/assign-drawer.css'

function mapToUserStruct(u) {
  if (!u) return null
  return {
    userId: u.userId || u.id,
    userName: u.userName || u.name || u.username || ('User' + (u.userId || u.id)),
    userAvatar: u.userAvatar || u.avatar || ''
  }
}

export default {
  name: 'AssignDrawer',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    currentUserId: {
      type: [String, Number],
      default: ''
    },
    initialSelectedIds: {
      type: Array,
      default: () => []
    },
    taskGroupId: {
      type: [String, Number],
      default: ''
    }
  },
  data () {
    return {
      loading: false,
      searchKeyword: '',
      allUserList: [], // All members fetched
      selectedUsers: [], // Currently selected user objects
      defaultAvatar
    }
  },
  computed: {
    visibleInternal: {
      get () { return this.visible },
      set (val) { this.$emit('update:visible', val) }
    },
    filteredUserList () {
      if (!this.searchKeyword) return this.allUserList
      const k = this.searchKeyword.toLowerCase()
      return this.allUserList.filter(u => 
        (u.userName && u.userName.toLowerCase().includes(k)) ||
        (String(u.userId).includes(k))
      )
    }
  },
  watch: {
    visible (val) {
      if (val) {
        this.initData()
      }
    }
  },
  methods: {
    async initData () {
      this.searchKeyword = ''
      this.selectedUsers = []
      await this.loadUsers()
    },
    onOpen () {
       this.$nextTick(() => {
         this.syncTableSelection()
       })
    },
    async loadUsers () {
      this.loading = true
      this.allUserList = []
      
      try {
        const gid = String(this.taskGroupId || '').trim()
        let rawList = []
        
        if (gid) {
          // Task Group Mode
          const res = await getTaskGroupMembers(gid)
          if (Array.isArray(res)) {
             rawList = res
          } else if (res && Array.isArray(res.data)) {
             rawList = res.data
          } else if (res && (res.list || res.records)) {
             rawList = res.list || res.records
          }
        } else {
          // Private Mode: Only Me
          let me = null
          try {
             const res = await getMe()
             me = res.data || res
          } catch (e) {
             console.error(e)
          }
          if (me) rawList = [me]
        }
        
        // Map to standard struct
        this.allUserList = rawList.map(mapToUserStruct)
        
        // Ensure current user is in the list if not present (optional, but good for safety)
        if (this.currentUserId && !this.allUserList.some(u => String(u.userId) === String(this.currentUserId))) {
           // fetch me and add? Or just ignore. 
           // Usually getTaskGroupMembers should include me if I am in the group.
        }

        // Sync initial selection
        this.$nextTick(() => {
          this.syncTableSelection()
        })
      } catch (e) {
        this.$message.error('Failed to load personnel list')
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    syncTableSelection () {
       if (!this.$refs.assignTable) return
       
       // Clear selection first? No, we want to set it based on initialSelectedIds and current selectedUsers
       this.$refs.assignTable.clearSelection()
       
       // Determine which IDs should be selected
       // Combine initialSelectedIds and any already in selectedUsers (if re-opening?)
       // Actually initData resets selectedUsers, so we rely on initialSelectedIds primarily on open.
       
       const targetIds = new Set(this.initialSelectedIds.map(String))
       
       // Also add current user if required? 
       // The parent TaskCreate handles "Me" logic, so we just respect initialSelectedIds.
       
       this.allUserList.forEach(row => {
         if (targetIds.has(String(row.userId))) {
            this.$refs.assignTable.toggleRowSelection(row, true)
            // Push to selectedUsers if empty (first load)
            if (!this.selectedUsers.some(u => String(u.userId) === String(row.userId))) {
               this.selectedUsers.push(row)
            }
         }
       })
    },
    onSearchInput () {
      // Filter is computed, no need to reload
    },
    onSelectionChange (selection) {
      // This gives the selection of the *current filtered list* + reserved selections?
      // element-ui table with reserve-selection handles ID based persistence across pages/filters usually?
      // But we are not using pagination anymore (client side filtering).
      
      // We just need to sync the state.
      // However, simpler approach: selection IS the list of selected users.
      this.selectedUsers = selection
    },
    removeSelectedUser (uid) {
      // Remove from selectedUsers
      const idx = this.selectedUsers.findIndex(u => String(u.userId) === String(uid))
      if (idx > -1) {
        // We need to uncheck it in the table
        const row = this.allUserList.find(u => String(u.userId) === String(uid))
        if (row && this.$refs.assignTable) {
          this.$refs.assignTable.toggleRowSelection(row, false)
        }
        // selection-change will fire and update selectedUsers
      }
    },
    selectionRowSelectable (row) {
      if (this.currentUserId && String(row.userId) === String(this.currentUserId)) {
        return false
      }
      return true
    },
    closeDrawer () {
      this.visibleInternal = false
    },
    confirmDrawer () {
      this.$emit('confirm', this.selectedUsers)
      this.visibleInternal = false
    }
  }
}
</script>
