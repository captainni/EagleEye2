/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#3b82f6', // Blue-600
        secondary: '#93c5fd', // Blue-300
        warning: '#f97316', // Orange-500
        danger: '#ef4444', // Red-500
        success: '#10b981', // Emerald-500
      },
      borderRadius: {
        'none': '0px',
        'sm': '2px',
        DEFAULT: '4px', // 默认值，对应视觉规范中的 button
        'md': '8px',
        'lg': '12px',
        'xl': '16px',
        '2xl': '20px',
        '3xl': '24px',
        'full': '9999px',
        'button': '4px' // 单独定义一个与视觉规范一致的按钮圆角
      }
    }
  },
  plugins: [],
} 