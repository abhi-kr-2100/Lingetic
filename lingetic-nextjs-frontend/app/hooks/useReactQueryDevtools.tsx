import { useEffect, useState } from "react";

export default function useReactQueryDevtools() {
  const [devtools, setDevtools] = useState(<></>);

  useEffect(() => {
    if (process.env.NEXT_PUBLIC_ENVIRONMENT !== "development") {
      return;
    }

    import("@tanstack/react-query-devtools").then(({ ReactQueryDevtools }) => {
      setDevtools(<ReactQueryDevtools initialIsOpen={false} />);
    });
  }, []);

  return devtools;
}
