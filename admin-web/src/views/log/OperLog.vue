<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.title" placeholder="模块标题" style="width:160px" clearable />
      <el-input v-model="query.operName" placeholder="操作人" style="width:160px" clearable />
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>
    <el-table :data="tableData" stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="title" label="模块" width="120" />
      <el-table-column prop="operName" label="操作人" width="120" />
      <el-table-column prop="requestMethod" label="方式" width="80" />
      <el-table-column prop="operUrl" label="URL" show-overflow-tooltip />
      <el-table-column prop="operIp" label="IP" width="140" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作时间" width="170">
        <template #default="{ row }">{{ fmt(row.operTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="90">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="del(row)">删除</el-button>
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

const query = reactive({ pageNum: 1, pageSize: 10, title: '', operName: '' })
const tableData = ref([])
const total = ref(0)
const fmt = (t: string) => t ? String(t).replace('T', ' ').slice(0, 19) : ''

const loadData = async () => {
  const res: any = await request.get('/system/operlog/list', { params: query })
  tableData.value = res.data.records
  total.value = res.data.total
}
const del = async (row: any) => {
  await request.delete(`/system/operlog/${row.id}`)
  ElMessage.success('已删除')
  loadData()
}
onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
