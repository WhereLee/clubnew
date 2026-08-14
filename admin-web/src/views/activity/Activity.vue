<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.clubId" placeholder="社团ID" style="width:160px" clearable />
      <el-select v-model="query.status" placeholder="状态" style="width:150px" clearable>
        <el-option label="草稿" value="DRAFT" />
        <el-option label="待审核" value="PENDING" />
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="进行中" value="ONGOING" />
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
      <el-table-column prop="checkinEnabled" label="签到" width="80">
        <template #default="{ row }">{{ row.checkinEnabled === 'Y' ? '开启' : '关闭' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="audit(row, true)">通过</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" type="danger" @click="audit(row, false)">驳回</el-button>
          <el-button v-if="row.status === 'PENDING'" size="small" @click="publish(row)">发布</el-button>
          <el-button v-if="['DRAFT','PENDING','PUBLISHED','ONGOING'].includes(row.status)" size="small" type="danger" @click="cancel(row)">取消</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'

const query = reactive({ pageNum: 1, pageSize: 10, clubId: '', status: '' })
const tableData = ref([])
const total = ref(0)

const statusText = (s: string) => ({ DRAFT: '草稿', PENDING: '待审核', PUBLISHED: '已发布', ONGOING: '进行中', ENDED: '已结束', CANCELLED: '已取消' } as any)[s] || s
const statusType = (s: string) => ({ DRAFT: 'info', PENDING: 'warning', PUBLISHED: 'success', ONGOING: 'success', ENDED: 'info', CANCELLED: 'danger' } as any)[s] || 'info'

const loadData = async () => {
  const res: any = await request.get('/activity/list', { params: query })
  tableData.value = res.data.records
  total.value = res.data.total
}
const audit = async (row: any, approved: boolean) => {
  await request.put(`/activity/${row.id}/audit`, { approved })
  ElMessage.success(approved ? '已通过' : '已驳回')
  loadData()
}
const publish = async (row: any) => {
  await request.put(`/activity/${row.id}/publish`)
  ElMessage.success('已发布')
  loadData()
}
const cancel = async (row: any) => {
  await ElMessageBox.confirm('确认取消该活动？', '提示')
  await request.put(`/activity/${row.id}/cancel`)
  ElMessage.success('已取消')
  loadData()
}
onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
