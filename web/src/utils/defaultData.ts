/**
 * 默认数据 - 用于临时替代 mock 数据
 * TODO: 后续应从真实 API 获取
 */

// 用户数据类型
export interface User {
  name: string;
  avatar: string;
  notifications: number;
}

// 快速入口类型
export interface QuickEntry {
  title: string;
  description: string;
  icon: string;
  link: string;
}

// 默认用户数据
export const defaultUserData: User = {
  name: '产品经理',
  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=product',
  notifications: 3
};

// 默认快速入口数据
export const defaultQuickEntryData: QuickEntry[] = [
  {
    title: '政策监控',
    description: '查看最新监管政策',
    icon: 'fa-file-alt',
    link: '/policy-monitoring'
  },
  {
    title: '竞品追踪',
    description: '监控竞品动态',
    icon: 'fa-chart-line',
    link: '/competitor-tracking'
  },
  {
    title: '需求池',
    description: '管理产品需求',
    icon: 'fa-tasks',
    link: '/requirement-pool'
  },
  {
    title: '设置',
    description: '个性化配置',
    icon: 'fa-cog',
    link: '/settings'
  }
];

// 筛选选项
export const policyImportanceOptions = [
  { label: '全部', value: '' },
  { label: '高', value: '高' },
  { label: '中', value: '中' },
  { label: '低', value: '低' }
];

export const policyRelevanceOptions = [
  { label: '全部', value: '' },
  { label: '高', value: '高' },
  { label: '中', value: '中' },
  { label: '低', value: '低' }
];

export const requirementStatusOptions = [
  { label: '全部状态', value: '' },
  { label: '待处理', value: 'pending' },
  { label: '进行中', value: 'in_progress' },
  { label: '已完成', value: 'completed' },
  { label: '已关闭', value: 'closed' }
];

export const requirementPriorityOptions = [
  { label: '全部优先级', value: '' },
  { label: '紧急', value: 'urgent' },
  { label: '高', value: 'high' },
  { label: '中', value: 'medium' },
  { label: '低', value: 'low' }
];

export const requirementSourceOptions = [
  { label: '全部来源', value: '' },
  { label: '政策建议', value: 'policy' },
  { label: '竞品分析', value: 'competitor' },
  { label: '手动创建', value: 'manual' }
];

// 竞品筛选选项
export const competitorTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '国有大行', value: 'large_state_bank' },
  { label: '股份制银行', value: 'joint_stock_bank' },
  { label: '城商行', value: 'city_bank' },
  { label: '互联网银行', value: 'internet_bank' },
  { label: '非银机构', value: 'non_bank' }
];

export const competitorTimeOptions = [
  { label: '全部时间', value: '' },
  { label: '最近一天', value: '1d' },
  { label: '最近一周', value: '1w' },
  { label: '最近一月', value: '1m' }
];

export const competitorCategoryOptions = [
  { label: '全部分类', value: '' },
  { label: '产品更新', value: 'product' },
  { label: '营销活动', value: 'marketing' },
  { label: '财报数据', value: 'financial' },
  { label: '战略调整', value: 'strategy' }
];

// 竞品筛选选项（数组格式，用于 CompetitorFilter 组件）
export const competitorTypeArray = ['全部类型', '国有大行', '股份制银行', '城商行', '互联网银行', '非银机构'];
export const competitorTimeArray = ['全部时间', '近7天', '近30天', '近90天'];
export const competitorCategoryArray = ['全部分类', '产品更新', '营销活动', '财报数据', '战略调整'];
export const competitorCompanyArray = ['全部机构', '工商银行', '建设银行', '农业银行', '中国银行', '招商银行', '浦发银行'];

// 竞品重要度和相关度选项
export const competitorImportanceOptions = [
  { label: '全部', value: '' },
  { label: '高', value: '高' },
  { label: '中', value: '中' },
  { label: '低', value: '低' }
];

export const competitorRelevanceOptions = [
  { label: '全部', value: '' },
  { label: '高', value: '高' },
  { label: '中', value: '中' },
  { label: '低', value: '低' }
];
