<#-- Tela de login: mesmos campos e textos da antiga tela do app (components/auth/login).
     Os nomes dos campos (username, password, credentialId, login) sao os que o Keycloak espera. -->
<#import "template.ftl" as layout>
<#import "passkeys.ftl" as passkeys>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=true; section>
    <#if section = "kicker">
        <span class="label-caps">${msg("fcBemVindo")}</span>
    <#elseif section = "header">
        ${msg("loginAccountTitle")}
    <#elseif section = "form">
        <#if realm.password>
            <form id="kc-form-login" class="fc-form" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post" novalidate>
                <#assign erroLogin = messagesPerField.existsError('username','password')>

                <#if !usernameHidden??>
                    <div class="fc-campo">
                        <input id="username" class="fc-input" name="username" value="${(login.username!'')}" type="email"
                               placeholder=" " autofocus maxlength="120"
                               autocomplete="${(enableWebAuthnConditionalUI?has_content)?then('username webauthn', 'username')}"
                               aria-invalid="<#if erroLogin>true</#if>" dir="ltr" />
                        <label for="username" class="fc-label">${msg("email")}<span class="obrigatorio">*</span></label>
                    </div>
                </#if>

                <div class="fc-campo">
                    <div class="fc-input-grupo" dir="ltr">
                        <input id="password" class="fc-input" name="password" type="password" placeholder=" "
                               autocomplete="current-password" aria-invalid="<#if erroLogin>true</#if>" />
                        <label for="password" class="fc-label">${msg("password")}<span class="obrigatorio">*</span></label>
                        <button class="fc-botao-olho" type="button" aria-label="${msg("showPassword")}"
                                aria-controls="password" data-password-toggle
                                data-icon-show="fc-icone fc-icone-olho" data-icon-hide="fc-icone fc-icone-olho-fechado"
                                data-label-show="${msg('showPassword')}" data-label-hide="${msg('hidePassword')}">
                            <i class="fc-icone fc-icone-olho" aria-hidden="true"></i>
                        </button>
                    </div>
                </div>

                <#if erroLogin>
                    <div id="input-error" class="mensagem callout callout-erro" role="alert">
                        <span class="fc-icone fc-icone-erro"></span>
                        <span>${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}</span>
                    </div>
                </#if>

                <#if realm.resetPasswordAllowed>
                    <div class="fc-opcoes">
                        <a href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a>
                    </div>
                </#if>

                <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                <button class="fc-botao fc-botao-primario fc-botao-bloco botao-entrar" name="login" id="kc-login" type="submit">
                    <span class="fc-icone fc-icone-entrar"></span>
                    ${msg("doLogIn")}
                </button>
            </form>
        </#if>
        <@passkeys.conditionalUIData />
        <script type="module" src="${url.resourcesPath}/js/passwordVisibility.js"></script>
    <#elseif section = "info">
        <#-- o cadastro continua no app (tela Criar conta); o link so aparece vindo do front -->
        <#if client?? && client.clientId == "fincontrol-frontend" && (client.baseUrl)?has_content>
            <p>${msg("fcSemConta")} <a href="${client.baseUrl}registro">${msg("fcCriarConta")}</a></p>
        </#if>
    </#if>
</@layout.registrationLayout>
