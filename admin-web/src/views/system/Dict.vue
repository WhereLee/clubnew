<template>
  <div class="dict-page">
    <el-row :gutter="16">
      <!-- 左：字典类型 -->
      <el-col :span="9">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-header">
              <span>字典类型</span>
              <el-button type="primary" size="small" @click="openTypeAdd">新增类型</el-button>
            </div>
          </template>
          <el-input v-model="typeQuery.dictName" placeholder="搜索类型名称" clearable style="margin-bottom: 10px" @keyup.enter="loadTypes" />
          <el-table
            :data="typeList"
            stripe
            highlight-current-row
            @current-change="onSelectType"
            max-height="480"
          >
            <el-table-column prop="dictName" label="字典名称" />
            <el-table-column prop="dictType" label="类型编码" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="64" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
                  {{ row.status === '0' ? '正常' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="104" align="center">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click.stop="openTypeEdit(row)">编辑</el-button>
                <el-button size="small" type="danger" link @click.stop="handleTypeDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="typeQuery.pageNum"
            v-model:page-size="typeQuery.pageSize"
            :total="typeTotal"
            layout="total, prev, pager, next"
            small
            style="margin-top: 10px"
            @current-change="loadTypes"
          />
        </el-card>
      </el-col>

      <!-- 右：字典数据 -->
      <el-col :span="15">
        <el-card shadow="never" class="panel">
          <template #header>
            <div class="panel-header">
              <span>字典数据{{ currentType ? ` · ${currentType.dictName} (${currentType.dictType})` : '' }}</span>
              <el-button type="primary" size="small" :disabled="!currentType" @click="openDataAdd">新增数据</el-button>
            </div>
          </template>
          <div v-if="!currentType" class="empty-hint">
            <el-empty description="点击左侧选择一个字典类型" :image-size="80" />
          </div>
          <template v-else>
            <el-input v-model="dataQuery.dictLabel" placeholder="搜索数据标签" clearable style="width: 220px; margin-bottom: 10px" @keyup.enter="loadData" />
            <el-table :data="dataList" stripe>
              <el-table-column prop="dictSort" label="排序" width="70" align="center" />
              <el-table-column prop="dictLabel" label="标签" min-width="130" />
              <el-table-column prop="dictValue" label="键值" min-width="110">
                <template #default="{ row }">
                  <el-tag size="small" type="info" effect="plain">{{ row.dictValue }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="isDefault" label="默认" width="70" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.isDefault === 'Y'" size="small" type="success" effect="dark">默认</el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="70" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
                    {{ row.status === '0' ? '正常' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="104" align="center">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="openDataEdit(row)">编辑</el-button>
                  <el-button size="small" type="danger" link @click="handleDataDelete(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-model:current-page="dataQuery.pageNum"
              v-model:page-size="dataQuery.pageSize"
              :total="dataTotal"
              layout="total, prev, pager, next"
              small
              style="margin-top: 10px"
              @current-change="loadData"
            />
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- 类型编辑弹窗 -->
    <el-dialog v-model="typeDialog" :title="typeForm.id ? '编辑字典类型' : '新增字典类型'" width="460px" destroy-on-close>
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="如：性别" />
        </el-form-item>
        <el-form-item label="类型编码" prop="dictType">
          <el-input v-model="typeForm.dictType" placeholder="如：sys_user_sex" :disabled="!!typeForm.id" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="typeForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleTypeSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- 数据编辑弹窗 -->
    <el-dialog v-model="dataDialog" :title="dataForm.id ? '编辑字典数据' : '新增字典数据'" width="460px" destroy-on-close>
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item label="数据标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="如：男" />
        </el-form-item>
        <el-form-item label="数据键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="如：0" />
        </el-form-item>
        <el-form-item label="显示排序">
          <el-input-number v-model="dataForm.dictSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="是否默认">
          <el-radio-group v-model="dataForm.isDefault">
            <el-radio value="Y">是</el-radio>
            <el-radio value="N">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialog = false">取消</el-button>
        <el-button type="primary" @click="handleDataSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

/* ---------- 类型 ---------- */
const typeQuery = reactive({ pageNum: 1, pageSize: 10, dictName: '' })
const typeList = ref<any[]>([])
const typeTotal = ref(0)
const currentType = ref<any>(null)
const typeDialog = ref(false)
const typeFormRef = ref<FormInstance>()
const typeForm = reactive<any>({})
const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入类型编码', trigger: 'blur' }],
}

const loadTypes = async () => {
  const res: any = await request.get('/system/dict/type/list', { params: typeQuery })
  typeList.value = res.data.records
  typeTotal.value = res.data.total
}

const onSelectType = (row: any) => {
  if (!row) return
  currentType.value = row
  dataQuery.pageNum = 1
  dataQuery.dictLabel = ''
  loadData()
}

const openTypeAdd = () => {
  Object.assign(typeForm, { id: null, dictName: '', dictType: '', status: '0' })
  typeDialog.value = true
}

const openTypeEdit = (row: any) => {
  Object.assign(typeForm, { id: row.id, dictName: row.dictName, dictType: row.dictType, status: row.status })
  typeDialog.value = true
}

const handleTypeSave = async () => {
  await typeFormRef.value?.validate()
  if (typeForm.id) {
    await request.put('/system/dict/type', typeForm)
  } else {
    await request.post('/system/dict/type', typeForm)
  }
  ElMessage.success('保存成功')
  typeDialog.value = false
  loadTypes()
}

const handleTypeDelete = async (row: any) => {
  await ElMessageBox.confirm(`确认删除字典类型「${row.dictName}」？关联的字典数据会一并失效。`, '提示', { type: 'warning' })
  await request.delete(`/system/dict/type/${row.id}`)
  ElMessage.success('删除成功')
  if (currentType.value?.id === row.id) currentType.value = null
  loadTypes()
}

/* ---------- 数据 ---------- */
const dataQuery = reactive({ pageNum: 1, pageSize: 10, dictLabel: '' })
const dataList = ref<any[]>([])
const dataTotal = ref(0)
const dataDialog = ref(false)
const dataFormRef = ref<FormInstance>()
const dataForm = reactive<any>({})
const dataRules = {
  dictLabel: [{ required: true, message: '请输入数据标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入数据键值', trigger: 'blur' }],
}

const loadData = async () => {
  if (!currentType.value) return
  const res: any = await request.get('/system/dict/data/list', {
    params: { ...dataQuery, dictType: currentType.value.dictType },
  })
  dataList.value = res.data.records
  dataTotal.value = res.data.total
}

const openDataAdd = () => {
  Object.assign(dataForm, { id: null, dictLabel: '', dictValue: '', dictSort: 0, isDefault: 'N', status: '0' })
  dataDialog.value = true
}

const openDataEdit = (row: any) => {
  Object.assign(dataForm, {
    id: row.id, dictLabel: row.dictLabel, dictValue: row.dictValue,
    dictSort: row.dictSort, isDefault: row.isDefault, status: row.status,
  })
  dataDialog.value = true
}

const handleDataSave = async () => {
  await dataFormRef.value?.validate()
  const payload = { ...dataForm, dictType: currentType.value.dictType }
  if (dataForm.id) {
    await request.put('/system/dict/data', payload)
  } else {
    await request.post('/system/dict/data', payload)
  }
  ElMessage.success('保存成功')
  dataDialog.value = false
  loadData()
}

const handleDataDelete = async (row: any) => {
  await ElMessageBox.confirm(`确认删除字典数据「${row.dictLabel}」？`, '提示', { type: 'warning' })
  await request.delete(`/system/dict/data/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadTypes)
</script>

<style scoped>
.panel :deep(.el-card__header) { padding: 12px 16px; }
.panel-header { display: flex; align-items: center; justify-content: space-between; font-weight: 600; }
.empty-hint { padding: 40px 0; }
:deep(.el-table__row) { cursor: pointer; transition: background-color 0.15s ease; }
:deep(.el-table__row:hover) { background-color: #eef2ff; }
</style>
