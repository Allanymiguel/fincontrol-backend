<#-- Layout de todas as telas do login: marca a esquerda, formulario a direita (como o .auth do app).
     As telas preenchem as secoes "header" (titulo), "form", "info" e, opcionalmente, "kicker"
     (sobretitulo em caixa alta). Os scripts do <head> sao os mesmos do tema base. -->
<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html lang="${lang}">

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <meta name="robots" content="noindex, nofollow" />
    <title>${msg("loginTitle",(realm.displayName!''))}</title>
    <link rel="icon" type="image/svg+xml" href="${url.resourcesPath}/img/favicon.svg" />
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet" />
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <script type="importmap">
        {
            "imports": {
                "rfc4648": "${url.resourcesCommonPath}/vendor/rfc4648/rfc4648.js"
            }
        }
    </script>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <script type="module">
        import { startSessionPolling } from "${url.resourcesPath}/js/authChecker.js";

        startSessionPolling(
            "${url.ssoLoginInOtherTabsUrl?no_esc}"
        );
    </script>
    <script type="module">
        document.addEventListener("click", (event) => {
            const link = event.target.closest("a[data-once-link]");

            if (!link) {
                return;
            }

            if (link.getAttribute("aria-disabled") === "true") {
                event.preventDefault();
                return;
            }

            const { disabledClass } = link.dataset;

            if (disabledClass) {
                link.classList.add(...disabledClass.trim().split(/\s+/));
            }

            link.setAttribute("role", "link");
            link.setAttribute("aria-disabled", "true");
        });
    </script>
    <#if authenticationSession??>
        <script type="module">
            import { checkAuthSession } from "${url.resourcesPath}/js/authChecker.js";

            checkAuthSession(
                "${authenticationSession.authSessionIdHash}"
            );
        </script>
    </#if>
</head>

<body data-page-id="login-${pageId}">
<div class="auth">
    <aside class="auth-marca" aria-hidden="true">
        <#-- simbolo do FinControl (o mesmo do componente app-logo) -->
        <svg class="auth-logo" viewBox="23 10 90 90" focusable="false">
            <path class="fatia-maior" d="M60,60 L62.51,12.07 A48,48 0 0,1 102.77,81.79 Z" />
            <path class="fatia-media" d="M60,60 L91.87,80.70 A38,38 0 0,1 28.13,80.70 Z" />
            <path class="fatia-menor" d="M60,60 L35.05,72.71 A28,28 0 0,1 58.54,32.04 Z" />
        </svg>
        <div>
            <p class="auth-titulo"><strong>Fin</strong>Control</p>
            <p class="auth-frase">${msg("fcFrase")}</p>
        </div>
        <ul class="auth-itens">
            <li><span class="fc-icone fc-icone-escopo"></span> ${msg("fcItemEscopo")}</li>
            <li><span class="fc-icone fc-icone-mei"></span> ${msg("fcItemMei")}</li>
            <li><span class="fc-icone fc-icone-recorrencia"></span> ${msg("fcItemRecorrencia")}</li>
        </ul>
    </aside>

    <main class="auth-conteudo">
        <div class="auth-card">
            <#nested "kicker">
            <h1 class="titulo" id="kc-page-title"><#nested "header"></h1>

            <#-- depois de digitar o e-mail (ex.: nova senha), mostra com quem esta entrando -->
            <#if auth?has_content && auth.showUsername() && !auth.showResetCredentials()>
                <#nested "show-username">
                <div id="kc-username" class="fc-usuario">
                    <span id="kc-attempted-username">${auth.attemptedUsername}</span>
                    <a id="reset-login" href="${url.loginRestartFlowUrl}">${msg("fcTrocarConta")}</a>
                </div>
            </#if>

            <#-- acoes iniciadas pelo app nao mostram o aviso de que a acao precisa ser concluida -->
            <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                <#assign tipoAviso = {"error": "callout-erro", "warning": "callout-alerta", "success": "callout-sucesso", "info": ""}>
                <#assign iconeAviso = {"error": "erro", "warning": "alerta", "success": "sucesso", "info": "info"}>
                <div class="mensagem callout ${tipoAviso[message.type]!''}" role="<#if message.type = 'error'>alert<#else>status</#if>">
                    <span class="fc-icone fc-icone-${iconeAviso[message.type]!'info'}"></span>
                    <span>${kcSanitize(message.summary)?no_esc}</span>
                </div>
            </#if>

            <#nested "form">

            <#if auth?has_content && auth.showTryAnotherWayLink()>
                <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
                    <div class="fc-campo">
                        <input type="hidden" name="tryAnotherWay" value="on"/>
                        <a href="#" id="try-another-way"
                           onclick="document.forms['kc-select-try-another-way-form'].requestSubmit();return false;">${msg("doTryAnotherWay")}</a>
                    </div>
                </form>
            </#if>

            <#nested "socialProviders">

            <#if displayInfo>
                <div id="kc-info" class="rodape">
                    <#nested "info">
                </div>
            </#if>
        </div>
    </main>
</div>
</body>
</html>
</#macro>
