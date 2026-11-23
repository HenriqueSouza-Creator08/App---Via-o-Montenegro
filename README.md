# Montenegro Escalas (Android)

Aplicativo Android nativo em **Kotlin + Jetpack Compose (Material Design 3)** para o Portal Operacional da **Viação Montenegro**, reescrito a partir do projeto web original preservando todas as funcionalidades e regras de negócio.

## Funcionalidades Portadas
- **Autenticação**: Perfis de **Motorista** (consulta na tabela `motoristas`) e **Administrador** (`.admin@montenegro.com`), opção "Lembrar de mim" via DataStore e tela de transição animada.
- **Início (Painel Operacional)**: Saudação dinâmica, data por extenso, resumo de escalas de hoje, último ponto / pontos de hoje, mural de avisos, contadores da frota/equipe e formulário administrativo de publicação de escalas por período com validação de conflitos.
- **Escalas da Semana**: Grade semanal horizontal com destaque para o dia atual e motorista logado, navegação entre semanas, cópia da semana passada, detecção de conflitos de motorista e faixa de carros/prefixos (`04430 ao 04435`) e adição rápida na grade.
- **Equipe & Frota**: Cadastro e listagem pesquisável de motoristas (com carregamento sob demanda das fotos) e veículos.
- **Comunicados & Ocorrências**: Publicação de avisos com contador de não lidos na Central de Notificações, registro de ocorrências operacionais e justificativas de falta.
- **Ponto, Prints & Relatórios**: Relógio em tempo real no fuso oficial de Brasília (`America/Sao_Paulo`), registro de ponto com animação do crachá no leitor + vibração + confetes, envio de prints por estágio da viagem e relatórios com cálculo automático de faturamento (`R$ 5,40` inteira / `R$ 1,50` meia) usando o Android Photo Picker.
- **Contabilidade & Montenegro Wrapped**: Totais acumulados, média mensal, projeção anual (12 meses), quilometragem rodada, motorista destaque e celebração com confetes.
