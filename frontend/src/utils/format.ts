import dayjs from 'dayjs'

/**
 * 格式化日期时间
 * @param dateString 日期字符串
 * @param format 格式化模式，默认为 YYYY-MM-DD HH:mm:ss
 * @returns 格式化后的日期字符串
 */
export function formatDate(dateString: string | undefined, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!dateString) return '未设置'
  
  try {
    return dayjs(dateString).format(format)
  } catch (error) {
    console.error('日期格式化错误:', error)
    return dateString || '未设置'
  }
}

/**
 * 格式化文件大小
 * @param bytes 字节数
 * @returns 格式化后的文件大小字符串
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化数字，添加千位分隔符
 * @param num 数字
 * @returns 格式化后的数字字符串
 */
export function formatNumber(num: number): string {
  return num.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1,')
} 