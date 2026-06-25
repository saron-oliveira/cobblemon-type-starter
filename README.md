# Cobblemon Type Starter

Mod para Fabric 1.21.1 que adiciona uma tela de escolha de tipo antes da escolha do Pokemon inicial do Cobblemon. Feito para campeonatos onde cada jogador representa um tipo diferente.

---

## Requisitos para RODAR (importante!)

Este mod **não funciona sozinho**. Ele precisa do **Fabric Loader** e dos mods abaixo
instalados juntos, todos para **Minecraft 1.21.1**. Se algum faltar, o Minecraft mostra
erro de dependência e parece que "o mod não é válido".

Coloque na pasta `mods` (do servidor **e** de cada cliente):

| Mod | Onde baixar |
|---|---|
| **Fabric Loader 1.21.1** | https://fabricmc.net/use/installer/ (instale a versão 1.21.1) |
| **Fabric API** (para 1.21.1) | https://modrinth.com/mod/fabric-api/versions |
| **Fabric Language Kotlin** | https://modrinth.com/mod/fabric-language-kotlin/versions |
| **Cobblemon 1.7.3** (Fabric, 1.21.1) | https://modrinth.com/mod/cobblemon/versions |
| **Cobblemon Type Starter** (este mod) | `build/libs/cobblemon-type-starter-1.0.0.jar` |

> O Cobblemon é escrito em Kotlin, por isso o **Fabric Language Kotlin** é obrigatório —
> é a causa mais comum de "mod não reconhecido".
>
> Use sempre o `.jar` **sem** o `-sources` no nome. O arquivo `-sources.jar` é só o
> código-fonte e o Minecraft o rejeita como mod inválido.

---

## Como funciona

1. Quando um jogador entra no servidor pela primeira vez, aparece uma tela com os 18 tipos.
2. O jogador escolhe um tipo — se já estiver pego, aparece mensagem de erro e ele deve escolher outro.
3. Após confirmar o tipo, aparece a tela com o(s) Pokemon disponíveis daquele tipo.
4. O jogador recebe o Pokemon no nível 5.
5. O tipo escolhido aparece ao lado do nome do jogador na lista TAB: `Steve [Fogo]`

---

## Tipos e Pokemon iniciais

| Tipo      | Pokemon     |
|-----------|-------------|
| Fogo      | Charmander / Cyndaquil / Torchic |
| Água      | Squirtle / Totodile / Mudkip |
| Planta    | Bulbasaur / Chikorita / Treecko |
| Normal    | Eevee |
| Elétrico  | Pikachu |
| Gelo      | Frigibax |
| Pedra     | Larvitar |
| Terra     | Sandile |
| Voador    | Rookidee |
| Veneno    | Gastly |
| Inseto    | Scyther |
| Lutador   | Machop |
| Psíquico  | Abra |
| Fantasma  | Litwick |
| Dragão    | Axew |
| Sombrio   | Pawniard |
| Aço       | Aron |
| Fada      | Ralts |

---

## Comandos Admin (requer op nível 2)

| Comando | O que faz |
|---|---|
| `/typestart list` | Lista todos os tipos já escolhidos e por quem |
| `/typestart reset all` | Reseta tudo (para iniciar novo campeonato) |
| `/typestart reset player <nome>` | Reseta a escolha de um jogador específico |
| `/typestart reopen <nome>` | Reabre a tela de escolha para um jogador |

---

## Como compilar

### Requisitos
- **JDK 21** instalado (Eclipse Temurin / Adoptium é uma boa opção)
- IntelliJ IDEA (recomendado, mas não obrigatório)

> O projeto é fixado no **Java 21**. Mesmo que o Java padrão do seu PC seja outro
> (ex: Java 24), o Gradle seleciona o 21 sozinho (e baixa se não tiver). Não precisa
> mexer em `JAVA_HOME`.

### Passos

1. Clone ou baixe este projeto
2. Abra o IntelliJ IDEA e selecione "Open" apontando para a pasta do projeto
3. Aguarde o Gradle baixar as dependências (pode demorar na primeira vez)
4. Execute o comando Gradle:
   - No IntelliJ: painel Gradle (direita) > Tasks > build > build
   - Ou no terminal: `./gradlew build` (Windows: `gradlew.bat build`)
5. O arquivo `.jar` estará em `build/libs/cobblemon-type-starter-1.0.0.jar`
6. Coloque esse `.jar` na pasta `mods` junto com o Cobblemon e as demais dependências
   (veja a seção **Requisitos para RODAR** no topo)

---

## Onde os dados ficam salvos

Os dados de quem escolheu qual tipo ficam em:
`(pasta do mundo)/typestart_data.json`

Para fazer backup do campeonato, basta guardar esse arquivo.

---

## Como mudar os Pokemon de cada tipo

Edite o arquivo:
`src/main/java/com/typestart/data/StarterTypes.java`

O mapa `TYPE_STARTERS`, cada linha é um tipo com a lista de Pokemon.
Os nomes dos Pokemon devem ser os nomes internos do Cobblemon em minúsculas (ex: `"charizard"`, `"gengar"`).
