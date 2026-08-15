<template>
  <div class="monitor-page">
    <!-- 页头：标题 + Grafana 外链 + 最后更新时间 -->
    <div class="m-head">
      <div>
        <h2>运行概览</h2>
        <p class="m-sub">JVM 与业务指标来自应用内存采集（与 Prometheus/Grafana 同源）</p>
      </div>
      <div class="m-head-actions">
        <span v-if="lastUpdated" class="m-updated">更新于 {{ lastUpdated }}</span>
        <a class="m-grafana" href="http://localhost:3000" target="_blank" rel="noopener">
          <el-icon><DataAnalysis /></el-icon> Grafana 深度看板
        </a>
      </div>
    </div>

    <!-- JVM 卡片 -->
    <div class="m-cards">
      <div class="m-card">
        <p class="m-card-label">堆内存使用</p>
        <p class="m-card-value">{{ ov?.jvm.heapUsedMb ?? '-' }} <span class="m-unit">MB</span></p>
        <p class="m-card-foot">上限 {{ ov?.jvm.heapMaxMb ?? '-' }} MB</p>
        <div class="m-bar"><div class="m-bar-fill" :style="{ width: (ov?.jvm.heapUsagePct ?? 0) + '%' }"></div></div>
      </div>
      <div class="m-card">
        <p class="m-card-label">存活线程</p>
        <p class="m-card-value">{{ ov?.jvm.threadsLive ?? '-' }}</p>
        <p class="m-card-foot">jvm.threads.live</p>
      </div>
      <div class="m-card">
        <p class="m-card-label">HTTP 请求总量</p>
        <p class="m-card-value">{{ (ov?.http.requestsTotal ?? 0).toLocaleString() }}</p>
        <p class="m-card-foot">平均 {{ ov?.http.avgMs ?? '-' }} ms / 峰值 {{ ov?.http.maxMs ?? '-' }} ms</p>
      </div>
      <div class="m-card">
        <p class="m-card-label">GC 累计</p>
        <p class="m-card-value">{{ ov?.jvm.gcCount ?? '-' }} <span class="m-unit">次</span></p>
        <p class="m-card-foot">耗时 {{ ov?.jvm.gcTimeSec ?? '-' }} s · 运行 {{ uptimeText }}</p>
      </div>
    </div>

    <!-- 趋势图 -->
    <div class="m-charts">
      <div class="m-chart-box">
        <p class="m-chart-title">堆内存使用趋势（MB，近 10 分钟采样）</p>
        <div ref="heapChartRef" class="m-chart"></div>
      </div>
      <div class="m-chart-box">
        <p class="m-chart-title">HTTP 请求总量趋势（累计值）</p>
        <div ref="httpChartRef" class="m-chart"></div>
      </div>
    </div>

    <!-- 业务计数器 -->
    <div class="m-biz">
      <p class="m-chart-title">业务计数器（club_*）</p>
      <div class="m-biz-grid">
        <div v-for="item in bizItems" :key="item.key" class="m-biz-item">
          <p class="m-biz-name">{{ item.label }}</p>
          <p class="m-biz-value">{{ item.value.toLocaleString() }}</p>
          <p class="m-biz-key">{{ item.key }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { DataAnalysis } from '@element-plus/icons-vue'
import { fetchOverview, type MonitorOverview } from '../../api/monitor'

const ov = ref<MonitorOverview | null>(null)
const lastUpdated = ref('')
const heapChartRef = ref<HTMLDivElement>()
const httpChartRef = ref<HTMLDivElement>()
let heapChart: echarts.ECharts | null = null
let httpChart: echarts.ECharts | null = null
let timer: number | undefined
const heapSeries: number[] = []
const httpSeries: number[] = []
const timeAxis: string[] = []
const MAX_POINTS = 60 // 10 秒 * 60 = 10 分钟

const BIZ_LABELS: Record<string, string> = {
  club_recruit_apply_total: '纳新报名请求',
  club_activity_signup_total: '活动报名请求',
  club_stock_prededuct_failures_total: '预扣失败（DB 兜底）',
  club_ratelimit_rejections_total: '限流拦截',
  club_repeatsubmit_rejections_total: '重复提交拦截',
  club_stream_event_published_total: 'Stream 事件发布',
  club_stream_event_publish_fallbacks_total: 'Stream 发布降级',
  club_stream_event_consumed_total: 'Stream 事件消费',
  club_stream_event_consume_failures_total: 'Stream 消费失败',
  club_audit_approved_total: '审批通过',
  club_audit_rejected_total: '审批驳回',
  club_agent_tool_success_total: 'AI 工具成功',
  club_agent_tool_failure_total: 'AI 工具失败',
  club_agent_tool_denied_total: 'AI 工具权限拒绝',
  club_login_failed_total: '登录失败（安全信号）',
}

const bizItems = computed(() =>
  (ov.value?.business ?? []).map((b) => ({
    ...b,
    label: BIZ_LABELS[b.key] ?? b.key,
  })),
)

const uptimeText = computed(() => {
  const s = ov.value?.uptimeSec ?? 0
  const d = Math.floor(s / 86400)
  const h = Math.floor((s % 86400) / 3600)
  const m = Math.floor((s % 3600) / 60)
  return d > 0 ? `${d} 天 ${h} 时` : `${h} 时 ${m} 分`
})

function pushPoint(series: number[], value: number) {
  series.push(value)
  if (series.length > MAX_POINTS) series.shift()
}

async function poll() {
  try {
    const data = await fetchOverview()
    ov.value = data
    lastUpdated.value = new Date().toLocaleTimeString()
    const now = new Date().toLocaleTimeString()
    timeAxis.push(now)
    if (timeAxis.length > MAX_POINTS) timeAxis.shift()
    pushPoint(heapSeries, data.jvm.heapUsedMb)
    pushPoint(httpSeries, data.http.requestsTotal)
    renderCharts()
  } catch {
    /* 轮询失败静默，等下一轮 */
  }
}

function renderCharts() {
  if (heapChart) {
    heapChart.setOption({
      grid: { left: 48, right: 16, top: 16, bottom: 28 },
      xAxis: { type: 'category', data: timeAxis, axisLabel: { color: '#8b8f98', fontSize: 10 } },
      yAxis: { type: 'value', name: 'MB', nameTextStyle: { color: '#8b8f98' }, splitLine: { lineStyle: { color: '#eef0f4' } }, axisLabel: { color: '#8b8f98' } },
      series: [{ type: 'line', data: heapSeries, smooth: true, symbol: 'none', lineStyle: { width: 2, color: '#2f6fed' }, areaStyle: { color: 'rgba(47,111,237,0.08)' } }],
      tooltip: { trigger: 'axis' },
    })
  }
  if (httpChart) {
    httpChart.setOption({
      grid: { left: 56, right: 16, top: 16, bottom: 28 },
      xAxis: { type: 'category', data: timeAxis, axisLabel: { color: '#8b8f98', fontSize: 10 } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#eef0f4' } }, axisLabel: { color: '#8b8f98' } },
      series: [{ type: 'line', data: httpSeries, smooth: true, symbol: 'none', lineStyle: { width: 2, color: '#c98a2d' }, areaStyle: { color: 'rgba(201,138,45,0.08)' } }],
      tooltip: { trigger: 'axis' },
    })
  }
}

onMounted(() => {
  heapChart = echarts.init(heapChartRef.value!)
  httpChart = echarts.init(httpChartRef.value!)
  poll()
  timer = window.setInterval(poll, 10000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
  heapChart?.dispose()
  httpChart?.dispose()
})
</script>

<style scoped>
.monitor-page { padding: 20px 24px; }
.m-head { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px; }
.m-head h2 { font-size: 20px; font-weight: 650; margin: 0; color: #1f2329; }
.m-sub { margin: 6px 0 0; font-size: 12px; color: #8b8f98; }
.m-head-actions { display: flex; align-items: center; gap: 14px; }
.m-updated { font-size: 12px; color: #8b8f98; }
.m-grafana {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 13px; color: #2f6fed; text-decoration: none;
  border: 1px solid #c9d6f5; padding: 7px 14px; border-radius: 6px;
  transition: background 0.2s;
}
.m-grafana:hover { background: #f2f6ff; }

.m-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 18px; }
.m-card {
  background: #fff; border: 1px solid #e8eaee; border-radius: 8px; padding: 16px 18px;
  transition: box-shadow 0.2s;
}
.m-card:hover { box-shadow: 0 4px 14px rgba(31, 35, 41, 0.07); }
.m-card-label { margin: 0; font-size: 12px; color: #8b8f98; }
.m-card-value { margin: 8px 0 4px; font-size: 26px; font-weight: 650; color: #1f2329; }
.m-unit { font-size: 13px; font-weight: 400; color: #8b8f98; }
.m-card-foot { margin: 0; font-size: 12px; color: #8b8f98; }
.m-bar { height: 4px; background: #eef0f4; border-radius: 2px; margin-top: 10px; overflow: hidden; }
.m-bar-fill { height: 100%; background: #2f6fed; border-radius: 2px; transition: width 0.6s ease; }

.m-charts { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 18px; }
.m-chart-box { background: #fff; border: 1px solid #e8eaee; border-radius: 8px; padding: 14px 16px; }
.m-chart-title { margin: 0 0 10px; font-size: 13px; font-weight: 600; color: #1f2329; }
.m-chart { height: 240px; }

.m-biz { background: #fff; border: 1px solid #e8eaee; border-radius: 8px; padding: 14px 16px; }
.m-biz-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px; margin-top: 12px; }
.m-biz-item { border: 1px solid #f0f1f4; border-radius: 6px; padding: 10px 12px; }
.m-biz-name { margin: 0; font-size: 12px; color: #5c616a; }
.m-biz-value { margin: 6px 0 2px; font-size: 20px; font-weight: 650; color: #1f2329; }
.m-biz-key { margin: 0; font-size: 10px; color: #b3b7be; word-break: break-all; }
</style>
