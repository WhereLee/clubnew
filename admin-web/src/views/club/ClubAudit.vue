<template>
  <div>
    <h3>社团审批</h3>
    <el-table :data="tableData" stripe>
      <el-table-column prop="name" label="社团名称" />
      <el-table-column prop="category" label="类别" />
      <el-table-column prop="description" label="简介" />
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button type="success" size="small" @click="handleAudit(row.id, true)">通过</el-button>
          <el-button type="danger" size="small" @click="handleAudit(row.id, false)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'

const tableData = ref([])
const loadData = async () => {
  const res: any = await request.get('/club/list', { params: { status: 'PENDING', pageNum: 1, pageSize: 20 } })
  tableData.value = res.data.records
}
const handleAudit = async (clubId: number, approved: boolean) => {
  await request.put('/club/audit', { clubId, approved, remark: approved ? '通过' : '驳回' })
  ElMessage.success('操作成功')
  loadData()
}
onMounted(loadData)
</script>
