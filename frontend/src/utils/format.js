export const statusLabelMap = {
  PENDING: '待处理',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  AVAILABLE: '在售中',
  RESERVED: '已预订',
  SOLD: '已售出',
  OFFLINE: '已下架',
  DONATING: '捐赠中',
  CANCELLED: '已取消',
  COMPLETED: '已完成',
  DISPUTED: '纠纷中',
  RESOLVED: '已处理',
  DRAFT: '草稿',
  FROZEN: '已冻结',
  USER: '普通用户',
  ADMIN: '管理员',
}

export const statusTypeMap = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  AVAILABLE: 'success',
  RESERVED: 'warning',
  SOLD: 'info',
  OFFLINE: 'info',
  DONATING: 'success',
  CANCELLED: 'info',
  COMPLETED: 'success',
  DISPUTED: 'danger',
  RESOLVED: 'success',
  DRAFT: '',
  FROZEN: 'danger',
  USER: '',
  ADMIN: 'danger',
}

export const formatStatus = (value) => statusLabelMap[value] || value || '-'
export const formatTagType = (value) => statusTypeMap[value] || ''
export const formatCurrency = (value) => `￥${Number(value || 0).toFixed(2)}`
export const formatBoolean = (value) => (value ? '是' : '否')
