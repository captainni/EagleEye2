/**
 * 格式化日期时间
 * @param dateTime 时间对象或字符串
 * @returns 格式化后的字符串 (YYYY-MM-DD HH:mm:ss)
 */
export function formatDateTime(dateTime: string | Date | null | undefined): string {
  if (!dateTime) {
    return '-';
  }
  try {
    const date = new Date(dateTime);
    if (isNaN(date.getTime())) {
      return '-'; // Invalid date
    }
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (error) {
    console.error("Error formatting date-time:", error);
    return '-'; // Return fallback on error
  }
}

// You can add other formatters here as needed 