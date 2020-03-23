/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.micro.prov.jms;

import java.util.Locale;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.shiro.subject.Subject;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.cfg.CfgBoolean;
import de.mhus.lib.core.security.AaaUtil;
import de.mhus.lib.core.shiro.ShiroSecurity;
import de.mhus.lib.core.shiro.ShiroUtil;
import de.mhus.lib.core.shiro.SubjectEnvironment;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.jms.CallContext;
import de.mhus.lib.jms.JmsInterceptor;
import de.mhus.micro.core.api.JmsApi;

public class TicketAccessInterceptor extends MLog implements JmsInterceptor {

    //	public static final CfgString TICKET_KEY = new CfgString(JmsApi.class, "aaaTicketParameter",
    // "mhus.ticket");
    //	public static final CfgString LOCALE_KEY = new CfgString(JmsApi.class, "aaaLocaleParameter",
    // "mhus.locale");
    public static final CfgBoolean RELAXED = new CfgBoolean(JmsApi.class, "aaaRelaxed", true);

    @Override
    public void begin(CallContext callContext) {
        String ticket;
        try {
            ticket = callContext.getMessage().getStringProperty(JmsApi.PARAM_AAA_TICKET);
        } catch (JMSException e) {
            throw new MRuntimeException(e);
        }
        Subject subject = M.l(ShiroSecurity.class).createSubject();
        AaaUtil.login(subject, ticket);
        try {
            String localeStr = callContext.getMessage().getStringProperty(JmsApi.PARAM_LOCALE);
            if (localeStr != null) {
                ShiroUtil.setLocale(subject, localeStr);
            }
        } catch (Throwable t) {
            log().d("Incoming Access Denied", callContext.getMessage());
            throw new RuntimeException(t);
        }
        SubjectEnvironment env = ShiroUtil.useSubject(subject);
        callContext.getProperties().put(TicketAccessInterceptor.class.getCanonicalName(), env);
    }

    @Override
    public void end(CallContext callContext) {

        SubjectEnvironment env = (SubjectEnvironment) callContext.getProperties().get(TicketAccessInterceptor.class.getCanonicalName());
        if (env == null) return;

        env.close();

    }

    @Override
    public void prepare(Message message) {

        Subject subject = ShiroUtil.getSubject();

        String ticket = AaaUtil.createTrustTicket(JmsApi.TRUST_NAME.value(), subject);
        Locale locale = ShiroUtil.getLocale(subject);
        try {
            message.setStringProperty(JmsApi.PARAM_AAA_TICKET, ticket);
            message.setStringProperty(JmsApi.PARAM_LOCALE, locale.toString());
        } catch (JMSException e) {
            throw new MRuntimeException(e);
        }
    }

    @Override
    public void answer(Message message) {}
}
