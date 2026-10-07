/* ============================================
   APEX F1 — API client
   Frontend is served by Spring Boot, so we use a
   relative base URL (same origin, no CORS needed).
   ============================================ */

const API_BASE = '/api';
const SEASON = 2026;

async function apiGet(path) {
  const res = await fetch(API_BASE + path);
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.json();
}

async function apiPost(path, body) {
  const res = await fetch(API_BASE + path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    let msg = `${res.status} ${res.statusText}`;
    try { const j = await res.json(); if (j.message) msg = j.message; } catch (_) {}
    throw new Error(msg);
  }
  return res.json();
}

async function apiPut(path, body) {
  const res = await fetch(API_BASE + path, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    let msg = `${res.status} ${res.statusText}`;
    try { const j = await res.json(); if (j.message) msg = j.message; } catch (_) {}
    throw new Error(msg);
  }
  return res.json();
}

async function apiDelete(path) {
  const res = await fetch(API_BASE + path, { method: 'DELETE' });
  if (!res.ok) {
    let msg = `${res.status} ${res.statusText}`;
    try { const j = await res.json(); if (j.message) msg = j.message; } catch (_) {}
    throw new Error(msg);
  }
  return true;
}

/** Format a lap time given in milliseconds as m:ss.mmm */
function formatLapTime(ms) {
  if (ms == null) return '—';
  const m = Math.floor(ms / 60000);
  const s = Math.floor((ms % 60000) / 1000);
  const millis = ms % 1000;
  return `${m}:${String(s).padStart(2, '0')}.${String(millis).padStart(3, '0')}`;
}

function showState(el, message, isError = false) {
  el.innerHTML = `<div class="state-msg${isError ? ' error' : ''}">${message}</div>`;
}
