// Language (en | pt) and Pokedex color. Both are per device (localStorage) and shared by player + master pages.
// Static texts: data-i (text), data-ip (placeholder). Texts built by JS use t('key', args...) with {0} {1} slots.
// Messages that come from the server (error replies) stay in English.

const TEXT = {
  en: {
    pcHint: 'Hold over the arrows to change box', nickname: 'Nickname', save: 'Save',
    pickBall: 'Choose a ball', capWho: 'Who throws?', noBalls: 'No balls in the bag. Ask the Master.', gotcha: 'Gotcha! {0} was caught!', brokeFree: '{0} broke free!', capRoll: 'Rolled {0}, needed {1} or less', capNat100: 'Only a natural 100 catches this one', capGone: 'Too late: that Pokémon is gone.', capNoRoom: 'No room: party and PC are full. Add a PC box.', capNoChar: 'Create a character first.', bagEmpty: 'Empty', giveItems: 'Give balls', ballType: 'Ball', giveItemBtn: 'Give balls to the character above', gaveItem: 'Gave {0} x{1} to {2} (now has {3})', cancel: 'Cancel', fRegion: 'Region', fType: 'Type', fStageAll: 'All stages', fStageBase: 'Not evolved', fStageEvolved: 'Evolved', fSortAz: 'Sort: A-Z', fSortDex: 'Sort: Pokédex number', fClear: 'Clear filters', fCount: '{0} Pokémon', fMore: 'Show {0} more', ty_Normal: 'Normal', ty_Fire: 'Fire', ty_Water: 'Water', ty_Electric: 'Electric', ty_Grass: 'Grass', ty_Ice: 'Ice', ty_Fighting: 'Fighting', ty_Poison: 'Poison', ty_Ground: 'Ground', ty_Flying: 'Flying', ty_Psychic: 'Psychic', ty_Bug: 'Bug', ty_Rock: 'Rock', ty_Ghost: 'Ghost', ty_Dragon: 'Dragon', ty_Dark: 'Dark', ty_Steel: 'Steel', ty_Fairy: 'Fairy',  inviteHead: 'Invite players', inviteOff: 'Players on other networks need the internet link.', inviteStart: 'Connect to the internet', inviteDownload: 'Downloading the tunnel tool (first time only)...', inviteStarting: 'Connecting to the internet...', inviteLink: 'Link', inviteCode: 'Session code', inviteNoCode: 'Create a session to get the code', inviteCopy: 'Copy invite', inviteCopied: 'Copied!', inviteMsg: 'Join {0} and use the code {1}', inviteRetry: 'Try again', inviteLan: 'Same Wi-Fi: {0}',  sfx: 'Effect sounds (capture, dice)', sfxVolume: 'Effects volume', holoSound: 'Hologram sound', sfxTest: 'Test effects', rcTitle: 'Capture rate', rcBase: 'Base', rcLevel: 'Level {0}', rcHp: 'HP {0}', rcEvo: 'Evolutions left: {0}', rcShiny: 'Shiny', rcInjury: 'Injuries x{0}', rcBonus: 'Master bonus', rcRoll: 'Roll', rcTotal: 'Roll + ball', rcNeed: 'Needs {0} or less', rcNat100: 'Natural 100!', rcSkip: 'tap to speed up', rcContinue: 'tap to continue', rcTries: 'Balls left: {0}', fled: '{0} ran away!', tries: 'Balls allowed', triesHint: '0 = unlimited. Shared by every player: when they run out the Pokémon runs away.', wildTries: 'Balls', fx_burn: 'Burn', fx_freeze: 'Freeze', fx_paralysis: 'Paralysis', fx_poison: 'Poison', fx_sleep: 'Sleep', fx_confusion: 'Confusion', fx_stuck: 'Stuck', fx_slow: 'Slowed', wildHp: 'HP', wildFx: 'Effects', wildInj: 'Injuries', wildBonus: 'Bonus', wildRate: 'Rate', wildFull: 'Full', wildOne: '1 HP', 
    pcFilter: 'Filter', pcParty: 'Party', pcBoxList: 'Box List', pcNewBox: '+ New box', pcBox: 'Box', pcNature: 'Nature', pcAbility: 'Ability', pcMove: 'Move', pcCancel: 'Cancel', pcPick: 'Select a Pokémon', pcHold: 'Now tap a slot to place it', pcEmpty: 'Empty', pcNoChars: 'No characters in this session yet.', pcShiny: 'Shiny', pcTrash: 'Release', stXpTotal: 'XP total {0}', stPageStatus: 'Status', stPageEvo: 'Evolution', evoFrom: 'Evolves from', evoTo: 'Evolves into', evoNone: 'No known evolutions for this Pokémon.', evoLevel: 'Level {0}', evoReady: 'Ready to evolve!', evoLeft: '{0} more level(s) to go', evoBtn: 'Evolve', evoAsk: 'Ask the Master', evoAsked: 'Request sent', evoAskText: 'wants to evolve into {0}', evoMasterOnly: 'The Master decides this one (item, move, time of day...).', evoNoReq: 'No requirement in the book: ask the Master', evoConfirm: 'Evolve {0} into {1}? This cannot be undone.', evoDone: '{0} evolved into {1}!', evoBack: 'The new base stats broke the Base Relation, so your spent points were given back to spend again.', evoNote: 'Nature, level, XP and damage are kept.', edEvolve: 'Evolve into', edEvolveBtn: 'Evolve now', edEvolveAsk: 'Evolve {0} into {1} now?', edEvolveNote: 'Ignores the requirement. Gender and ability are adjusted to the new species; spent points are only given back if the Base Relation breaks.', stFreeTag: 'free', stFreeTitle: 'Ignores the Base Relation rule (allowed by the Master)', edFree: 'Ignore the Base Relation rule in:', edFreeNote: 'For Features / Edges of the book that break the rule. Only the checked stats can go past the others.', edEdit: 'Edit', edSave: 'Save changes', edNote: 'Changing the nature gives every spent point back (they are spent again by the player). Level, ability and gender follow the rules for this Pokémon.', edPoints: 'Stat points', edReset: 'Reset spent points', edResetAsk: 'Give back all points spent by {0}?', edDone: 'Done.', reqBtn: 'Ask the Master to adjust', reqSent: 'Request sent', reqHint: 'To reset points or change the nature.', reqPrompt: 'What do you need? (optional)', reqNote: '{0}: {1} needs an adjustment', stStatus: 'Status', stClose: 'Close', stMaxAfter: 'max {0} after applying', stFull: 'Full', stXpMax: 'max level', stXpLeft: '{0} XP to level {1}', stPoints: 'Points to spend', stBase: 'Base', stNat: 'Nat.', stSpent: 'Spent', stTotal: 'Total', stApply: 'Apply', stClear: 'Clear', stat0: 'HP', stat1: 'Attack', stat2: 'Defense', stat3: 'Sp. Atk', stat4: 'Sp. Def', stat5: 'Speed', giveXp: 'Give XP', setHp: 'Set HP', xpGave: 'XP given.', xpLevelUp: 'Level up! {0} → {1}', confirmOk: 'Confirm', pcTrashAsk: 'Release {0} for good?', pcRename: 'Click to rename',
    areas: 'Areas', newArea: '+ New area', areaName: 'Name', minLv: 'Lowest level', maxLv: 'Highest level', delAreaAsk: 'Delete the area {0}?', areaForms: '{0} Pokémon', areaPool: 'Pokémon of this area', addForm: 'Add to area', weight: 'Weight (higher = more common)', areaEmpty: 'No Pokémon yet. Add some below.', noAreas: 'No areas yet. Create one in the Areas tab.', areaPick: 'Area', pickArea: 'Pick an area first.',
    rjAsk: 'There is already a player called {0} in this session. Is it you?', rjBtn: 'Yes, it is me — ask the Master', rjWait: 'Waiting for the Master to approve…', rjCancel: 'Cancel', rjDenied: 'The Master said no.', rjGone: 'The request expired. Try again.', rjMaster: '{0} wants to come back to session {1}', rjOnline: 'seems online now', rjYes: 'Approve', rjNo: 'Deny',
    stepWhich: 'Which Pokémon?', stepDetails: 'Details (optional)', stepSend: 'Send it', stepGive: 'Give directly', advanced: 'Advanced options', fFilters: 'Filters',
    srcPick: 'Choose a Pokémon', srcArea: 'Roll an area', sendHeading: 'Send', catchRate: 'Chance to catch', catchNormal: 'Normal roll', catchNormalShort: 'Normal', catchHint: '100% always catches, 0% never does. Players do not see this.', onField: 'On the field', fieldEmpty: 'Nothing on the field right now.', qty: 'Quantity', sessionMeta: 'day {0} · {1} player(s)',
    appearance: 'Appearance', modeDefault: 'Default', modeLight: 'Light', modeDark: 'Dark', modeAuto: 'Auto', frameColor: 'Pokédex color', resetColor: 'Reset color', brightness: 'Brightness', menuPlace: 'Position', soundHead: 'Sound',
    settings: 'Settings', sound: 'Cry sounds', volume: 'Cry volume', tabMode: 'Menu buttons', rolling: 'Rolling (swipe / wheel)', fixed: 'Fixed (arrows)', menu: 'Menu', side: 'Side', bottom: 'Bottom', top: 'Top', language: 'Language', color: 'Pokédex color',
    campaigns: 'Campaigns', leave: 'Leave session',
    whoAreYou: 'Who are you?', yourName: 'Your name', continue: 'Continue', hi: 'Hi,', changeName: 'change name', otherDev: 'Use on another device', otherDevHelp: 'Copy this link and open it on the other device: same name, characters and campaigns. Keep it private.', copyLink: 'Copy link', linkCopied: 'Copied!', connect: 'Connect',
    codePh: 'Session code (e.g. K7P2)', lost: 'Lost access to a session?',
    claimHelp: 'Type the session code above and the reconnect code the Master gave you.', claimPh: 'Reconnect code', reconnect: 'Reconnect',
    online: 'Online', lastSeen: 'seen {0}', neverSeen: 'not seen yet', secAgo: '{0}s ago', minAgo: '{0} min ago', hourAgo: '{0} h ago', dexProgressTip: 'Pokémon caught in the Pokédex', inPc: '{0} in the PC', noParty: 'No Pokémon in the party', qGive: 'Give Pokémon / balls', qPc: 'Open PC', qSpawn: 'Spawn for him', xpHp: 'XP and HP', resetDex: 'Reset Pokédex', resetDexAsk: 'Reset the whole Pokédex of {0}? The album and stickers are erased and it cannot be undone.', clearField: 'Clear the field', clearFieldAsk: 'Send every wild Pokémon away? Players stop seeing them.', clearFieldDone: '{0} removed.', playersHelp: 'Each card is a player: who is online, their team and quick buttons. Use the buttons instead of hunting through the tabs.', wildInfo: 'Stats and capabilities', caps: 'Capabilities',
    statNames: 'HP,Attack,Defense,Sp. Atk,Sp. Def,Speed', hpLbl: 'HP', freePts: 'Free stat points: {0}', setLevel: 'Set level', mapBrushLbl: 'Brush', mapUndo: 'Undo (Ctrl+Z)', mapCenterBtn: 'Center the map', mapMe: 'Me', mapNoTokens: 'No pieces yet', mapNpcName: 'Name of the NPC:', mapTokH: 'Pieces on the map', mapTokHelp: 'Click to put a piece in the middle of the view; drag it with the Hand tool. A piece placed twice is just moved. The x removes it.', mapAddNpc: '+ NPC', mapWildH: 'Wild Pokemon on the field', mapOnMap: 'On this map', mapMineHelp: 'Tap your character or a Pokemon to put it on the map or take it off. Press and drag a piece to move it.', mapTab: 'Map', mapTabM: 'Maps', mapHelp: 'The map the Master is showing. Drag to move around; two fingers zoom.', mapHelpM: 'Build maps with blocks and show one to the players. Pick a block below and paint (press and drag). Use the Hand to move around; mouse wheel or two fingers zoom. The + creates a new map; click a map to edit it.', mapNone: 'No map is being shown', mapNew: 'New map', mapShow: 'Show to players', mapHide: 'Stop showing', mapRename: 'Rename', mapDelete: 'Delete', mapNamePrompt: 'Name of the map:', mapDeleteAsk: 'Delete the map "{0}"?', mtHand: 'Hand', mtTree: 'Forest', mvTree: 'Oak,Pine,Autumn,Palm,Snowy pine,Dead tree', mtSand: 'Sand', mtSnow: 'Snow', mtDirt: 'Dirt', mvGrass: 'Plain,Flowers,Dry,Forest', mvWater: 'Lake,Deep sea,Swamp,Lava', mvCave: 'Cave,Light rock,Volcanic', mvSand: 'Beach,Desert,Gravel', mvSnow: 'Snow,Ice', mvDirt: 'Dirt,Mud,Red clay', mvRoad: 'Dirt / wood,Stone,Brick,Asphalt', mvWall: 'Stone,Brick,Wood,Hedge,Ice', mvHouse: 'Red,Blue,Green,Cabin,Stone,Straw', mvElev: 'Level 1,Level 2,Level 3,Level 4', mtGrass: 'Grass', mtWater: 'Water', mtCave: 'Cave', mtRoad: 'Road / bridge', mtElev: 'Elevation', mtFlat: 'Lower', mtWall: 'Wall', mtHouse: 'House', mtBare: 'Clear top', mtErase: 'Erase', tableTab: 'Table', tblHelp: 'Move your hand over the table. Choose how many to take, press on an item to pick it up, move it and let go: on the table to throw it, over the chest to keep it.', tblHelpM: 'Everyone in the session sees this table. Type amounts on the list and throw the list, or press an item and drag it onto the table. each player grabs what he wants with his hand. You also have a hand: press and drag to move items.', tblTakeLbl: 'Take', tblAll: 'All', tblChest: 'Chest', tblChestHint: 'The chest is right under the table. Press an item (hold a moment on the phone) and drag it onto the table; to keep it, let go over the chest. Scroll for more.', tblNoItems: 'No items', tblNoChar: 'You have no character in this session. Add one in the Characters tab to keep items.', tblSelHint: 'Click an item to add 1 to the list (right click: -1). Drag one onto the table to throw it.', tblItemsH: 'Items', tblThrowList: 'Throw the list', tblListClear: 'Zero the amounts', tblListEmpty: 'Type an amount on at least one item first.', tblGrab: 'Grab', tblEmpty: 'Empty', tblGot: '{0} x{1} went to the chest.', tblThrow: 'Throw on the table', tblThrowBtn: 'Throw', tblClear: 'Clear the table', tblClearAsk: 'Remove everything from the table?',
    dex: 'Pokédex', dexSummary: 'Seen {0} · Caught {1} · of {2} Pokémon', dexSeen: 'Seen', dexCaught: 'Caught', dexHand: 'Stickers in hand', dexAll: 'All', wildRemove: 'Remove from the field', wildRemoveAsk: 'Remove this Pokémon from the field? Players will stop seeing it.', newSticker: 'New sticker in your pack!', packBtn: 'Open the sticker pack ({0})', packNone: 'No new stickers', packTitle: 'Sticker pack', packCount: '{0} stickers inside', packTear: 'Press and hold the ball until it bursts open.', packKeep: 'Keep them', dexHandPack: 'Open the pack above to get your stickers.', dexShiny: 'Shiny', dexPeelHint: 'Press and rub over the sticker from the bottom to the top to stick it on.', dexHandEmpty: 'No loose stickers. Your first capture of each Pokémon gives one.', dexWrong: 'That sticker does not belong in this slot.', dexStuck: '{0} stuck on the album!', dexLvl: 'Lv. {0}',
    encounters: 'Wild', characters: 'Trainers', bag: 'Bag', pc: 'PC', nothing: 'Nothing yet...', capture: 'Capture',
    newChar: 'New character name', soon: 'Coming soon.', bringHelp: 'Tick the characters to bring into the session:', myChars: 'My characters',
    myCharsHelp: 'Made here, before any session. Each session gets its own independent copy.', createChar: 'Create character', noSheets: 'No characters yet. Create one below.',
    delSheet: 'Delete this character? Copies already inside sessions stay there.', addFromMine: 'Add one of my characters', addToSession: 'Add to this session',
    manageHelp: 'Create or delete characters on the Campaigns screen.', noneLeft: 'All your characters are already here',
    create: 'Create', sessions: 'Sessions', players: 'Players', spawn: 'Spawn', activeSession: 'Active session:', newSession: 'New session',
    createSession: 'Create session', yourSessions: 'Your sessions', loading: 'Loading...', spawnTitle: 'Spawn a Pokémon', searchPh: 'Search (e.g. rotom)',
    shiny: 'Shiny', shinyRandom: 'Random (1%)', yes: 'Yes', no: 'No', random: 'Random', nature: 'Nature', ability: 'Ability', gender: 'Gender', level: 'Level', sendTo: 'Wild, send to:', everyone: 'Everyone', spawnWild: 'Spawn (wild)', giveTo: 'Or give to:', giveBtn: 'Give to character',
    typeName: 'Type your name.', welcome: '{0} (player #{1}) in session {2}', yourCampaigns: 'Your campaigns',
    confirmLeave: 'Leave session {0}? Your characters in it are deleted (your saved sheets stay).', kicked: 'The Master removed you from {0}.', deleted: 'Session {0} was deleted.',
    manyWild: '{0} wild Pokémon appeared!', wildNear: 'A wild Pokémon appeared near you!', wildAll: 'A wild Pokémon appeared!', gotPoke: 'You received a Pokémon!',
    captureTodo: 'Capture is the next step - not built yet.', noPoke: 'No Pokémon yet', noChars: 'No characters in this session yet.', del: 'Delete', delChar: 'Delete this character?',
    lv: 'Lv.', playersOpen: 'Players open:', sessionRow: 'Code {0} · Master {1} · day {2} · {3} player(s)', noSessions: 'No sessions yet.',
    delSession: 'Delete session {0}? Players are removed and it cannot be undone.', noPlayers: 'No players yet.', reconnectCode: 'Reconnect code', kick: 'Kick',
    noCharNote: '{0} - no character yet (player must add one)', claimMsg: 'Give this to the player: {0}\nSession {1} (valid for 10 minutes, works once)',
    kickConfirm: 'Kick this player?', created: 'Created! Code: {0}', pickMon: 'Pick a Pokémon first.', pickMonChar: 'Pick a Pokémon and a character first.',
    gave: 'Gave {0} Lv. {1} to {2}', spawned: 'Spawned {0} ({1}) Lv. {2} in {3} for {4}', male: 'Male', female: 'Female', genderless: 'Genderless'
  },
  pt: {
    pcHint: 'Segure sobre as setas para trocar de caixa', nickname: 'Apelido', save: 'Salvar',
    pickBall: 'Escolha uma bola', capWho: 'Quem arremessa?', noBalls: 'Sem bolas na mochila. Peça ao Mestre.', gotcha: 'Capturou! {0} foi capturado!', brokeFree: '{0} escapou!', capRoll: 'Rolou {0}, precisava de {1} ou menos', capNat100: 'Só um 100 natural captura este', capGone: 'Tarde demais: esse Pokémon não está mais lá.', capNoRoom: 'Sem espaço: equipe e PC cheios. Crie uma caixa no PC.', capNoChar: 'Crie um personagem primeiro.', bagEmpty: 'Vazia', giveItems: 'Dar bolas', ballType: 'Bola', giveItemBtn: 'Dar bolas ao personagem acima', gaveItem: 'Deu {0} x{1} para {2} (agora tem {3})', cancel: 'Cancelar', fRegion: 'Região', fType: 'Tipo', fStageAll: 'Todos os estágios', fStageBase: 'Sem evolução', fStageEvolved: 'Evoluídos', fSortAz: 'Ordem: A-Z', fSortDex: 'Ordem: número da Pokédex', fClear: 'Limpar filtros', fCount: '{0} Pokémon', fMore: 'Mostrar mais {0}', ty_Normal: 'Normal', ty_Fire: 'Fogo', ty_Water: 'Água', ty_Electric: 'Elétrico', ty_Grass: 'Planta', ty_Ice: 'Gelo', ty_Fighting: 'Lutador', ty_Poison: 'Veneno', ty_Ground: 'Terrestre', ty_Flying: 'Voador', ty_Psychic: 'Psíquico', ty_Bug: 'Inseto', ty_Rock: 'Pedra', ty_Ghost: 'Fantasma', ty_Dragon: 'Dragão', ty_Dark: 'Sombrio', ty_Steel: 'Aço', ty_Fairy: 'Fada',  inviteHead: 'Convidar jogadores', inviteOff: 'Jogadores em outras redes precisam do link da internet.', inviteStart: 'Conectar pela internet', inviteDownload: 'Baixando a ferramenta do túnel (só na primeira vez)...', inviteStarting: 'Conectando à internet...', inviteLink: 'Link', inviteCode: 'Código da sessão', inviteNoCode: 'Crie uma sessão para ter o código', inviteCopy: 'Copiar convite', inviteCopied: 'Copiado!', inviteMsg: 'Entre em {0} e use o código {1}', inviteRetry: 'Tentar de novo', inviteLan: 'Mesmo Wi-Fi: {0}',  sfx: 'Sons de efeitos (captura, dado)', sfxVolume: 'Volume dos efeitos', holoSound: 'Som do holograma', sfxTest: 'Testar efeitos', rcTitle: 'Taxa de captura', rcBase: 'Base', rcLevel: 'Nível {0}', rcHp: 'HP {0}', rcEvo: 'Evoluções restantes: {0}', rcShiny: 'Shiny', rcInjury: 'Ferimentos x{0}', rcBonus: 'Bônus do Mestre', rcRoll: 'Dado', rcTotal: 'Dado + bola', rcNeed: 'Precisa de {0} ou menos', rcNat100: '100 natural!', rcSkip: 'toque para acelerar', rcContinue: 'toque para continuar', rcTries: 'Bolas restantes: {0}', fled: '{0} fugiu!', tries: 'Bolas permitidas', triesHint: '0 = ilimitado. Vale para todos os jogadores juntos: acabando, o Pokémon foge.', wildTries: 'Bolas', fx_burn: 'Queimadura', fx_freeze: 'Congelado', fx_paralysis: 'Paralisia', fx_poison: 'Veneno', fx_sleep: 'Sono', fx_confusion: 'Confusão', fx_stuck: 'Preso', fx_slow: 'Lento', wildHp: 'HP', wildFx: 'Efeitos', wildInj: 'Ferimentos', wildBonus: 'Bônus', wildRate: 'Taxa', wildFull: 'Cheio', wildOne: '1 HP', 
    pcFilter: 'Filtrar', pcParty: 'Equipe', pcBoxList: 'Lista de caixas', pcNewBox: '+ Nova caixa', pcBox: 'Caixa', pcNature: 'Natureza', pcAbility: 'Habilidade', pcMove: 'Mover', pcCancel: 'Cancelar', pcPick: 'Selecione um Pokémon', pcHold: 'Agora toque num espaço para colocá-lo', pcEmpty: 'Vazio', pcNoChars: 'Nenhum personagem nesta sessão ainda.', pcShiny: 'Shiny', pcTrash: 'Soltar', stXpTotal: 'XP total {0}', stPageStatus: 'Status', stPageEvo: 'Evolução', evoFrom: 'Evolui de', evoTo: 'Evolui para', evoNone: 'Nenhuma evolução conhecida para este Pokémon.', evoLevel: 'Nível {0}', evoReady: 'Pronto para evoluir!', evoLeft: 'faltam {0} nível(is)', evoBtn: 'Evoluir', evoAsk: 'Pedir ao Mestre', evoAsked: 'Pedido enviado', evoAskText: 'quer evoluir para {0}', evoMasterOnly: 'O Mestre decide esta (item, golpe, horário...).', evoNoReq: 'Sem requisito no livro: fale com o Mestre', evoConfirm: 'Evoluir {0} para {1}? Não dá para desfazer.', evoDone: '{0} evoluiu para {1}!', evoBack: 'Os novos atributos base quebraram a Relação de Base, então seus pontos gastos foram devolvidos para você gastar de novo.', evoNote: 'Natureza, nível, XP e dano são mantidos.', edEvolve: 'Evoluir para', edEvolveBtn: 'Evoluir agora', edEvolveAsk: 'Evoluir {0} para {1} agora?', edEvolveNote: 'Ignora o requisito. Gênero e habilidade se ajustam à nova espécie; os pontos gastos só são devolvidos se a Relação de Base quebrar.', stFreeTag: 'livre', stFreeTitle: 'Ignora a Relação de Base (liberado pelo Mestre)', edFree: 'Ignorar a Relação de Base em:', edFreeNote: 'Para Features / Edges do livro que quebram a regra. Só os atributos marcados podem passar dos outros.', edEdit: 'Editar', edSave: 'Salvar alterações', edNote: 'Trocar a natureza devolve todos os pontos gastos (o jogador gasta de novo). Nível, habilidade e gênero seguem as regras deste Pokémon.', edPoints: 'Pontos de atributo', edReset: 'Resetar pontos gastos', edResetAsk: 'Devolver todos os pontos gastos de {0}?', edDone: 'Pronto.', reqBtn: 'Pedir ajuste ao Mestre', reqSent: 'Pedido enviado', reqHint: 'Para resetar pontos ou trocar a natureza.', reqPrompt: 'O que você precisa? (opcional)', reqNote: '{0}: {1} precisa de ajuste', stStatus: 'Status', stClose: 'Fechar', stMaxAfter: 'máx. {0} ao aplicar', stFull: 'Cheio', stXpMax: 'nível máximo', stXpLeft: 'faltam {0} XP para o nível {1}', stPoints: 'Pontos para gastar', stBase: 'Base', stNat: 'Nat.', stSpent: 'Gasto', stTotal: 'Total', stApply: 'Aplicar', stClear: 'Limpar', stat0: 'HP', stat1: 'Ataque', stat2: 'Defesa', stat3: 'Atq. Esp.', stat4: 'Def. Esp.', stat5: 'Veloc.', giveXp: 'Dar XP', setHp: 'Definir HP', xpGave: 'XP dado.', xpLevelUp: 'Subiu de nível! {0} → {1}', confirmOk: 'Confirmar', pcTrashAsk: 'Soltar {0} para sempre?', pcRename: 'Clique para renomear',
    areas: 'Áreas', newArea: '+ Nova área', areaName: 'Nome', minLv: 'Nível mínimo', maxLv: 'Nível máximo', delAreaAsk: 'Apagar a área {0}?', areaForms: '{0} Pokémon', areaPool: 'Pokémon desta área', addForm: 'Adicionar à área', weight: 'Peso (maior = mais comum)', areaEmpty: 'Nenhum Pokémon ainda. Adicione abaixo.', noAreas: 'Nenhuma área ainda. Crie uma na aba Áreas.', areaPick: 'Área', pickArea: 'Escolha uma área primeiro.',
    rjAsk: 'Já existe um jogador chamado {0} nessa sessão. É você?', rjBtn: 'Sim, sou eu — pedir ao Mestre', rjWait: 'Esperando o Mestre aprovar…', rjCancel: 'Cancelar', rjDenied: 'O Mestre negou.', rjGone: 'O pedido expirou. Tente de novo.', rjMaster: '{0} quer voltar para a sessão {1}', rjOnline: 'parece estar online agora', rjYes: 'Aprovar', rjNo: 'Negar',
    stepWhich: 'Qual Pokémon?', stepDetails: 'Detalhes (opcional)', stepSend: 'Enviar', stepGive: 'Dar direto', advanced: 'Opções avançadas', fFilters: 'Filtros',
    srcPick: 'Escolher um Pokémon', srcArea: 'Sortear uma área', sendHeading: 'Enviar', catchRate: 'Chance de captura', catchNormal: 'Rolagem normal', catchNormalShort: 'Normal', catchHint: '100% sempre captura, 0% nunca captura. Os jogadores não veem isso.', onField: 'No campo', fieldEmpty: 'Nada no campo agora.', qty: 'Quantidade', sessionMeta: 'dia {0} · {1} jogador(es)',
    appearance: 'Aparência', modeDefault: 'Padrão', modeLight: 'Claro', modeDark: 'Escuro', modeAuto: 'Auto', frameColor: 'Cor da Pokédex', resetColor: 'Cor padrão', brightness: 'Brilho', menuPlace: 'Posição', soundHead: 'Som',
    settings: 'Configurações', sound: 'Sons dos cries', volume: 'Volume dos cries', tabMode: 'Botões do menu', rolling: 'Rolante (deslizar / roda)', fixed: 'Fixo (setas)', menu: 'Menu', side: 'Lateral', bottom: 'Embaixo', top: 'Em cima', language: 'Idioma', color: 'Cor da Pokédex',
    campaigns: 'Campanhas', leave: 'Sair da sessão',
    whoAreYou: 'Quem é você?', yourName: 'Seu nome', continue: 'Continuar', hi: 'Olá,', changeName: 'trocar nome', otherDev: 'Usar em outro aparelho', otherDevHelp: 'Copie este link e abra no outro aparelho: mesmo nome, personagens e campanhas. Não compartilhe com ninguém.', copyLink: 'Copiar link', linkCopied: 'Copiado!', connect: 'Conectar',
    codePh: 'Código da sessão (ex.: K7P2)', lost: 'Perdeu o acesso a uma sessão?',
    claimHelp: 'Digite o código da sessão acima e o código de reconexão que o Mestre te deu.', claimPh: 'Código de reconexão', reconnect: 'Reconectar',
    online: 'Online', lastSeen: 'visto {0}', neverSeen: 'ainda não apareceu', secAgo: 'há {0}s', minAgo: 'há {0} min', hourAgo: 'há {0} h', dexProgressTip: 'Pokémon capturados na Pokédex', inPc: '{0} no PC', noParty: 'Nenhum Pokémon no time', qGive: 'Dar Pokémon / bolas', qPc: 'Abrir PC', qSpawn: 'Spawn para ele', xpHp: 'XP e HP', resetDex: 'Zerar Pokédex', resetDexAsk: 'Zerar a Pokédex inteira de {0}? O álbum e as figurinhas são apagados e não dá para desfazer.', clearField: 'Limpar o campo', clearFieldAsk: 'Mandar embora todos os Pokémon selvagens? Os jogadores deixam de vê-los.', clearFieldDone: '{0} removidos.', playersHelp: 'Cada cartão é um jogador: quem está online, o time e botões rápidos. Use os botões em vez de procurar nas abas.', wildInfo: 'Atributos e capabilities', caps: 'Capabilities',
    statNames: 'PV,Ataque,Defesa,Atq. Esp.,Def. Esp.,Velocidade', hpLbl: 'PV', freePts: 'Pontos de atributo livres: {0}', setLevel: 'Definir nível', mapBrushLbl: 'Pincel', mapUndo: 'Desfazer (Ctrl+Z)', mapCenterBtn: 'Centralizar o mapa', mapMe: 'Eu', mapNoTokens: 'Nenhuma peça ainda', mapNpcName: 'Nome do NPC:', mapTokH: 'Peças no mapa', mapTokHelp: 'Clique para pôr uma peça no meio da tela; arraste com a ferramenta Mão. Pôr de novo só move a peça. O x tira.', mapAddNpc: '+ NPC', mapWildH: 'Pokémon selvagens no campo', mapOnMap: 'Neste mapa', mapMineHelp: 'Toque no seu personagem ou num Pokémon para pôr no mapa ou tirar. Aperte e arraste uma peça para movê-la.', mapTab: 'Mapa', mapTabM: 'Mapas', mapHelp: 'O mapa que o Mestre está mostrando. Arraste para andar pelo mapa; dois dedos dão zoom.', mapHelpM: 'Monte mapas com blocos e mostre um aos jogadores. Escolha um bloco abaixo e pinte (aperte e arraste). Use a Mão para andar pelo mapa; roda do mouse ou dois dedos dão zoom. O + cria um mapa novo; clique num mapa para editar.', mapNone: 'Nenhum mapa em exibição', mapNew: 'Novo mapa', mapShow: 'Mostrar aos jogadores', mapHide: 'Parar de mostrar', mapRename: 'Renomear', mapDelete: 'Apagar', mapNamePrompt: 'Nome do mapa:', mapDeleteAsk: 'Apagar o mapa "{0}"?', mtHand: 'Mão', mtTree: 'Floresta', mvTree: 'Carvalho,Pinheiro,Outono,Palmeira,Pinheiro nevado,Árvore seca', mtSand: 'Areia', mtSnow: 'Neve', mtDirt: 'Terra', mvGrass: 'Normal,Flores,Seca,Floresta', mvWater: 'Lago,Mar fundo,Pântano,Lava', mvCave: 'Caverna,Rocha clara,Vulcânica', mvSand: 'Praia,Deserto,Cascalho', mvSnow: 'Neve,Gelo', mvDirt: 'Terra,Lama,Argila', mvRoad: 'Terra / madeira,Pedra,Tijolo,Asfalto', mvWall: 'Pedra,Tijolo,Madeira,Sebe,Gelo', mvHouse: 'Vermelha,Azul,Verde,Cabana,Pedra,Palha', mvElev: 'Nível 1,Nível 2,Nível 3,Nível 4', mtGrass: 'Grama', mtWater: 'Água', mtCave: 'Caverna', mtRoad: 'Estrada / ponte', mtElev: 'Elevação', mtFlat: 'Rebaixar', mtWall: 'Parede', mtHouse: 'Casa', mtBare: 'Limpar cima', mtErase: 'Apagar bloco', tableTab: 'Mesa', tblHelp: 'Mova a mãozinha sobre a mesa. Escolha quantos pegar, aperte em cima de um item, mova e solte: na mesa para jogar, em cima do baú para guardar.', tblHelpM: 'Todos da sessão veem esta mesa. Digite quantidades na lista e jogue a lista, ou aperte num item e arraste para a mesa; cada jogador pega o que quiser com a mãozinha. Você também tem uma mão: aperte e arraste para mover itens.', tblTakeLbl: 'Pegar', tblAll: 'Tudo', tblChest: 'Baú', tblChestHint: 'O baú fica logo embaixo da mesa. Aperte um item (segure um instante no celular) e arraste para a mesa; para guardar, solte em cima do baú. Role para ver mais.', tblNoItems: 'Nenhum item', tblNoChar: 'Você não tem personagem nesta sessão. Adicione um na aba Elenco para guardar itens.', tblSelHint: 'Clique num item para somar 1 na lista (botão direito: -1). Arraste um para a mesa para jogá-lo.', tblItemsH: 'Itens', tblThrowList: 'Jogar a lista', tblListClear: 'Zerar quantidades', tblListEmpty: 'Digite uma quantidade em pelo menos um item.', tblGrab: 'Pegar', tblEmpty: 'Vazio', tblGot: '{0} x{1} foi para o baú.', tblThrow: 'Jogar na mesa', tblThrowBtn: 'Jogar', tblClear: 'Limpar a mesa', tblClearAsk: 'Tirar tudo da mesa?',
    dex: 'Pokédex', dexSummary: 'Vistos {0} · Capturados {1} · de {2} Pokémon', dexSeen: 'Visto', dexCaught: 'Capturado', dexHand: 'Figurinhas na mão', dexAll: 'Todos', wildRemove: 'Remover do campo', wildRemoveAsk: 'Remover este Pokémon do campo? Os jogadores deixam de vê-lo.', newSticker: 'Figurinha nova no seu pacote!', packBtn: 'Abrir o pacote de figurinhas ({0})', packNone: 'Nenhuma figurinha nova', packTitle: 'Pacote de figurinhas', packCount: '{0} figurinhas dentro', packTear: 'Segure a bola até ela abrir.', packKeep: 'Guardar', dexHandPack: 'Abra o pacote acima para pegar suas figurinhas.', dexShiny: 'Shiny', dexPeelHint: 'Aperte e passe o dedo (ou o mouse) sobre a figurinha, de baixo para cima, para colar.', dexHandEmpty: 'Nenhuma figurinha solta. A primeira captura de cada Pokémon dá uma.', dexWrong: 'Essa figurinha não é deste espaço.', dexStuck: '{0} colado no álbum!', dexLvl: 'Nv. {0}',
    encounters: 'Campo', characters: 'Elenco', bag: 'Mochila', pc: 'PC', nothing: 'Nada por enquanto...', capture: 'Capturar',
    newChar: 'Nome do novo personagem', soon: 'Em breve.', bringHelp: 'Marque os personagens que vão para a sessão:', myChars: 'Meus personagens',
    myCharsHelp: 'Criados aqui, antes de qualquer sessão. Cada sessão recebe uma cópia independente.', createChar: 'Criar personagem', noSheets: 'Nenhum personagem ainda. Crie um abaixo.',
    delSheet: 'Apagar este personagem? As cópias que já estão em sessões continuam lá.', addFromMine: 'Adicionar um dos meus personagens', addToSession: 'Adicionar a esta sessão',
    manageHelp: 'Crie ou apague personagens na tela de Campanhas.', noneLeft: 'Todos os seus personagens já estão aqui',
    create: 'Criar', sessions: 'Sessões', players: 'Jogadores', spawn: 'Spawnar', activeSession: 'Sessão ativa:', newSession: 'Nova sessão',
    createSession: 'Criar sessão', yourSessions: 'Suas sessões', loading: 'Carregando...', spawnTitle: 'Spawnar um Pokémon', searchPh: 'Buscar (ex.: rotom)',
    shiny: 'Shiny', shinyRandom: 'Aleatório (1%)', yes: 'Sim', no: 'Não', random: 'Aleatório', nature: 'Natureza', ability: 'Habilidade', gender: 'Gênero', level: 'Nível', sendTo: 'Selvagem, enviar para:', everyone: 'Todos', spawnWild: 'Spawnar (selvagem)', giveTo: 'Ou dar para:', giveBtn: 'Dar ao personagem',
    typeName: 'Digite seu nome.', welcome: '{0} (jogador #{1}) na sessão {2}', yourCampaigns: 'Suas campanhas',
    confirmLeave: 'Sair da sessão {0}? Seus personagens nela serão apagados (suas fichas continuam salvas).', kicked: 'O Mestre removeu você de {0}.', deleted: 'A sessão {0} foi apagada.',
    manyWild: '{0} Pokémon selvagens apareceram!', wildNear: 'Um Pokémon selvagem apareceu perto de você!', wildAll: 'Um Pokémon selvagem apareceu!', gotPoke: 'Você recebeu um Pokémon!',
    captureTodo: 'A captura é o próximo passo - ainda não existe.', noPoke: 'Nenhum Pokémon ainda', noChars: 'Nenhum personagem nesta sessão ainda.', del: 'Apagar', delChar: 'Apagar este personagem?',
    lv: 'Nv.', playersOpen: 'Jogadores abrem:', sessionRow: 'Código {0} · Mestre {1} · dia {2} · {3} jogador(es)', noSessions: 'Nenhuma sessão ainda.',
    delSession: 'Apagar a sessão {0}? Os jogadores são removidos e não dá para desfazer.', noPlayers: 'Nenhum jogador ainda.', reconnectCode: 'Código de reconexão', kick: 'Expulsar',
    noCharNote: '{0} - sem personagem ainda (o jogador precisa criar um)', claimMsg: 'Passe isto ao jogador: {0}\nSessão {1} (vale 10 minutos, uma vez só)',
    kickConfirm: 'Expulsar este jogador?', created: 'Criada! Código: {0}', pickMon: 'Escolha um Pokémon primeiro.', pickMonChar: 'Escolha um Pokémon e um personagem primeiro.',
    gave: 'Deu {0} Nv. {1} para {2}', spawned: 'Spawnou {0} ({1}) Nv. {2} em {3} para {4}', male: 'Macho', female: 'Fêmea', genderless: 'Sem gênero'
  }
};

function lang() {
  try { const l = localStorage.getItem('ptu.lang'); if (l in TEXT) return l; } catch (e) {}
  return (navigator.language || '').toLowerCase().startsWith('pt') ? 'pt' : 'en';
}

// Messages the server sends (English) -> Portuguese; anything unknown is shown as it came
const ERR_PT = {
  'This evolution needs the Master': 'Esta evolução precisa do Mestre.',
  "This Pokémon doesn't evolve into that": 'Este Pokémon não evolui para isso.',
  'Not enough stat points': 'Você não tem pontos suficientes.',
  "Base Relation rule: a stat can't pass one with a higher base": 'Regra de Relação de Base: um atributo não pode passar outro que tem base maior.',
  'XP must be 1-100000': 'O XP deve ser de 1 a 100000.',
  'Amount must be 1-99': 'A quantidade deve ser de 1 a 99.',
  'Level must be 1-100': 'O nível deve ser de 1 a 100.',
  'Bad numbers': 'Números inválidos.',
  'formId and level must be numbers': 'Escolha um Pokémon e um nível válidos.',
  'formId, level and trainerId must be numbers': 'Escolha um Pokémon, um nível e um personagem.',
  'Bad person key': 'Chave de pessoa inválida.',
  'Character not in this session': 'Esse personagem não está nesta sessão.',
  'Player not in this session': 'Esse jogador não está nesta sessão.',
  'Code and name required': 'Informe o código e o nome.',
  'Name required': 'Informe um nome.',
  'Name must have 1-30 characters': 'O nome deve ter de 1 a 30 caracteres.',
  'Form not found': 'Pokémon não encontrado.',
  'Item not found': 'Item não encontrado.',
  'Invalid or expired reconnect code': 'Código de reconexão inválido ou expirado.',
  'Master only': 'Somente o Mestre pode fazer isso.',
  'No room: party and every PC box are full': 'Sem espaço: equipe e todas as caixas do PC estão cheias.',
  'Not your character': 'Esse personagem não é seu.',
  'Not your trainer': 'Esse personagem não é seu.',
  'Session no longer exists': 'A sessão não existe mais.',
  'That name is already used in this session. If it is you, ask the Master for a reconnect code.': 'Esse nome já está em uso nesta sessão. Se é você, peça um código de reconexão ao Mestre.',
  'Unknown code': 'Código desconhecido.',
  'Unknown session': 'Sessão desconhecida.',
  'Unknown nature': 'Natureza desconhecida.',
  'You were removed from this session': 'Você foi removido desta sessão.',
  'Character not found': 'Personagem não encontrado.',
  'This character is already in this session': 'Esse personagem já está nesta sessão.',
  'Pokemon not found': 'Pokémon não encontrado.',
  'Bad slot': 'Espaço inválido.',
  'Nickname must have at most 20 characters': 'O apelido deve ter no máximo 20 caracteres.',
  'removed': 'Você foi removido desta sessão.'
};

function errText(msg) {
  if (lang() !== 'pt') return msg;
  if (ERR_PT[msg]) return ERR_PT[msg];
  let m = /^This Pok.mon can't be (.+)$/.exec(msg);
  if (m) return 'Este Pokémon não pode ser: ' + m[1] + '.';
  m = /^That ability is not available at level (\d+)$/.exec(msg);
  if (m) return 'Essa habilidade não está disponível no nível ' + m[1] + '.';
  return msg;
}

function t(key, ...args) {
  return (TEXT[lang()][key] ?? TEXT.en[key] ?? key).replace(/\{(\d)\}/g, (m, i) => args[i]);
}

// the page is rebuilt on change: simplest way to redo every text that JS wrote
function setLang(l) {
  try { localStorage.setItem('ptu.lang', l); } catch (e) {}
  location.reload();
}

//=========LOOK: frame color (wheel) + light / dark mode=========
// The frame color is one hex (ptu.color) picked on a hue/saturation wheel + a lightness slider.
// The other frame variables (keys, rail, toggle, key text) are derived from it here.

const DEFAULT_COLOR = '#d32f2f';
const OLD_THEMES = { red: '#d32f2f', purple: '#7b1fa2', blue: '#1976d2', green: '#2e7d32', black: '#2b2b2b', white: '#eceff1' };  // from the old preset buttons

const store = (k, v) => { try { if (v === undefined) return localStorage.getItem(k); localStorage.setItem(k, v); } catch (e) {} return null; };

function hslToHex(h, s, l) {
  s /= 100; l /= 100;
  const f = n => { const k = (n + h / 30) % 12, a = s * Math.min(l, 1 - l); return Math.round(255 * (l - a * Math.max(-1, Math.min(k - 3, 9 - k, 1)))); };
  return '#' + [f(0), f(8), f(4)].map(x => x.toString(16).padStart(2, '0')).join('');
}

function hexToHsl(hex) {
  const n = parseInt(hex.slice(1), 16), r = (n >> 16) / 255, g = (n >> 8 & 255) / 255, b = (n & 255) / 255;
  const max = Math.max(r, g, b), min = Math.min(r, g, b), d = max - min, l = (max + min) / 2;
  let h = 0;
  if (d) h = max === r ? ((g - b) / d + 6) % 6 : max === g ? (b - r) / d + 2 : (r - g) / d + 4;
  return { h: Math.round(h * 60), s: d ? Math.round(d / (1 - Math.abs(2 * l - 1)) * 100) : 0, l: Math.round(l * 100) };
}

const validHex = x => /^#[0-9a-f]{6}$/i.test(x || '');
const savedColor = () => { const c = store('ptu.color'); return validHex(c) ? c : OLD_THEMES[store('ptu.theme')] || DEFAULT_COLOR; };

// frame = the picked color; keys a bit darker, rail and toggle much darker; key text dark on light colors
function applyColor(hex) {
  const { h, s, l } = hexToHsl(hex), root = document.documentElement.style;
  const r = (parseInt(hex.slice(1, 3), 16) * 299 + parseInt(hex.slice(3, 5), 16) * 587 + parseInt(hex.slice(5, 7), 16) * 114) / 1000;
  root.setProperty('--red', hex);
  root.setProperty('--key', hslToHex(h, s, Math.max(l - 6, 4)));
  root.setProperty('--rail', hslToHex(h, s, Math.max(l * 0.45, 3)));
  root.setProperty('--toggle', hslToHex(h, s, Math.max(l * 0.3, 2)));
  root.setProperty('--keytext', r > 170 ? '#12313b' : '#fff');
}

function setLook(m) {
  store('ptu.mode', m);
  applyMode();
}

function applyMode() {
  let m = store('ptu.mode') || 'default';
  if (m === 'auto') m = matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'default';
  document.documentElement.dataset.mode = m;
  document.querySelectorAll('#modeSeg button').forEach(b => b.classList.toggle('on', b.dataset.v === (store('ptu.mode') || 'default')));
}

// The "Appearance" block of the Settings panel (same on the player and Master pages)
function buildAppearance(box) {
  box.innerHTML =
    '<h4>' + t('appearance') + '</h4>' +
    '<div class="seg" id="modeSeg">' + [['default', t('modeDefault')], ['light', t('modeLight')], ['dark', t('modeDark')], ['auto', t('modeAuto')]].map(([v, label]) =>
      '<button data-v="' + v + '" onclick="setLook(\'' + v + '\')">' + label + '</button>').join('') + '</div>' +
    '<div class="wheelbox"><div class="wheel" id="wheel"><canvas width="264" height="264"></canvas><i></i></div>' +
    '<div class="wheelside"><div class="chip" id="chip"></div><input type="text" id="hexIn" maxlength="7" spellcheck="false" aria-label="' + t('frameColor') + '">' +
    '<button id="colorReset">' + t('resetColor') + '</button></div>' +
    '<label class="lrow"><span>' + t('brightness') + '</span><input type="range" id="lightIn" min="8" max="92"></label></div>';

  const wheel = box.querySelector('#wheel'), canvas = wheel.querySelector('canvas'), dot = wheel.querySelector('i');
  const hexIn = box.querySelector('#hexIn'), lightIn = box.querySelector('#lightIn');
  let exact = savedColor(), c = hexToHsl(exact);   // exact: the hex as typed/saved (HSL rounding would shift it a little)
  const R = canvas.width / 2, ctx = canvas.getContext('2d');

  // hue = angle, saturation = distance from the center; lightness comes from the slider
  function paint() {
    const img = ctx.createImageData(canvas.width, canvas.height);
    for (let y = 0; y < canvas.height; y++) for (let x = 0; x < canvas.width; x++) {
      const dx = x - R + 0.5, dy = y - R + 0.5, d = Math.hypot(dx, dy), i = (y * canvas.width + x) * 4;
      if (d > R) continue;
      const hex = hslToHex((Math.atan2(dy, dx) * 180 / Math.PI + 360) % 360, d / R * 100, c.l), n = parseInt(hex.slice(1), 16);
      img.data[i] = n >> 16; img.data[i + 1] = n >> 8 & 255; img.data[i + 2] = n & 255; img.data[i + 3] = 255;
    }
    ctx.putImageData(img, 0, 0);
  }

  function show() {
    const hex = exact || hslToHex(c.h, c.s, c.l), a = c.h * Math.PI / 180, d = c.s / 100 * 50;
    dot.style.left = (50 + d * Math.cos(a)) + '%'; dot.style.top = (50 + d * Math.sin(a)) + '%';
    box.querySelector('#chip').style.background = hex;
    hexIn.value = hex; lightIn.value = c.l;
    return hex;
  }

  function commit(repaint) {
    const hex = show();
    if (repaint) paint();
    store('ptu.color', hex);
    applyColor(hex);
  }

  function pick(e) {
    const r = wheel.getBoundingClientRect(), dx = e.clientX - r.left - r.width / 2, dy = e.clientY - r.top - r.height / 2;
    c.h = Math.round((Math.atan2(dy, dx) * 180 / Math.PI + 360) % 360);
    c.s = Math.round(Math.min(Math.hypot(dx, dy) / (r.width / 2), 1) * 100);
    exact = null; commit(false);
  }
  wheel.onpointerdown = e => { wheel.setPointerCapture(e.pointerId); pick(e); wheel.onpointermove = pick; };
  wheel.onpointerup = wheel.onpointercancel = () => { wheel.onpointermove = null; };
  lightIn.oninput = () => { c.l = +lightIn.value; exact = null; commit(true); };
  hexIn.onchange = () => { const v = hexIn.value.startsWith('#') ? hexIn.value : '#' + hexIn.value; if (validHex(v)) { exact = v.toLowerCase(); c = hexToHsl(exact); commit(true); } else show(); };
  box.querySelector('#colorReset').onclick = () => { exact = DEFAULT_COLOR; c = hexToHsl(exact); commit(true); };

  paint(); show();
}

(function init() {
  document.documentElement.lang = lang() === 'pt' ? 'pt-BR' : 'en';
  document.querySelectorAll('[data-i]').forEach(e => { e.textContent = t(e.dataset.i); });
  document.querySelectorAll('[data-ip]').forEach(e => { e.placeholder = t(e.dataset.ip); });
  const sel = document.getElementById('langSel');
  if (sel) sel.value = lang();
  const look = document.getElementById('appearance');
  if (look) buildAppearance(look);
  applyColor(savedColor());
  applyMode();
})();
