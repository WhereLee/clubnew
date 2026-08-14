<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.name" placeholder="社团名称" style="width:200px" clearable />
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>
    <el-table :data="tableData" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="社团名称" />
      <el-table-column prop="code" label="编号" />
      <el-table-column prop="category" label="类别" />
      <el-table-column prop="status" label="状态">
        <template #default="{row}">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="memberCount" label="成员数" />
      <el-table-column prop="starLevel" label="星级" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'

const query = reactive({ name: '', pageNum: 1, pageSize: 10 })
const tableData = ref([])
const statusType = (s: string) => ({ PENDING: 'warning', APPROVED: 'success', SUSPENDED: 'info', DISSOLVED: 'danger', REJECTED: 'danger' }[s] || 'info')
const loadData = async () => {
  const res: any = await request.get('/club/list', { params: query })
  tableData.value = res.data.records
}
onMounted(loadData)
</script>

<style scoped>.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }</style>
