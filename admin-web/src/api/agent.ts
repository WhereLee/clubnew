import request, { getToken } from '../utils/request'

export interface AgentSession {
  id: number
  title: string
  model: string
  updateTime?: string
}

export interface AgentMessage {
  id: number
  sessionId: number
  role: 'user' | 'assistant'
  content: string
  toolCalls?: string
  createTime?: string
}

/** 流式对话：SSE 解析（text 增量 / done 结束） */
export async function streamChat(
  sessionId: number | null,
  content: string,
  onText: (chunk: string) => void,
  onDone: (meta: { sessionId: number; messageId: number }) => void,
  onError: (msg: string) => void,
): Promise<void> {
  const resp = await fetch('/api/agent/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${getToken()}`,
    },
    body: JSON.stringify({ sessionId, content }),
  })
  if (!resp.ok || !resp.body) {
    onError(`请求失败（${resp.status}）`)
    return
  }
  const reader = resp.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  try {
    for (;;) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      // 按 SSE 事件边界切分
      let idx: number
      while ((idx = buffer.indexOf('\n\n')) >= 0) {
        const block = buffer.slice(0, idx)
        buffer = buffer.slice(idx + 2)
        let event = 'message'
        let data = ''
        for (const line of block.split('\n')) {
          if (line.startsWith('event:')) event = line.slice(6).trim()
          else if (line.startsWith('data:')) data += line.slice(5).trim()
        }
        if (event === 'done' && data) {
          try {
            onDone(JSON.parse(data))
          } catch {
            /* done 元数据解析失败不阻断 */
          }
        } else if (data) {
          onText(data)
        }
      }
    }
  } catch (e) {
    onError('连接中断，请重试')
  }
}

export const listSessions = () => request.get<AgentSession[]>('/agent/sessions')

export const listMessages = (sessionId: number) =>
  request.get<AgentMessage[]>(`/agent/sessions/${sessionId}/messages`)

export const deleteSession = (sessionId: number) => request.delete(`/agent/sessions/${sessionId}`)
