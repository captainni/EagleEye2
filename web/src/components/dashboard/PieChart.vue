<template>
  <div class="bg-white rounded-lg shadow-sm p-6 card-hover">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-semibold text-gray-800 flex items-center">
        <i class="fas fa-chart-pie text-primary mr-2"></i> 数据分布
      </h3>
    </div>
    <div ref="chartRef" class="h-64"></div>
  </div>
</template>

<script lang="ts" setup>
import { defineProps, ref, onMounted, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps<{
  chartData: any;
}>();

const chartRef = ref<HTMLDivElement | null>(null);
let chart: echarts.ECharts | null = null;

onMounted(() => {
  if (!chartRef.value) return;

  chart = echarts.init(chartRef.value);
  updateChart();

  window.addEventListener('resize', () => chart?.resize());
});

function updateChart() {
  if (!chart || !props.chartData) return;

  // 使用第一个可用的数据集
  const datasetKey = Object.keys(props.chartData)[0];
  const data = props.chartData[datasetKey];

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data.labels?.map((label: string, i: number) => ({
          name: label,
          value: data.datasets?.[0]?.data?.[i] || 0
        })) || []
      }
    ]
  };

  chart.setOption(option);
}

watch(() => props.chartData, () => {
  updateChart();
}, { deep: true });
</script>

<style scoped>
.card-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}
.card-hover {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
</style>
