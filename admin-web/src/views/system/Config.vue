<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.configName" placeholder="参数名称" style="width: 180px" clearable @keyup.enter="loadData" />
      <el-input v-model="query.configKey" placeholder="参数键名" style="width: 180px" clearable @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button type="success" @click="openAdd">新增参数</el-button>
    </div>

    <el-table :data="tableData" stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="configName" label="参数名称" min-width="150" />
      <el-table-column prop="configKey" label="参数键名" min-width="160">
        <template #default="{ row }">
          <el-tag size="small" type="info" effect="plain">{{ row.configKey }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="configValue" label="参数键值" min-width="140" show-overflow-tooltip />
      <el-table-column prop="configType" label="类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.configType === 'Y' ? 'success' : 'info'">
            {{ row.configType === 'Y' ? '内置' : '普通' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="104" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 16px"
      @current-change="loadData"
    />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑参数' : '新增参数'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="参数名称" prop="configName">
          <el-input v-model="form.configName" placeholder="如：社团创建审核开关" />
        </el-form-item>
        <el-form-item label="参数键名" prop="configKey">
          <el-input v-model="form.configKey" placeholder="如：club.create.enabled" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="参数键值" prop="configValue">
          <el-input v-model="form.configValue" placeholder="如：true" />
        </el-form-item>
        <el-form-item label="参数类型">
          <el-radio-group v-model="form.configType">
            <el-radio value="N">普通</el-radio>
            <el-radio value="Y">内置</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-alert
          title="保存后会自动清除该键名的缓存，业务侧下次读取将拿到最新值"
          type="info"
          :closable="false"
          show-icon
        />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const query = reactive({ pageNum: 1, pageSize: 10, configName: '', configKey: '' })
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<any>({})
const rules = {
  configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入参数键名', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入参数键值', trigger: 'blur' }],
}

const loadData = async () => {
  const res: any = await request.get('/system/config/list', { params: query })
  tableData.value = res.data.records
  total.value = res.data.total
}

const resetQuery = () => {
  query.configName = ''
  query.configKey = ''
  query.pageNum = 1
  loadData()
}

const openAdd = () => {
  Object.assign(form, { id: null, configName: '', configKey: '', configValue: '', configType: 'N' })
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  Object.assign(form, {
    id: row.id, configName: row.configName, configKey: row.configKey,
    configValue: row.configValue, configType: row.configType,
  })
  dialogVisible.value = true
}

const handleSave = async () => {
  await formRef.value?.validate()
  if (form.id) {
    await request.put('/system/config', form)
  } else {
    await request.post('/system/config', form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm(`确认删除参数「${row.configName}」？`, '提示', { type: 'warning' })
  await request.delete(`/system/config/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; align-items: center; flex-wrap: wrap; }
</style>
