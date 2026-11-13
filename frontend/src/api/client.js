const BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');

export class ApiError extends Error {
  constructor({ status = 0, message = 'Request failed', body = null, alternatives = null }) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
    this.alternatives = alternatives;
  }
}

function withQuery(path, query = {}) {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(query)) {
    if (value !== undefined && value !== null && value !== '') {
      params.append(key, value);
    }
  }
  const suffix = params.toString();
  return suffix ? `${path}?${suffix}` : path;
}

export async function apiFetch(path, { method = 'GET', body, query, timeoutMs = 12000 } = {}) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  let response;
  try {
    const token = localStorage.getItem('tc_access_token');
    response = await fetch(`${BASE}${withQuery(path, query)}`, {
      method,
      headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal: controller.signal,
    });
  } catch (err) {
    clearTimeout(timer);
    if (err?.name === 'AbortError') {
      throw new ApiError({ message: 'Request timed out. Please try again.' });
    }
    throw new ApiError({
      message: 'Cannot reach the backend at localhost:8081. Is Spring Boot running?',
    });
  }
  clearTimeout(timer);

  let data = null;
  try {
    const text = await response.text();
    data = text ? JSON.parse(text) : null;
  } catch {
    data = null;
  }

  if (!response.ok) {
    const message =
      data?.message ||
      (response.status === 404 ? 'Not found' : `Request failed (${response.status})`);
    throw new ApiError({
      status: response.status,
      message,
      body: data,
      alternatives: data?.smartAlternatives || data?.alternatives || null,
    });
  }
  return data;
}
