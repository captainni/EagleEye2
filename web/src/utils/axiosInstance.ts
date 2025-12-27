import axios from 'axios';

const instance = axios.create({
  baseURL: 'http://localhost:9090/api', // 后端API地址，修正API前缀
  timeout: 10000, // 请求超时时间
});

// 请求拦截器
instance.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么，例如添加token
    // const token = localStorage.getItem('token');
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  error => {
    // 对请求错误做些什么
    console.error('Request Error:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  response => {
    // 对响应数据做点什么
    const res = response.data;
    
    // 如果后台返回的不是CommonResult格式，或者code不是200，则抛出错误
    // 这里可以根据实际后端返回格式调整
    if (res.code !== 200) {
        console.error('Response Error:', res.message || 'Error');
        // 可以使用Element Plus的消息提示
        // ElMessage.error(res.message || 'Error');
        return Promise.reject(new Error(res.message || 'Error'));
    }
    return res;
  },
  error => {
    // 对响应错误做点什么
    console.error('Response Error:', error.message);
    // ElMessage.error(error.message);
    return Promise.reject(error);
  }
);

export default instance; 