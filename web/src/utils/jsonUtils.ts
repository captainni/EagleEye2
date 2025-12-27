/**
 * JSON处理工具函数
 */

/**
 * 格式化JSON字符串，使其更易于阅读
 * @param jsonString 需要格式化的JSON字符串或对象
 * @returns 格式化后的JSON字符串，无效JSON返回"{}"
 */
export function formatJSON(jsonString: string | any): string {
  if (!jsonString) return '{}';
  
  try {
    // 如果是对象，先转成字符串
    const jsonObj = typeof jsonString === 'string' 
      ? JSON.parse(jsonString) 
      : jsonString;
    
    // 使用2个空格缩进格式化
    return JSON.stringify(jsonObj, null, 2);
  } catch (error) {
    console.error('JSON格式化失败:', error);
    return '{}';
  }
}

/**
 * 验证JSON字符串是否有效
 * @param jsonString 要验证的JSON字符串
 * @returns 是否为有效的JSON
 */
export function validateJSON(jsonString: string): boolean {
  if (!jsonString || jsonString.trim() === '' || jsonString.trim() === '{}') {
    return true; // 空字符串或空对象视为有效
  }
  
  try {
    JSON.parse(jsonString);
    return true;
  } catch (error) {
    return false;
  }
}

/**
 * 安全地将对象转换为JSON字符串(无格式)
 * @param value 要转换的对象或字符串
 * @returns 有效的JSON字符串，无效时返回"{}"
 */
export function safeJSONStringify(value: any): string {
  if (!value) return '{}';
  
  if (typeof value === 'string') {
    try {
      // 如果已经是字符串，验证是否为有效JSON
      JSON.parse(value);
      return value; // 是有效的JSON字符串，直接返回
    } catch (error) {
      return '{}'; // 无效的JSON字符串，返回空对象
    }
  }
  
  try {
    return JSON.stringify(value);
  } catch (error) {
    return '{}';
  }
}

/**
 * 修复并清理JSON字符串，移除非法字符
 * @param jsonString 可能不完全有效的JSON字符串
 * @returns 修复后的JSON字符串，无法修复则返回"{}"
 */
export function fixJSON(jsonString: string): string {
  if (!jsonString) return '{}';
  
  try {
    // 尝试解析，如果有效直接返回格式化后的JSON
    JSON.parse(jsonString);
    return formatJSON(jsonString);
  } catch (error) {
    // 尝试修复常见问题
    try {
      // 替换单引号为双引号
      let fixed = jsonString.replace(/'/g, '"');
      
      // 尝试处理未加引号的键
      fixed = fixed.replace(/(\w+):/g, '"$1":');
      
      // 尝试解析修复后的JSON
      const obj = JSON.parse(fixed);
      return formatJSON(obj);
    } catch (e) {
      return '{}';
    }
  }
} 