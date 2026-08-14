<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.userName" placeholder="用户名" style="width: 200px" clearable @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
      <el-button type="success" @click="openAdd">新增</el-button>
    </div>
    <el-table :data="tableData" stripe>
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="phone" label="手机号" width="130">
        <template #default="{ row }">{{ row.phone || '-' }}</template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="150">
        <template #default="{ row }">{{ row.email || '-' }}</template>
      </el-table-column>
      <el-table-column prop="userType" label="身份" width="90" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.userType === 'ADMIN' ? 'warning' : 'info'" effect="dark">
            {{ row.userType === 'ADMIN' ? '管理员' : '学生' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
            {{ row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" link @click="openResetPwd(row)">重置密码</el-button>
          <el-button size="small" :type="row.status === '0' ? 'danger' : 'success'" link @click="handleToggleStatus(row)">
            {{ row.status === '0' ? '停用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" link :disabled="row.id === 1" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="460px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="显示名称" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少 6 位" show-password />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="选填" />
        </el-form-item>
        <el-form-item label="身份">
          <el-radio-group v-model="form.userType">
            <el-radio value="STUDENT">学生</el-radio>
            <el-radio value="ADMIN">管理员</el-radio>
          </el-radio-group>
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

    <!-- 重置密码 -->
    <el-dialog v-model="pwdDialog" title="重置密码" width="400px" destroy-on-close>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="用户">
          <span>{{ pwdForm.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="pwdForm.password" type="password" placeholder="至少 6 位" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialog = false">取消</el-button>
        <el-button type="primary" @click="handleResetPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const query = reactive({ pageNum: 1, pageSize: 10, userName: '' })
const tableData = ref<any[]>([])
const total = ref(0)

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<any>({})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
}

const pwdDialog = ref(false)
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive<any>({})
const pwdRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
}

const loadData = async () => {
  const res: any = await request.get('/system/user/list', { params: query })
  tableData.value = res.data.records
  total.value = res.data.total
}

const openAdd = () => {
  Object.assign(form, { id: null, username: '', nickname: '', password: '', phone: '', email: '', userType: 'STUDENT', status: '0' })
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  Object.assign(form, {
    id: row.id, username: row.username, nickname: row.nickname,
    phone: row.phone, email: row.email, userType: row.userType, status: row.status,
  })
  dialogVisible.value = true
}

const handleSave = async () => {
  await formRef.value?.validate()
  if (form.id) {
    await request.put('/system/user', form)
  } else {
    await request.post('/system/user', form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const openResetPwd = (row: any) => {
  Object.assign(pwdForm, { id: row.id, username: row.username, password: '' })
  pwdDialog.value = true
}

const handleResetPwd = async () => {
  await pwdFormRef.value?.validate()
  await request.put('/system/user/resetPwd', { id: pwdForm.id, password: pwdForm.password })
  ElMessage.success('密码已重置')
  pwdDialog.value = false
}

const handleToggleStatus = async (row: any) => {
  const action = row.status === '0' ? '停用' : '启用'
  await ElMessageBox.confirm(`确认${action}用户「${row.username}」？`, '提示', { type: 'warning' })
  await request.put('/system/user/changeStatus', { id: row.id, status: row.status === '0' ? '1' : '0' })
  ElMessage.success(`${action}成功`)
  loadData()
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」？此操作不可恢复。`, '提示', { type: 'warning' })
  await request.delete(`/system/user/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; align-items: center; }
:deep(.el-table__row) { transition: background-color 0.15s ease; }
:deep(.el-table__row:hover) { background-color: #eef2ff; }
</style>
