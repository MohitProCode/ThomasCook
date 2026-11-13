import { createContext, useContext, useMemo, useState } from 'react';

const BookingContext = createContext(null);
const SEARCH_KEY = 'tc_last_search';

function loadLastSearch() {
  try {
    const raw = sessionStorage.getItem(SEARCH_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function BookingProvider({ children }) {
  const [lastSearch, setLastSearchState] = useState(loadLastSearch);
  const [lastBooking, setLastBooking] = useState(null);

  const value = useMemo(
    () => ({
      lastSearch,
      lastBooking,
      saveSearch(search) {
        setLastSearchState(search);
        try {
          sessionStorage.setItem(SEARCH_KEY, JSON.stringify(search));
        } catch {
          /* session storage unavailable; memory copy still works */
        }
      },
      saveBooking: setLastBooking,
    }),
    [lastSearch, lastBooking],
  );

  return <BookingContext.Provider value={value}>{children}</BookingContext.Provider>;
}

export function useBooking() {
  return useContext(BookingContext);
}
