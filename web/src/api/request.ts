import axios from 'axios';

// 创建axios实例
const instance = axios.create({
  baseURL: 'http://localhost:9090/api', // 与后端application.yml中的端口和context-path对应
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
instance.interceptors.request.use(
  config => {
    // 可以在这里添加token等认证信息
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  response => {
    const res = response.data;
    // 根据后端的CommonResult结构处理响应
    if (res.code === 200) {
      return res.data;
    } else {
      console.error('API请求错误:', res.message);
      return Promise.reject(new Error(res.message || '未知错误'));
    }
  },
  error => {
    console.error('请求失败:', error);
    return Promise.reject(error);
  }
);

export default instance;