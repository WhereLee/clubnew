<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.clubId" placeholder="社团ID" style="width:160px" clearable />
      <el-select v-model="query.status" placeholder="状态" style="width:150px" clearable>
        <el-option label="未开始" value="NOT_STARTED" />
        <el-option label="进行中" value="IN_PROGRESS" />
        <el-option label="已结束" value="ENDED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>
    <el-table :data="tableData" stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="quota" label="名额" width="80" />
      <el-table-column prop="appliedCount" label="已报名" width="90" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开始时间" width="170">
        <template #default="{ row }">{{ fmt(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="结束时间" width="170">
        <template #default="{ row }">{{ fmt(row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="viewRecords(row)">报名记录</el-button>
          <el-button size="small" type="danger" @click="handleCancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
      layout="total, prev, pager, next" @current-change="loadData" />

    <el-dialog v-model="recordVisible" title="报名记录">
      <el-table :data="records" stripe>
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="报名时间" width="180">
          <template #default="{ row }">{{ fmt(row.applyTime) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const query = reactive({ pageNum: 1, pageSize: 10, clubId: '', status: '' })
const tableData = ref([])
const total = ref(0)
const recordVisible = ref(false)
const records = ref([])

const statusText = (s: string) => ({ NOT_STARTED: '未开始', IN_PROGRESS: '进行中', ENDED: '已结束', CANCELLED: '已取消' } as any)[s] || s
const statusType = (s: string) => ({ NOT_STARTED: 'info', IN_PROGRESS: 'success', ENDED: 'warning', CANCELLED: 'danger' } as any)[s] || 'info'
const fmt = (t: string) => t ? String(t).replace('T', ' ').slice(0, 16) : ''

const loadData = async () => {
  const res: any = await request.get('/recruit/list', { params: query })
  tableData.value = res.data.records
  total.value = res.data.total
}
const viewRecords = async (row: any) => {
  const res: any = await request.get(`/recruit/${row.id}/records`, { params: { pageNum: 1, pageSize: 100 } })
  records.value = res.data.records
  recordVisible.value = true
}
const handleCancel = async (row: any) => {
  await ElMessageBox.confirm('确认取消该纳新？', '提示')
  await request.put(`/recruit/${row.id}/cancel`)
  ElMessage.success('已取消')
  loadData()
}
onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
