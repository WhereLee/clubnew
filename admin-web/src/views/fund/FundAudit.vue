<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.clubId" placeholder="社团ID" style="width:160px" clearable />
      <el-select v-model="query.status" placeholder="状态" style="width:150px" clearable>
        <el-option label="待审批" value="PENDING" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已驳回" value="REJECTED" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>
    <el-table :data="tableData" stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="title" label="申请标题" />
      <el-table-column prop="clubId" label="社团ID" width="100" />
      <el-table-column prop="amount" label="金额" width="110" />
      <el-table-column prop="type" label="类型" width="90">
        <template #default="{ row }">{{ row.type === 'INCOME' ? '收入' : '支出' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button size="small" type="success" @click="audit(row, true)">通过</el-button>
            <el-button size="small" type="danger" @click="audit(row, false)">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
      layout="total, prev, pager, next" @current-change="loadData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'

const query = reactive({ pageNum: 1, pageSize: 10, clubId: '', status: '' })
const tableData = ref([])
const total = ref(0)

const statusText = (s: string) => ({ PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' } as any)[s] || s
const statusType = (s: string) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' } as any)[s] || 'info'

const loadData = async () => {
  const res: any = await request.get('/fund/list', { params: query })
  tableData.value = res.data.records
  total.value = res.data.total
}
const audit = async (row: any, approved: boolean) => {
  await request.put(`/fund/${row.id}/audit`, { approved, remark: '' })
  ElMessage.success(approved ? '已通过' : '已驳回')
  loadData()
}
onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
