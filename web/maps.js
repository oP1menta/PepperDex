// Maps: an endless grid of blocks. The Master paints and shows a map to the session; players look at it and drag to move around.
// A cell = ground (grass, water, cave) + something on top (road, elevation, wall). v = ground | top << 3.
const MAP = { on: false, cv: null, cells: new Map(), id: 0, v: 0, name: '', cam: { x: 6, y: 4, z: 40 }, tool: 'hand', maps: [], timer: 0, raf: 0,
  tokens: [], tpos: {}, dragTok: null, me: 0, imgs: {}, wild: [], tick: 0, pts: new Map(), strokes: [], brush: 1, undo: [], cur: null, hover: null, flush: 0, drag: null, pinch: 0, dirty: true };
const mapIsMaster = () => !!document.getElementById('tab-players');
const mapKey = (x, y) => x + ',' + y;

function mapSync(tab) {
  const here = tab === 'maps' || tab === 'map';
  MAP.on = here;
  clearInterval(MAP.timer); clearInterval(MAP.wildTimer); cancelAnimationFrame(MAP.raf);
  if (!here) return;
  MAP.cv = document.getElementById('mapCanvas');
  if (!MAP.cv.dataset.ready) mapBind();
  if (mapIsMaster()) mapLoadList();
  mapPoll();
  MAP.timer = setInterval(mapPoll, 500);
  if (mapIsMaster()) { mapLoadWild(); clearInterval(MAP.wildTimer); MAP.wildTimer = setInterval(mapLoadWild, 4000); }
  mapRenderMine();
  mapDraw();
}

// ---------- data ----------
async function mapPoll() {
  if (MAP.strokes.length || MAP.drag && MAP.drag.paint) return;                       // never overwrite a stroke in progress
  let url;
  if (mapIsMaster()) { if (!MAP.id) return; url = '/api/maps?action=get&id=' + MAP.id + '&since=' + MAP.v; }
  else url = '/api/map?' + new URLSearchParams({ ...auth(), v: MAP.v });
  let j;
  try { const r = await fetch(url); if (!r.ok) return; j = await r.json(); } catch (e) { return; }
  if (j.me) MAP.me = j.me;
  if (!j.id) { if (MAP.id && !mapIsMaster()) { MAP.id = 0; MAP.cells.clear(); MAP.tokens = []; MAP.v = 0; MAP.dirty = true; mapStatus(); } return; }
  if (j.tokens && !MAP.dragTok) { MAP.tokens = j.tokens; mapRenderMine(); }
  if (j.same && j.id === MAP.id) return;
  const first = j.id !== MAP.id;
  MAP.id = j.id; MAP.v = j.v; MAP.name = j.name;
  if (j.cells !== undefined) mapLoadCells(j.cells);
  if (first) mapCenter();
  MAP.dirty = true; mapStatus();
}
function mapLoadCells(s) {
  MAP.cells.clear();
  if (s) for (const c of s.split(';')) { const [x, y, v] = c.split(','); MAP.cells.set(mapKey(+x, +y), +v); }
}
function mapCenter() {
  if (!MAP.cells.size) { MAP.cam.x = 0; MAP.cam.y = 0; return; }
  let x0 = 1e9, x1 = -1e9, y0 = 1e9, y1 = -1e9;
  for (const k of MAP.cells.keys()) { const [x, y] = k.split(',').map(Number); x0 = Math.min(x0, x); x1 = Math.max(x1, x); y0 = Math.min(y0, y); y1 = Math.max(y1, y); }
  MAP.cam.x = (x0 + x1 + 1) / 2; MAP.cam.y = (y0 + y1 + 1) / 2;
  MAP.cam.z = Math.max(14, Math.min(48, Math.min(MAP.cv.clientWidth / (x1 - x0 + 3), MAP.cv.clientHeight / (y1 - y0 + 3))));   // the whole map fits at first
}
function mapStatus() {
  const el = document.getElementById('mapStatus');
  if (el) el.textContent = MAP.id ? MAP.name : t('mapNone');
}

// ---------- master: list of maps ----------
async function mapApi(action, extra = {}) {
  const r = await fetch('/api/maps', { method: 'POST', body: new URLSearchParams({ action, code: document.getElementById('spawnSession').value || '', ...extra }) });
  if (!r.ok) return null;
  const j = await r.json(); MAP.maps = j.maps; mapRenderList(); return j;
}
async function mapLoadList() { await mapApi('list'); if (!MAP.id && MAP.maps.length) mapSelect(MAP.maps[0].id); }
function mapRenderList() {
  const box = document.getElementById('mapList'); if (!box) return;
  box.innerHTML = '<button class="mapplus" title="' + esc(t('mapNew')) + '" onclick="mapCreate()">+</button>' +
    MAP.maps.map(m => '<button class="mapbtn' + (m.id === MAP.id ? ' on' : '') + (m.shown ? ' shown' : '') + '" title="' + esc(m.name) + '" onclick="mapSelect(' + m.id + ')">' + esc(m.name.slice(0, 10)) + (m.shown ? ' ▶' : '') + '</button>').join('');
  const cur = MAP.maps.find(m => m.id === MAP.id), sh = document.getElementById('mapShowBtn');
  if (sh) { sh.textContent = cur && cur.shown ? t('mapHide') : t('mapShow'); sh.disabled = !cur; }
}
async function mapCreate() {
  const name = await askConfirm(t('mapNamePrompt'), true);
  if (name === null) return;
  const j = await mapApi('create', { name });
  if (j) mapSelect(j.id);
}
async function mapSelect(id) { MAP.id = id; MAP.v = 0; await mapPoll(); mapRenderList(); }
async function mapToggleShow() { const cur = MAP.maps.find(m => m.id === MAP.id); if (cur) await mapApi('show', { id: cur.shown ? 0 : cur.id }); }
async function mapRename() { const cur = MAP.maps.find(m => m.id === MAP.id); if (!cur) return; const n = await askConfirm(t('mapNamePrompt'), true); if (n) { await mapApi('rename', { id: cur.id, name: n }); MAP.v = 0; mapPoll(); } }
async function mapDelete() {
  const cur = MAP.maps.find(m => m.id === MAP.id); if (!cur) return;
  if (!await askConfirm(t('mapDeleteAsk', cur.name))) return;
  await mapApi('delete', { id: cur.id }); MAP.id = 0; MAP.cells.clear(); MAP.v = 0; MAP.dirty = true;
  if (MAP.maps.length) mapSelect(MAP.maps[0].id); else { mapStatus(); mapRenderList(); }
}
// tools with variants: how many, and the text key of their names (comma separated in prefs.js)
const MAP_VARS = { grass: ['mvGrass', 4], water: ['mvWater', 4], cave: ['mvCave', 3], sand: ['mvSand', 3], snow: ['mvSnow', 2], dirt: ['mvDirt', 3], road: ['mvRoad', 4], wall: ['mvWall', 5], house: ['mvHouse', 6], tree: ['mvTree', 6], elev: ['mvElev', 4] };
MAP.vsel = {};                                                                      // the variant picked for each tool
const mapEff = () => MAP_VARS[MAP.tool] ? MAP.tool + ':' + (MAP.vsel[MAP.tool] || (MAP.tool === 'elev' ? 1 : 0)) : MAP.tool;
function mapTool(name) {
  MAP.tool = name;
  document.querySelectorAll('#mapTools button').forEach(b => b.classList.toggle('on', b.dataset.tool === name));
  mapRenderVars();
}
function mapVar(i) { MAP.vsel[MAP.tool] = i; mapRenderVars(); }
// a little picture of one block, drawn with the real tile code
function mapPreview(tool, i) {
  const c = document.createElement('canvas'), S = 40, dpr = window.devicePixelRatio || 1; c.width = c.height = S * dpr; c.style.width = c.style.height = S + 'px';
  const g = c.getContext('2d'); g.setTransform(dpr, 0, 0, dpr, 0, 0); g.fillStyle = '#10161d'; g.fillRect(0, 0, S, S);
  const bi = MAP_BASE.indexOf(tool), ti = MAP_TOP.indexOf(tool);
  const v = bi > 0 ? bi | i << 8 : ti > 0 ? 1 | ti << 3 | i << 11 : 1 | (tool === 'elev' ? i << 14 : 0);
  const keep = MAP.cells, X = 900, Y = 900; MAP.cells = new Map([[mapKey(X, Y), v]]);
  if (tool === 'elev') MAP.cells.set(mapKey(X, Y - 1), 0);
  g.save(); g.beginPath(); g.rect(0, 0, S, S); g.clip(); mapTile(g, X, Y, 0, 0, S, v, 0); g.restore(); MAP.cells = keep;
  return c;
}
function mapRenderVars() {
  const box = document.getElementById('mapVars'); if (!box) return;
  const d = MAP_VARS[MAP.tool]; box.innerHTML = ''; box.hidden = !d; if (!d) return;
  const names = t(d[0]).split(','), cur = MAP.vsel[MAP.tool] || (MAP.tool === 'elev' ? 1 : 0);
  for (let i = MAP.tool === 'elev' ? 1 : 0; i < (MAP.tool === 'elev' ? 5 : d[1]); i++) {
    const b = document.createElement('button'); b.className = 'mapvar' + (i === cur ? ' on' : ''); b.title = names[MAP.tool === 'elev' ? i - 1 : i] || '';
    b.onclick = () => mapVar(i); b.appendChild(mapPreview(MAP.tool, i));
    const sp = document.createElement('span'); sp.textContent = names[MAP.tool === 'elev' ? i - 1 : i] || ''; b.appendChild(sp); box.appendChild(b);
  }
}

// ---------- input ----------
const mapZoomTo = (z, px, py) => {
  const c = MAP.cam, before = mapToWorld(px, py);
  c.z = Math.max(8, Math.min(120, z));
  const after = mapToWorld(px, py); c.x += before[0] - after[0]; c.y += before[1] - after[1]; MAP.dirty = true;
};
const mapToWorld = (px, py) => [MAP.cam.x + (px - MAP.cv.clientWidth / 2) / MAP.cam.z, MAP.cam.y + (py - MAP.cv.clientHeight / 2) / MAP.cam.z];

function mapBind() {
  const cv = MAP.cv; cv.dataset.ready = 1;
  const pos = e => { const r = cv.getBoundingClientRect(); return [e.clientX - r.left, e.clientY - r.top]; };
  cv.addEventListener('pointerdown', e => {
    e.preventDefault(); cv.setPointerCapture(e.pointerId);
    MAP.pts.set(e.pointerId, pos(e));
    if (MAP.pts.size === 2) { MAP.pinch = mapPinchDist(); MAP.drag = null; return; }
    const [px, py] = pos(e);
    const tk = (!mapIsMaster() || MAP.tool === 'hand') && e.button === 0 ? mapTokenAt(px, py) : null;      // a piece under the finger: carry it
    if (tk && (mapIsMaster() || tk.o === MAP.me)) { const [wx, wy] = mapToWorld(px, py); const n = mapN(tk); MAP.dragTok = { id: tk.id, wx, wy, n, off: [wx - (tk.x + n / 2), wy - (tk.y + n / 2)], cell: [tk.x, tk.y], sent: 0 }; MAP.drag = null; return; }
    const paint = mapIsMaster() && MAP.tool !== 'hand' && e.button === 0 && MAP.id;
    MAP.drag = { px, py, cx: MAP.cam.x, cy: MAP.cam.y, paint: !!paint };
    if (paint) { MAP.strokes = []; MAP.cur = new Map(); mapPaintAt(px, py); }
  });
  cv.addEventListener('pointermove', e => {
    if (mapIsMaster() && MAP.tool !== 'hand') { const [hx, hy] = mapToWorld(...pos(e)); MAP.hover = [Math.floor(hx), Math.floor(hy)]; MAP.dirty = true; }
    if (!MAP.pts.has(e.pointerId)) return;
    MAP.pts.set(e.pointerId, pos(e));
    if (MAP.pts.size === 2) {                                                   // two fingers: pinch to zoom
      const d = mapPinchDist(), pts = [...MAP.pts.values()];
      if (MAP.pinch) mapZoomTo(MAP.cam.z * d / MAP.pinch, (pts[0][0] + pts[1][0]) / 2, (pts[0][1] + pts[1][1]) / 2);
      MAP.pinch = d; return;
    }
    if (MAP.dragTok) { const [wx, wy] = mapToWorld(...pos(e)), d = MAP.dragTok; d.wx = wx; d.wy = wy; const c = mapDragCell(d); if ((c[0] !== d.cell[0] || c[1] !== d.cell[1]) && performance.now() - d.sent > 120) mapTokenMove(d, c); MAP.dirty = true; return; }
    if (!MAP.drag) return;
    const [px, py] = pos(e);
    if (MAP.drag.paint) mapPaintAt(px, py);
    else { MAP.cam.x = MAP.drag.cx - (px - MAP.drag.px) / MAP.cam.z; MAP.cam.y = MAP.drag.cy - (py - MAP.drag.py) / MAP.cam.z; MAP.dirty = true; }
  });
  const end = e => {
    MAP.pts.delete(e.pointerId); MAP.pinch = 0;
    if (MAP.dragTok) { const d = MAP.dragTok, c = mapDragCell(d); mapTokenMove(d, c); MAP.dragTok = null; MAP.dirty = true; return; }
    if (MAP.drag && MAP.drag.paint) { mapFlush(); if (MAP.cur && MAP.cur.size) { MAP.undo.push({ id: MAP.id, cells: MAP.cur }); if (MAP.undo.length > 40) MAP.undo.shift(); } MAP.cur = null; }
    if (!MAP.pts.size) MAP.drag = null;
  };
  cv.addEventListener('pointerup', end); cv.addEventListener('pointercancel', end);
  cv.addEventListener('wheel', e => { e.preventDefault(); const [px, py] = pos(e); mapZoomTo(MAP.cam.z * (e.deltaY < 0 ? 1.12 : 1 / 1.12), px, py); }, { passive: false });
  cv.addEventListener('pointerleave', () => { MAP.hover = null; MAP.dirty = true; });
  cv.addEventListener('contextmenu', e => e.preventDefault());
  document.addEventListener('keydown', e => { if (MAP.on && mapIsMaster() && (e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z' && !/INPUT|TEXTAREA/.test((document.activeElement || {}).tagName || '')) { e.preventDefault(); mapUndo(); } });
}
const mapPinchDist = () => { const p = [...MAP.pts.values()]; return p.length < 2 ? 0 : Math.hypot(p[0][0] - p[1][0], p[0][1] - p[1][1]); };

// paint under the finger and every cell between the last point and this one
function mapPaintAt(px, py) {
  const [wx, wy] = mapToWorld(px, py), x = Math.floor(wx), y = Math.floor(wy), d = MAP.drag;
  const from = d.last || [x, y];
  const n = Math.max(Math.abs(x - from[0]), Math.abs(y - from[1])) || 1;
  const r = (MAP.brush - 1) / 2;
  for (let i = 1; i <= n; i++) {
    const cx = Math.round(from[0] + (x - from[0]) * i / n), cy = Math.round(from[1] + (y - from[1]) * i / n);
    for (let dx = -r; dx <= r; dx++) for (let dy = -r; dy <= r; dy++) mapApply(cx + dx, cy + dy);
  }
  d.last = [x, y]; MAP.dirty = true;
}
function mapApply(x, y) {
  const k = mapKey(x, y), [tool, vr] = mapEff().split(':'), n = +vr || 0, old = MAP.cells.get(k) || 0;
  if (MAP.cur && !MAP.cur.has(k)) MAP.cur.set(k, MAP.cells.has(k) ? old : -1);   // for undo
  let b = old & 7, tp = (old >> 3) & 7, bv = mapBv(old), tv = mapTv(old), lv = mapLv(old);
  const bi = MAP_BASE.indexOf(tool), ti = MAP_TOP.indexOf(tool);
  if (tool === 'erase') { MAP.cells.delete(k); }
  else {
    if (bi > 0) { b = bi; bv = n; } else if (ti > 0) { tp = ti; tv = n; } else if (tool === 'bare') { tp = 0; tv = 0; } else if (tool === "elev") lv = Math.max(1, Math.min(4, n)); else if (tool === 'flat') lv = 0; else return;
    if (!b && !tp && !lv) MAP.cells.delete(k); else MAP.cells.set(k, b | tp << 3 | bv << 8 | tv << 11 | lv << 14);
  }
  MAP.strokes.push(x, y);
  if (MAP.strokes.length >= 600) mapFlush();
}
async function mapUndo() {
  const u = MAP.undo.pop(); if (!u || u.id !== MAP.id) return;
  const out = [];
  for (const [k, v] of u.cells) { const [x, y] = k.split(',').map(Number); out.push(x, y, v); if (v < 0) MAP.cells.delete(k); else MAP.cells.set(k, v); }
  MAP.dirty = true;
  await fetch('/api/maps', { method: 'POST', body: new URLSearchParams({ action: 'restore', id: MAP.id, cells: out.join(',') }) });
  MAP.v = 0;
}
function mapBrush(n) {
  MAP.brush = n;
  document.querySelectorAll('#mapBrush button').forEach(b => b.classList.toggle('on', +b.dataset.n === n));
}
async function mapFlush() {
  if (!MAP.strokes.length) return;
  const cells = MAP.strokes.join(','); MAP.strokes = [];
  await fetch('/api/maps', { method: 'POST', body: new URLSearchParams({ action: 'paint', id: MAP.id, tool: mapEff(), cells }) });
  MAP.v = 0;                                                                     // the next poll takes the truth from the server
}

// ---------- drawing ----------
// random-looking but fixed per cell (never per pixel, or the grass would shimmer while moving)
const MAP_HASH = (x, y, n) => { let h = (x * 374761393 + y * 668265263 + n * 2147483647) | 0; h = (h ^ (h >>> 13)) * 1274126177; return ((h ^ (h >>> 16)) >>> 0) / 4294967295; };

// ---------- block looks: every ground / top has variants (the number is stored in the cell, see MapServices) ----------
const MAP_BASE = ['', 'grass', 'water', 'cave', 'sand', 'snow', 'dirt'], MAP_TOP = ['', 'road', '', 'wall', 'house', 'tree'];
const mapBv = v => (v >> 8) & 7, mapTv = v => (v >> 11) & 7, mapLv = v => (v >> 14) & 7;     // ground variant, top variant, elevation level 0-4
const MAP_MIXC = {};
const mapMix = (a, b, t) => {                                                      // blend two #rrggbb colours (t = how much of b)
  const k = a + b + t; if (MAP_MIXC[k]) return MAP_MIXC[k];
  const p = c => [1, 3, 5].map(i => parseInt(c.slice(i, i + 2), 16));
  const A = p(a), B = p(b); return MAP_MIXC[k] = '#' + A.map((x, i) => Math.round(x + (B[i] - x) * t).toString(16).padStart(2, '0')).join('');
};
const MAP_GRASS = [
  { c: ['#5db24a', '#63b84f', '#58ab46'], hi: '#68bd54', tuft: '#4a9a3b', fl: 0.12, fc: ['#f4e46a', '#ffffff', '#f29ac2'] },
  { c: ['#5db24a', '#63b84f', '#58ab46'], hi: '#68bd54', tuft: '#4a9a3b', fl: 0.55, fc: ['#f4e46a', '#ffffff', '#f29ac2', '#c58bff', '#ff9f43'] },
  { c: ['#b9b255', '#c2ba5c', '#aea84c'], hi: '#cdc76a', tuft: '#968f3a', fl: 0.04, fc: ['#e8c04a'] },
  { c: ['#3f8a3a', '#459440', '#397d34'], hi: '#4a9c45', tuft: '#2d6a2a', fl: 0.03, fc: ['#e9f1d0'] },
];
const MAP_SPECK = {   // sand, snow, dirt: a base colour with darker / lighter specks
  4: [{ c: '#e6d79e', s1: '#d3c286', s2: '#f2e7b9' }, { c: '#dcae62', s1: '#c4954b', s2: '#e9c481' }, { c: '#9d9486', s1: '#807868', s2: '#b3ab9d' }],
  5: [{ c: '#f1f6fa', s1: '#d5e2ee', s2: '#ffffff' }, { c: '#bfe1f1', s1: '#9ccbe0', s2: '#e2f4fb', ice: 1 }],
  6: [{ c: '#9a7448', s1: '#7e5c37', s2: '#b08858' }, { c: '#6a5139', s1: '#4f3b28', s2: '#85694b', mud: 1 }, { c: '#b4613d', s1: '#964c2c', s2: '#c97a53' }],
};
const MAP_CAVE = [
  { c: '#5a5048', s1: '#4a423b', s2: '#6b6058', crack: '#3e3731', rim: '#2f2a26' },
  { c: '#8d8a85', s1: '#74716c', s2: '#a3a09a', crack: '#5e5b57', rim: '#4a4845' },
  { c: '#3b3033', s1: '#2b2224', s2: '#4d4144', crack: '#e2612a', rim: '#1c1618' },
];
const MAP_WATER = [
  { sh: '#3f8ddf', dp: '#2f78c8', wave: '#9ccdf7', foam: '#d9efff' },
  { sh: '#2a6fba', dp: '#1d5599', wave: '#7fb1e3', foam: '#bcd8f2' },
  { sh: '#6b8a3e', dp: '#58762f', wave: '#8fb35a', foam: '#a9c97a' },
  { sh: '#e0551f', dp: '#c9440f', wave: '#ffb347', foam: '#ffd27a' },
];
const MAP_ROCK = { 1: '#8a6240', 3: '#5d544d', 4: '#a8864f', 5: '#8fa6b8', 6: '#7a5738' };      // cliff colour by ground
const MAP_ROAD = [   // outer, inner, pebbles, then the bridge: planks, deck, rails
  { o: '#a98650', i: '#d4b57a', p: '#c4a366', bd: '#8a5a2b', bp: '#a97440', br: '#5b3a1a' },
  { o: '#6f7078', i: '#a3a4ab', p: '#8b8c93', bd: '#6a6c74', bp: '#9a9ca4', br: '#4c4e55', cob: 1 },
  { o: '#8e3f2e', i: '#c0644a', p: '#a84f3a', bd: '#7a3526', bp: '#b45a44', br: '#5f2a20', cob: 1 },
  { o: '#33353b', i: '#4f525a', p: '#62656d', bd: '#2a2c31', bp: '#444750', br: '#1c1d21' },
];
const MAP_WALL = ['#8b9099', '#b0583f', '#9b6b3a', '#3f8a3a', '#9fc4d8'];             // stone, brick, wood, hedge, ice
const MAP_ROOF = [['#b94a3b', '#ece0c4'], ['#3f6fb5', '#ece0c4'], ['#4a9a52', '#ece0c4'], ['#7a5a36', '#b58b55'], ['#6f7580', '#d6d6d8'], ['#d4b24a', '#f0e6c8']];   // roof, wall

const mapLvTop = (ground, lv) => mapMix(ground, ['', '#9fd070', '#c4c58c', '#aaa59c', '#f3f6f9'][lv], 0.55);   // higher = greyer rock, the top is snow
// One cell. It looks at its four neighbours, so roads join up, water has a shore, plateaus have cliffs and rims.
function mapTile(g, cx, cy, x, y, s, v, now) {
  const b = v & 7, tp = (v >> 3) & 7, bv = mapBv(v), tv = mapTv(v), lv = mapLv(v), w = Math.ceil(s) + 1, px = Math.round(x), py = Math.round(y), u = s / 16;
  const at = (dx, dy) => MAP.cells.get(mapKey(cx + dx, cy + dy)) || 0;
  const nb = [at(0, -1), at(1, 0), at(0, 1), at(-1, 0)];                          // N E S W
  const rect = (c, rx, ry, rw, rh) => { g.fillStyle = c; g.fillRect(px + rx * s, py + ry * s, rw * s, rh * s); };
  const hs = n => MAP_HASH(cx, cy, n);
  const land = q => { const k = q & 7; return k !== 0 && k !== 2; };
  let ground = '#5db24a';                                                          // the colour the plateau top takes from the ground below

  // ---- ground ----
  if (b === 1) {
    const G = MAP_GRASS[bv] || MAP_GRASS[0]; ground = G.c[0];
    const patch = MAP_HASH(cx >> 2, cy >> 2, 7);                                      // big soft patches, so a large lawn is not one flat green
    rect(patch < 0.33 ? G.c[0] : patch < 0.66 ? G.c[1] : G.c[2], 0, 0, 1, 1);
    if (hs(1) < 0.5) rect(G.hi, hs(2) * 0.6, hs(3) * 0.6, 0.35, 0.3);
    g.fillStyle = G.tuft;
    for (let i = 0; i < 3; i++) { const bx = px + (0.1 + hs(10 + i) * 0.78) * s, by = py + (0.1 + hs(20 + i) * 0.7) * s; g.fillRect(bx, by, Math.max(1, u * 1.2), Math.max(2, u * 3)); g.fillRect(bx + u * 1.6, by + u, Math.max(1, u * 1.2), Math.max(2, u * 2.4)); }
    const nf = bv === 1 ? 3 : 1;
    for (let i = 0; i < nf; i++) if (hs(30 + i * 5) < G.fl) { g.fillStyle = G.fc[Math.floor(hs(33 + i * 5) * G.fc.length)]; g.fillRect(px + hs(31 + i * 5) * s * 0.8 + u, py + hs(32 + i * 5) * s * 0.8 + u, Math.max(2, u * 2), Math.max(2, u * 2)); }
  } else if (b >= 4) {
    const G = MAP_SPECK[b][bv] || MAP_SPECK[b][0]; ground = G.c;
    rect(G.c, 0, 0, 1, 1);
    g.fillStyle = G.s1; for (let i = 0; i < 4; i++) g.fillRect(px + hs(40 + i) * s * 0.85, py + hs(50 + i) * s * 0.85, Math.max(2, u * (1 + hs(60 + i) * 1.6)), Math.max(2, u * 1.1));
    g.fillStyle = G.s2; for (let i = 0; i < 3; i++) g.fillRect(px + hs(70 + i) * s * 0.85, py + hs(75 + i) * s * 0.85, Math.max(2, u * 1.4), Math.max(1, u * 0.9));
    if (G.ice) { g.strokeStyle = '#ffffffaa'; g.lineWidth = Math.max(1, u * 0.7); g.beginPath(); const kx = px + hs(80) * s * 0.5, ky = py + hs(81) * s * 0.6; g.moveTo(kx, ky); g.lineTo(kx + u * 4, ky + u * 3); g.lineTo(kx + u * 8, ky + u * 2); g.stroke(); }
    if (G.mud && hs(82) < 0.5) { g.fillStyle = '#4f3b28'; g.beginPath(); g.ellipse(px + (0.3 + hs(83) * 0.4) * s, py + (0.3 + hs(84) * 0.4) * s, u * 3.2, u * 1.8, 0, 0, 6.3); g.fill(); g.fillStyle = '#7d94a8'; g.fillRect(px + (0.3 + hs(83) * 0.4) * s - u, py + (0.3 + hs(84) * 0.4) * s - u * 0.6, u * 1.6, Math.max(1, u * 0.7)); }
    if (!nb[2]) { rect('#7a5a3a', 0, 1, 1, 0.22); rect('#5e4429', 0, 1.14, 1, 0.08); }
  } else if (b === 2) {
    const W = MAP_WATER[bv] || MAP_WATER[0], deep = nb.every(q => (q & 7) === 2);       // cells away from the shore are darker
    rect(deep ? W.dp : W.sh, 0, 0, 1, 1);
    g.strokeStyle = W.wave; g.lineWidth = Math.max(1, u * 0.9); g.lineCap = 'round';
    const ph = now / (bv === 3 ? 1500 : 900) + (cx * 0.8 + cy * 0.5);
    for (let r = 0; r < 2; r++) { const yy = py + (0.3 + r * 0.4) * s + Math.sin(ph + r * 2) * u * 0.8, xx = px + (0.1 + hs(5 + r) * 0.2) * s; g.beginPath(); g.moveTo(xx, yy); g.quadraticCurveTo(xx + s * 0.2, yy - u * 1.6, xx + s * 0.4, yy); g.quadraticCurveTo(xx + s * 0.6, yy + u * 1.6, xx + s * 0.7, yy); g.stroke(); }
    if (bv === 2) { g.fillStyle = '#9ccb5a'; for (let i = 0; i < 2; i++) { g.beginPath(); g.ellipse(px + (0.2 + hs(90 + i) * 0.6) * s, py + (0.2 + hs(92 + i) * 0.6) * s, u * 2.2, u * 1.3, 0, 0, 6.3); g.fill(); } }
    g.fillStyle = W.foam; const f = Math.max(2, u * 1.4);                           // foam where water meets land
    if (land(nb[0])) g.fillRect(px, py, w, f); if (land(nb[2])) g.fillRect(px, py + s - f, w, f);
    if (land(nb[3])) g.fillRect(px, py, f, w); if (land(nb[1])) g.fillRect(px + s - f, py, f, w);
  } else if (b === 3) {
    const C = MAP_CAVE[bv] || MAP_CAVE[0]; ground = C.c;
    rect(C.c, 0, 0, 1, 1);
    g.fillStyle = C.s1; for (let i = 0; i < 4; i++) g.fillRect(px + hs(40 + i) * s * 0.8, py + hs(50 + i) * s * 0.8, Math.max(2, u * (1.5 + hs(60 + i) * 2)), Math.max(2, u * 1.6));
    g.strokeStyle = C.crack; g.lineWidth = Math.max(1, u * (bv === 2 ? 0.9 : 0.6)); g.beginPath(); const kx = px + hs(72) * s * 0.6 + u, ky = py + hs(73) * s * 0.6 + u; g.moveTo(kx, ky); g.lineTo(kx + u * 3, ky + u * 2); g.lineTo(kx + u * 5, ky + u * 1); g.stroke();   // a crack
    g.fillStyle = C.s2; g.fillRect(px + hs(70) * s * 0.7, py + hs(71) * s * 0.7, Math.max(2, u * 2), Math.max(2, u));
    g.fillStyle = C.rim; const e = Math.max(2, u * 1.4);                            // dark rim where the cave floor ends
    if (!((nb[0] & 7) === 3)) g.fillRect(px, py, w, e); if (!((nb[2] & 7) === 3)) g.fillRect(px, py + s - e, w, e);
    if (!((nb[3] & 7) === 3)) g.fillRect(px, py, e, w); if (!((nb[1] & 7) === 3)) g.fillRect(px + s - e, py, e, w);
  }

  // ---- elevation: levels 1-4. The top face is a whole cell; the cliff hangs on the cell to the SOUTH, as tall as the drop ----
  if (lv) {
    const top = mapLvTop(ground, lv), e = Math.max(2, u * 1.6);
    rect(top, 0, 0, 1, 1);
    g.fillStyle = mapMix(top, '#000000', 0.1); g.fillRect(px + hs(90) * s * 0.6, py + hs(91) * s * 0.6, s * 0.3, s * 0.18);
    g.fillStyle = mapMix(top, '#ffffff', 0.18); for (let i = 0; i < 2; i++) g.fillRect(px + (0.1 + hs(92 + i) * 0.75) * s, py + (0.1 + hs(94 + i) * 0.7) * s, Math.max(1, u), Math.max(2, u * 2.4));
    g.fillStyle = mapMix(top, '#ffffff', 0.4); if (mapLv(nb[0]) < lv) g.fillRect(px, py, w, e); if (mapLv(nb[3]) < lv) g.fillRect(px, py, e, w);
    g.fillStyle = mapMix(top, '#000000', 0.3); if (mapLv(nb[1]) < lv) g.fillRect(px + s - e, py, e, w);
  }
  const drop = mapLv(nb[0]) - lv;
  if (drop > 0) {                                                                  // under a higher cell: its cliff face
    const CH = 0.36, fh = CH * Math.min(drop, 2.5), rock = MAP_ROCK[b] || MAP_ROCK[1], e = Math.max(2, u * 1.2);
    const upper = mapLvTop(ground, mapLv(nb[0]));
    rect(rock, 0, 0, 1, fh); rect(upper, 0, 0, 1, 0.06); rect(mapMix(rock, '#000000', 0.22), 0, 0.05, 1, 0.05);
    for (let r = 1; r <= drop && r <= 2; r++) rect(mapMix(rock, '#000000', 0.12), 0, CH * r - 0.16, 1, 0.05);
    g.fillStyle = mapMix(rock, '#ffffff', 0.25); for (let r = 0; r < Math.min(drop, 3); r++) { g.fillRect(px + hs(96 + r) * s * 0.7, py + (0.11 + CH * r) * s, s * 0.2, Math.max(1, u * 0.8)); g.fillRect(px + hs(97 + r) * s * 0.7, py + (0.26 + CH * r) * s, s * 0.25, Math.max(1, u * 0.8)); }
    g.strokeStyle = mapMix(rock, '#000000', 0.4); g.lineWidth = Math.max(1, u * 0.7); g.beginPath(); for (let i = 0; i < 2 * Math.min(drop, 3) && i < 5; i++) { const cxk = px + (0.2 + hs(98 + i) * 0.6) * s, y0 = py + (0.1 + CH * Math.floor(i / 2)) * s; g.moveTo(cxk, y0); g.lineTo(cxk + u * (hs(100 + i) - 0.5) * 3, y0 + 0.2 * s); } g.stroke();   // cracks in the rock
    g.fillStyle = mapMix(rock, '#000000', 0.55);                                    // the sides, where the cliff ends
    if (!(mapLv(at(-1, -1)) > mapLv(nb[3]))) g.fillRect(px, py, e, fh * s + 1); if (!(mapLv(at(1, -1)) > mapLv(nb[1]))) g.fillRect(px + s - e, py, e, fh * s + 1);
    g.fillStyle = '#0003'; g.fillRect(px, py + fh * s, w, Math.max(2, u * 1.6));
  }

  // ---- on top ----
  if (tp === 1) {
    const R = MAP_ROAD[tv] || MAP_ROAD[0];
    const rN = ((nb[0] >> 3) & 7) === 1, rE = ((nb[1] >> 3) & 7) === 1, rS = ((nb[2] >> 3) & 7) === 1, rW = ((nb[3] >> 3) & 7) === 1;
    const gap = !lv && ((mapLv(nb[1]) > 0 && mapLv(nb[3]) > 0) || (mapLv(nb[0]) > 0 && mapLv(nb[2]) > 0));   // low cell between two plateaus
    if ((land(v) || lv) && !gap) {                                                 // a road: a path that joins its neighbours
      const draw = (c, i) => {
        g.fillStyle = c;
        g.fillRect(px + (0.16 + i) * s, py + (0.16 + i) * s, (0.68 - 2 * i) * s, (0.68 - 2 * i) * s);
        if (rN) g.fillRect(px + (0.16 + i) * s, py, (0.68 - 2 * i) * s, 0.2 * s); if (rS) g.fillRect(px + (0.16 + i) * s, py + 0.8 * s, (0.68 - 2 * i) * s, 0.2 * s + 1);
        if (rW) g.fillRect(px, py + (0.16 + i) * s, 0.2 * s, (0.68 - 2 * i) * s); if (rE) g.fillRect(px + 0.8 * s, py + (0.16 + i) * s, 0.2 * s + 1, (0.68 - 2 * i) * s);
      };
      draw(R.o, 0); draw(R.i, 0.05);
      if (R.cob) {                                                                 // stones / bricks
        g.strokeStyle = R.o; g.lineWidth = Math.max(1, u * 0.6); g.beginPath();
        for (let r = 1; r < 4; r++) { g.moveTo(px + 0.2 * s, py + (0.16 + 0.17 * r) * s); g.lineTo(px + 0.8 * s, py + (0.16 + 0.17 * r) * s); }
        for (let r = 0; r < 4; r++) { const xx = px + (0.2 + ((r + cx + cy) & 1 ? 0.2 : 0.4)) * s; g.moveTo(xx, py + (0.16 + 0.17 * r) * s); g.lineTo(xx, py + (0.16 + 0.17 * (r + 1)) * s); }
        g.stroke();
      } else if (tv === 3) {                                                          // asphalt: a dashed yellow line along the road, each arm of a junction gets its own dash
        g.fillStyle = '#e8d24a'; const th = Math.max(2, u * 1.4), n = rN + rE + rS + rW;
        const dash = (hz, a, b) => hz ? g.fillRect(px + a * s, py + 0.5 * s - th / 2, (b - a) * s, th) : g.fillRect(px + 0.5 * s - th / 2, py + a * s, th, (b - a) * s);
        if (n === 0) dash(false, 0.3, 0.7);
        else if (n === 1 || (n === 2 && rN && rS) || (n === 2 && rE && rW)) dash(!!(rE || rW) && !(rN || rS), 0.14, 0.86);
        else { if (rN) dash(false, 0.08, 0.34); if (rS) dash(false, 0.66, 0.92); if (rW) dash(true, 0.08, 0.34); if (rE) dash(true, 0.66, 0.92); }
      }
      else {
        g.fillStyle = R.p; for (let i = 0; i < 3; i++) g.fillRect(px + (0.28 + hs(82 + i) * 0.4) * s, py + (0.28 + hs(85 + i) * 0.4) * s, Math.max(2, u * 1.3), Math.max(2, u * 1.1));   // pebbles
      }
    } else {                                                                       // a bridge over water or void: planks along the way
      const horiz = (rE || rW) && !(rN || rS), cross = (rE || rW) && (rN || rS);
      const plank = (hz) => {
        g.fillStyle = R.bd; g.fillRect(px + (hz ? 0 : 0.12 * s), py + (hz ? 0.12 * s : 0), hz ? s + 1 : 0.76 * s, hz ? 0.76 * s : s + 1);
        g.fillStyle = R.bp; const n = 4, oy = hz ? 0.12 * s : 0, ox = hz ? 0 : 0.12 * s;
        for (let i = 0; i < n; i++) { if (hz) g.fillRect(px + i * s / n + 1, py + oy + 1, s / n - 2, 0.76 * s - 2); else g.fillRect(px + ox + 1, py + i * s / n + 1, 0.76 * s - 2, s / n - 2); }
        g.fillStyle = R.br; const r = Math.max(2, u * 1.4);                        // the rails
        if (hz) { g.fillRect(px, py + oy, s + 1, r); g.fillRect(px, py + oy + 0.76 * s - r, s + 1, r); } else { g.fillRect(px + ox, py, r, s + 1); g.fillRect(px + ox + 0.76 * s - r, py, r, s + 1); }
      };
      if (cross) { plank(true); plank(false); } else plank(horiz);
    }
  } else if (tp === 3) {                                                          // wall: joins its neighbours
    const col = MAP_WALL[tv] || MAP_WALL[0], hedge = tv === 3;
    const wN = ((nb[0] >> 3) & 7) === 3, wE = ((nb[1] >> 3) & 7) === 3, wS = ((nb[2] >> 3) & 7) === 3, wW = ((nb[3] >> 3) & 7) === 3;
    const topH = wS ? 1 : 0.74, line = mapMix(col, '#000000', 0.22), lit = mapMix(col, '#ffffff', 0.3), dark = mapMix(col, '#000000', 0.35), front = mapMix(col, '#000000', 0.25);
    rect(col, 0, 0, 1, topH);
    g.strokeStyle = line; g.lineWidth = Math.max(1, u * 0.7); g.beginPath();
    if (tv === 2) { for (let r = 1; r < 4; r++) { g.moveTo(px + s * r / 4, py); g.lineTo(px + s * r / 4, py + topH * s); } }   // wood: planks
    else if (hedge) { g.fillStyle = mapMix(col, '#ffffff', 0.15); for (let i = 0; i < 5; i++) g.fillRect(px + hs(110 + i) * s * 0.85, py + hs(115 + i) * s * 0.6, Math.max(2, u * 2), Math.max(2, u * 1.6)); }
    else {
      const off = (cy & 1) ? 0.25 : 0.75;
      g.moveTo(px, py + topH * s * 0.5); g.lineTo(px + s, py + topH * s * 0.5);
      g.moveTo(px + off * s, py); g.lineTo(px + off * s, py + topH * s * 0.5);
      g.moveTo(px + (1 - off) * s, py + topH * s * 0.5); g.lineTo(px + (1 - off) * s, py + topH * s);
    }
    g.stroke();
    const e = Math.max(2, u * 1.2);
    g.fillStyle = lit; if (!wN) g.fillRect(px, py, w, e); if (!wW) g.fillRect(px, py, e, topH * s + 1);
    g.fillStyle = dark; if (!wE) g.fillRect(px + s - e, py, e, topH * s + 1);
    if (!wS) {
      rect(front, 0, 0.74, 1, 0.26); rect(mapMix(front, '#000000', 0.15), 0, 0.74, 1, 0.05);
      g.strokeStyle = mapMix(front, '#000000', 0.2); g.lineWidth = Math.max(1, u * 0.7); g.beginPath();
      if (tv === 2) { for (let r = 1; r < 4; r++) { g.moveTo(px + s * r / 4, py + 0.74 * s); g.lineTo(px + s * r / 4, py + s); } } else { g.moveTo(px + s * 0.5, py + 0.74 * s); g.lineTo(px + s * 0.5, py + s); g.moveTo(px, py + 0.87 * s); g.lineTo(px + s, py + 0.87 * s); }
      g.stroke();
      g.fillStyle = '#0003'; g.fillRect(px, py + s, w, Math.max(2, u * 1.6));
    }
  } else if (tp === 4) {                                                         // house: the more you paint, the bigger the roof; the front row gets the walls, windows and a door
    const [rc, wc] = MAP_ROOF[tv] || MAP_ROOF[0];
    const hN = ((nb[0] >> 3) & 7) === 4, hE = ((nb[1] >> 3) & 7) === 4, hS = ((nb[2] >> 3) & 7) === 4, hW = ((nb[3] >> 3) & 7) === 4;
    const roofH = hS ? 1 : 0.56, e = Math.max(2, u * 1.3), tl = mapMix(rc, '#000000', 0.18);
    rect(rc, 0, 0, 1, roofH);
    g.strokeStyle = tl; g.lineWidth = Math.max(1, u * 0.7); g.beginPath();   // rows of tiles, offset every other row
    for (let r = 1; r <= 3; r++) { const yy = py + roofH * s * r / 4; g.moveTo(px, yy); g.lineTo(px + s, yy); }
    for (let r = 0; r < 4; r++) { const y0 = py + roofH * s * r / 4, off = ((cx + r + cy) & 1) ? 0.25 : 0.75; g.moveTo(px + off * s, y0); g.lineTo(px + off * s, y0 + roofH * s / 4); g.moveTo(px + (off - 0.5) * s + (off < 0.5 ? s : 0), y0); g.lineTo(px + (off - 0.5) * s + (off < 0.5 ? s : 0), y0 + roofH * s / 4); }
    g.stroke();
    g.fillStyle = mapMix(rc, '#ffffff', 0.25); if (!hN) g.fillRect(px, py, w, e);                // lit top edge (the ridge)
    g.fillStyle = mapMix(rc, '#ffffff', 0.32); if (!hW) g.fillRect(px, py, e, roofH * s + 1);
    g.fillStyle = mapMix(rc, '#000000', 0.4); if (!hE) g.fillRect(px + s - e, py, e, roofH * s + 1);
    if (!hS) {
      rect(wc, 0, 0.56, 1, 0.44); rect(mapMix(wc, '#000000', 0.12), 0, 0.56, 1, 0.06); rect(mapMix(wc, '#000000', 0.4), 0, 0.97, 1, 0.03);
      g.fillStyle = mapMix(rc, '#000000', 0.55); g.fillRect(px, py + 0.56 * s, w, Math.max(2, u * 1.2));
      // where am I along the front? the door sits in the middle of each run of front cells
      const front = d => { const c = MAP.cells.get(mapKey(cx + d, cy)) || 0, sc = MAP.cells.get(mapKey(cx + d, cy + 1)) || 0; return ((c >> 3) & 7) === 4 && ((sc >> 3) & 7) !== 4; };
      let i = 0, n = 1; while (i < 60 && front(-(i + 1))) i++; while (n < 60 && front(n)) n++; const len = i + n, door = Math.floor(len / 2);
      if (i === door) {
        g.fillStyle = '#6a3f1f'; g.fillRect(px + 0.3 * s, py + 0.66 * s, 0.4 * s, 0.34 * s); g.fillStyle = '#8a5a2e'; g.fillRect(px + 0.34 * s, py + 0.7 * s, 0.32 * s, 0.3 * s);
        g.fillStyle = '#f2c94c'; g.fillRect(px + 0.58 * s, py + 0.84 * s, Math.max(2, u * 1.4), Math.max(2, u * 1.4));
      } else {
        g.fillStyle = '#5b4a35'; g.fillRect(px + 0.28 * s, py + 0.66 * s, 0.44 * s, 0.26 * s); g.fillStyle = '#9fd4f5'; g.fillRect(px + 0.32 * s, py + 0.7 * s, 0.36 * s, 0.18 * s);
        g.fillStyle = '#5b4a35'; g.fillRect(px + 0.49 * s, py + 0.7 * s, Math.max(1, u * 0.8), 0.18 * s);
      }
      g.fillStyle = '#0003'; g.fillRect(px, py + s, w, Math.max(2, u * 1.6));
    }
  } else if (tp === 5) {                                                         // tree: neighbours blend into a forest; the south one stands in front
    const tN = ((nb[0] >> 3) & 7) === 5, tE = ((nb[1] >> 3) & 7) === 5, tW = ((nb[3] >> 3) & 7) === 5, k = 0.92 + hs(120) * 0.16, jx = (hs(121) - 0.5) * 0.08;
    const disc = (cxx, cyy, r, c) => { g.fillStyle = c; g.beginPath(); g.arc(px + cxx * s, py + cyy * s, r * s * k, 0, 6.3); g.fill(); };
    g.fillStyle = '#0003'; g.beginPath(); g.ellipse(px + 0.5 * s, py + 0.9 * s, 0.34 * s, 0.1 * s, 0, 0, 6.3); g.fill();
    if (tv === 1 || tv === 4) {                                                    // pine: three stacked triangles
      const snow = tv === 4, dk = snow ? '#2c5a46' : '#245a34', md = snow ? '#3c7458' : '#2f7a42';
      rect('#5b3d22', 0.44 + jx, 0.78, 0.12, 0.16);
      for (let i = 0; i < 3; i++) {
        const top = 0.0 + i * 0.22, hw = (0.2 + i * 0.1) * k, bot = top + 0.36;
        g.fillStyle = i % 2 ? md : dk; g.beginPath(); g.moveTo(px + (0.5 + jx) * s, py + top * s); g.lineTo(px + (0.5 + jx + hw) * s, py + bot * s); g.lineTo(px + (0.5 + jx - hw) * s, py + bot * s); g.closePath(); g.fill();
        if (snow) { g.fillStyle = '#f4f8fb'; g.beginPath(); g.moveTo(px + (0.5 + jx) * s, py + top * s); g.lineTo(px + (0.5 + jx + hw * 0.62) * s, py + (top + 0.22) * s); g.lineTo(px + (0.5 + jx - hw * 0.62) * s, py + (top + 0.22) * s); g.closePath(); g.fill(); }
      }
    } else if (tv === 3) {                                                         // palm: a leaning trunk and fronds
      g.strokeStyle = '#a07a46'; g.lineWidth = Math.max(2, s * 0.09); g.lineCap = 'round'; g.beginPath(); g.moveTo(px + 0.46 * s, py + 0.92 * s); g.quadraticCurveTo(px + (0.5 + jx) * s, py + 0.5 * s, px + (0.56 + jx) * s, py + 0.24 * s); g.stroke();
      const hx = px + (0.56 + jx) * s, hy = py + 0.24 * s; g.strokeStyle = '#2f9a45'; g.lineWidth = Math.max(2, s * 0.07);
      for (const [dx, dy] of [[-0.34, 0.08], [-0.22, -0.12], [0.0, -0.2], [0.24, -0.12], [0.36, 0.08], [-0.12, 0.2], [0.14, 0.2]]) { g.beginPath(); g.moveTo(hx, hy); g.quadraticCurveTo(hx + dx * s * 0.6, hy + (dy - 0.12) * s, hx + dx * s, hy + dy * s); g.stroke(); }
      g.fillStyle = '#6a4a28'; g.beginPath(); g.arc(hx, hy + s * 0.03, s * 0.04, 0, 6.3); g.fill();
    } else if (tv === 5) {                                                         // dead tree: bare branches
      g.strokeStyle = '#6b5a4a'; g.lineWidth = Math.max(2, s * 0.08); g.lineCap = 'round'; g.beginPath(); g.moveTo(px + 0.5 * s, py + 0.92 * s); g.lineTo(px + (0.5 + jx) * s, py + 0.4 * s); g.stroke();
      g.lineWidth = Math.max(1, s * 0.05); g.beginPath();
      g.moveTo(px + (0.5 + jx) * s, py + 0.62 * s); g.lineTo(px + 0.24 * s, py + 0.38 * s); g.moveTo(px + (0.5 + jx) * s, py + 0.5 * s); g.lineTo(px + 0.78 * s, py + 0.22 * s);
      g.moveTo(px + (0.5 + jx) * s, py + 0.42 * s); g.lineTo(px + 0.4 * s, py + 0.12 * s); g.moveTo(px + 0.3 * s, py + 0.46 * s); g.lineTo(px + 0.16 * s, py + 0.44 * s); g.stroke();
    } else {                                                                       // oak (0) and autumn (2): a round canopy of overlapping blobs
      const C = tv === 2 ? ['#b5601d', '#d9822b', '#efb04a'] : ['#2f7a34', '#3e9a43', '#6bc15a'];
      rect('#6b4a2a', 0.44 + jx, 0.62, 0.12, 0.3); rect('#4f351d', 0.53 + jx, 0.64, 0.03, 0.28);
      disc(0.5 + jx, 0.4, 0.36, C[0]); disc(0.3, 0.52, 0.25, C[0]); disc(0.7, 0.52, 0.25, C[0]);
      if (tW) disc(0.0, 0.5, 0.24, C[0]); if (tE) disc(1.0, 0.5, 0.24, C[0]); if (tN) disc(0.5, 0.0, 0.3, C[0]);
      disc(0.5 + jx, 0.36, 0.3, C[1]); disc(0.32, 0.48, 0.2, C[1]); disc(0.68, 0.48, 0.2, C[1]);
      disc(0.4 + jx, 0.26, 0.12, C[2]); disc(0.6, 0.4, 0.07, C[2]);
    }
  }
}

function mapDraw() {
  if (!MAP.on) return;
  MAP.raf = requestAnimationFrame(mapDraw);
  const cv = MAP.cv, dpr = window.devicePixelRatio || 1, W = cv.clientWidth, H = cv.clientHeight;
  if (cv.width !== Math.round(W * dpr) || cv.height !== Math.round(H * dpr)) { cv.width = Math.round(W * dpr); cv.height = Math.round(H * dpr); MAP.dirty = true; }
  const now = performance.now();
  if (!MAP.dirty && !MAP.cells.size) return;
  const g = cv.getContext('2d'); g.setTransform(dpr, 0, 0, dpr, 0, 0);
  const z = MAP.cam.z, ox = W / 2 - MAP.cam.x * z, oy = H / 2 - MAP.cam.y * z;
  g.fillStyle = '#10161d'; g.fillRect(0, 0, W, H);
  const x0 = Math.floor(-ox / z), x1 = Math.ceil((W - ox) / z), y0 = Math.floor(-oy / z), y1 = Math.ceil((H - oy) / z);
  const editing = mapIsMaster();
  if (editing && z >= 12) {                                                       // the Master sees the empty cells too
    g.fillStyle = '#18212b';
    for (let x = x0; x < x1; x++) for (let y = y0; y < y1; y++) if (!MAP.cells.has(mapKey(x, y)) && (x + y) % 2 === 0) g.fillRect(ox + x * z, oy + y * z, z, z);
  }
  for (let x = x0; x < x1; x++) for (let y = y0; y < y1; y++) { const v = MAP.cells.get(mapKey(x, y)) || 0; if (v || mapLv(MAP.cells.get(mapKey(x, y - 1)) || 0)) mapTile(g, x, y, ox + x * z, oy + y * z, z, v, now); }
  if (z >= 12) {                                                                    // visible grid: dark line + light line beside it, readable on any block
    for (const [col, d] of [['#00000066', 0.5], ['#ffffff40', 1.5]]) {
      g.strokeStyle = col; g.lineWidth = 1; g.beginPath();
      for (let x = x0; x <= x1; x++) { g.moveTo(Math.floor(ox + x * z) + d, 0); g.lineTo(Math.floor(ox + x * z) + d, H); }
      for (let y = y0; y <= y1; y++) { g.moveTo(0, Math.floor(oy + y * z) + d); g.lineTo(W, Math.floor(oy + y * z) + d); }
      g.stroke();
    }
  }
  mapDrawTokens(g, ox, oy, z);
  if (MAP.hover && MAP.id && MAP.tool !== 'hand') {                                  // where the brush will paint
    const r = (MAP.brush - 1) / 2; g.strokeStyle = '#f9c22e'; g.lineWidth = 2; g.strokeRect(ox + (MAP.hover[0] - r) * z + 1, oy + (MAP.hover[1] - r) * z + 1, MAP.brush * z - 2, MAP.brush * z - 2);
  }
  if (!MAP.id) { g.fillStyle = '#9bb8cc'; g.font = 'bold 16px system-ui'; g.textAlign = 'center'; g.fillText(t('mapNone'), W / 2, H / 2); }
  MAP.dirty = MAP.cells.size ? true : false;                                       // water waves move: keep drawing while there is a map
}


// ---------- pieces (tokens) ----------
// PTU size classes: Small/Medium 1x1, Large 2x2, Huge 3x3, Gigantic 4x4 (web/assets/pokemon-sizes.json lists the big ones, from the Pokedex books).
// A piece's x,y is its top-left cell; people and NPCs are always 1x1.
fetch('/assets/pokemon-sizes.json').then(r => r.json()).then(j => { MAP.sizes = j; MAP.dirty = true; }).catch(() => {});
const mapN = tk => (tk && tk.s && MAP.sizes && MAP.sizes[tk.s]) || 1;
const mapDragCell = d => { const n = d.n; return [Math.round(d.wx - d.off[0] - n / 2), Math.round(d.wy - d.off[1] - n / 2)]; };

const mapPost = body => fetch('/api/map/token', { method: 'POST', body: new URLSearchParams({ ...(mapIsMaster() ? { mapId: MAP.id, code: document.getElementById('spawnSession').value || '' } : auth()), ...body }) });

function mapTokenAt(px, py) {
  const [wx, wy] = mapToWorld(px, py);
  const list = MAP.tokens.filter(tk => { const n = mapN(tk); return wx > tk.x - 0.05 && wx < tk.x + n + 0.05 && wy > tk.y - 0.05 && wy < tk.y + n + 0.05; });
  const mine = tk => mapIsMaster() || tk.o === MAP.me;                                        // what you can carry comes first when pieces overlap
  return list.sort((a, b) => (mine(b) - mine(a)) || (mapN(a) - mapN(b)) || (b.y - a.y))[0] || null;
}
function mapTokenMove(d, c) {
  d.cell = c; d.sent = performance.now();
  const tk = MAP.tokens.find(q => q.id === d.id); if (tk) { tk.x = c[0]; tk.y = c[1]; }
  mapPost({ action: 'move', id: d.id, x: c[0], y: c[1] });
}
// where new pieces appear: the middle of what you are looking at
const mapSpot = (n = 1) => {
  const cx = Math.floor(MAP.cam.x - n / 2 + 0.5), cy = Math.floor(MAP.cam.y - n / 2 + 0.5);
  const free = (x, y) => !MAP.tokens.some(q => { const m = mapN(q); return x < q.x + m && q.x < x + n && y < q.y + m && q.y < y + n; });
  for (let r = 0; r < 10; r++) for (let dx = -r; dx <= r; dx++) for (let dy = -r; dy <= r; dy++) {   // the nearest free spot, circling outwards
    if (Math.max(Math.abs(dx), Math.abs(dy)) !== r) continue;
    if (free(cx + dx, cy + dy)) return [cx + dx, cy + dy];
  }
  return [cx, cy];
};
async function mapAddToken(type, ref, name, color, sprite = '', shiny = false, owner = 0) {
  const [x, y] = mapSpot((MAP.sizes && MAP.sizes[sprite]) || 1);
  const r = await mapPost({ action: 'add', type, ref, name, color, s: sprite, sh: shiny ? 1 : 0, x, y, owner });
  if (r.ok) { MAP.tokens = (await r.json()).tokens; mapRenderMine(); MAP.dirty = true; }
}
async function mapRemoveToken(id) {
  const r = await mapPost({ action: 'remove', id });
  if (r.ok) { MAP.tokens = (await r.json()).tokens; mapRenderMine(); MAP.dirty = true; }
}
const mapTokenOf = (type, ref) => MAP.tokens.find(q => q.t === type && q.r === ref);
function mapToggle(type, ref, name, color, sprite, shiny, owner) {
  const have = mapTokenOf(type, ref);
  if (have) mapRemoveToken(have.id); else mapAddToken(type, ref, name, color, sprite, shiny, owner);
}

const mapIcon = (key, shiny) => {
  if (!key || typeof art === 'undefined' || !art[key]) return null;
  const url = '/assets/sprites/' + (shiny ? 'shiny/' : '') + art[key] + '.png';
  let im = MAP.imgs[url];
  if (!im) { im = MAP.imgs[url] = new Image(); im.src = url; }
  return im.complete && im.naturalWidth ? im : null;
};

function mapDrawTokens(g, ox, oy, z) {
  if (!MAP.tokens.length) return;
  const list = MAP.tokens.map(tk => {
    const n = mapN(tk); let tx = tk.x + n / 2, ty = tk.y + n / 2;
    if (MAP.dragTok && MAP.dragTok.id === tk.id) { const d = MAP.dragTok, c = mapDragCell(d); tx = c[0] + n / 2; ty = c[1] + n / 2; }
    const p = MAP.tpos[tk.id] || (MAP.tpos[tk.id] = { x: tx, y: ty });
    const k = MAP.dragTok && MAP.dragTok.id === tk.id ? 0.6 : 0.3;
    p.x += (tx - p.x) * k; p.y += (ty - p.y) * k;
    return { tk, p, n };
  }).sort((a, b) => (a.p.y + a.n / 2) - (b.p.y + b.n / 2));
  const alive = new Set(MAP.tokens.map(q => q.id)); for (const id of Object.keys(MAP.tpos)) if (!alive.has(+id)) delete MAP.tpos[id];
  for (const { tk, p, n } of list) {
    const cx = ox + p.x * z, cy = oy + p.y * z, r = z * 0.42 * n, lifted = MAP.dragTok && MAP.dragTok.id === tk.id;
    g.fillStyle = '#0005'; g.beginPath(); g.ellipse(cx, cy + r * 0.8, r * 0.9, r * 0.35, 0, 0, Math.PI * 2); g.fill();
    const ring = tk.t === 'wild' ? '#e5484d' : tk.c;
    g.save(); if (lifted) g.translate(0, -z * 0.12);
    const im = tk.s ? mapIcon(tk.s, tk.sh) : null;
    if (im && (tk.t === 'pokemon' || tk.t === 'wild')) {   // a Pokemon is just its sprite, with a coloured ring on the ground
      g.lineWidth = Math.max(2, z * 0.07); g.strokeStyle = ring;
      if (n > 1) { const bx = ox + (p.x - n / 2) * z + 2, by = oy + (p.y - n / 2) * z + 2; g.fillStyle = ring + '26'; g.fillRect(bx, by, n * z - 4, n * z - 4); g.strokeRect(bx, by, n * z - 4, n * z - 4); }   // the squares it covers
      else { g.beginPath(); g.ellipse(cx, cy + r * 0.8, r * 0.9, r * 0.35, 0, 0, Math.PI * 2); g.stroke(); }
      g.imageSmoothingEnabled = false; const sz = z * 1.15 * (n > 1 ? n * 0.85 : 1); g.drawImage(im, cx - sz / 2, cy - sz * 0.62 + (n > 1 ? sz * 0.12 : 0), sz, sz);
    } else {
      g.beginPath(); g.arc(cx, cy, r, 0, Math.PI * 2);
      g.fillStyle = tk.t === 'pokemon' || tk.t === 'wild' ? '#e9f1f7' : tk.c; g.fill();
      g.lineWidth = Math.max(2, z * 0.08); g.strokeStyle = tk.t === 'player' ? '#ffffff' : ring; g.stroke();
      g.fillStyle = '#fff'; g.font = 'bold ' + Math.round(z * 0.5) + 'px system-ui'; g.textAlign = 'center'; g.textBaseline = 'middle'; g.fillText((tk.n || '?').slice(0, 1).toUpperCase(), cx, cy + z * 0.03);
    }
    g.restore();
    if (z >= 22 && tk.n) {
      g.font = 'bold ' + Math.round(Math.max(10, z * 0.26)) + 'px system-ui'; g.textAlign = 'center'; g.textBaseline = 'alphabetic';
      g.lineWidth = 3; g.strokeStyle = '#000b'; const ly = cy + (n > 1 ? n * z / 2 : r) + z * 0.3; g.strokeText(tk.n, cx, ly); g.fillStyle = '#fff'; g.fillText(tk.n, cx, ly);
    }
  }
  MAP.dirty = true;
}

// ---------- the "my pieces" bar (players) and the pieces panel (Master) ----------
let mapMineSig = '';
function mapRenderMine() {
  const mine = document.getElementById('mapMine'), master = document.getElementById('mapTokens');
  if (mine && typeof pcData !== 'undefined') {
    const sig = JSON.stringify([pcData.map(x => [x.id, x.name, x.team.filter(m => m.party > 0).map(m => m.id)]), MAP.tokens.map(q => q.id)]);
    if (sig === mapMineSig) return; mapMineSig = sig;
    const col = typeof savedColor === 'function' ? savedColor() : '#d32f2f';
    mine.innerHTML = pcData.map(tr => '<div class="mmine"><b>' + esc(tr.name) + '</b>' +
      '<button class="chip' + (mapTokenOf('player', tr.id) ? ' on' : '') + '" onclick="mapToggle(\'player\',' + tr.id + ',\'' + esc(tr.name).replace(/'/g, '') + '\',\'' + col + '\',\'\',false,0)">' + esc(t('mapMe')) + '</button>' +
      tr.team.filter(m => m.party > 0).sort((a, b) => a.party - b.party).map(m => '<button class="chip' + (mapTokenOf('pokemon', m.id) ? ' on' : '') + '" title="' + esc(displayName(m)) + '" onclick="mapToggle(\'pokemon\',' + m.id + ',\'' + esc(displayName(m)).replace(/'/g, '') + '\',\'' + col + '\',\'' + esc(keyOf(m)).replace(/'/g, '') + '\',' + !!m.shiny + ',0)">' + sprite(keyOf(m), 'small', m.shiny) + '</button>').join('') + '</div>').join('') || '<small class="muted">' + t('noChars') + '</small>';
  }
  if (master) {
    const sel = document.getElementById('mapTrSel'), keep = sel.value;
    if (typeof pcData !== 'undefined') sel.innerHTML = pcData.map(tr => '<option value="' + tr.id + '">' + esc(tr.name) + ' (' + esc(tr.owner || '') + ')</option>').join('');
    if ([...sel.options].some(o => o.value === keep)) sel.value = keep;
    const tr = typeof pcData !== 'undefined' ? pcData.find(x => x.id === +sel.value) : null;
    const col = tr ? '#4f86a3' : '#888';
    document.getElementById('mapTrChips').innerHTML = tr
      ? '<button class="chip" onclick="mapToggle(\'player\',' + tr.id + ',\'' + esc(tr.name).replace(/'/g, '') + '\',\'#d32f2f\',\'\',false,' + (tr.ownerId || 0) + ')">' + esc(t('mapMe')) + ' ' + esc(tr.name) + '</button>' +
        tr.team.map(m => '<button class="chip" title="' + esc(displayName(m)) + '" onclick="mapToggle(\'pokemon\',' + m.id + ',\'' + esc(displayName(m)).replace(/'/g, '') + '\',\'#d32f2f\',\'' + esc(keyOf(m)).replace(/'/g, '') + '\',' + !!m.shiny + ',' + (tr.ownerId || 0) + ')">' + sprite(keyOf(m), 'small', m.shiny) + '</button>').join('')
      : '';
    document.getElementById('mapWildChips').innerHTML = MAP.wild.map(a => '<button class="chip" title="' + esc(a.species) + '" onclick="mapAddToken(\'wild\',0,\'' + esc(a.species).replace(/'/g, '') + '\',\'#e5484d\',\'' + esc(keyOf(a)).replace(/'/g, '') + '\',' + !!a.shiny + ',0)">' + sprite(keyOf(a), 'small', a.shiny) + '</button>').join('') || '<small class="muted">' + t('fieldEmpty') + '</small>';
    document.getElementById('mapTokList').innerHTML = MAP.tokens.map(q => '<span class="chip on" title="' + esc(q.t) + '">' + esc(q.n || '?') + ' <b onclick="mapRemoveToken(' + q.id + ')" style="cursor:pointer">\u2715</b></span>').join('') || '<small class="muted">' + t('mapNoTokens') + '</small>';
  }
}
function mapAddSpecies() {   // Master: any Pokemon on the map, no need to spawn it on the field first
  const inp = document.getElementById('mapSpecies'), key = inp.value.trim();
  const k = Object.keys(art).find(x => x.toLowerCase() === key.toLowerCase() || x.toLowerCase() === (key + '|normal').toLowerCase());
  if (!k) { inp.focus(); return; }
  mapAddToken('wild', 0, k.split('|')[0] + (k.endsWith('|Normal') ? '' : ' (' + k.split('|')[1] + ')'), '#e5484d', k, document.getElementById('mapSpShiny').checked, 0);
  inp.value = '';
}
async function mapAddNpc() {
  const name = await askConfirm(t('mapNpcName'), true);
  if (!name) return;
  const cols = ['#8e44ad', '#2e86c1', '#d68910', '#17a589', '#c0392b', '#7f8c8d'];
  mapAddToken('npc', 0, name, cols[Math.floor(Math.random() * cols.length)]);
}
async function mapLoadWild() {
  const code = document.getElementById('spawnSession').value; if (!code) return;
  const r = await fetch('/api/active?code=' + code); if (r.ok) { MAP.wild = await r.json(); mapMineSig = ''; mapRenderMine(); }
}

function mapFillSp() { const l = document.getElementById('mapSpList'); if (!l.children.length) l.innerHTML = Object.keys(art).map(k => '<option value="' + esc(k.replace('|Normal', '')) + '">').join(''); }
