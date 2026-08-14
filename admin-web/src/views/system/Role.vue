<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.roleName" placeholder="角色名称" style="width: 200px" clearable @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="状态" style="width: 130px" clearable>
        <el-option label="正常" value="0" />
        <el-option label="停用" value="1" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="openAdd">新增角色</el-button>
    </div>

    <el-table :data="tableData" stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="roleName" label="角色名称" min-width="130" />
      <el-table-column prop="roleKey" label="权限标识" min-width="140">
        <template #default="{ row }">
          <el-tag size="small" type="info" effect="plain">{{ row.roleKey }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="roleSort" label="排序" width="70" align="center" />
      <el-table-column label="数据范围" min-width="150">
        <template #default="{ row }">
          <el-tag size="small" effect="dark" :type="scopeTag(row.dataScope)">{{ scopeText(row.dataScope) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
            {{ row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="104" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link :disabled="row.id <= 3" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑角色' : '新增角色'" width="560px" destroy-on-close @opened="syncCheckedKeys">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="如：社长" />
        </el-form-item>
        <el-form-item label="权限标识" prop="roleKey">
          <el-input v-model="form.roleKey" placeholder="如：president" />
        </el-form-item>
        <el-form-item label="显示排序">
          <el-input-number v-model="form.roleSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="数据范围" prop="dataScope">
          <el-select v-model="form.dataScope" placeholder="选择数据范围" style="width: 100%">
            <el-option label="全部数据权限" value="1" />
            <el-option label="本社团及以下数据权限" value="2" />
            <el-option label="本社团数据权限" value="3" />
            <el-option label="仅本人数据权限" value="4" />
            <el-option label="自定义数据权限" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单权限">
          <div class="menu-tree-box">
            <el-tree
              ref="menuTreeRef"
              :data="menuTree"
              :props="{ label: 'label', children: 'children' }"
              node-key="id"
              show-checkbox
              default-expand-all
            >
              <template #default="{ data }">
                <span>{{ data.label }}</span>
              </template>
            </el-tree>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
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

const query = reactive({ pageNum: 1, pageSize: 10, roleName: '', status: '' })
const tableData = ref<any[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<any>({})
const menuTree = ref<any[]>([])
const menuTreeRef = ref()
const checkedKeys = ref<number[]>([])

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入权限标识', trigger: 'blur' }],
  dataScope: [{ required: true, message: '请选择数据范围', trigger: 'change' }],
}

const scopeText = (s: string) =>
  ({ '1': '全部', '2': '本社团及以下', '3': '本社团', '4': '仅本人', '5': '自定义' } as any)[s] || s
const scopeTag = (s: string) =>
  ({ '1': 'success', '2': 'warning', '3': 'warning', '4': 'danger', '5': 'info' } as any)[s] || 'info'

const loadData = async () => {
  const res: any = await request.get('/system/role/list', { params: query })
  tableData.value = res.data.records
  total.value = res.data.total
}

const loadMenuTree = async () => {
  const res: any = await request.get('/system/menu/treeselect')
  menuTree.value = res.data || []
}

const openAdd = () => {
  Object.assign(form, { id: null, roleName: '', roleKey: '', roleSort: 0, dataScope: '4', status: '0' })
  checkedKeys.value = []
  dialogVisible.value = true
}

const openEdit = async (row: any) => {
  Object.assign(form, {
    id: row.id, roleName: row.roleName, roleKey: row.roleKey,
    roleSort: row.roleSort, dataScope: row.dataScope, status: row.status,
  })
  const res: any = await request.get(`/system/menu/roleMenuTreeselect/${row.id}`)
  checkedKeys.value = res.data.checkedKeys || []
  dialogVisible.value = true
}

/** dialog 打开后同步树勾选状态 */
const syncCheckedKeys = () => {
  if (menuTreeRef.value) {
    menuTreeRef.value.setCheckedKeys(checkedKeys.value)
  }
}

const handleSave = async () => {
  await formRef.value?.validate()
  // 全选 + 半选（父目录部分勾选）一并保存，保证菜单树完整
  const menuIds = menuTreeRef.value
    ? [...menuTreeRef.value.getCheckedKeys(), ...menuTreeRef.value.getHalfCheckedKeys()]
    : []
  const payload = { ...form, menuIds }
  if (form.id) {
    await request.put('/system/role', payload)
  } else {
    await request.post('/system/role', payload)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm(`确认删除角色「${row.roleName}」？`, '提示', { type: 'warning' })
  await request.delete(`/system/role/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
  loadMenuTree()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; align-items: center; }
.menu-tree-box {
  width: 100%;
  max-height: 260px;
  overflow: auto;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 8px;
}
:deep(.el-table__row) { transition: background-color 0.15s ease; }
:deep(.el-table__row:hover) { background-color: #eef2ff; }
</style>
