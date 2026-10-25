# PTU - Mesa do Mestre

App web de apoio ao RPG **Pokémon Tabletop United**: o Mestre roda o programa no próprio computador e os jogadores
entram pelo navegador (celular ou PC), **de qualquer lugar**. Pokédex, party e PC, captura com dado, áreas de spawn,
evolução, XP, ferramentas do Mestre. Português e inglês.

## Para o Mestre: baixar e jogar

1. Abra a aba **Releases** deste repositório (precisa estar logado no GitHub e ter sido convidado).
2. Baixe `PTU-Mestre-Windows.zip` (Windows) ou `PTU-Mestre-Linux.tar.gz` (Linux) e extraia.
3. **Windows:** dois cliques em `Iniciar.bat`. **Linux:** `./iniciar.sh`.
4. A página do Mestre abre sozinha. Na aba **Sessões** aparece o convite (link + código + QR): clique em
   **Copiar convite** e mande aos jogadores. Eles abrem o link de qualquer rede e digitam o código (ou leem o QR).

Não precisa instalar Java nem nada: ele já vai dentro do pacote. Na primeira vez o programa baixa o `cloudflared`
(gratuito, da Cloudflare) para criar o link da internet. O link muda a cada vez que o programa abre.

Seus dados ficam em `pokemon.db` na pasta do programa. **Ao atualizar de versão, copie o `pokemon.db` antigo para a pasta nova.**

### Segurança

Só a página aberta **neste computador** tem poderes de Mestre. Quem entra pelo link da internet (ou pela rede Wi-Fi)
entra sempre como jogador: o servidor recusa pedidos de Mestre que chegam com cabeçalhos de proxy/túnel ou com nome de
host público. Não compartilhe o link da página do Mestre (`localhost`): ela só abre no seu próprio computador.

## Para quem desenvolve

Projeto Java (Eclipse, módulo `Pokemon`) + páginas web puras em `web/` (sem build, é só editar e dar F5).

```
src/Server/...        servidor (Java 21+): banco SQLite, regras PTU, servidor web, túnel
web/                  páginas do Mestre e do Jogador (HTML/JS/CSS), sprites e cries em web/assets
pokemon.json          dados de todos os Pokémon (lido na primeira execução para criar o pokemon.db)
packaging/            build do pacote sem instalação (jlink) e lançadores
.github/workflows/    gera os pacotes Windows e Linux e publica em Releases
```

Rodar em desenvolvimento: execute `Server.Server` (Eclipse) e abra `http://localhost:8080/master`.
Na aba Sessões, o botão **Conectar pela internet** liga o túnel (em desenvolvimento ele não liga sozinho).

Gerar o pacote do seu sistema localmente (precisa de JDK 21+): `bash packaging/build.sh` → `dist/PTU-Mestre/`.

### Publicar este repositório (privado) e uma versão

```bash
git init -b main
git add .
git commit -m "PTU Mesa do Mestre"
git remote add origin git@github.com:SEU-USUARIO/NOME-DO-REPOSITORIO.git   # repositório criado como Private
git push -u origin main
```

Convide os Mestres em *Settings → Collaborators*. Para lançar uma versão (gera os dois pacotes em Releases):

```bash
git tag v1.0.0
git push origin v1.0.0
```

O `pokemon.db` e a pasta `tools/` ficam fora do Git (`.gitignore`).
