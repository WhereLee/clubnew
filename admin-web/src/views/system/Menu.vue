<template>
  <div class="menu-page">
    <div class="toolbar">
      <el-input v-model="query.menuName" placeholder="菜单名称" style="width: 200px" clearable @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="状态" style="width: 130px" clearable>
        <el-option label="正常" value="0" />
        <el-option label="停用" value="1" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="openAdd()">新增菜单</el-button>
      <el-button @click="toggleExpand">
        {{ expanded ? '折叠' : '展开' }}
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="treeData"
      row-key="id"
      stripe
      :default-expand-all="expanded"
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="menuName" label="菜单名称" min-width="180" />
      <el-table-column prop="icon" label="图标" width="80" align="center" />
      <el-table-column prop="orderNum" label="排序" width="70" align="center" />
      <el-table-column prop="perms" label="权限标识" min-width="160">
        <template #default="{ row }">
          <el-tag v-if="row.perms" size="small" type="info" effect="plain">{{ row.perms }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路由地址" width="140" />
      <el-table-column prop="menuType" label="类型" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.menuType)" size="small" effect="dark">{{ typeText(row.menuType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
            {{ row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openAdd(row)">新增下级</el-button>
          <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑菜单' : '新增菜单'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'label', children: 'children' }"
            node-key="id"
            value-key="id"
            check-strictly
            :render-after-expand="false"
            placeholder="选择上级菜单（不选则为根菜单）"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType" @change="onTypeChange">
            <el-radio-button value="M">目录</el-radio-button>
            <el-radio-button value="C">菜单</el-radio-button>
            <el-radio-button value="F">按钮</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="如：用户管理" maxlength="50" />
        </el-form-item>
        <el-form-item label="显示排序" prop="orderNum">
          <el-input-number v-model="form.orderNum" :min="0" :max="999" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="路由地址" prop="path">
          <el-input v-model="form.path" placeholder="如：system/user" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 'C'" label="组件路径">
          <el-input v-model="form.component" placeholder="如：system/user/index" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'M'" label="权限标识">
          <el-input v-model="form.perms" placeholder="如：system:user:list" />
        </el-form-item>
        <el-form-item label="菜单图标">
          <el-input v-model="form.icon" placeholder="如：User" />
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
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const query = reactive({ menuName: '', status: '' })
const treeData = ref<any[]>([])
const parentOptions = ref<any[]>([])
const loading = ref(false)
const expanded = ref(true)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<any>({})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  orderNum: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路由地址', trigger: 'blur' }],
}

const typeText = (t: string) => ({ M: '目录', C: '菜单', F: '按钮' } as any)[t] || t
const typeTag = (t: string) => ({ M: 'warning', C: 'success', F: 'info' } as any)[t] || 'info'

/** 平铺列表 → 树（el-table 树形数据） */
const buildTree = (list: any[]) => {
  const map = new Map<number, any>()
  list.forEach((m) => map.set(m.id, { ...m, children: [] }))
  const tree: any[] = []
  map.forEach((m) => {
    if (m.parentId && map.has(m.parentId)) {
      map.get(m.parentId).children.push(m)
    } else {
      tree.push(m)
    }
  })
  return tree
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/system/menu/list', { params: query })
    treeData.value = buildTree(res.data || [])
  } finally {
    loading.value = false
  }
}

const loadParentOptions = async () => {
  const res: any = await request.get('/system/menu/treeselect')
  parentOptions.value = res.data || []
}

const toggleExpand = () => {
  expanded.value = !expanded.value
}

const resetForm = () => {
  Object.assign(form, {
    id: null, parentId: null, menuName: '', menuType: 'C', orderNum: 1,
    path: '', component: '', perms: '', icon: '', status: '0',
  })
}

const onTypeChange = () => {
  if (form.menuType === 'M') { form.component = ''; form.perms = '' }
  if (form.menuType === 'F') { form.path = ''; form.component = '' }
}

const openAdd = (parent?: any) => {
  resetForm()
  if (parent) form.parentId = parent.id
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  Object.assign(form, {
    id: row.id, parentId: row.parentId, menuName: row.menuName, menuType: row.menuType,
    orderNum: row.orderNum, path: row.path, component: row.component,
    perms: row.perms, icon: row.icon, status: row.status,
  })
  dialogVisible.value = true
}

const handleSave = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = {
      id: form.id,
      parentId: form.parentId || 0,
      menuName: form.menuName,
      menuType: form.menuType,
      orderNum: form.orderNum,
      path: form.path,
      component: form.component,
      perms: form.perms,
      icon: form.icon,
      status: form.status,
    }
    if (form.id) {
      await request.put('/system/menu', payload)
    } else {
      await request.post('/system/menu', payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
    loadParentOptions()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm(`确认删除菜单「${row.menuName}」？`, '提示', { type: 'warning' })
  await request.delete(`/system/menu/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
  loadParentOptions()
}

onMounted(() => {
  loadData()
  loadParentOptions()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; align-items: center; flex-wrap: wrap; }
:deep(.el-table .el-table__row) { transition: background-color 0.15s ease; }
:deep(.el-table .el-table__row:hover) { background-color: #eef2ff; }
:deep(.el-button.is-link) { margin-left: 0; padding: 0 6px; }
</style>
