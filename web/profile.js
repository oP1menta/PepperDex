// Player profile (button in the top bar), item sheet (use on a Pokemon / hold / throw on the table).
// Rules live in the server (PokemonServices.applyItem); here we only decide which buttons to show.
Object.assign(TEXT.en, {
  profile: 'Profile', profDesc: 'Description', profDescPh: 'Who is your character? Look, history, personality...', profSave: 'Save',
  profMoney: 'Money', profBag: 'Inventory', profEmpty: 'Empty', itUse: 'Use', itHold: 'Give to hold', itToss: 'Throw on the table', itQty: 'How many?',
  itPick: 'On which Pokémon?', itNone: 'No Pokémon can use this now.', itClose: 'Close', itHeld: 'Holding', itTake: 'Take back', itNoUse: 'This one has no use here: the Master decides.',
  itChoose: 'Evolve into...', itDone: 'Done: {0}', itTossed: 'Thrown on the table.', fainted: 'Fainted',
});
Object.assign(TEXT.pt, {
  profile: 'Perfil', profDesc: 'Descrição', profDescPh: 'Quem é seu personagem? Aparência, história, personalidade...', profSave: 'Salvar',
  profMoney: 'Dinheiro', profBag: 'Inventário', profEmpty: 'Vazio', itUse: 'Usar', itHold: 'Dar para segurar', itToss: 'Jogar na mesa', itQty: 'Quantos?',
  itPick: 'Em qual Pokémon?', itNone: 'Nenhum Pokémon pode usar isso agora.', itClose: 'Fechar', itHeld: 'Segurando', itTake: 'Pegar de volta', itNoUse: 'Este não tem uso aqui: o Mestre decide.',
  itChoose: 'Evoluir para...', itDone: 'Feito: {0}', itTossed: 'Jogado na mesa.', fainted: 'Desmaiado',
});

const HEAL = ['Potion', 'Super Potion', 'Hyper Potion', 'Max Potion', 'Full Restore', 'Energy Powder'], REVIVE = ['Revive', 'Max Revive'];
const itemUsable = i => HEAL.includes(i.name) || REVIVE.includes(i.name) || i.cat === 'Vitamin' || (i.cat === 'Stone' && i.name !== 'Everstone');
const itemHoldable = i => i.cat === 'Held' || i.cat === 'Berry' || i.name === 'Everstone';
const itemIco = (i, n) => i.skin ? ballImg(i.skin, n) : '<span class="itico" style="font-size:' + Math.round(n * .8) + 'px">' + esc(i.icon || '📦') + '</span>';
const itTrainer = () => pcData.find(x => x.id === pcTrainerId);
const stoneRows = (p, i) => ((p.evo && p.evo.to) || []).filter(r => r[1].replace(/\s/g, '').toLowerCase().includes(i.name.replace(/\s/g, '').toLowerCase()));
const hpMax = p => p.maxHp || 1;

//=========PROFILE=========
function openProfile() {
  if (!pcData.length) { out.textContent = t('capNoChar'); return; }
  let o = document.getElementById('profDlg');
  if (!o) {
    o = document.createElement('div'); o.id = 'profDlg'; o.className = 'ovl';
    o.onclick = e => { if (e.target === o) closeOvl('profDlg'); };
    document.body.appendChild(o);
  }
  drawProfile();
}
function closeOvl(id) { const o = document.getElementById(id); if (o) o.remove(); }

function drawProfile() {
  const o = document.getElementById('profDlg'); if (!o) return;
  if (document.activeElement && document.activeElement.id === 'profBio') return;   // don't wipe what he is typing
  const tr = itTrainer(); if (!tr) { closeOvl('profDlg'); return; }
  const money = (tr.items.find(i => i.cat === 'Currency') || { qty: 0 }).qty;
  const bag = tr.items.filter(i => i.cat !== 'Currency');
  const keep = o.firstElementChild ? o.firstElementChild.scrollTop : 0;
  o.innerHTML = '<div class="st"><div class="dlg-top"><span class="leds"><i></i><i></i><i></i></span><button class="st-x" onclick="closeOvl(\'profDlg\')">&times;</button></div><div class="st-screen">' +
    (pcData.length > 1 ? '<select onchange="pcPickTrainer(this.value); drawProfile()">' + pcData.map(x => '<option value="' + x.id + '"' + (x.id === tr.id ? ' selected' : '') + '>' + esc(x.name) + '</option>').join('') + '</select>' : '') +
    '<h3 class="profname">' + esc(tr.name) + '</h3>' +
    '<div class="st-line"><b>' + t('profMoney') + '</b><span>🪙 ' + money.toLocaleString() + '</span></div>' +
    profileHtml(tr, false) +
    '<div class="st-block"><div class="st-line"><b>' + t('profDesc') + '</b></div><textarea id="profBio" maxlength="600" rows="4" placeholder="' + esc(t('profDescPh')) + '">' + esc(tr.profile.bio || '') + '</textarea>' +
    '<button onclick="profSaveBio(' + tr.id + ')">' + t('profSave') + '</button></div>' +
    '<div class="st-block"><div class="st-line"><b>' + t('profBag') + '</b></div><div class="bag-grid profbag">' +
    (bag.length ? bag.map(i => itemSlot(tr.id, i)).join('') : '<small class="muted">' + t('profEmpty') + '</small>') + '</div></div>' +
    '</div></div>';
  o.firstElementChild.scrollTop = keep;
}
async function profSaveBio(id) { await profSend(id, { bio: document.getElementById('profBio').value }); document.getElementById('profBio').blur(); drawProfile(); }

const itemSlot = (trId, i) => '<button class="slot" title="' + esc(i.name) + '" onclick="openItem(' + trId + ',' + i.id + ')">' + itemIco(i, 44) + '<span class="nm">' + esc(i.name) + '</span><b class="qty">x' + i.qty + '</b></button>';

// keep it fresh when the server data changes
const _pcSetData = pcSetData;
pcSetData = function (list) { _pcSetData(list); drawProfile(); if (document.getElementById('itDlg')) drawItem(); };

//=========ITEM SHEET=========
let itSel = null, itMsg = '', itMode = '';   // itMode: '' (sheet) | 'use' | 'hold' | 'evo:<pokemonId>'
function openItem(trId, itemId) {
  pcTrainerId = trId; itSel = itemId; itMsg = ''; itMode = '';
  let o = document.getElementById('itDlg');
  if (!o) { o = document.createElement('div'); o.id = 'itDlg'; o.className = 'ovl top'; o.onclick = e => { if (e.target === o) closeOvl('itDlg'); }; document.body.appendChild(o); }
  drawItem();
}

function drawItem() {
  const o = document.getElementById('itDlg'); if (!o) return;
  const tr = itTrainer(), i = tr && tr.items.find(x => x.id === itSel);
  if (!i) { closeOvl('itDlg'); return; }
  let body = '';
  if (itMode === 'use' || itMode === 'hold') body = pickBody(tr, i);
  else if (itMode.startsWith('evo:')) body = evoBody(tr, i, +itMode.slice(4));
  else {
    body = '<p class="itinfo">' + esc(i.info || '') + '</p><div class="itbtns">' +
      (itemUsable(i) ? '<button class="go" onclick="itMode=\'use\';drawItem()">' + t('itUse') + '</button>' : '') +
      (itemHoldable(i) ? '<button class="go" onclick="itMode=\'hold\';drawItem()">' + t('itHold') + '</button>' : '') +
      (!itemUsable(i) && !itemHoldable(i) && i.cat !== 'Ball' ? '<small class="muted">' + t('itNoUse') + '</small>' : '') + '</div>' +
      '<div class="st-block"><div class="st-line"><b>' + t('itToss') + '</b></div><div class="itbtns"><label>' + t('itQty') + ' <input id="itQty" type="number" min="1" max="' + i.qty + '" value="1"></label>' +
      '<button onclick="itToss()">' + t('itToss') + '</button></div></div>';
  }
  o.innerHTML = '<div class="st"><div class="dlg-top"><span class="leds"><i></i><i></i><i></i></span><button class="st-x" onclick="closeOvl(\'itDlg\')">&times;</button></div><div class="st-screen">' +
    '<div class="st-head">' + itemIco(i, 56) + '<div><h3>' + esc(i.name) + '</h3><small>x' + i.qty + '</small></div></div>' +
    (itMode ? '<button class="itback" onclick="itMode=\'\';itMsg=\'\';drawItem()">&lsaquo; ' + esc(i.name) + '</button>' : '') + body +
    (itMsg ? '<p class="itmsg">' + esc(itMsg) + '</p>' : '') + '</div></div>';
}

function pickBody(tr, i) {
  const hold = itMode === 'hold';
  const ok = p => hold || (HEAL.includes(i.name) ? p.hp > 0 && p.hp < hpMax(p) : REVIVE.includes(i.name) ? p.hp <= 0 : i.cat === 'Stone' ? stoneRows(p, i).length > 0 : true);
  const list = tr.team.filter(ok);
  return '<div class="st-line"><b>' + t('itPick') + '</b></div>' + (list.length ? '<div class="itpick">' + list.map(p =>
    '<button onclick="itGo(' + p.id + ')">' + sprite(keyOf(p), 'small', p.shiny) + '<b>' + esc(displayName(p)) + '</b><small>' + t('lv') + p.level + ' &middot; ' +
    (p.hp <= 0 ? t('fainted') : 'HP ' + Math.round(p.hp) + '/' + p.maxHp) + (p.held ? ' &middot; ' + esc(p.held.icon) + ' ' + esc(p.held.name) : '') + '</small></button>').join('') + '</div>'
    : '<p class="muted">' + t('itNone') + '</p>');
}

function evoBody(tr, i, pid) {
  const p = tr.team.find(x => x.id === pid); if (!p) return '';
  return '<div class="st-line"><b>' + t('itChoose') + '</b></div><div class="itpick">' + stoneRows(p, i).map(r =>
    '<button onclick="itSend(' + pid + ',\'' + esc(r[0]) + '\')">' + sprite(r[0] + '|Normal', 'small') + '<b>' + esc(r[0]) + '</b></button>').join('') + '</div>';
}

function itGo(pid) {
  const tr = itTrainer(), i = tr.items.find(x => x.id === itSel);
  if (itMode === 'hold') return itPost('/api/item/hold', { pokemonId: pid, itemId: i.id });
  if (i.cat === 'Stone') {
    const p = tr.team.find(x => x.id === pid), rows = stoneRows(p, i);
    if (rows.length > 1) { itMode = 'evo:' + pid; drawItem(); return; }
    return itSend(pid, rows[0][0]);
  }
  itSend(pid, '');
}
const itSend = (pid, arg) => itPost('/api/item/use', { pokemonId: pid, itemId: itSel, arg });

async function itPost(path, extra) {
  const r = await fetch(path, { method: 'POST', body: new URLSearchParams({ ...auth(), trainerId: pcTrainerId, ...extra }) });
  const j = await r.json().catch(() => ({}));
  if (!r.ok) { itMsg = errText(j.error); drawItem(); return; }
  itMsg = j.msg ? t('itDone', j.msg) : ''; itMode = '';
  await loadTrainers();
  if (!itTrainer().items.some(x => x.id === itSel)) { closeOvl('itDlg'); out.textContent = itMsg; } else drawItem();
}

async function itToss() {
  const q = +document.getElementById('itQty').value || 1;
  const r = await fetch('/api/table/toss', { method: 'POST', body: new URLSearchParams({ ...auth(), trainerId: pcTrainerId, itemId: itSel, qty: q }) });
  if (!r.ok) { itMsg = errText((await r.json()).error); drawItem(); return; }
  itMsg = t('itTossed'); await loadTrainers();
  if (!itTrainer().items.some(x => x.id === itSel)) closeOvl('itDlg'); else drawItem();
}

// take back what a Pokemon holds (button on its status screen)
async function heldTake(pid) { itSel = null; await itPost('/api/item/hold', { pokemonId: pid, itemId: 0 }); drawStatus(); }
