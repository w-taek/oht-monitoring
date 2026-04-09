import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 응답 인터셉터: ApiResponse에서 data 추출
api.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.status === 200) {
      return body.data
    }
    return body
  },
  (error) => {
    const message = error.response?.data?.message || '서버 오류가 발생했습니다'
    console.error('[API Error]', message)
    return Promise.reject(new Error(message))
  }
)

export default api
