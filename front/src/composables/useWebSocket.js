import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

const BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const WS_URL = `${BASE}/ws`
const MAX_RETRIES = 3

export function useWebSocket() {
  const connected = ref(false)
  let client = null
  const subscriptions = []
  let retryCount = 0

  function connect() {
    if (client && client.connected) return

    client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 3000,
      onConnect: () => {
        connected.value = true
        retryCount = 0
      },
      onStompError: (frame) => {
        console.error('[STOMP Error]', frame.headers?.message)
        connected.value = false
      },
      onDisconnect: () => {
        connected.value = false
      },
      onWebSocketClose: () => {
        connected.value = false
        retryCount++
        if (retryCount >= MAX_RETRIES) {
          console.warn('[WebSocket] 최대 재시도 횟수 초과')
        }
      },
    })

    client.activate()
  }

  function subscribe(topic, callback) {
    if (!client) connect()

    // 이미 연결 상태이면 즉시 구독
    if (client.connected) {
      const sub = client.subscribe(topic, (message) => {
        callback(JSON.parse(message.body))
      })
      subscriptions.push(sub)
      return sub
    }

    // 연결 대기 중이면 onConnect에서 구독
    const originalOnConnect = client.onConnect
    client.onConnect = (frame) => {
      originalOnConnect(frame)
      const sub = client.subscribe(topic, (message) => {
        callback(JSON.parse(message.body))
      })
      subscriptions.push(sub)
      // 원래 핸들러 복원
      client.onConnect = originalOnConnect
    }

    return null
  }

  function disconnect() {
    subscriptions.forEach((sub) => {
      try { sub.unsubscribe() } catch (e) { /* ignore */ }
    })
    subscriptions.length = 0
    if (client) {
      client.deactivate()
      client = null
    }
    connected.value = false
  }

  onUnmounted(() => {
    disconnect()
  })

  return { connected, connect, subscribe, disconnect }
}
