import { ElMessage } from 'element-plus'
import type { MessageOptions, MessageType } from 'element-plus'

const duration = 2600
type MessageContent = Exclude<MessageOptions['message'], undefined>

function show(type: MessageType, message: MessageContent, options: Omit<MessageOptions, 'type' | 'message'> = {}) {
  return ElMessage({
    type,
    message,
    duration,
    showClose: false,
    customClass: `app-message app-message--${type}`,
    ...options
  })
}

/** 统一后台提示样式，保持与产品原型 Toast 的视觉和停留时间一致。 */
export const message = {
  success: (text: MessageContent, options?: Omit<MessageOptions, 'type' | 'message'>) => show('success', text, options),
  warning: (text: MessageContent, options?: Omit<MessageOptions, 'type' | 'message'>) => show('warning', text, options),
  error: (text: MessageContent, options?: Omit<MessageOptions, 'type' | 'message'>) => show('error', text, options),
  info: (text: MessageContent, options?: Omit<MessageOptions, 'type' | 'message'>) => show('info', text, options)
}
