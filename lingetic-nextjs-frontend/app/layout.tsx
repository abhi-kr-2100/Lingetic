import './globals.css';
import type { Metadata } from 'next';
import Navbar from './components/NavBar';

export const metadata: Metadata = {
  title: 'Lingetic',
  description: 'AI-Powered Language Learning for Real-World Conversations',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body>
        <Navbar />
        {children}
      </body>
    </html>
  );
}
