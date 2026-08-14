<template>
  <div class="rank-page">
    <div class="toolbar">
      <span>统计天数：</span>
      <el-input-number v-model="days" :min="1" :max="90" @change="loadData" />
    </div>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>社团活跃度榜</template>
          <el-table :data="clubRank" stripe>
            <el-table-column type="index" label="排名" width="70" />
            <el-table-column prop="clubId" label="社团ID" width="120" />
            <el-table-column prop="score" label="活跃度" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>动态热度榜</template>
          <el-table :data="postRank" stripe>
            <el-table-column type="index" label="排名" width="70" />
            <el-table-column prop="postId" label="动态ID" width="120" />
            <el-table-column prop="score" label="热度" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../../utils/request'

const days = ref(7)
const clubRank = ref([])
const postRank = ref([])

const loadData = async () => {
  const clubRes: any = await request.get('/rank/club/activity', { params: { days: days.value } })
  clubRank.value = clubRes.data || []
  const postRes: any = await request.get('/rank/post/hot', { params: { days: days.value } })
  postRank.value = postRes.data || []
}
onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
</style>
