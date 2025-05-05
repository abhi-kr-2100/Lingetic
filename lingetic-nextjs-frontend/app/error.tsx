"use client";

import Link from "next/link";

interface ErrorPageProps {
  error: Error;
}

export default function ErrorPage({ error }: ErrorPageProps) {
  const handleReload = () => {
    window.location.reload();
  };

  return (
    <div className="flex-1 flex items-center justify-center bg-gray-100">
      <div className="max-w-md w-full space-y-8 p-10 bg-white shadow rounded-xl">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-[#374151] mb-2">
            Oops! Something went wrong.
          </h1>
          <p className="text-[#dc2626] mb-8">
            We encountered an unexpected error.
            {process.env.NEXT_PUBLIC_ENVIRONMENT === "development" && (
              <span className="block mt-2 text-sm">{error.message}</span>
            )}
          </p>
        </div>
        <div className="flex flex-col space-y-4">
          <button
            autoFocus
            type="button"
            onClick={handleReload}
            className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-[#ffffff] bg-[#2563eb] hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            Reload page
          </button>
          <Link
            href="/"
            className="w-full flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-[#374151] bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            Return to homepage
          </Link>
        </div>
      </div>
    </div>
  );
}
