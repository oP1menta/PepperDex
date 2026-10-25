// Shared by player.html and master.html.
// Note: don't name an element id "name": it clashes with window.name.

// Text typed by players (names) goes into innerHTML: always through esc()
const esc = s => String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

//=========ART: sprites + cries (files in web/assets, made by build_art.py)=========
// art maps "Species|Form" -> PokeAPI id; sprite = assets/sprites/<id>.png, cry = assets/cries/<id>.mp3

let art = {};
const artReady = fetch('/assets/pokemon-art.json').then(r => r.json()).then(j => { art = j; }).catch(() => {});

// sparkles of the Encounters show: a few stars scattered over the field
document.addEventListener('DOMContentLoaded', () => {
  const box = document.getElementById('sparks');
  if (!box) return;
  for (let k = 0; k < 22; k++) {
    const s = document.createElement('i');
    s.style.cssText = 'left:' + (10 + Math.random() * 80) + '%;top:' + (15 + Math.random() * 60) + '%;animation-delay:' + (Math.random() * 1.2).toFixed(2) + 's;--s:' + (12 + Math.random() * 20).toFixed(0) + 'px';
    box.appendChild(s);
  }
});

const keyOf = p => p.species + '|' + p.form;

function sprite(key, cls = '', shiny = false) {
  return art[key] ? '<img class="sprite ' + cls + '" src="/assets/sprites/' + (shiny ? 'shiny/' : '') + art[key] + '.png" alt="" title="' + esc(key.replace('|Normal', '').replace('|', ' ')) + '">' : '';
}

const soundEnabled = () => { try { return localStorage.getItem('ptu.sound') !== '0'; } catch (e) { return true; } };
function setSound(on) { try { localStorage.setItem('ptu.sound', on ? '1' : '0'); } catch (e) {} }

// browsers may refuse to play before the first tap on the page: then it is just silent
// 0..100, saved per device; the cries are loud files, so the default is low
const volume = () => { try { const v = localStorage.getItem('ptu.volume'); return v === null ? 25 : +v; } catch (e) { return 25; } };
function setVolume(v) { try { localStorage.setItem('ptu.volume', v); } catch (e) {} }

// Effect sounds (capture show, ball) have their own switch and volume, apart from the cries; the hologram has its own switch too
const lsGet = (k, d) => { try { const v = localStorage.getItem(k); return v === null ? d : v; } catch (e) { return d; } };
const lsSet = (k, v) => { try { localStorage.setItem(k, v); } catch (e) {} };
const sfxEnabled = () => lsGet('ptu.sfx', '1') !== '0';
const sfxVolume = () => +lsGet('ptu.sfxVol', 60);
const holoSoundEnabled = () => lsGet('ptu.holoSnd', '1') !== '0';

function playCry(key) {
  if (!art[key]) return;
  const audio = new Audio('/assets/cries/' + art[key] + '.mp3');
  audio.volume = volume() / 100;
  const bars = () => [...document.querySelectorAll('[data-cry]')].filter(b => b.dataset.cry === key);
  bars().forEach(b => b.classList.add('playing'));
  const stop = () => bars().forEach(b => b.classList.remove('playing'));
  audio.onended = audio.onerror = stop;
  audio.play().catch(stop);
}

// every <button data-cry="Species|Form"> plays that cry
document.addEventListener('click', e => {
  const b = e.target.closest('[data-cry]');
  if (b) playCry(b.dataset.cry);
});

const SAVED = 'ptu.sessions';  // { CODE: {token, name} }: one entry per campaign joined from this browser
const CURRENT = 'ptu.current';

const saved = () => JSON.parse(localStorage.getItem(SAVED) || '{}');
function save(code, token, name) {
  const all = saved(); all[code] = { token, name };
  localStorage.setItem(SAVED, JSON.stringify(all));
  localStorage.setItem(CURRENT, code);
}
function forget(code) {
  const all = saved(); delete all[code];
  localStorage.setItem(SAVED, JSON.stringify(all));
  if (localStorage.getItem(CURRENT) === code) localStorage.removeItem(CURRENT);
}

//=========TABS=============
// Buttons [data-tab=x] show section #tab-x and hide the other .tab sections.
// Elements with data-show-on="a b" are visible only on those tabs.
// The choice lives in the URL hash (#spawn), so F5 keeps the tab.

let currentTab = null;
let fallbackTab;

// A new wild Pokemon plays its "appears" show when the player is looking at Encounters
// (right away, or when he opens that tab).
let pendingIntro = false;
function playPendingIntro() {
  if (currentTab !== 'encounters' || !pendingIntro) return;
  pendingIntro = false;
  intro();
}

function showTab(name) {
  if (!document.getElementById('tab-' + name)) name = fallbackTab;
  currentTab = name;
  document.querySelectorAll('.tab').forEach(s => { s.hidden = s.id !== 'tab-' + name; });
  document.querySelectorAll('.tabs button').forEach(b => b.classList.toggle('active', b.dataset.tab === name));
  showTabButton(name);
  document.querySelectorAll('[data-show-on]').forEach(e => { e.hidden = !e.dataset.showOn.split(' ').includes(name); });
  for (let i = notifs.length - 1; i >= 0; i--) if (notifs[i].tab === name) notifs.splice(i, 1);
  document.querySelectorAll('.tabs-host > .screen').forEach(s => s.classList.toggle('flexcol', name === 'encounters'));
  renderBall();
  playPendingIntro();
  if (name === 'spawn' && typeof loadActive === 'function') loadActive();
  if (typeof tableSync === 'function') tableSync(name);
  history.replaceState(null, '', '#' + name);
}

function initTabs(first) {
  fallbackTab = first;
  let pos = 'side', hidden = false;
  try {
    pos = localStorage.getItem('ptu.menu') || pos;
    hidden = localStorage.getItem('ptu.menuHidden') === '1';
  } catch (e) {}
  document.body.classList.toggle('menu-hidden', hidden);
  applyTabPos(pos);
  if (window.soundBox) { soundBox.checked = soundEnabled(); soundBox.onchange = () => setSound(soundBox.checked); }
  if (window.volumeRange) { volumeRange.value = volume(); volumeRange.oninput = () => setVolume(volumeRange.value); }
  if (window.sfxBox) { sfxBox.checked = sfxEnabled(); sfxBox.onchange = () => lsSet('ptu.sfx', sfxBox.checked ? '1' : '0'); }
  if (window.sfxRange) { sfxRange.value = sfxVolume(); sfxRange.oninput = () => lsSet('ptu.sfxVol', sfxRange.value); }
  if (window.holoBox) { holoBox.checked = holoSoundEnabled(); holoBox.onchange = () => lsSet('ptu.holoSnd', holoBox.checked ? '1' : '0'); }
  tabModeSel.value = tabMode();
  window.addEventListener('resize', layoutTabs);
  initTabSwipe();
  showTab(location.hash.slice(1) || first);
}

// Where the menu sits: side | bottom | top. Remembered per device once chosen.
function setTabPos(pos) {
  try { localStorage.setItem('ptu.menu', pos); } catch (e) {}
  applyTabPos(pos);
}

function applyTabPos(pos) {
  document.body.classList.remove('tabs-bottom', 'tabs-side', 'tabs-top');
  document.body.classList.add('tabs-' + pos);
  tabPos.value = pos;
  drawToggle();
  layoutTabs();
}

// The arrow hides the menu (only the arrow stays); tap again to bring it back. Remembered per device.
function toggleMenu() {
  const hidden = document.body.classList.toggle('menu-hidden');
  try { localStorage.setItem('ptu.menuHidden', hidden ? '1' : '0'); } catch (e) {}
  drawToggle();
  layoutTabs();
}

function drawToggle() {
  const hidden = document.body.classList.contains('menu-hidden');
  const pos = ['side', 'bottom', 'top'].find(p => document.body.classList.contains('tabs-' + p));
  const arrows = { side: ['\u2039', '\u203A'], bottom: ['\u25BE', '\u25B4'], top: ['\u25B4', '\u25BE'] }[pos];  // [hide, show]
  document.querySelectorAll('.tabs-toggle').forEach(b => { b.textContent = arrows[hidden ? 1 : 0]; });
}

//=========MENU BUTTONS: rolling wheel or fixed pages=========
// More buttons than fit?
//  roll   = an endless wheel: swipe / mouse wheel moves it one button at a time, last is followed by first
//  static = fixed pages of buttons with an arrow at each end to go to the next / previous page
// With room for all of them nothing rolls and no arrows show.

const tabMode = () => { try { return localStorage.getItem('ptu.tabMode') === 'static' ? 'static' : 'roll'; } catch (e) { return 'roll'; } };
let tabStart = 0, tabCount = 99;  // index of the first visible button, how many fit

function setTabMode(mode) {
  try { localStorage.setItem('ptu.tabMode', mode); } catch (e) {}
  tabStart = 0;
  layoutTabs();
  showTabButton(currentTab);
}

function layoutTabs() {
  const list = document.querySelector('.tabs-list');
  if (!list) return;
  const btns = [...list.children], n = btns.length;
  const side = document.body.classList.contains('tabs-side');
  const prev = document.getElementById('tabsPrev'), next = document.getElementById('tabsNext');
  btns.forEach((b, i) => { b.classList.remove('off'); b.style.order = i; });
  prev.hidden = next.hidden = true;
  const room = side ? list.clientHeight : list.clientWidth;
  if (document.body.classList.contains('menu-hidden') || room <= 0) return;

  const size = side ? 56 : 66, gap = 6;  // a button's height (side) / minimum width (top, bottom), and the gap between
  const fit = r => Math.max(1, Math.floor((r + gap) / (size + gap)));
  tabCount = fit(room);
  if (tabCount >= n) { tabCount = n; tabStart = 0; return; }

  const stat = tabMode() === 'static';
  if (stat) {  // the two arrows take room too
    tabCount = fit(room - 2 * (26 + gap));
    tabStart = Math.min(tabStart, Math.max(0, n - tabCount));
    prev.hidden = next.hidden = false;
    const arrows = side ? ['\u25B2', '\u25BC'] : ['\u25C0', '\u25B6'];
    prev.textContent = arrows[0]; next.textContent = arrows[1];
    prev.disabled = tabStart <= 0;
    next.disabled = tabStart + tabCount >= n;
  }
  btns.forEach((b, i) => {
    const slot = stat ? i - tabStart : (i - tabStart + n) % n;  // roll: the wheel turns, so the order turns with it
    b.style.order = slot;
    b.classList.toggle('off', slot < 0 || slot >= tabCount);
  });
}

// static: next / previous page
function pageTabs(d) {
  tabStart = Math.max(0, tabStart + d * tabCount);
  layoutTabs();
}

// roll: one button forward / back, endless
function rollTabs(d) {
  const n = document.querySelectorAll('.tabs-list > button').length;
  tabStart = (tabStart + d + n) % n;
  layoutTabs();
}

// make sure the button of tab "name" is on screen (a notification may open a tab that is scrolled away)
function showTabButton(name) {
  const btns = [...document.querySelectorAll('.tabs-list > button')];
  const i = btns.findIndex(b => b.dataset.tab === name);
  if (i < 0 || tabCount >= btns.length) return;
  const slot = tabMode() === 'static' ? i - tabStart : (i - tabStart + btns.length) % btns.length;
  if (slot < 0 || slot >= tabCount) {
    tabStart = tabMode() === 'static' ? Math.floor(i / tabCount) * tabCount : i;
    layoutTabs();
  }
}

// mouse wheel and finger / mouse drag over the buttons turn the wheel
function initTabSwipe() {
  const list = document.querySelector('.tabs-list');
  if (!list) return;
  const rolling = () => tabMode() === 'roll' && tabCount < list.children.length;
  const vertical = () => document.body.classList.contains('tabs-side');

  let wheelAt = 0;
  list.addEventListener('wheel', e => {
    if (!rolling()) return;
    e.preventDefault();
    if (Date.now() - wheelAt < 120) return;
    wheelAt = Date.now();
    rollTabs((vertical() ? e.deltaY : e.deltaX || e.deltaY) > 0 ? 1 : -1);
  }, { passive: false });

  let from = null, moved = false;
  list.addEventListener('pointerdown', e => { from = vertical() ? e.clientY : e.clientX; moved = false; });
  list.addEventListener('pointermove', e => {
    if (from === null || !rolling()) return;
    const d = (vertical() ? e.clientY : e.clientX) - from;
    if (Math.abs(d) > 28) { rollTabs(d < 0 ? 1 : -1); from = vertical() ? e.clientY : e.clientX; moved = true; }
  });
  addEventListener('pointerup', () => { from = null; });
  list.addEventListener('click', e => { if (moved) { e.stopPropagation(); e.preventDefault(); moved = false; } }, true);
}

// Notification ball (player page): always there, black when empty. Each notification is
// {text, tab}; tapping the lit ball opens the tab of the newest one. Opening a tab
// (by the ball or by its own button) clears that tab's notifications.
const notifs = [];

function notify(text, tab, action) {
  if (currentTab === tab && !action) return;  // already looking at it
  notifs.push({ text, tab, action });
  renderBall();
}

function renderBall() {
  if (!document.getElementById('ball')) return;  // master page has no ball
  const n = notifs.length;
  ball.classList.toggle('on', n > 0);
  ball.disabled = n === 0;
  ballText.textContent = n ? notifs[n - 1].text : '';
  ballCount.textContent = n > 1 ? n : '';
}

function openNotif() {
  const n = notifs.pop();
  if (n && n.action) n.action();  // e.g. the Master's "adjust this Pokemon" request
  else if (n) showTab(n.tab);
}

//=========PLAYER=============

const NAME = 'ptu.name';  // the person's own name, kept in this browser

// 'nameScreen' | 'homeScreen' | 'playingBox': only one is visible
function screen(id) {
  for (const s of ['nameScreen', 'homeScreen', 'playingBox']) document.getElementById(s).hidden = s !== id;
}

function saveName() {
  const n = playerNameInput.value.trim();
  if (!n) { out.textContent = t('typeName'); return; }
  localStorage.setItem(NAME, n);
  out.textContent = '';
  showHome();
}

function changeName() {
  playerNameInput.value = localStorage.getItem(NAME) || '';
  screen('nameScreen');
}

//=========PLAYER: MY CHARACTERS (made before any session)=========
// The "person key" is a random secret kept in this browser: it owns the characters.
function person() {
  let k = localStorage.getItem('ptu.person');
  if (!/^[0-9a-f]{32}$/.test(k || '')) {
    k = [...crypto.getRandomValues(new Uint8Array(16))].map(b => b.toString(16).padStart(2, '0')).join('');
    localStorage.setItem('ptu.person', k);
  }
  return k;
}

async function loadSheets(method = 'GET', extra = {}, path = '/api/sheets') {
  const q = new URLSearchParams({ person: person(), ...extra });
  const r = method === 'GET' ? await fetch(path + '?' + q) : await fetch(path, { method, body: q });
  if (!r.ok) { out.textContent = errText((await r.json()).error); return []; }
  return r.json();
}

// Home screen: the list (with "bring" ticks) + delete buttons
async function renderSheets() {
  const list = await loadSheets();
  sheetList.innerHTML = list.length
    ? list.map(c => '<div class="wild"><b>' + esc(c.name) + '</b> <button onclick="deleteSheet(' + c.id + ')">' + t('del') + '</button></div>').join('')
    : t('noSheets');
  bringList.innerHTML = list.map(c => '<label><input type="checkbox" value="' + c.id + '" checked> ' + esc(c.name) + '</label>').join('');
}

async function createSheet() {
  await loadSheets('POST', { name: sheetNameInput.value });
  sheetNameInput.value = '';
  renderSheets();
}

async function deleteSheet(id) {
  if (!await askConfirm(t('delSheet'))) return;
  await loadSheets('POST', { sheetId: id }, '/api/sheet/delete');
  renderSheets();
}

// In a session: put one of my characters here (an independent copy)
async function fillSheetSelect() {
  const c = auth();
  const list = (await loadSheets('GET', c)).filter(s => !s.placed);
  sheetSelect.innerHTML = list.length
    ? list.map(s => '<option value="' + s.id + '">' + esc(s.name) + '</option>').join('')
    : '<option value="" disabled>' + t('noneLeft') + '</option>';
}

async function placeSheet() {
  if (!sheetSelect.value) return;
  const c = auth();
  const r = await fetch('/api/place', { method: 'POST', body: new URLSearchParams({ ...c, person: person(), sheetId: sheetSelect.value }) });
  if (!r.ok) { out.textContent = errText((await r.json()).error); return; }
  loadTrainers();
}

function showHome() {
  clearInterval(pollTimer);
  who.textContent = localStorage.getItem(NAME);
  sessionBtns.hidden = true; welcome.textContent = '';
  renderSaved();
  renderSheets();
  screen('homeScreen');
}

// Connect = join the session with my name + create the first trainer
async function join() {
  const body = new URLSearchParams({ code: codeInput.value, name: localStorage.getItem(NAME) });
  const r = await fetch('/api/join', { method: 'POST', body });
  const j = await r.json();
  if (!r.ok) {
    if (j.rejoin) { showRejoinAsk(localStorage.getItem(NAME)); out.textContent = ''; } else out.textContent = errText(j.error);
    return;
  }

  rejoinBox.hidden = true;
  save(j.code, j.token, j.name);
  for (const box of bringList.querySelectorAll('input:checked')) {
    await fetch('/api/place', { method: 'POST', body: new URLSearchParams({ code: j.code, token: j.token, person: person(), sheetId: box.value }) });
  }
  showPlaying(j);
}

// The name is already in the session: maybe it is me coming back (new phone, new link...). Ask the Master to confirm.
let rejoinTimer = 0;
function showRejoinAsk(name) {
  clearInterval(rejoinTimer);
  rejoinBox.hidden = false;
  rejoinBox.innerHTML = '<p>' + t('rjAsk', esc(name)) + '</p><button class="primary" onclick="askRejoin()">' + t('rjBtn') + '</button>';
}

async function askRejoin() {
  const r = await fetch('/api/rejoin/request', { method: 'POST', body: new URLSearchParams({ code: codeInput.value, name: localStorage.getItem(NAME) }) });
  const j = await r.json();
  if (!r.ok) { rejoinBox.innerHTML = '<p>' + esc(errText(j.error)) + '</p>'; return; }
  rejoinBox.innerHTML = '<p class="wait">\u23F3 ' + t('rjWait') + '</p><button onclick="cancelRejoin()">' + t('rjCancel') + '</button>';
  rejoinTimer = setInterval(async () => {
    const s = await (await fetch('/api/rejoin/status?id=' + j.id + '&secret=' + j.secret)).json().catch(() => ({ status: 'pending' }));
    if (s.status === 'pending') return;
    clearInterval(rejoinTimer);
    if (s.status === 'approved') { rejoinBox.hidden = true; save(s.player.code, s.player.token, s.player.name); showPlaying(s.player); return; }
    rejoinBox.innerHTML = '<p>' + t(s.status === 'denied' ? 'rjDenied' : 'rjGone') + '</p>';
  }, 2000);
}

function cancelRejoin() { clearInterval(rejoinTimer); rejoinBox.hidden = true; }

// Lost the browser data? The Master gives a one-time code: same player, same characters.
async function reconnect() {
  const body = new URLSearchParams({ code: codeInput.value, claim: claimInput.value });
  const r = await fetch('/api/claim', { method: 'POST', body });
  const j = await r.json();
  if (!r.ok) { out.textContent = errText(j.error); return; }
  save(j.code, j.token, j.name);
  showPlaying(j);
}

// On page load: ask for the name once, then reopen the last campaign.
// The token proves who we are: no duplicate player, and the name can't be hijacked.
function restorePlayer() {
  localStorage.removeItem('ptu.player');  // old format, no token
  if (!localStorage.getItem(NAME)) { screen('nameScreen'); return; }
  const code = localStorage.getItem(CURRENT);
  if (code && saved()[code]) openCampaign(code); else showHome();
}

async function openCampaign(code) {
  const me = saved()[code];
  const r = await fetch('/api/me?code=' + encodeURIComponent(code) + '&token=' + me.token);
  if (!r.ok) {
    out.textContent = errText((await r.json()).error) + ' (' + code + ')';
    forget(code);
    showHome();
    return;
  }
  localStorage.setItem(CURRENT, code);
  showPlaying(await r.json());
}

// Campaigns this browser already joined: tap to switch
function renderSaved() {
  const codes = Object.keys(saved());
  savedBox.innerHTML = codes.length
    ? '<h2>' + t('yourCampaigns') + '</h2>' + codes.map(c =>
        '<button onclick="openCampaign(\'' + c + '\')">' + c + ' &middot; ' + esc(saved()[c].name) + '</button>').join('')
    : '';
}

function showPlaying(j) {
  screen('playingBox');
  welcome.textContent = t('welcome', j.name, j.playerId, j.code);
  sessionBtns.hidden = false;
  out.textContent = '';
  lastTeamTotal = null;
  loadTrainers();
  startPolling(j.code);
}

async function leave() {
  const code = localStorage.getItem(CURRENT);
  if (!code || !await askConfirm(t('confirmLeave', code))) return;
  await fetch('/api/leave', { method: 'POST', body: new URLSearchParams({ code, token: saved()[code].token }) });
  forget(code);
  showHome();
}

// Ask the server every 2s for new wild Pokemon in our session (this also marks us online).
// First call has no "after": the server just says where the list is now.
// Encounters: one wild Pokemon per "page" (wilds[cur]), arrows go through them
let wilds = [], cur = 0;

function renderWild() {
  const n = wilds.length, p = wilds[cur];
  fieldEmpty.hidden = n > 0;
  arena.hidden = !n;
  wildCount.textContent = n ? (cur + 1) + ' / ' + n : '';
  prevWild.disabled = nextWild.disabled = n < 2;
  catchBtn.disabled = !n;
  if (!n) { field.dataset.phase = ''; return; }
  const key = keyOf(p);
  field.classList.toggle('shiny', !!p.shiny);
  wildImg.style.bottom = '';
  wildImg.src = art[key] ? '/assets/sprites/' + (p.shiny ? 'shiny/' : '') + art[key] + '.png' : '';
  const name = esc(p.species) + (p.form !== 'Normal' ? ' (' + esc(p.form) + ')' : '');
  wildInfo.innerHTML = '<b>' + (p.shiny ? '<span class="shinyname">' + t('shiny') + ' ' + name + '</span>' : name) + '</b><br>' + t('lv') + ' ' + p.level +
    ' &middot; ' + p.types.join('/') + ' &middot; ' + p.gender +
    ' <button class="cry" data-cry="' + esc(key) + '" aria-label="Cry"><i></i><i></i><i></i><i></i><i></i></button>' +
    '<div id="wildLive"></div>';
  renderWildLive();
}

// HP bar + status effects of the Pokemon on screen (the Master changes them; the poll brings the numbers)
let wildStates = {};
function renderWildLive() {
  const p = wilds[cur];
  if (!p || !window.wildLive) return;
  const s = wildStates[p.id] || { hp: p.hp, maxHp: p.hp || 1, fx: [], inj: 0 };
  const pct = Math.max(0, Math.min(100, Math.round(100 * s.hp / s.maxHp)));
  wildLive.innerHTML = '<div class="bar hp ' + (pct > 50 ? 'ok' : pct > 25 ? 'mid' : 'low') + '"><i style="width:' + pct + '%"></i></div>' +
    '<small>HP ' + Math.round(s.hp) + ' / ' + s.maxHp + (s.tries >= 0 ? ' &middot; ' + t('rcTries', s.tries) : '') + '</small>' +
    '<div class="fxrow">' + s.fx.map(c => '<span class="chip fx-' + c + '">' + t('fx_' + c) + '</span>').join('') +
    (s.inj ? '<span class="chip fx-injury">' + t('rcInjury', s.inj) + '</span>' : '') + '</div>';
}

// Sprites have empty margin under the feet: find the lowest visible row so the feet touch the shadow
if (window.wildImg) wildImg.onload = () => {
  try {
    const c = document.createElement('canvas');
    c.width = wildImg.naturalWidth; c.height = wildImg.naturalHeight;
    const g = c.getContext('2d'); g.drawImage(wildImg, 0, 0);
    const px = g.getImageData(0, 0, c.width, c.height).data;
    let last = c.height - 1;
    while (last > 0 && ![...Array(c.width).keys()].some(x => px[(last * c.width + x) * 4 + 3] > 20)) last--;
    const gap = (c.height - 1 - last) / c.height * wildImg.clientHeight;
    wildImg.style.bottom = 'calc(50% - ' + (gap - 4).toFixed(1) + 'px)';
  } catch (e) {}
};

// "Appears" show: 1 black screen, 2 sparkles, 3 black silhouette, 4 reveal + cry
let introTimers = [];
function intro() {
  introTimers.forEach(clearTimeout);
  renderWild();
  const p = wilds[cur];
  if (!p) return;
  const phase = (name, ms) => introTimers.push(setTimeout(() => { field.dataset.phase = name; }, ms));
  field.dataset.phase = 'dark';
  phase('sparks', 500);
  phase('shadow', 1900);
  phase('reveal', 3000);
  introTimers.push(setTimeout(() => { if (soundEnabled()) playCry(keyOf(p)); }, 3000));
  phase('done', 3800);
}

function stepWild(d) {
  introTimers.forEach(clearTimeout);
  cur = (cur + d + wilds.length) % wilds.length;
  renderWild();
  field.dataset.phase = 'reveal';  // quick reveal, no show
}
// a Pokemon someone caught leaves everybody's field
function dropGone(ids) {
  if (capturing || !wilds.some(w => ids.includes(w.id))) return;
  const here = wilds[cur];
  wilds = wilds.filter(w => !ids.includes(w.id));
  cur = Math.max(0, Math.min(here ? wilds.indexOf(here) : cur, wilds.length - 1));
  renderWild();
  if (wilds.length) field.dataset.phase = 'reveal';
}

//=========PLAYER: CAPTURE=============
// The server rolls (PTU) and spends the ball; the page only plays what the server said.
// Ball pictures: web/assets/items/<skin>.png (PokeAPI item sprites, same pixel style as the Pokemon).

let capturing = false, capTrainerId = null;
const sleep = ms => new Promise(r => setTimeout(r, ms));
// look of each ball's throw: trail colours + spin + how big the burst is
const BALL_FX = {
  poke:   { colors: ['#ffffff', '#ffcdd2'], spin: 540,  trail: 1, burst: 12 },
  great:  { colors: ['#4fc3f7', '#e3f2fd'], spin: 720,  trail: 1.4, burst: 16 },
  ultra:  { colors: ['#ffd740', '#fff59d'], spin: 900,  trail: 1.8, burst: 20 },
  master: { colors: ['#e040fb', '#f48fb1', '#ffffff'], spin: 1260, trail: 2.6, burst: 30 } };

const ballImg = (skin, size, cls = '') => '<img class="ballimg ' + cls + '" src="/assets/items/' + esc(skin) + '.png?v=75" style="width:' + size + 'px;height:' + size + 'px" alt="">';

function captureWild() {
  if (capturing || !wilds.length) return;
  if (!pcData.length) { out.textContent = t('capNoChar'); return; }
  if (!pcData.some(tr => tr.id === capTrainerId)) capTrainerId = pcTrainerId ?? pcData[0].id;
  if (!ballPicker.hidden) { ballPicker.hidden = true; return; }
  renderBallPicker();
}

function renderBallPicker() {
  const tr = pcData.find(x => x.id === capTrainerId);
  const balls = tr.items.filter(i => i.skin);
  ballPicker.innerHTML = '<div class="sheet-head"><b>' + t('pickBall') + '</b>' +
    (pcData.length > 1 ? '<select onchange="capTrainerId = +this.value; renderBallPicker()">' +
      pcData.map(x => '<option value="' + x.id + '"' + (x.id === capTrainerId ? ' selected' : '') + '>' + esc(x.name) + '</option>').join('') + '</select>' : '') +
    '</div>' +
    (balls.length
      ? '<div class="ball-grid">' + balls.map(b => '<button class="slot" onclick="throwBall(' + b.id + ')">' + ballImg(b.skin, 52) + '<span class="nm">' + esc(b.name) + '</span><b class="qty">x' + b.qty + '</b></button>').join('') + '</div>'
      : '<p>' + t('noBalls') + '</p>') +
    '<button class="cancel" onclick="ballPicker.hidden = true">' + t('cancel') + '</button>';
  ballPicker.hidden = false;
}

async function throwBall(ballId) {
  const wild = wilds[cur];
  ballPicker.hidden = true;
  if (capturing || !wild) return;
  capturing = true;
  document.querySelectorAll('#fieldBar button').forEach(b => { b.disabled = true; });
  catchMsg.hidden = true;
  try {
    const r = await fetch('/api/capture', { method: 'POST', body: new URLSearchParams({ ...auth(), trainerId: capTrainerId, pokemonId: wild.id, ballId }) });
    const j = await r.json();
    const drop = () => { wilds = wilds.filter(w => w !== wild); cur = Math.max(0, Math.min(cur, wilds.length - 1)); };
    if (!r.ok) {
      showCatchMsg(j.error === 'noroom' ? t('capNoRoom') : j.error === 'noball' ? t('noBalls') : t('capGone'));
      if (j.error === 'gone') drop();
    } else {
      if (j.caught && lastTeamTotal !== null) lastTeamTotal++;  // already told by the animation: no "you received" notification
      await playRoll(j);
      await playThrow(j);
      const name = wild.species + (wild.form !== 'Normal' ? ' (' + wild.form + ')' : '');
      if (wildStates[wild.id]) wildStates[wild.id].tries = j.left;
      if (j.sticker) notify(t('newSticker'), 'dex');
      showCatchMsg(j.caught ? t('gotcha', esc(name)) : j.fled ? t('fled', esc(name)) : t('brokeFree', esc(name)));
      if (j.caught || j.fled) drop();
    }
  } catch (e) {
    showCatchMsg(esc(String(e)));
  } finally {
    capturing = false;
    renderWild();
    if (wilds.length) field.dataset.phase = 'reveal';
    await loadTrainers();
  }
}

function showCatchMsg(html) {
  catchMsg.innerHTML = html;
  catchMsg.hidden = false;
  clearTimeout(showCatchMsg.timer);
  showCatchMsg.timer = setTimeout(() => { catchMsg.hidden = true; }, 4000);
}

//=========SOUND EFFECTS (capture show)=========
// All synthesised, no sound files. Metal = a short noise "strike" plus partials that are NOT harmonics
// (ratios of a struck steel plate), which is what makes it clang instead of beep. Own switch and volume
// (settings: "Effect sounds"), apart from the cries; the hologram has its own switch.
const sfx = (() => {
  const ctx = () => { try { const c = tick.ctx = tick.ctx || new AudioContext(); if (c.state === 'suspended') c.resume(); return c; } catch (e) { return null; } };
  const vol = () => Math.max(0.005, sfxVolume() / 100 * 0.55);
  const out = c => { if (!out.bus || out.bus.ctx !== c) { const comp = c.createDynamicsCompressor(); comp.connect(c.destination); out.bus = comp; out.bus.ctx = c; } return out.bus; };
  const METAL = [1, 2.32, 4.25, 6.63, 9.38];  // inharmonic partials of a struck plate
  // one metal strike: base Hz, ring time in seconds, relative volume, brightness (0..1), delay
  const clang = (c, f, ring, v, bright = .6, delay = 0) => {
    const t = c.currentTime + delay, bus = out(c);
    METAL.forEach((m, i) => {
      const o = c.createOscillator(), g = c.createGain();
      o.type = i === 0 ? 'triangle' : 'sine'; o.frequency.value = f * m;
      const peak = vol() * v * (i === 0 ? 1 : .55 * Math.pow(bright, i * .5));
      g.gain.setValueAtTime(0.0001, t); g.gain.exponentialRampToValueAtTime(peak, t + .003);
      g.gain.exponentialRampToValueAtTime(0.0001, t + ring / (1 + i * .55));
      o.connect(g); g.connect(bus); o.start(t); o.stop(t + ring + .05);
    });
    noise(c, .035, 5000, v * .9, delay, 'highpass');  // the hit itself
  };
  // short burst of filtered noise: type + frequency choose the colour
  const noise = (c, dur, freq, v, delay = 0, type = 'bandpass') => {
    const n = Math.floor(c.sampleRate * dur), buf = c.createBuffer(1, n, c.sampleRate), d = buf.getChannelData(0);
    for (let i = 0; i < n; i++) d[i] = (Math.random() * 2 - 1) * Math.pow(1 - i / n, 2);
    const s = c.createBufferSource(), f = c.createBiquadFilter(), g = c.createGain();
    s.buffer = buf; f.type = type; f.frequency.value = freq; f.Q.value = 1.1; g.gain.value = vol() * v;
    s.connect(f); f.connect(g); g.connect(out(c)); s.start(c.currentTime + delay);
  };
  // many tiny noise bursts at random moments inside "span" seconds: crackle of paper / adhesive
  const grain = (c, count, span, f0, f1, v, delay = 0) => {
    for (let i = 0; i < count; i++) noise(c, .006 + Math.random() * .012, f0 + (f1 - f0) * Math.random(), v * (.5 + Math.random() * .7), delay + Math.random() * span);
  };
  // soft sine twinkle (a shiny sparkle): quick attack, short decay
  const twinkle = (c, f, delay = 0) => {
    const t = c.currentTime + delay, o = c.createOscillator(), g = c.createGain();
    o.type = 'sine'; o.frequency.value = f; g.gain.setValueAtTime(0.0001, t); g.gain.exponentialRampToValueAtTime(vol() * .5, t + .01); g.gain.exponentialRampToValueAtTime(0.0001, t + .35);
    o.connect(g); g.connect(out(c)); o.start(t); o.stop(t + .4);
  };
  // radio static that is "tuned": noise through a band filter that sweeps, chopped by a fast tremolo (crackle)
  const radio = (c, dur, f0, f1, v, delay = 0) => {
    const t = c.currentTime + delay, n = Math.floor(c.sampleRate * dur), buf = c.createBuffer(1, n, c.sampleRate), d = buf.getChannelData(0);
    for (let i = 0; i < n; i++) d[i] = Math.random() * 2 - 1;
    const s = c.createBufferSource(), f = c.createBiquadFilter(), g = c.createGain(), lfo = c.createOscillator(), lg = c.createGain();
    s.buffer = buf; f.type = 'bandpass'; f.Q.value = 3.5;
    f.frequency.setValueAtTime(f0, t); f.frequency.exponentialRampToValueAtTime(f1, t + dur);
    g.gain.setValueAtTime(0.0001, t); g.gain.exponentialRampToValueAtTime(vol() * v, t + dur * .15); g.gain.exponentialRampToValueAtTime(0.0001, t + dur);
    lfo.type = 'square'; lfo.frequency.value = 38; lg.gain.value = vol() * v * .6;  // crackle: the gain is chopped
    lfo.connect(lg); lg.connect(g.gain);
    s.connect(f); f.connect(g); g.connect(out(c));
    s.start(t); lfo.start(t); lfo.stop(t + dur); s.stop(t + dur);
  };
  // futuristic blip: a short square/sine chirp
  const chirp = (c, f0, f1, dur, v, delay = 0) => {
    const t = c.currentTime + delay, o = c.createOscillator(), g = c.createGain();
    o.type = 'square'; o.frequency.setValueAtTime(f0, t); o.frequency.exponentialRampToValueAtTime(f1, t + dur);
    g.gain.setValueAtTime(vol() * v * .35, t); g.gain.exponentialRampToValueAtTime(0.0001, t + dur);
    o.connect(g); g.connect(out(c)); o.start(t); o.stop(t + dur + .02);
  };
  const SCALE = [0, 2, 4, 7, 9, 12, 14, 16, 19];  // major pentatonic: each bonus a step higher, never sour
  const on = fn => (...a) => { if (!sfxEnabled()) return; const c = ctx(); if (c) fn(c, ...a); };
  const onHolo = fn => (...a) => { if (!sfxEnabled() || !holoSoundEnabled()) return; const c = ctx(); if (c) fn(c, ...a); };
  return {
    up: on((c, i) => { const f = 330 * Math.pow(2, SCALE[Math.min(i, SCALE.length - 1)] / 12); clang(c, f, .55, 1, .8); clang(c, f * 1.5, .35, .35, .8, .07); }),  // clang + a quick fifth above
    down: on(c => { clang(c, 120, .5, 1.2, .35); noise(c, .12, 600, .5); }),                                                                // heavy dull clunk
    knock: on((c, p = 1) => { clang(c, 210, .22, .8 * p, .3); noise(c, .06, 2200, .6 * p); }),                                              // metal on the table
    land: on(c => { clang(c, 90, .9, 1.3, .4); clang(c, 260, .5, .6, .7, .03); }),                                                          // the die stops: a deep ring
    holo: onHolo(c => { radio(c, .9, 700, 4200, 1.1); chirp(c, 1500, 3000, .07, 1, .5); chirp(c, 2000, 4000, .07, 1, .62); chirp(c, 2600, 5200, .09, 1, .74); }),
    // sticker / paper sounds: only bursts of bright noise (no metal): the crackle of adhesive and paper
    lift: on(c => { grain(c, 8, .11, 2500, 7500, .6); noise(c, .12, 1800, .25); }),                                                       // sticker pulled off its sheet: "trrk"
    peel: on((c, speed = 1) => { grain(c, 4, .05, 3000, 8500, .4 * Math.min(1, speed)); noise(c, .08, 2300 + Math.random() * 900, .2 * Math.min(1, speed)); }),  // finger rubbing it down
    stick: on((c, shiny) => { noise(c, .08, 380, 1.1, 0, 'lowpass'); noise(c, .03, 2600, .5); if (shiny) { twinkle(c, 1760, 0.05); twinkle(c, 2349, .13); twinkle(c, 2960, .21); } }),  // pressed flat: a soft "pap"
    nope: on(c => { noise(c, .1, 320, .8, 0, 'lowpass'); }),
    rattle: on(c => { grain(c, 5, .08, 800, 3500, .3); }),
    tear: on(c => { grain(c, 6, .06, 1500, 9000, .7); }),                                                                                 // foil / paper rip, while the finger moves
    tearEnd: on(c => { grain(c, 16, .2, 1000, 8500, 1); noise(c, .1, 420, 1, .18, 'lowpass'); }),
    reveal: on((c, shiny) => { grain(c, 3, .05, 3000, 8000, .4); noise(c, .08, 500, .6, 0, 'lowpass'); if (shiny) { twinkle(c, 1760, .04); twinkle(c, 2349, .12); } }),
    glitch: onHolo(c => { radio(c, .22, 1800, 900, .9); chirp(c, 3500, 1200, .06, .8, .04); }),                                               // the number changed
  };
})();

//=========METAL d100=========
// A real 100-sided die: 100 flat faces, each with its own number, drawn on a canvas (no library, works offline).
// How it is built: 100 evenly spread points on a sphere; the die is the solid whose faces are the planes touching
// the sphere at those points (the dual of the points' convex hull). It spins and bounces, then settles with the
// rolled number facing you; the face lights up and the number is projected above it like a hologram.
// Looks: DIE_SKINS (colour and an optional texture picture painted on every face).

const DIE_SKINS = {
  steel: { base: [150, 156, 164], edge: 'rgba(12,18,26,.65)', ink: '#10161d', texture: '' },
  gold:  { base: [200, 162, 70],  edge: 'rgba(45,28,5,.65)',  ink: '#2a1a05', texture: '' },
  // texture: '/assets/dice/<picture>.png' is tiled over every face, under the light and shade (see web/assets/dice/LEIA-ME.txt)
};
const dieSkin = () => { let k = 'steel'; try { k = localStorage.getItem('ptu.die') || 'steel'; } catch (e) {} return DIE_SKINS[k] || DIE_SKINS.steel; };

const V3 = {
  dot: (a, b) => a[0] * b[0] + a[1] * b[1] + a[2] * b[2],
  cross: (a, b) => [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]],
  sub: (a, b) => [a[0] - b[0], a[1] - b[1], a[2] - b[2]],
  unit: v => { const l = Math.hypot(...v); return v.map(x => x / l); },
  // 3x3 matrices as 3 rows
  app: (M, v) => [V3.dot(M[0], v), V3.dot(M[1], v), V3.dot(M[2], v)],
  mul: (A, B) => A.map(r => [0, 1, 2].map(j => r[0] * B[0][j] + r[1] * B[1][j] + r[2] * B[2][j])),
  rot: (ax, a) => {  // rotation by angle a around the unit axis ax (Rodrigues)
    const [x, y, z] = ax, c = Math.cos(a), s = Math.sin(a), d = 1 - c;
    return [[c + x * x * d, x * y * d - z * s, x * z * d + y * s], [y * x * d + z * s, c + y * y * d, y * z * d - x * s], [z * x * d - y * s, z * y * d + x * s, c + z * z * d]];
  },
};

const DIE = (() => {
  const N = 100, pts = [];
  for (let i = 0; i < N; i++) { const y = 1 - 2 * (i + .5) / N, r = Math.sqrt(1 - y * y), a = i * 2.399963; pts.push([r * Math.cos(a), y, r * Math.sin(a)]); }
  const tri = [];  // hull triangles + the point where their 3 face planes meet (a corner of the die)
  for (let a = 0; a < N; a++) for (let b = a + 1; b < N; b++) for (let c = b + 1; c < N; c++) {
    const A = pts[a], B = pts[b], C = pts[c];
    let n = V3.cross(V3.sub(B, A), V3.sub(C, A));
    if (V3.dot(n, A) < 0) n = n.map(x => -x);
    const d = V3.dot(n, A);
    let ok = true;
    for (let k = 0; k < N && ok; k++) if (k !== a && k !== b && k !== c && V3.dot(n, pts[k]) > d + 1e-9) ok = false;
    if (!ok) continue;
    const bc = V3.cross(B, C), ca = V3.cross(C, A), ab = V3.cross(A, B), det = V3.dot(A, bc);
    tri.push({ ids: [a, b, c], v: [0, 1, 2].map(i => (bc[i] + ca[i] + ab[i]) / det) });
  }
  // numbers 1..100 shuffled with a fixed seed: the same die every time
  const nums = Array.from({ length: N }, (_, i) => i + 1);
  let seed = 7;
  for (let i = N - 1; i > 0; i--) { seed = (seed * 16807) % 2147483647; const j = seed % (i + 1); [nums[i], nums[j]] = [nums[j], nums[i]]; }
  const faces = pts.map((n, i) => {
    const e1 = V3.unit(V3.cross(n, Math.abs(n[1]) < .9 ? [0, 1, 0] : [1, 0, 0])), e2 = V3.cross(n, e1);  // e1 x e2 = n
    const ang = v => Math.atan2(V3.dot(v, e2), V3.dot(v, e1));
    const vs = tri.filter(t => t.ids.includes(i)).map(t => t.v).sort((p, q) => ang(p) - ang(q));
    seed = (seed * 16807) % 2147483647;
    return { n, e1, e2, vs, num: nums[i], shade: .9 + (seed % 1000) / 5000 };  // each face a little lighter or darker: hammered metal
  });
  return { faces, byNum: Object.fromEntries(faces.map(f => [f.num, f])) };
})();

// One frame. st = { M: rotation, lift, dx, glow 0..1, label, time, target: the face that landed }
function drawDie(cv, st, skin, pattern) {
  const g = cv.getContext('2d'), W = cv.width, H = cv.height, R = H * 0.25;
  const cx = W / 2 + st.dx, floor = H * 0.9, cy = floor - R - st.lift;
  g.clearRect(0, 0, W, H);

  // table shadow: smaller and lighter while the die is in the air
  const air = Math.min(1, st.lift / (H * .3)), sh = 1 - air * .45;
  g.fillStyle = 'rgba(0,0,0,' + (.5 * sh) + ')';
  g.beginPath(); g.ellipse(cx, floor + 2, R * .95 * sh, R * .2 * sh, 0, 0, 7); g.fill();

  const L = [-0.45, -0.6, 0.66], hv = [L[0], L[1], L[2] + 1], hl = Math.hypot(...hv);  // light, and the half vector for the shine
  const px = (p) => [cx + p[0] * R, cy + p[1] * R];
  for (const f of DIE.faces) {
    const n = V3.app(st.M, f.n);
    if (n[2] <= 0.02) continue;  // faces turned away are hidden
    const vs = f.vs.map(v => px(V3.app(st.M, v)));
    const lum = (.2 + .8 * Math.max(0, V3.dot(n, L))) * f.shade;
    const spec = Math.pow(Math.max(0, V3.dot(n, hv) / hl), 22);
    const path = () => { g.beginPath(); vs.forEach((q, i) => g[i ? 'lineTo' : 'moveTo'](q[0], q[1])); g.closePath(); };

    g.fillStyle = 'rgb(' + skin.base.map(v => Math.round(v * Math.min(1.1, lum + .15))).join(',') + ')';
    path(); g.fill();
    if (pattern) { g.save(); g.globalAlpha = .75; g.fillStyle = pattern; path(); g.fill(); g.restore(); }
    g.fillStyle = 'rgba(0,0,0,' + Math.max(0, (1 - lum) * .7) + ')'; path(); g.fill();        // shade
    if (spec > .02) { g.fillStyle = 'rgba(255,255,255,' + Math.min(.85, spec * .8) + ')'; path(); g.fill(); }  // shine
    g.strokeStyle = skin.edge; g.lineWidth = 1.2; path(); g.stroke();

    // the engraved number lies in the face's plane: its two axes are drawn as the transform
    if (n[2] > .22) {
      const a = V3.app(st.M, f.e1), b = V3.app(st.M, f.e2), c = px(n), s = R / 100;
      const lit = st.target === f && st.glow > 0;
      g.save();
      g.transform(a[0] * s, a[1] * s, b[0] * s, b[1] * s, c[0], c[1]);
      g.font = '700 17px Georgia, serif'; g.textAlign = 'center'; g.textBaseline = 'middle';
      const txt = String(f.num);
      if (lit) { g.shadowColor = '#00e5ff'; g.shadowBlur = 14 * st.glow; g.fillStyle = '#c9faff'; }
      else { g.fillStyle = 'rgba(255,255,255,.28)'; g.fillText(txt, .6, 1.1); g.fillStyle = skin.ink; }
      g.fillText(txt, 0, 0);
      if (/[69]/.test(txt) && !lit) g.fillRect(-g.measureText(txt).width / 2, 10, g.measureText(txt).width, 1.6);  // underline: 6 and 9 look alike
      g.restore();
    }
  }

  // the face that landed lights up
  if (st.target && st.glow > 0) {
    const f = st.target, vs = f.vs.map(v => px(V3.app(st.M, v)));
    g.save(); g.globalCompositeOperation = 'lighter';
    g.beginPath(); vs.forEach((q, i) => g[i ? 'lineTo' : 'moveTo'](q[0], q[1])); g.closePath();
    g.fillStyle = 'rgba(40,200,255,' + (.3 * st.glow) + ')'; g.shadowColor = '#00e5ff'; g.shadowBlur = 22 * st.glow; g.fill();
    g.strokeStyle = 'rgba(160,245,255,' + st.glow + ')'; g.lineWidth = 2; g.stroke();
    g.restore();
    drawHolo(g, W, H, cx, cy, R, st);
  }
}

// The number as a hologram floating over the die: light cone, scanlines, a little flicker
let holoCv = null;
function drawHolo(g, W, H, cx, cy, R, st) {
  const hy = cy - R - H * .2, th = H * .26, k = st.glow, pop = 1 + (1 - st.pop) * .5;
  // cone of light from the face up to the number
  const grad = g.createLinearGradient(0, cy - R * .4, 0, hy);
  grad.addColorStop(0, 'rgba(60,210,255,' + (.35 * k) + ')'); grad.addColorStop(1, 'rgba(60,210,255,0)');
  g.fillStyle = grad;
  g.beginPath(); g.moveTo(cx - R * .35, cy - R * .4); g.lineTo(cx + R * .35, cy - R * .4); g.lineTo(cx + R * 1.05, hy); g.lineTo(cx - R * 1.05, hy); g.closePath(); g.fill();
  // number drawn on its own canvas so the scanlines can be cut out of it
  holoCv = holoCv || document.createElement('canvas');
  holoCv.width = W; holoCv.height = Math.ceil(th * 1.6);
  const h = holoCv.getContext('2d'), mid = holoCv.height / 2;
  h.font = '900 ' + Math.round(th) + 'px "Courier New", monospace'; h.textAlign = 'center'; h.textBaseline = 'middle';
  h.shadowColor = '#00e5ff'; h.shadowBlur = 22; h.fillStyle = '#d4fcff';
  h.save(); h.translate(W / 2, mid); h.scale(pop, pop); h.fillText(st.label, 0, 0); h.restore();
  h.globalCompositeOperation = 'destination-out'; h.shadowBlur = 0; h.fillStyle = 'rgba(0,0,0,.55)';
  const off = (st.time / 30) % 4;
  for (let y = -off; y < holoCv.height; y += 4) h.fillRect(0, y, W, 1.6);  // scanlines crawl upward
  g.save();
  g.globalAlpha = k * (.82 + .18 * Math.sin(st.time / 23) * Math.random());
  g.globalCompositeOperation = 'lighter';
  g.drawImage(holoCv, 0, hy - mid + th * .2);
  g.restore();
  // projector ring on top of the die
  g.strokeStyle = 'rgba(120,235,255,' + (.7 * k) + ')'; g.lineWidth = 1.5;
  g.beginPath(); g.ellipse(cx, cy - R * .62, R * .5, R * .13, 0, 0, 7); g.stroke();
}

// Starts the roll: ~2.4 s of spinning and 3 bounces, then it settles on the rolled number.
// Returns { landed: Promise, setLabel(text), stop() }. skipped() fast-forwards to the landing.
function startDie(cv, number, skipped) {
  const skin = dieSkin();
  let pattern = null;
  if (skin.texture) { const img = new Image(); img.onload = () => { pattern = cv.getContext('2d').createPattern(img, 'repeat'); }; img.src = skin.texture; }
  const target = DIE.byNum[number];
  const final = V3.mul(V3.rot([1, 0, 0], -0.22), [target.e1, target.e2, target.n]);  // face upright, tilted a little so it looks solid
  const rnd = () => V3.unit([Math.random() - .5, Math.random() - .5, Math.random() - .5]);
  const ax1 = rnd(), ax2 = rnd(), A1 = Math.PI * 2 * (2 + Math.random() * 1.5), A2 = Math.PI * 2 * (1 + Math.random());
  const dur = 2400, t0 = performance.now();
  let hits = 0, landedAt = null, label = String(number), labelAt = 0, stopped = false, done;
  const landed = new Promise(r => { done = r; });
  const frame = now => {
    if (stopped) return;
    const u = landedAt !== null ? 1 : skipped() ? 1 : Math.min(1, (now - t0) / dur);
    const k = Math.pow(1 - u, 3);  // spin slows down
    const M = V3.mul(V3.rot(ax1, A1 * k), V3.mul(V3.rot(ax2, A2 * k), final));
    const drop = Math.pow(Math.max(0, 1 - u / .16), 2) * cv.height * .5;  // falls in from above
    const lift = drop + Math.abs(Math.sin(u * Math.PI * 3.5)) * Math.pow(1 - u, 1.4) * cv.height * .28;
    const phase = Math.floor(u * 3.5);
    if (u < 1 && phase > hits) { hits = phase; if (!skipped()) sfx.knock(.4 + k); }
    if (u >= 1 && landedAt === null) { landedAt = now; if (!skipped()) { sfx.land(); sfx.holo(); } done(); }
    drawDie(cv, { M, lift, dx: k * cv.width * .18, glow: landedAt === null ? 0 : Math.min(1, (now - landedAt) / 300), pop: Math.min(1, (now - Math.max(landedAt ?? now, labelAt)) / 250), label, time: now, target: landedAt === null ? null : target }, skin, pattern);
    requestAnimationFrame(frame);
  };
  requestAnimationFrame(frame);
  return { landed, setLabel(text) { label = String(text); labelAt = performance.now(); }, stop() { stopped = true; } };
}

//=========PLAYER: CAPTURE CALCULATION SHOW=========
// Before the ball flies: 1 the capture rate is built line by line (a bell climbs for each bonus, a thunk for each
// penalty), 2 the d100 bounces and lands, 3 the ball changes the roll (the hologram shows the new number),
// 4 roll vs rate. A tap speeds it up; when it is over the player taps again to go on. The server already decided.

const RC_LABEL = {
  base: m => t('rcBase'), level: m => t('rcLevel', m.a), hp: m => t('rcHp', m.a), evo: m => t('rcEvo', m.a), shiny: m => t('rcShiny'),
  fx: m => t('fx_' + m.a), injury: m => t('rcInjury', m.a), bonus: m => t('rcBonus'), ball: m => m.a };
const signed = v => (v > 0 ? '+' : '') + v;

async function playRoll(res) {
  let skip = false, done = false, release = null, ups = 0;
  const wait = ms => skip ? Promise.resolve() : sleep(ms);
  rollBox.hidden = false;
  rollBox.onclick = () => { if (!done) skip = true; else if (release) release(); };
  rollBox.innerHTML = '<div class="rb-head">' + t('rcTitle') + '</div><div class="rb-num" id="rbRate">0</div><div class="rb-lines" id="rbLines"></div>' +
    '<div class="rb-dice" id="rbDice" hidden><canvas id="rbCanvas" width="440" height="440"></canvas></div><div class="rb-end" id="rbEnd"></div><small class="rb-skip" id="rbHint">' + t('rcSkip') + '</small>';
  const line = m => {
    const row = document.createElement('div');
    row.className = 'rb-line ' + (m.v >= 0 ? 'pos' : 'neg');
    row.innerHTML = '<span>' + esc(RC_LABEL[m.k] ? RC_LABEL[m.k](m) : m.k) + '</span><b>' + signed(m.v) + '</b>';
    rbLines.appendChild(row);
    if (m.v >= 0) { if (!skip) sfx.up(ups); ups++; } else if (!skip) sfx.down();
  };

  // 1 - the rate
  let rate = 0;
  for (const m of res.pre) {
    rate += m.v;
    line(m);
    rbRate.textContent = rate;
    rbRate.classList.remove('hit'); void rbRate.offsetWidth; rbRate.classList.add('hit');
    await wait(600);
  }
  await wait(450);

  // 2 - the d100
  rbDice.hidden = false;
  const die = startDie(rbCanvas, res.roll, () => skip);
  await die.landed;
  if (res.roll === 100) rbEnd.innerHTML = '<span class="nat">' + t('rcNat100') + '</span>';
  await wait(1300);

  // 3 - the ball changes the roll
  let total = res.roll;
  for (const m of res.post) {
    line(m);
    total += m.v;
    die.setLabel(total);
    if (!skip) sfx.glitch();
    await wait(900);
  }

  // 4 - roll against the rate (no verdict: the ball shows it)
  rbEnd.insertAdjacentHTML('beforeend', '<div class="rb-vs"><b>' + (res.post.length ? t('rcTotal') : t('rcRoll')) + ' ' + total + '</b> &nbsp;' + esc(t('rcNeed', res.rate)) + '</div>');
  done = true;
  rbHint.textContent = t('rcContinue'); rbHint.classList.add('go');
  await new Promise(r => { release = r; });
  die.stop();
  rollBox.hidden = true;
  rollBox.onclick = null;
}

// little "tick" for every wobble (follows the sound setting and volume)
function tick() {
  if (!sfxEnabled()) return;
  try {
    tick.ctx = tick.ctx || new AudioContext();
    const o = tick.ctx.createOscillator(), g = tick.ctx.createGain();
    o.type = 'square'; o.frequency.value = 520;
    g.gain.value = Math.max(0.005, sfxVolume() / 100 * 0.08);
    o.connect(g); g.connect(tick.ctx.destination);
    o.start(); o.stop(tick.ctx.currentTime + 0.07);
  } catch (e) {}
}

// expanding light ring at (x, y) of the field
function ring(x, y, color, size = 160, ms = 450) {
  const d = document.createElement('i');
  d.className = 'ring';
  d.style.cssText = 'left:' + x + 'px;top:' + y + 'px;border-color:' + color + ';box-shadow:0 0 18px ' + color + ',inset 0 0 18px ' + color;
  stars.appendChild(d);
  d.animate([{ transform: 'translate(-50%,-50%) scale(.1)', opacity: 1 }, { transform: 'translate(-50%,-50%) scale(' + size / 40 + ')', opacity: 0 }], { duration: ms, easing: 'ease-out' }).onfinish = () => d.remove();
}

// one trail / burst particle
function spark(x, y, color, dx, dy, ms, size = 8, star = false) {
  const s = document.createElement('i');
  s.className = star ? 'star' : 'dot';
  s.style.cssText = 'left:' + x + 'px;top:' + y + 'px;width:' + size + 'px;height:' + size + 'px;background:' + color + ';box-shadow:0 0 6px ' + color;
  stars.appendChild(s);
  s.animate([{ transform: 'translate(-50%,-50%) scale(1)', opacity: 1 }, { transform: 'translate(calc(-50% + ' + dx + 'px),calc(-50% + ' + dy + 'px)) scale(.1)', opacity: 0 }], { duration: ms, easing: 'ease-out' }).onfinish = () => s.remove();
}

function burst(x, y, fx) {
  ring(x, y, fx.colors[0], 190, 550);
  for (let i = 0; i < fx.burst; i++) {
    const a = (i / fx.burst) * Math.PI * 2, d = 55 + Math.random() * 70;
    spark(x, y, fx.colors[i % fx.colors.length], Math.cos(a) * d, Math.sin(a) * d - 15, 700 + Math.random() * 500, 10 + Math.random() * 8, true);
  }
}

const SIL = 'brightness(0) invert(1) drop-shadow(0 0 14px #ff5252)';  // red-white "energy" look of a Pokemon in a ball

// 1 arc to the Pokemon (trail), 2 it turns to light and is sucked in, 3 ball drops, bounces and
// wobbles, 4a burst (caught) or 4b the ball pops and it comes back out
async function playThrow(res) {
  const fx = BALL_FX[res.skin] || BALL_FX.poke;
  const fr = field.getBoundingClientRect(), wr = wildImg.getBoundingClientRect();
  const size = 80;
  const sx = fr.width / 2, sy = fr.height - 30;
  const tx = wr.left + wr.width / 2 - fr.left, ty = wr.top + wr.height * 0.55 - fr.top;
  const groundY = wr.bottom - fr.top - 8;
  const pos = (x, y, rot = 0, sc = 1, sy2 = sc) => 'translate(' + x + 'px,' + y + 'px) translate(-50%,-50%) rotate(' + rot + 'deg) scale(' + sc + ',' + sy2 + ')';

  flyBall.src = '/assets/items/' + res.skin + '.png';
  flyBall.style.cssText = 'width:' + size + 'px;height:' + size + 'px;left:0;top:0;--glow:' + fx.colors[0];
  flyBall.hidden = false;
  ballShadow.style.cssText = 'left:' + tx + 'px;top:' + (groundY + 6) + 'px';

  // 1 - the arc, with a trail of particles in the ball's colours
  const frames = [];
  for (let i = 0; i <= 12; i++) {
    const u = i / 12;
    frames.push({ transform: pos(sx + (tx - sx) * u, sy + (ty - sy) * u - 4 * 150 * u * (1 - u), fx.spin * u, 1.15 - 0.4 * u) });
  }
  const flight = flyBall.animate(frames, { duration: 900, easing: 'linear', fill: 'forwards' });
  const trail = setInterval(() => {
    const b = flyBall.getBoundingClientRect();
    const x = b.left + b.width / 2 - fr.left, y = b.top + b.height / 2 - fr.top;
    for (let k = 0; k < fx.trail; k++) {
      const c = fx.colors[Math.floor(Math.random() * fx.colors.length)];
      spark(x + (Math.random() - .5) * 14, y + (Math.random() - .5) * 14, c, (Math.random() - .5) * 40, 10 + Math.random() * 30, 450 + Math.random() * 300, 5 + Math.random() * 6, res.skin !== 'poke' && Math.random() < .5);
    }
  }, 28);
  await flight.finished;
  clearInterval(trail);

  // 2 - flash, the Pokemon becomes light and flows into the ball
  burst(tx, ty, { ...fx, burst: 8 });
  flyBall.animate([{ transform: pos(tx, ty, fx.spin, .75), filter: 'brightness(1)' }, { transform: pos(tx, ty, fx.spin, .95), filter: 'brightness(2.5)', offset: .3 }, { transform: pos(tx, ty, fx.spin, .75), filter: 'brightness(1)' }], { duration: 520, fill: 'forwards' });
  await wildImg.animate([
    { filter: 'none', transform: 'scale(1)', opacity: 1 },
    { filter: SIL, transform: 'scale(1.05)', opacity: 1, offset: .25 },
    { filter: SIL, transform: 'scale(.35,1.2)', opacity: 1, offset: .75 },
    { filter: SIL, transform: 'scale(0,.2)', opacity: 0 }
  ], { duration: 520, fill: 'forwards', easing: 'ease-in' }).finished;

  // 3 - falls, squashes, bounces twice, then rests on the grass
  ballShadow.animate([{ opacity: 0, transform: 'translate(-50%,-50%) scale(.3)' }, { opacity: 1, transform: 'translate(-50%,-50%) scale(1)' }], { duration: 400, fill: 'forwards' });
  await flyBall.animate([
    { transform: pos(tx, ty, fx.spin, .75), easing: 'cubic-bezier(.5,0,1,.6)' },
    { transform: pos(tx, groundY, 0, 1, 1), offset: .4 },
    { transform: pos(tx, groundY + 4, 0, 1.18, .82), offset: .46 },
    { transform: pos(tx, groundY - 40, 0, 1, 1), offset: .66, easing: 'cubic-bezier(.5,0,1,.6)' },
    { transform: pos(tx, groundY, 0, 1.08, .92), offset: .82 },
    { transform: pos(tx, groundY - 12, 0, 1, 1), offset: .91 },
    { transform: pos(tx, groundY, 0) }
  ], { duration: 950, fill: 'forwards' }).finished;

  // wobbles: the ball tilts, pauses, ticks
  const rest = pos(tx, groundY, 0);
  for (let i = 0; i < res.shakes; i++) {
    await sleep(520);
    await flyBall.animate([
      { transform: rest }, { transform: pos(tx, groundY, -28), offset: .3 }, { transform: pos(tx, groundY, 24), offset: .65 }, { transform: pos(tx, groundY, -8), offset: .85 }, { transform: rest }
    ], { duration: 700, easing: 'ease-in-out', fill: 'forwards' }).finished;
    tick();
    ring(tx, groundY, fx.colors[0], 70, 350);
  }
  await sleep(600);

  if (res.caught) {
    burst(tx, groundY - 10, fx);
    await flyBall.animate([{ transform: rest, filter: 'brightness(1)' }, { transform: pos(tx, groundY, 0, 1.3), filter: 'brightness(2.6)', offset: .35 }, { transform: rest, filter: 'brightness(.75) saturate(.7)' }], { duration: 700, fill: 'forwards' }).finished;
    await sleep(900);
  } else {
    burst(tx, groundY - 10, { ...fx, burst: 10, colors: ['#ffffff', '#ff8a80'] });
    flyBall.animate([{ transform: rest, opacity: 1 }, { transform: pos(tx, groundY, 0, 1.6), opacity: 0 }], { duration: 300, fill: 'forwards' });
    ballShadow.animate([{ opacity: 1 }, { opacity: 0 }], { duration: 300, fill: 'forwards' });
    await wildImg.animate([
      { filter: SIL, transform: 'scale(.05)', opacity: 1 },
      { filter: SIL, transform: 'scale(1.2)', opacity: 1, offset: .55 },
      { filter: 'none', transform: 'scale(1)', opacity: 1 }
    ], { duration: 650, fill: 'forwards', easing: 'ease-out' }).finished;
  }
  flyBall.hidden = true;
  ballShadow.animate([{ opacity: 0 }], { duration: 1, fill: 'forwards' });
  stars.innerHTML = '';
  wildImg.getAnimations().forEach(x => x.cancel());
  flyBall.getAnimations().forEach(x => x.cancel());
  ballShadow.getAnimations().forEach(x => x.cancel());
}

//=========PLAYER: BAG=============

function renderBag() {
  if (!document.getElementById('bagList')) return;
  bagList.innerHTML = pcData.length
    ? pcData.map(tr => {
        const cells = tr.items.map(i => '<div class="slot" title="' + esc(i.name) + '">' + ballImg(i.skin || i.name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, ''), 52) + '<span class="nm">' + esc(i.name) + '</span><b class="qty">x' + i.qty + '</b></div>');
        while (cells.length < 12 || cells.length % 6) cells.push('<div class="slot empty"></div>');
        return '<div class="bagpanel"><h3>' + esc(tr.name) + '</h3><div class="bag-grid">' + cells.join('') + '</div></div>';
      }).join('')
    : t('noChars');
}

let pollTimer = null;
let lastSeq = null;

function startPolling(code) {
  clearInterval(pollTimer);
  lastSeq = null;
  introTimers.forEach(clearTimeout); wilds = []; cur = 0; renderWild();
  pollSpawns(code);
  pollTimer = setInterval(() => pollSpawns(code), 2000);
}

// Only one request at a time: on a slow/throttled phone two polls with the same "after"
// would both return the same spawns and show them twice
let polling = false;
async function pollSpawns(code) {
  if (polling) return;
  polling = true;
  try { await pollOnce(code); } finally { polling = false; }
}

async function pollOnce(code) {
  await artReady;
  const url = '/api/spawns?code=' + encodeURIComponent(code) + '&token=' + saved()[code].token +
    (lastSeq === null ? '' : '&after=' + lastSeq);
  const r = await fetch(url);
  if (r.status === 403 || r.status === 404) {  // kicked, or the Master deleted the session
    out.textContent = r.status === 403 ? t('kicked', code) : t('deleted', code);
    forget(code);
    showHome();
    return;
  }
  if (!r.ok) return;
  const j = await r.json();
  const firstFetch = lastSeq === null;
  lastSeq = j.last;
  if (!firstFetch && j.spawns.length) {
    notify(j.spawns.length > 1 ? t('manyWild', j.spawns.length) : j.spawns[0].private ? t('wildNear') : t('wildAll'), 'encounters');
  }
  loadTrainers();  // the Master may have given us a Pok\u00e9mon
  wildStates = j.states || {};
  if (!capturing) renderWildLive();

  dropGone(j.gone || []);
  if (j.spawns.length) {
    wilds.push(...j.spawns);
    cur = wilds.length - 1;  // jump to the newest (its show starts below)
  }
  if (!firstFetch && j.spawns.length) { pendingIntro = true; playPendingIntro(); }
  // page just opened (or reopened after a server restart): what is already on the field shows right away, without the entrance show
  if (firstFetch && j.spawns.length) { renderWild(); field.dataset.phase = 'reveal'; }
}

//=========PLAYER: CHARACTERS=============

let lastTeamTotal = null;
const auth = () => { const c = localStorage.getItem(CURRENT); return { code: c, token: saved()[c].token }; };

async function loadTrainers(method = 'GET', extra = {}, path = '/api/trainers') {
  const q = new URLSearchParams({ ...auth(), ...extra });
  const r = method === 'GET' ? await fetch(path + '?' + q) : await fetch(path, { method, body: q });
  if (!r.ok) { out.textContent = errText((await r.json()).error); return; }
  await artReady;
  const list = await r.json();
  const total = list.reduce((n, tr) => n + tr.team.length, 0);
  if (lastTeamTotal !== null && total > lastTeamTotal) notify(t('gotPoke'), 'characters');
  lastTeamTotal = total;
  fillSheetSelect();
  pcSetData(list);
  trainerList.innerHTML = list.length
    ? list.map(tr => '<div class="wild">#' + tr.id + ' <b>' + esc(tr.name) + '</b> <button onclick="deleteTrainer(' + tr.id + ')">' + t('del') + '</button>' +
        (tr.team.length
          ? '<div class="team">' + tr.team.map(p => sprite(keyOf(p), 'small', p.shiny)).join('') + '</div><small>' +
            tr.team.map(p => esc(displayName(p)) + ' ' + t('lv') + p.level).join(' &middot; ') + '</small>'
          : '<br><small>' + t('noPoke') + '</small>') + '</div>').join('')
    : t('noChars');
}

//=========PLAYER: PC (Cobblemon-style: detail left, boxes center, party right)=========
// Slot rules live in the server (PokemonServices.movePokemon); here: select, pick up, place.

const IS_MASTER = !!document.getElementById('tab-players');  // the Master page shows every player's PC (read-only + Edit)
let pcData = [], pcSig = '', pcTrainerId = null, pcSel = null, pcHold = false, pcBox = 1, pcList = false;
const PC_PARTY = 6;  // boxes: tr.boxes = slot limit of each box (every character starts with one; the player adds more)

function pcSetData(list) {
  pcData = list;
  const sig = JSON.stringify(list);
  if (sig === pcSig) return;  // polled every 2 s: only redraw on a real change
  pcSig = sig;
  renderBag();
  if (!list.some(tr => tr.id === pcTrainerId)) { pcTrainerId = list.length ? list[0].id : null; pcSel = null; pcHold = false; }
  renderPC();
}

function pcBoxes() {
  const tr = pcData.find(x => x.id === pcTrainerId);
  return tr ? tr.boxes : [30];
}

function pcAddBox() {
  loadTrainers('POST', { trainerId: pcTrainerId }, '/api/pc/box');
}

function pcTeam() {
  const tr = pcData.find(x => x.id === pcTrainerId);
  return tr ? tr.team : [];
}

function pcPickTrainer(id) { pcTrainerId = +id; pcSel = null; pcHold = false; renderPC(); }
function pcStep(d) { const n = pcBoxes().length; pcBox = (pcBox - 1 + d + n) % n + 1; renderPC(); }
function pcToggleList() { pcList = !pcList; renderPC(); }
function pcOpenBox(n) { pcBox = n; pcList = false; renderPC(); }

const displayName = p => p.nickname || p.species;
const dragging = p => pcDrag && pcDrag.active && pcDrag.id === p.id;

function pcSlotHtml(p, zone, box, slot, dim) {
  const cls = 'pc-slot' + (p ? '' : ' empty') + (p && p.id === pcSel ? ' sel' : '') + (p && p.id === pcSel && pcHold ? ' hold' : '') + (dim ? ' dim' : '') + (p && dragging(p) ? ' lifted' : '');
  const data = ' data-zone="' + zone + '" data-box="' + box + '" data-slot="' + slot + '"';
  const sym = p ? (p.shiny ? '<i class="sym s" title="' + t('pcShiny') + '">&#9733;</i>' : '') + (p.gender === 'Male' ? '<i class="sym m">&#9794;</i>' : p.gender === 'Female' ? '<i class="sym f">&#9792;</i>' : '') : '';
  return '<button class="' + cls + '"' + data + ' onclick="pcClick(\'' + zone + '\',' + box + ',' + slot + ')"' + (p ? ' title="' + esc(displayName(p) + (p.nickname ? ' (' + p.species + ')' : '')) + '"' : '') + '>' +
    (p ? '<span class="pname' + (p.shiny ? ' shinyname' : '') + '">' + esc(displayName(p)) + '</span><span class="syms">' + sym + '</span>' +
         sprite(keyOf(p), 'small', p.shiny) + '<span class="lv">' + t('lv') + p.level + '</span>' : '') + '</button>';
}

function pcClick(zone, box, slot) {
  if (pcSuppressClick) return;  // the click that follows a drag-and-drop
  const team = pcTeam();
  const p = team.find(x => zone === 'party' ? x.party === slot : (x.box === box && x.slot === slot));
  if (zone === 'trash') {
    const sel = pcHold && team.find(x => x.id === pcSel);
    pcHold = false;
    if (sel) pcRelease(sel); else renderPC();
    return;
  }
  if (pcHold && pcSel !== null) {
    pcHold = false;
    if (p && p.id === pcSel) { renderPC(); return; }
    loadTrainers('POST', { trainerId: pcTrainerId, pokemonId: pcSel, zone, box, slot }, '/api/pc/move');
    renderPC();
  } else if (p) {
    pcSel = p.id;  // select; tapping the selected one again picks it up
    renderPC();
  }
}


// click the name -> it becomes a text box; Enter / leaving saves, empty goes back to the species name, Esc cancels
function pcEditName() {
  const sel = pcTeam().find(p => p.id === pcSel), h = document.getElementById('pcName');
  if (!sel || !h || h.querySelector('input')) return;
  h.onclick = null;
  h.innerHTML = '<input id="nickInput" maxlength="20" value="' + esc(sel.nickname) + '" placeholder="' + esc(sel.species) + '">';
  nickInput.focus(); nickInput.select();
  let done = false;
  const finish = save => {
    if (done) return; done = true;
    if (save && nickInput.value.trim() !== (sel.nickname || '')) loadTrainers('POST', { trainerId: pcTrainerId, pokemonId: sel.id, nickname: nickInput.value }, '/api/pc/nickname');
    else renderPC();
  };
  nickInput.onkeydown = e => { if (e.key === 'Enter') finish(true); else if (e.key === 'Escape') finish(false); };
  nickInput.onblur = () => finish(true);
}

// let a Pokemon go (asks first)
async function pcRelease(p) {
  if (!await askConfirm(t('pcTrashAsk', displayName(p)))) { renderPC(); return; }
  pcSel = null; pcHold = false;
  await loadTrainers('POST', { trainerId: pcTrainerId, pokemonId: p.id }, '/api/pc/release');
  renderPC();
}

// Same rules as the server (PokemonServices.movePokemon), applied at once so the Pokemon does not jump back
function pcLocalMove(p, zone, box, slot) {
  const here = x => zone === 'party' ? x.party === slot : (x.box === box && x.slot === slot);
  const other = pcTeam().find(x => x !== p && here(x));
  if (other) { other.party = p.party; other.box = p.box; other.slot = p.slot; }
  p.party = zone === 'party' ? slot : 0;
  p.box = zone === 'party' ? 0 : box;
  p.slot = zone === 'party' ? 0 : slot;
}

//=========PC: DRAG AND DROP (a little hand carries the Pokemon)=========
// Mouse and touch: press a Pokemon and move. Slots with a Pokemon have touch-action: none, so a finger
// that starts on one drags it instead of scrolling the page (it still scrolls from anywhere else).

let pcDrag = null, pcSuppressClick = false;
// a glove in the Pokedex colors (see .hand in style.css): palm, four fingers, thumb, cuff with a stripe
const HAND = '<svg class="hand" viewBox="0 0 70 78" width="70" height="78"><g class="fingers">' +
  [14, 26, 38, 50].map((x, i) => '<rect class="fing f' + i + '" x="' + x + '" y="28" width="11" height="' + [30, 34, 32, 27][i] + '" rx="5.5"/>').join('') +
  '<rect class="fing thumb" x="3" y="26" width="11" height="25" rx="5.5"/></g>' +
  '<rect class="palm" x="10" y="8" width="52" height="36" rx="13"/><path class="shine" d="M18 14h30" />' +
  '<g class="cuffg"><rect class="cuff" x="13" y="0" width="46" height="13" rx="5"/><rect class="stripe" x="13" y="5" width="46" height="3"/></g></svg>';

document.addEventListener('pointerdown', e => {
  const slot = e.target.closest && e.target.closest('.pc-slot:not(.empty)');
  if (IS_MASTER || !slot || (e.pointerType === 'mouse' && e.button !== 0)) return;
  const p = pcTeam().find(x => slot.dataset.zone === 'party' ? x.party === +slot.dataset.slot : (x.box === +slot.dataset.box && x.slot === +slot.dataset.slot));
  if (!p) return;
  pcDrag = { id: p.id, p, slot, x: e.clientX, y: e.clientY, lastX: e.clientX, active: false, touch: e.pointerType !== 'mouse', ghost: null, over: null, hover: null };
});

document.addEventListener('pointermove', e => {
  if (!pcDrag) return;
  if (!pcDrag.active) {
    const moved = Math.hypot(e.clientX - pcDrag.x, e.clientY - pcDrag.y);
    if (moved < 5) return;
    pcDragStart(e.clientX, e.clientY);
  }
  pcDragMove(e.clientX, e.clientY);
});

document.addEventListener('contextmenu', e => { if (e.target.closest && e.target.closest('.pc-slot')) e.preventDefault(); });

function pcDragStart(x, y) {
  if (!pcDrag || pcDrag.active) return;
  pcDrag.active = true;
  pcSel = pcDrag.id; pcHold = false;
  const g = document.createElement('div');
  g.id = 'pcGhost';
  g.innerHTML = '<div class="gwrap"><div class="ghand">' + HAND + '</div>' + sprite(keyOf(pcDrag.p), 'ghostsprite', pcDrag.p.shiny) + '</div>';
  document.body.appendChild(g);
  pcDrag.ghost = g;
  pcDragMove(x, y);
  requestAnimationFrame(() => g.classList.add('grab'));
  document.querySelector('.pc').classList.add('dragging');  // arrows blink, hint appears
  pcHint.hidden = false;
  renderPC();  // the source slot is shown as "lifted"
}

function pcDragMove(x, y) {
  const d = pcDrag, g = d.ghost;
  g.style.transform = 'translate(' + x + 'px,' + y + 'px)';
  g.style.setProperty('--tilt', Math.max(-28, Math.min(28, (x - d.lastX) * 2.2)) + 'deg');  // the hand sways with the movement
  d.lastX = x;
  const el = document.elementFromPoint(x, y);
  const slot = el && el.closest('.pc-slot, .pc-trash');
  if (slot !== d.over) {
    if (d.over) d.over.classList.remove('drop');
    d.over = slot;
    if (slot) slot.classList.add('drop');
  }
  // holding the Pokemon over the box arrows flips through the boxes
  const arrow = el && el.closest('.pc-head button');
  if (arrow !== d.hover) {
    clearTimeout(d.hoverTimer); d.hoverTimer = null;
    if (d.hover) d.hover.classList.remove('charging');
    d.hover = arrow;
  }
  if (arrow && !d.hoverTimer) {
    arrow.classList.add('charging');  // a bar fills while it waits
    d.hoverTimer = setTimeout(() => {
      d.hoverTimer = null;
      arrow.classList.remove('charging');
      if (pcDrag !== d || d.hover !== arrow) return;
      arrow.click();
      pcGrid.classList.remove('flip'); void pcGrid.offsetWidth; pcGrid.classList.add('flip');  // the box flashes: it changed
    }, 600);
  }
}

function pcDragEnd(cancel) {
  const d = pcDrag;
  pcDrag = null;
  clearTimeout(d.timer);
  if (!d.active) return;
  clearTimeout(d.hoverTimer);
  document.querySelector('.pc').classList.remove('dragging');
  pcHint.hidden = true;
  document.querySelectorAll('.pc-head .charging').forEach(b => b.classList.remove('charging'));
  pcSuppressClick = true;
  setTimeout(() => { pcSuppressClick = false; }, 0);
  const target = !cancel && d.over && document.body.contains(d.over) ? d.over : null;
  const g = d.ghost;
  g.classList.add('release');  // the hand opens and lets go
  if (target && !(target.dataset.zone === d.slot.dataset.zone && target.dataset.box === d.slot.dataset.box && target.dataset.slot === d.slot.dataset.slot)) {
    const r = target.getBoundingClientRect();
    if (target.dataset.zone === 'trash') pcRelease(d.p);
    g.style.transition = 'transform .16s ease-out';
    g.style.transform = 'translate(' + (r.left + r.width / 2) + 'px,' + (r.top + r.height / 2) + 'px)';
    const zone = target.dataset.zone, box = +target.dataset.box, slot = +target.dataset.slot;
    if (zone !== 'trash') {
      pcLocalMove(d.p, zone, box, slot);
      loadTrainers('POST', { trainerId: pcTrainerId, pokemonId: d.id, zone, box, slot }, '/api/pc/move');
    }
  } else {
    const r = document.querySelector('.pc-slot.lifted');  // not dropped on a slot: it goes back where it was
    if (r) { const b = r.getBoundingClientRect(); g.style.transition = 'transform .2s ease-out'; g.style.transform = 'translate(' + (b.left + b.width / 2) + 'px,' + (b.top + b.height / 2) + 'px)'; }
  }
  setTimeout(() => g.remove(), 260);
  document.querySelectorAll('.drop').forEach(s => s.classList.remove('drop'));
  renderPC();
}

document.addEventListener('pointerup', () => { if (pcDrag) pcDragEnd(false); });
document.addEventListener('pointercancel', () => { if (pcDrag) pcDragEnd(true); });

//=========PC: STATUS SCREEN (HP, XP, free stat points)=========
// Opens over the page. Points are only spent for real when "Apply" is pressed; the server re-checks the Base Relation rule.

let stId = null, stPend = [0, 0, 0, 0, 0, 0], stPage = 0, stSlide = '', stMsg = '';   // stPage: 0 = status, 1 = evolution
const STAT_NAMES = ['HP', 'Attack', 'Defense', 'Special Attack', 'Special Defense', 'Speed'];

function openStatus() {
  if (pcSel === null) return;
  stId = pcSel; stPend = [0, 0, 0, 0, 0, 0]; stPage = 0; stMsg = '';
  drawStatus();
}

function closeStatus() {
  stId = null;
  const o = document.getElementById('stDlg');
  if (o) o.remove();
  document.removeEventListener('keydown', stKey, true);
}

function stKey(e) { if (e.key === 'Escape' && !document.getElementById('dlg')) { e.preventDefault(); closeStatus(); } }

// same Base Relation check as the server: a stat can't pass one that has a higher (base + nature)
function stCanAdd(adj, pts, i, free) {
  if (free && free[i]) return true;   // the Master let this stat ignore the rule
  return adj.every((a, j) => !(a > adj[i] && adj[i] + pts[i] + 1 > a + pts[j]));
}

// XP bar: always the whole level; the effects grow with the progress (see style.css)
function xpBar(p) {
  const pct = p.xpNext < 0 ? 100 : Math.max(0, Math.min(100, (p.xp - p.xpFloor) / (p.xpNext - p.xpFloor) * 100));
  const tier = pct >= 90 ? 4 : pct >= 70 ? 3 : pct >= 40 ? 2 : pct >= 10 ? 1 : 0;
  return '<div class="bar xp t' + tier + (p.shiny ? ' shiny' : '') + (p.xpNext < 0 ? ' max' : '') + '"><i style="width:' + pct + '%"><s></s><s></s><s></s></i></div>';
}

function stMon() { return pcTeam().find(p => p.id === stId) || null; }

function drawStatus() {
  const p = stMon();
  if (!p) { closeStatus(); return; }
  let o = document.getElementById('stDlg');
  if (!o) {
    o = document.createElement('div');
    o.id = 'stDlg';
    o.onclick = e => { if (e.target === o) closeStatus(); };
    document.body.appendChild(o);
    document.addEventListener('keydown', stKey, true);
  }
  if (!o.dataset.swipe) {   // swipe left/right on a phone = next/previous page
    o.dataset.swipe = '1';
    let x0 = 0, y0 = 0;
    o.addEventListener('touchstart', e => { x0 = e.touches[0].clientX; y0 = e.touches[0].clientY; }, { passive: true });
    o.addEventListener('touchend', e => {
      const dx = e.changedTouches[0].clientX - x0, dy = e.changedTouches[0].clientY - y0;
      if (Math.abs(dx) > 60 && Math.abs(dy) < 45) stGo(dx < 0 ? 1 : 0);
    }, { passive: true });
  }
  const keepScroll = o.firstElementChild ? o.firstElementChild.scrollTop : 0;
  const oldBars = [...o.querySelectorAll('.bar i')].map(i => i.style.width);
  o.classList.toggle('again', !!o.firstElementChild);   // redrawing: no pop-in again, no blink

  const adj = p.base.map((b, i) => Math.max(1, b + p.nat[i]));
  const pts = p.pts.map((v, i) => v + stPend[i]);
  const spent = stPend.reduce((a, b) => a + b, 0), free = p.points - spent;
  const maxHp = p.level + (adj[0] + pts[0]) * 3 + 10;   // preview with the points not applied yet
  const hp = Math.round(p.hp), pct = Math.max(0, Math.min(100, hp / p.maxHp * 100));

  const rows = STAT_NAMES.map((n, i) => {
    const nat = p.nat[i] ? '<span class="' + (p.nat[i] > 0 ? 'up' : 'down') + '">' + (p.nat[i] > 0 ? '+' : '') + p.nat[i] + '</span>' : '&ndash;';
    const canPlus = free > 0 && stCanAdd(adj, pts, i, p.free);
    return '<tr><th>' + t('stat' + i) + '</th><td>' + p.base[i] + '</td><td>' + nat + '</td><td>' + p.pts[i] +
      (stPend[i] ? ' <b class="pend">+' + stPend[i] + '</b>' : '') + '</td><td class="tot">' + (adj[i] + pts[i]) + (p.free[i] ? ' <small class="st-tag" title="' + t('stFreeTitle') + '">' + t('stFreeTag') + '</small>' : '') + '</td>' +
      '<td class="ctl"><button ' + (stPend[i] ? '' : 'disabled') + ' onclick="stStep(' + i + ',-1)" aria-label="-">&minus;</button>' +
      '<button ' + (canPlus ? '' : 'disabled') + ' onclick="stStep(' + i + ',1)" aria-label="+">+</button></td></tr>';
  }).join('');

  o.innerHTML = '<div class="st"><div class="dlg-top"><span class="leds"><i></i><i></i><i></i></span><button class="st-x" onclick="closeStatus()" aria-label="' + t('stClose') + '">&times;</button></div>' +
    '<div class="st-screen">' +
      '<div class="st-head">' + sprite(keyOf(p), 'big', p.shiny) + '<div><h3' + (p.shiny ? ' class="shinyname"' : '') + '>' + esc(displayName(p)) +
        (p.shiny ? ' <i class="sym s">&#9733;</i>' : '') + (p.gender === 'Male' ? ' <i class="sym m">&#9794;</i>' : p.gender === 'Female' ? ' <i class="sym f">&#9792;</i>' : '') + '</h3>' +
        '<small>' + t('lv') + ' ' + p.level + (p.nickname || p.form !== 'Normal' ? ' &middot; ' + esc(p.species) + (p.form !== 'Normal' ? ' (' + esc(p.form) + ')' : '') : '') + '</small>' +
        '<div class="pc-types">' + p.types.map(x => '<span>' + esc(x) + '</span>').join('') + '</div>' +
        '<small>' + esc(p.nature) + ' &middot; ' + esc(p.ability) + '</small></div></div>' +

      stNav(p) +
      '<div class="st-pg' + stSlide + '">' + (stPage === 1 ? evoPage(p) : (
      '<div class="st-block"><div class="st-line"><b>HP</b><span>' + hp + ' / ' + p.maxHp + (maxHp !== p.maxHp ? ' <em>(' + t('stMaxAfter', maxHp) + ')</em>' : '') + '</span></div>' +
        '<div class="bar hp ' + (pct > 50 ? 'ok' : pct > 25 ? 'mid' : 'low') + '"><i style="width:' + pct + '%"></i></div>' +
        '<div class="st-hpbtns">' + [-5, -1, 1, 5].map(d => '<button onclick="stHp(' + d + ')">' + (d > 0 ? '+' : '&minus;') + Math.abs(d) + '</button>').join('') +
        '<button onclick="stHpTo(' + p.maxHp + ')">' + t('stFull') + '</button><button onclick="stHpTo(0)">0</button></div></div>' +

      '<div class="st-block"><div class="st-line"><b>XP</b><span>' + (p.xpNext < 0 ? p.xp + ' &middot; ' + t('stXpMax') : (p.xp - p.xpFloor) + ' / ' + (p.xpNext - p.xpFloor)) + '</span></div>' +
        xpBar(p) +
        '<small>' + t('stXpTotal', p.xp) + (p.xpNext < 0 ? '' : ' &middot; ' + t('stXpLeft', p.xpNext - p.xp, p.level + 1)) + '</small>' + '</div>' +

      '<div class="st-block"><div class="st-line"><b>' + t('stPoints') + '</b><span class="st-free' + (free ? ' has' : '') + '">' + free + '</span></div>' +
        '<table class="st-table"><thead><tr><th></th><td>' + t('stBase') + '</td><td>' + t('stNat') + '</td><td>' + t('stSpent') + '</td><td>' + t('stTotal') + '</td><td></td></tr></thead><tbody>' + rows + '</tbody></table>' +
        (spent ? '<div class="st-apply"><button onclick="stClear()">' + t('stClear') + '</button><button class="go" onclick="stApply()">' + t('stApply') + ' (' + spent + ')</button></div>' : '') + '</div>' +
      (p.caps && p.caps.length ? '<div class="st-block"><div class="st-line"><b>' + t('caps') + '</b></div><div class="caps">' + p.caps.map(c => '<span class="chip ccap">' + esc(c[0]) + (c[1] ? ' ' + esc(c[1]) : '') + '</span>').join('') + '</div></div>' : '') +
      '<div class="st-block st-ask"><button onclick="stAsk()"' + (stAsked.has(p.id) ? ' disabled' : '') + '>' + (stAsked.has(p.id) ? t('reqSent') : t('reqBtn')) + '</button><small>' + t('reqHint') + '</small></div>')) + '</div>' +
    '</div></div>';
  o.firstElementChild.scrollTop = keepScroll;
  stSlide = '';
  // the bars slide from where they were instead of jumping
  const bars = [...o.querySelectorAll('.bar i')];
  if (oldBars.length === bars.length || !oldBars.length) {   // first draw: the bars grow from empty
    const target = bars.map(i => i.style.width);
    bars.forEach((i, k) => { i.style.width = oldBars[k] || '0%'; });
    requestAnimationFrame(() => requestAnimationFrame(() => bars.forEach((i, k) => { i.style.width = target[k]; })));
  }
}

//=========STATUS PAGE 2: EVOLUTION=========
// p.evo = { from: [[species, book requirement]], to: [[species, book requirement]] } (sent by the server).
// A player evolves alone only when the book asks nothing but a level ("Minimum 30"); the rest is the Master's call.

function stGo(page) {
  if (page === stPage || stId === null) return;
  stSlide = page > stPage ? ' pg-l' : ' pg-r';
  stPage = page;
  stMsg = '';
  drawStatus();
}

const evoPlain = req => /^\s*Minimum (\d+)\s*$/.test(req || '');
const evoLevelOf = req => { const m = /(?:minimum|min\.?)\s*(\d+)/i.exec(req || ''); return m ? +m[1] : null; };
const evoReadyRows = p => ((p.evo && p.evo.to) || []).filter(r => evoPlain(r[1]) && p.level >= evoLevelOf(r[1]));

// the book's words minus the level, with the few connecting words translated (item and move names stay as printed)
function evoRest(req) {
  let rest = (req || '').replace(/(?:minimum|min\.?)\s*\d+/i, '').replace(/^[\s,;]+|[\s,;]+$/g, '').replace(/\s{2,}/g, ' ');
  if (lang() === 'pt') {
    [[/\(A\)/g, '(Alola)'], [/\(G\)/g, '(Galar)'], [/\bHolding\b/g, 'Segurando'], [/\bLearn\b/g, 'Aprender'], [/\bat Night\b/gi, 'à noite'],
     [/\bNight\b/g, 'à noite'], [/\bFemale\b/g, 'Fêmea'], [/\bMale\b/g, 'Macho'], [/\bin Rain\b/g, 'na chuva'], [/\bInteract with\b/gi, 'Interagir com'],
     [/\bsee Split Evolution\b/g, 'ver evolução dividida']].forEach(([a, b]) => { rest = rest.replace(a, b); });
  }
  return rest;
}

// the row under the head: previous page / page dots / next page; glows when something can evolve right now
function stNav(p) {
  const ready = evoReadyRows(p).length > 0;
  return '<div class="st-nav">' +
    (stPage === 1 ? '<button onclick="stGo(0)" aria-label="' + t('stPageStatus') + '">&lsaquo; ' + t('stPageStatus') + '</button>' : '<span></span>') +
    '<span class="st-dots"><i class="' + (stPage === 0 ? 'on' : '') + '"></i><i class="' + (stPage === 1 ? 'on' : '') + '"></i></span>' +
    (stPage === 0 ? '<button class="' + (ready ? 'ready' : '') + '" onclick="stGo(1)" aria-label="' + t('stPageEvo') + '">' + t('stPageEvo') + ' &rsaquo;</button>' : '<span></span>') +
    '</div>';
}

function evoPage(p) {
  const evo = p.evo || { from: [], to: [] };
  const line = (row, arrow) => {
    const lv = evoLevelOf(row[1]), rest = evoRest(row[1]);
    return '<div class="evo-from">' + arrow + ' ' + sprite(row[0] + '|Normal', 'small') + '<b>' + esc(row[0]) + '</b><small>' +
      esc([lv ? t('evoLevel', lv) : '', rest].filter(Boolean).join(' · ')) + '</small></div>';
  };
  let h = '<div class="st-block evo">';
  if (evo.from.length) h += '<div class="st-line"><b>' + t('evoFrom') + '</b></div>' + evo.from.map(r => line(r, '&lsaquo;')).join('');
  h += '<div class="st-line"><b>' + t('evoTo') + '</b></div>';
  if (!evo.to.length) h += '<p class="evo-none">' + t('evoNone') + '</p>';
  h += evo.to.map(row => {
    const [name, req] = row, lv = evoLevelOf(req), rest = evoRest(req), plain = evoPlain(req);
    const left = lv ? Math.max(0, lv - p.level) : 0;
    const asked = stAsked.has('evo' + p.id + name);
    let state, btn;
    if (plain && !left) {
      state = '<span class="evo-ok">' + t('evoReady') + '</span>';
      btn = '<button class="go" onclick="stEvolve(' + JSON.stringify(name).replace(/"/g, '&quot;') + ')">' + t('evoBtn') + '</button>';
    } else if (plain) {
      state = '<span class="evo-wait">' + t('evoLeft', left) + '</span>';
      btn = '<button disabled>' + t('evoBtn') + '</button>';
    } else {
      state = left ? '<span class="evo-wait">' + t('evoLeft', left) + '</span>' : '<span class="evo-cond">' + t('evoMasterOnly') + '</span>';
      btn = '<button ' + (asked ? 'disabled' : '') + ' onclick="stEvoAsk(' + JSON.stringify(name).replace(/"/g, '&quot;') + ')">' + (asked ? t('evoAsked') : t('evoAsk')) + '</button>';
    }
    return '<div class="evo-card' + (plain && !left ? ' ready' : '') + '">' + sprite(name + '|Normal', 'big', p.shiny) +
      '<div class="evo-info"><b>' + esc(name) + '</b>' +
      '<div class="evo-chips">' + (lv ? '<span>' + t('evoLevel', lv) + '</span>' : '') + (rest ? '<span>' + esc(rest) + '</span>' : '') + (!req ? '<span>' + t('evoNoReq') + '</span>' : '') + '</div>' +
      state + '</div>' + btn + '</div>';
  }).join('');
  h += '<small class="evo-note">' + t('evoNote') + '</small>' + (stMsg ? '<p class="evo-msg">' + esc(stMsg) + '</p>' : '') + '</div>';
  return h;
}

async function stEvoAsk(name) {
  const p = stMon();
  if (!p) return;
  await loadTrainers('POST', { trainerId: pcTrainerId, pokemonId: p.id, text: t('evoAskText', name) }, '/api/pc/request');
  stAsked.add('evo' + p.id + name);
  drawStatus();
}

async function stEvolve(to) {
  const p = stMon();
  if (!p) return;
  if (!await askConfirm(t('evoConfirm', displayName(p), to))) return;
  const old = { key: keyOf(p), shiny: p.shiny, name: p.species, points: p.points };
  const r = await fetch('/api/pc/evolve', { method: 'POST', body: new URLSearchParams({ ...auth(), trainerId: pcTrainerId, pokemonId: p.id, to }) });
  if (!r.ok) { stMsg = errText((await r.json()).error); drawStatus(); return; }
  await loadTrainers();
  const now = stMon();
  if (!now) return;
  stMsg = now.points > old.points ? t('evoBack') : '';
  stPend = [0, 0, 0, 0, 0, 0];
  await evoAnim(old, now);
  drawStatus();
}

// the old sprite blinks faster and faster as a white shape, a flash, then the new one appears with its cry
function evoAnim(old, now) {
  return new Promise(done => {
    const o = document.createElement('div');
    o.id = 'evoAnim';
    o.innerHTML = '<div class="evo-stage"><div class="evo-old">' + sprite(old.key, 'big', old.shiny) + '</div><div class="evo-new">' + sprite(keyOf(now), 'big', now.shiny) + '</div>' +
      '<p class="evo-text">' + esc(t('evoDone', old.name, now.species)) + '</p></div>';
    document.body.appendChild(o);
    let over = false;
    const close = () => { if (over) return; over = true; o.classList.add('out'); setTimeout(() => { o.remove(); done(); }, 250); };
    o.onclick = close;
    setTimeout(() => playCry(keyOf(now)), 2300);
    setTimeout(close, 6000);
  });
}

const stAsked = new Set();  // Pokemon the player already asked the Master about (this visit)

async function stAsk() {
  const p = stMon();
  if (!p) return;
  const text = await askConfirm(t('reqPrompt'), true);
  if (text === null) return;
  await loadTrainers('POST', { trainerId: pcTrainerId, pokemonId: p.id, text }, '/api/pc/request');
  stAsked.add(p.id);
  drawStatus();
}

function stStep(i, d) {
  const p = stMon();
  if (!p) return;
  const spent = stPend.reduce((a, b) => a + b, 0);
  if (d > 0 && spent >= p.points) return;
  if (d < 0 && !stPend[i]) return;
  stPend[i] += d;
  drawStatus();
}

function stClear() { stPend = [0, 0, 0, 0, 0, 0]; drawStatus(); }

async function stApply() {
  const p = stMon();
  if (!p) return;
  const add = stPend.join(',');
  stPend = [0, 0, 0, 0, 0, 0];
  await loadTrainers('POST', { trainerId: pcTrainerId, pokemonId: p.id, add }, '/api/pc/stats');
  drawStatus();
}

function stHpTo(v) {
  const p = stMon();
  if (p) loadTrainers('POST', { trainerId: pcTrainerId, pokemonId: p.id, hp: Math.max(0, Math.min(p.maxHp, v)) }, '/api/pc/hp');
}
function stHp(d) { const p = stMon(); if (p) stHpTo(Math.round(p.hp) + d); }

function pcHoldToggle() { pcHold = !pcHold; renderPC(); }

//=========PC DETAIL: 3 pages (summary / stats / XP + evolution), swipe or arrows=========
let pcPage = 0, pcSlide = '';
const PC_PAGES = 3;

function pcGo(n) {
  n = Math.max(0, Math.min(PC_PAGES - 1, n));
  if (n === pcPage) return;
  pcSlide = n > pcPage ? ' pg-l' : ' pg-r';
  pcPage = n;
  pcHold = false;   // the Move button only lives on the first page
  renderPC();
}

(function () {   // swipe on the detail panel (it is never replaced, only its content)
  const box = document.getElementById('pcDetail');
  if (!box) return;
  let x0 = 0, y0 = 0;
  box.addEventListener('touchstart', e => { x0 = e.touches[0].clientX; y0 = e.touches[0].clientY; }, { passive: true });
  box.addEventListener('touchend', e => {
    const dx = e.changedTouches[0].clientX - x0, dy = e.changedTouches[0].clientY - y0;
    if (Math.abs(dx) > 50 && Math.abs(dy) < 40) pcGo(pcPage + (dx < 0 ? 1 : -1));
  }, { passive: true });
})();

function pcStatsPage(sel) {
  const rows = STAT_NAMES.map((n, i) => {
    const nat = sel.nat[i];
    return '<tr class="' + (nat > 0 ? 'nup' : nat < 0 ? 'ndown' : '') + '"><th>' + t('stat' + i) + '</th><td>' + sel.base[i] + '</td><td>' +
      (nat ? '<b class="' + (nat > 0 ? 'up' : 'down') + '">' + (nat > 0 ? '&#9650;' : '&#9660;') + Math.abs(nat) + '</b>' : '&ndash;') + '</td><td>' +
      (sel.pts[i] ? '<b>+' + sel.pts[i] + '</b>' : '0') + '</td><td class="tot">' + (Math.max(1, sel.base[i] + nat) + sel.pts[i]) +
      (sel.free[i] ? ' <small class="st-tag" title="' + t('stFreeTitle') + '">' + t('stFreeTag') + '</small>' : '') + '</td></tr>';
  }).join('');
  return '<table class="st-table pc-stats"><thead><tr><th></th><td>' + t('stBase') + '</td><td>' + t('stNat') + '</td><td>' + t('stSpent') + '</td><td>' + t('stTotal') + '</td></tr></thead><tbody>' + rows + '</tbody></table>' +
    '<small class="pc-sub">' + t('pcNature') + ': <b>' + esc(sel.nature) + '</b> &middot; ' + t('stPoints') + ': <b>' + sel.points + '</b></small>';
}

function pcXpPage(sel) {
  const evo = (sel.evo && sel.evo.to) || [];
  return '<div class="st-line"><b>XP</b><span>' + (sel.xpNext < 0 ? t('stXpMax') : (sel.xp - sel.xpFloor) + ' / ' + (sel.xpNext - sel.xpFloor)) + '</span></div>' + xpBar(sel) +
    '<small class="pc-sub">' + t('stXpTotal', sel.xp) + (sel.xpNext < 0 ? '' : ' &middot; ' + t('stXpLeft', sel.xpNext - sel.xp, sel.level + 1)) + '</small>' +
    '<div class="st-line pc-evo-h"><b>' + t('evoTo') + '</b></div>' +
    (evo.length ? evo.map(r => {
      const lv = evoLevelOf(r[1]), rest = evoRest(r[1]), ready = evoPlain(r[1]) && sel.level >= lv;
      return '<div class="pc-evo' + (ready ? ' ready' : '') + '">' + sprite(r[0] + '|Normal', 'small') + '<div><b>' + esc(r[0]) + '</b><small>' +
        esc([lv ? t('evoLevel', lv) : '', rest].filter(Boolean).join(' · ')) + '</small>' + (ready ? '<em>' + t('evoReady') + '</em>' : '') + '</div></div>';
    }).join('') : '<small class="pc-sub">' + t('evoNone') + '</small>') +
    (IS_MASTER || !evo.length ? '' : '<button onclick="openStatus(); stGo(1)">' + t('stPageEvo') + ' &rsaquo;</button>');
}

function pcDetailHtml(sel) {
  const hpPct = Math.max(0, Math.min(100, sel.hp / sel.maxHp * 100));
  let page;
  if (pcPage === 1) page = pcStatsPage(sel);
  else if (pcPage === 2) page = pcXpPage(sel);
  else page =
    '<div class="pc-art">' + sprite(keyOf(sel), 'big', sel.shiny) + '</div>' +
    '<button class="cry" data-cry="' + esc(keyOf(sel)) + '" aria-label="Cry"><i></i><i></i><i></i><i></i><i></i></button>' +
    '<div class="pc-types">' + sel.types.map(x => '<span>' + esc(x) + '</span>').join('') + '</div>' +
    '<div class="pc-hp"><div class="st-line"><b>HP</b><span>' + Math.round(sel.hp) + ' / ' + sel.maxHp + '</span></div><div class="bar hp ' + (hpPct > 50 ? 'ok' : hpPct > 25 ? 'mid' : 'low') + '"><i style="width:' + hpPct + '%"></i></div></div>' +
    '<dl><dt>' + t('pcNature') + '</dt><dd>' + esc(sel.nature) + '</dd><dt>' + t('pcAbility') + '</dt><dd>' + esc(sel.ability) + '</dd></dl>';
  const nav = '<div class="pc-nav"><button onclick="pcGo(' + (pcPage - 1) + ')" ' + (pcPage ? '' : 'disabled') + ' aria-label="&lsaquo;">&lsaquo;</button><span class="st-dots">' +
    [0, 1, 2].map(i => '<i class="' + (i === pcPage ? 'on' : '') + '" onclick="pcGo(' + i + ')"></i>').join('') + '</span>' +
    '<button class="' + (pcPage < 2 && evoReadyRows(sel).length ? 'ready' : '') + '" onclick="pcGo(' + (pcPage + 1) + ')" ' + (pcPage < PC_PAGES - 1 ? '' : 'disabled') + ' aria-label="&rsaquo;">&rsaquo;</button></div>';
  return nav + '<div class="pc-lv">' + t('lv') + sel.level + '</div><h3 id="pcName" class="' + (IS_MASTER ? '' : 'rename') + (sel.shiny ? ' shinyname' : '') + '"' + (IS_MASTER ? '' : ' title="' + t('pcRename') + '" onclick="pcEditName()"') + '>' + esc(displayName(sel)) +
    (sel.shiny ? ' <i class="sym s" title="' + t('pcShiny') + '">&#9733;</i>' : '') + (sel.gender === 'Male' ? ' <i class="sym m">&#9794;</i>' : sel.gender === 'Female' ? ' <i class="sym f">&#9792;</i>' : '') + '</h3>' +
    (sel.nickname || sel.form !== 'Normal' ? '<small class="species">' + esc(sel.species) + (sel.form !== 'Normal' ? ' (' + esc(sel.form) + ')' : '') + '</small>' : '') +
    (pcPage ? '<div class="pc-page' + pcSlide + '">' + page + '</div>' : page.replace('<div class="pc-art">', '<div class="pc-art' + pcSlide + '">')) +
    (pcPage ? '' : (
    (pcPage ? '' : (IS_MASTER ? '' : '<button class="st-open" onclick="openStatus()">' + t('stStatus') + (sel.points ? ' <b class="st-badge">' + sel.points + '</b>' : '') + (evoReadyRows(sel).length ? ' <b class="st-badge evo" title="' + t('evoReady') + '">&uarr;</b>' : '') + '</button>') +
      (IS_MASTER ? '<button class="ed-open" onclick="openEdit()">' + t('edEdit') + '</button>'
        : '<button onclick="pcHoldToggle()">' + (pcHold ? t('pcCancel') : t('pcMove')) + '</button>') +
      (pcHold ? '<small>' + t('pcHold') + '</small>' : ''))
    ));
}

function renderPC() {
  if (!document.getElementById('pcGrid')) return;
  pcTrainer.innerHTML = pcData.map(tr => '<option value="' + tr.id + '"' + (tr.id === pcTrainerId ? ' selected' : '') + '>' + esc(tr.name) + (tr.owner ? ' (' + esc(tr.owner) + ')' : '') + '</option>').join('');
  pcTrainer.hidden = pcData.length < (IS_MASTER ? 1 : 2);
  const team = pcTeam();
  const boxes = pcBoxes();
  if (pcBox > boxes.length) pcBox = 1;
  const sel = team.find(p => p.id === pcSel) || null;
  if (!sel) { pcSel = null; pcHold = false; }

  // detail (left)
  pcDetail.classList.toggle('flat', !!sel && pcPage > 0);
  pcDetail.innerHTML = sel ? pcDetailHtml(sel) : '<p>' + (pcData.length ? t('pcPick') : t('pcNoChars')) + '</p>';
  pcSlide = '';

  // center: box grid or box list
  const q = pcFilter.value.trim().toLowerCase();
  const hit = p => !q || p.species.toLowerCase().includes(q) || p.types.some(x => x.toLowerCase().includes(q));
  if (pcList) {
    pcBoxName.textContent = t('pcBoxList');
    pcGrid.className = 'pc-boxlist';
    pcGrid.innerHTML = boxes.map((limit, i) =>
      '<button onclick="pcOpenBox(' + (i + 1) + ')">' + t('pcBox') + ' ' + (i + 1) + ' <small>' + team.filter(p => p.box === i + 1).length + '/' + limit + '</small></button>').join('') +
      (IS_MASTER ? '' : '<button class="pc-new" onclick="pcAddBox()">' + t('pcNewBox') + '</button>');
  } else {
    pcBoxName.textContent = t('pcBox') + ' ' + pcBox + ' / ' + boxes.length;
    pcGrid.className = 'pc-grid';
    let h = '';
    for (let slot = 1; slot <= boxes[pcBox - 1]; slot++) {
      const p = team.find(x => x.box === pcBox && x.slot === slot);
      h += pcSlotHtml(p, 'box', pcBox, slot, p && !hit(p));
    }
    pcGrid.innerHTML = h;
  }

  // party (right)
  let party = '';
  for (let slot = 1; slot <= PC_PARTY; slot++) party += pcSlotHtml(team.find(x => x.party === slot), 'party', 0, slot, false);
  pcParty.innerHTML = party;
  if (stId !== null) drawStatus();  // the status screen follows the data
}

async function deleteTrainer(id) {
  if (await askConfirm(t('delChar'))) loadTrainers('POST', { trainerId: id }, '/api/trainer/delete');
}

// Replaces confirm(): a little Pokedex screen that opens over the page. Resolves true (Confirm / Enter) or false (Cancel / Esc / outside).
// With askConfirm(text, true) it also has a text box and resolves the text (or null when cancelled).
function askConfirm(text, withInput = false) {
  return new Promise(done => {
    const o = document.createElement('div');
    o.id = 'dlg';
    o.innerHTML = '<div class="dlg" role="alertdialog" aria-modal="true"><div class="dlg-top"><span class="leds"><i></i><i></i><i></i></span></div>' +
      '<div class="dlg-screen"><p>' + esc(text) + '</p>' + (withInput ? '<input class="dlg-input" maxlength="120">' : '') +
      '<div class="dlg-btns"><button class="dno">' + t('cancel') + '</button><button class="dyes">' + t('confirmOk') + '</button></div></div></div>';
    const input = o.querySelector('.dlg-input');
    const close = ok => { document.removeEventListener('keydown', key, true); o.classList.add('out'); setTimeout(() => o.remove(), 140); done(withInput ? (ok ? input.value : null) : ok); };
    const key = e => { if (e.key === 'Escape') { e.preventDefault(); e.stopPropagation(); close(false); } else if (e.key === 'Enter') { e.preventDefault(); e.stopPropagation(); close(true); } };
    o.onclick = e => { if (e.target === o) close(false); };
    o.querySelector('.dno').onclick = () => close(false);
    o.querySelector('.dyes').onclick = () => close(true);
    document.addEventListener('keydown', key, true);
    document.body.appendChild(o);
    (input || o.querySelector('.dno')).focus();
  });
}

//=========MASTER=============

async function loadSessions() {
  const r = await fetch('/api/sessions');
  const j = await r.json();

  playerUrl.innerHTML = t('playersOpen') + ' <b>' + j.playerUrl + '</b>';
  const keep = spawnSession.value;
  spawnSession.innerHTML = j.sessions.map(s => '<option>' + s.code + '</option>').join('');
  const want = keep || localStorage.getItem('ptu.masterSession');
  if (want && j.sessions.some(x => x.code === want)) spawnSession.value = want;
  sessions.innerHTML = j.sessions.length
    ? j.sessions.map(s =>
        '<div class="card scard"><b class="scode">' + s.code + '</b><span class="shost">' + esc(s.host) + '</span>' +
        '<small class="muted">' + t('sessionMeta', s.day, s.players) + '</small>' +
        '<button class="danger" onclick="deleteSession(\'' + s.code + '\')">' + t('del') + '</button></div>').join('')
    : '<p class="muted">' + t('noSessions') + '</p>';
  loadPlayers();
}

async function deleteSession(code) {
  if (!await askConfirm(t('delSession', code))) return;
  await fetch('/api/session/delete', { method: 'POST', body: new URLSearchParams({ code }) });
  loadSessions();
}

// Players of the selected session, with online status; also fills the "Send to" list.
//=========MASTER: EDIT A POKEMON (PC tab) + REQUESTS FROM PLAYERS=========

const seenReq = new Set();

// Players ask for an adjustment from the Pokemon's status screen: the Master gets a notification that opens it
async function pollRequests() {
  if (!spawnSession.value) return;
  const r = await fetch('/api/requests?code=' + spawnSession.value);
  if (!r.ok) return;
  for (const q of await r.json()) {
    if (seenReq.has(q.id)) continue;
    seenReq.add(q.id);
    notify(t('reqNote', q.who, q.pokemon) + (q.text ? ': ' + q.text : ''), 'req', () => goRequest(q));
  }
}

async function goRequest(q) {
  await fetch('/api/request/done', { method: 'POST', body: new URLSearchParams({ id: q.id }) });
  showTab('pc');
  pcTrainerId = q.trainerId; pcSel = q.pokemonId; pcList = false;
  const m = pcTeam().find(x => x.id === q.pokemonId);
  if (m && m.box) pcBox = m.box;
  renderPC();
  if (m) openEdit();
}

function closeEdit() {
  const o = document.getElementById('edDlg');
  if (o) o.remove();
  document.removeEventListener('keydown', edKey, true);
}
function edKey(e) { if (e.key === 'Escape' && !document.getElementById('dlg')) { e.preventDefault(); closeEdit(); } }

// Nature / ability / gender lists come from the server (what the rules allow for this form at this level)
async function edOptions(p, level, keep) {
  const r = await fetch('/api/options?formId=' + p.formId + '&level=' + level);
  if (!r.ok) return;
  const o = await r.json();
  const fill = (sel, items, cur) => { sel.innerHTML = items.map(i => '<option value="' + i[0] + '">' + esc(i[1]) + '</option>').join(''); if (items.some(i => String(i[0]) === String(cur))) sel.value = cur; };
  fill(edNature, o.natures.map(n => [n.id, n.name + ' (+' + STAT[n.up] + ' -' + STAT[n.down] + ')']), keep ? edNature.value : p.natureId);
  const ab = o.abilities.map(a => [a.id, a.name]);
  if (!ab.some(a => a[0] === p.abilityId)) ab.unshift([p.abilityId, p.ability]);   // the one it already has stays selectable
  fill(edAbility, ab, keep ? edAbility.value : p.abilityId);
  fill(edGender, o.genders.map(g => [g, t(GENDER[g])]), keep ? edGender.value : p.gender);
}

async function openEdit() {
  const p = pcTeam().find(x => x.id === pcSel);
  if (!p) return;
  const again = !!document.getElementById('edDlg');   // refreshing the open dialog: no pop-in again
  closeEdit();
  const o = document.createElement('div');
  o.id = 'edDlg';
  o.className = 'stOverlay' + (again ? ' again' : '');
  o.dataset.id = p.id;
  o.onclick = e => { if (e.target === o) closeEdit(); };
  o.innerHTML = '<div class="st"><div class="dlg-top"><span class="leds"><i></i><i></i><i></i></span><button class="st-x" onclick="closeEdit()" aria-label="' + t('stClose') + '">&times;</button></div>' +
    '<div class="st-screen"><div class="st-head">' + sprite(keyOf(p), 'big', p.shiny) + '<div><h3' + (p.shiny ? ' class="shinyname"' : '') + '>' + esc(displayName(p)) + '</h3>' +
    '<small>' + t('lv') + ' ' + p.level + ' &middot; ' + esc(p.species) + (p.form !== 'Normal' ? ' (' + esc(p.form) + ')' : '') + '</small><br>' +
    '<small>HP ' + Math.round(p.hp) + '/' + p.maxHp + ' &middot; XP ' + p.xp + ' &middot; ' + t('stPoints') + ': ' + p.points + '</small></div></div>' +
    '<div class="ed-grid">' +
      '<label>' + t('nature') + '<select id="edNature"></select></label>' +
      '<label>' + t('ability') + '<select id="edAbility"></select></label>' +
      '<label>' + t('gender') + '<select id="edGender"></select></label>' +
      '<label>' + t('shiny') + '<select id="edShiny"><option value="0">' + t('no') + '</option><option value="1">' + t('yes') + '</option></select></label>' +
      '<label>' + t('level') + '<input id="edLevel" type="number" min="1" max="100"></label></div>' +
    '<div class="ed-free"><b>' + t('edFree') + '</b><div>' + STAT_NAMES.map((n, i) => '<label><input type="checkbox" class="edFree" ' + (p.free[i] ? 'checked' : '') + '> ' + t('stat' + i) + '</label>').join('') + '</div><small>' + t('edFreeNote') + '</small></div>' +
    '<small class="ed-note">' + t('edNote') + '</small>' +
    '<div class="st-apply"><button class="go" onclick="edSave()">' + t('edSave') + '</button></div>' +
    '<div class="st-block"><div class="st-line"><b>' + t('edPoints') + '</b></div><button onclick="edReset()">' + t('edReset') + '</button></div>' +
    (p.evo && p.evo.to.length ? '<div class="st-block"><div class="st-line"><b>' + t('edEvolve') + '</b></div><div class="ed-row"><select id="edEvoTo">' +
      p.evo.to.map(r => '<option value="' + esc(r[0]) + '">' + esc(r[0] + (r[1] ? ' (' + r[1] + ')' : '')) + '</option>').join('') +
      '</select><button onclick="edEvolve()">' + t('edEvolveBtn') + '</button></div><small class="ed-note">' + t('edEvolveNote') + '</small></div>' : '') +
    '<div class="st-block"><div class="ed-row"><input id="edXpIn" type="number" min="1" placeholder="XP"><button onclick="edXp()">' + t('giveXp') + '</button></div>' +
    '<div class="ed-row"><input id="edHpIn" type="number" min="0" placeholder="HP" value="' + Math.round(p.hp) + '"><button onclick="edHp()">' + t('setHp') + '</button></div></div>' +
    '</div></div>';
  document.body.appendChild(o);
  document.addEventListener('keydown', edKey, true);
  edShiny.value = p.shiny ? '1' : '0';
  edLevel.value = p.level;
  await edOptions(p, p.level, false);
  edLevel.onchange = () => { const l = Math.max(1, Math.min(100, +edLevel.value || p.level)); edLevel.value = l; edOptions(p, l, true); };
}

function edMon() { return pcTeam().find(x => x.id === +document.getElementById('edDlg').dataset.id); }

async function edPost(url, body) {
  const r = await fetch(url, { method: 'POST', body: new URLSearchParams(body) });
  const j = await r.json();
  if (!r.ok) { out.textContent = errText(j.error); return false; }
  out.textContent = '';
  return j;
}

async function edSave() {
  const p = edMon();
  const body = { pokemonId: p.id };
  if (+edNature.value !== p.natureId) body.natureId = edNature.value;
  if (+edAbility.value !== p.abilityId) body.abilityId = edAbility.value;
  if (edGender.value !== p.gender) body.gender = edGender.value;
  if (edShiny.value !== (p.shiny ? '1' : '0')) body.shiny = edShiny.value;
  if (+edLevel.value !== p.level) body.level = edLevel.value;
  const free = [...document.querySelectorAll('.edFree')].map(c => c.checked ? 1 : 0);
  if (free.join() !== p.free.join()) body.free = free.join(',');
  if (Object.keys(body).length > 1 && await edPost('/api/edit', body)) { out.textContent = t('edDone'); await edRefresh(); }
  else if (Object.keys(body).length === 1) closeEdit();
}

async function edReset() {
  const p = edMon();
  if (!await askConfirm(t('edResetAsk', displayName(p)))) return;
  if (await edPost('/api/edit', { pokemonId: p.id, reset: 1 })) { out.textContent = t('edDone'); await edRefresh(); }
}

async function edEvolve() {
  const p = edMon(), to = edEvoTo.value;
  if (!await askConfirm(t('edEvolveAsk', displayName(p), to))) return;
  if (await edPost('/api/evolve', { pokemonId: p.id, to })) { out.textContent = t('edDone'); await edRefresh(); }
}

async function edXp() {
  const p = edMon();
  const j = await edPost('/api/xp', { pokemonId: p.id, amount: edXpIn.value });
  if (j) { out.textContent = j.to > j.from ? t('xpLevelUp', j.from, j.to) : t('xpGave'); await edRefresh(); }
}

async function edHp() {
  const p = edMon();
  if (await edPost('/api/hp', { pokemonId: p.id, hp: edHpIn.value })) await edRefresh();
}

// reload the players (so the PC behind shows the change) and reopen the dialog with fresh numbers
async function edRefresh() {
  playerHtml = ''; pcSig = '';
  await loadPlayers();
  await openEdit();
}

let playerHtml = '', mOpen = new Set(), playerNames = {};
function mToggle(d) { const id = +d.dataset.t; if (d.open) mOpen.add(id); else mOpen.delete(id); }

async function masterXp(id) {
  const r = await fetch('/api/xp', { method: 'POST', body: new URLSearchParams({ pokemonId: id, amount: document.getElementById('mv' + id).value }) });
  const j = await r.json();
  out.textContent = r.ok ? (j.to > j.from ? t('xpLevelUp', j.from, j.to) : t('xpGave')) : errText(j.error);
  playerHtml = ''; loadPlayers();
}

async function masterHp(id) {
  const r = await fetch('/api/hp', { method: 'POST', body: new URLSearchParams({ pokemonId: id, hp: document.getElementById('mv' + id).value }) });
  if (!r.ok) out.textContent = errText((await r.json()).error);
  playerHtml = ''; loadPlayers();
}

const ago = s => s < 0 ? t('neverSeen') : s < 60 ? t('secAgo', s) : s < 3600 ? t('minAgo', Math.floor(s / 60)) : t('hourAgo', Math.floor(s / 3600));

// Quick actions of a player card: jump to the screen that does it, already pointing at him
function quickSpawn(playerId) { target.value = playerId; showTab('spawn'); target.scrollIntoView({ block: 'center' }); }
function quickGive(trainerId) { giveTo.value = trainerId; showTab('spawn'); giveTo.scrollIntoView({ block: 'center' }); }
function quickPc(trainerId) { pcPickTrainer(trainerId); pcTrainer.value = trainerId; showTab('pc'); }

async function resetDex(playerId) {
  if (!await askConfirm(t('resetDexAsk', playerNames[playerId] || ''))) return;
  await fetch('/api/dex/reset', { method: 'POST', body: new URLSearchParams({ code: spawnSession.value, playerId }) });
  playerHtml = ''; loadPlayers();
}

async function clearField() {
  if (!spawnSession.value || !await askConfirm(t('clearFieldAsk'))) return;
  const r = await fetch('/api/field/clear', { method: 'POST', body: new URLSearchParams({ code: spawnSession.value }) });
  fieldMsg.textContent = r.ok ? t('clearFieldDone', (await r.json()).removed) : '';
  activeHtml = ''; loadActive();
}

async function loadPlayers() {
  if (spawnSession.value) try { localStorage.setItem('ptu.masterSession', spawnSession.value); } catch (e) { /* private mode */ }
  if (!spawnSession.value) { playerList.textContent = ''; target.innerHTML = '<option value="all">' + t('everyone') + '</option>'; return; }
  const r = await fetch('/api/players?code=' + spawnSession.value);
  if (!r.ok) return;
  const list = (await r.json()).players;
  pcSetData(list.flatMap(p => p.trainers.map(tr => ({ ...tr, owner: p.name }))));  // the PC tab
  pollRequests();

  // the list is rebuilt every 3 s: leave it alone while the Master is typing in it or nothing changed
  playerNames = Object.fromEntries(list.map(p => [p.id, p.name]));
  const html = list.length ? list.map(p => {
    const balls = p.trainers.flatMap(tr => tr.items).reduce((n, i) => n + i.qty, 0);
    return '<div class="card pcard"><div class="phead"><span class="dot' + (p.online ? ' on' : '') + '"></span><b>' + esc(p.name) + '</b>' +
      '<small class="muted">' + (p.online ? t('online') : t('lastSeen', ago(p.ago))) + '</small></div>' +
      '<div class="pstats"><span title="' + esc(t('dexProgressTip')) + '">\uD83D\uDCD6 ' + p.dex + '</span><span>\u26AA ' + balls + '</span></div>' +
      p.trainers.map(tr => {
        const party = tr.team.filter(m => m.party > 0).sort((x, y) => x.party - y.party);
        return '<div class="pchar"><div class="pcname"><b>' + esc(tr.name) + '</b> <small class="muted">#' + tr.id + ' &middot; ' + t('inPc', tr.team.length - party.length) + '</small></div>' +
          '<div class="pparty">' + (party.map(m => { const pct = Math.max(0, Math.min(100, Math.round(100 * m.hp / m.maxHp)));
            return '<div class="pmon" title="' + esc(displayName(m)) + ' ' + t('lv') + m.level + ' &middot; HP ' + Math.round(m.hp) + '/' + m.maxHp + '">' + sprite(keyOf(m), 'small', m.shiny) +
              '<div class="bar hp ' + (pct > 50 ? 'ok' : pct > 25 ? 'mid' : 'low') + '"><i style="width:' + pct + '%"></i></div></div>'; }).join('') || '<small class="muted">' + t('noParty') + '</small>') + '</div>' +
          '<div class="qbtns"><button onclick="quickGive(' + tr.id + ')">' + t('qGive') + '</button><button onclick="quickPc(' + tr.id + ')">' + t('qPc') + '</button></div>' +
          '<details class="mchar" data-t="' + tr.id + '"' + (mOpen.has(tr.id) ? ' open' : '') + ' ontoggle="mToggle(this)"><summary>' + t('xpHp') + '</summary>' +
          tr.team.map(m => '<div class="mp">' + sprite(keyOf(m), 'small', m.shiny) + '<span class="mpn">' + esc(displayName(m)) + ' ' + t('lv') + m.level + '<br><small>HP ' + Math.round(m.hp) + '/' + m.maxHp +
            ' &middot; XP ' + m.xp + ' &middot; ' + t('stPoints') + ': ' + m.points + '</small></span>' +
            '<input id="mv' + m.id + '" type="number" min="0" placeholder="0">' +
            '<button onclick="masterXp(' + m.id + ')">' + t('giveXp') + '</button><button onclick="masterHp(' + m.id + ')">' + t('setHp') + '</button></div>').join('') +
          '</details></div>'; }).join('') +
      '<div class="qbtns"><button onclick="quickSpawn(' + p.id + ')">' + t('qSpawn') + '</button><button onclick="claimCode(' + p.id + ')">' + t('reconnectCode') + '</button>' +
      '<button class="danger" onclick="resetDex(' + p.id + ')">' + t('resetDex') + '</button><button class="danger" onclick="kick(' + p.id + ')">' + t('kick') + '</button></div></div>'; }).join('')
    : '<p class="muted">' + t('noPlayers') + '</p>';
  if (html !== playerHtml && !(document.activeElement && document.activeElement.tagName === 'INPUT' && playerList.contains(document.activeElement))) { playerList.innerHTML = html; playerHtml = html; }

  const keepGive = giveTo.value;
  // Pokemon go to a CHARACTER, so a player with none is listed greyed out
  giveTo.innerHTML = list.flatMap(p => p.trainers.length
    ? p.trainers.map(tr => '<option value="' + tr.id + '">' + esc(tr.name) + ' #' + tr.id + ' (' + esc(p.name) + ')</option>')
    : ['<option disabled>' + esc(t('noCharNote', p.name)) + '</option>']).join('');
  if ([...giveTo.options].some(o => o.value === keepGive && !o.disabled)) giveTo.value = keepGive;
  else giveTo.selectedIndex = [...giveTo.options].findIndex(o => !o.disabled);

  const keep = target.value;
  target.innerHTML = '<option value="all">' + t('everyone') + '</option>' +
    list.map(p => '<option value="' + p.id + '">' + esc(p.name) + '</option>').join('');
  target.value = [...target.options].some(o => o.value === keep) ? keep : 'all';
}

// Players asking to come back: a bar at the top of the Master page, whatever tab or session is open
let rejoinHtml = null;
async function pollRejoin() {
  const r = await fetch('/api/rejoin/pending').catch(() => null);
  if (!r || !r.ok) return;
  const list = await r.json();
  const html = list.map(x => '<div class="rjreq"><span>\uD83D\uDD14 ' + t('rjMaster', esc(x.name), x.code) + (x.online ? ' <small>(' + t('rjOnline') + ')</small>' : '') + '</span>' +
    '<button class="primary" onclick="answerRejoin(' + x.id + ',1)">' + t('rjYes') + '</button><button class="danger" onclick="answerRejoin(' + x.id + ',0)">' + t('rjNo') + '</button></div>').join('');
  if (html !== rejoinHtml) { rejoinBar.innerHTML = html; rejoinHtml = html; }
}

async function answerRejoin(id, ok) {
  await fetch('/api/rejoin/answer', { method: 'POST', body: new URLSearchParams({ id, ok }) });
  pollRejoin();
}

async function claimCode(playerId) {
  const r = await fetch('/api/claimcode', { method: 'POST', body: new URLSearchParams({ code: spawnSession.value, playerId }) });
  const j = await r.json();
  if (r.ok) alert(t('claimMsg', j.claim, spawnSession.value));
}

async function kick(playerId) {
  if (!await askConfirm(t('kickConfirm'))) return;
  await fetch('/api/kick', { method: 'POST', body: new URLSearchParams({ code: spawnSession.value, playerId }) });
  loadPlayers();
}

async function createSession() {
  const body = new URLSearchParams({ name: nameInput.value });
  const r = await fetch('/api/session', { method: 'POST', body });
  const j = await r.json();
  if (!r.ok) { out.textContent = errText(j.error); return; }

  out.innerHTML = t('created', '<b>' + j.code + '</b>');
  loadSessions();
}

//=========MASTER: GIVE=============

// the Master's choices (empty = random)
const picked = () => ({ nature: natureSel.value, ability: abilitySel.value, gender: genderSel.value, shiny: shinySel.value });

async function give() {
  if (!formList.value || !giveTo.value) { spawnLog.textContent = t('pickMonChar'); return; }
  const body = new URLSearchParams({
    code: spawnSession.value, formId: formList.value, level: levelInput.value, trainerId: giveTo.value, ...picked()
  });
  const r = await fetch('/api/give', { method: 'POST', body });
  const j = await r.json();
  if (!r.ok) { spawnLog.textContent = errText(j.error); return; }
  spawnLog.insertAdjacentHTML('afterbegin',
    '<p>' + sprite(keyOf(j), 'small', j.shiny) + esc(t('gave', (j.shiny ? t('shiny') + ' ' : '') + j.species, j.level, giveTo.options[giveTo.selectedIndex].text)) + '<br><small>' + esc(j.nature + ' · ' + j.ability + ' · ' + t(GENDER[j.gender])) + '</small></p>');
}

//=========MASTER: SPAWN=============

let allForms = [];

async function loadForms() {
  await artReady;
  formList.onchange = () => { showPreview(); loadOptions(); };
  shinySel.onchange = showPreview;
  levelInput.oninput = loadOptions;
  const r = await fetch('/api/forms');
  allForms = await r.json();
  allForms.forEach(f => { f.dex = dexOf(f); f.regions = regionsOf(f); });
  renderFilters('spawn');
  filterForms();
  renderAreas();
}

//=========POKEMON FILTERS (Spawn picker + Areas picker)=========
// Region comes from the national Pokedex number (the number in the art file name, "25", "26-alola"); regional forms
// count for their own region too (Alolan Raichu shows under Kanto and Alola). Filters stack: region (any of them),
// type (all of them), evolution stage. Same bar in both pickers, each with its own state.

const REGIONS = [['Kanto', 1, 151], ['Johto', 152, 251], ['Hoenn', 252, 386], ['Sinnoh', 387, 493], ['Unova', 494, 649], ['Kalos', 650, 721], ['Alola', 722, 809], ['Galar', 810, 898]];
const TYPE_COLORS = { Normal: '#a8a878', Fire: '#f08030', Water: '#6890f0', Electric: '#f8d030', Grass: '#78c850', Ice: '#98d8d8', Fighting: '#c03028', Poison: '#a040a0', Ground: '#e0c068',
  Flying: '#a890f0', Psychic: '#f85888', Bug: '#a8b820', Rock: '#b8a038', Ghost: '#705898', Dragon: '#7038f8', Dark: '#705848', Steel: '#b8b8d0', Fairy: '#ee99ac' };

const dexOf = f => { const m = /^(\d+)/.exec(art[f.key] || ''); return m ? +m[1] : 0; };
function regionsOf(f) {
  const n = dexOf(f), r = REGIONS.find(x => n >= x[1] && n <= x[2]), out = r ? [r[0]] : [];
  if (/\(Alolan\)/.test(f.label)) out.push('Alola');
  if (/\(Galarian\)/.test(f.label)) out.push('Galar');
  return [...new Set(out)];
}

const newFilter = () => ({ regions: new Set(), types: new Set(), stage: 'all', sort: 'az', limit: 120 });
const FILTER = { spawn: newFilter(), area: newFilter() };
const FILTER_RUN = { spawn: () => filterForms(), area: () => filterAreaForms() };

function applyFilters(st, text, src) {
  let list = searchForms(text, src);
  if (st.regions.size) list = list.filter(f => f.regions.some(r => st.regions.has(r)));
  if (st.types.size) list = list.filter(f => [...st.types].every(ty => f.types.includes(ty)));
  if (st.stage === 'base') list = list.filter(f => f.stage === 1);
  else if (st.stage === 'evolved') list = list.filter(f => f.stage > 1);
  if (st.sort === 'dex') list = [...list].sort((a, b) => a.dex - b.dex || a.label.localeCompare(b.label));
  return list;
}

function renderFilters(key) {
  const st = FILTER[key], box = document.getElementById({ spawn: 'spawnFilters', area: 'aFilters' }[key]);
  if (!box) return;
  const chip = (kind, value, label, color) => '<button type="button" class="fchip' + (st[kind].has(value) ? ' on' : '') + '"' + (color ? ' style="--c:' + color + '"' : '') +
    ' onclick="toggleFilter(\'' + key + '\',\'' + kind + '\',\'' + value + '\')">' + label + '</button>';
  const sel = (field, opts) => '<select onchange="setFilter(\'' + key + '\',\'' + field + '\',this.value)">' + opts.map(([v, l]) => '<option value="' + v + '"' + (st[field] === v ? ' selected' : '') + '>' + l + '</option>').join('') + '</select>';
  const n = st.regions.size + st.types.size;
  box.innerHTML =
    '<div class="fbar">' +
      '<button type="button" class="fopen' + (st.open ? ' on' : '') + '" onclick="toggleFilterPanel(\'' + key + '\')">\u2630 ' + t('fFilters') + (n ? ' <span class="fbadge">' + n + '</span>' : '') + '</button>' +
      sel('stage', [['all', t('fStageAll')], ['base', t('fStageBase')], ['evolved', t('fStageEvolved')]]) +
      sel('sort', [['az', t('fSortAz')], ['dex', t('fSortDex')]]) +
      (n || st.stage !== 'all' ? '<button type="button" class="chipbtn" onclick="clearFilters(\'' + key + '\')">' + t('fClear') + '</button>' : '') +
      '<span class="fcount" id="fcount_' + key + '"></span></div>' +
    (st.open ?
      '<div class="fpanel"><div class="fgroup"><h5>' + t('fRegion') + '</h5><div class="fgrid reg">' + REGIONS.map(r => chip('regions', r[0], r[0])).join('') + '</div></div>' +
      '<div class="fgroup"><h5>' + t('fType') + '</h5><div class="fgrid typ">' + Object.keys(TYPE_COLORS).map(ty => chip('types', ty, t('ty_' + ty), TYPE_COLORS[ty])).join('') + '</div></div></div>' : '');
}

function toggleFilterPanel(key) { FILTER[key].open = !FILTER[key].open; renderFilters(key); FILTER_RUN[key](); }

function setFilterCount(key, n) {
  const el = document.getElementById('fcount_' + key);
  if (el) el.textContent = t('fCount', n);
}

function toggleFilter(key, kind, value) {
  const set = FILTER[key][kind];
  if (!set.delete(value)) set.add(value);
  FILTER[key].limit = 120;
  renderFilters(key); FILTER_RUN[key]();
}
function setFilter(key, field, value) { FILTER[key][field] = value; FILTER[key].limit = 120; FILTER_RUN[key](); }
function clearFilters(key) { const o = FILTER[key].open; Object.assign(FILTER[key], newFilter(), { open: o }); renderFilters(key); FILTER_RUN[key](); }

// Searching "bulba" shows Bulbasaur AND Ivysaur and Venusaur:
// every form whose evolution family has a match, in family + stage order.
// Used by the Spawn picker and by the Areas picker.
function searchForms(text, src = allForms) {
  text = text.trim().toLowerCase();
  if (!text) return src;

  const families = new Set(src.filter(f => f.label.toLowerCase().includes(text)).map(f => f.family));
  const firstLabel = {};  // family -> alphabetical first member, to order the groups
  src.filter(f => families.has(f.family)).forEach(f => { firstLabel[f.family] ??= f.label; });

  return src
    .filter(f => families.has(f.family))
    .sort((a, b) =>
      firstLabel[a.family].localeCompare(firstLabel[b.family]) ||
      a.family - b.family || a.stage - b.stage || a.label.localeCompare(b.label));
}

// <option>s for a form list; with a search text the evolution stage is shown with arrows
function formOptions(shown, text, st) {
  return shown
    .map(f => '<option value="' + f.id + '">' + (st && st.sort === 'dex' ? '#' + String(f.dex).padStart(3, '0') + ' ' : '') + (text.trim() ? '\u21B3'.repeat(f.stage - 1) + ' ' : '') + esc(f.label) + '</option>').join('');
}

function filterForms() {
  const list = applyFilters(FILTER.spawn, formSearch.value);
  setFilterCount('spawn', list.length);
  formList.innerHTML = formOptions(list, formSearch.value, FILTER.spawn);
  if (formList.options.length) formList.selectedIndex = 0;
  showPreview();
  loadOptions();
}

// Nature / ability / gender lists for the selected form and level (what the rules allow)
const STAT = { 'HP': 'HP', 'Attack': 'Atk', 'Defense': 'Def', 'Special Attack': 'SpA', 'Special Defense': 'SpD', 'Speed': 'Spe' };
const GENDER = { Male: 'male', Female: 'female', Genderless: 'genderless' };

async function loadOptions() {
  if (!formList.value) return;
  const r = await fetch('/api/options?formId=' + formList.value + '&level=' + (levelInput.value || 1));
  if (!r.ok) return;
  const o = await r.json();
  const fill = (sel, items) => {
    const keep = sel.value;
    sel.innerHTML = '<option value="">' + t('random') + '</option>' + items.map(i => '<option value="' + esc(i[0]) + '">' + esc(i[1]) + '</option>').join('');
    if ([...sel.options].some(op => op.value === keep)) sel.value = keep;
  };
  fill(natureSel, o.natures.map(n => [n.id, n.name + ' (+' + STAT[n.up] + ' -' + STAT[n.down] + ')']));
  fill(abilitySel, o.abilities.map(x => [x.id, x.name]));
  fill(genderSel, o.genders.map(g => [g, t(GENDER[g])]));
}

function showPreview() {
  const f = allForms.find(f => f.id == formList.value);
  preview.src = f && art[f.key] ? '/assets/sprites/' + (shinySel.value === '1' ? 'shiny/' : '') + art[f.key] + '.png' : '';
  preview.hidden = !preview.src;
  preview.classList.toggle('shiny', shinySel.value === '1');
}

async function spawn() {
  const fromArea = srcMode === 'area';
  if (fromArea ? !spawnArea.value : !formList.value) { spawnLog.textContent = fromArea ? t('pickArea') : t('pickMon'); return; }

  const body = new URLSearchParams(fromArea
    ? { code: spawnSession.value, areaId: spawnArea.value, target: target.value, shiny: shinySel.value, catch: catchMode, tries: triesInput.value }
    : { code: spawnSession.value, formId: formList.value, level: levelInput.value, target: target.value, catch: catchMode, tries: triesInput.value, ...picked() });
  const r = await fetch(fromArea ? '/api/area/roll' : '/api/spawn', { method: 'POST', body });
  const j = await r.json();
  if (!r.ok) { spawnLog.textContent = errText(j.error); return; }

  spawnLog.insertAdjacentHTML('afterbegin',
    '<p>' + sprite(keyOf(j), 'small', j.shiny) + esc(t('spawned', (j.shiny ? t('shiny') + ' ' : '') + j.species, j.form, j.level, spawnSession.value, target.options[target.selectedIndex].text)) + '<br><small>' + esc(j.nature + ' · ' + j.ability + ' · ' + t(GENDER[j.gender])) + '</small></p>');
  activeHtml = ''; loadActive();
}

//=========MASTER: INVITE (internet link + code + QR)=========
// The server puts this computer on the internet with a Cloudflare quick tunnel; players open the link from any network.

let inviteHtml = '';

async function loadInvite() {
  if (!window.invite) return;
  let j;
  try { const r = await fetch('/api/tunnel'); if (!r.ok) return; j = await r.json(); } catch (e) { return; }
  const code = window.spawnSession ? spawnSession.value : '';
  let html;
  if (j.status === 'ready') {
    const text = j.url + (code ? '/?code=' + code : '');
    const c = document.createElement('canvas');
    const qr = QR.draw(c, text, 6) ? '<img class="qr" alt="QR" src="' + c.toDataURL() + '">' : '';
    html = '<h3>' + t('inviteHead') + '</h3><div class="invrow">' + qr + '<div class="invtxt">' +
      '<div><span class="muted">' + t('inviteLink') + '</span><br><b class="invlink">' + esc(j.url) + '</b></div>' +
      '<div><span class="muted">' + t('inviteCode') + '</span><br><b class="invcode">' + (code ? esc(code) : t('inviteNoCode')) + '</b></div>' +
      '<button class="primary" onclick="copyInvite(\'' + esc(j.url) + '\',\'' + esc(code) + '\')">' + t('inviteCopy') + '</button>' +
      '<small class="muted">' + t('inviteLan', esc(j.lan)) + '</small></div></div>';
  } else if (j.status === 'download' || j.status === 'starting') {
    html = '<h3>' + t('inviteHead') + '</h3><p>' + t(j.status === 'download' ? 'inviteDownload' : 'inviteStarting') + '</p>';
  } else {
    html = '<h3>' + t('inviteHead') + '</h3>' +
      (j.status === 'error' ? '<p class="err">' + esc(j.message) + '</p>' : '<p class="muted">' + t('inviteOff') + '</p>') +
      '<button class="primary" onclick="tunnelStart()">' + t(j.status === 'error' ? 'inviteRetry' : 'inviteStart') + '</button><small class="muted">' + t('inviteLan', esc(j.lan)) + '</small>';
  }
  if (html !== inviteHtml) { invite.innerHTML = html; inviteHtml = html; }
}

async function tunnelStart() {
  await fetch('/api/tunnel', { method: 'POST' });
  inviteHtml = ''; loadInvite();
}

async function copyInvite(url, code) {
  const text = code ? t('inviteMsg', url, code) : url;
  try { await navigator.clipboard.writeText(text); }
  catch (e) { const a = document.createElement('textarea'); a.value = text; document.body.appendChild(a); a.select(); try { document.execCommand('copy'); } catch (e2) {} a.remove(); }
  const b = document.querySelector('#invite button.primary');
  if (b) { const old = b.textContent; b.textContent = t('inviteCopied'); setTimeout(() => { b.textContent = old; }, 1500); }
}

//=========MASTER: CATCH MODE (what is on the field)=========

let catchMode = 'normal', srcMode = 'pick', activeHtml = '';

function setCatch(v) { catchMode = v; document.querySelectorAll('#catchSeg button').forEach(b => b.classList.toggle('on', b.dataset.v === v)); }

function setSource(v) {
  srcMode = v;
  document.querySelectorAll('#srcSeg button').forEach(b => b.classList.toggle('on', b.dataset.v === v));
  pickBox.hidden = v !== 'pick'; areaBox.hidden = v !== 'area';
  if (v === 'area') areaInfo();
}

const segHtml = (id, mode) => '<div class="seg mini">' + [['normal', t('catchNormalShort')], ['always', '100%'], ['never', '0%']].map(([v, label]) =>
  '<button class="' + (mode === v ? 'on' : '') + '" onclick="setMode(' + id + ',\'' + v + '\')">' + label + '</button>').join('') + '</div>';

async function setMode(id, mode) {
  await fetch('/api/catchmode', { method: 'POST', body: new URLSearchParams({ pokemonId: id, mode }) });
  activeHtml = ''; loadActive();
}

async function loadActive() {
  if (currentTab !== 'spawn' || !spawnSession.value) return;
  if (activeList.contains(document.activeElement) && document.activeElement.tagName === 'INPUT') return;  // do not rebuild while typing
  const r = await fetch('/api/active?code=' + spawnSession.value);
  if (!r.ok) return;
  const list = (await r.json()).reverse();
  const html = list.length ? list.map(a =>
    '<div class="actwrap"><div class="act">' + sprite(keyOf(a), 'small', a.shiny) +
    '<span class="actn"><b>' + esc((a.shiny ? t('shiny') + ' ' : '') + a.species) + '</b> ' + t('lv') + a.level +
    '<br><small class="muted">' + esc(a.target ? (playerNames[a.target] || '#' + a.target) : t('everyone')) + '</small></span>' + segHtml(a.id, a.mode) + '</div>' + wildControls(a) + '</div>').join('')
    : '<p class="muted">' + t('fieldEmpty') + '</p>';
  if (html !== activeHtml) { activeList.innerHTML = html; activeHtml = html; }
}

// "Combat" without combat: the Master weakens the wild Pokemon and puts effects on it; each one changes the capture rate
const wOpen = new Set();
function wToggle(d) { const id = +d.dataset.w; if (d.open) wOpen.add(id); else wOpen.delete(id); }
const STAT_SHORT = ['HP', 'Atk', 'Def', 'SpA', 'SpD', 'Spe'];

// Stats (base + nature + spent), nature, ability and capabilities of a wild Pokemon: the Master's cheat sheet for combat
function wildInfo2(a) {
  if (!a.base) return '';
  const tot = a.base.map((b, i) => Math.max(i ? 0 : 1, b + a.nat[i]) + a.pts[i]);
  return '<details class="winfo" data-w="' + a.id + '"' + (wOpen.has(a.id) ? ' open' : '') + ' ontoggle="wToggle(this)"><summary>' + t('wildInfo') + '</summary>' +
    '<small>' + esc(a.nature) + ' &middot; ' + esc(a.ability) + '</small>' +
    '<table class="st-table wstats"><tr>' + STAT_SHORT.map(x => '<th>' + x + '</th>').join('') + '</tr><tr>' + tot.map(v => '<td>' + v + '</td>').join('') + '</tr></table>' +
    '<div class="caps">' + a.caps.map(c => '<span class="chip ccap">' + esc(c[0]) + (c[1] ? ' ' + esc(c[1]) : '') + '</span>').join('') + '</div></details>';
}

const FX_PLUS = { burn: 10, freeze: 10, paralysis: 10, poison: 10, sleep: 5, confusion: 5, stuck: 10, slow: 5 };  // same numbers as CaptureServices.EFFECTS (hint only; the server computes)

function wildControls(a) {
  const pct = Math.round(100 * a.hp / a.maxHp);
  const hpBtn = (label, f) => '<button class="chipbtn" onclick="wildPost(' + a.id + ',{hp:' + Math.max(f === 1 / a.maxHp ? 1 : 0, Math.round(a.maxHp * f)) + '})">' + label + '</button>';
  return '<div class="wildctl">' + wildInfo2(a) +
    '<div class="wrow"><b>' + t('wildHp') + '</b><div class="bar hp ' + (pct > 50 ? 'ok' : pct > 25 ? 'mid' : 'low') + '"><i style="width:' + pct + '%"></i></div>' +
      '<input type="number" class="w" id="whp' + a.id + '" min="0" max="' + a.maxHp + '" value="' + Math.round(a.hp) + '" onchange="wildPost(' + a.id + ',{hp:this.value})"><small>/ ' + a.maxHp + '</small></div>' +
    '<div class="wrow">' + hpBtn(t('wildFull'), 1) + hpBtn('50%', .5) + hpBtn('25%', .25) + hpBtn(t('wildOne'), 1 / a.maxHp) + '</div>' +
    '<div class="wrow">' + Object.keys(FX_PLUS).map(c => '<button class="chipbtn' + (a.fx.includes(c) ? ' on' : '') + '" onclick="wildPost(' + a.id + ',{fx:\'' + c + '\'})">' + t('fx_' + c) + ' +' + FX_PLUS[c] + '</button>').join('') + '</div>' +
    '<div class="wrow"><b>' + t('wildInj') + '</b><button class="sq" onclick="wildPost(' + a.id + ',{inj:' + (a.inj - 1) + '})">-</button><span>' + a.inj + '</span><button class="sq" onclick="wildPost(' + a.id + ',{inj:' + (a.inj + 1) + '})">+</button>' +
      '<b>' + t('wildTries') + '</b><button class="sq" onclick="wildPost(' + a.id + ',{tries:' + (a.tries < 0 ? 3 : Math.max(1, a.tries - 1)) + '})">-</button><span>' + (a.tries < 0 ? '\u221E' : a.tries) + '</span><button class="sq" onclick="wildPost(' + a.id + ',{tries:' + (a.tries < 0 ? 3 : a.tries + 1) + '})">+</button><button class="chipbtn' + (a.tries < 0 ? ' on' : '') + '" onclick="wildPost(' + a.id + ',{tries:0})">\u221E</button>' +
      '<b>' + t('wildBonus') + '</b><input type="number" class="w" value="' + a.bonus + '" onchange="wildPost(' + a.id + ',{bonus:this.value})">' +
      '<span class="rate">' + t('wildRate') + ' <b>' + a.rate + '</b></span></div>' +
    '<div class="wrow"><button class="danger" onclick="wildRemove(' + a.id + ')">' + t('wildRemove') + '</button></div></div>';
}

async function wildRemove(id) {
  if (!await askConfirm(t('wildRemoveAsk'))) return;
  await fetch('/api/wild', { method: 'POST', body: new URLSearchParams({ pokemonId: id, remove: 1, code: spawnSession.value }) });
  activeHtml = ''; loadActive();
}

async function wildPost(id, fields) {
  await fetch('/api/wild', { method: 'POST', body: new URLSearchParams({ pokemonId: id, ...fields }) });
  activeHtml = ''; loadActive();
}

//=========MASTER: AREAS=========
// An area = name + level range + forms with weights. "Roll an area" in the Spawn tab picks one at random.

let areas = [], areaSel = 0;

async function loadAreas() {
  const r = await fetch('/api/areas');
  if (!r.ok) return;
  areas = await r.json();
  renderAreas();
}

const formOf = id => allForms.find(f => f.id === id);

function renderAreas() {
  areaList.innerHTML = areas.length ? areas.map(a =>
    '<button class="areaitem' + (a.id === areaSel ? ' on' : '') + '" onclick="areaPick(' + a.id + ')"><b>' + esc(a.name) + '</b><small>' +
    t('lv') + a.min + '–' + a.max + ' · ' + t('areaForms', a.forms.length) + '</small></button>').join('') : '<p class="muted">' + t('noAreas') + '</p>';

  const a = areas.find(x => x.id === areaSel);
  const total = a ? a.forms.reduce((n, f) => n + f.weight, 0) : 0;
  areaEditor.innerHTML =
    '<h3>' + (a ? esc(a.name) : t('newArea')) + '</h3>' +
    '<label><span>' + t('areaName') + '</span><input id="aName" value="' + esc(a ? a.name : '') + '"></label>' +
    '<div class="grid2"><label><span>' + t('minLv') + '</span><input id="aMin" type="number" min="1" max="100" value="' + (a ? a.min : 2) + '"></label>' +
    '<label><span>' + t('maxLv') + '</span><input id="aMax" type="number" min="1" max="100" value="' + (a ? a.max : 6) + '"></label></div>' +
    '<div class="row"><button class="primary" onclick="areaSave()">' + t('save') + '</button>' + (a ? '<button class="danger" onclick="areaDelete()">' + t('del') + '</button>' : '') + '</div>' +
    (a ? '<hr><h3>' + t('areaPool') + '</h3>' +
      (a.forms.length ? a.forms.map(f => {
        const form = formOf(f.formId);
        return '<div class="act">' + (form ? sprite(form.key, 'small') : '') +
          '<span class="actn"><b>' + esc(form ? form.label : '#' + f.formId) + '</b><br><small class="muted">' + Math.round(f.weight / total * 100) + '%</small></span>' +
          '<input class="w" type="number" min="0.1" step="0.1" value="' + f.weight + '" onchange="areaWeight(' + f.formId + ',this.value)" title="' + t('weight') + '">' +
          '<button class="danger sq" onclick="areaRemoveForm(' + f.id + ')" aria-label="' + t('del') + '">&times;</button></div>';
      }).join('') : '<p class="muted">' + t('areaEmpty') + '</p>') +
      '<div class="addform"><input id="aFormSearch" placeholder="' + t('searchPh') + '" oninput="FILTER.area.limit = 120; filterAreaForms()">' +
      '<div id="aFilters" class="filters"></div>' +
      '<div id="aFormList" class="tiles"></div>' +
      '<div class="row"><input id="aWeight" class="w" type="number" min="0.1" step="0.1" value="1" title="' + t('weight') + '"><button onclick="areaAddForm()">' + t('addForm') + '</button></div></div>'
      : '');
  if (a) { renderFilters('area'); filterAreaForms(); }
  fillAreaSelect();
}

// Picture grid (a <select> cannot show images): click a tile to choose the Pokemon to add
let areaPickId = 0;
function filterAreaForms() {
  const text = aFormSearch.value, st = FILTER.area;
  const list = applyFilters(st, text), shown = list.slice(0, st.limit);
  setFilterCount('area', list.length);
  if (!shown.some(f => f.id === areaPickId)) areaPickId = shown.length ? shown[0].id : 0;
  aFormList.innerHTML = shown.map(f =>
    '<button type="button" class="tile' + (f.id === areaPickId ? ' on' : '') + '" data-id="' + f.id + '" onclick="areaTile(' + f.id + ')">' +
    sprite(f.key, 'small') + '<span>' + (st.sort === 'dex' ? '#' + String(f.dex).padStart(3, '0') + ' ' : '') + (text.trim() ? '\u21B3'.repeat(f.stage - 1) + ' ' : '') + esc(f.label) + '</span></button>').join('') +
    (list.length > shown.length ? '<button type="button" class="tile more" onclick="FILTER.area.limit += 120; filterAreaForms()">' + t('fMore', list.length - shown.length) + '</button>' : '');
}

function areaTile(id) {
  areaPickId = id;
  aFormList.querySelectorAll('.tile').forEach(b => b.classList.toggle('on', +b.dataset.id === id));
}

function fillAreaSelect() {
  const keep = spawnArea.value;
  spawnArea.innerHTML = areas.map(a => '<option value="' + a.id + '">' + esc(a.name) + '</option>').join('');
  if ([...spawnArea.options].some(o => o.value === keep)) spawnArea.value = keep;
  areaInfo();
}

function areaInfo() {
  const a = areas.find(x => String(x.id) === spawnArea.value);
  areaNote.textContent = !a ? t('noAreas') : a.forms.length ? t('lv') + a.min + '–' + a.max + ' · ' + t('areaForms', a.forms.length) : t('areaEmpty');
}

function areaPick(id) { areaSel = id; renderAreas(); }

async function areaPost(url, body) {
  const r = await fetch(url, { method: 'POST', body: new URLSearchParams(body) });
  const j = await r.json();
  if (!r.ok) { out.textContent = errText(j.error); return false; }
  out.textContent = '';
  areas = j; renderAreas();
  return true;
}

async function areaSave() {
  const before = areas.map(a => a.id);
  if (await areaPost('/api/area', { id: areaSel || '', name: aName.value, min: aMin.value, max: aMax.value }) && !areaSel) {
    const made = areas.find(a => !before.includes(a.id));
    if (made) areaPick(made.id);
  }
}

async function areaDelete() {
  const a = areas.find(x => x.id === areaSel);
  if (!await askConfirm(t('delAreaAsk', a.name))) return;
  areaSel = 0;
  areaPost('/api/area/delete', { id: a.id });
}

function areaAddForm() {
  if (!areaPickId) return;
  areaPost('/api/area/form', { areaId: areaSel, formId: areaPickId, weight: aWeight.value });
}

function areaWeight(formId, weight) { areaPost('/api/area/form', { areaId: areaSel, formId, weight }); }
function areaRemoveForm(rowId) { areaPost('/api/area/form', { rowId }); }

// Opened from the invite link or QR (?code=ABCD): the session code is already filled in
window.addEventListener('DOMContentLoaded', () => {
  const c = new URLSearchParams(location.search).get('code');
  if (c && window.codeInput) codeInput.value = c.toUpperCase().slice(0, 4);
});


//=========POKEDEX ALBUM (player)=========
// Two sticker albums (normal and shiny), by region or all together. Every slot starts black ("???" + number).
// SEEN: the Pokemon in black and white with its name. The first capture of a form puts a sticker in the PACK (state 3);
// opening the pack (tear it open) puts the stickers in the hand (state 1); a sticker is dropped on its slot and pressed
// down with the finger from the bottom to the top (state 2 = stuck). Sticker states: 0 none, 1 loose, 2 stuck, 3 in the pack.

let dexData = { total: 0, caught: 0, shinyCaught: 0, entries: [] };
let dexRegion = 'all', dexShiny = false, dexPick = 0, dexPeel = null, dexSlots = null;

// one slot per art id (971): number, region (Alolan / Galarian forms belong to Alola / Galar)
function dexBuildSlots() {
  const regionOf = id => id.endsWith('-alola') ? 'Alola' : id.endsWith('-galar') ? 'Galar' : (REGIONS.find(r => parseInt(id) >= r[1] && parseInt(id) <= r[2]) || [''])[0];
  dexSlots = {};
  for (const id of new Set(Object.values(art))) (dexSlots[regionOf(id)] ??= []).push({ id, n: parseInt(id) });
  const order = (a, b) => a.n - b.n || a.id.length - b.id.length || a.id.localeCompare(b.id);
  for (const list of Object.values(dexSlots)) list.sort(order);
  dexSlots.all = Object.values(dexSlots).flat().sort(order);
}

// The sprite files are 96x96 with a lot of empty border (a Pidgey is a small dot in the middle). A sticker is the
// Pokemon itself, so each sprite is cropped to the pixels it really uses and scaled up: box = where they are.
const DEX_BOX = {};
async function dexLoadBoxes() {
  const ids = [...new Set(dexData.entries.map(e => art[e.key]).filter(id => id && !DEX_BOX[id]))];
  await Promise.all(ids.map(id => new Promise(done => {
    const im = new Image();
    im.onload = () => {
      const c = document.createElement('canvas'); c.width = c.height = 96;
      const g = c.getContext('2d'); g.drawImage(im, 0, 0);
      const d = g.getImageData(0, 0, 96, 96).data;
      let x0 = 96, y0 = 96, x1 = -1, y1 = -1;
      for (let y = 0; y < 96; y++) for (let x = 0; x < 96; x++) if (d[(y * 96 + x) * 4 + 3] > 20) { x0 = Math.min(x0, x); x1 = Math.max(x1, x); y0 = Math.min(y0, y); y1 = Math.max(y1, y); }
      DEX_BOX[id] = x1 >= x0 ? { x: x0, y: y0, w: x1 - x0 + 1, h: y1 - y0 + 1 } : { x: 0, y: 0, w: 96, h: 96 };
      done();
    };
    im.onerror = done;
    im.src = '/assets/sprites/' + id + '.png';
  })));
}
const fitSize = (key, max) => { const b = DEX_BOX[art[key]] || { w: 96, h: 96 }, k = max / Math.max(b.w, b.h); return { w: Math.round(b.w * k), h: Math.round(b.h * k) }; };
function fit(key, shiny, max, cls = '') {
  const id = art[key], b = DEX_BOX[id] || { x: 0, y: 0, w: 96, h: 96 }, k = max / Math.max(b.w, b.h);
  return '<div class="fit ' + cls + '" style="width:' + Math.round(b.w * k) + 'px;height:' + Math.round(b.h * k) + 'px;background-image:url(/assets/sprites/' + (shiny ? 'shiny/' : '') + id + '.png);background-size:' + 96 * k + 'px;background-position:-' + b.x * k + 'px -' + b.y * k + 'px"></div>';
}

async function loadDex() {
  await artReady;
  if (!dexSlots) dexBuildSlots();
  const c = auth();
  const r = await fetch('/api/dex?code=' + encodeURIComponent(c.code) + '&token=' + encodeURIComponent(c.token));
  if (!r.ok) return;
  dexData = await r.json();
  await dexLoadBoxes();
  renderDex();
}

const dexNum = n => '#' + String(n).padStart(3, '0');
const dexList = () => dexData.entries.filter(e => e.shiny === dexShiny);
const dexEntryOf = id => dexList().filter(e => art[e.key] === id);
const dexRegionOfEntry = e => REGIONS.map(r => r[0]).find(n => dexSlots[n].some(s => s.id === art[e.key]));
const dexPackItems = () => dexData.entries.filter(e => e.sticker === 3);

function dexToggleShiny() { dexShiny = !dexShiny; dexPick = 0; dexPeel = null; document.getElementById('dexMsg').textContent = ''; renderDex(); }
function dexSetRegion(name) { dexRegion = name; dexPeel = null; renderDex(); }

// handToo = false while a sticker is being dragged: its own element must stay alive
function renderDex(handToo = true) {
  if (!document.getElementById('dexGrid') || !dexSlots) return;
  const mine = dexList();
  const stuckIn = list => list.filter(s => dexEntryOf(s.id).some(e => e.sticker === 2)).length;
  const loose = shiny => dexData.entries.filter(e => e.shiny === shiny && e.sticker === 1).length;
  document.getElementById('dexSummary').textContent = t('dexSummary', mine.length, dexShiny ? dexData.shinyCaught : dexData.caught, dexData.total);
  const btn = document.getElementById('dexShinyBtn');
  btn.classList.toggle('on', dexShiny);
  btn.innerHTML = '\u2728 ' + t('dexShiny') + (loose(!dexShiny) ? ' <span class="fbadge">' + loose(!dexShiny) + '</span>' : '');
  document.getElementById('dexAlbum').classList.toggle('gold', dexShiny);
  document.getElementById('dexTabs').innerHTML = [['all', t('dexAll')], ...REGIONS.map(r => [r[0], r[0]])].map(([n, label]) =>
    '<button type="button" class="' + (n === dexRegion ? 'on' : '') + '" onclick="dexSetRegion(\'' + n + '\')">' + label + '<small>' + stuckIn(dexSlots[n] || []) + '/' + (dexSlots[n] || []).length + '</small></button>').join('');

  const pickedEntry = dexPick ? dexData.entries.find(e => e.id === dexPick && e.shiny === dexShiny) : null;
  const wanted = pickedEntry ? art[pickedEntry.key] : '';
  document.getElementById('dexGrid').innerHTML = (dexSlots[dexRegion] || []).map(s => {
    const es = dexEntryOf(s.id), stuck = es.find(e => e.sticker === 2), seen = stuck || es[0];
    const peeling = dexPeel && dexPeel.artId === s.id;
    const cls = 'dslot ' + (stuck ? 'stuck' : seen ? 'seen' : 'empty') + (s.id === wanted ? ' target' : '') + (peeling ? ' peeling' : '');
    return '<button type="button" class="' + cls + '" data-art="' + s.id + '" onclick="slotTap(\'' + s.id + '\')">' +
      (seen ? fit(seen.key, dexShiny, 78, stuck ? 'die' + (dexShiny ? ' gold' : '') : 'ghostfit') + '<span>' + esc(seen.label) + '</span>' : '<i class="q"></i><span>???</span>') +
      '<b>' + dexNum(s.n) + '</b>' + (stuck && stuck.lvl ? '<em>' + t('dexLvl', stuck.lvl) + (dexShiny ? ' \u2605' : '') + '</em>' : '') +
      (peeling ? peelHtml(dexPeel.bmp) : '') + '</button>';
  }).join('');

  paintPeel();
  document.querySelector('.dslot.peeling, .dslot.target')?.scrollIntoView({ block: 'center' });
  if (!handToo) return;
  const n = dexPackItems().length, pb = document.getElementById('dexPackBtn');
  pb.classList.toggle('empty', !n);
  pb.querySelector('span').textContent = n ? t('packBtn', n) : t('packNone');
  const hand = mine.filter(e => e.sticker === 1);
  document.getElementById('dexHand').innerHTML = hand.length ? hand.map(e => '<div class="sticker' + (e.id === dexPick ? ' on' : '') + '" ondragstart="return false" onpointerdown="stickerDown(event, ' + e.id + ')">' +
    fit(e.key, e.shiny, 84, 'die' + (e.shiny ? ' gold' : '')) + '<span>' + esc(e.label) + '</span><em>' + t('dexLvl', e.lvl) + (e.shiny ? ' \u2605' : '') + '</em></div>').join('')
    : '<p class="muted">' + t(n ? 'dexHandPack' : 'dexHandEmpty') + '</p>';
}

// The sticker over its slot, drawn on a canvas (see paintPeel). Below the line it is stuck (flat); above it, it is held up
// off the page, curved, with its blank back showing, like a real sticker being laid down. dexPeel.p = 0..1 = how much is stuck.
let peelSize = 120;
const PEEL_PAD = 6, PEEL_OX = 6, PEEL_OY = 14;
function peelHtml(bmp) {
  return '<canvas class="peelc" width="' + (bmp.width + PEEL_OX * 2) + '" height="' + (bmp.height + PEEL_OY * 2) + '" onpointerdown="peelDown(event)" ondragstart="return false"></canvas>';
}

// the sticker as one bitmap: the Pokemon cropped to its pixels, scaled up, with the white die-cut outline
async function peelBitmap(e) {
  const id = art[e.key], b = DEX_BOX[id] || { x: 0, y: 0, w: 96, h: 96 }, k = peelSize / Math.max(b.w, b.h);
  const im = new Image();
  await new Promise(done => { im.onload = im.onerror = done; im.src = '/assets/sprites/' + (e.shiny ? 'shiny/' : '') + id + '.png'; });
  const W = Math.round(b.w * k), H = Math.round(b.h * k);
  const tmp = document.createElement('canvas'); tmp.width = W; tmp.height = H;
  const tg = tmp.getContext('2d'); tg.imageSmoothingEnabled = false; tg.drawImage(im, b.x, b.y, b.w, b.h, 0, 0, W, H);
  const sil = document.createElement('canvas'); sil.width = W; sil.height = H;
  const sg = sil.getContext('2d'); sg.drawImage(tmp, 0, 0); sg.globalCompositeOperation = 'source-in'; sg.fillStyle = e.shiny ? '#fff3b0' : '#fff'; sg.fillRect(0, 0, W, H);
  const off = document.createElement('canvas'); off.width = W + PEEL_PAD * 2; off.height = H + PEEL_PAD * 2;
  const og = off.getContext('2d');
  for (let a = 0; a < 24; a++) og.drawImage(sil, PEEL_PAD + Math.cos(a / 24 * 6.283) * 3, PEEL_PAD + Math.sin(a / 24 * 6.283) * 3);
  og.drawImage(tmp, PEEL_PAD, PEEL_PAD);
  return off;
}

// Stuck part: flat. Free part: every row above the line is lifted along a curve (the higher up, the more it leans toward you,
// so the picture is squashed), shaded, with the blank back showing and a soft shadow on the page.
function paintPeel() {
  const cv = document.querySelector('.peelc');
  if (!cv || !dexPeel || !dexPeel.bmp) return;
  const g = cv.getContext('2d'), off = dexPeel.bmp, sw = off.width, sh = off.height, ox = PEEL_OX, oy = PEEL_OY, yp = sh * (1 - dexPeel.p);
  g.clearRect(0, 0, cv.width, cv.height);
  if (yp < sh) g.drawImage(off, 0, yp, sw, sh - yp, ox, oy + yp, sw, sh - yp);
  if (yp <= 0) return;
  const lift = Math.min(1, yp / 50), R = 34, maxAng = 1.35;
  const lay = document.createElement('canvas'); lay.width = sw; lay.height = sh + 60;
  const lg = lay.getContext('2d');
  let rise = 0;
  for (let d = 0; d < yp; d++) {
    rise += Math.cos(Math.min(1, d / R) * maxAng * lift);
    lg.drawImage(off, 0, yp - d - 1, sw, 1, 0, yp - rise, sw, 1.3);
  }
  lg.globalCompositeOperation = 'source-atop'; lg.fillStyle = 'rgba(236,239,243,.9)'; lg.fillRect(0, 0, sw, sh + 60);  // the blank back
  g.save(); g.shadowColor = 'rgba(0,0,0,.6)'; g.shadowBlur = 16 * lift; g.shadowOffsetY = 12 * lift;
  g.drawImage(lay, ox, oy); g.restore();
  if (dexPeel.p > 0.02) {  // shine where it is pressed
    const gr = g.createLinearGradient(0, oy + yp - 6, 0, oy + yp + 10);
    gr.addColorStop(0, 'rgba(255,255,255,0)'); gr.addColorStop(.5, 'rgba(255,255,255,.35)'); gr.addColorStop(1, 'rgba(255,255,255,0)');
    g.save(); g.globalCompositeOperation = 'source-atop'; g.fillStyle = gr; g.fillRect(ox, oy + yp - 6, sw, 16); g.restore();
  }
}

// Drag a sticker (touch and mouse): pointer events, because the browser's own drag-and-drop does not work on phones.
// Without moving it is a tap: it gets selected and the album jumps to its region.
function stickerDown(ev, formId) {
  const el = ev.currentTarget, x0 = ev.clientX, y0 = ev.clientY;
  let ghost = null;
  el.setPointerCapture(ev.pointerId);
  const move = e => {
    if (!ghost && Math.hypot(e.clientX - x0, e.clientY - y0) > 8) {
      dexSelect(formId, false); sfx.lift();
      ghost = el.cloneNode(true); ghost.classList.add('ghost'); ghost.removeAttribute('onpointerdown'); document.body.appendChild(ghost);
      el.classList.add('lifted');
    }
    if (ghost) { ghost.style.left = e.clientX + 'px'; ghost.style.top = e.clientY + 'px'; }
  };
  const up = e => {
    el.removeEventListener('pointermove', move); el.removeEventListener('pointerup', up); el.removeEventListener('pointercancel', up);
    if (!ghost) { dexSelect(dexPick === formId ? 0 : formId, true); sfx.lift(); return; }
    ghost.remove(); el.classList.remove('lifted');
    const slot = e.type === 'pointerup' ? document.elementFromPoint(e.clientX, e.clientY)?.closest('.dslot') : null;
    if (slot) dexStartPeel(formId, slot.dataset.art);
  };
  el.addEventListener('pointermove', move); el.addEventListener('pointerup', up); el.addEventListener('pointercancel', up);
}

function dexSelect(formId, redrawHand) {
  dexPick = formId; dexPeel = null;
  const e = dexData.entries.find(x => x.id === formId && x.shiny === dexShiny);
  if (e && dexRegion !== 'all') dexRegion = dexRegionOfEntry(e) || dexRegion;
  renderDex(redrawHand);
}

function slotTap(artId) {
  if (dexPick && !(dexPeel && dexPeel.artId === artId)) dexStartPeel(dexPick, artId);
}

// Right slot: the sticker lies on it, still held up. Wrong slot: it goes back to the hand.
async function dexStartPeel(formId, artId) {
  const e = dexData.entries.find(x => x.id === formId && x.shiny === dexShiny);
  const msg = document.getElementById('dexMsg');
  if (!e || art[e.key] !== artId) { msg.textContent = t('dexWrong'); sfx.nope(); return; }
  msg.textContent = t('dexPeelHint'); sfx.lift();
  const slotEl = document.querySelector('.dslot[data-art="' + artId + '"]');
  peelSize = Math.max(80, Math.min(130, (slotEl ? slotEl.clientWidth : 150) - 28));  // the sticker must fit inside its slot
  dexPeel = { formId, artId, p: 0, shiny: dexShiny, bmp: await peelBitmap(e) };
  renderDex();
}

// Press and rub from the bottom to the top: the stuck part follows the finger (it only grows).
function peelDown(ev) {
  ev.preventDefault(); ev.stopPropagation();
  const el = ev.currentTarget;
  el.setPointerCapture(ev.pointerId);
  let lastY = ev.clientY, lastT = Date.now();
  const move = e => {
    const r = el.getBoundingClientRect(), sh = dexPeel.bmp.height;
    const p = Math.min(1, Math.max(dexPeel.p, (r.top + PEEL_OY + sh - e.clientY) / sh));
    const now = Date.now();
    if (p === dexPeel.p) { lastY = e.clientY; lastT = now; return; }
    const speed = Math.abs(e.clientY - lastY) / Math.max(1, now - lastT) / 0.6;  // ~1 = a normal swipe
    dexPeel.p = p;
    paintPeel();
    if (now - (peelDown.last || 0) > 45) { peelDown.last = now; sfx.peel(speed); }
    lastY = e.clientY; lastT = now;
    if (p >= 0.97) { stop(); dexPeel.p = 1; paintPeel(); dexFinishPeel(); }
  };
  const stop = () => { el.removeEventListener('pointermove', move); el.removeEventListener('pointerup', stop); el.removeEventListener('pointercancel', stop); };
  el.addEventListener('pointermove', move); el.addEventListener('pointerup', stop); el.addEventListener('pointercancel', stop);
  move(ev);
}

async function dexFinishPeel() {
  const { formId, artId, shiny } = dexPeel, e = dexData.entries.find(x => x.id === formId && x.shiny === shiny);
  const c = auth();
  const r = await fetch('/api/dex/paste', { method: 'POST', body: new URLSearchParams({ code: c.code, token: c.token, formId, shiny: shiny ? 1 : 0 }) });
  document.getElementById('dexMsg').textContent = r.ok ? t('dexStuck', e.label) : '';
  if (r.ok) sfx.stick(shiny);
  dexPick = 0; dexPeel = null;
  await loadDex();
  document.querySelector('.dslot[data-art="' + artId + '"]')?.classList.add('just');
}

//=========STICKER PACK=========
// Every first capture goes into ONE pack. The player tears it open (swipe across the top, left to right),
// the stickers pop out one by one and land in his hand.

function packOpenUi() {
  const items = dexPackItems();
  if (!items.length) return;
  const ov = document.getElementById('packOv');
  ov.hidden = false;
  ov.innerHTML = '<button type="button" class="chipbtn packx" onclick="packClose()">\u2715</button>' +
    '<div class="packwrap"><div class="cap" id="cap" onpointerdown="packDown(event)" oncontextmenu="return false">' +
    '<div class="caph captop"><i></i></div><div class="caph capbot"><i></i></div><div class="capband"></div><div class="capbtn"></div>' +
    '<b class="capcount">' + items.length + '</b></div>' +
    '<p class="packhint">' + t('packTear') + '</p></div>';
}

function packClose() { document.getElementById('packOv').hidden = true; loadDex(); }

// Hold the ball: it shakes harder and glows until it bursts open
function packDown(ev) {
  ev.preventDefault();
  const el = ev.currentTarget, t0 = performance.now();
  let raf = 0, done = false;
  el.setPointerCapture(ev.pointerId);
  const stop = () => { if (done) return; cancelAnimationFrame(raf); el.classList.remove('shake'); el.style.removeProperty('--p'); el.removeEventListener('pointerup', stop); el.removeEventListener('pointercancel', stop); };
  const tick = () => {
    const p = Math.min(1, (performance.now() - t0) / 1100);
    el.classList.add('shake');
    el.style.setProperty('--p', p);
    el.style.animationDuration = (0.14 - p * 0.09) + 's';
    if (Math.random() < 0.12 + p * 0.25) sfx.rattle();
    if (p >= 1) { done = true; el.classList.remove('shake'); packRip(el); return; }
    raf = requestAnimationFrame(tick);
  };
  el.addEventListener('pointerup', stop); el.addEventListener('pointercancel', stop);
  raf = requestAnimationFrame(tick);
}

async function packRip(cap) {
  const items = dexPackItems();
  sfx.tearEnd();
  cap.classList.add('open');
  const c = auth();
  await fetch('/api/dex/open', { method: 'POST', body: new URLSearchParams({ code: c.code, token: c.token }) });
  await new Promise(r => setTimeout(r, 450));
  const ov = document.getElementById('packOv');
  ov.innerHTML = '<div class="packreveal">' + items.map((e, i) =>
    '<div class="rv" style="animation-delay:' + Math.min(i, 14) * 0.28 + 's">' + fit(e.key, e.shiny, 96, 'die' + (e.shiny ? ' gold' : '')) +
    '<span>' + esc(e.label) + (e.shiny ? ' \u2605' : '') + '</span></div>').join('') + '</div>' +
    '<button type="button" class="primary" onclick="packClose()">' + t('packKeep') + '</button>';
  items.forEach((e, i) => setTimeout(() => sfx.reveal(e.shiny), Math.min(i, 14) * 280 + 60));
}
