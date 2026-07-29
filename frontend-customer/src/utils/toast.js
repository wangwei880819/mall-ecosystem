/**
 * 显示居中深色toast提示
 * @param {string} text - 提示文字
 * @param {number} duration - 显示时长(ms)，默认2000
 */
export function toast(text, duration = 2000) {
  const el = document.createElement('div')
  el.className = 'toast'
  el.textContent = text
  document.body.appendChild(el)

  setTimeout(() => {
    el.remove()
  }, duration)
}
