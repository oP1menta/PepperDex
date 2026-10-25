//=========SHARED TABLE=========
// A round table seen in perspective. The server runs the physics; this page polls the items and hands,
// draws them and sends where MY hand is. Master and players use the same canvas.

const TBL_CHEST_Y = 1.2;                 // below this line (world units) a released item goes to the chest
const TBL = { on: false, cv: null, snap: null, prev: null, snapAt: 0, prevAt: 0, hand: { x: 0, y: 1.3, down: false, inside: false }, take: 1, timer: 0, send: 0, imgs: {}, raf: 0, sent: '', sentAt: 0, me: -1, mySeat: 0, n: 0, chestKey: '', listQty: {}, items: [] };
const tblIsMaster = () => !!document.getElementById('tab-players');

// Called by showTab: the table only runs (polls, draws) while its tab is open
function tableSync(name) {
  const open = name === 'table';
  if (open === TBL.on) return;
  TBL.on = open;
  if (!open) { clearTimeout(TBL.timer); clearInterval(TBL.send); cancelAnimationFrame(TBL.raf); return; }
  TBL.cv = document.getElementById('tblCanvas');
  if (!TBL.cv.dataset.ready) tableBind();
  tblPoll(); TBL.send = setInterval(tblSendHand, 50); tblDraw();
  tblRenderChest();
}

const tblCode = () => tblIsMaster() ? document.getElementById('spawnSession').value : auth().code;
const tblAuth = () => tblIsMaster() ? { master: 1 } : { token: auth().token };

async function tblPoll() {
  if (!TBL.on) return;
  try {
    const code = tblCode();
    if (code) {
      const r = await fetch('/api/table?' + new URLSearchParams({ code, ...tblAuth() }));
      if (r.ok) { TBL.prev = TBL.snap; TBL.prevAt = TBL.snapAt; TBL.snap = await r.json(); TBL.me = TBL.snap.me; TBL.mySeat = TBL.snap.ms; TBL.snapAt = performance.now(); }
    }
  } catch (e) { /* offline: try again */ }
  if (!tblIsMaster() && ++TBL.n % 40 === 0 && !TBL.hand.down) loadTrainers().then(tblRenderChest);   // about every 3 s
  TBL.timer = setTimeout(tblPoll, 70);
}

// The table is a disc (radius 1) shared by everybody: the server keeps WORLD coordinates. Every viewer sits at a seat
// (60 degrees apart) and sees the world turned so that HIS seat is at the bottom of his screen, his chest in front of him.
// view = what is drawn on my screen (x right, y toward me); world = what the server stores.
const tblW = () => TBL.cv.clientWidth, tblH = () => TBL.cv.clientHeight;
const tblSx = x => tblW() / 2 + x * tblW() * 0.33;
const tblSy = y => tblH() * 0.46 + y * tblH() * 0.27;
const tblWx = px => (px - tblW() / 2) / (tblW() * 0.33);
const tblWy = py => (py - tblH() * 0.46) / (tblH() * 0.27);
const tblAng = seat => (seat || 0) * Math.PI / 3;
function tblToView(x, y, seat) { const r = Math.hypot(x, y), a = Math.atan2(x, y) - tblAng(seat); return [r * Math.sin(a), r * Math.cos(a)]; }
function tblToWorld(x, y, seat) { const r = Math.hypot(x, y), a = Math.atan2(x, y) + tblAng(seat); return [r * Math.sin(a), r * Math.cos(a)]; }
// where a seat's chest (r = 1.3) and arm entry (r = 2.2) lie on MY screen
function tblSeatXY(seat, r) { const [vx, vy] = tblToView(Math.sin(tblAng(seat)) * r, Math.cos(tblAng(seat)) * r, TBL.mySeat); return [tblSx(vx), tblSy(vy)]; }

function tableBind() {
  const cv = TBL.cv;
  cv.dataset.ready = 1;
  const move = e => { const r = cv.getBoundingClientRect(); TBL.hand.x = tblWx(e.clientX - r.left); TBL.hand.y = tblWy(e.clientY - r.top); TBL.hand.inside = true;
    // players: the chest is the slot grid right under the table. Below the table = over the chest; on the table the hand stays above it
    if (!tblIsMaster()) TBL.hand.y = e.clientY > r.bottom ? 1.35 : Math.min(TBL.hand.y, 1.15);
  };
  TBL.move = move;
  cv.addEventListener('pointermove', move);
  cv.addEventListener('pointerleave', () => { if (!TBL.hand.down) TBL.hand.inside = false; });
  cv.addEventListener('pointerdown', e => { e.preventDefault(); cv.setPointerCapture(e.pointerId); move(e); TBL.hand.down = true; tblSendHand(true); });
  const up = e => {
    if (!TBL.hand.down) return;
    move(e); TBL.hand.down = false;
    const chest = TBL.hand.y > TBL_CHEST_Y && !tblIsMaster();
    tblSendHand(true, chest);
    if (!chest && e.pointerType === 'touch') TBL.hand.inside = false;
  };
  cv.addEventListener('pointerup', up); cv.addEventListener('pointercancel', up);
  cv.addEventListener('contextmenu', e => e.preventDefault());
}

async function tblSendHand(force = false, chest = false) {
  const h = TBL.hand;
  if (!h.inside && !h.down && !force) return;
  const body = { code: tblCode(), ...tblAuth(), color: savedColor(), x: tblToWorld(h.x, h.y, TBL.mySeat)[0].toFixed(3), y: tblToWorld(h.x, h.y, TBL.mySeat)[1].toFixed(3), down: h.down ? 1 : 0, take: TBL.take };
  if (chest) { body.chest = 1; body.trainerId = tblTrainerId(); }
  const key = JSON.stringify(body);
  if (!force && key === TBL.sent && !h.down && performance.now() - TBL.sentAt < 1000) return;   // a still hand still reports once a second
  TBL.sent = key; TBL.sentAt = performance.now();
  try {
    const r = await fetch('/api/table/hand', { method: 'POST', body: new URLSearchParams(body) });
    const j = await r.json();
    if (j.deposited) { tblMsg(t('tblGot', j.name, j.deposited)); if (!tblIsMaster()) loadTrainers().then(tblRenderChest); }
  } catch (e) { /* next tick */ }
}

// how many to take: a number (typed or with the buttons), -1 = half of the stack, 999 = all
function tblTakeSet(n) {
  TBL.take = n === -1 ? -1 : Math.max(1, Math.min(99999, n || 1));
  const box = document.getElementById('tblTake');
  box.value = TBL.take > 0 && TBL.take < 99999 ? TBL.take : '';
  box.placeholder = TBL.take === -1 ? '\u00BD' : TBL.take >= 99999 ? t('tblAll') : '';
  document.getElementById('tblHalf').classList.toggle('on', TBL.take === -1);
  document.getElementById('tblAllB').classList.toggle('on', TBL.take >= 99999);
}
function tblTakeType(v) { if (v !== '') { TBL.take = Math.max(1, Math.min(99999, +v || 1)); document.getElementById('tblHalf').classList.remove('on'); document.getElementById('tblAllB').classList.remove('on'); } }

function tblMsg(text) { const m = document.getElementById('tblMsg'); if (m) m.textContent = text; }

//=========CHEST (player)=========
let tblTrainer = null;
function tblTrainerId() { if (typeof pcData === 'undefined' || !pcData.length) return ''; if (!pcData.some(x => x.id === tblTrainer)) tblTrainer = pcData[0].id; return tblTrainer; }

const tblSlug = n => String(n || '').toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
const tblIcon = (i, size = 28) => ballImg(i.skin || tblSlug(i.name), size);

// The chest = the whole bag of the character. Press an item and drag: it comes out of the bag into the hand
// (how many: the "Take" box above the table). Let go over the chest strip to put it back.
function tblRenderChest() {
  const box = document.getElementById('tblChest');
  if (!box || tblIsMaster()) return;
  tblTrainerId();
  const tr = pcData.find(x => x.id === tblTrainer);
  const key = JSON.stringify([tblTrainer, tr && tr.items.map(i => [i.id, i.qty])]);
  if (key === TBL.chestKey && box.innerHTML) return;      // nothing changed: do not rebuild under the finger
  TBL.chestKey = key;
  box.innerHTML = '<div class="tblgrid">' + tblSlots(tr ? tr.items : []) + '</div><div class="tblchest-h"><b>' + t('tblChest') + '</b>' +
    (pcData.length > 1 ? '<select onchange="tblTrainer = +this.value; tblRenderChest()">' + pcData.map(x => '<option value="' + x.id + '"' + (x.id === tblTrainer ? ' selected' : '') + '>' + esc(x.name) + '</option>').join('') + '</select>' : (tr ? ' <small>' + esc(tr.name) + '</small>' : '')) +
    '</div><p class="muted tblhint">' + t('tblChestHint') + '</p>';
}

function tblSlots(items) {
  const cols = innerWidth < 700 ? 6 : 8, n = Math.max(24, Math.ceil((items.length + 1) / cols) * cols);   // always a spare row, like a real chest
  let h = '';
  for (let k = 0; k < n; k++) {
    const i = items[k];
    h += i ? '<div class="tblslot full" title="' + esc(i.name) + '" onpointerdown="tblSlotDown(event,' + i.id + ')" onpointerup="tblSlotCancel()" onpointercancel="tblSlotCancel()" onpointerleave="tblSlotCancel()">' + tblIcon(i, 40) + '<small>' + esc(i.name) + '</small>' + (i.qty > 1 ? '<b>' + i.qty + '</b>' : '') + '</div>' : '<div class="tblslot"></div>';
  }
  return h;
}
// mouse: pick up at once. finger: hold a moment (a quick swipe scrolls the chest instead)
let tblHold = 0;
function tblSlotDown(e, id) {
  if (e.pointerType === 'mouse') return tblPull(e, id);
  tblSlotCancel();
  tblHold = setTimeout(() => { tblHold = 0; tblPull(e, id); }, 220);
}
function tblSlotCancel() { clearTimeout(tblHold); tblHold = 0; }
document.addEventListener('touchmove', e => { if (TBL.hand && TBL.hand.down && e.cancelable) e.preventDefault(); }, { passive: false });

// Start carrying a stack that does not lie on the table yet (from the chest or the Master's list)
async function tblPull(e, itemId, take) {
  e.preventDefault();
  const cv = TBL.cv;
  if (!cv) return;
  cv.setPointerCapture(e.pointerId);                    // from now on the canvas gets the moves and the release
  TBL.move(e);
  TBL.hand.down = true;
  const body = { code: tblCode(), ...tblAuth(), color: savedColor(), itemId, take: take ?? TBL.take,
    x: tblToWorld(TBL.hand.x, TBL.hand.y, TBL.mySeat)[0].toFixed(3), y: tblToWorld(TBL.hand.x, TBL.hand.y, TBL.mySeat)[1].toFixed(3) };
  if (!tblIsMaster()) body.trainerId = tblTrainerId();
  try {
    const r = await fetch('/api/table/pull', { method: 'POST', body: new URLSearchParams(body) });
    if (!r.ok) { TBL.hand.down = false; tblMsg(errText((await r.json()).error)); return; }
  } catch (err) { TBL.hand.down = false; return; }
  if (!tblIsMaster()) loadTrainers().then(tblRenderChest);
}


//=========MASTER=========
// The Master's "chest" is the list of every item that exists: type how many, then toss the list or drag one row onto the table.
async function tblLoadItems() {
  const r = await fetch('/api/items');
  if (r.ok) { TBL.items = await r.json(); tblListRender(); }
}

function tblListRender() {
  const box = document.getElementById('tblList');
  if (!box) return;
  const q = (document.getElementById('tblFilter').value || '').toLowerCase();
  let html = '', cat = '';
  for (const i of TBL.items) {
    if (q && !(i.name + ' ' + i.cat).toLowerCase().includes(q)) continue;
    if (i.cat !== cat) { cat = i.cat; html += '<div class="tblcat">' + esc(cat) + '</div>'; }
    const n = TBL.listQty[i.id] || 0;
    html += '<div class="tblslot full' + (n ? ' sel' : '') + '" data-id="' + i.id + '" title="' + esc(i.name + ' - ' + i.info) + '" onpointerdown="tblMDown(event,' + i.id + ')" oncontextmenu="tblMAdd(' + i.id + ',-1); return false">' +
      tblIcon(i, 40) + '<small>' + esc(i.name) + '</small>' + (n ? '<b>' + n + '</b>' : '') + '</div>';
  }
  box.innerHTML = html || '<small class="muted">' + t('tblNoItems') + '</small>';
  const sel = TBL.items.find(i => i.id === TBL.selId), bar = document.getElementById('tblSel');
  if (bar) bar.innerHTML = sel ? '<b>' + esc(sel.name) + '</b> <input type="number" min="0" max="99999" value="' + (TBL.listQty[sel.id] || 0) + '" oninput="TBL.listQty[' + sel.id + '] = Math.max(0, +this.value || 0); tblBadge(' + sel.id + ')">' : '<small class="muted">' + t('tblSelHint') + '</small>';
}
// the badge on a slot follows the number without rebuilding the grid (so typing is not interrupted)
function tblBadge(id) {
  const el = document.querySelector('#tblList .tblslot[data-id="' + id + '"]'); if (!el) return;
  const n = TBL.listQty[id] || 0; let b = el.querySelector('b');
  if (n && !b) el.appendChild(b = document.createElement('b'));
  if (b) { if (n) b.textContent = n; else b.remove(); }
  el.classList.toggle('sel', n > 0);
}
function tblMAdd(id, d) { TBL.selId = id; TBL.listQty[id] = Math.max(0, (TBL.listQty[id] || 0) + d); tblListRender(); }
// a click adds 1 to the list (right click = -1); press and move (finger: hold a moment first) drags the stack onto the table
function tblMDown(e, id) {
  if (e.button === 2) return;
  const x0 = e.clientX, y0 = e.clientY, touch = e.pointerType !== 'mouse', el = e.currentTarget;
  let drag = false, timer = 0;
  const start = ev => { drag = true; end(); tblPull(ev, id, tblListQty(id) || TBL.take || 1); };
  const mv = ev => { if (!touch && Math.hypot(ev.clientX - x0, ev.clientY - y0) > 6) start(ev); else if (touch && Math.hypot(ev.clientX - x0, ev.clientY - y0) > 10) end(); };
  const up = () => { end(); if (!drag) tblMAdd(id, 1); };
  const end = () => { clearTimeout(timer); el.removeEventListener('pointermove', mv); el.removeEventListener('pointerup', up); el.removeEventListener('pointercancel', cancel); };
  const cancel = () => end();
  if (touch) timer = setTimeout(() => start(e), 250);
  el.addEventListener('pointermove', mv); el.addEventListener('pointerup', up); el.addEventListener('pointercancel', cancel);
}
const tblListQty = id => TBL.listQty[id] || 0;

async function tblThrowList() {
  const list = Object.entries(TBL.listQty).filter(([, n]) => n > 0).map(([id, n]) => id + ':' + n).join(',');
  if (!list) { tblMsg(t('tblListEmpty')); return; }
  await fetch('/api/table/spawnlist', { method: 'POST', body: new URLSearchParams({ code: spawnSession.value, list }) });
  tblListClear();
}
function tblListClear() { TBL.listQty = {}; tblListRender(); }
async function tblClearAll() {
  if (!await askConfirm(t('tblClearAsk'))) return;
  await fetch('/api/table/clear', { method: 'POST', body: new URLSearchParams({ code: spawnSession.value }) });
}

//=========DRAWING=========
function tblImg(skin) {
  if (!skin) return null;
  let im = TBL.imgs[skin];
  if (!im) { im = TBL.imgs[skin] = new Image(); im.src = '/assets/items/' + skin + '.png?v=75'; }
  return im.complete && im.naturalWidth ? im : null;
}

// the position of everything now: slide from the previous snapshot to the newest one
function tblNow() {
  const s = TBL.snap;
  if (!s) return { items: [], hands: [] };
  const a = TBL.prev ? Math.min(1, (performance.now() - TBL.snapAt) / Math.max(60, TBL.snapAt - TBL.prevAt)) : 1;
  const old = {};
  if (TBL.prev) for (const p of TBL.prev.items) old[p.id] = p;
  const mix = (b, c) => b + (c - b) * a;
  const items = s.items.map(p => { const o = old[p.id]; return o ? { ...p, x: mix(o.x, p.x), y: mix(o.y, p.y), z: mix(o.z, p.z) } : p; });
  return { items, hands: s.hands };
}

function tblDraw() {
  if (!TBL.on) return;
  TBL.raf = requestAnimationFrame(tblDraw);
  const cv = TBL.cv, dpr = window.devicePixelRatio || 1;
  const W = cv.clientWidth, H = Math.round(W * (W < 520 ? 1.1 : 0.8));
  if (cv.style.height !== H + 'px') cv.style.height = H + 'px';
  if (cv.width !== Math.round(W * dpr) || cv.height !== Math.round(H * dpr)) { cv.width = Math.round(W * dpr); cv.height = Math.round(H * dpr); }
  const g = cv.getContext('2d');
  g.setTransform(dpr, 0, 0, dpr, 0, 0);
  g.clearRect(0, 0, W, H);

  // floor
  const bg = g.createLinearGradient(0, 0, 0, H); bg.addColorStop(0, '#16222f'); bg.addColorStop(1, '#0b1118');
  g.fillStyle = bg; g.fillRect(0, 0, W, H);

  // the table: side, top, wood rings
  const cx = W / 2, cy = tblSy(0), rx = W * 0.33, ry = H * 0.27, thick = H * 0.04;
  g.fillStyle = '#4a2f1b'; g.beginPath(); g.ellipse(cx, cy + thick, rx, ry, 0, 0, Math.PI * 2); g.fill();
  g.fillRect(cx - rx, cy, rx * 2, thick);
  g.beginPath(); g.ellipse(cx, cy + thick, rx, ry, 0, 0, Math.PI); g.fill();
  const top = g.createRadialGradient(cx, cy - ry * 0.2, rx * 0.1, cx, cy, rx);
  top.addColorStop(0, '#b27a45'); top.addColorStop(1, '#8a5a30');
  g.fillStyle = top; g.beginPath(); g.ellipse(cx, cy, rx, ry, 0, 0, Math.PI * 2); g.fill();
  g.strokeStyle = '#00000022'; g.lineWidth = 1;
  for (let k = 0.25; k < 1; k += 0.2) { g.beginPath(); g.ellipse(cx, cy, rx * k, ry * k, 0, 0, Math.PI * 2); g.stroke(); }
  g.strokeStyle = '#d9a46a'; g.lineWidth = 2; g.beginPath(); g.ellipse(cx, cy, rx, ry, 0, 0, Math.PI * 2); g.stroke();

  // my chest = the slot grid under the table: it lights up while I carry something over it
  const gr = document.querySelector('.tblgrid');
  if (gr) gr.classList.toggle('over', !!(TBL.hand.down && TBL.hand.y > TBL_CHEST_Y));

  const now = tblNow();

  // other hands glide toward their latest reported place (no jumps between polls)
  TBL.hs = TBL.hs || {};
  const alive = new Set(), seen = [];
  for (const hd of now.hands) {
    if (hd.p === TBL.me) continue;
    alive.add(hd.p);
    const o = TBL.hs[hd.p] || (TBL.hs[hd.p] = { x: hd.x, y: hd.y });
    o.x += (hd.x - o.x) * 0.3; o.y += (hd.y - o.y) * 0.3;
    seen.push({ hd, o });
  }
  for (const k of Object.keys(TBL.hs)) if (!alive.has(+k)) delete TBL.hs[k];
  // each player's chest sits at HIS seat, on the far side of the table from where he comes in; lit when his hand is over it
  for (const { hd, o } of seen) tblSeatChest(g, tblSeatXY(hd.s, 1.32), hd.n, hd.c, tblToView(o.x, o.y, hd.s)[1] > TBL_CHEST_Y, W, H);
  for (const { hd, o } of seen) { const [vx, vy] = tblToView(o.x, o.y, TBL.mySeat); tblHand(g, vx, vy, hd.d, hd.n, hd.s, false, hd.c, tblSeatXY(hd.s, 2.2)); }
  if (TBL.hand.inside || TBL.hand.down) tblHand(g, TBL.hand.x, TBL.hand.y, TBL.hand.down, '', TBL.mySeat, true, savedColor(), tblSeatXY(TBL.mySeat, 2.2));

  // items, far ones first
  const list = now.items.map(p => { const [vx, vy] = tblToView(p.x, p.y, TBL.mySeat); return { ...p, vx, vy }; }).sort((a, b) => a.vy - b.vy);
  for (const p of list) {
    const size = Math.min(W * 0.17, H * 0.125) * (1 + p.vy * 0.18), px = tblSx(p.vx), py = tblSy(p.vy);
    g.fillStyle = '#0006'; g.beginPath(); g.ellipse(px, py + size * 0.15, size * 0.38 * (1 - p.z * 0.8), size * 0.13 * (1 - p.z * 0.8), 0, 0, Math.PI * 2); g.fill();
    const lift = p.z * H * 0.22, im = tblImg(p.s || tblSlug(p.n));
    if (im) { g.imageSmoothingEnabled = false; g.drawImage(im, px - size / 2, py - size * 0.85 - lift, size, size); }
    else { g.font = Math.round(size * 0.8) + 'px system-ui, "Apple Color Emoji", "Segoe UI Emoji", sans-serif'; g.textAlign = 'center'; g.textBaseline = 'middle'; g.fillStyle = '#fff'; g.fillText(p.e || '\uD83D\uDCE6', px, py - size * 0.4 - lift); }
    if (p.q > 1) { g.fillStyle = '#000b'; g.font = 'bold ' + Math.round(size * 0.34) + 'px system-ui'; g.textAlign = 'left'; g.textBaseline = 'alphabetic'; g.fillText('x' + p.q, px + size * 0.1, py + size * 0.05 - lift * 0.3); g.fillStyle = '#fff'; g.fillText('x' + p.q, px + size * 0.1 - 1, py + size * 0.05 - 1 - lift * 0.3); }
  }
}

// Sleeve colour per player; the hand is a white cartoon glove
const TBL_SLEEVES = ['#e53935', '#1e88e5', '#43a047', '#fb8c00', '#8e24aa', '#00acc1'];
// a small chest box at a seat
function tblSeatChest(g, pt, name, color, open, W, H) {
  const w = Math.min(W * 0.16, H * 0.12), h = w * 0.7;
  g.save(); g.translate(pt[0], pt[1]);
  g.fillStyle = color || '#8d5a2b'; g.strokeStyle = open ? '#f9c22e' : '#000a'; g.lineWidth = open ? 3 : 2;
  g.beginPath(); g.roundRect(-w / 2, -h / 2, w, h, 6); g.fill(); g.stroke();
  g.fillStyle = '#000a'; g.fillRect(-w / 2 + 3, -h * 0.05, w - 6, 3);
  g.fillStyle = '#fff'; g.font = 'bold ' + Math.round(H * 0.03) + 'px system-ui'; g.textAlign = 'center'; g.textBaseline = 'top'; g.fillText(name || '', 0, h / 2 + 3);
  g.restore();
}
function tblHand(g, x, y, down, name, skin, mine, color, anchor) {
  const W = tblW(), H = tblH(), px = tblSx(x), py = tblSy(y), s = Math.min(W * 0.2, H * 0.135) * (1 + y * 0.15);
  const [ax, ay] = anchor;
  const ang = Math.atan2(py - ay, px - ax) + Math.PI / 2;   // the glove points away from where the arm comes from
  const sleeve = color || TBL_SLEEVES[skin];
  // arm: a sleeve that narrows from its edge to the wrist
  const arm = (dx, dy, w0, w1, col) => { const wx = px + dx - Math.sin(ang) * s * 0.5, wy = py + dy + Math.cos(ang) * s * 0.5, a = Math.atan2(wy - ay, wx - ax) + Math.PI / 2, nx = Math.cos(a), ny = Math.sin(a);
    g.fillStyle = col; g.beginPath(); g.moveTo(ax + nx * w0, ay + ny * w0); g.lineTo(wx + nx * w1, wy + ny * w1); g.lineTo(wx - nx * w1, wy - ny * w1); g.lineTo(ax - nx * w0, ay - ny * w0); g.closePath(); g.fill(); };
  arm(3, 2, s * 0.44, s * 0.28, '#00000040');
  arm(0, 0, s * 0.4, s * 0.26, sleeve);
  // glove (pointing up from the wrist)
  g.save(); g.translate(px, py); g.rotate(ang);
  g.fillStyle = '#fff'; g.strokeStyle = mine ? '#f9c22e' : '#334'; g.lineWidth = mine ? 3 : 2; g.lineJoin = 'round';
  if (down) {  // fist: a rounded block with finger lines and the thumb on top
    g.beginPath(); g.roundRect(-s * 0.42, -s * 0.38, s * 0.84, s * 0.75, s * 0.28); g.fill(); g.stroke();
    g.beginPath(); for (let k = -1; k <= 1; k++) { g.moveTo(k * s * 0.2, -s * 0.38); g.lineTo(k * s * 0.2, -s * 0.12); } g.stroke();
    g.beginPath(); g.ellipse(-s * 0.4, s * 0.02, s * 0.14, s * 0.22, -0.4, 0, Math.PI * 2); g.fill(); g.stroke();
  } else {     // open hand: palm, four fingers and a thumb
    for (let k = -1.5; k <= 1.5; k++) {
      g.beginPath(); g.roundRect(k * s * 0.2 - s * 0.085, -s * (0.78 - Math.abs(k) * 0.07), s * 0.17, s * 0.62, s * 0.085); g.fill(); g.stroke();
    }
    g.beginPath(); g.roundRect(-s * 0.4, -s * 0.28, s * 0.8, s * 0.62, s * 0.26); g.fill(); g.stroke();
    g.beginPath(); g.save(); g.translate(-s * 0.42, -s * 0.02); g.rotate(-0.7); g.roundRect(-s * 0.09, -s * 0.38, s * 0.18, s * 0.42, s * 0.09); g.fill(); g.stroke(); g.restore();
    g.beginPath(); g.roundRect(-s * 0.4, -s * 0.2, s * 0.8, s * 0.5, s * 0.24); g.fill();
  }
  // cuff
  g.fillStyle = sleeve; g.beginPath(); g.roundRect(-s * 0.45, s * 0.28, s * 0.9, s * 0.2, s * 0.08); g.fill(); g.stroke();
  g.restore();
  if (name) { g.fillStyle = '#000a'; g.font = 'bold ' + Math.round(H * 0.034) + 'px system-ui'; g.textAlign = 'center'; g.textBaseline = 'alphabetic'; g.fillText(name, px + 1, py - s * 1.0 + 1); g.fillStyle = '#fff'; g.fillText(name, px, py - s * 1.0); }
}
