import "./globals.css";
import type { Metadata } from "next";
import { Analytics } from "@vercel/analytics/next";
import Navbar from "./components/NavBar";
import Providers from "./providers";

export const metadata: Metadata = {
  title: "Lingetic",
  description: "AI-Powered Language Learning for Real-World Conversations",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>
        <Providers>
          <Navbar />
          {children}
        </Providers>
        <Analytics />
      </body>
    </html>
  );
}
