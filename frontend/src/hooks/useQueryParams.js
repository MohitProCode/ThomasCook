import { useSearchParams } from 'react-router-dom';
import { useMemo } from 'react';

export function useQueryParams() {
  const [params] = useSearchParams();
  return useMemo(
    () => ({
      location: params.get('location') || '',
      checkIn: params.get('checkIn') || '',
      checkOut: params.get('checkOut') || '',
      guests: Number(params.get('guests') || 2),
    }),
    [params],
  );
}
