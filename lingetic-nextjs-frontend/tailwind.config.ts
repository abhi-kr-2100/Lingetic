import type { Config } from "tailwindcss";

export default {
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      textColor: {
        skin: {
          base: "var(--color-text-base)",
          inverted: "var(--color-text-inverted)",
          error: "var(--color-text-error)",
          success: "var(--color-text-success)",
        },
      },
      borderColor: {
        skin: {
          base: "var(--color-text-base)",
        },
      },
      backgroundColor: {
        skin: {
          "button-primary": "var(--color-button-primary)",
        },
      },
    },
  },
  plugins: [],
} satisfies Config;
