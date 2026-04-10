import axios from 'axios'
import router from '@/router'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 요청 인터셉터: JWT 토큰 자동 첨부
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 응답 인터셉터: ApiResponse에서 data 추출 + 401 처리
api.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.status === 200) {
      return body.data
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || '서버 오류가 발생했습니다'

    // 401 → 토큰 만료/무효 → 로그인으로 리다이렉트
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userName')
      localStorage.removeItem('userRole')
      router.push('/login')
    }

    console.error('[API Error]', message)
    return Promise.reject(new Error(message))
  }
)

export default api
