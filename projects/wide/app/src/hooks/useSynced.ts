import { useEffect, useState } from "react";

export function useSynced(data: unknown) {
  const [at, setAt] = useState<Date | null>(null);
  useEffect(() => {
    if (data) setAt(new Date());
  }, [data]);
  return at;
}
