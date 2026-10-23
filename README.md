# PepperDex

**A companion app for Pokémon Tabletop United (PTU)** — built to help Game Masters and players manage a campaign, Pokémon, captures, trades, encounters, and more.

**English** | [Português](#português)

> **Unofficial fan project.** PepperDex is a community-made tool inspired by Pokémon Tabletop United. It is not affiliated with, endorsed by, or associated with Nintendo, Game Freak, The Pokémon Company, or the creators of PTU. Pokémon names and imagery belong to their respective owners.

## Features

- **Master and player interfaces** served in the browser.
- **Pokémon collection management:** party and PC/boxes.
- **Captures** and Pokémon spawning for encounters.
- **Trades** between players.
- **Campaign sessions:** invite links, session codes, and QR codes for players.
- **Encounter and area tools** for the Game Master.
- **Pokémon progression:** evolution and XP-related tools.
- **Visual effects and animations** in the interface.
- **Pokémon sprites**, including shiny variants, and Pokémon cries.
- **English and Brazilian Portuguese** interface text.

### Pokémon data and media

The current project files contain:

- **972 Pokémon/form entries** in the root `pokemon.json` data file.
- **971 regular sprite files** and **971 shiny sprite files** (1,942 PNG files total).
- **971 cry audio files** (MP3).

Coverage is incomplete; some Pokémon/forms are not included, and Generation IX is currently excluded. Counts refer to files in the supplied project snapshot and may change as the dataset evolves.

## Screenshots and visuals

The image above is a preview assembled from sprite assets included in the project. The supplied repository archive did not contain captured screenshots of the running interface, so this README does not present mockups as if they were real screenshots.

For a stronger project page, add real screenshots captured from the running app to `docs/images/`, for example:

- `master.png` — Master dashboard / session tools.
- `player.png` — Player view.
- `encounters.png` — Encounter or capture flow.

Then embed them here with Markdown, for example: `![Master interface](docs/images/master.png)`.

## Running the app

### Using a packaged build

If a packaged release is available for your operating system:

1. Download and extract the Windows or Linux package from the repository's **Releases** page.
2. On Windows, run `Iniciar.bat`. On Linux, run `./iniciar.sh`.
3. The Master interface should open in your browser. The local address is `http://localhost:8080/master`.
4. Create a session and share the generated invitation link, code, or QR code with your players.

The packaged build includes its own Java runtime. On first use, the app may download `cloudflared` to create an internet tunnel, so an internet connection is required for that feature. The public invitation link may change when the app is restarted.

**Campaign data:** the app stores local state in `pokemon.db`. Back up this file before updating or moving to a new package. To migrate to a new version, close the app and copy your previous `pokemon.db` into the new app folder before starting it.

### Running from source

Requirements: **JDK 21 or newer**.

1. Clone or download the repository.
2. From the project root, build the self-contained package:

   ```bash
   bash packaging/build.sh
   ```

3. The build output is created in `dist/PTU-Mestre/`.
4. Run `Iniciar.bat` on Windows or `./iniciar.sh` on Linux from the packaged folder.

For development in Eclipse, run `Server.Server` and open `http://localhost:8080/master`. The web interface lives in `web/`; most front-end changes can be tested by refreshing the browser.

> The current source build script targets the local operating system. Cross-platform release packages require building on the corresponding platform or setting up a release workflow.

## Project structure

```text
PepperDex/
├── README.md
├── src/
│   ├── Client/
│   │   ├── Master/                 # Master client MVC classes
│   │   └── Player/                 # Player client MVC classes
│   ├── Localization/               # Language support
│   └── Server/
│       ├── Database/                # SQLite connection and schema
│       ├── Encounter/               # Encounters, areas, spawn data
│       ├── Evolution/                # Evolution rules and conditions
│       ├── Item/                     # Items
│       ├── Master/                   # Master model
│       ├── PC/                       # PC and box management
│       ├── Pokemon/                  # Pokémon entities and attributes
│       ├── PokemonData/              # Species data: moves, abilities, skills
│       ├── Repository/               # Persistence access
│       ├── Services/                 # Capture, sessions, areas, tunnel, Pokédex
│       ├── Session/                  # Game sessions and participants
│       ├── Trade/                    # Trades and offers
│       ├── Trainer/                  # Trainer and inventory data
│       ├── Util/                     # Utilities
│       ├── Web/                      # HTTP server
│       ├── ClientHandler.java
│       ├── Server.java
│       ├── ServerController.java
│       ├── ServerModel.java
│       └── ServerView.java
├── web/
│   ├── master.html                  # Master interface
│   ├── player.html                  # Player interface
│   ├── app.js                       # Shared front-end logic
│   ├── style.css
│   └── assets/
│       ├── sprites/
│       │   └── shiny/
│       ├── cries/
│       ├── dice/
│       ├── items/
│       └── pokemon-art.json
├── resources/
│   ├── i18n/                        # English and Brazilian Portuguese strings
│   └── pokemon/
│       └── pokemon.json             # Duplicate data copy present in the snapshot
├── lib/
│   └── sqlite-jdbc.jar
├── packaging/
│   ├── build.sh
│   ├── Iniciar.bat
│   ├── iniciar.sh
│   └── LEIA-ME.txt
├── pokemon.json                     # Initial Pokémon data used at startup
└── docs/
    └── images/
        └── pokemon-sprite-preview.png
```

**Local/generated files** such as `bin/`, `dist/`, `build/`, `pokemon.db`, `pokedex.dat`, `spawnfeed.dat`, and the downloaded `tools/cloudflared` binary should not be committed unless you intentionally want to distribute those files. Keep a backup of your own `pokemon.db`; do not publish campaign or player data.

## Tech stack

- Java (JDK 21+ for building)
- Built-in Java HTTP server
- HTML, CSS, and JavaScript
- SQLite with the SQLite JDBC driver
- `jlink` for packaging a compact Java runtime

## Contributing

Bug reports, feature suggestions, and pull requests are welcome. Before opening a pull request, describe the problem or feature and include steps to reproduce or test it.

## License and data attribution

No project license was present in the supplied archive. Add a `LICENSE` file before presenting the source code as open source, and verify redistribution rights for the PTU-derived dataset, Pokémon sprites, cries, and third-party dependencies. Public availability on GitHub does not automatically grant permission to redistribute copyrighted material.

---

# Português

**Um aplicativo de apoio para Pokémon Tabletop United (PTU)** — desenvolvido para ajudar Mestres e jogadores a organizar campanhas, Pokémon, capturas, trocas, encontros e outras atividades da mesa.

## Funcionalidades

- **Interfaces do Mestre e dos jogadores** acessadas pelo navegador.
- **Gerenciamento de Pokémon:** equipe e PC/boxes.
- **Capturas** e geração de Pokémon para encontros.
- **Trocas** entre jogadores.
- **Sessões de campanha:** links de convite, códigos de sessão e QR codes.
- **Ferramentas de áreas e encontros** para o Mestre.
- **Progressão dos Pokémon:** evolução e ferramentas relacionadas a XP.
- **Efeitos visuais e animações** na interface.
- **Sprites de Pokémon**, incluindo versões shiny, e cries/áudios dos Pokémon.
- **Interface em português brasileiro e inglês.**

### Dados e mídia de Pokémon

Os arquivos atuais do projeto contêm:

- **972 entradas de Pokémon/formas** no arquivo `pokemon.json` da raiz.
- **971 sprites normais** e **971 sprites shiny** (1.942 arquivos PNG no total).
- **971 arquivos de cry** em MP3.

A cobertura ainda é incompleta: alguns Pokémon/formas não estão incluídos e a Geração IX está atualmente fora do conjunto de dados. Esses números refletem o ZIP analisado e podem mudar conforme a base evoluir.

## Imagens do projeto

A imagem no início deste README é uma prévia montada com sprites que já fazem parte do projeto. O ZIP fornecido não continha capturas de tela da interface em execução; por isso, não incluí mockups fingindo ser screenshots reais.

Para deixar a página do GitHub mais forte, capture telas reais do aplicativo em execução e salve em `docs/images/`, por exemplo:

- `master.png` — painel do Mestre e ferramentas de sessão.
- `player.png` — interface do jogador.
- `encounters.png` — tela de encontro ou fluxo de captura.

Depois, inclua-as com Markdown, por exemplo: `![Interface do Mestre](docs/images/master.png)`.

## Como executar

### Usando uma versão empacotada

Se houver uma versão publicada para seu sistema operacional:

1. Baixe e extraia o pacote de Windows ou Linux na página **Releases** do repositório.
2. No Windows, execute `Iniciar.bat`. No Linux, execute `./iniciar.sh`.
3. A interface do Mestre deve abrir no navegador. O endereço local é `http://localhost:8080/master`.
4. Crie uma sessão e compartilhe com os jogadores o link, o código ou o QR code gerado.

O pacote inclui seu próprio runtime Java. No primeiro uso, o aplicativo pode baixar o `cloudflared` para criar um túnel de acesso pela internet; esse recurso exige conexão com a internet. O link público de convite pode mudar quando o programa for reiniciado.

**Dados da campanha:** o estado local fica no arquivo `pokemon.db`. Faça backup antes de atualizar ou mover o aplicativo. Para migrar para uma versão nova, feche o app e copie o `pokemon.db` anterior para a pasta da nova versão antes de iniciar.

### Executando a partir do código-fonte

Requisito: **JDK 21 ou mais recente**.

1. Clone ou baixe o repositório.
2. Na raiz do projeto, gere o pacote:

   ```bash
   bash packaging/build.sh
   ```

3. O resultado será criado em `dist/PTU-Mestre/`.
4. Execute `Iniciar.bat` no Windows ou `./iniciar.sh` no Linux dentro da pasta empacotada.

Para desenvolvimento no Eclipse, execute `Server.Server` e acesse `http://localhost:8080/master`. A interface web fica em `web/`; a maioria das alterações de front-end pode ser testada atualizando o navegador.

> O script atual gera o pacote para o sistema operacional em que é executado. Para publicar pacotes de vários sistemas, é necessário compilar em cada plataforma ou configurar um fluxo de publicação.

## Estrutura do projeto

A árvore acima apresenta os principais módulos e pastas do PepperDex. Em resumo:

- `src/` — código Java do cliente, servidor, regras, sessões, captura, trocas e persistência.
- `web/` — páginas do Mestre e do jogador, lógica de interface, estilos, sprites e áudios.
- `resources/i18n/` — textos em português brasileiro e inglês.
- `resources/pokemon/pokemon.json` — cópia adicional da base que existe no ZIP; confirme se ela ainda é necessária para evitar manter duas versões divergentes.
- `lib/` — driver JDBC do SQLite.
- `packaging/` — scripts de compilação e inicialização.
- `pokemon.json` — dados iniciais usados pelo aplicativo.

Arquivos locais/gerados, como `bin/`, `dist/`, `build/`, `pokemon.db`, `pokedex.dat`, `spawnfeed.dat` e o executável baixado `tools/cloudflared`, não devem ser publicados por padrão. Mantenha backup do seu `pokemon.db` e nunca publique dados privados da campanha ou dos jogadores.

## Tecnologias

- Java (JDK 21+ para compilar)
- Servidor HTTP nativo do Java
- HTML, CSS e JavaScript
- SQLite com driver JDBC
- `jlink` para empacotar um runtime Java compacto

## Contribuições

Relatos de bugs, sugestões e pull requests são bem-vindos. Ao abrir uma contribuição, descreva o problema ou a funcionalidade e inclua passos para reproduzir ou testar.

## Licença e atribuição dos dados

O ZIP analisado não continha um arquivo de licença do projeto. Adicione um `LICENSE` antes de apresentar o código como open source e verifique os direitos de redistribuição dos dados derivados de PTU, sprites, cries e dependências de terceiros. Publicar algo no GitHub não concede automaticamente permissão para redistribuir material protegido por direitos autorais.
