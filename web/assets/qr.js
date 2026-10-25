// Tiny QR code generator (byte mode, error correction M, versions 1-10 = up to 213 bytes). No library, works offline.
// QR.matrix(text) -> array of rows of booleans (true = dark), or null when the text is too long.
// QR.draw(canvas, text, pixelsPerModule) paints it with the 4-module quiet zone. Checked by decoding the output in tests.
const QR = (() => {
  // GF(256) arithmetic for the Reed-Solomon error correction
  const EXP = new Array(512), LOG = new Array(256);
  { let x = 1; for (let i = 0; i < 255; i++) { EXP[i] = x; LOG[x] = i; x <<= 1; if (x & 256) x ^= 0x11d; } for (let i = 255; i < 512; i++) EXP[i] = EXP[i - 255]; }
  const mul = (a, b) => a && b ? EXP[LOG[a] + LOG[b]] : 0;
  const generator = n => { let g = [1]; for (let i = 0; i < n; i++) { const nx = new Array(g.length + 1).fill(0); g.forEach((c, j) => { nx[j] ^= c; nx[j + 1] ^= mul(c, EXP[i]); }); g = nx; } return g; };
  const ecBytes = (data, n) => { const g = generator(n), r = new Array(n).fill(0); for (const d of data) { const f = d ^ r.shift(); r.push(0); if (f) for (let i = 0; i < n; i++) r[i] ^= mul(g[i + 1], f); } return r; };

  // per version (1..10), level M: error-correction bytes per block, and [number of blocks, data bytes per block] groups
  const SPEC = [null, { e: 10, b: [[1, 16]] }, { e: 16, b: [[1, 28]] }, { e: 26, b: [[1, 44]] }, { e: 18, b: [[2, 32]] }, { e: 24, b: [[2, 43]] },
    { e: 16, b: [[4, 27]] }, { e: 18, b: [[4, 31]] }, { e: 22, b: [[2, 38], [2, 39]] }, { e: 22, b: [[3, 36], [2, 37]] }, { e: 26, b: [[4, 43], [1, 44]] }];
  const ALIGN = [null, [], [6, 18], [6, 22], [6, 26], [6, 30], [6, 34], [6, 22, 38], [6, 24, 42], [6, 26, 46], [6, 28, 50]];
  const dataBytes = v => SPEC[v].b.reduce((n, [c, d]) => n + c * d, 0);

  function encode(text) {
    const bytes = Array.from(new TextEncoder().encode(text));
    let v = 1;
    const need = ver => 4 + (ver < 10 ? 8 : 16) + bytes.length * 8;
    while (v <= 10 && need(v) > dataBytes(v) * 8) v++;
    if (v > 10) return null;
    const bits = [];
    const put = (val, len) => { for (let i = len - 1; i >= 0; i--) bits.push((val >>> i) & 1); };
    put(4, 4); put(bytes.length, v < 10 ? 8 : 16); bytes.forEach(b => put(b, 8));
    const cap = dataBytes(v) * 8;
    put(0, Math.min(4, cap - bits.length));
    while (bits.length % 8) bits.push(0);
    for (let pad = 0xec; bits.length < cap; pad ^= 0xec ^ 0x11) put(pad, 8);
    const data = []; for (let i = 0; i < bits.length; i += 8) data.push(parseInt(bits.slice(i, i + 8).join(''), 2));
    // split in blocks, add error correction, interleave
    const blocks = []; let pos = 0;
    for (const [count, size] of SPEC[v].b) for (let k = 0; k < count; k++) { const d = data.slice(pos, pos + size); pos += size; blocks.push({ d, e: ecBytes(d, SPEC[v].e) }); }
    const out = [];
    for (let i = 0; i < Math.max(...blocks.map(b => b.d.length)); i++) blocks.forEach(b => { if (i < b.d.length) out.push(b.d[i]); });
    for (let i = 0; i < SPEC[v].e; i++) blocks.forEach(b => out.push(b.e[i]));
    return { v, out };
  }

  function matrix(text) {
    const enc = encode(text);
    if (!enc) return null;
    const { v, out } = enc, size = 17 + 4 * v;
    const mod = Array.from({ length: size }, () => new Array(size).fill(false));
    const fn = Array.from({ length: size }, () => new Array(size).fill(false));
    const set = (x, y, dark) => { mod[y][x] = dark; fn[y][x] = true; };  // function module (not data)
    const bit = (n, i) => ((n >>> i) & 1) === 1;

    for (let i = 0; i < size; i++) { set(6, i, i % 2 === 0); set(i, 6, i % 2 === 0); }  // timing
    for (const [cx, cy] of [[3, 3], [size - 4, 3], [3, size - 4]])                      // finders
      for (let dy = -4; dy <= 4; dy++) for (let dx = -4; dx <= 4; dx++) {
        const x = cx + dx, y = cy + dy, d = Math.max(Math.abs(dx), Math.abs(dy));
        if (x >= 0 && x < size && y >= 0 && y < size) set(x, y, d !== 2 && d !== 4);
      }
    const al = ALIGN[v];                                                                 // alignment
    al.forEach((ax, i) => al.forEach((ay, j) => {
      if ((i === 0 && j === 0) || (i === 0 && j === al.length - 1) || (i === al.length - 1 && j === 0)) return;
      for (let dy = -2; dy <= 2; dy++) for (let dx = -2; dx <= 2; dx++) set(ax + dx, ay + dy, Math.max(Math.abs(dx), Math.abs(dy)) !== 1);
    }));
    const format = mask => {
      const data = mask;  // level M = 00
      let rem = data; for (let i = 0; i < 10; i++) rem = (rem << 1) ^ ((rem >>> 9) * 0x537);
      const bits = (data << 10 | rem) ^ 0x5412;
      for (let i = 0; i <= 5; i++) set(8, i, bit(bits, i));
      set(8, 7, bit(bits, 6)); set(8, 8, bit(bits, 7)); set(7, 8, bit(bits, 8));
      for (let i = 9; i < 15; i++) set(14 - i, 8, bit(bits, i));
      for (let i = 0; i < 8; i++) set(size - 1 - i, 8, bit(bits, i));
      for (let i = 8; i < 15; i++) set(8, size - 15 + i, bit(bits, i));
      set(8, size - 8, true);  // always dark
    };
    format(0);
    if (v >= 7) {
      let rem = v; for (let i = 0; i < 12; i++) rem = (rem << 1) ^ ((rem >>> 11) * 0x1f25);
      const bits = v << 12 | rem;
      for (let i = 0; i < 18; i++) { const a = size - 11 + i % 3, b = Math.floor(i / 3); set(a, b, bit(bits, i)); set(b, a, bit(bits, i)); }
    }
    // data modules, zigzag from the bottom right
    let k = 0;
    for (let right = size - 1; right >= 1; right -= 2) {
      if (right === 6) right = 5;
      for (let vert = 0; vert < size; vert++) for (let j = 0; j < 2; j++) {
        const x = right - j, y = ((right + 1) & 2) === 0 ? size - 1 - vert : vert;
        if (!fn[y][x] && k < out.length * 8) { mod[y][x] = bit(out[k >>> 3], 7 - (k & 7)); k++; }
      }
    }
    // try the 8 masks, keep the one with the lowest penalty
    const MASKS = [(x, y) => (x + y) % 2 === 0, (x, y) => y % 2 === 0, (x, y) => x % 3 === 0, (x, y) => (x + y) % 3 === 0,
      (x, y) => (Math.floor(x / 3) + Math.floor(y / 2)) % 2 === 0, (x, y) => x * y % 2 + x * y % 3 === 0,
      (x, y) => (x * y % 2 + x * y % 3) % 2 === 0, (x, y) => ((x + y) % 2 + x * y % 3) % 2 === 0];
    const apply = m => { for (let y = 0; y < size; y++) for (let x = 0; x < size; x++) if (!fn[y][x] && MASKS[m](x, y)) mod[y][x] = !mod[y][x]; };
    const penalty = () => {
      let p = 0;
      const lines = [];
      for (let a = 0; a < size; a++) { lines.push(mod[a].map(b => b ? '1' : '0').join('')); lines.push(mod.map(r => r[a] ? '1' : '0').join('')); }
      for (const s of lines) {
        for (const run of s.match(/0+|1+/g)) if (run.length >= 5) p += 3 + run.length - 5;
        p += 40 * ((s.match(/(?=00001011101)/g) || []).length + (s.match(/(?=10111010000)/g) || []).length);
      }
      for (let y = 0; y < size - 1; y++) for (let x = 0; x < size - 1; x++) if (mod[y][x] === mod[y][x + 1] && mod[y][x] === mod[y + 1][x] && mod[y][x] === mod[y + 1][x + 1]) p += 3;
      const dark = mod.flat().filter(Boolean).length, total = size * size;
      return p + 10 * (Math.ceil(Math.abs(dark * 20 - total * 10) / total) - 1);
    };
    let best = 0, bestP = Infinity;
    for (let m = 0; m < 8; m++) { apply(m); format(m); const p = penalty(); if (p < bestP) { bestP = p; best = m; } apply(m); }
    apply(best); format(best);
    return mod;
  }

  function draw(canvas, text, px = 6) {
    const m = matrix(text);
    if (!m) return false;
    const n = m.length + 8;
    canvas.width = canvas.height = n * px;
    const g = canvas.getContext('2d');
    g.fillStyle = '#fff'; g.fillRect(0, 0, n * px, n * px);
    g.fillStyle = '#000';
    m.forEach((row, y) => row.forEach((d, x) => { if (d) g.fillRect((x + 4) * px, (y + 4) * px, px, px); }));
    return true;
  }
  return { matrix, draw };
})();
