/**
 * 通用分页数据结构
 */
export interface CommonPage<T> {
    list: T[];       // 当前页数据列表
    total: number;   // 总记录数
    pageNum: number; // 当前页码
    pageSize: number;// 每页记录数
    pages: number;   // 总页数
}

/**
 * 通用API返回结果
 */
export interface CommonResult<T> {
    code: number;    // 状态码，例如 200 表示成功
    message: string; // 提示信息
    data: T;         // 返回的数据
}

/**
 * 预警信息类型
 */
export interface Alert {
  id: number | string; // 可以是数字或字符串ID
  type: string;      // 预警类型，如'紧急预警'
  title: string;     // 预警标题
  importance?: string; // 重要性级别
  impactArea?: string; // 影响范围
  suggestion?: string; // 建议内容
  description: string; // 预警描述
  link?: string;      // 相关链接 (可选)
} 