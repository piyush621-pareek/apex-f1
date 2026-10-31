/* ============================================
   APEX F1 — track map loader
   Loads an SVG track map into the page (inline, so we can draw on top of it)
   and finds the racing-line path: <path id="track">, or else the longest path.
   Includes automatic .png -> .svg recovery and an inline fallback circuit SVG
   so custom or newly added circuits always work in Race Sim.
   ============================================ */

const FALLBACK_TRACK_SVG = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 500" role="img" aria-label="Circuit track layout">
  <title>Grand Prix Circuit</title>
  <path d="M 180,380 L 600,380 C 670,380 700,330 680,270 C 660,210 590,220 550,180 C 510,140 540,95 470,90 L 260,90 C 190,90 150,140 170,200 C 190,260 250,250 230,305 C 210,350 130,380 180,380 Z"
        fill="none" stroke="#1f1f27" stroke-width="18" stroke-linejoin="round" stroke-linecap="round"/>
  <path id="track" d="M 180,380 L 600,380 C 670,380 700,330 680,270 C 660,210 590,220 550,180 C 510,140 540,95 470,90 L 260,90 C 190,90 150,140 170,200 C 190,260 250,250 230,305 C 210,350 130,380 180,380 Z"
        fill="none" stroke="#e10600" stroke-width="4" stroke-linejoin="round" stroke-linecap="round"/>
  <path d="M 380,366 L 380,394" stroke="#ffffff" stroke-width="4" stroke-linecap="butt"/>
</svg>`;

async function fetchTrackSvgText(url) {
  if (!url) return FALLBACK_TRACK_SVG;
  const candidates = [url];
  if (/\.png$/i.test(url)) {
    candidates.unshift(url.replace(/\.png$/i, '.svg'));
  }
  for (const candidate of candidates) {
    try {
      const res = await fetch(candidate);
      if (!res.ok) continue;
      let text = await res.text();
      text = text.replace(/<\?xml[^>]*\?>/i, '').replace(/<!DOCTYPE[^>]*>/i, '').trim();
      if (/^<svg[\s>]/i.test(text)) return text;
    } catch (_) {
      // try next candidate or fallback
    }
  }
  return FALLBACK_TRACK_SVG;
}

async function mountTrack(container, url) {
  const text = await fetchTrackSvgText(url);
  container.innerHTML = text;
  const svg = container.querySelector('svg');
  svg.classList.add('sim-track');
  svg.removeAttribute('width');
  svg.removeAttribute('height');

  let path = svg.querySelector('#track');
  if (!path) {
    let bestLen = -1;
    svg.querySelectorAll('path').forEach((p) => {
      const len = (p.getAttribute('d') || '').length;
      if (len > bestLen) { bestLen = len; path = p; }
    });
  }
  if (!path) throw new Error('no <path> found in the track SVG');

  const vb = (svg.getAttribute('viewBox') || '0 0 800 500').split(/[\s,]+/).map(Number);
  return {
    svg,
    path,
    parent: path.parentNode,          // markers are added here, so they share any transform
    length: path.getTotalLength(),
    unit: (vb[2] || 800) / 800,       // marker size scales with the map's coordinate system
  };
}
