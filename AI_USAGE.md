# AI Usage

## 1. Purpose

Este arquivo é o resumo executivo do uso de IA no projeto. O historico detalhado, cronologico e auditavel pode ser acessado em `AI_USAGE_LOGS.md`.

## 2. Ferramentas usadas

* `ChatGPT` para interpretacao do desafio, planejamento inicial, estrutura de specs/ADRs e primeiros rascunhos documentais.
* `Codex` para implementacoes pequenas e guiadas por prompt, geracao e ajuste de testes, revisoes de consistencia, validacao de runtime e consolidacao final da documentacao.

## 3. Prompts estrategicos utilizados

Os prompts que mais aceleraram o projeto foram:

* prompts de estruturacao inicial: bootstrap, backend scaffold, database migrations e roadmap SDD;
* prompts de dominio critico: currency engine, pricing engine, settlement flow, settlement detail e statement query;
* prompts de UX operacional: frontend simulation, frontend statement grid, exchange rates e settlement UI;
* prompts de entrega: docker/delivery, startup test data seed e final review.

Em termos praticos, a utilização de IA ajudou mais onde havia muito trabalho repetitivo, como:

* scaffolding de backend e frontend;
* geracao de testes de contrato e integracao;
* massa inicial controlada para facilitar validacao manual;
* revisao de consistencia entre README, specs, ADRs, prompts e implementacao.

## 4. Onde a IA alucinou ou gerou risco

Os problemas mais recorrentes nao foram "inventar features", mas sim sugerir atalhos errados ou incompletos que precisaram de correcao manual:

* em frontend, houve erros de tipagem e ordem de inicializacao que quebravam compilacao ou testes;
* em testes, algumas assercoes iniciais miravam cenarios invalidos do ponto de vista do contrato e precisaram ser reescritas;
* em dados de seed, apareceu a tentacao de inferir estados por caminhos indiretos em vez de usar os repositorios corretos;
* em documentacao final, havia risco de afirmar sucesso de runtime antes de validar Docker e endpoints de verdade;
* na consolidacao final, a simples renumeracao dos prompts quebraria o caminho do prompt de review se executada de forma literal e sem interpretacao.

As correcoes aplicadas seguiram sempre o mesmo criterio:

* voltar para specs e ADRs;
* checar o diff manualmente;
* executar build, testes e smoke checks reais;
* rejeitar qualquer atalho que ameacasse precisao financeira, atomicidade, auditabilidade ou rastreabilidade.

## 5. Analise critica

### Onde a IA economizou tempo

Ela foi muito eficiente para:

* transformar requisitos dispersos em documentacao estruturada;
* acelerar scaffolding e codigo repetitivo;
* expandir cobertura de testes sem partir do zero;
* localizar drift entre especificacao, implementacao e README;
* fazer a revisao final com uma checklist ampla e disciplinada.

### Onde a IA atrapalhou

Ela atrapalhou quando a primeira resposta parecia "boa o suficiente", mas escondia algum detalhe importante:

* tipagem frouxa no frontend;
* cenarios de teste mal escolhidos;
* referencias documentais desatualizadas;
* risco de aceitar conteúdo rico sem validacao real de runtime.

Em resumo: a IA acelerou muito o trabalho mecanico e a navegacao no repositorio, mas exigiu vigilancia constante justamente nas partes mais sensiveis.

## 6. Regra de uso e ownership

Toda saida de IA foi tratada como rascunho. Nenhuma logica financeira, decisao arquitetural ou afirmacao de prontidao foi aceita sem revisao e validacao do autor.

O ownership final permanece humano:

* corretude das regras de negocio;
* precisao monetaria;
* consistencia transacional;
* cobertura de testes;
* qualidade da documentacao;
* coerencia do historico Git;
* avaliacao critica do que a IA ajudou e do que ela piorou.

## 7. Referencia detalhada

Para evidencias completas de cada interacao material com IA, consulte `AI_USAGE_LOGS.md`.
