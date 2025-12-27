import axios from 'axios';
import { ElMessage } from 'element-plus';

// 创建 axios 实例
const apiClient = axios.create({
  // API 的 base_url (可以从环境变量读取)
  // baseURL: process.env.VUE_APP_BASE_API || '/api', 
  baseURL: '/api', // 先使用 /api 前缀，配合 Vite proxy 或 Nginx 转发
  timeout: 10000, // 请求超时时间
});

// 请求拦截器 (可选)
apiClient.interceptors.request.use(
  (config) => {
    // 可以在这里添加认证 token 等
    // const token = localStorage.getItem('token');
    // if (token) {
    //   config.headers['Authorization'] = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => {
    console.error('Request Error:', error); // for debug
    return Promise.reject(error);
  }
);

// 响应拦截器 (可选)
apiClient.interceptors.response.use(
  (response) => {
    // 直接返回响应的 data 部分
    // 后端返回的结构是 { code: number, message: string, data: any }
    const res = response.data;
    console.log('API Response:', response.config.url, res); // 添加调试日志

    // 可以根据 code 进行统一的错误处理
    if (res.code !== 200) {
      ElMessage({
        message: res.message || 'Error',
        type: 'error',
        duration: 5 * 1000,
      });

      // 可以在这里处理特定的错误码，例如 401 未授权
      // if (res.code === 401) {
      //   // 处理未授权逻辑，例如跳转到登录页
      // }
      
      // 返回一个 rejected Promise，阻止后续 then 的执行
      // 注意：如果希望业务代码还能拿到非200的响应信息，可以不 reject，而是直接 return res
      return Promise.reject(new Error(res.message || 'Error')); 
    }
    // 如果 code 是 200，则只返回 data 部分，让业务代码直接使用
    return res.data; 
  },
  (error) => {
    console.error('Response Error:', error); // for debug
    ElMessage({
      message: error.message,
      type: 'error',
      duration: 5 * 1000,
    });
    return Promise.reject(error);
  }
);

export default apiClient; 