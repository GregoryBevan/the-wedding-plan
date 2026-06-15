/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./public/**/*.{vue,js,ts,jsx,tsx}",
    "./backoffice/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#788f9c',
        secondary: '#bac2bd',
        accent: '#e0cec9',
        background: '#f4f5f5', // Very light ash grey
        text: '#37474f',       // Dark blue-grey (matches primary)
      },
      fontFamily: {
        serif: ['ui-serif', 'Georgia', 'Cambria', '"Times New Roman"', 'Times', 'serif'],
      },
    },
  },
  plugins: [],
}
