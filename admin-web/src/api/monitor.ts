import request from '../utils/request'

export interface MonitorOverview {
  jvm: {
    heapUsedMb: number
    heapMaxMb: number
    heapUsagePct: number
    threadsLive: number
    gcCount: number
    gcTimeSec: number
  }
  http: {
    requestsTotal: number
    avgMs: number
    maxMs: number
  }
  business: Array<{ key: string; value: number }>
  uptimeSec: number
}

export const fetchOverview = () =>
  request.get<MonitorOverview>('/monitor/overview').then((r: any) => r.data)
