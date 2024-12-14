# Avatar-daystohorders             
## project structure
```                    
avatar-daystohorders/
    README.md
    mine.xlsx
    build.gradle
    LICENSE.txt
    gradlew
    changelog.txt
    settings.gradle
    CREDITS.txt
    gradle.properties
    gradlew.bat
    src/
        main/
            java/
                com/
                    avatar/
                        avatar_daystohorders/
                            GlobalConfig.java
                            Main.java
                            server/
                                Events.java
                                ServerConfig.java
                            animation/
                                Animate.java
                            Client/
                                StatusBarRenderer.java
                            function/
                                MobBlockPlaceHandler.java
                                MobSpawnHandler.java
                                PortalSpawnHandler.java
                                MobTeleport.java
                                MobCreate.java
                            object/
                                MobWaveDescripton.java
                            network/
                                StatusUpdatePacket.java
            resources/
                pack.mcmeta
                META-INF/
                    mods.toml
    gradle/
        wrapper/
            gradle-wrapper.jar
            gradle-wrapper.properties                
```
## Resumo do Projeto Minecraft Forge

Este projeto abrange diversos componentes de um mod para Minecraft utilizando a API Minecraft Forge.  Os componentes incluem:  um sistema de ondas de monstros com configuração persistente, um gerenciador de portal com efeitos visuais, um sistema de atualização de status do jogador via rede,  e mecanismos para teletransporte de monstros e colocação automática de blocos. O código utiliza Forge Config Spec para gerenciamento de configurações, `FriendlyByteBuf` para comunicação em rede e diversas classes para lidar com eventos de jogo e manipulação de entidades.  O projeto enfatiza a persistência de dados entre sessões de jogo e a comunicação eficiente entre o cliente e o servidor.

## Propósito e Descrição do Projeto

Este projeto é um mod para Minecraft que adiciona diversas funcionalidades, incluindo um sistema de ondas de monstros, um portal personalizado com efeitos visuais, um sistema de atualização de status para o jogador, e mecanismos de teletransporte de monstros e colocação automática de blocos.  O mod utiliza a API Minecraft Forge e é focado em fornecer uma experiência de jogo dinâmica e envolvente.

## Dependências

* Forge MDK (Minecraft Development Kit)
* Java Development Kit (JDK)
* Gradle (ou outro gerenciador de dependências compatível com Forge)
* Bibliotecas necessárias da API Minecraft Forge


## Como Instalar

1. Clonar este repositório.
2. Instalar as dependências usando Gradle (ou outro gerenciador de dependências).
3. Configurar as configurações do mod (se necessário).
4. Compilar e instalar o mod no Minecraft.

## Como Usar

O mod adiciona novas funcionalidades ao jogo que serão ativadas automaticamente após a instalação. A interação específica com cada funcionalidade dependerá da implementação detalhada em cada classe. As configurações do mod podem ser alteradas através de um arquivo de configuração.

## Arquitetura

O mod é baseado em um modelo de eventos, utilizando o barramento de eventos da Minecraft Forge.  Ele utiliza múltiplas classes para lidar com diferentes aspectos do jogo, incluindo a geração de monstros, a criação do portal, a comunicação cliente-servidor e a renderização da interface do usuário.  O gerenciamento de configurações é feito através de arquivos de configuração gerenciados pela API Forge Config Spec.

## Pipeline

O pipeline do mod envolve a seguinte sequência de eventos:

1. **Inicialização:** Carregamento das configurações e registro dos eventos.
2. **Eventos do jogo:**  Eventos de jogo acionam o processamento de diferentes ações, como o surgimento de ondas de monstros, a criação do portal, e a atualização do status do jogador.
3. **Comunicação em rede:**  Atualizações de status são enviadas através de pacotes de rede.
4. **Renderização:** A interface do usuário é atualizada para refletir as mudanças no jogo.
5. **Persistência de dados:** Os dados relevantes são salvos em arquivos de configuração para persistência entre sessões do jogo.

                
                