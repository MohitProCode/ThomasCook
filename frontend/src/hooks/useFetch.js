import { useCallback, useEffect, useRef, useState } from 'react';

export function useFetch(fetcher, deps = []) {
  const [state, setState] = useState({ status: 'idle', data: null, error: null });
  const fetcherRef = useRef(fetcher);
  fetcherRef.current = fetcher;

  const run = useCallback(async () => {
    setState((prev) => ({ ...prev, status: 'loading', error: null }));
    try {
      const data = await fetcherRef.current();
      setState({ status: 'success', data, error: null });
    } catch (error) {
      setState({ status: 'error', data: null, error });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(() => {
    run();
  }, [run]);

  return { ...state, retry: run };
}
