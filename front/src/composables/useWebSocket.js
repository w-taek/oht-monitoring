import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

const BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
const WS_URL = `${BASE}/ws`

export function useWebSocket() {
  const connected = ref(false)
  let client = null

  /**
   * 구독 정의 목록: { topic, callback }
   * 재접속 시 자동으로 다시 구독하기 위해 정의를 보관
   */
  const subscriptionDefs = []
  /** 실제 STOMP subscription 객체 (unsubscribe용) */
  const activeSubs = []

  function resubscribeAll() {
    // 기존 구독 정리
    activeSubs.forEach((sub) => {
      try { sub.unsubscribe() } catch (e) { /* ignore */ }
    })
    activeSubs.length = 0

    // 저장된 정의로 재구독
    for (const def of subscriptionDefs) {
      const sub = client.subscribe(def.topic, (message) => {
        def.callback(JSON.parse(message.body))
      })
      activeSubs.push(sub)
    }

    if (subscriptionDefs.length > 0) {
      console.log(`[WebSocket] ${subscriptionDefs.length}개 토픽 재구독 완료`)
    }
  }

  function connect() {
    if (client && client.connected) return

    client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 3000,
      onConnect: () => {
        connected.value = true
        console.log('[WebSocket] 연결됨')
        // 연결(재연결) 시마다 모든 구독 재등록
        resubscribeAll()
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
      },
    })

    client.activate()
  }

  function subscribe(topic, callback) {
    // 구독 정의를 저장 (재접속 시 재사용)
    subscriptionDefs.push({ topic, callback })

    if (!client) connect()

    // 이미 연결 상태이면 즉시 구독
    if (client.connected) {
      const sub = client.subscribe(topic, (message) => {
        callback(JSON.parse(message.body))
      })
      activeSubs.push(sub)
      return sub
    }

    // 아직 연결 중이면 onConnect에서 resubscribeAll()이 처리
    return null
  }

  function disconnect() {
    activeSubs.forEach((sub) => {
      try { sub.unsubscribe() } catch (e) { /* ignore */ }
    })
    activeSubs.length = 0
    subscriptionDefs.length = 0
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
