<div class="osscmnrdr oss-login-div">
    <fieldset>
        <legend>Please, login</legend>
        <br/>
        <form method="post" action="renderer">
            <table>
                <tr>
                    <td align="right"><label for="username">Username: </label></td>
                    <td><input type="text" name="username" id="username" size="40"/></td>
                </tr>
                <tr>
                    <td align="right"><label for="password">Password: </label></td>
                    <td><input type="password" name="password" id="password"
                               size="40"/></td>
                </tr>
                <tr>
                    <td/>
                    <td><input type="submit" value="Login"/></td>
                </tr>
            </table>
        <#list hiddenParameterList as p>
            <#assign v = request.getParameter(p)!/>
            <#if v?has_content>
                <input type="hidden" name="${p}" value="${v}"/>
            </#if>
        </#list>
        </form>
    </fieldset>
<#if error?has_content>
    <div id="osscmnrdr oss-error">${error}</div>
</#if>
</div>