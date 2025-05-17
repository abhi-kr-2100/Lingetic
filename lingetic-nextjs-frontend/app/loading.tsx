import { Loader2 } from "lucide-react";
import { ReactNode } from "react";

export default function LoadingPage() {
  return (
    <div className="flex-1">
      <LoadingComponent />
    </div>
  );
}

interface LoadingComponentProps {
  children?: ReactNode;
}

export function LoadingComponent({ children }: LoadingComponentProps) {
  return (
    <div className="h-full bg-gradient-to-b from-blue-50 to-white flex items-center justify-center">
      <div className="bg-white p-8 rounded-xl shadow-lg max-w-md w-full text-center">
        <Loader2 className="h-12 w-12 text-[#2563eb] animate-spin mx-auto mb-4" />
        {children ?? <p className="text-[#374151] text-lg">Loading...</p>}
      </div>
    </div>
  );
}
