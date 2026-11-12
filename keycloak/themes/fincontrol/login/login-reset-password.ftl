<#-- Esqueci minha senha: pede o e-mail e o Keycloak envia o link (no dev, chega no Mailpit) -->
<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username') displayInfo=true; section>
    <#if section = "kicker">
        <span class="label-caps">${msg("fcRecuperacao")}</span>
    <#elseif section = "header">
        ${msg("emailForgotTitle")}
    <#elseif section = "form">
        <p class="instruction">${msg("emailInstruction")}</p>
        <form id="kc-reset-password-form" class="fc-form" action="${url.loginAction}" method="post" novalidate>
            <div class="fc-campo">
                <input type="email" id="username" name="username" class="fc-input" placeholder=" " autofocus maxlength="120"
                       value="${(auth.attemptedUsername!'')}" autocomplete="username"
                       aria-invalid="<#if messagesPerField.existsError('username')>true</#if>" dir="ltr" />
                <label for="username" class="fc-label">${msg("email")}<span class="obrigatorio">*</span></label>
                <#if messagesPerField.existsError('username')>
                    <span id="input-error-username" class="fc-erro-campo" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('username'))?no_esc}
                    </span>
                </#if>
            </div>
            <button class="fc-botao fc-botao-primario fc-botao-bloco" type="submit">${msg("doSubmit")}</button>
        </form>
    <#elseif section = "info">
        <p><a href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></p>
    </#if>
</@layout.registrationLayout>
