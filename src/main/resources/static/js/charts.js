/* ============================================
   APEX F1 — tiny SVG chart toolkit
   Plain JavaScript, no external library.
     lineChart(host, options)   line / step / area charts with hover tooltip
     stintChart(host, rows, n)  tyre-strategy (stint analysis) bars
   ============================================ */

const COMPOUND_COLORS = {
  SOFT: '#ff4d4d',
  MEDIUM: '#f5c518',
  HARD: '#e8e8e8',
  INTERMEDIATE: '#3ec16d',
  WET: '#3d8bff',
};
const COMPOUND_TEXT = { SOFT: '#fff', MEDIUM: '#111', HARD: '#111', INTERMEDIATE: '#111', WET: '#fff' };

function chEsc(v) {
  return String(v ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/** "Nice" tick values between min and max (about `count` of them). */
function niceTicks(min, max, count) {
  const span = max - min || 1;
  const rough = span / Math.max(1, count);
  const pow = Math.pow(10, Math.floor(Math.log10(rough)));
  const frac = rough / pow;
  const step = (frac < 1.5 ? 1 : frac < 3 ? 2 : frac < 7 ? 5 : 10) * pow;
  const ticks = [];
  for (let v = Math.ceil(min / step) * step; v <= max + step * 0.001; v += step) {
    ticks.push(Math.round(v / step) * step);
  }
  return ticks;
}

/** index of the point whose x is closest to xv (points sorted by x) */
function nearestIndex(points, xv) {
  let lo = 0, hi = points.length - 1;
  while (hi - lo > 1) {
    const mid = (lo + hi) >> 1;
    if (points[mid].x < xv) lo = mid; else hi = mid;
  }
  return Math.abs(points[lo].x - xv) <= Math.abs(points[hi].x - xv) ? lo : hi;
}

/**
 * options: {
 *   series: [{ name, color, points:[{x,y,c?}], step?, area?, dots?, dash? }],
 *   height, yMin, yMax, xMin, xMax, invertY, yTicks, xTicks,
 *   xFormat, yFormat, xTipFormat, xLabel, vlines:[{x,label,color}]
 * }
 */
function lineChart(host, o) {
  const W = 900, H = o.height || 220;
  const m = { l: o.left || 58, r: 16, t: 12, b: 34 };
  const iw = W - m.l - m.r, ih = H - m.t - m.b;
  const series = (o.series || []).filter((s) => s.points && s.points.length);
  if (!series.length) {
    host.innerHTML = '<div class="state-msg">No data for this selection.</div>';
    return;
  }
  const xs = [], ys = [];
  series.forEach((s) => s.points.forEach((p) => { xs.push(p.x); ys.push(p.y); }));
  let xmin = o.xMin ?? Math.min(...xs), xmax = o.xMax ?? Math.max(...xs);
  let ymin = o.yMin ?? Math.min(...ys), ymax = o.yMax ?? Math.max(...ys);
  if (o.yMin === undefined && o.yMax === undefined) {
    const pad = (ymax - ymin || 1) * 0.08;
    ymin -= pad; ymax += pad;
  }
  if (ymax === ymin) { ymax += 1; ymin -= 1; }
  if (xmax === xmin) xmax += 1;

  const X = (x) => m.l + ((x - xmin) / (xmax - xmin)) * iw;
  const Y = (y) => {
    const f = (y - ymin) / (ymax - ymin);
    return o.invertY ? m.t + f * ih : m.t + (1 - f) * ih;
  };
  const yTicks = o.yTicks || niceTicks(ymin, ymax, 5);
  const xTicks = o.xTicks || niceTicks(xmin, xmax, 7);
  const yFmt = o.yFormat || ((v) => String(Math.round(v * 100) / 100));
  const xFmt = o.xFormat || ((v) => String(Math.round(v * 100) / 100));
  const xTipFmt = o.xTipFormat || xFmt;

  let svg = `<svg viewBox="0 0 ${W} ${H}" role="img" aria-label="${chEsc(o.label || 'chart')}">`;
  yTicks.forEach((t) => {
    svg += `<line class="ch-grid" x1="${m.l}" x2="${W - m.r}" y1="${Y(t).toFixed(1)}" y2="${Y(t).toFixed(1)}"/>`;
    svg += `<text class="ch-text" x="${m.l - 8}" y="${(Y(t) + 4).toFixed(1)}" text-anchor="end">${chEsc(yFmt(t))}</text>`;
  });
  xTicks.forEach((t) => {
    svg += `<text class="ch-text" x="${X(t).toFixed(1)}" y="${H - m.b + 16}" text-anchor="middle">${chEsc(xFmt(t))}</text>`;
  });
  svg += `<line class="ch-axis" x1="${m.l}" x2="${W - m.r}" y1="${H - m.b}" y2="${H - m.b}"/>`;
  if (o.xLabel) svg += `<text class="ch-title" x="${m.l + iw / 2}" y="${H - 4}" text-anchor="middle">${chEsc(o.xLabel)}</text>`;

  (o.vlines || []).forEach((v) => {
    if (v.x < xmin || v.x > xmax) return;
    svg += `<line x1="${X(v.x).toFixed(1)}" x2="${X(v.x).toFixed(1)}" y1="${m.t}" y2="${H - m.b}" stroke="${v.color || '#8a8a90'}" stroke-width="1" stroke-dasharray="4 4"/>`;
    if (v.label) svg += `<text class="ch-text" x="${(X(v.x) + 3).toFixed(1)}" y="${m.t + 10}">${chEsc(v.label)}</text>`;
  });

  series.forEach((s) => {
    const color = s.color || '#e10600';
    let d = '';
    s.points.forEach((p, i) => {
      const px = X(p.x).toFixed(1), py = Y(p.y).toFixed(1);
      const prev = s.points[i - 1];
      if (i === 0 || (s.gapX && prev && p.x - prev.x > s.gapX)) d += `${i === 0 ? '' : ' '}M${px},${py}`;
      else if (s.step) d += ` H${px} V${py}`;
      else d += ` L${px},${py}`;
    });
    if (s.area) {
      const first = s.points[0], last = s.points[s.points.length - 1];
      const base = Y(Math.max(ymin, 0)).toFixed(1);
      svg += `<path d="${d} L${X(last.x).toFixed(1)},${base} L${X(first.x).toFixed(1)},${base} Z" fill="${color}" fill-opacity="0.22" stroke="none"/>`;
    }
    svg += `<path d="${d}" fill="none" stroke="${color}" stroke-width="2" stroke-linejoin="round"${s.dash ? ' stroke-dasharray="6 4"' : ''}/>`;
    if (s.dots) {
      s.points.forEach((p) => {
        svg += `<circle cx="${X(p.x).toFixed(1)}" cy="${Y(p.y).toFixed(1)}" r="2.8" fill="${p.c || color}"/>`;
      });
    }
  });
  svg += `<line class="ch-cross" y1="${m.t}" y2="${H - m.b}" x1="0" x2="0"/>`;
  svg += '</svg>';

  const legend = series.some((s) => s.name)
    ? `<div class="chart-legend">${series.filter((s) => s.name).map((s) =>
        `<span><span class="legend-swatch" style="background:${s.color || '#e10600'}"></span>${chEsc(s.name)}</span>`).join('')}</div>`
    : '';
  host.innerHTML = `${legend}<div class="chart-wrap">${svg}<div class="chart-tip"></div></div>`;

  // ---- hover tooltip ----
  const svgEl = host.querySelector('svg');
  const tip = host.querySelector('.chart-tip');
  const cross = host.querySelector('.ch-cross');
  svgEl.addEventListener('mousemove', (ev) => {
    const r = svgEl.getBoundingClientRect();
    const px = ((ev.clientX - r.left) / r.width) * W;
    if (px < m.l || px > W - m.r) { tip.style.display = 'none'; cross.style.display = 'none'; return; }
    const xv = xmin + ((px - m.l) / iw) * (xmax - xmin);
    const base = series[0].points[nearestIndex(series[0].points, xv)];
    let html = `<b>${chEsc(xTipFmt(base.x))}</b>`;
    series.forEach((s) => {
      const p = s.points[nearestIndex(s.points, xv)];
      html += `<div><span class="legend-swatch" style="background:${s.color || '#e10600'}"></span>${s.name ? chEsc(s.name) + ': ' : ''}${chEsc(yFmt(p.y))}</div>`;
    });
    tip.innerHTML = html;
    tip.style.display = 'block';
    const frac = X(base.x) / W;
    tip.style.left = (frac * 100).toFixed(1) + '%';
    tip.style.transform = frac > 0.6 ? 'translateX(calc(-100% - 10px))' : 'translateX(10px)';
    cross.setAttribute('x1', X(base.x).toFixed(1));
    cross.setAttribute('x2', X(base.x).toFixed(1));
    cross.style.display = 'block';
  });
  svgEl.addEventListener('mouseleave', () => { tip.style.display = 'none'; cross.style.display = 'none'; });
}

/**
 * Tyre-strategy chart. rows: [{ label, color, stints:[{compound, startLap, endLap}] }]
 */
function stintChart(host, rows, totalLaps) {
  if (!rows.length) { host.innerHTML = '<div class="state-msg">No stint data.</div>'; return; }
  const W = 900, labelW = 70, rowH = 20, gap = 6, top = 6, bottom = 30, right = 12;
  const H = top + rows.length * (rowH + gap) + bottom;
  const X = (lap) => labelW + (lap / totalLaps) * (W - labelW - right);
  let svg = `<svg viewBox="0 0 ${W} ${H}" role="img" aria-label="Tyre strategy by driver">`;
  niceTicks(0, totalLaps, 8).forEach((t) => {
    svg += `<line class="ch-grid" x1="${X(t).toFixed(1)}" x2="${X(t).toFixed(1)}" y1="${top}" y2="${H - bottom}"/>`;
    svg += `<text class="ch-text" x="${X(t).toFixed(1)}" y="${H - bottom + 16}" text-anchor="middle">${t}</text>`;
  });
  svg += `<text class="ch-title" x="${labelW + (W - labelW - right) / 2}" y="${H - 4}" text-anchor="middle">Lap</text>`;
  rows.forEach((r, i) => {
    const y = top + i * (rowH + gap);
    svg += `<rect x="6" y="${y + 5}" width="9" height="9" rx="2" fill="${r.color || '#888'}"/>`;
    svg += `<text class="ch-text" x="22" y="${y + 14}" style="fill:var(--text-primary)">${chEsc(r.label)}</text>`;
    r.stints.forEach((s) => {
      const x1 = X(s.startLap - 1) + 1, x2 = X(s.endLap) - 1;
      const w = Math.max(2, x2 - x1);
      const laps = s.endLap - s.startLap + 1;
      svg += `<g><title>${chEsc(r.label)} · ${s.compound} · laps ${s.startLap}–${s.endLap} (${laps} laps)</title>`;
      svg += `<rect x="${x1.toFixed(1)}" y="${y}" width="${w.toFixed(1)}" height="${rowH}" rx="3" fill="${COMPOUND_COLORS[s.compound] || '#888'}"/>`;
      if (w > 26) svg += `<text x="${(x1 + w / 2).toFixed(1)}" y="${y + 14}" text-anchor="middle" font-size="11" font-family="var(--font-mono)" fill="${COMPOUND_TEXT[s.compound] || '#111'}">${laps}</text>`;
      svg += '</g>';
    });
  });
  svg += '</svg>';
  const legend = `<div class="chart-legend">${Object.keys(COMPOUND_COLORS).slice(0, 3).map((c) =>
    `<span><span class="cmp-dot" style="background:${COMPOUND_COLORS[c]}"></span>${c}</span>`).join('')}</div>`;
  host.innerHTML = `${legend}<div class="chart-wrap">${svg}</div>`;
}
