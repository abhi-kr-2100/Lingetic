import "./globals.css";
import type { Metadata } from "next";
import Navbar from "./components/NavBar";
import Providers from "./providers";
import PostHogUserIdentifier from "./components/PostHogUserIdentifier";

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
          <PostHogUserIdentifier />
          <Navbar />
          <main className="min-h-[calc(100vh-72px)] flex">{children}</main>
        </Providers>
      </body>
    </html>
  );
}
