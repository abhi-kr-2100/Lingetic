import { Loader2 } from "lucide-react";

export function LoadingState() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white flex items-center justify-center">
      <div className="bg-white p-8 rounded-xl shadow-lg max-w-md w-full text-center">
        <Loader2 className="h-12 w-12 text-[#2563eb] animate-spin mx-auto mb-4" />
        <p className="text-[#374151] text-lg">Loading question lists...</p>
      </div>
    </div>
  );
}
